package fr.wdes.profile;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.wdes.LauncherConstants;
import fr.wdes.updater.VersionFilter;
import fr.wdes.versions.ReleaseType;


public class Profile {
    public static class Resolution {
    	protected int width;
    	protected int height;

        public Resolution() {
        }

        public Resolution(final int width, final int height) {
            this.width = width;
            this.height = height;
        }

        public Resolution(final Resolution resolution) {
            this(resolution.getWidth(), resolution.getHeight());
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }
    }

    public static final String DEFAULT_JRE_ARGUMENTS_64BIT = "-Xmx1G";
    public static final String DEFAULT_JRE_ARGUMENTS_32BIT = "-Xmx512M";
    public static final Resolution DEFAULT_RESOLUTION = new Resolution(854, 480);
    public static final LauncherVisibilityRule DEFAULT_LAUNCHER_VISIBILITY = LauncherVisibilityRule.CLOSE_LAUNCHER;
    public static final Set<ReleaseType> DEFAULT_RELEASE_TYPES = new HashSet<ReleaseType>(Arrays.asList(new ReleaseType[] { ReleaseType.RELEASE }));
    protected String name;
    protected File gameDir;
    protected String lastVersionId;
    protected String javaDir;
    protected String javaArgs;
    protected Resolution resolution;
    protected Set<ReleaseType> allowedReleaseTypes;
    protected String playerUUID;


    protected LauncherVisibilityRule launcherVisibilityOnGameClose;

    @Deprecated
    protected Map<String, String> authentication;

    public Profile() {
    }

    public Profile(final Profile copy) {
        name = copy.name;
        gameDir = copy.gameDir;
        playerUUID = copy.playerUUID;
        //copy.lastVersionId
        lastVersionId = copy.lastVersionId;
        javaDir = copy.javaDir;
        javaArgs = copy.javaArgs;
        resolution = copy.resolution == null ? null : new Resolution(copy.resolution);
        allowedReleaseTypes = copy.allowedReleaseTypes == null ? null : new HashSet<ReleaseType>(copy.allowedReleaseTypes);
        launcherVisibilityOnGameClose = copy.launcherVisibilityOnGameClose;
    }

    public Profile(final String name) {
        this.name = name;
    }

    public Set<ReleaseType> getAllowedReleaseTypes() {
        return allowedReleaseTypes;
    }

    @Deprecated
    public Map<String, String> getAuthentication() {
        return authentication;
    }

    public File getGameDir() {
        return gameDir;
    }

    public String getJavaArgs() {
        return javaArgs;
    }

    public String getJavaPath() {
        return javaDir;
    }

    public String getLastVersionId() {
    	return resolveVersionId(lastVersionId, LauncherConstants.VERSION_MINECRAFT);
    }

    /**
     * Decide which version id to launch. The user's explicit pick on the
     * profile always wins; if they haven't picked one, the operator can pin
     * a version through the web config's {@code version} field. The magic
     * value {@code "auto-mc"} (or null / empty) means "no operator pin -
     * let the launcher pick the latest" and is the default for new configs.
     */
    public static String resolveVersionId(final String profileVersion, final String operatorPin) {
    	if (profileVersion != null && profileVersion.length() > 0) {
    		return profileVersion;
    	}
    	if (operatorPin != null && operatorPin.length() > 0 && !operatorPin.equals("auto-mc")) {
    		return operatorPin;
    	}
    	return null;
    }

    public LauncherVisibilityRule getLauncherVisibilityOnGameClose() {
        return launcherVisibilityOnGameClose;
    }

    public String getName() {
        return name;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public Resolution getResolution() {
        return resolution;
    }



    public VersionFilter getVersionFilter() {
        final VersionFilter filter = new VersionFilter().setMaxCount(2147483647);

        if(allowedReleaseTypes == null)
            filter.onlyForTypes(DEFAULT_RELEASE_TYPES.toArray(new ReleaseType[DEFAULT_RELEASE_TYPES.size()]));
        else
            filter.onlyForTypes(allowedReleaseTypes.toArray(new ReleaseType[allowedReleaseTypes.size()]));

        return filter;
    }

    public void setAllowedReleaseTypes(final Set<ReleaseType> allowedReleaseTypes) {
        this.allowedReleaseTypes = allowedReleaseTypes;
    }

    @Deprecated
    public void setAuthentication(final Map<String, String> authentication) {
        this.authentication = authentication;
    }

    public void setGameDir(final File gameDir) {
        //this.gameDir = gameDir;
    }

    public void setJavaArgs(final String javaArgs) {
        this.javaArgs = javaArgs;
    }

    public void setJavaDir(final String javaDir) {
        this.javaDir = javaDir;
    }

    public void setLastVersionId(final String lastVersionId) {
        // The assignment used to be commented out, which silently turned
        // every settings-dropdown change into a no-op - the user could
        // pick another version, click Save, and the field never moved.
        this.lastVersionId = lastVersionId;
    }

    public void setLauncherVisibilityOnGameClose(final LauncherVisibilityRule launcherVisibilityOnGameClose) {
        this.launcherVisibilityOnGameClose = launcherVisibilityOnGameClose;
    }

    public void setName(final String name) {
       this.name = name;
    }

    public void setPlayerUUID(final String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setResolution(final Resolution resolution) {
        this.resolution = resolution;
    }


}