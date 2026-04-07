package fr.wdes.events;

import fr.wdes.authentication.AuthenticationService;

public abstract interface AuthenticationChangedListener {
    public abstract void onAuthenticationChanged(AuthenticationService paramAuthenticationService);

    public abstract boolean shouldReceiveEventsInUIThread();
}