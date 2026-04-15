package fr.wdes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.wdes.authentication.AuthenticationService;
import fr.wdes.download.DownloadJob;
import fr.wdes.download.DownloadListener;
import fr.wdes.download.Downloadable;
import fr.wdes.download.assets.AssetIndex;
import fr.wdes.process.JavaProcess;
import fr.wdes.process.JavaProcessLauncher;
import fr.wdes.process.JavaProcessRunnable;
import fr.wdes.profile.LauncherVisibilityRule;
import fr.wdes.profile.Profile;
import fr.wdes.ui.lite.Compatibility;
import fr.wdes.updater.DateTypeAdapter;
import fr.wdes.updater.LocalVersionList;
import fr.wdes.updater.VersionList;
import fr.wdes.updater.VersionSyncInfo;
import fr.wdes.versions.CompleteVersion;
import fr.wdes.versions.ExtractRules;
import fr.wdes.versions.Library;



public class GameLauncher implements JavaProcessRunnable, DownloadListener {
    private final Object lock = new Object();
    private final Launcher launcher;
    private final List<DownloadJob> jobs = new ArrayList<DownloadJob>();
    private CompleteVersion version;
    private LauncherVisibilityRule visibilityRule;
    private boolean isWorking;
    /**
     * Set by {@link #playGame()} once both the version+libraries and resources
     * download jobs are queued, cleared as soon as {@link #launchGame()} is
     * dispatched. We need this in addition to {@link #isWorking} because the
     * launcher's own background fonds download also flows through
     * {@link #onDownloadJobFinished} - without this gate, the fonds job
     * completing in the small window between {@code setWorking(true)} and the
     * play-sequence's {@code addJob} calls would trip {@code !hasRemainingJobs}
     * and kick off the game before its own download jobs were even queued.
     */
    private boolean launchPending;
    private File nativeDir;
    private static final String CRASH_IDENTIFIER_MAGIC = "#@!@#";
    protected final Gson gson = new Gson();
    private final DateTypeAdapter dateAdapter = new DateTypeAdapter();
    public GameLauncher(final Launcher launcher) {
        this.launcher = launcher;
    }

    public void addJob(final DownloadJob job) {
        synchronized(lock) {
            jobs.add(job);
        }
    }

    private String constructClassPath(final CompleteVersion version) {
        final StringBuilder result = new StringBuilder();
        final Collection<File> classPath = version.getClassPath(OperatingSystem.getCurrentPlatform(), launcher.getWorkingDirectory());
        final String separator = System.getProperty("path.separator");

        for(final File file : classPath) {
            if(!file.isFile())
                throw new RuntimeException("Classpath file not found: " + file);
            if(result.length() > 0)
                result.append(separator);
            result.append(file.getAbsolutePath());
        }

        return result.toString();
    }

    private String[] getMinecraftArguments(final CompleteVersion version, final Profile selectedProfile, final File gameDirectory, final File assetsDirectory, final AuthenticationService authentication) {
        final boolean hasLegacy = version.getMinecraftArguments() != null;
        final boolean hasModern = version.getArguments() != null && version.getArguments().has("game");
        if (!hasLegacy && !hasModern) {
            logger.warn("Can't run version, missing minecraftArguments and arguments.game");
            setWorking(false);
            return null;
        }

        final Map<String, String> map = buildPlaceholderMap(version, selectedProfile, gameDirectory, assetsDirectory, authentication);
        final StrSubstitutor substitutor = new StrSubstitutor(map);

        final String[] split;
        if (hasLegacy) {
            split = version.getMinecraftArguments().split(" ");
        } else {
            // Minecraft 1.13+: arguments.game is a mixed array of strings and
            // conditional objects ({rules, value}). Plain strings are
            // always emitted; conditional entries are only included when
            // their feature toggles match the current launch context (is
            // the user in demo mode, has a custom resolution been set...).
            final java.util.List<String> collected = new java.util.ArrayList<String>();
            final com.google.gson.JsonElement gameArr = version.getArguments().get("game");
            if (gameArr != null && gameArr.isJsonArray()) {
                for (com.google.gson.JsonElement el : gameArr.getAsJsonArray()) {
                    if (el.isJsonPrimitive()) {
                        collected.add(el.getAsString());
                    } else if (el.isJsonObject()) {
                        final com.google.gson.JsonObject obj = el.getAsJsonObject();
                        if (rulesAllow(obj.getAsJsonArray("rules"), selectedProfile, authentication)) {
                            final com.google.gson.JsonElement value = obj.get("value");
                            if (value == null) {
                                continue;
                            } else if (value.isJsonPrimitive()) {
                                collected.add(value.getAsString());
                            } else if (value.isJsonArray()) {
                                for (com.google.gson.JsonElement v : value.getAsJsonArray()) {
                                    if (v.isJsonPrimitive()) collected.add(v.getAsString());
                                }
                            }
                        }
                    }
                }
            }
            split = collected.toArray(new String[collected.size()]);
        }
        for (int i = 0; i < split.length; i++) {
            split[i] = substitutor.replace(split[i]);
        }
        return split;
    }

