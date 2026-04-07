package fr.wdes.updater;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import fr.wdes.Http;
import fr.wdes.LauncherConstants;
import fr.wdes.OperatingSystem;
import fr.wdes.versions.CompleteVersion;


public class RemoteVersionList extends VersionList {
    private final Proxy proxy;
    public RemoteVersionList(Proxy proxy)
    {
      this.proxy = proxy;
    }

    public URL getIndex(String indexName) throws MalformedURLException
    {
        String url = LauncherConstants.URL_DOWNLOAD_INDEXES_BASE + "indexes/" + indexName + ".json";
        return new URL(url);
    }

    @Override
    protected String getContent(final String path) throws IOException {
        return Http.performGet(new URL(LauncherConstants.URL_DOWNLOAD_VERSIONS_BASE + path), proxy);
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public boolean hasAllFiles(final CompleteVersion version, final OperatingSystem os) {
        return true;
    }
}
