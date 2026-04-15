package fr.wdes.updater;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.wdes.Http;
import fr.wdes.Launcher;
import fr.wdes.LauncherConstants;
import fr.wdes.OperatingSystem;
import fr.wdes.logger;
import fr.wdes.versions.CompleteVersion;

/**
 * Remote version list backed by Mojang's {@code piston-meta} endpoint, with
 * an optional overlay of the operator's own {@code versions/versions.json}
 * so modded / custom versions served from {@link LauncherConstants#URL_DOWNLOAD_VERSIONS_BASE}
 * show up alongside vanilla.
 *
 * <p>{@link #refreshVersions()}:
 * <ul>
 *   <li>fetches {@link LauncherConstants#URL_MOJANG_VERSION_MANIFEST} and
 *       caches it under {@code <workingDir>/cache/version_manifest_v2.json};</li>
 *   <li>optionally fetches {@code <URL_DOWNLOAD_VERSIONS_BASE>/versions/versions.json}
 *       (non-fatal - the launcher works with just Mojang);</li>
 *   <li>merges the two lists, <em>operator entries take precedence on id
 *       collision</em> so a custom "1.7.10" from the mirror shadows the
 *       vanilla Mojang one;</li>
 *   <li>remembers each Mojang version's per-version JSON URL so subsequent
 *       {@link #getContent} calls for {@code versions/&lt;id&gt;/&lt;id&gt;.json}
 *       hit the hash-addressed Mojang URL and write the response back to
 *       the local versions tree (where {@link LocalVersionList} picks it
 *       up next run - offline survival).</li>
 * </ul>
 *
 * <p>Operator-only versions have no URL in the manifest and therefore fall
 * through to the {@code URL_DOWNLOAD_VERSIONS_BASE + path} fetch in
 * {@link #getContent}.
 */
public class RemoteVersionList extends VersionList {
    private final Proxy proxy;

    /** Cached manifest body so {@link #getContent} can return it without refetching. */
    private String manifestBody;

    /** id -> per-version JSON URL, populated from the Mojang manifest only. */
    private final Map<String, String> versionUrlById = new HashMap<String, String>();

    public RemoteVersionList(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void refreshVersions() throws IOException {
        // -- Mojang: mandatory source ----------------------------------
        final String mojangBody = fetchManifestWithFallback();
        cacheManifestQuietly(mojangBody);
        MojangVersionManifest mojang = null;
        try {
            mojang = new Gson().fromJson(mojangBody, MojangVersionManifest.class);
        } catch (RuntimeException re) {
            logger.warn("Could not parse Mojang version manifest", re);
        }

        // -- Operator mirror: optional overlay -------------------------
        // Fetches <URL_DOWNLOAD_VERSIONS_BASE>/versions/versions.json to
        // discover modded / custom entries. Silent on failure - absence is
        // normal.
        final OperatorVersionList operator = fetchOperatorVersionsQuietly();

        // -- Merge into a single manifest body -------------------------
        manifestBody = mergeIntoManifestBody(mojang, operator);

        // -- URL map (Mojang only; operator entries served by the mirror)
        versionUrlById.clear();
        final Set<String> operatorIds = new HashSet<String>();
        if (operator != null && operator.versions != null) {
            for (OperatorEntry oe : operator.versions) {
                if (oe != null && oe.id != null) {
                    operatorIds.add(oe.id);
                }
            }
        }
        if (mojang != null && mojang.versions != null) {
            for (MojangVersionManifest.Entry entry : mojang.versions) {
                if (entry == null || entry.id == null || entry.url == null) {
                    continue;
                }
                // Operator overrides Mojang for colliding ids.
                if (operatorIds.contains(entry.id)) {
                    continue;
                }
                versionUrlById.put(entry.id, entry.url);
            }
        }

        // Hand off to the base class, which will call getContent("versions/
        // versions.json") and parse our merged body into the existing
        // PartialVersion list.
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

        // No URL in the manifest (operator-only id) and no cache - fetch
        // from the configured operator mirror.
        try {
            final String body = Http.performGet(new URL(LauncherConstants.URL_DOWNLOAD_VERSIONS_BASE + path), proxy);
            cacheVersionJsonQuietly(id, body);
            return body;
        } catch (IOException mirrorErr) {
            if (network != null) {
                throw network;
            }
            throw mirrorErr;
        }
    }

    /** Silent best-effort fetch of the operator's versions.json. */
    private OperatorVersionList fetchOperatorVersionsQuietly() {
        try {
            final String body = Http.performGet(new URL(LauncherConstants.URL_DOWNLOAD_VERSIONS_BASE + "versions/versions.json"), proxy);
            return new Gson().fromJson(body, OperatorVersionList.class);
        } catch (IOException e) {
            logger.info("Operator versions.json unavailable, using Mojang only: " + e.getMessage());
            return null;
        } catch (RuntimeException re) {
            logger.warn("Operator versions.json malformed", re);
            return null;
        }
    }

    /**
     * Build a {@code version_manifest_v2.json}-shaped body combining both
     * sources. Operator entries come first and take precedence on id
     * collision; Mojang entries fill in the rest. Kept as raw JSON so the
     * base class's {@code RawVersionList} parser sees a familiar shape.
     */
    static String mergeIntoManifestBody(final MojangVersionManifest mojang, final OperatorVersionList operator) {
        final JsonObject root = new JsonObject();

        // latest: prefer operator overrides.
        final JsonObject latest = new JsonObject();
        if (operator != null && operator.latest != null) {
            for (Map.Entry<String, String> e : operator.latest.entrySet()) {
                if (e.getValue() != null) {
                    latest.addProperty(e.getKey(), e.getValue());
                }
            }
        }
        if (mojang != null && mojang.latest != null) {
            for (Map.Entry<String, String> e : mojang.latest.entrySet()) {
                if (!latest.has(e.getKey()) && e.getValue() != null) {
                    latest.addProperty(e.getKey(), e.getValue());
                }
            }
        }
        root.add("latest", latest);

        // versions: operator first (override), then Mojang.
        final JsonArray versions = new JsonArray();
        final Set<String> seen = new HashSet<String>();
        if (operator != null && operator.versions != null) {
            for (OperatorEntry oe : operator.versions) {
                if (oe == null || oe.id == null) {
                    continue;
                }
                versions.add(buildEntry(oe.id, oe.type, oe.time, oe.releaseTime, null));
                seen.add(oe.id);
            }
        }
        if (mojang != null && mojang.versions != null) {
            for (MojangVersionManifest.Entry e : mojang.versions) {
                if (e == null || e.id == null || seen.contains(e.id)) {
                    continue;
                }
                versions.add(buildEntry(e.id, e.type, e.time, e.releaseTime, e.url));
                seen.add(e.id);
            }
        }
        root.add("versions", versions);

        return root.toString();
    }

    private static JsonObject buildEntry(String id, String type, String time, String releaseTime, String url) {
        final JsonObject o = new JsonObject();
        o.addProperty("id", id);
        if (type != null)        o.addProperty("type", type);
        if (time != null)        o.addProperty("time", time);
        if (releaseTime != null) o.addProperty("releaseTime", releaseTime);
        if (url != null)         o.addProperty("url", url);
        return o;
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

    /** Legacy mirror shape: {@code {"latest": {...}, "versions": [{id,type,time,releaseTime}, ...]}}. */
    static final class OperatorVersionList {
        Map<String, String> latest;
        List<OperatorEntry> versions;
    }

    static final class OperatorEntry {
        String id;
        String type;
        String time;
        String releaseTime;
    }
}
