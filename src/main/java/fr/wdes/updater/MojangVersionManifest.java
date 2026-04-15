package fr.wdes.updater;

import java.util.List;
import java.util.Map;

/**
 * Mojang's {@code version_manifest_v2.json} schema. Field names match the
 * keys in the JSON so Gson can deserialise straight into this class. We only
 * care about the {@code id} -> {@code url} mapping at the moment - the other
 * fields are accepted (and ignored) by the existing {@link VersionList}
 * deserialisation path that re-parses the same JSON into {@code RawVersionList}.
 */
public final class MojangVersionManifest {
    public Map<String, String> latest;
    public List<Entry> versions;

    public static final class Entry {
        public String id;
        public String type;
        public String url;
        public String time;
        public String releaseTime;
        public String sha1;
        public int complianceLevel;
    }
}
