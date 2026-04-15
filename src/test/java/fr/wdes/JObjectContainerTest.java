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

    /**
     * Real config served by the operator's site. Width / height come through
     * as JSON numbers; the launcher tolerates that even though the fields are
     * declared as String, but we keep an eye on it via the test.
     */
    @Test
    public void parsesActualOperatorConfig() {
        final String json = "{\n" +
                "  \"nom\": \"Wdes\",\n" +
                "  \"version\": \"1.7.10\",\n" +
                "  \"width\": 1920,\n" +
                "  \"height\": 720,\n" +
                "  \"URL_FONDS_DOWNLOAD\": \"http://wdeslaunchers.wdes.fr/fonds/\",\n" +
                "  \"PLACEHOLDER_LOGIN\": \"Utilisateur\",\n" +
                "  \"PLACEHOLDER_PASSD\": \"Mot de passe\",\n" +
                "  \"URL_AUTHENTIFICATION_SYSTEM\": \"https://kureuils.servers.wdes.eu/minecraft-auth/\",\n" +
                "  \"links\": [\n" +
                "    {\"url\": \"https://wdes.fr\",         \"tooltip\": \"Site\",   \"name\": \"Wdes\"},\n" +
                "    {\"url\": \"https://blog.williamdes.eu\",\"tooltip\": \"Blog\",  \"name\": \"Blog\"},\n" +
                "    {\"url\": \"https://williamdes.eu\", \"tooltip\": \"Site personnel\", \"name\": \"Williamdes\"}\n" +
                "  ],\n" +
                "  \"socials\": {\n" +
                "    \"twitter\":  {\"url\": \"https://x.com/wdesportes\",   \"tooltip\": \"Find me on X.com\"},\n" +
                "    \"youtube\":  {\"url\": \"https://www.youtube.com/@williamdes\",  \"tooltip\": \"My playlists\"},\n" +
                "    \"steam\":    {\"url\": \"https://steamcommunity.com/id/williamdes\",\"tooltip\": \"Add me on Steam\"}\n" +
                "  }\n" +
                "}";

        final JObjectContainer cfg = new Gson().fromJson(json, JObjectContainer.class);
        assertNotNull(cfg);
        assertEquals("Wdes", cfg.nom);
        assertEquals("1.7.10", cfg.version);
        // Width / height are declared as String to keep older configs (which
        // used quoted values) working - Gson's lenient string coercion turns
        // the unquoted numbers in the live config into "1920" / "720".
        assertEquals("1920", cfg.width);
        assertEquals("720", cfg.height);
        assertEquals("http://wdeslaunchers.wdes.fr/fonds/", cfg.URL_FONDS_DOWNLOAD);
        assertEquals("Utilisateur", cfg.PLACEHOLDER_LOGIN);
        assertEquals("Mot de passe", cfg.PLACEHOLDER_PASSD);
        assertEquals("https://kureuils.servers.wdes.eu/minecraft-auth/", cfg.URL_AUTHENTIFICATION_SYSTEM);

        assertNotNull(cfg.links);
        assertEquals(3, cfg.links.size());
        assertEquals("Wdes", cfg.links.get(0).name);
        assertEquals("Blog", cfg.links.get(1).name);
        assertEquals("Williamdes", cfg.links.get(2).name);
        assertEquals("Site personnel", cfg.links.get(2).tooltip);

        assertNotNull(cfg.socials);
        assertEquals(3, cfg.socials.size());
        assertEquals("https://x.com/wdesportes", cfg.socials.get("twitter").url);
        assertEquals("My playlists", cfg.socials.get("youtube").tooltip);
        assertEquals("https://steamcommunity.com/id/williamdes", cfg.socials.get("steam").url);
    }
}
