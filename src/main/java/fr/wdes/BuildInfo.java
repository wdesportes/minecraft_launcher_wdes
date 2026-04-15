package fr.wdes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Exposes the {@code git.properties} file generated at build time by
 * {@code git-commit-id-maven-plugin} so the launcher can report exactly
 * which commit is running.
 *
 * <p>Every field falls back to {@code "unknown"} if:
 * <ul>
 *   <li>the jar was built outside a git working tree (the plugin is
 *       configured with {@code failOnNoGitDirectory=false});</li>
 *   <li>the resource is missing for any other reason (custom packaging,
 *       running out of the IDE without the plugin executing, etc.).</li>
 * </ul>
 * That way a bug report always includes <em>something</em> in the boot
 * marker - either a real commit hash pinning down the running tree, or
 * an explicit "unknown" telling us the report is from a non-standard
 * build and we should ask which source it was built from.
 */
public final class BuildInfo {
    public static final String COMMIT_ABBREV;
    public static final String COMMIT_FULL;
    public static final String BRANCH;
    public static final String BUILD_TIME;
    public static final String COMMIT_TIME;
    public static final boolean DIRTY;

    private static final String UNKNOWN = "unknown";

    static {
        final Properties props = new Properties();
        InputStream in = null;
        try {
            in = BuildInfo.class.getResourceAsStream("/git.properties");
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // Fall through to the UNKNOWN defaults.
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException ignored) { }
            }
        }
        COMMIT_ABBREV = props.getProperty("git.commit.id.abbrev", UNKNOWN);
        COMMIT_FULL   = props.getProperty("git.commit.id.full",   UNKNOWN);
        BRANCH        = props.getProperty("git.branch",           UNKNOWN);
        BUILD_TIME    = props.getProperty("git.build.time",       UNKNOWN);
        COMMIT_TIME   = props.getProperty("git.commit.time",      UNKNOWN);
        DIRTY         = Boolean.parseBoolean(props.getProperty("git.dirty", "false"));
    }

    private BuildInfo() {}

    /** One-line summary suitable for the boot-marker log line. */
    public static String summary() {
        final StringBuilder sb = new StringBuilder();
        sb.append("commit=").append(COMMIT_ABBREV);
        if (DIRTY) {
            sb.append("-dirty");
        }
        sb.append(" branch=").append(BRANCH);
        sb.append(" built=").append(BUILD_TIME);
        return sb.toString();
    }
}
