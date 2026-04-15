package fr.wdes.versions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.wdes.OperatingSystem;
import fr.wdes.download.Downloadable;


public class CompleteVersion implements Version {
	protected String id;
	protected Date time;
	protected Date releaseTime;
	protected ReleaseType type;
	protected String minecraftArguments;
	protected List<Library> libraries;
	protected String mainClass;
	protected int minimumLauncherVersion;
	protected String incompatibilityReason;
	protected List<Rule> rules;
	protected volatile boolean synced = false;
    protected String assets;
    /**
     * Mojang piston-meta version JSON ships an {@code assetIndex} block
     * pointing at the hash-addressed asset-index JSON for this version.
     * Populated by Gson when present; nullable on older version JSONs
     * served by mirrors that predate the modern format.
     */
    protected AssetIndexInfo assetIndex;
    /**
     * Mojang piston-meta version JSON also ships a {@code downloads} block
     * with the hash-addressed client/server jar URLs. Only {@code client.url}
     * is consumed today; nullable on older mirror payloads.
     */
    protected DownloadsInfo downloads;


    public CompleteVersion() {
    }

    public CompleteVersion(final CompleteVersion version) {
        this(version.getId(),version.getAssets(), version.getReleaseTime(), version.getUpdatedTime(), version.getType(), version.getMainClass(), version.getMinecraftArguments());
    }

    public CompleteVersion(final String id,final String assets, final Date releaseTime, final Date updateTime, final ReleaseType type, final String mainClass, final String minecraftArguments) {
        if(id == null || id.length() == 0)
            throw new IllegalArgumentException("ID cannot be null or empty");
        if(releaseTime == null)
            throw new IllegalArgumentException("Release time cannot be null");
        if(updateTime == null)
            throw new IllegalArgumentException("Update time cannot be null");
        if(type == null)
            throw new IllegalArgumentException("Release type cannot be null");
        if(mainClass == null || mainClass.length() == 0)
            throw new IllegalArgumentException("Main class cannot be null or empty");
        if(minecraftArguments == null)
            throw new IllegalArgumentException("Process arguments cannot be null or empty");

        this.id = id;
        this.releaseTime = releaseTime;
        time = updateTime;
        this.type = type;
        this.assets = assets;
        this.mainClass = mainClass;
        libraries = new ArrayList<Library>();
        this.minecraftArguments = minecraftArguments;
    }

    public CompleteVersion(final Version version, final String mainClass, final String minecraftArguments) {
        this(version.getId(), version.getAssets(), version.getReleaseTime(), version.getUpdatedTime(), version.getType(), mainClass, minecraftArguments);
    }

    public boolean appliesToCurrentEnvironment() {
        if(rules == null)
            return true;
        Rule.Action lastAction = Rule.Action.DISALLOW;

        for(final Rule rule : rules) {
            final Rule.Action action = rule.getAppliedAction();
            if(action != null)
                lastAction = action;
        }

        return lastAction == Rule.Action.ALLOW;
    }

    public Collection<File> getClassPath(final OperatingSystem os, final File base) {
        final Collection<Library> libraries = getRelevantLibraries();
        final Collection<File> result = new ArrayList<File>();

        for(final Library library : libraries)
            if(library.getNatives() == null)
                result.add(new File(base, "libraries/" + library.getArtifactPath()));

        result.add(new File(base, "versions/" + getId() + "/" + getId() + ".jar"));

        return result;
    }

    public Collection<String> getExtractFiles(final OperatingSystem os) {
        final Collection<Library> libraries = getRelevantLibraries();
        final Collection<String> result = new ArrayList<String>();

        for(final Library library : libraries) {
            final Map<OperatingSystem, String> natives = library.getNatives();

            if(natives != null && natives.containsKey(os))
                result.add("libraries/" + library.getArtifactPath(natives.get(os)));
        }

        return result;
    }

    public String getId() {
        return id;
    }

    public String getIncompatibilityReason() {
        return incompatibilityReason;
    }

