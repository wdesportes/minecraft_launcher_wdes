package fr.wdes.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

/**
 * Smoke-tests the bits of the Mojang manifest pipeline that don't require the
 * network or a live {@code Launcher} singleton: the {@code versions/X/X.json}
 * path parser, and Gson deserialisation of {@link MojangVersionManifest} from
 * the actual {@code version_manifest_v2.json} shape.
 */
public class RemoteVersionListTest {

    @Test
    public void extractVersionId_recognisesValidPaths() {
        assertEquals("1.7.10", RemoteVersionList.extractVersionId("versions/1.7.10/1.7.10.json"));
        assertEquals("1.20.4-rc1", RemoteVersionList.extractVersionId("versions/1.20.4-rc1/1.20.4-rc1.json"));
    }

    @Test
    public void extractVersionId_rejectsPathsThatAreNotPerVersionJson() {
        assertNull(RemoteVersionList.extractVersionId(null));
        assertNull(RemoteVersionList.extractVersionId(""));
        assertNull(RemoteVersionList.extractVersionId("versions/versions.json"));
        assertNull(RemoteVersionList.extractVersionId("versions/1.7.10/1.7.10.jar"));
        assertNull(RemoteVersionList.extractVersionId("indexes/1.7.10.json"));
        // Mismatched id between folder and filename - rejected so we never
        // accidentally write a fetched JSON to the wrong cache slot.
        assertNull(RemoteVersionList.extractVersionId("versions/1.7.10/1.8.0.json"));
    }

    @Test
    public void mojangManifest_parsesTrimmedFixture() {
        // Trimmed copy of the actual https://piston-meta.mojang.com response
        // shape: just enough to verify Gson populates id/url, which is all
        // RemoteVersionList.refreshVersions() relies on.
        final String json = "{\n" +
                "  \"latest\": {\"release\": \"1.20.4\", \"snapshot\": \"24w13a\"},\n" +
                "  \"versions\": [\n" +
                "    {\"id\": \"1.20.4\", \"type\": \"release\", \"url\": \"https://piston-meta.mojang.com/v1/packages/abc/1.20.4.json\", \"time\": \"2024-01-01T00:00:00+00:00\", \"releaseTime\": \"2024-01-01T00:00:00+00:00\", \"sha1\": \"abc\", \"complianceLevel\": 1},\n" +
                "    {\"id\": \"1.7.10\", \"type\": \"release\", \"url\": \"https://piston-meta.mojang.com/v1/packages/def/1.7.10.json\", \"time\": \"2014-01-01T00:00:00+00:00\", \"releaseTime\": \"2014-01-01T00:00:00+00:00\", \"sha1\": \"def\", \"complianceLevel\": 0}\n" +
                "  ]\n" +
                "}";

        final MojangVersionManifest manifest = new Gson().fromJson(json, MojangVersionManifest.class);
        assertNotNull(manifest);
        assertNotNull(manifest.versions);
        assertEquals(2, manifest.versions.size());
        assertEquals("1.20.4", manifest.versions.get(0).id);
        assertEquals("https://piston-meta.mojang.com/v1/packages/abc/1.20.4.json", manifest.versions.get(0).url);
        assertEquals("release", manifest.versions.get(0).type);
        assertEquals("1.7.10", manifest.versions.get(1).id);
        assertTrue(manifest.latest.containsKey("release"));
        assertEquals("1.20.4", manifest.latest.get("release"));
    }
}
