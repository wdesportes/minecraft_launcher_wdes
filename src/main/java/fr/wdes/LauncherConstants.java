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
    public static final String LIBRARY_DOWNLOAD_BASE = "https://libraries.minecraft.net/";// {groupId}/{artifactId}/{version}/{artifactId}-{version}.jar
    public static final String URL_CONFIGS           = "http://wdeslaunchers.wdes.fr/configs/";
    /**
     * Operator mirror root. The launcher expects the following layout
     * underneath this base and will GET each path on demand:
     * <ul>
     *   <li>{@code versions/versions.json} - optional, merged into the
     *       Mojang manifest to surface modded / custom ids in the
     *       dropdown (operator entries take precedence on id collision);</li>
     *   <li>{@code versions/<id>/<id>.json} - per-version JSON for
     *       operator-owned ids (either because they're mirror-only or
     *       because the operator shadowed a vanilla id in versions.json);</li>
     *   <li>{@code versions/<id>/<id>.jar} - jar fallback when the
     *       per-version JSON has no {@code downloads.client.url}
     *       (legacy / modded format);</li>
     *   <li>{@code indexes/<id>.json} - asset-index fallback when the
     *       per-version JSON has no {@code assetIndex.url} (same
     *       legacy reason). This used to live at a separate
     *       URL_DOWNLOAD_INDEXES_BASE - it's now derived from the same
     *       base because every known operator served both on one host.</li>
     * </ul>
     * None of these paths are required for vanilla Mojang versions -
     * those are fetched from piston-meta / piston-data.mojang.com. The
     * mirror only matters for entries the operator publishes in
     * versions/versions.json.
     */
    public static final String URL_DOWNLOAD_VERSIONS_BASE     = "http://wdeslaunchers.wdes.fr/";
    /**
     * Mojang's modern launcher meta endpoint. Returns the version manifest
     * (latest release/snapshot ids + array of {@code {id, type, url, time,
     * releaseTime, sha1, complianceLevel}} entries). Each entry's {@code url}
     * points at the per-version JSON, hash-addressed under
     * {@code piston-meta.mojang.com/v1/packages/<sha>/<id>.json}.
     */
    public static final String URL_MOJANG_VERSION_MANIFEST    = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    public static final String URL_ASSETS_DOWNLOAD_BASE       = "https://resources.download.minecraft.net/";// {hash[0:2]}/{hash}
    // URL_LOGO_BASE was removed: the per-server logo image fetched from
    // <base>/logos/<uuid>.png has been replaced by a painted text wordmark
    // (see fr.wdes.ui.lite.LogoLabel) on both the splash and the home page.
    // The associated SQLite "Launchers" cache table is no longer created
    // on new installs (see Launcher.java); existing user databases keep
    // their orphan table - harmless and not worth a migration.
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
