package fr.wdes;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fr.wdes.authentication.AuthenticationService;
import fr.wdes.authentication.custom.YggdrasilAuthenticationService;
import fr.wdes.authentication.exceptions.AuthenticationException;
import fr.wdes.authentication.exceptions.InvalidCredentialsException;
import fr.wdes.download.DownloadJob;
import fr.wdes.profile.Profile;
import fr.wdes.profile.ProfileManager;
import fr.wdes.ui.LauncherPanel;
import fr.wdes.updater.LocalVersionList;
import fr.wdes.updater.RemoteVersionList;
import fr.wdes.updater.VersionManager;


public class Launcher {
	public final Logger LOGGER = Logger.getLogger("Wdes");
	private static Launcher instance;
	private static final List<String> delayedSysout = new ArrayList<String>();
    public static Launcher getInstance() {
        return instance;
    }

    private static void setLookAndFeel() {
        final JFrame frame = new JFrame();
        frame.setUndecorated(true);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(final Throwable ignored) {
            try {
            	logger.warn("Your java failed to provide normal look and feel, trying the old fallback now");
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            catch(final Throwable t) {
            	logger.warn("Unexpected exception setting look and feel");
                t.printStackTrace();
            }
        }
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("test"));
        frame.add(panel);

        try {
            frame.pack();
        }
        catch(final Throwable t) {
        	logger.warn("Custom (broken) theme detected, falling back onto x-platform theme");
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            catch(final Throwable ex) {
            	logger.warn("Unexpected exception setting look and feel", ex);
            }
        }

        frame.dispose();
    }

