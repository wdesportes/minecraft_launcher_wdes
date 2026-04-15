package fr.wdes.download.fonds;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import fr.wdes.Launcher;
import fr.wdes.logger;
import fr.wdes.download.Downloadable;
import fr.wdes.download.HttpStatusException;
import fr.wdes.download.MonitoringInputStream;


public class BackgroundDownloadable
  extends Downloadable
{

  private final String name;
  private final Fonds.Fond asset;
  private final String urlBase;
  private final File destination;
  private Status status = Status.DOWNLOADING;

  public BackgroundDownloadable(Proxy proxy, String name, Fonds.Fond asset, String urlBase, File destination)
    throws MalformedURLException
  {
    super(proxy, new URL(urlBase), destination, false);
    this.name = name;
    this.asset = asset;
    this.urlBase = urlBase;
    this.destination = destination;
  }

  protected static String createPathFromHash(String hash)
  {
    return  hash;
  }

  public String download()
    throws IOException
  {
    this.status = Status.DOWNLOADING;

    this.numAttempts += 1;
    File localAsset = getTarget();
    File localCompressed = this.asset.hasCompressedAlternative() ? new File(this.destination, createPathFromHash(this.asset.getCompressedHash())) : null;
    URL remoteAsset = getUrl();
    URL remoteCompressed = this.asset.hasCompressedAlternative() ? new URL(this.urlBase + createPathFromHash(this.asset.getCompressedHash())) : null;

    ensureFileWritable(localAsset);
    if (localCompressed != null) {
      ensureFileWritable(localCompressed);
    }
    if (localAsset.isFile())
    {
      if (FileUtils.sizeOf(localAsset) == this.asset.getSize()) {
          return "Fichier local,tailles identiques !!";
      }
      logger.info("Fichier local,mauvaise taille, {} devait être {}"+ new Object[] { Long.valueOf(FileUtils.sizeOf(localAsset)), Long.valueOf(this.asset.getSize()) });
      FileUtils.deleteQuietly(localAsset);
      this.status = Status.DOWNLOADING;
    }
    if ((localCompressed != null) && (localCompressed.isFile()))
    {
      String localCompressedHash = getDigest(localCompressed, "SHA", 40);
      if (localCompressedHash.equalsIgnoreCase(this.asset.getCompressedHash())) {
        return decompressAsset(localAsset, localCompressed);
      }
      logger.info("Fichier local,mauvaise signature, {} devait être {}"+ new Object[] { this.asset.getCompressedHash(), localCompressedHash });
      FileUtils.deleteQuietly(localCompressed);
    }
    if ((remoteCompressed != null) && (localCompressed != null))
    {

      HttpURLConnection connection = makeConnection(remoteCompressed.toString());
      int status = connection.getResponseCode();
      if (status / 100 == 2)
      {
        updateExpectedSize(connection);

        InputStream inputStream = new MonitoringInputStream(connection.getInputStream(), getMonitor());
        FileOutputStream outputStream = new FileOutputStream(localCompressed);
        String hash = copyAndDigest(inputStream, outputStream, "SHA", 40);
        if (hash.equalsIgnoreCase(this.asset.getCompressedHash())) {
          return decompressAsset(localAsset, localCompressed);
        }
        FileUtils.deleteQuietly(localCompressed);
        throw new RuntimeException(String.format("Hash did not match downloaded compressed fond (Expected %s, downloaded %s)", new Object[] { this.asset.getCompressedHash(), hash }));
      }
      throw new HttpStatusException(status);
    }
    Launcher.getInstance().getLauncherPanel().getProgressBar().setString("Téléchargement de : "+this.destination.getName());
    HttpURLConnection connection = makeConnection(remoteAsset.toString());
    int status = connection.getResponseCode();
    if (status / 100 == 2)
    {
      updateExpectedSize(connection);

      InputStream inputStream = new MonitoringInputStream(connection.getInputStream(), getMonitor());
      FileOutputStream outputStream = new FileOutputStream(localAsset);
      String hash = copyAndDigest(inputStream, outputStream, "SHA", 40);
      if (hash.equalsIgnoreCase(this.asset.getHash())) {
          return "[200] Fichier téléchargé,vérifié par signature";
      }
      FileUtils.deleteQuietly(localAsset);
      throw new RuntimeException(String.format("Hash did not match downloaded fond (Expected %s, downloaded %s)", new Object[] { this.asset.getHash(), hash }));
    }
    throw new HttpStatusException(status);
  }

  public String getStatus()
  {
    return this.status.name + " " + this.name;
  }

  protected String decompressAsset(File localAsset, File localCompressed)
    throws IOException
  {
    this.status = Status.EXTRACTING;
    OutputStream outputStream = FileUtils.openOutputStream(localAsset);
    InputStream inputStream = new GZIPInputStream(FileUtils.openInputStream(localCompressed));
    String hash;
    try
    {
      hash = copyAndDigest(inputStream, outputStream, "SHA", 40);
    }
    finally
    {
      IOUtils.closeQuietly(outputStream);
      IOUtils.closeQuietly(inputStream);
    }
    this.status = Status.DOWNLOADING;
    if (hash.equalsIgnoreCase(this.asset.getHash())) {
        return "Fichier local compressé,décompression,vérifié par Etag";
    }
    FileUtils.deleteQuietly(localAsset);
    throw new RuntimeException("Fichier local compressé,décompression,signatures différentes  " + this.asset.getHash() + " a la place de : " + hash + "");
  }

  private static enum Status
  {
	    DOWNLOADING("Téléchargement"),  EXTRACTING("Extraction");

    private final String name;

    private Status(String name)
    {
      this.name = name;
    }
  }
}
