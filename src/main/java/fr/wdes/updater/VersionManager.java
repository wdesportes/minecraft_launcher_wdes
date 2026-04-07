package fr.wdes.updater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import fr.wdes.Http;
import fr.wdes.Launcher;
import fr.wdes.LauncherConstants;
import fr.wdes.OperatingSystem;
import fr.wdes.logger;
import fr.wdes.download.DownloadJob;
import fr.wdes.download.Downloadable;
import fr.wdes.download.assets.AssetDownloadable;
import fr.wdes.download.assets.AssetIndex;
import fr.wdes.download.fonds.BackgroundDownloadable;
import fr.wdes.download.fonds.Fonds;
import fr.wdes.events.RefreshedVersionsListener;
import fr.wdes.versions.CompleteVersion;
import fr.wdes.versions.ReleaseType;
import fr.wdes.versions.Version;



public class VersionManager {
	protected final VersionList localVersionList;
	protected final RemoteVersionList remoteVersionList;
	protected final ThreadPoolExecutor executorService = new ExceptionalThreadPoolExecutor(8);
	protected final List<RefreshedVersionsListener> refreshedVersionsListeners = Collections.synchronizedList(new ArrayList<RefreshedVersionsListener>());
	protected final Object refreshLock = new Object();
	protected boolean isRefreshing;
	protected final Gson gson = new Gson();
    public VersionManager(final VersionList localVersionList, final RemoteVersionList remoteVersionList) {
        this.localVersionList = localVersionList;
        this.remoteVersionList = remoteVersionList;
    }

    public void addRefreshedVersionsListener(final RefreshedVersionsListener listener) {
        refreshedVersionsListeners.add(listener);
    }

    public DownloadJob downloadResources(final DownloadJob job, CompleteVersion version) throws IOException {
        final File baseDirectory = ((LocalVersionList) localVersionList).getBaseDirectory();
        logger.info("Démarrage de la tache des ressources ");
        job.addDownloadables(getResourceFiles(this.remoteVersionList.getProxy(), baseDirectory,version));

        return job;
    }