    private final VersionManager versionManager;
    private final JFrame frame;
    private final LauncherPanel launcherPanel;
    private final GameLauncher gameLauncher;
    private final File workingDirectory;
    private final Proxy proxy;
    private final PasswordAuthentication proxyAuth;
    private final String[] additionalArgs;
    private final Integer bootstrapVersion;
    public JObjectContainer config;
    public String uuid;
    public String appdata;
    public int width;
    public int height;
    private final ProfileManager profileManager;
    private UUID clientToken = UUID.randomUUID();
    private File sqlitedb;
    public Connection db;
    public Statement statement;
    public Launcher(final JFrame frame,final String uuid,final String appdata, final Proxy proxy, final PasswordAuthentication proxyAuth, final String[] args) {
        this(frame, uuid, appdata, proxy, proxyAuth, args, Integer.valueOf(0));
        System.out.println("Lancement...");
    }
    public Launcher(final JFrame frame,final String uuid,final String appdata, final Proxy proxy, final PasswordAuthentication proxyAuth, final String[] args, final Integer bootstrapVersion) {
    	System.out.println("Lancement...");
    	this.bootstrapVersion = bootstrapVersion;
        instance = this;
        setLookAndFeel();
        ComponentMover cm = new ComponentMover();
        cm.registerComponent(frame);
        this.proxy = proxy;
        this.proxyAuth = proxyAuth;
        additionalArgs = args;
        this.uuid = uuid;
        this.appdata = appdata;
        this.frame = frame;
        LOGGER.setUseParentHandlers(false);
        Handler conHdlr = new ConsoleHandler();
        conHdlr.setFormatter(new SimpleFormatter() {
          public String format(LogRecord record) {
        	  String line  = "[ "+record.getLevel() + " ] "+ record.getMessage() + "\n";
        	    if (Launcher.getInstance().getLauncherPanel() == null) {
					Launcher.delayedSysout.add(line);
        	      } else {
        	          for(final String lines : delayedSysout)
        	              launcherPanel.getTabPanel().getConsole().print(lines);
        	          delayedSysout.clear();

        	    	  Launcher.getInstance().getLauncherPanel().getTabPanel().getConsole().print(line);
        	      }

            System.out.print(line);
            return "";
          }
        });

        LOGGER.addHandler(conHdlr);
        //this.downloaderExecutorService.allowCoreThreadTimeOut(true);
        Gson gson = new Gson() ;
        String configUrl = LauncherConstants.URL_CONFIGS+this.uuid+".conf";
        try {
			config = gson.fromJson(Http.performGet(new URL(configUrl), proxy),JObjectContainer.class) ;
			logger.info(" UUID Local : "+this.uuid);
			if(!config.width.isEmpty() && !config.height.isEmpty()){
			this.width  = Integer.parseInt(config.width);
			this.height = Integer.parseInt(config.height);
			}
			else{
			this.width  = Profile.DEFAULT_RESOLUTION.getWidth();
			this.height = Profile.DEFAULT_RESOLUTION.getHeight();
			}
        } catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (config == null) {
        	JOptionPane.showMessageDialog(
                frame,
                "Impossible de récupérer la config depuis " + configUrl,
                "[WdesLaunchers] Erreur",
                JOptionPane.ERROR_MESSAGE
            );
            logger.warn("Impossible de récupérer la config depuis " + configUrl);
            System.exit(1);
		}

        final File workdir = Utilis.getWorkingDirectory();
        if(workdir.exists() && !workdir.isDirectory())
        	JOptionPane.showMessageDialog(frame,
        			".Appdata invalide: "+workdir.toString()+" C'est peut être un fichier ??",
    			    "[WdesLaunchers] Erreur",
    			    JOptionPane.ERROR_MESSAGE);

        if(!workdir.exists() && !workdir.mkdirs())
        	JOptionPane.showMessageDialog(frame,
        			"Création du dossier impossible : "+workdir.toString()+" C'est peut être un emplacement protégé ?? Allez Googler \"chmod dossier \"",
    			    "[WdesLaunchers] Erreur",
    			    JOptionPane.ERROR_MESSAGE);

        this.workingDirectory = workdir;
        this.sqlitedb = new File(this.workingDirectory,"wdeslaunchers.db");
        /** *SQLITE DB*  */

        try {
          db = DriverManager.getConnection("jdbc:sqlite:"+this.sqlitedb.toPath(),this.uuid,this.appdata);
          DatabaseMetaData dm = (DatabaseMetaData) db.getMetaData();
          System.out.println("Driver name: " + dm.getDriverName());
          System.out.println("Driver version: " + dm.getDriverVersion());
          System.out.println("Product name: " + dm.getDatabaseProductName());
          System.out.println("Product version: " + dm.getDatabaseProductVersion());

        } catch ( Exception e ) {
          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
          System.exit(1);
        }
        System.out.println("Opened database successfully");

        try {

        	logger.info("WdesLaunchers DB : "+this.sqlitedb.toPath());
            statement = db.createStatement();

            String query = "CREATE TABLE IF NOT EXISTS Launchers(ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, DATE TEXT, LOGO BLOB);";
            statement.execute(query);
            String query2 = "CREATE TABLE IF NOT EXISTS Users(ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, ETAG TEXT, AVATAR BLOB);";
            statement.execute(query2);

            statement.executeBatch();
        } catch ( SQLException e) {
            e.printStackTrace();
        }

        /** *SQLITE DB*  */

        logger.info("Launcher WdesLauncher v (1.0) started on " + OperatingSystem.getCurrentPlatform().getName() + "...");
        logger.info("Current time is " + DateFormat.getDateTimeInstance(2, 2, Locale.FRANCE).format(new Date()));
        if(!OperatingSystem.getCurrentPlatform().isSupported())
        logger.info("Ce système d'exploitation est inconnu,il n'est peut être pas supporté.");
        logger.info("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
        logger.info("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
        logger.info("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
        logger.info("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
        logger.info("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
        logger.info("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");

        gameLauncher = new GameLauncher(this);
        profileManager = new ProfileManager(this);
        versionManager = new VersionManager(
            new LocalVersionList(this.workingDirectory),
            new RemoteVersionList(proxy)
        );
        launcherPanel = new LauncherPanel(this);
        initializeFrame();

 /*
        if(bootstrapVersion.intValue() < 4) {
            showOutdatedNotice();
            return;
        }

        downloadResources(); */

        refreshVersionsAndProfiles();
        Telechargment_fonds();
    }

    public void closeLauncher(String source) {
		try {
			db.close();
			if(db.isClosed()){
				logger.info("Base de données fermée !!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	logger.info("Fermeture demandée par : "+source);
        frame.dispatchEvent(new WindowEvent(frame, 201));
    }
    public void minimizeLauncher() {
    	frame.setState(JFrame.ICONIFIED);
    }
/*
    private void downloadResources() {
        final DownloadJob job = new DownloadJob("Resources", true, gameLauncher);
        gameLauncher.addJob(job);
        versionManager.getExecutorService().submit(new Runnable() {
            public void run() {
                try {
                    versionManager.downloadResources(job);
                    job.startDownloading(versionManager.getExecutorService());
                }
                catch(final IOException e) {
                    Launcher.getInstance().println("Unexpected exception queueing resource downloads", e);
                }
            }
        });
    }
*/
    private void Telechargment_fonds() {
        // Wrap gameLauncher so we get notified when the fonds download finishes
        // and can refresh the BackgroundImage. The image is loaded once when
        // LauncherPanel is constructed (before this method runs), so without
        // a refresh the launcher keeps showing the bundled default until the
        // next session.
        final fr.wdes.download.DownloadListener wrapped = new fr.wdes.download.DownloadListener() {
            public void onDownloadJobFinished(final DownloadJob finished) {
                gameLauncher.onDownloadJobFinished(finished);
                if(launcherPanel != null && launcherPanel.launcherhome != null) {
                    launcherPanel.launcherhome.refresh();
                }
            }
            public void onDownloadJobProgressChanged(final DownloadJob changed) {
                gameLauncher.onDownloadJobProgressChanged(changed);
            }
        };
        final DownloadJob job = new DownloadJob("Fonds du Launcher", true, wrapped);
        gameLauncher.addJob(job);
        versionManager.getExecutorService().submit(new Runnable() {
            public void run() {
                try {
                    versionManager.telecharger_fonds(job);
                    job.startDownloading(versionManager.getExecutorService());
                }
                catch(final IOException e) {
                	logger.warn("Exeption générée par le téléchargement des fonds", e);
                }
            }
        });
    }
    public void ensureLoggedIn() {
        final Profile selectedProfile = profileManager.getSelectedProfile();
        final AuthenticationService auth = profileManager.getAuthDatabase().getByUUID(selectedProfile.getPlayerUUID());

        if(!auth.isLoggedIn()) {
            if(auth.canLogIn())
                try {
                    auth.logIn();
                    try {
                        profileManager.saveProfiles();
                    }
                    catch(final IOException e) {
                    	logger.warn("Couldn't save profiles after refreshing auth!", e);
                    }
                    profileManager.fireRefreshEvent();
                }
                catch(final AuthenticationException e) {
                	e.printStackTrace();

                }

        }
        else if(!auth.canPlayOnline())
            try {
            	logger.info("Tentative de reconnexion...");
                Launcher.getInstance().getLauncherPanel().getProgressBar().setString("Tentative de reconnexion...");
                auth.logIn();
                try {
                    profileManager.saveProfiles();
                }
                catch(final IOException e) {
                	logger.warn("Couldn't save profiles after refreshing auth!", e);
                }
                profileManager.fireRefreshEvent();
            }
            catch(final InvalidCredentialsException e) {
            	logger.warn(e);
                showLoginPrompt();
            }
            catch(final AuthenticationException e) {
            	e.printStackTrace();
            }
    }

    public String[] getAdditionalArgs() {
        return additionalArgs;
    }

    public int getBootstrapVersion() {
        return bootstrapVersion.intValue();
    }

    public UUID getClientToken() {
        return clientToken;
    }

    public JFrame getFrame() {
        return frame;
    }

    public JObjectContainer getConfig() {
        return config;
    }
    public void setConfig(JObjectContainer conf) {
        config.nom = conf.nom;
    }
    public GameLauncher getGameLauncher() {
        return gameLauncher;
    }

    public LauncherPanel getLauncherPanel() {
        return launcherPanel;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public PasswordAuthentication getProxyAuth() {
        return proxyAuth;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    protected void initializeFrame() {
        frame.getContentPane().removeAll();
        frame.setTitle(LauncherConstants.SERVER_NAME + " Launcher v(1.0)");
        //Bottom bar : 900,580 IDF: 900,507
        //frame.setPreferredSize(new Dimension(900, 507));
        frame.setDefaultCloseOperation(2);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                frame.setVisible(false);
                frame.dispose();
                versionManager.getExecutorService().shutdown();
            }
        });
        try {
            final InputStream in = Launcher.class.getResourceAsStream("/fr/wdes/ressources/favicon.png");
            if(in != null)
                frame.setIconImage(ImageIO.read(in));
        }
        catch(final IOException localIOException) {
        }
        frame.add(launcherPanel);

        frame.pack();
        frame.setVisible(true);
    }





    public void refreshVersionsAndProfiles() {

        versionManager.getExecutorService().submit(new Runnable() {
            public void run() {
                try {
                    versionManager.refreshVersions();
                }
                catch(final Exception e) {
                	logger.warn("Unexpected exception refreshing version list", e);
                }
                try {
                    profileManager.loadProfiles();
                    logger.info("Chargé :  " + profileManager.getProfiles().size() + " profile(s); selectionnés '" + profileManager.getSelectedProfile().getName() + "'");
                }
                catch(final Exception e) {
                	logger.warn("Unexpected exception refreshing profile list", e);
                }

                ensureLoggedIn();
            }
        });
    }

    public void setClientToken(final UUID clientToken) {
        this.clientToken = clientToken;
    }

    @SuppressWarnings("deprecation")
	public void showLoginPrompt() {
        try {
            profileManager.saveProfiles();
        }
        catch(final IOException e) {
        	logger.warn("Couldn't save profiles before logging in!", e);
        }

        for(final Profile profile : profileManager.getProfiles().values()) {
            final Map<String, String> credentials = profile.getAuthentication();

            if(credentials != null) {
                final AuthenticationService auth = new YggdrasilAuthenticationService();
                auth.loadFromStorage(credentials);

                if(auth.isLoggedIn()) {
                    final String uuid = auth.getSelectedProfile() == null ? "demo-" + auth.getUsername() : auth.getSelectedProfile().getId();
                    if(profileManager.getAuthDatabase().getByUUID(uuid) == null)
                        profileManager.getAuthDatabase().register(uuid, auth);
                }

                profile.setAuthentication(null);
            }
        }



    }
/*
    private void showOutdatedNotice() {
        final String error = "Sorry, but your launcher is outdated! Please redownload it at " + LauncherConstants.URL_BOOTSTRAP_DOWNLOAD;

        frame.getContentPane().removeAll();

        final int result = JOptionPane.showOptionDialog(frame, error, "Outdated launcher", 0, 0, null, new String[] { "Go to URL", "Close" }, "Go to URL");

        if(result == 0)
            try {
                OperatingSystem.openLink(new URI(LauncherConstants.URL_BOOTSTRAP_DOWNLOAD));
            }
            catch(final URISyntaxException e) {
                println("Couldn't open bootstrap download link. Please visit " + LauncherConstants.URL_BOOTSTRAP_DOWNLOAD + " manually.", e);
            }
        closeLauncher();
    }
*/


}
