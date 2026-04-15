package fr.wdes.updater;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import fr.wdes.Http;
import fr.wdes.Launcher;
import fr.wdes.LauncherConstants;
import fr.wdes.OperatingSystem;
import fr.wdes.logger;
import fr.wdes.versions.CompleteVersion;

/**
 * Remote version list backed by Mojang's {@code piston-meta} endpoint.
 *
 * <p>{@link #refreshVersions()} fetches {@link LauncherConstants#URL_MOJANG_VERSION_MANIFEST},
 * caches it under {@code <workingDir>/cache/version_manifest_v2.json}, and
 * remembers each version's per-version JSON URL so subsequent
 * {@link #getContent} calls for {@code versions/<id>/<id>.json} can fetch the
 * version metadata from Mojang's hash-addressed
 * {@code piston-meta.mojang.com/v1/packages/<sha>/<id>.json} URL and write the
 * response into the local versions tree where {@link LocalVersionList} picks
 * it up on subsequent runs - so once a version has been seen online it stays
 * usable even if the network or Mojang itself is unreachable.
 */
public class RemoteVersionList extends VersionList {
    private final Proxy proxy;

    /** Cached manifest body so {@link #getContent} can return it without refetching. */
    private String manifestBody;

    /** id -> per-version JSON URL, populated from the manifest. */
    private final Map<String, String> versionUrlById = new HashMap<String, String>();

    public RemoteVersionList(Proxy proxy) {
        this.proxy = proxy;
    }

    public URL getIndex(String indexName) throws MalformedURLException {
        // NOTE: kept on the legacy index endpoint for now. Modern Mojang
        // hash-addresses asset indexes via each version's assetIndex.url,
        // which the CompleteVersion model in this codebase doesn't expose yet.
        String url = LauncherConstants.URL_DOWNLOAD_INDEXES_BASE + "indexes/" + indexName + ".json";
        return new URL(url);
    }

    @Override
    public void refreshVersions() throws IOException {
        manifestBody = fetchManifestWithFallback();
        cacheManifestQuietly(manifestBody);

        try {
            final MojangVersionManifest manifest = new Gson().fromJson(manifestBody, MojangVersionManifest.class);
            versionUrlById.clear();
            if (manifest != null && manifest.versions != null) {
                for (MojangVersionManifest.Entry entry : manifest.versions) {
                    if (entry != null && entry.id != null && entry.url != null) {
                        versionUrlById.put(entry.id, entry.url);
                    }
                }
            }
        } catch (RuntimeException re) {
            // Don't blow up the refresh if the manifest is malformed - we'll
            // fall back to whatever URLs the LocalVersionList knows about.
            logger.warn("Could not parse Mojang version manifest", re);
        }

        // Hand off to the base class, which will call getContent("versions/
        // versions.json") and parse it into the existing PartialVersion list.
        // The body it parses is the same Mojang manifest we already cached.
        super.refreshVersions();
    }

    @Override
    protected String getContent(final String path) throws IOException {
        if ("versions/versions.json".equals(path)) {
            if (manifestBody == null) {
                manifestBody = fetchManifestWithFallback();
                cacheManifestQuietly(manifestBody);
            }
            return manifestBody;
        }

        final String id = extractVersionId(path);
        if (id != null) {
            return fetchVersionJsonWithFallback(id, path);
        }

        // Legacy fallback for unrecognised paths: hit the configured mirror.
        return Http.performGet(new URL(LauncherConstants.URL_DOWNLOAD_VERSIONS_BASE + path), proxy);
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public boolean hasAllFiles(final CompleteVersion version, final OperatingSystem os) {
        return false;
    }

    // -- helpers ---------------------------------------------------------

    /** {@code versions/X/X.json} -> {@code X}, otherwise null. */
    static String extractVersionId(final String path) {
        if (path == null) {
            return null;
        }
        if (!path.startsWith("versions/") || !path.endsWith(".json")) {
            return null;
        }
        final String[] parts = path.split("/");
        if (parts.length != 3) {
            return null;
        }
        if (!parts[2].equals(parts[1] + ".json")) {
            return null;
        }
        return parts[1];
    }

    private String fetchManifestWithFallback() throws IOException {
        try {
            return Http.performGet(new URL(LauncherConstants.URL_MOJANG_VERSION_MANIFEST), proxy);
        } catch (IOException e) {
            final File cached = manifestCacheFile();
            if (cached != null && cached.isFile()) {
                logger.warn("Couldn't reach Mojang manifest, using cached copy at " + cached, e);
                return FileUtils.readFileToString(cached);
            }
            throw e;
        }
    }

    private String fetchVersionJsonWithFallback(final String id, final String path) throws IOException {
        final String url = versionUrlById.get(id);
        IOException network = null;

        if (url != null) {
            try {
                final String body = Http.performGet(new URL(url), proxy);
                cacheVersionJsonQuietly(id, body);
                return body;
            } catch (IOException e) {
                network = e;
            }
        }

        // Fall back to whatever was cached on disk for this version.
        final File local = versionJsonFile(id);
        if (local != null && local.isFile()) {
            logger.warn("Couldn't fetch " + id + " from Mojang, using cached copy at " + local, network);
            return FileUtils.readFileToString(local);
        }

        if (network != null) {
            throw network;
        }
        // No URL in the manifest and no cache - one last try with the legacy mirror.
        return Http.performGet(new URL(LauncherConstants.URL_DOWNLOAD_VERSIONS_BASE + path), proxy);
    }

    private File manifestCacheFile() {
        final Launcher launcher = Launcher.getInstance();
        if (launcher == null || launcher.getWorkingDirectory() == null) {
            return null;
        }
        return new File(new File(launcher.getWorkingDirectory(), "cache"), "version_manifest_v2.json");
    }

    private File versionJsonFile(final String id) {
        final Launcher launcher = Launcher.getInstance();
        if (launcher == null || launcher.getWorkingDirectory() == null) {
            return null;
        }
        return new File(launcher.getWorkingDirectory(), "versions/" + id + "/" + id + ".json");
    }

    private void cacheManifestQuietly(final String body) {
        if (body == null) return;
        final File f = manifestCacheFile();
        if (f == null) return;
        try {
            if (f.getParentFile() != null) {
                f.getParentFile().mkdirs();
            }
            FileUtils.writeStringToFile(f, body);
        } catch (IOException e) {
            logger.warn("Couldn't cache Mojang manifest to " + f, e);
        }
    }

    private void cacheVersionJsonQuietly(final String id, final String body) {
        if (body == null || id == null) return;
        final File f = versionJsonFile(id);
        if (f == null) return;
        try {
            if (f.getParentFile() != null) {
                f.getParentFile().mkdirs();
            }
            FileUtils.writeStringToFile(f, body);
        } catch (IOException e) {
            logger.warn("Couldn't cache version JSON for " + id + " to " + f, e);
        }
    }
}
