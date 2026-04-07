package fr.wdes.launchers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLHandshakeException;
import net.minecraft.launcher.LauncherConstants;

public class Util {
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
            workingDirectory = new File(userHome, "." + LauncherConstants.APPLICATION_NAME + "/");
            break;
        case WINDOWS:
            final String applicationData = System.getenv("APPDATA");
            final String folder = applicationData != null ? applicationData : userHome;

            workingDirectory = new File(folder, "." + LauncherConstants.APPLICATION_NAME + "/");
            break;
        case MACOS:
            workingDirectory = new File(userHome, "Library/Application Support/" + LauncherConstants.APPLICATION_NAME);
            break;
        default:
            workingDirectory = new File(userHome, LauncherConstants.APPLICATION_NAME + "/");
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
 class Downloader implements Runnable {
	    public static class Controller {
	        public final CountDownLatch foundUpdateLatch = new CountDownLatch(1);
	        public final AtomicBoolean foundUpdate = new AtomicBoolean(false);
	        public final CountDownLatch hasDownloadedLatch = new CountDownLatch(1);
	    }

	    //private static final int MAX_RETRIES = 10;
	    private final Proxy proxy;
	    private final String currentMd5;
	    private final File targetFile;
	    private final Controller controller;

	    private final Wdes bootstrap;

	    public Downloader(final Controller controller, final Wdes bootstrap, final Proxy proxy, final String currentMd5, final File targetFile) {
	        this.controller = controller;
	        this.bootstrap = bootstrap;
	        this.proxy = proxy;
	        this.currentMd5 = currentMd5;
	        this.targetFile = targetFile;
	    }

	    public HttpURLConnection getConnection(final URL url) throws IOException {
	        return (HttpURLConnection) url.openConnection(proxy);
	    }

	    public void log(final String str) {
	        bootstrap.println(str);
	    }

	    public void run() {
	        int retries = 0;
	        while(true) {
	            retries++;
	            if(retries > 10)
	                break;
	            try {
	                final URL url = new URL(LauncherConstants.LAUNCHER_URL);

	                final HttpURLConnection connection = getConnection(url);

	                connection.setUseCaches(false);
	                connection.setDefaultUseCaches(false);
	                connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
	                connection.setRequestProperty("Expires", "0");
	                connection.setRequestProperty("Pragma", "no-cache");
	                if(currentMd5 != null)
	                    connection.setRequestProperty("If-None-Match", currentMd5.toLowerCase());

	                connection.setConnectTimeout(30000);
	                connection.setReadTimeout(10000);

	                log(new StringBuilder().append("Downloading: ").append(LauncherConstants.LAUNCHER_URL).append(retries > 1 ? String.format(" (try %d/%d)", new Object[] { Integer.valueOf(retries), Integer.valueOf(10) }) : "").toString());
	                final long start = System.nanoTime();
	                connection.connect();
	                final long elapsed = System.nanoTime() - start;
	                log(new StringBuilder().append("Got reply in: ").append(elapsed / 1000000L).append("ms").toString());

	                final int code = connection.getResponseCode() / 100;

	                if(code == 2) {
	                    String eTag = connection.getHeaderField("ETag");

	                    if(eTag == null)
	                        eTag = "-";
	                    else
	                        eTag = eTag.substring(1, eTag.length() - 1);

	                    controller.foundUpdate.set(true);
	                    controller.foundUpdateLatch.countDown();

	                    final InputStream inputStream = connection.getInputStream();
	                    final FileOutputStream outputStream = new FileOutputStream(targetFile);

	                    final MessageDigest digest = MessageDigest.getInstance("MD5");

	                    final long startDownload = System.nanoTime();
	                    long bytesRead = 0L;
	                    final byte[] buffer = new byte[65536];
	                    try {
	                        int read = inputStream.read(buffer);
	                        while(read >= 1) {
	                            bytesRead += read;
	                            digest.update(buffer, 0, read);
	                            outputStream.write(buffer, 0, read);
	                            read = inputStream.read(buffer);
	                        }
	                    }
	                    finally {
	                        inputStream.close();
	                        outputStream.close();
	                    }
	                    final long elapsedDownload = System.nanoTime() - startDownload;

	                    final float elapsedSeconds = (1L + elapsedDownload) / 1.0E+09F;
	                    final float kbRead = bytesRead / 1024.0F;
	                    log(String.format("Downloaded %.1fkb in %ds at %.1fkb/s", new Object[] { Float.valueOf(kbRead), Integer.valueOf((int) elapsedSeconds), Float.valueOf(kbRead / elapsedSeconds) }));

	                    final String md5sum = String.format("%1$032x", new Object[] { new BigInteger(1, digest.digest()) });
	                    if(!eTag.contains("-") && !eTag.equalsIgnoreCase(md5sum))
	                        log("After downloading, the MD5 hash didn't match. Retrying");
	                    else {
	                        controller.hasDownloadedLatch.countDown();
	                        return;
	                    }
	                }
	                else if(code == 4)
	                    log("Remote file not found.");
	                else {
	                    controller.foundUpdate.set(false);
	                    controller.foundUpdateLatch.countDown();
	                    log("No update found.");
	                    return;
	                }
	            }
	            catch(final Exception e) {
	                log(new StringBuilder().append("Exception: ").append(e.toString()).toString());
	                suggestHelp(e);
	            }
	        }

	        log("Unable to download remote file. Check your internet connection/proxy settings.");
	    }

	    public void suggestHelp(final Throwable t) {
	        if(t instanceof BindException)
	            log("Recognized exception: the likely cause is a broken ipv4/6 stack. Check your TCP/IP settings.");
	        else if(t instanceof SSLHandshakeException)
	            log("Recognized exception: the likely cause is a set of broken/missing root-certificates. Check your java install and perhaps reinstall it.");
	    }
	}