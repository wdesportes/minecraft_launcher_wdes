package fr.wdes.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
    public void completeVersionParsesAssetIndexBlock() {
        // Trimmed real Mojang per-version JSON. The launcher only consumes
        // assetIndex.url to fetch the asset index; the rest of the
        // CompleteVersion fields (libraries, mainClass, ...) are validated
        // elsewhere via the running launcher.
        final String json = "{\n" +
                "  \"id\": \"1.7.10\",\n" +
                "  \"type\": \"release\",\n" +
                "  \"mainClass\": \"net.minecraft.client.main.Main\",\n" +
                "  \"minecraftArguments\": \"--username ${auth_player_name}\",\n" +
                "  \"time\": \"2014-05-14T19:29:23+00:00\",\n" +
                "  \"releaseTime\": \"2014-05-14T19:29:23+00:00\",\n" +
                "  \"assets\": \"1.7.10\",\n" +
                "  \"assetIndex\": {\n" +
                "    \"id\": \"1.7.10\",\n" +
                "    \"sha1\": \"abcdef1234567890\",\n" +
                "    \"size\": 12345,\n" +
                "    \"totalSize\": 67890,\n" +
                "    \"url\": \"https://piston-meta.mojang.com/v1/packages/abcdef/1.7.10.json\"\n" +
                "  }\n" +
                "}";
        final fr.wdes.versions.CompleteVersion v =
            new com.google.gson.GsonBuilder()
                .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
                .registerTypeAdapter(java.util.Date.class, new DateTypeAdapter())
                .create()
                .fromJson(json, fr.wdes.versions.CompleteVersion.class);
        assertNotNull(v);
        assertNotNull("assetIndex must hydrate from JSON", v.getAssetIndex());
        assertEquals("1.7.10", v.getAssetIndex().id);
        assertEquals("https://piston-meta.mojang.com/v1/packages/abcdef/1.7.10.json", v.getAssetIndex().url);
        assertEquals(12345L, v.getAssetIndex().size);
    }

    @Test
    public void completeVersionParsesDownloadsClientBlock() {
        final String json = "{\n" +
                "  \"id\": \"1.7.10\",\n" +
                "  \"type\": \"release\",\n" +
                "  \"mainClass\": \"net.minecraft.client.main.Main\",\n" +
                "  \"minecraftArguments\": \"--username ${auth_player_name}\",\n" +
                "  \"time\": \"2014-05-14T19:29:23+00:00\",\n" +
                "  \"releaseTime\": \"2014-05-14T19:29:23+00:00\",\n" +
                "  \"downloads\": {\n" +
                "    \"client\": {\n" +
                "      \"sha1\": \"deadbeef1234\",\n" +
                "      \"size\": 5000000,\n" +
                "      \"url\": \"https://piston-data.mojang.com/v1/objects/deadbeef/client.jar\"\n" +
                "    },\n" +
                "    \"server\": {\n" +
                "      \"sha1\": \"cafebabe5678\",\n" +
                "      \"size\": 9000000,\n" +
                "      \"url\": \"https://piston-data.mojang.com/v1/objects/cafebabe/server.jar\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        final fr.wdes.versions.CompleteVersion v =
            new com.google.gson.GsonBuilder()
                .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
                .registerTypeAdapter(java.util.Date.class, new DateTypeAdapter())
                .create()
                .fromJson(json, fr.wdes.versions.CompleteVersion.class);
        assertNotNull(v);
        assertNotNull(v.getDownloads());
        assertNotNull(v.getDownloads().client);
        assertEquals("https://piston-data.mojang.com/v1/objects/deadbeef/client.jar",
                v.getDownloads().client.url);
        assertEquals(5000000L, v.getDownloads().client.size);
        // Server block retained even though the launcher doesn't download it,
        // so round-tripping the JSON to disk doesn't silently drop fields.
        assertEquals("https://piston-data.mojang.com/v1/objects/cafebabe/server.jar",
                v.getDownloads().server.url);
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

    @Test
    public void mergeIntoManifestBody_operatorOverridesMojangOnIdCollision() {
        final MojangVersionManifest mojang = new MojangVersionManifest();
        mojang.latest = new HashMap<String, String>();
        mojang.latest.put("release", "1.20.4");
        mojang.versions = new java.util.ArrayList<MojangVersionManifest.Entry>();
        final MojangVersionManifest.Entry vanilla = new MojangVersionManifest.Entry();
        vanilla.id = "1.7.10";
        vanilla.type = "release";
        vanilla.url = "https://piston-meta.mojang.com/v1/packages/def/1.7.10.json";
        mojang.versions.add(vanilla);
        final MojangVersionManifest.Entry modern = new MojangVersionManifest.Entry();
        modern.id = "1.20.4";
        modern.type = "release";
        modern.url = "https://piston-meta.mojang.com/v1/packages/abc/1.20.4.json";
        mojang.versions.add(modern);

        final RemoteVersionList.OperatorVersionList operator = new RemoteVersionList.OperatorVersionList();
        operator.versions = new java.util.ArrayList<RemoteVersionList.OperatorEntry>();
        final RemoteVersionList.OperatorEntry forge = new RemoteVersionList.OperatorEntry();
        forge.id = "1.7.10-forge10.13.4";
        forge.type = "release";
        operator.versions.add(forge);
        // Intentional collision: operator ships its own "1.7.10".
        final RemoteVersionList.OperatorEntry customVanilla = new RemoteVersionList.OperatorEntry();
        customVanilla.id = "1.7.10";
        customVanilla.type = "release";
        operator.versions.add(customVanilla);

        final String merged = RemoteVersionList.mergeIntoManifestBody(mojang, operator);
        final JsonObject root = new JsonParser().parse(merged).getAsJsonObject();
        final JsonArray versions = root.getAsJsonArray("versions");
        assertNotNull(versions);

        // All three unique ids are present.
        final Set<String> ids = new HashSet<String>();
        int collisionIndex = -1;
        int modernIndex    = -1;
        int forgeIndex     = -1;
        for (int i = 0; i < versions.size(); i++) {
            final JsonObject v = versions.get(i).getAsJsonObject();
            final String id = v.get("id").getAsString();
            ids.add(id);
            if ("1.7.10".equals(id))             collisionIndex = i;
            if ("1.20.4".equals(id))             modernIndex = i;
            if ("1.7.10-forge10.13.4".equals(id)) forgeIndex = i;
        }
        assertTrue(ids.contains("1.7.10"));
        assertTrue(ids.contains("1.20.4"));
        assertTrue(ids.contains("1.7.10-forge10.13.4"));

        // Operator-sourced entries win for colliding ids: they have NO url
        // (served from the mirror), so the downstream getContent code falls
        // back to URL_DOWNLOAD_VERSIONS_BASE instead of Mojang.
        final JsonObject collisionEntry = versions.get(collisionIndex).getAsJsonObject();
        assertFalse("operator should shadow Mojang's url", collisionEntry.has("url"));

        // Operator-only entries likewise have no url.
        assertFalse(versions.get(forgeIndex).getAsJsonObject().has("url"));

        // Mojang-only entries keep their piston-meta url for direct fetch.
        assertEquals("https://piston-meta.mojang.com/v1/packages/abc/1.20.4.json",
                versions.get(modernIndex).getAsJsonObject().get("url").getAsString());

        // Operator entries come first in the list.
        assertTrue("operator ids should appear before Mojang-only ones",
                Math.max(collisionIndex, forgeIndex) < modernIndex);
    }

    @Test
    public void mergeIntoManifestBody_operatorAlone() {
        final RemoteVersionList.OperatorVersionList operator = new RemoteVersionList.OperatorVersionList();
        operator.versions = new java.util.ArrayList<RemoteVersionList.OperatorEntry>();
        final RemoteVersionList.OperatorEntry only = new RemoteVersionList.OperatorEntry();
        only.id = "1.7.10-forge10.13.4";
        only.type = "release";
        operator.versions.add(only);

        final String merged = RemoteVersionList.mergeIntoManifestBody(null, operator);
        final JsonObject root = new JsonParser().parse(merged).getAsJsonObject();
        assertEquals(1, root.getAsJsonArray("versions").size());
        assertEquals("1.7.10-forge10.13.4",
                root.getAsJsonArray("versions").get(0).getAsJsonObject().get("id").getAsString());
    }

    @Test
    public void mergeIntoManifestBody_mojangAlone() {
        final MojangVersionManifest mojang = new MojangVersionManifest();
        mojang.versions = new java.util.ArrayList<MojangVersionManifest.Entry>();
        final MojangVersionManifest.Entry e = new MojangVersionManifest.Entry();
        e.id = "1.20.4";
        e.type = "release";
        e.url = "https://piston-meta.mojang.com/v1/packages/abc/1.20.4.json";
        mojang.versions.add(e);

        final String merged = RemoteVersionList.mergeIntoManifestBody(mojang, null);
        final JsonObject root = new JsonParser().parse(merged).getAsJsonObject();
        assertEquals(1, root.getAsJsonArray("versions").size());
        assertEquals("1.20.4", root.getAsJsonArray("versions").get(0).getAsJsonObject().get("id").getAsString());
        assertEquals("https://piston-meta.mojang.com/v1/packages/abc/1.20.4.json",
                root.getAsJsonArray("versions").get(0).getAsJsonObject().get("url").getAsString());
    }
}
