package fr.wdes.download;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import fr.wdes.LauncherConstants;
import fr.wdes.logger;


public class Downloadable {
    protected static String name = "";

	public static void closeSilently(final Closeable closeable) {
        if(closeable != null)
            try {
                closeable.close();
            }
            catch(final IOException localIOException) {
            }
    }

    public static String copyAndDigest(final InputStream inputStream, final OutputStream outputStream) throws IOException, NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("MD5");
        final byte[] buffer = new byte[65536];
        try {
            int read = inputStream.read(buffer);
            while(read >= 1) {
                digest.update(buffer, 0, read);
                outputStream.write(buffer, 0, read);
                read = inputStream.read(buffer);
            }
        }
        finally {
            closeSilently(inputStream);
            closeSilently(outputStream);
        }

        return String.format("%1$032x", new Object[] { new BigInteger(1, digest.digest()) });
    }

    public static String getEtag(final HttpURLConnection connection) {
        return getEtag(connection.getHeaderField("ETag"));
    }

    public static String getEtag(String etag) {
        if(etag == null)
            etag = "-";
        else if(etag.startsWith("\"") && etag.endsWith("\""))
            etag = etag.substring(1, etag.length() - 1);

        return etag;
    }

    public static String getMD5(final File file) {
        DigestInputStream stream = null;
        try {
            stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5"));
            final byte[] buffer = new byte[65536];

            int read = stream.read(buffer);
            while(read >= 1)
                read = stream.read(buffer);
        }
        catch(final Exception ignored) {
            return null;
        }
        finally {
            closeSilently(stream);
        }

        return String.format("%1$032x", new Object[] { new BigInteger(1, stream.getMessageDigest().digest()) });
    }

    protected final URL url;
    protected final File target;

    protected final boolean forceDownload;

    protected final Proxy proxy;

    protected final ProgressContainer monitor;

    protected int numAttempts;

    protected long expectedSize;

    public Downloadable(final Proxy proxy, final URL remoteFile, final File localFile, final boolean forceDownload) {
        this.proxy = proxy;
        url = remoteFile;
        target = localFile;
        this.forceDownload = forceDownload;
        monitor = new ProgressContainer();
    }
    protected void ensureFileWritable(File target)
    {
      if ((target.getParentFile() != null) && (!target.getParentFile().isDirectory()))
      {
    	  logger.info("Making directory " + target.getParentFile());
        if ((!target.getParentFile().mkdirs()) && 
          (!target.getParentFile().isDirectory())) {
          throw new RuntimeException("Could not create directory " + target.getParentFile());
        }
      }
      if ((target.isFile()) && (!target.canWrite())) {
        throw new RuntimeException("Do not have write permissions for " + target + " - aborting!");
      }
    }
    protected void updateExpectedSize(HttpURLConnection connection)
    {
      if (this.expectedSize == 0L)
      {
        this.monitor.setTotal(connection.getContentLength());
        setExpectedSize(connection.getContentLength());
      }
      else
      {
        this.monitor.setTotal(this.expectedSize);
      }
    }
    public static String getDigest(File file, String algorithm, int hashLength)
    {
      DigestInputStream stream = null;
      int read;
      try
      {
        stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(algorithm));
        byte[] buffer = new byte[65536];
        do
        {
          read = stream.read(buffer);
        } while (read > 0);
      }
      catch (Exception ignored)
      {
        
        return null;
      }
      finally
      {
        closeSilently(stream);
      }
      return String.format("%1$0" + hashLength + "x", new Object[] { new BigInteger(1, stream.getMessageDigest().digest()) });
    }
    public static String copyAndDigest(InputStream inputStream, OutputStream outputStream, String algorithm, int hashLength)
    	    throws IOException
    	  {
    	    MessageDigest digest;
    	    try
    	    {
    	      digest = MessageDigest.getInstance(algorithm);
    	    }
    	    catch (NoSuchAlgorithmException e)
    	    {
    	      closeSilently(inputStream);
    	      closeSilently(outputStream);
    	      throw new RuntimeException("Missing Digest." + algorithm, e);
    	    }
    	    byte[] buffer = new byte[65536];
    	    try
    	    {
    	      int read = inputStream.read(buffer);
    	      while (read >= 1)
    	      {
    	        digest.update(buffer, 0, read);
    	        outputStream.write(buffer, 0, read);
    	        read = inputStream.read(buffer);
    	      }
    	    }
    	    finally
    	    {
    	      closeSilently(inputStream);
    	      closeSilently(outputStream);
    	    }
    	    return String.format("%1$0" + hashLength + "x", new Object[] { new BigInteger(1, digest.digest()) });
    	  }
    public String download() throws IOException {
    	String localMd5 = null;
    	String localDate = null;
        numAttempts += 1;

        if(target.getParentFile() != null && !target.getParentFile().isDirectory())
            target.getParentFile().mkdirs();
        if(!forceDownload && target.isFile()){
            //localMd5 = getMD5(target);
            localDate = getGMT(target.lastModified());
        }
        name = target.getName();
        if(target.isFile() && !target.canWrite())
            throw new RuntimeException("Do not have write permissions for " + target + " - aborting!");
        try {
            final HttpURLConnection connection = makeConnection(localMd5,localDate);
            final int status = connection.getResponseCode();
            //Etag ou
            if(status == 304)
                return "[304] Fichiers identiques,vérification par Last-Modified";
            if(status == 505)
                return "[505]Fichier indisponible";
            if(status == 200) {
                if(expectedSize == 0L)
                    monitor.setTotal(connection.getContentLength());
                else
                    monitor.setTotal(expectedSize);

                final InputStream inputStream = new MonitoringInputStream(connection.getInputStream(), monitor);
                final FileOutputStream outputStream = new FileOutputStream(target);
                final String md5 = copyAndDigest(inputStream, outputStream);
                final String etag = getEtag(connection);

                if(etag.contains("-"))
                    return "[200] Fichier téléchargé,pas d'Etag";
                if(etag.equalsIgnoreCase(md5))
                    return "[200] Fichier téléchargé,vérifié par Etag";
                throw new RuntimeException(String.format("E-tag did not match downloaded MD5 (ETag was %s, downloaded %s)", new Object[] { etag, md5 }));
            }
            if(target.isFile())
                return "[" + status + "] Serveur innacessible,fichier local disponible,non vérifié";
            throw new RuntimeException("Status du serveur : " + status);
        }
        catch(final IOException e) {
            if(target.isFile())
                return "Impossible de se connecter au serveur (" + e.getClass().getSimpleName() + ": '" + e.getMessage() + "') ,fichier local disponible,non vérifié";
            throw e;
        }
        catch(final NoSuchAlgorithmException e) {
            throw new RuntimeException("Missing Digest.MD5", e);
        }
    }

    private String getGMT(long lastModified) {
    	DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.US);
    	dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); 
    	String GMT = dateFormat.format(lastModified);
		return GMT;
	}

	public long getExpectedSize() {
        return expectedSize;
    }
    
    public String getName() {
        return name;
    }
    public ProgressContainer getMonitor() {
        return monitor;
    }

    public int getNumAttempts() {
        return numAttempts;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public File getTarget() {
        return target;
    }

    public URL getUrl() {
        return url;
    }

    protected HttpURLConnection makeConnection(final String localMd5,final String localDate) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        connection.setRequestProperty("User-Agent", LauncherConstants.USER_AGENT);
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");
        if(localMd5 != null)
            connection.setRequestProperty("If-None-Match", localMd5);
        if(localDate != null)
            connection.setRequestProperty("If-Modified-Since", localDate);
        

        connection.connect();

        return connection;
    }
    protected HttpURLConnection makeConnection(final String localMd5) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        connection.setRequestProperty("User-Agent", LauncherConstants.USER_AGENT);
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");
        if(localMd5 != null)
            connection.setRequestProperty("If-None-Match", localMd5);
        

        connection.connect();

        return connection;
    }
    public void setExpectedSize(final long expectedSize) {
        this.expectedSize = expectedSize;
    }

    public boolean shouldIgnoreLocal() {
        return forceDownload;
    }
}