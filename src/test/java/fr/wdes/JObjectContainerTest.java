package fr.wdes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;

import org.junit.Test;

/**
 * Verifies that the structured links / socials shape from the operator's web
 * config deserialises straight into {@link JObjectContainer}'s public fields.
 */
public class JObjectContainerTest {

    @Test
    public void parsesStructuredLinksAndSocials() {
        final String json = "{\n" +
                "  \"nom\": \"Wdes Launcher\",\n" +
                "  \"version\": \"1.7.10\",\n" +
                "  \"links\": [\n" +
                "    {\"url\": \"https://wdes.fr\", \"tooltip\": \"Site\", \"name\": \"Wdes\"},\n" +
                "    {\"url\": \"https://launchers.wdes.fr\", \"tooltip\": \"Forum\", \"name\": \"Forum\"}\n" +
                "  ],\n" +
                "  \"socials\": {\n" +
                "    \"twitter\": {\"url\": \"https://twitter.com/wdes\", \"tooltip\": \"Follow us\"},\n" +
                "    \"youtube\": {\"url\": \"https://youtube.com/@wdes\", \"tooltip\": \"Subscribe\"}\n" +
                "  }\n" +
                "}";

        final JObjectContainer cfg = new Gson().fromJson(json, JObjectContainer.class);
        assertNotNull(cfg);
        assertNotNull(cfg.links);
        assertEquals(2, cfg.links.size());
        assertEquals("Wdes", cfg.links.get(0).name);
        assertEquals("https://wdes.fr", cfg.links.get(0).url);
        assertEquals("Forum", cfg.links.get(1).name);

        assertNotNull(cfg.socials);
        assertEquals(2, cfg.socials.size());
        assertEquals("https://twitter.com/wdes", cfg.socials.get("twitter").url);
        assertEquals("Subscribe", cfg.socials.get("youtube").tooltip);
    }

    @Test
    public void absentLinksAndSocialsRemainNull() {
        final String json = "{\"nom\": \"Wdes\", \"version\": \"1.7.10\"}";

        final JObjectContainer cfg = new Gson().fromJson(json, JObjectContainer.class);
        assertNotNull(cfg);
        // null collections are how the launcher's "skip rendering" path
        // detects that the operator didn't configure anything.
        org.junit.Assert.assertNull(cfg.links);
        org.junit.Assert.assertNull(cfg.socials);
    }
}
