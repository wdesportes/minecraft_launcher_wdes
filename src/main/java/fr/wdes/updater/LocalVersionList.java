package fr.wdes.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import com.google.gson.JsonSyntaxException;

import fr.wdes.Launcher;
import fr.wdes.OperatingSystem;
import fr.wdes.logger;
import fr.wdes.versions.CompleteVersion;
import fr.wdes.versions.ReleaseType;
import fr.wdes.versions.Version;

public class LocalVersionList extends FileBasedVersionList {
	protected final File baseDirectory;
    protected final File baseVersionsDir;
    public LocalVersionList(File baseDirectory) {
        if(baseDirectory == null || !baseDirectory.isDirectory())
            throw new IllegalArgumentException("Base directory is not a folder!");
        this.baseDirectory = baseDirectory;
        baseVersionsDir = new File(this.baseDirectory, "versions");
        if(!baseVersionsDir.isDirectory())
            baseVersionsDir.mkdirs();
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    @Override
    protected InputStream getFileInputStream(final String path) throws FileNotFoundException {
        return new FileInputStream(new File(baseDirectory, path));
    }

    @Override
    public boolean hasAllFiles(final CompleteVersion version, final OperatingSystem os) {
        final Set<String> files = version.getRequiredFiles(os);

        for(final String file : files)
            if(!new File(baseDirectory, file).isFile())
                return false;

        return true;
    }

    @Override
    public void refreshVersions() throws IOException {
        clearCache();

        final File[] files = baseVersionsDir.listFiles();
        if(files == null)
            return;

        for(final File directory : files) {
            final String id = directory.getName();
            final File jsonFile = new File(directory, id + ".json");

            if(directory.isDirectory() && jsonFile.exists())
                try {
                    final String path = "versions/" + id + "/" + id + ".json";
                    final CompleteVersion version = gson.fromJson(getContent(path), CompleteVersion.class);

                    if(version.getId().equals(id))
                        addVersion(version);
                    else if(Launcher.getInstance() != null)
                    	logger.info("Ignoring: " + path + "; it contains id: '" + version.getId() + "' expected '" + id + "'");
                }
                catch(final RuntimeException ex) {
                    if(Launcher.getInstance() != null)
                    	logger.warn("Couldn't load local version " + jsonFile.getAbsolutePath(), ex);
                    else
                        throw new JsonSyntaxException("Loading file: " + jsonFile.toString(), ex);
                }
        }

        for(final Version version : getVersions()) {
            final ReleaseType type = version.getType();

            if(getLatestVersion(type) == null || getLatestVersion(type).getUpdatedTime().before(version.getUpdatedTime()))
                setLatestVersion(version);
        }
    }

    public void saveVersion(final CompleteVersion version) throws IOException {
        final String text = serializeVersion(version);
        final File target = new File(baseVersionsDir, version.getId() + "/" + version.getId() + ".json");
        if(target.getParentFile() != null)
            target.getParentFile().mkdirs();
        final PrintWriter writer = new PrintWriter(target);
        writer.print(text);
        writer.close();
    }

    public void saveVersionList() throws IOException {
        final String text = serializeVersionList();
        final PrintWriter writer = new PrintWriter(new File(baseVersionsDir, "versions.json"));
        writer.print(text);
        writer.close();
    }
}
