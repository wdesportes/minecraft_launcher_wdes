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
    private final String baseUrl;
    public RemoteVersionList(String baseUrl, Proxy proxy)
    {
      this.baseUrl = baseUrl;
      this.proxy = proxy;
    }
    public URL getUrl(String file)
    	    throws MalformedURLException
    	  {
    	    return new URL(this.baseUrl + file);
    	  }
    	  
    @Override
    protected String getContent(final String path) throws IOException {
        return Http.performGet(new URL(LauncherConstants.URL_VERSION_LIST + path), proxy);
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public boolean hasAllFiles(final CompleteVersion version, final OperatingSystem os) {
        return true;
    }
}