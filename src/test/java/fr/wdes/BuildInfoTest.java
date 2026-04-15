package fr.wdes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Smoke test. The git-commit-id-maven-plugin runs during the {@code initialize}
 * phase so by the time {@code surefire} picks this up the properties file is
 * either present (commit + branch + timestamps) or genuinely absent (the
 * plugin couldn't detect a git directory, in which case the fields fall back
 * to {@code "unknown"}). Either outcome is valid - what must never happen is
 * BuildInfo throwing on class init.
 */
public class BuildInfoTest {

    @Test
    public void buildInfoLoadsWithoutError() {
        // Static init has already run by the time we read a field.
        assertNotNull(BuildInfo.COMMIT_ABBREV);
        assertNotNull(BuildInfo.COMMIT_FULL);
        assertNotNull(BuildInfo.BRANCH);
        assertNotNull(BuildInfo.BUILD_TIME);
        assertNotNull(BuildInfo.COMMIT_TIME);
    }

    @Test
    public void summaryContainsAtLeastCommitAndBranch() {
        final String s = BuildInfo.summary();
        assertNotNull(s);
        assertTrue("summary exposes commit", s.contains("commit="));
        assertTrue("summary exposes branch", s.contains("branch="));
        assertTrue("summary exposes build time", s.contains("built="));
        // Nothing should leak a literal ${...} token from an unresolved
        // Maven property - if the plugin ran, the value is the rendered
        // hash/time; if it didn't, the fallback is "unknown".
        assertFalse("no unresolved maven placeholder", s.contains("${"));
    }
}
