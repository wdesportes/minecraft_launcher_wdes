package fr.wdes;

import java.io.File;
import fr.wdes.LauncherConstants;

public class Utilis {
    public static enum OS {
        WINDOWS, MACOS, SOLARIS, LINUX, UNKNOWN;
    }

    public static OS getPlatform() {
        final String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("win"))
            return OS.WINDOWS;
        if(osName.contains("mac"))
            return OS.MACOS;
        if(osName.contains("linux"))
            return OS.LINUX;
        if(osName.contains("unix"))
            return OS.LINUX;
        return OS.UNKNOWN;
    }

    public static File getWorkingDirectory() {
        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch(getPlatform()) {
        case SOLARIS:
        case LINUX:
            workingDirectory = new File(userHome, "." + LauncherConstants.APPDATA + "/");
            break;
        case WINDOWS:
            final String applicationData = System.getenv("APPDATA");
            final String folder = applicationData != null ? applicationData : userHome;

            workingDirectory = new File(folder, "." + LauncherConstants.APPDATA + "/");
            break;
        case MACOS:
            workingDirectory = new File(userHome, "Library/Application Support/" + LauncherConstants.APPDATA);
            break;
        default:
            workingDirectory = new File(userHome, LauncherConstants.APPDATA + "/");
        }

        return workingDirectory;
    }
}
 @SuppressWarnings("serial")
class FatalBootstrapError extends RuntimeException {
    public FatalBootstrapError(final String reason) {
        super(reason);
    }
}
