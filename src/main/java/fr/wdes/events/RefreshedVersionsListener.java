package fr.wdes.events;

import fr.wdes.updater.VersionManager;

public abstract interface RefreshedVersionsListener {
    public abstract void onVersionsRefreshed(VersionManager paramVersionManager);

    public abstract boolean shouldReceiveEventsInUIThread();
}