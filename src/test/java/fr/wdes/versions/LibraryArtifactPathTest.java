package fr.wdes.versions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Pins down how {@link Library} turns its Maven-coordinate name into a
 * Maven-layout path under {@code libraries.minecraft.net}. Both the legacy
 * shape (three-part name, classifier supplied via the {@code natives.os} map)
 * and the modern 1.13+ shape (four-part name with the classifier baked in)
 * must produce the same canonical
 * {@code <groupPath>/<artifact>/<version>/<artifact>-<version>[-<classifier>].jar}.
 *
 * <p>Regression guard for the 1.19 / LWJGL 3.3.1 bug where a {@code split(":",
 * 3)} collapsed the classifier into the version and produced 404-bait URLs
 * like {@code .../3.3.1:natives-linux/lwjgl-3.3.1:natives-linux.jar}.
 */
public class LibraryArtifactPathTest {

    @Test
    public void threePartNameUsesExplicitClassifier() {
        final Library l = new Library("org.lwjgl.lwjgl:lwjgl:2.9.1");
        assertEquals("org/lwjgl/lwjgl/lwjgl/2.9.1/lwjgl-2.9.1-natives-linux.jar",
                l.getArtifactPath("natives-linux"));
        assertEquals("org/lwjgl/lwjgl/lwjgl/2.9.1/lwjgl-2.9.1.jar",
                l.getArtifactPath());
    }

    @Test
    public void fourPartNamePicksUpEmbeddedClassifier() {
        // Modern Mojang 1.13+ natives layout. No natives map, classifier
        // lives as parts[3] of the name itself.
        final Library l = new Library("org.lwjgl:lwjgl:3.3.1:natives-linux");
        assertEquals("org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1-natives-linux.jar",
                l.getArtifactPath());
    }

    @Test
    public void explicitClassifierWinsOverEmbeddedOne() {
        // Shouldn't happen in practice (the modern format doesn't use the
        // natives map), but if both are present the explicit argument is
        // the one getRequiredDownloadables asked for - honour it.
        final Library l = new Library("org.lwjgl:lwjgl:3.3.1:natives-linux");
        assertEquals("org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1-natives-macos.jar",
                l.getArtifactPath("natives-macos"));
    }

    @Test
    public void plainThreePartNameHasNoSpuriousClassifier() {
        final Library l = new Library("com.mojang:realms:1.3.5");
        assertEquals("com/mojang/realms/1.3.5/realms-1.3.5.jar",
                l.getArtifactPath());
    }
}
