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
    private File nativeDir;
    //private static final String CRASH_IDENTIFIER_MAGIC = "#@!@#";
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
        if(version.getMinecraftArguments() == null) {
        	logger.warn("Can't run version, missing minecraftArguments");
            setWorking(false);
            return null;
        }

        final Map<String, String> map = new HashMap<String, String>();
        final StrSubstitutor substitutor = new StrSubstitutor(map);
        final String[] split = version.getMinecraftArguments().split(" ");
        // ----DEBUT V2----
        map.put("auth_access_token", authentication.getSessionToken());
        map.put("user_properties", new GsonBuilder().registerTypeAdapter(PropertyMap.class, new LegacyPropertyMapSerializer()).create().toJson(authentication.getUserProperties()));
        map.put("user_property_map", new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(authentication.getUserProperties()));
        // ----FIN   V2----

        //DEBUT V1
        map.put("auth_username", authentication.getUsername());
        map.put("auth_session", authentication.getSessionToken() == null && authentication.canPlayOnline() ? "-" : authentication.getSessionToken());

        if(authentication.getSelectedProfile() != null) {
            map.put("auth_player_name", authentication.getUsername());
            map.put("auth_uuid", authentication.getSelectedProfile().getId());
            // ----DEBUT V2----
            //LEGACY("legacy"),  MOJANG("mojang");
            map.put("user_type", "mojang");
            // ----FIN   V2----
        }
        else {
            map.put("auth_player_name", "Player");
            map.put("auth_uuid", new UUID(0L, 0L).toString());
            // ----DEBUT V2----
            map.put("user_type", "legacy");
            // ----FIN   V2----
        }

        map.put("profile_name", selectedProfile.getName());
        map.put("version_name", version.getId());

        map.put("game_directory", gameDirectory.getAbsolutePath());
        map.put("game_assets", assetsDirectory.getAbsolutePath());
        // ----DEBUT V2----
        map.put("assets_root", new File(gameDirectory.getAbsolutePath(), "assets").getAbsolutePath());
        map.put("assets_index_name", version.getAssets() == null ? "legacy" : version.getAssets());
        // ----FIN   V2----
        //  FIN V1
        for(int i = 0; i < split.length; i++)
            split[i] = substitutor.replace(split[i]);

        return split;
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

        nativeDir = new File(launcher.getWorkingDirectory(), "versions/" + version.getId() + "/" + version.getId() + "-natives-" + System.nanoTime());
        if(!nativeDir.isDirectory())
            nativeDir.mkdirs();
        logger.info("Unpacking natives to " + nativeDir);
        try {
            unpackNatives(version, nativeDir);
        }
        catch(final IOException e) {
            logger.warn("Couldn't unpack natives!", e);
            return;
        }

        final File gameDirectory = selectedProfile.getGameDir() == null ? launcher.getWorkingDirectory() : selectedProfile.getGameDir();
        logger.info("Launching in " + gameDirectory);

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
          assetsDir = reconstructAssets();
        }
        catch (IOException e)
        {
        	logger.warn("Couldn't unpack natives!", e);
          return;
        }

        final OperatingSystem os = OperatingSystem.getCurrentPlatform();
        if(os.equals(OperatingSystem.OSX))
			try {
				processLauncher.addCommands(new String[] { "-Xdock:icon=" + getAssetObject("icons/minecraft.icns").getAbsolutePath(), "-Xdock:name=" + LauncherConstants.SERVER_NAME });
			} catch (IOException e1) {e1.printStackTrace();}
		else if(os.equals(OperatingSystem.WINDOWS))
            processLauncher.addCommands(new String[] { "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump" });

        final String profileArgs = selectedProfile.getJavaArgs();

        if(profileArgs != null)
            processLauncher.addSplitCommands(profileArgs);
        else {
            final boolean is32Bit = "32".equals(System.getProperty("sun.arch.data.model"));
            final String defaultArgument = is32Bit ? "-Xmx512M" : "-Xmx1G";
            processLauncher.addSplitCommands(defaultArgument);
        }

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

        if(selectedProfile.getResolution() != null) {
            processLauncher.addCommands(new String[] { "--width", String.valueOf(selectedProfile.getResolution().getWidth()) });
            processLauncher.addCommands(new String[] { "--height", String.valueOf(selectedProfile.getResolution().getHeight()) });
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
        	
        	
        	
            logger.info("Running " + full.toString());
            
            final JavaProcess process = processLauncher.start();
            process.safeSetExitRunnable(this);

            if(visibilityRule != LauncherVisibilityRule.DO_NOTHING)
                launcher.getFrame().setVisible(false);
        }
        catch(final IOException e) {
        	logger.warn("Couldn't launch game", e);
            setWorking(false);
            return;
        }
    }

    public void onDownloadJobFinished(final DownloadJob job) {
        updateProgressBar();
        synchronized(lock) {
            if(job.getFailures() > 0) {
            	logger.info("Job '" + job.getName() + "' finished with " + job.getFailures() + " failure(s)!");
                setWorking(false);
            }
            else {
            	logger.info("Job '" + job.getName() + "' finished successfully");

                if(isWorking() && !hasRemainingJobs())
                    try {
                        launchGame();
                    }
                    catch(final Throwable ex) {
                    	logger.warn("Fatal error launching game. Report this to http://mojang.atlassian.net please!", ex);
                    }
            }
        }
    }

    public void onDownloadJobProgressChanged(final DownloadJob job) {
        updateProgressBar();
    }

    public void onJavaProcessEnded(final JavaProcess process) {
        final int exitCode = process.getExitCode();

        if(exitCode == 0) {
        	logger.info("Game ended with no troubles detected (exit code " + exitCode + ")");

            if(visibilityRule == LauncherVisibilityRule.CLOSE_LAUNCHER)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    	logger.info("Following visibility rule and exiting launcher as the game has ended");
                        launcher.closeLauncher("GameLauncher #378");
                    }
                });
            else if(visibilityRule == LauncherVisibilityRule.HIDE_LAUNCHER)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    	logger.info("Following visibility rule and showing launcher as the game has ended");
                        launcher.getFrame().setVisible(true);
                    }
                });
        }
        else {
        	logger.warn("Game ended with bad state (exit code " + exitCode + ")");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	logger.info("Ignoring visibility rule and showing launcher due to a game crash");
                    launcher.getFrame().setVisible(true);
                }
            });
            String errorText = null;
            final String[] sysOut = process.getSysOutLines().getItems();

            for(int i = sysOut.length - 1; i >= 0; i--) {
                final String line = sysOut[i];
                final String crashIdentifier = "#@!@#";
                final int pos = line.lastIndexOf(crashIdentifier);

                if(pos >= 0 && pos < line.length() - crashIdentifier.length() - 1) {
                    errorText = line.substring(pos + crashIdentifier.length()).trim();
                    break;
                }
            }

            if(errorText != null) {
                final File file = new File(errorText);

                if(file.isFile()) {
                	logger.warn("Crash report detected, opening: " + errorText);
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
        synchronized(lock) {
            if(isWorking) {
            	logger.info("Tried to play game but game is already starting!");
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
        Launcher.getInstance().getLauncherPanel().getProgressBar().setString("Chargement des informations de version...");
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
        	logger.info("Queueing library & version downloads");
            try {
                version = launcher.getVersionManager().getLatestCompleteVersion(syncInfo);
            }
            catch(final IOException e) {
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
                	logger.warn("Couldn't sync local and remote versions", e);
                }
                version.setSynced(true);
            }

            if(!version.appliesToCurrentEnvironment()) {
                String reason = version.getIncompatibilityReason();
                if(reason == null)
                    reason = "This version is incompatible with your computer. Please try another one by going into Edit Profile and selecting one through the dropdown. Sorry!";
                logger.warn("Version " + version.getId() + " is incompatible with current environment: " + reason);
                JOptionPane.showMessageDialog(launcher.getFrame(), reason, "Cannot play game", 0);
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
                        logger.info("Installed " + syncInfo.getLatestVersion());
                    }
                }
                catch(final IOException e) {
                	logger.warn("Couldn't save version info to install " + syncInfo.getLatestVersion(), e);
                    setWorking(false);
                    return;
                }
            try {
                DownloadJob job = new DownloadJob(" Version & Libraries "+launcher.config.version, false, this);
                addJob(job);
                launcher.getVersionManager().downloadVersion(syncInfo, job);
                job.startDownloading(launcher.getVersionManager().getExecutorService());
                
                DownloadJob resourceJob = new DownloadJob("Resources", true, this);
                addJob(resourceJob);
                launcher.getVersionManager().downloadResources(resourceJob, this.version);
                resourceJob.startDownloading(launcher.getVersionManager().getExecutorService());
            }
            catch(final IOException e) {
            	logger.warn("Couldn't get version info for " + syncInfo.getLatestVersion(), e);
                setWorking(false);
                return;
            }
        }
    }

    private void setWorking(final boolean working) {
        synchronized(lock) {
            if(nativeDir != null) {
            	logger.info("Deleting " + nativeDir);
                if(!nativeDir.isDirectory() || FileUtils.deleteQuietly(nativeDir))
                    nativeDir = null;
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

    protected void updateProgressBar() {
        final float progress = getProgress();
        final boolean hasTasks = hasRemainingJobs();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                launcher.getLauncherPanel().getProgressBar().setVisible(hasTasks);
                launcher.getLauncherPanel().getProgressBar().setValue((int) (progress * 100.0F));
            }
        });
    }
}