    /** Collate every placeholder the version JSON might reference. */
    private Map<String, String> buildPlaceholderMap(final CompleteVersion version, final Profile selectedProfile, final File gameDirectory, final File assetsDirectory, final AuthenticationService authentication) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("auth_access_token", authentication.getSessionToken());
        // Minecraft 1.7+ deserialises --userProperties / --userProperty_map
        // with Gson.fromJson(arg, Map.class) and then immediately calls
        // entrySet() on the result. If we let Gson.toJson(null) produce the
        // literal "null" string, the deserialisation returns null and the
        // game crashes with NullPointerException at Main.main(SourceFile:116).
        // Always emit an empty JSON object when there are no properties to
        // ship.
        final com.google.gson.JsonElement userProps = authentication.getUserProperties();
        map.put("user_properties", userProps == null
            ? "{}"
            : new GsonBuilder().registerTypeAdapter(PropertyMap.class, new LegacyPropertyMapSerializer()).create().toJson(userProps));
        map.put("user_property_map", userProps == null
            ? "{}"
            : new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(userProps));

        map.put("auth_username", authentication.getUsername());
        map.put("auth_session", authentication.getSessionToken() == null && authentication.canPlayOnline() ? "-" : authentication.getSessionToken());

        if(authentication.getSelectedProfile() != null) {
            map.put("auth_player_name", authentication.getUsername());
            map.put("auth_uuid", authentication.getSelectedProfile().getId());
            //LEGACY("legacy"),  MOJANG("mojang");
            map.put("user_type", "mojang");
        }
        else {
            map.put("auth_player_name", "Player");
            map.put("auth_uuid", new UUID(0L, 0L).toString());
            map.put("user_type", "legacy");
        }

        map.put("profile_name", selectedProfile.getName());
        map.put("version_name", version.getId());

        map.put("game_directory", gameDirectory.getAbsolutePath());
        map.put("game_assets", assetsDirectory.getAbsolutePath());
        map.put("assets_root", new File(gameDirectory.getAbsolutePath(), "assets").getAbsolutePath());
        map.put("assets_index_name", version.getAssets() == null ? "legacy" : version.getAssets());

        // 1.13+ additions.
        map.put("version_type", version.getType() == null ? "release" : version.getType().getName());
        // Microsoft auth fields - empty for the legacy / offline flow,
        // present so any unconditional ${clientid} / ${auth_xuid} tokens
        // in the version JSON don't leak literal ${...} into the argv.
        map.put("clientid", "");
        map.put("auth_xuid", "");
        // Natives / classpath - populated per-launch below, but also
        // exposed as placeholders so version JSONs that mention them in
        // arguments.jvm (not yet consumed here) don't fail to substitute.
        if (nativeDir != null) {
            map.put("natives_directory", nativeDir.getAbsolutePath());
        }
        map.put("launcher_name", "wdes-launcher");
        map.put("launcher_version", "2.0.0");