    public DownloadJob downloadVersion(final VersionSyncInfo syncInfo, final DownloadJob job) throws IOException {
        if(!(localVersionList instanceof LocalVersionList))
            throw new IllegalArgumentException("Cannot download if local repo isn't a LocalVersionList");

        final CompleteVersion version = getLatestCompleteVersion(syncInfo);
        final File baseDirectory = ((LocalVersionList) localVersionList).getBaseDirectory();
        final Proxy proxy = remoteVersionList.getProxy();

        job.addDownloadables(version.getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), proxy, baseDirectory, false));

        final String jarFile = "versions/" + version.getId() + "/" + version.getId() + ".jar";
        job.addDownloadables(new Downloadable[] { new Downloadable(proxy, new URL(LauncherConstants.URL_DOWNLOAD_VERSIONS_BASE + jarFile), new File(baseDirectory, jarFile), false) });

        return job;
    }

    public ThreadPoolExecutor getExecutorService() {
        return executorService;
    }

    public List<VersionSyncInfo> getInstalledVersions() {
        final List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();

        for(final Version version : localVersionList.getVersions())
            if(version.getType() != null && version.getUpdatedTime() != null) {
                final VersionSyncInfo syncInfo = getVersionSyncInfo(version, remoteVersionList.getVersion(version.getId()));
                result.add(syncInfo);
            }
        return result;
    }

    public CompleteVersion getLatestCompleteVersion(final VersionSyncInfo syncInfo) throws IOException {
        if(syncInfo.getLatestSource() == VersionSyncInfo.VersionSource.REMOTE) {
            CompleteVersion result = null;
            IOException exception = null;
            try {
                result = remoteVersionList.getCompleteVersion(syncInfo.getLatestVersion());
            }
            catch(final IOException e) {
                exception = e;
                try {
                    result = localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
                }
                catch(final IOException localIOException1) {
                }
            }
            if(result != null)
                return result;
            throw exception;
        }

        return localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
    }

    public VersionList getLocalVersionList() {
        return localVersionList;
    }

    public RemoteVersionList getRemoteVersionList() {
        return remoteVersionList;
    }

    private Set<Downloadable> getResourceFiles(Proxy proxy, File baseDirectory, CompleteVersion version)
    {
      Set<Downloadable> result = new HashSet<Downloadable>();
      InputStream inputStream = null;
      File assets = new File(baseDirectory, "assets");
      File objectsFolder = new File(assets, "objects");
      File indexesFolder = new File(assets, "indexes");
      String indexName = version.getAssets();
      long start = System.nanoTime();
      if (indexName == null) {
        indexName = "legacy";
      }
      File indexFile = new File(indexesFolder, indexName + ".json");
      try
      {
        URL indexUrl = this.remoteVersionList.getIndex(indexName);
        inputStream = indexUrl.openConnection(proxy).getInputStream();
        String json = IOUtils.toString(inputStream);
        FileUtils.writeStringToFile(indexFile, json);
        AssetIndex index = (AssetIndex)this.gson.fromJson(json, AssetIndex.class);
        for (Map.Entry<AssetIndex.AssetObject, String> entry : index.getUniqueObjects().entrySet())
        {
          AssetIndex.AssetObject object = (AssetIndex.AssetObject)entry.getKey();
          String filename = object.getHash().substring(0, 2) + "/" + object.getHash();
          File file = new File(objectsFolder, filename);
          if ((!file.isFile()) || (FileUtils.sizeOf(file) != object.getSize()))
          {
            Downloadable downloadable = new AssetDownloadable(proxy, (String)entry.getValue(), object, "http://resources.download.minecraft.net/", objectsFolder);
            downloadable.setExpectedSize(object.getSize());
            result.add(downloadable);
          }
        }
        long end = System.nanoTime();
        long delta = end - start;
        logger.info("Delta time to compare resources: " + delta / 1000000L + " ms ");
      }
      catch (Exception ex)
      {
    	  logger.warn("Couldn't download resources", ex);
      }
      finally
      {
        IOUtils.closeQuietly(inputStream);
      }

      return result;
    }
    private Set<Downloadable> Telecharger_fonds(Proxy proxy)
    {
    	logger.info("Téléchargement des fonds en cours");
      Set<Downloadable> result = new HashSet<Downloadable>();
      InputStream inputStream = null;
      //File dossier_fonds = new File(Launcher.getInstance().getWorkingDirectory(), "fonds");
      long start = System.nanoTime();

      //File indexFile = new File(dossier_fonds,  "fonds.json");
           try
      {
        URL indexUrl = new URL(LauncherConstants.URL_FONDS_DOWNLOAD+"index.json");
        Http.performGet(indexUrl, proxy);

        String json = Http.performGet(indexUrl, proxy);
        //FileUtils.writeStringToFile(indexFile, json);

        Fonds index = (Fonds)this.gson.fromJson(json, Fonds.class);
        logger.info("Traitement des fonds (" + index.count()  + ").");

        for (Map.Entry<Fonds.Fond, String> entry : index.getUniqueObjects().entrySet())
        {

        	Fonds.Fond object = (Fonds.Fond)entry.getKey();
          String filename = object.getName();
          //File file = new File(dossier_fonds, filename);
          File emplacement = new File(Launcher.getInstance().getWorkingDirectory(),filename);
          //logger.info("Téléchargement de : "+filename +"=======>"+emplacement.getAbsolutePath());
          if ((!emplacement.isFile()) || (FileUtils.sizeOf(emplacement) != object.getSize()))
          {
            String fondUrl = LauncherConstants.URL_FONDS_DOWNLOAD+filename;
            logger.info("Le fond " + filename + " (" + fondUrl + ") sera téléchargé.");
            Downloadable downloadable = new BackgroundDownloadable(proxy, (String)entry.getValue(), object, fondUrl, emplacement);
            downloadable.setExpectedSize(object.getSize());
            result.add(downloadable);
          } else {
              logger.info("Le fond " + filename + " est OK.");
          }
        }
        long end = System.nanoTime();
        long delta = end - start;
        logger.info("Temps de comparaison des fonds : " + delta / 1000000L + " ms ");
      }
      catch (Exception ex)
      {
    	  logger.warn("Impossible de télécharger les fonds", ex);
      }
      finally
      {
        IOUtils.closeQuietly(inputStream);
      }

      return result;
    }
    public List<VersionSyncInfo> getVersions() {
        return getVersions(null);
    }

    public List<VersionSyncInfo> getVersions(final VersionFilter filter) {
        synchronized(refreshLock) {
            if(isRefreshing)
                return new ArrayList<VersionSyncInfo>();
        }

        final List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();
        final Map<String, VersionSyncInfo> lookup = new HashMap<String, VersionSyncInfo>();
        final Map<ReleaseType, Integer> counts = new EnumMap<ReleaseType, Integer>(ReleaseType.class);

        for(final ReleaseType type : ReleaseType.values())
            counts.put(type, Integer.valueOf(0));

        for(final Version version : localVersionList.getVersions())
            if(version.getType() != null && version.getUpdatedTime() != null && (filter == null || filter.getTypes().contains(version.getType()) && counts.get(version.getType()).intValue() < filter.getMaxCount())) {
                final VersionSyncInfo syncInfo = getVersionSyncInfo(version, remoteVersionList.getVersion(version.getId()));
                lookup.put(version.getId(), syncInfo);
                result.add(syncInfo);
            }
        for(final Version version : remoteVersionList.getVersions())
            if(version.getType() != null && version.getUpdatedTime() != null && !lookup.containsKey(version.getId()) && (filter == null || filter.getTypes().contains(version.getType()) && counts.get(version.getType()).intValue() < filter.getMaxCount())) {
                final VersionSyncInfo syncInfo = getVersionSyncInfo(localVersionList.getVersion(version.getId()), version);
                lookup.put(version.getId(), syncInfo);
                result.add(syncInfo);

                if(filter != null)
                    counts.put(version.getType(), Integer.valueOf(counts.get(version.getType()).intValue() + 1));
            }
        if(result.isEmpty())
            for(final Version version : localVersionList.getVersions())
                if(version.getType() != null && version.getUpdatedTime() != null) {
                    final VersionSyncInfo syncInfo = getVersionSyncInfo(version, remoteVersionList.getVersion(version.getId()));
                    lookup.put(version.getId(), syncInfo);
                    result.add(syncInfo);
                }

        Collections.sort(result, new Comparator<VersionSyncInfo>() {
            public int compare(final VersionSyncInfo a, final VersionSyncInfo b) {
                final Version aVer = a.getLatestVersion();
                final Version bVer = b.getLatestVersion();

                if(aVer.getReleaseTime() != null && bVer.getReleaseTime() != null)
                    return bVer.getReleaseTime().compareTo(aVer.getReleaseTime());
                return bVer.getUpdatedTime().compareTo(aVer.getUpdatedTime());
            }
        });
        return result;
    }

    public VersionSyncInfo getVersionSyncInfo(final String name) {
        return getVersionSyncInfo(localVersionList.getVersion(name), remoteVersionList.getVersion(name));
    }

    public VersionSyncInfo getVersionSyncInfo(final Version version) {
        return getVersionSyncInfo(version.getId());
    }

    public VersionSyncInfo getVersionSyncInfo(final Version localVersion, final Version remoteVersion) {
        final boolean installed = localVersion != null;
        boolean upToDate = installed;

        if(installed && remoteVersion != null)
            upToDate = !remoteVersion.getUpdatedTime().after(localVersion.getUpdatedTime());
        if(localVersion instanceof CompleteVersion)
            upToDate &= localVersionList.hasAllFiles((CompleteVersion) localVersion, OperatingSystem.getCurrentPlatform());

        return new VersionSyncInfo(localVersion, remoteVersion, installed, upToDate);
    }
    public void refreshVersions() throws IOException {
        synchronized(refreshLock) {
            isRefreshing = true;
        }
        try {
        	logger.info("Refreshing local version list...");
            localVersionList.refreshVersions();
            logger.info("Refreshing remote version list...");
            remoteVersionList.refreshVersions();
        }
        catch(final IOException ex) {
            synchronized(refreshLock) {
                isRefreshing = false;
            }
            throw ex;
        }

        logger.info("Refresh complete.");

        synchronized(refreshLock) {
            isRefreshing = false;
        }

        final List<RefreshedVersionsListener> listeners = new ArrayList<RefreshedVersionsListener>(refreshedVersionsListeners);
        for(final Iterator<RefreshedVersionsListener> iterator = listeners.iterator(); iterator.hasNext();) {
            final RefreshedVersionsListener listener = iterator.next();

            if(!listener.shouldReceiveEventsInUIThread()) {
                listener.onVersionsRefreshed(this);
                iterator.remove();
            }
        }

        if(!listeners.isEmpty())
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for(final RefreshedVersionsListener listener : listeners)
                        listener.onVersionsRefreshed(VersionManager.this);
                }
            });
    }

    public void removeRefreshedVersionsListener(final RefreshedVersionsListener listener) {
        refreshedVersionsListeners.remove(listener);
    }

    public DownloadJob telecharger_fonds(final DownloadJob job) throws IOException {
    	logger.info("Démarrage de la tache des ressources ");
        job.addDownloadables(Telecharger_fonds(Launcher.getInstance().getProxy()));

        return job;
    }
}
