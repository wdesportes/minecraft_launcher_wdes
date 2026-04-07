package fr.wdes.updater;

import fr.wdes.versions.Version;

public class VersionSyncInfo {
    public static enum VersionSource {
        REMOTE, LOCAL;
    }

    protected final Version localVersion;
    protected final Version remoteVersion;
    protected final boolean isInstalled;

    protected final boolean isUpToDate;

    public VersionSyncInfo(final Version localVersion, final Version remoteVersion, final boolean installed, final boolean upToDate) {
        this.localVersion = localVersion;
        this.remoteVersion = remoteVersion;
        isInstalled = installed;
        isUpToDate = upToDate;
    }

    public VersionSource getLatestSource() {
        if(getLocalVersion() == null)
            return VersionSource.REMOTE;
        if(getRemoteVersion() == null)
            return VersionSource.LOCAL;
        if(getRemoteVersion().getUpdatedTime().after(getLocalVersion().getUpdatedTime()))
            return VersionSource.REMOTE;
        return VersionSource.LOCAL;
    }

    public Version getLatestVersion() {
        if(getLatestSource() == VersionSource.REMOTE)
            return remoteVersion;
        return localVersion;
    }

    public Version getLocalVersion() {
        return localVersion;
    }

    public Version getRemoteVersion() {
        return remoteVersion;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public boolean isOnRemote() {
        return remoteVersion != null;
    }

    public boolean isUpToDate() {
        return isUpToDate;
    }

    @Override
    public String toString() {
        return "VersionSyncInfo{localVersion=" + localVersion + ", remoteVersion=" + remoteVersion + ", isInstalled=" + isInstalled + ", isUpToDate=" + isUpToDate + '}';
    }
}