        final Profile.Resolution res = selectedProfile.getResolution();
        if (res != null) {
            map.put("resolution_width",  String.valueOf(res.getWidth()));
            map.put("resolution_height", String.valueOf(res.getHeight()));
        }
        return map;
    }

    /**
     * Evaluate a 1.13+ {@code rules} array for an {@code arguments.game}
     * conditional entry. Returns true if the entry should be included.
     * Keeps the implementation tiny - only the two {@code features} we
     * actually care about: demo mode and custom resolution.
     */
    private boolean rulesAllow(final com.google.gson.JsonArray rules, final Profile profile, final AuthenticationService authentication) {
        if (rules == null || rules.size() == 0) {
            return true;
        }
        boolean allow = false;
        for (com.google.gson.JsonElement el : rules) {
            if (!el.isJsonObject()) continue;
            final com.google.gson.JsonObject rule = el.getAsJsonObject();
            final String action = rule.has("action") ? rule.get("action").getAsString() : "allow";
            final boolean matches;
            if (rule.has("features") && rule.get("features").isJsonObject()) {
                matches = featuresMatch(rule.getAsJsonObject("features"), profile, authentication);
            } else {
                // OS rules (os.name = windows / linux / osx) - evaluate
                // for completeness so the JVM block, if ever consumed, works.
                matches = rule.has("os") ? osRuleMatches(rule.getAsJsonObject("os")) : true;
            }
            if (matches) {
                allow = "allow".equalsIgnoreCase(action);
            }
        }
        return allow;
    }

    private boolean featuresMatch(final com.google.gson.JsonObject features, final Profile profile, final AuthenticationService authentication) {
        for (java.util.Map.Entry<String, com.google.gson.JsonElement> e : features.entrySet()) {
            final String key = e.getKey();
            final boolean expected = e.getValue().isJsonPrimitive() && e.getValue().getAsBoolean();
            final boolean actual;
            if ("is_demo_user".equals(key)) {
                actual = authentication.getSelectedProfile() == null;
            } else if ("has_custom_resolution".equals(key)) {
                actual = profile.getResolution() != null;
            } else {
                // Unknown feature (has_quick_plays_support, etc.) - treat
                // as false so we don't spuriously include flags we can't
                // honour.
                actual = false;
            }
            if (actual != expected) {
                return false;
            }
        }
        return true;
    }

    private boolean osRuleMatches(final com.google.gson.JsonObject os) {
        if (!os.has("name")) return true;
        final String wanted = os.get("name").getAsString();
        final OperatingSystem current = OperatingSystem.getCurrentPlatform();
        return wanted.equalsIgnoreCase(current.getName());
    }

    protected float getProgress() {
        synchronized(lock) {
            float max = 0.0F;
            float result = 0.0F;

            for(final DownloadJob job : jobs) {
                final float progress = job.getProgress();

                if(progress >= 0.0F) {
                    result += progress;
                    max += 1.0F;
                }
            }

            return result / max;
        }
    }

    public boolean hasRemainingJobs() {
        synchronized(lock) {
            for(final DownloadJob job : jobs)
                if(!job.isComplete())
                    return true;
        }

        return false;
    }

    public boolean isWorking() {
        return isWorking;
    }
    private File reconstructAssets()
    	    throws IOException
    	  {
    	    File assetsDir = new File(launcher.getWorkingDirectory(), "assets");
    	    File indexDir = new File(assetsDir, "indexes");
    	    File objectDir = new File(assetsDir, "objects");
    	    String assetVersion = version.getAssets() == null ? "legacy" : version.getAssets();
    	    File indexFile = new File(indexDir, assetVersion + ".json");
    	    File virtualRoot = new File(new File(assetsDir, "virtual"), assetVersion);
    	    if (!indexFile.isFile())
    	    {
    	    	logger.warn("No assets index file " + virtualRoot + "; can't reconstruct assets");
    	      return virtualRoot;
    	    }
    	    AssetIndex index = (AssetIndex)this.gson.fromJson(FileUtils.readFileToString(indexFile), AssetIndex.class);
    	    if (index.isVirtual())
    	    {
    	    	logger.info("Reconstructing virtual assets folder at " + virtualRoot);
    	      for (Map.Entry<String, AssetIndex.AssetObject> entry : index.getFileMap().entrySet())
    	      {
    	        File target = new File(virtualRoot, (String)entry.getKey());
    	        File original = new File(new File(objectDir, ((AssetIndex.AssetObject)entry.getValue()).getHash().substring(0, 2)), ((AssetIndex.AssetObject)entry.getValue()).getHash());
    	        if (!target.isFile()) {
    	          FileUtils.copyFile(original, target, false);
    	        }
    	      }
    	      FileUtils.writeStringToFile(new File(virtualRoot, ".lastused"), this.dateAdapter.serializeToString(new Date()));
    	    }
    	    return virtualRoot;
    	  }
    private File getAssetObject(String name)
    	    throws IOException
    	  {
    	    File assetsDir = new File(launcher.getWorkingDirectory(), "assets");
    	    File indexDir = new File(assetsDir, "indexes");
    	    File objectsDir = new File(assetsDir, "objects");
    	    String assetVersion = version.getAssets() == null ? "legacy" : version.getAssets();
    	    File indexFile = new File(indexDir, assetVersion + ".json");
    	    AssetIndex index = (AssetIndex)this.gson.fromJson(FileUtils.readFileToString(indexFile), AssetIndex.class);

    	    String hash = ((AssetIndex.AssetObject)index.getFileMap().get(name)).getHash();
    	    return new File(objectsDir, hash.substring(0, 2) + "/" + hash);
    	  }
    protected void launchGame() {
    	logger.info("Démarrage du jeu");
        final Profile selectedProfile = launcher.getProfileManager().getSelectedProfile();

        if(version == null) {
        	logger.warn("Aborting launch; version is null?");
            return;
        }
        launcher.getLauncherPanel().progressBar.setText("Décompression des natives ...");
        nativeDir = new File(launcher.getWorkingDirectory(), "versions/" + version.getId() + "/" + version.getId() + "-natives-" + System.nanoTime());
        if(!nativeDir.isDirectory())
            nativeDir.mkdirs();
        logger.info("Décompression des natives : " + nativeDir);
        try {
            unpackNatives(version, nativeDir);
        }
        catch(final IOException e) {
        	launcher.getLauncherPanel().progressBar.setText("Echec de décompression des natives.");
            logger.warn("Echec de décompression des natives.", e);
            return;
        }

        final File gameDirectory = selectedProfile.getGameDir() == null ? launcher.getWorkingDirectory() : selectedProfile.getGameDir();
        logger.info("Démarrage dans : " + gameDirectory);

        if(!gameDirectory.exists()) {
            if(!gameDirectory.mkdirs())
            	logger.warn("Aborting launch; couldn't create game directory");
        }
        else if(!gameDirectory.isDirectory()) {
        	logger.warn("Aborting launch; game directory is not actually a directory");
            return;
        }

        final JavaProcessLauncher processLauncher = new JavaProcessLauncher(selectedProfile.getJavaPath(), new String[0]);
        processLauncher.directory(gameDirectory);

        File assetsDir;
        try
        {
          launcher.getLauncherPanel().progressBar.setText("Reconstruction des assets ...");
          assetsDir = reconstructAssets();
        }
        catch (IOException e)
        {
        	logger.warn("Impossible de reconstruire les assets !", e);
          return;
        }

        final OperatingSystem os = OperatingSystem.getCurrentPlatform();
        if(os.equals(OperatingSystem.OSX)){
			try {
				processLauncher.addCommands(new String[] { "-Xdock:icon=" + getAssetObject("icons/minecraft.icns").getAbsolutePath(), "-Xdock:name=" + LauncherConstants.SERVER_NAME });
			} catch (IOException e1) {e1.printStackTrace();}
        }
		else if(os.equals(OperatingSystem.WINDOWS)){
            processLauncher.addCommands(new String[] { "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump" });
		}
        final String profileArgs = selectedProfile.getJavaArgs();

        if(profileArgs != null)
            processLauncher.addSplitCommands(profileArgs);
        else {
            final boolean is32Bit = "32".equals(System.getProperty("sun.arch.data.model"));
            final String defaultArgument = is32Bit ? "-Xmx512M" : "-Xmx1G";
            processLauncher.addSplitCommands(defaultArgument);
        }

        // Quiet the in-game "MCO Availability Checker" thread that tries to
        // reach the Realms API every minute. realms-1.3.5.jar (shipped with
        // 1.7.10) can't parse the modern API's response and spams parse
        // errors. -Drealms.disabled=true is a no-op on that old client but
        // is honoured by newer versions, and -Dminecraft.api.env=local is
        // Mojang's official "kill the API" knob - the checker fails fast
        // and quietly instead of looping. Both are harmless for offline
        // play.
        processLauncher.addCommands(new String[] { "-Drealms.disabled=true" });
        processLauncher.addCommands(new String[] { "-Dminecraft.api.env=local" });
        processLauncher.addCommands(new String[] { "-Djava.library.path=" + nativeDir.getAbsolutePath() });
        processLauncher.addCommands(new String[] { "-cp", constructClassPath(version) });
        processLauncher.addCommands(new String[] { version.getMainClass() });

        final AuthenticationService auth = launcher.getProfileManager().getAuthDatabase().getByUUID(selectedProfile.getPlayerUUID());

        final String[] args = getMinecraftArguments(version, selectedProfile, gameDirectory, assetsDir, auth);
        if(args == null)
            return;
        processLauncher.addCommands(args);

        final Proxy proxy = launcher.getProxy();
        final PasswordAuthentication proxyAuth = launcher.getProxyAuth();
        if(!proxy.equals(Proxy.NO_PROXY)) {
            final InetSocketAddress address = (InetSocketAddress) proxy.address();
            processLauncher.addCommands(new String[] { "--proxyHost", address.getHostName() });
            processLauncher.addCommands(new String[] { "--proxyPort", Integer.toString(address.getPort()) });
            if(proxyAuth != null) {
                processLauncher.addCommands(new String[] { "--proxyUser", proxyAuth.getUserName() });
                processLauncher.addCommands(new String[] { "--proxyPass", new String(proxyAuth.getPassword()) });
            }

        }

        processLauncher.addCommands(launcher.getAdditionalArgs());

        if(auth == null || auth.getSelectedProfile() == null)
            processLauncher.addCommands(new String[] { "--demo" });

        if(selectedProfile.getResolution() != null && Launcher.getInstance().config != null) {
            processLauncher.addCommands(new String[] { "--width", String.valueOf(Launcher.getInstance().config.width) });
            processLauncher.addCommands(new String[] { "--height", String.valueOf(Launcher.getInstance().config.height) });
        }
        try {
            final List<String> parts = processLauncher.getFullCommands();
            final StringBuilder full = new StringBuilder();
            boolean first = true;
            for(final String part : parts) {
                if(!first)
                    full.append(" ");
                full.append(part);
                first = false;
            }

        		String [] jars = constructClassPath(version).split(System.getProperty("path.separator"));
        		logger.info(jars.length+" Jars");
            	for(final String part : jars) {
            		logger.info("Part : "+part.replace(Launcher.getInstance().getWorkingDirectory().toString(), ""));
            	}



            logger.info("Démarré " + full.toString());
            launcher.getLauncherPanel().progressBar.setText("Processus démarré !!");
            final JavaProcess process = processLauncher.start();
            process.safeSetExitRunnable(this);

            if(visibilityRule != LauncherVisibilityRule.DO_NOTHING)
                launcher.getFrame().setVisible(false);
        }
        catch(final IOException e) {
        	logger.warn("Impossible de lancer le jeu.", e);
        	launcher.getLauncherPanel().progressBar.setText("Impossible de lancer le jeu.");
            setWorking(false);
            return;
        }
    }

    public void onDownloadJobFinished(final DownloadJob job) {

        synchronized(lock) {
            if(job.getFailures() > 0) {
            	launcher.getLauncherPanel().progressBar.setText("[FAIL] Tâche '" + job.getName() + "' terminée," + job.getFailures() + " echec(s)!");
            	logger.warn("[FAIL] Tâche '" + job.getName() + "' terminée avec : " + job.getFailures() + " echec(s)!");
                launchPending = false;
                setWorking(false);
                return;
            }

            launcher.getLauncherPanel().progressBar.setText("[OK] Tâche '" + job.getName() + "' terminée.");
            logger.info("[OK] Tâche '" + job.getName() + "' terminée.");

            // Once every download for the queued play-sequence is complete,
            // hand off to launchGame. The synchronous !hasRemainingJobs check
            // at the bottom of playGame() doesn't fire because startDownloading
            // is async, so the game would never actually start without this
            // trigger. launchPending ensures we only fire for the play
            // sequence and not for stray background downloads (fonds, etc.).
            if (launchPending && !hasRemainingJobs()) {
                launchPending = false;
                try {
                    launchGame();
                }
                catch(final Throwable ex) {
                	logger.warn("Erreur,merci de contacter le support !!", ex);
                }
            }
        }
    }

    public void onDownloadJobProgressChanged(final DownloadJob job) {
    	launcher.getLauncherPanel().progressBar.setText("Tâche : "+job.getName()+" en cours ...");
    }

    public void onJavaProcessEnded(final JavaProcess process) {
        final int exitCode = process.getExitCode();
        // Always dump everything we know about the exit so a JVM that died
        // within milliseconds of starting leaves an actually useful trace.
        logger.info("===== Game JVM exited =====");
        logger.info("Exit code: " + exitCode);
        logger.info("Command:   " + process.getStartupCommand());
        final java.util.List<String> argv = process.getStartupCommands();
        if (argv != null) {
            final StringBuilder rebuilt = new StringBuilder();
            for (int i = 0; i < argv.size(); i++) {
                if (i > 0) rebuilt.append(' ');
                rebuilt.append(argv.get(i));
            }
            logger.info("Argv:      " + rebuilt.toString());
        }
        final String[] tail = process.getSysOutLines().getItems();
        if (tail.length == 0) {
            logger.warn("No stdout/stderr captured before JVM exited - typically means the binary couldn't start (wrong java path, ELF/PE mismatch, missing libstdc++, killed by SIGKILL, etc).");
        } else {
            for (int i = 0; i < tail.length; i++) {
                if (tail[i] != null) {
                    logger.info("[Minecraft tail] " + tail[i]);
                }
            }
        }
        detectKnownCrashes(tail);
        logger.info("===========================");

        if(exitCode == 0) {
        	launcher.getLauncherPanel().progressBar.setText("[" + exitCode + "] Arrêt du jeu,aucun problème détécté.");
        	logger.info("[" + exitCode + "]Arrêt du jeu,aucun problème détécté.");

            if(visibilityRule == LauncherVisibilityRule.CLOSE_LAUNCHER)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    	logger.info("Arrêt du launcher selon la règle.");
                        launcher.closeLauncher("GameLauncher #378");
                    }
                });
            else if(visibilityRule == LauncherVisibilityRule.HIDE_LAUNCHER)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    	logger.info("Affichage du launcher selon la règle.");
                        launcher.getFrame().setVisible(true);
                    }
                });
        }
        else {
        	launcher.getLauncherPanel().progressBar.setText("[" + exitCode + "] Arrêt du jeu,crash détécté.");
        	logger.warn("[" + exitCode + "]Arrêt du jeu,crash détécté.");

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	logger.info("Règle ignorée car le jeu a crash.");
                    launcher.getFrame().setVisible(true);
                }
            });
            String errorText = null;
            final String[] sysOut = process.getSysOutLines().getItems();

            for(int i = sysOut.length - 1; i >= 0; i--) {
                final String line = sysOut[i];
                final int pos = line.lastIndexOf(CRASH_IDENTIFIER_MAGIC);

                if(pos >= 0 && pos < line.length() - CRASH_IDENTIFIER_MAGIC.length() - 1) {
                    errorText = line.substring(pos + CRASH_IDENTIFIER_MAGIC.length()).trim();
                    break;
                }
            }

            if(errorText != null) {
                final File file = new File(errorText);

                if(file.isFile()) {
                	logger.warn("Crash report detected, opening: " + errorText);
                	Compatibility.open(file);
                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(file);
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        final StringBuilder result = new StringBuilder();
                        String line;
                        while((line = reader.readLine()) != null) {
                            if(result.length() > 0)
                                result.append("\n");
                            result.append(line);
                        }

                        reader.close();
                    }
                    catch(final IOException e) {
                    	logger.warn("Couldn't open crash report", e);
                    }
                    finally {
                        Downloadable.closeSilently(inputStream);
                    }
                }
                else
                	logger.warn("Crash report detected, but unknown format: " + errorText);
            }
        }

        setWorking(false);
    }

    public void playGame() {
    	launcher.getLauncherPanel().progressBar.setVisible(true);
    	launcher.getLauncherPanel().progressBar.setText("Essai de démarrage du jeu !!");
        synchronized(lock) {
            if(isWorking) {
            	logger.info("Le jeu démarre déja !!");
                return;
            }

            setWorking(true);
        }
/*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //launcher.getLauncherPanel().getTabPanel().showConsole();
            }
        });
        */
        logger.info("Chargement des informations de version...");
        Launcher.getInstance().getLauncherPanel().progressBar.setText("Chargement des informations de version...");
        final Profile profile = launcher.getProfileManager().getSelectedProfile();
        final String lastVersionId = profile.getLastVersionId();
        VersionSyncInfo syncInfo = null;

        if(profile.getLauncherVisibilityOnGameClose() == null)
            visibilityRule = Profile.DEFAULT_LAUNCHER_VISIBILITY;
        else
            visibilityRule = profile.getLauncherVisibilityOnGameClose();

        if(lastVersionId != null)
            syncInfo = launcher.getVersionManager().getVersionSyncInfo(lastVersionId);

        if(syncInfo == null || syncInfo.getLatestVersion() == null)
            syncInfo = launcher.getVersionManager().getVersions(profile.getVersionFilter()).get(0);

        if(syncInfo == null) {
        	logger.info("Tried to launch a version without a version being selected...");
            setWorking(false);
            return;
        }

        synchronized(lock) {
        	logger.info("Création les Librairies & Versions");
        	launcher.getLauncherPanel().progressBar.setText("Récupération des informations de version.");
            try {
                version = launcher.getVersionManager().getLatestCompleteVersion(syncInfo);
            }
            catch(final IOException e) {
            	launcher.getLauncherPanel().progressBar.setText("Echec de récupération des informations de version.");
            	logger.warn("Couldn't get complete version info for " + syncInfo.getLatestVersion(), e);
                setWorking(false);
                return;
            }

            if(syncInfo.getRemoteVersion() != null && syncInfo.getLatestSource() != VersionSyncInfo.VersionSource.REMOTE && !version.isSynced()) {
                try {
                    final CompleteVersion remoteVersion = launcher.getVersionManager().getRemoteVersionList().getCompleteVersion(syncInfo.getRemoteVersion());
                    launcher.getVersionManager().getLocalVersionList().removeVersion(version);
                    launcher.getVersionManager().getLocalVersionList().addVersion(remoteVersion);
                    ((LocalVersionList) launcher.getVersionManager().getLocalVersionList()).saveVersion(remoteVersion);
                    version = remoteVersion;
                }
                catch(final IOException e) {
                	launcher.getLauncherPanel().progressBar.setText("Echec de synchronisation des informations de versions.");
                	logger.warn("Couldn't sync local and remote versions", e);
                }
                version.setSynced(true);
            }

            if(!version.appliesToCurrentEnvironment()) {
                String reason = version.getIncompatibilityReason();
                if(reason == null)
                    reason = "This version is incompatible with your computer. Please try another one by going into Edit Profile and selecting one through the dropdown. Sorry!";
                logger.warn("Version " + version.getId() + " is incompatible with current environment: " + reason);

                launcher.getLauncherPanel().progressBar.setText("[MC][" + version.getId() + "] Impossible de jouer,incompatible.");
                JOptionPane.showMessageDialog(launcher.getFrame(), reason, "[Jeu] Impossible de jouer,incompatible.", 0);
                setWorking(false);
                return;
            }
/*
            if(version.getMinimumLauncherVersion() > 7) {
                Launcher.getInstance().println("An update to your launcher is available and is required to play " + version.getId() + ". Please restart your launcher.");
                setWorking(false);
                return;
            }
*/
            if(!syncInfo.isInstalled())
                try {
                    final VersionList localVersionList = launcher.getVersionManager().getLocalVersionList();
                    if(localVersionList instanceof LocalVersionList) {
                        ((LocalVersionList) localVersionList).saveVersion(version);
                    	launcher.getLauncherPanel().progressBar.setText("Version installée : " + syncInfo.getLatestVersion());
                        logger.info("Version installée : " + syncInfo.getLatestVersion());
                    }
                }
                catch(final IOException e) {
                	logger.warn("Couldn't save version info to install " + syncInfo.getLatestVersion(), e);
                    setWorking(false);
                    return;
                }
            try {
                // Use the version we actually resolved from the profile, not
                // the operator's config default - otherwise picking 1.5.2 in
                // the dropdown still labels the job as the config version.
                DownloadJob job = new DownloadJob(" Version & Libraries " + version.getId(), false, this);
                addJob(job);
                launcher.getVersionManager().downloadVersion(syncInfo, job);

                DownloadJob resourceJob = new DownloadJob("Resources", true, this);
                addJob(resourceJob);
                launcher.getVersionManager().downloadResources(resourceJob, this.version);

                // Both play-sequence jobs are queued; arm the launch trigger
                // before they actually start so onDownloadJobFinished knows
                // it should hand off to launchGame once everything completes.
                launchPending = true;

                job.startDownloading(launcher.getVersionManager().getExecutorService());
                resourceJob.startDownloading(launcher.getVersionManager().getExecutorService());
            }
            catch(final IOException e) {
            	logger.warn("Couldn't get version info for " + syncInfo.getLatestVersion(), e);
                launchPending = false;
                setWorking(false);
                return;
            }
            synchronized(lock) {

                    // Edge case: every queued job was already cached and the
                    // executor finished them before we got here. Hand off
                    // straight away rather than waiting for a callback that
                    // already fired.
                    if(launchPending && isWorking() && !hasRemainingJobs()) {
                        launchPending = false;
                        try {
                            launchGame();
                        }
                        catch(final Throwable ex) {
                        	logger.warn("Erreur,merci de contacter le support !!", ex);
                        }
                    }

         }
        }
    }

    /**
     * Pattern-match the tail of stdout for well-known crashes that the
     * generic "[Minecraft tail] ..." dump doesn't make self-evident, and
     * surface an actionable hint both in the log (English, grep-friendly)
     * and via a French message on the progress bar plus a modal dialog.
     * Currently handles:
     * <ul>
     *   <li>pre-1.8 launchwrapper casting getClassLoader() to URLClassLoader
     *       (blows up on Java 9+ because the AppClassLoader is no longer a
     *       URLClassLoader) - tell the user to pin the profile to a Java 8
     *       executable.</li>
     *   <li>Debian / Ubuntu Java 8 shipping an accessibility.properties
     *       that references the GNOME ATK wrapper, which isn't installed
     *       by default - any AWT init then throws
     *       {@code AWTError: Assistive Technology not found:
     *       org.GNOME.Accessibility.AtkWrapper}.</li>
     * </ul>
     */
    private void detectKnownCrashes(final String[] tail) {
        if (tail == null || tail.length == 0) {
            return;
        }
        boolean sawClassCast = false;
        boolean sawLaunchWrapper = false;
        boolean sawAtkMissing = false;
        for (int i = 0; i < tail.length; i++) {
            final String line = tail[i];
            if (line == null) continue;
            if (line.contains("java.lang.ClassCastException") && line.contains("URLClassLoader")) {
                sawClassCast = true;
            }
            if (line.contains("net.minecraft.launchwrapper.Launch")) {
                sawLaunchWrapper = true;
            }
            if (line.contains("Assistive Technology not found") || line.contains("org.GNOME.Accessibility.AtkWrapper")) {
                sawAtkMissing = true;
            }
        }
        if (sawClassCast && sawLaunchWrapper) {
            final String hint =
                "HINT: launchwrapper <=1.11 casts the system class loader to URLClassLoader, "
                + "which only works on Java 8. Point this profile's \"Executable Java\" at a "
                + "Java 8 install, or publish a patched launchwrapper in the mirror's version JSON.";
            logger.warn(hint);

            final String frenchShort = "Cette version nécessite Java 8 (voir Paramètres du profil)";
            final String frenchLong =
                "Cette version de Minecraft utilise launchwrapper 1.5, qui ne fonctionne plus "
                + "sur Java 9 ou supérieur.\n\n"
                + "Pour jouer à cette version, installez Java 8 puis indiquez son chemin dans "
                + "le profil :\n"
                + "    Paramètres du profil \u2192 Paramètres Java (Avancé) \u2192 Executable Java\n\n"
                + "Exemple (Linux) :  /usr/lib/jvm/java-8-openjdk/bin/java";

            showCrashHint(frenchShort, frenchLong, "Version de Java incompatible");
            return;
        }
        if (sawAtkMissing) {
            final String hint =
                "HINT: Debian/Ubuntu's openjdk-8 ships accessibility.properties referencing "
                + "org.GNOME.Accessibility.AtkWrapper but doesn't install the wrapper by "
                + "default. Either install libatk-wrapper-java / libatk-wrapper-java-jni, or "
                + "add -Djavax.accessibility.assistive_technologies= to the profile's JVM "
                + "arguments to skip AT loading, or comment the assistive_technologies line "
                + "in /etc/java-8-openjdk/accessibility.properties.";
            logger.warn(hint);

            final String frenchShort = "Accessibilité Java manquante - voir dialogue";
            final String frenchLong =
                "Java ne trouve pas la passerelle d'accessibilité GNOME (AtkWrapper) - c'est "
                + "un bug bien connu du paquet openjdk-8 sur Debian / Ubuntu.\n\n"
                + "Trois solutions, de la plus simple à la plus invasive :\n\n"
                + "1. Ajouter cette option JVM dans votre profil pour désactiver le chargement "
                + "des technologies d'assistance :\n"
                + "    Paramètres du profil \u2192 Paramètres Java (Avancé) \u2192 Arguments JVM\n"
                + "    \u2192 ajoutez :   -Djavax.accessibility.assistive_technologies=\n\n"
                + "2. Installer le paquet manquant :\n"
                + "    sudo apt install libatk-wrapper-java libatk-wrapper-java-jni\n\n"
                + "3. Commenter la ligne fautive dans la config Java :\n"
                + "    sudo sed -i '/^assistive_technologies=/s/^/#/' /etc/java-8-openjdk/accessibility.properties";

            showCrashHint(frenchShort, frenchLong, "Accessibilité Java introuvable");
        }
    }

    /** Show a French hint both on the progress bar (short) and as a modal (long). */
    private void showCrashHint(final String progressBarText, final String dialogBody, final String dialogTitle) {
        launcher.getLauncherPanel().progressBar.setText(progressBarText);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(
                    launcher.getFrame(),
                    dialogBody,
                    dialogTitle,
                    JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void setWorking(final boolean working) {
        synchronized(lock) {
            if(nativeDir != null) {
            	launcher.getLauncherPanel().progressBar.setText("Suppression de : " + nativeDir.getName());
            	logger.info("Suppression de : " + nativeDir);
                if(!nativeDir.isDirectory() || FileUtils.deleteQuietly(nativeDir)){
                    nativeDir = null;
                    launcher.getLauncherPanel().progressBar.setText("[Jeu] Prêt au lancement.");
                }
                else {
                	logger.warn("Couldn't delete " + nativeDir + " - scheduling for deletion upon exit");
                    try {
                        FileUtils.forceDeleteOnExit(nativeDir);
                    }
                    catch(final Throwable localThrowable) {
                    }
                }
            }
            isWorking = working;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //launcher.getLauncherPanel().getBottomBar().getPlayButtonPanel().checkState();
                	launcher.getLauncherPanel().checkState();
                }
            });
        }
    }

    private void unpackNatives(final CompleteVersion version, final File targetDir) throws IOException {
        final OperatingSystem os = OperatingSystem.getCurrentPlatform();
        final Collection<Library> libraries = version.getRelevantLibraries();

        for(final Library library : libraries) {
            final Map<OperatingSystem, String> nativesPerOs = library.getNatives();

            if(nativesPerOs != null && nativesPerOs.get(os) != null) {
                final File file = new File(launcher.getWorkingDirectory(), "libraries/" + library.getArtifactPath(nativesPerOs.get(os)));
                final ZipFile zip = new ZipFile(file);
                final ExtractRules extractRules = library.getExtractRules();
                try {
                    final Enumeration<? extends ZipEntry> entries = zip.entries();

                    while(entries.hasMoreElements()) {
                        final ZipEntry entry = entries.nextElement();

                        if(extractRules == null || extractRules.shouldExtract(entry.getName())) {
                            final File targetFile = new File(targetDir, entry.getName());
                            if(targetFile.getParentFile() != null)
                                targetFile.getParentFile().mkdirs();

                            if(!entry.isDirectory()) {
                                final BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));

                                final byte[] buffer = new byte[2048];
                                final FileOutputStream outputStream = new FileOutputStream(targetFile);
                                final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                                try {
                                    int length;
                                    while((length = inputStream.read(buffer, 0, buffer.length)) != -1)
                                        bufferedOutputStream.write(buffer, 0, length);
                                }
                                finally {
                                    Downloadable.closeSilently(bufferedOutputStream);
                                    Downloadable.closeSilently(outputStream);
                                    Downloadable.closeSilently(inputStream);
                                }
                            }
                        }
                    }
                }
                finally {
                    zip.close();
                }
            }
        }
    }


}
