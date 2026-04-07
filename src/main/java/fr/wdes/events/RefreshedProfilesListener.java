package fr.wdes.events;

import fr.wdes.profile.ProfileManager;

public abstract interface RefreshedProfilesListener {
    public abstract void onProfilesRefreshed(ProfileManager paramProfileManager);

    public abstract boolean shouldReceiveEventsInUIThread();
}