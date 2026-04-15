package fr.wdes.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Pins down the version-id resolution rules used by
 * {@link Profile#getLastVersionId()}. The bug the user hit was that the
 * operator's web-config {@code version: "1.7.10"} was always returned even
 * when the profile had explicitly picked another version through the
 * settings dropdown. The contract now is:
 * <ul>
 *   <li>profile pick wins when set;</li>
 *   <li>operator pin is the fallback;</li>
 *   <li>{@code "auto-mc"} (or null/empty) on the operator side means "no pin".</li>
 * </ul>
 */
public class ProfileVersionResolutionTest {

    @Test
    public void profilePickWinsOverOperatorPin() {
        // The reported failure: dropdown selected 1.5.2, config pinned 1.7.10.
        // 1.5.2 must win.
        assertEquals("1.5.2", Profile.resolveVersionId("1.5.2", "1.7.10"));
    }

    @Test
    public void operatorPinUsedWhenProfileHasNoPick() {
        assertEquals("1.7.10", Profile.resolveVersionId(null, "1.7.10"));
        assertEquals("1.7.10", Profile.resolveVersionId("",   "1.7.10"));
    }

    @Test
    public void autoMcMagicValueDoesNotPin() {
        assertNull(Profile.resolveVersionId(null, "auto-mc"));
    }

    @Test
    public void allNullReturnsNull() {
        assertNull(Profile.resolveVersionId(null, null));
        assertNull(Profile.resolveVersionId("", ""));
    }

    /**
     * Regression: {@link Profile#setLastVersionId(String)} used to have its
     * assignment commented out, so dropdown picks were silently dropped.
     * Reach into the protected field (same package) so we don't depend on
     * {@link Profile#getLastVersionId()} pulling on LauncherConstants.
     */
    @Test
    public void setLastVersionIdActuallyAssigns() {
        final Profile p = new Profile("test");
        org.junit.Assert.assertNull(p.lastVersionId);

        p.setLastVersionId("1.5.2");
        org.junit.Assert.assertEquals("1.5.2", p.lastVersionId);

        p.setLastVersionId(null);
        org.junit.Assert.assertNull(p.lastVersionId);
    }
}
