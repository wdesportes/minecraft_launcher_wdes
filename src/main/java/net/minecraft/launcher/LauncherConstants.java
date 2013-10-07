package net.minecraft.launcher;

import java.net.URI;
import java.net.URISyntaxException;

public class LauncherConstants {
    public static final String VERSION_NAME = "1.0";
    public static final int VERSION_NUMERIC = 7;
    public static final String DEFAULT_PROFILE_NAME = "Minecraft";
    public static final String SERVER_NAME = "WdesLaunchers";
    public static final String APPLICATION_NAME = "WdesLaunchers";
    public static final URI URL_REGISTER = constantURI("https://launchers.wdes.fr/auth/register.php");
    // public static final String URL_DOWNLOAD_BASE =
    // "https://s3.amazonaws.com/Minecraft.Download/";
    public static final String URL_DOWNLOAD_BASE = "https://s3.amazonaws.com/Minecraft.Download/";
    public static final String URL_RESOURCE_BASE = "https://s3.amazonaws.com/MinecraftResources/";
    public static final String LIBRARY_DOWNLOAD_BASE = "https://libraries.minecraft.net/";
    public static final String URL_BLOG = "https://launchers.wdes.fr";
    public static final String URL_STATUS_CHECKER = "http://status.mojang.com/check";
    public static final String URL_BOOTSTRAP_DOWNLOAD = "https://launchers.wdes.fr";
    public static final URI URL_FORGOT_USERNAME = constantURI("http://help.mojang.com/customer/portal/articles/1233873");
    public static final URI URL_FORGOT_PASSWORD_MINECRAFT = constantURI("http://help.mojang.com/customer/portal/articles/329524-change-or-forgot-password");
    public static final URI URL_FORGOT_MIGRATED_EMAIL = constantURI("http://help.mojang.com/customer/portal/articles/1205055-minecraft-launcher-error---migrated-account");
    public static final String MD5_FILE = "https://launchers.wdes.fr/md5.txt";
    public static final String LAUNCHER_URL = "https://launchers.wdes.fr/launcher.jar";

    public static URI constantURI(final String input) {
        try {
            return new URI(input);
        }
        catch(final URISyntaxException e) {
            throw new Error(e);
        }
    }
}