    public Collection<Library> getLibraries() {
        return libraries;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getMinecraftArguments() {
        return minecraftArguments;
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public Collection<Library> getRelevantLibraries() {
        final List<Library> result = new ArrayList<Library>();

        for(final Library library : libraries)
            if(library.appliesToCurrentEnvironment())
                result.add(library);

        return result;
    }

    public Set<Downloadable> getRequiredDownloadables(final OperatingSystem os, final Proxy proxy, final File targetDirectory, final boolean ignoreLocalFiles) throws MalformedURLException {
        final Set<Downloadable> neededFiles = new HashSet<Downloadable>();

        for(final Library library : getRelevantLibraries()) {
            String file = null;

            if(library.getNatives() != null) {
                final String natives = library.getNatives().get(os);
                if(natives != null)
                    file = library.getArtifactPath(natives);
            }
            else
                file = library.getArtifactPath();

            if(file != null) {
                final URL url = new URL(library.getDownloadUrl() + file);
                final File local = new File(targetDirectory, "libraries/" + file);

                if(!local.isFile() || !library.hasCustomUrl())
                    neededFiles.add(new Downloadable(proxy, url, local, ignoreLocalFiles));
            }
        }

        return neededFiles;
    }

    public Set<String> getRequiredFiles(final OperatingSystem os) {
        final Set<String> neededFiles = new HashSet<String>();

        for(final Library library : getRelevantLibraries())
            if(library.getNatives() != null) {
                final String natives = library.getNatives().get(os);
                if(natives != null)
                    neededFiles.add("libraries/" + library.getArtifactPath(natives));
            }
            else
                neededFiles.add("libraries/" + library.getArtifactPath());

        return neededFiles;
    }

    public ReleaseType getType() {
        return type;
    }

    public Date getUpdatedTime() {
        return time;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setMainClass(final String mainClass) {
        if(mainClass == null || mainClass.length() == 0)
            throw new IllegalArgumentException("Main class cannot be null or empty");
        this.mainClass = mainClass;
    }

    public void setMinecraftArguments(final String minecraftArguments) {
        if(minecraftArguments == null)
            throw new IllegalArgumentException("Process arguments cannot be null or empty");
        this.minecraftArguments = minecraftArguments;
    }

    public void setMinimumLauncherVersion(final int minimumLauncherVersion) {
        this.minimumLauncherVersion = minimumLauncherVersion;
    }

    public void setReleaseTime(final Date time) {
        if(time == null)
            throw new IllegalArgumentException("Time cannot be null");
        releaseTime = time;
    }

    public void setSynced(final boolean synced) {
        this.synced = synced;
    }

    public void setType(final ReleaseType type) {
        if(type == null)
            throw new IllegalArgumentException("Release type cannot be null");
        this.type = type;
    }

    public void setUpdatedTime(final Date time) {
        if(time == null)
            throw new IllegalArgumentException("Time cannot be null");
        this.time = time;
    }

    @Override
    public String toString() {
        return "CompleteVersion{id='" + id + '\'' + ", time=" + time + ", type=" + type + ", libraries=" + libraries + ", mainClass='" + mainClass + '\'' + ", minimumLauncherVersion=" + minimumLauncherVersion + '}';
    }

	public String getAssets() {
		return this.assets;
	}

	public AssetIndexInfo getAssetIndex() {
		return assetIndex;
	}

	public DownloadsInfo getDownloads() {
		return downloads;
	}

	/**
	 * Mojang's {@code downloads} block. Only the {@code client.url} is used
	 * (to locate the version jar); server/windows_server artefacts aren't
	 * currently downloaded by this launcher but are kept in the model so
	 * round-tripping the JSON doesn't drop them.
	 */
	public static class DownloadsInfo {
		public Download client;
		public Download server;
		public Download windows_server;
	}

	public static class Download {
		public String sha1;
		public long size;
		public String url;
	}

	/**
	 * Mojang's per-version {@code assetIndex} block. We only consume
	 * {@code id} and {@code url} but the other fields are kept as Gson-
	 * populated metadata (and so future code can verify integrity).
	 */
	public static class AssetIndexInfo {
		public String id;
		public String sha1;
		public long size;
		public long totalSize;
		public String url;
	}
}