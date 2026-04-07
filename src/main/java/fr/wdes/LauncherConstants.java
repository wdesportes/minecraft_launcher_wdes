package fr.wdes;

import java.net.URI;
import java.net.URISyntaxException;
public class LauncherConstants {
	/**

I8,        8        ,8I         88                            88                                                             88
`8b       d8b       d8'         88                            88                                                             88
 "8,     ,8"8,     ,8"          88                            88                                                             88
  Y8     8P Y8     8P   ,adPPYb,88   ,adPPYba,  ,adPPYba,     88           ,adPPYYba,  88       88  8b,dPPYba,    ,adPPYba,  88,dPPYba,    ,adPPYba,  8b,dPPYba,  ,adPPYba,
  `8b   d8' `8b   d8'  a8"    `Y88  a8P_____88  I8[    ""     88           ""     `Y8  88       88  88P'   `"8a  a8"     ""  88P'    "8a  a8P_____88  88P'   "Y8  I8[    ""
   `8a a8'   `8a a8'   8b       88  8PP"""""""   `"Y8ba,      88           ,adPPPPP88  88       88  88       88  8b          88       88  8PP"""""""  88           `"Y8ba,
    `8a8'     `8a8'    "8a,   ,d88  "8b,   ,aa  aa    ]8I     88           88,    ,88  "8a,   ,a88  88       88  "8a,   ,aa  88       88  "8b,   ,aa  88          aa    ]8I
     `8'       `8'      `"8bbdP"Y8   `"Ybbd8"'  `"YbbdP"'     88888888888  `"8bbdP"Y8   `"YbbdP'Y8  88       88   `"Ybbd8"'  88       88   `"Ybbd8"'  88          `"YbbdP"'


	 */
    public static final String UUID                  = Launcher.getInstance().uuid;
    public static final String VERSION_MINECRAFT     = Launcher.getInstance().config.version;//auto-mc
    public static final String DEFAULT_PROFILE_NAME  = "Minecraft";
    public static final String SERVER_NAME           = Launcher.getInstance().config.nom;
    public static final String APPDATA               = Launcher.getInstance().appdata;
    public static final String USER_AGENT            = "Mozilla/5.0 (WdesAuth; fr-FR) Gecko/20100316 Firefox/3.6.2";
    public static final String LIBRARY_DOWNLOAD_BASE = "https://libraries.minecraft.net/";
    public static final String URL_CONFIGS           = "http://wdeslaunchers.wdes.fr/configs/";
    public static String URL_VERSION_LIST            = "https://s3.amazonaws.com/Minecraft.Download/";
    public static final String URL_DOWNLOAD_VERSIONS_BASE     = "http://wdeslaunchers.wdes.fr/";// versions/{version}/{version}.jar
    public static final String URL_RESOURCE_BASE     = "https://s3.amazonaws.com/Minecraft.Download/";//https://s3.amazonaws.com/MinecraftResources/
    public static final String URL_STATUS_CHECKER    = Launcher.getInstance().config.URL_STATUS_CHECKER;
    public static final String URL_FONDS_DOWNLOAD    = Launcher.getInstance().config.URL_FONDS_DOWNLOAD;
    public static final String PLACEHOLDER_LOGIN             = Launcher.getInstance().config.PLACEHOLDER_LOGIN;
    public static final String PLACEHOLDER_PASSD             = Launcher.getInstance().config.PLACEHOLDER_PASSD;
    public static final String URL_AUTHENTIFICATION_SYSTEM   = Launcher.getInstance().config.URL_AUTHENTIFICATION_SYSTEM; // https://authserver.mojang.com
    public static URI constantURI(final String input) {
        try {
            return new URI(input);
        }
        catch(final URISyntaxException e) {
            throw new Error(e);
        }
    }
}
