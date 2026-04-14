package fr.wdes.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import fr.wdes.Http;
import fr.wdes.Launcher;
import fr.wdes.LauncherConstants;
import fr.wdes.logger;
import fr.wdes.authentication.AuthenticationDatabase;
import fr.wdes.authentication.AuthenticationService;
import fr.wdes.authentication.GameProfile;
import fr.wdes.authentication.custom.YggdrasilAuthenticationService;
import fr.wdes.authentication.exceptions.AuthenticationException;
import fr.wdes.authentication.exceptions.InvalidCredentialsException;
import fr.wdes.authentication.exceptions.UserMigratedException;
import fr.wdes.events.RefreshedProfilesListener;
import fr.wdes.events.RefreshedVersionsListener;
import fr.wdes.profile.Profile;
import fr.wdes.profile.ProfileManager;
import fr.wdes.ui.lite.BackgroundImage;
import fr.wdes.ui.lite.Compatibility;
import fr.wdes.ui.lite.DynamicButton;
import fr.wdes.ui.lite.FutureImage;
import fr.wdes.ui.lite.HyperlinkJLabel;
import fr.wdes.ui.lite.ImageCallback;
import fr.wdes.ui.lite.ImageUtils;
import fr.wdes.ui.lite.LiteButton;
import fr.wdes.ui.lite.LiteProgressBar;
import fr.wdes.ui.lite.OperatingSystem;
import fr.wdes.ui.lite.ResourceUtils;
import fr.wdes.ui.lite.TransparentButton;
import fr.wdes.ui.popups.profile.ProfileEditorPopup;
import fr.wdes.ui.tabs.LauncherTabPanel;
import fr.wdes.updater.VersionManager;
import fr.wdes.updater.VersionSyncInfo;
@SuppressWarnings("serial")
public class LauncherPanel extends JPanel implements ActionListener, RefreshedProfilesListener, RefreshedVersionsListener {

    private final CardLayout cardLayout;
    private final LauncherTabPanel tabPanel;
    //private final BottomBarPanel bottomBar;
    public LiteProgressBar progressBar = new LiteProgressBar();
    public BackgroundImage launcherhome;
    private final Launcher launcher;
    private boolean console;
	private JLayeredPane topWrapper;
	public PlaceholderTextField logininput = new PlaceholderTextField();
	public PlaceholderPasswordField passwordinput = new PlaceholderPasswordField();
	public  LiteButton loginbtn = new LiteButton("Connexion");
	public  LiteButton gamebtn = new LiteButton("Jouer");
	private JLabel welcomeText = new JLabel();
	private JLabel versionText = new JLabel();
	private JLabel errorLabel = new JLabel();
	Font largerMinecraft;
	JLabel bottomRectangle = new JLabel();
    JLabel logo = new JLabel();
	JCheckBox remember = new fr.wdes.ui.lite.LiteCheckBox("Retenir");
    private static final URL minimizeIcon = Launcher.class.getResource("/fr/wdes/ressources/minimize.png");
    private static final URL optionsIcon = Launcher.class.getResource("/fr/wdes/ressources/options.png");
    private static final URL closeIcon = Launcher.class.getResource("/fr/wdes/ressources/close.png");
	private static final URL youtubeIcon = Launcher.class.getResource("/fr/wdes/ressources/youtube.png");
	private static final URL youtubeHoverIcon = Launcher.class.getResource("/fr/wdes/ressources/youtube_hover.png");
	private static final URL twitterIcon = Launcher.class.getResource("/fr/wdes/ressources/twitter.png");
	private static final URL twitterHoverIcon = Launcher.class.getResource("/fr/wdes/ressources/twitter_hover.png");
	private static final URL facebookIcon = Launcher.class.getResource("/fr/wdes/ressources/facebook.png");
	private static final URL facebookHoverIcon = Launcher.class.getResource("/fr/wdes/ressources/facebook_hover.png");
	private static final URL steamIcon = Launcher.class.getResource("/fr/wdes/ressources/steam.png");
	private static final URL steamHoverIcon = Launcher.class.getResource("/fr/wdes/ressources/steam_hover.png");
	private static final URL gplusIcon = Launcher.class.getResource("/fr/wdes/ressources/gplus.png");
	private static final URL gplusHoverIcon = Launcher.class.getResource("/fr/wdes/ressources/gplus_hover.png");
	private static final String CLOSE_ACTION = "close";
	private static final String GAME_ACTION = "play";
	private static final String MINIMIZE_ACTION = "minimize";
	private static final String OPTIONS_ACTION = "options";
    private static final String YOUTUBE_ACTION = "youtube";
	private static final String STEAM_ACTION = "steam";
	private static final String FACEBOOK_ACTION = "facebook";
	private static final String TWITTER_ACTION = "twitter";
	private static final String GPLUS_ACTION = "gplus";
	private static final String LOGIN_ACTION = "login";
	private static final String IMAGE_LOGIN_ACTION = "image_login";
	private static final String REMOVE_USER = "remove";
	private static URI youtubeURL, twitterURL, facebookURL, gplusURL, steamURL;
	private TransparentButton close, minimize, options;
	protected static final int FRAME_WIDTH = 880, FRAME_HEIGHT = 520;
    private final AuthenticationService authentication = new YggdrasilAuthenticationService();
	private final Map<JButton, DynamicButton> removeButtons = new HashMap<JButton, DynamicButton>();
	protected boolean loggedin = false;
	List<JLink> JLinks = new ArrayList<JLink>();
	List<JBouton> JBoutons = new ArrayList<JBouton>();
    private BackgroundImage CreateHome()
    {
	       InputStream is = Launcher.class.getResourceAsStream("/fr/wdes/ressources/minecraft.ttf");
			Font minecraft = null;
			try {
				minecraft = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, 12f);
			} catch (FontFormatException e) {
				e.printStackTrace();
			}
			 catch ( IOException e) {
					e.printStackTrace();
				}
			if (OperatingSystem.getOS().isUnix()) {
				largerMinecraft = minecraft.deriveFont((float)16);
			} else {
				largerMinecraft = minecraft.deriveFont((float)18);
			}

		//int xShift = 0;
		int yShift = 0;
		if (launcher.getFrame().isUndecorated()) {
			yShift += 30;
		}
    	   this.options = new TransparentButton();
	       this.options.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(optionsIcon)));
			if (OperatingSystem.getOS().isMac()) {
				this.options.setBounds(74, 0, 37, 20);
			} else {
				this.options.setBounds(FRAME_WIDTH - 111, 0, 37, 20);
			}
	       this.options.setTransparency(0.7F);
	       this.options.setHoverTransparency(1.0F);
	       this.options.setActionCommand(OPTIONS_ACTION);
	       this.options.setToolTipText("Console");
	       this.options.addActionListener(this);
	       this.options.setBorder(BorderFactory.createEmptyBorder());
	       this.options.setContentAreaFilled(false);

  	       this.minimize = new TransparentButton();
  	       this.minimize.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(minimizeIcon)));
  			if (OperatingSystem.getOS().isMac()) {
  				this.minimize.setBounds(37, 0, 37, 20);
  			} else {
  				this.minimize.setBounds(FRAME_WIDTH - 74, 0, 37, 20);
  			}
  	       this.minimize.setTransparency(0.7F);
  	       this.minimize.setHoverTransparency(1.0F);
  	       this.minimize.setActionCommand(MINIMIZE_ACTION);
  	       this.minimize.addActionListener(this);
  	       this.minimize.setBorder(BorderFactory.createEmptyBorder());
  	       this.minimize.setContentAreaFilled(false);

  	       this.close = new TransparentButton();
  	       this.close.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(closeIcon)));
  			if (OperatingSystem.getOS().isMac()) {
  				this.close.setBounds(0, 0, 37, 20);
  			} else {
  				this.close.setBounds(FRAME_WIDTH - 37, 0, 37, 20);
  			}
  	       this.close.setTransparency(0.7F);
  	       this.close.setHoverTransparency(1.0F);
  	       this.close.setActionCommand(CLOSE_ACTION);
  	  	   this.close.addActionListener(this);
  	       this.close.setBorder(BorderFactory.createEmptyBorder());
  	       this.close.setContentAreaFilled(false);

  	       this.logininput.setPlaceholder(LauncherConstants.PLACEHOLDER_LOGIN);
  	       this.logininput.setBounds(FRAME_WIDTH / 2 - 90, 309 + yShift, 180, 24);
  	       this.logininput.setBackground(new Color(0, 0, 0, 150));
  	       this.logininput.setForeground(Color.LIGHT_GRAY);
	       this.logininput.setFont(minecraft);

  	       this.passwordinput.setPlaceholder(LauncherConstants.PLACEHOLDER_PASSD);
  	       this.passwordinput.setBounds(FRAME_WIDTH / 2 - 90, 338 + yShift, 180, 24);
  	       this.passwordinput.setBackground(new Color(0, 0, 0, 150));
  	       this.passwordinput.setForeground(Color.LIGHT_GRAY);
	       this.passwordinput.setFont(minecraft);

  	       this.gamebtn.setBounds(FRAME_WIDTH / 2 + 5, 367 + yShift, 90, 24);
  	       this.gamebtn.addActionListener(this);
  	       this.gamebtn.setActionCommand(GAME_ACTION);
	       this.gamebtn.setFont(minecraft);

	       this.loginbtn.setBounds(FRAME_WIDTH / 2 + 5, 367 + yShift, 90, 24);
	       this.loginbtn.addActionListener(this);
	       this.loginbtn.setActionCommand(LOGIN_ACTION);
	       this.loginbtn.setFont(minecraft);

	       this.welcomeText.setBounds(10, 450, 200, 24);
	       this.welcomeText.setOpaque(false);
	       this.welcomeText.setVisible(false);
 	       this.welcomeText.setForeground(Color.WHITE);
 	       this.welcomeText.setFont(minecraft);

	       this.versionText.setBounds(605, 450, 300, 24);
	       this.versionText.setOpaque(false);
	       this.versionText.setVisible(false);
 	       this.versionText.setForeground(Color.WHITE);
 	       this.versionText.setFont(minecraft);

	       this.errorLabel.setBounds(500, 450, 300, 24);
	       this.errorLabel.setOpaque(false);

 	       this.progressBar.setBounds(FRAME_WIDTH / 2 - 192, passwordinput.getY() + 60, 384, 23);
 	       this.progressBar.setVisible(false);
 	       this.progressBar.setStringPainted(true);
 	       this.progressBar.setOpaque(true);
 	       this.progressBar.setTransparency(0.70F);
 	       this.progressBar.setHoverTransparency(0.70F);
 	       this.progressBar.setFont(minecraft);

 	       this.remember.setBounds(FRAME_WIDTH / 2 - 95, 367 + yShift, 110, 24);
 	       this.remember.setOpaque(false);
 	       this.remember.setBorderPainted(false);
 	       this.remember.setContentAreaFilled(false);
 	       this.remember.setBorder(null);
 	       this.remember.setForeground(Color.WHITE);
 	       this.remember.setFont(minecraft);
 	       this.remember.setSelected(true);

 	        JLink home = new JLink("Wdes", "https://wdes.fr", "Wdes.fr", 10, FRAME_HEIGHT - 27, 65, 20);
 	        JLinks.add(home);
 	        JLink forums = new JLink("Forum", "https://launchers.wdes.fr", "Wdes.fr", 82, FRAME_HEIGHT - 27, 90, 20);
 	        JLinks.add(forums);
 	        JLink donate = new JLink("Donner", "https://launchers.wdes.fr", "Wdes.fr", 185, FRAME_HEIGHT - 27, 85, 20);
 	        JLinks.add(donate);

 	       for(JLink link: JLinks) {
 	          HyperlinkJLabel alink = new HyperlinkJLabel(link.getName(), link.getLink());
 	          alink.setToolTipText(link.getToolTip());
 	          alink.setBounds(link.x(),link.y(), link.w(), link.h());
 	          alink.setForeground(Color.WHITE);
 	          alink.setOpaque(false);
 	          alink.setTransparency(0.40F);
 	          alink.setHoverTransparency(1F);
 	          alink.setFont(largerMinecraft);
 	          launcherhome.add(alink);
 	        }
	        JBouton steam = new JBouton( STEAM_ACTION, "Game with us on Steam",FRAME_WIDTH - 35, FRAME_HEIGHT - 32, 30, 30,steamIcon,steamHoverIcon);
	        JBoutons.add(steam);
	        JBouton facebook = new JBouton( FACEBOOK_ACTION, "Like us on Facebook",FRAME_WIDTH - 70, FRAME_HEIGHT - 32, 30, 30,facebookIcon,facebookHoverIcon);
	        JBoutons.add(facebook);
	        JBouton twitter = new JBouton( TWITTER_ACTION, "Follow us on Twitter",FRAME_WIDTH - 105, FRAME_HEIGHT - 32, 30, 30,twitterIcon,twitterHoverIcon);
	        JBoutons.add(twitter);
	        JBouton gplus = new JBouton( GPLUS_ACTION, "Follow us on Google+",FRAME_WIDTH - 140, FRAME_HEIGHT - 32, 30, 30,gplusIcon,gplusHoverIcon);
	        JBoutons.add(gplus);
	        JBouton youtube = new JBouton( YOUTUBE_ACTION, "Subscribe to our videos",FRAME_WIDTH - 175, FRAME_HEIGHT - 32, 30, 30,youtubeIcon,youtubeHoverIcon);
	        JBoutons.add(youtube);
	        for(JBouton btn: JBoutons) {
	        	TransparentButton abtn = new TransparentButton();
	        	abtn.setToolTipText("Game with us on Steam");
	        	abtn.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(btn.getIcon()).getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
	        	abtn.setBounds(btn.x(),btn.y(), btn.w(), btn.h());
	        	abtn.setTransparency(0.70F);
	        	abtn.setHoverTransparency(1F);
	        	abtn.setActionCommand(btn.getAction());
	        	abtn.addActionListener(this);
	        	abtn.setBorder(BorderFactory.createEmptyBorder());
	        	abtn.setContentAreaFilled(false);
	        	abtn.setRolloverIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(btn.getHoverIcon()).getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
	        	launcherhome.add(abtn);
	        }



 			bottomRectangle.setBounds(0, FRAME_HEIGHT - 34, FRAME_WIDTH, 34);
 			bottomRectangle.setBackground(new Color(30, 30, 30, 180));
 			bottomRectangle.setOpaque(true);
  	        logo.setBounds(FRAME_WIDTH / 2 - 200, 35, 400, 109);




  	        try {
				setIcon(logo,getLogo(Launcher.getInstance().uuid), logo.getWidth(), logo.getHeight());
			} catch (Exception e) {
				e.printStackTrace();
			}


  	        launcherhome.add(logo);
  	        launcherhome.add(logininput);
  	        launcherhome.add(passwordinput);
  	        launcherhome.add(loginbtn);
  	        launcherhome.add(gamebtn);
  	        launcherhome.add(welcomeText);
  	        launcherhome.add(versionText);
  	        launcherhome.add(progressBar);
  	        launcherhome.add(bottomRectangle);
			launcherhome.add(remember);
  	        return launcherhome;

    }
    private void log_out(){
    	  loggedin = false;
          launcher.getProfileManager().getSelectedProfile().setPlayerUUID(null);
          launcher.getProfileManager().trimAuthDatabase();
          checkState();
          checkPlayerState();
          loginbtn.setVisible(true);
          try {
			launcher.getProfileManager().saveProfiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	private void setIcon(JButton button, String iconName, int size) {
		try {
			button.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/fr/wdes/ressources/" + iconName)), size, size)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unused")
	private void setIcon(JLabel label, String iconName, int w, int h) {
		try {
			label.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/fr/wdes/ressources/" + iconName)), w, h)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void setIcon(JLabel label, BufferedImage iconName, int w, int h) {
		label.setIcon(new ImageIcon(ImageUtils.scaleImage(iconName, w, h)));
	}

	private static BufferedImage getDefaultImage() {
		try {
			return ImageIO.read(Launcher.class.getResourceAsStream("/fr/wdes/ressources/face.png"));
		} catch (IOException e) {
			throw new RuntimeException("Erreur de lecture de l'image par défault");
		}
	}
	private static BufferedImage getDefaultLogo() {
		try {
			return ImageIO.read(Launcher.class.getResourceAsStream("/fr/wdes/ressources/logo.png"));
		} catch (IOException e) {
			throw new RuntimeException("Erreur de lecture de l'image par défault");
		}
	}
	private static class CallbackTask implements Callable<BufferedImage> {
		private final Callable<BufferedImage> task;
		private volatile ImageCallback callback;
		CallbackTask(Callable<BufferedImage> task) {
			this.task = task;
		}

		public void setCallback(ImageCallback callback) {
			this.callback = callback;
		}

		public BufferedImage call() throws Exception {
			BufferedImage image = null;
			try {
				image = task.call();
				return image;
			} finally {
				callback.done(image);
			}
		}
	}

	public byte[] BufferedImageToByteArray(BufferedImage orImage){
		  try{

		  ByteArrayOutputStream baos=new ByteArrayOutputStream();

		  ImageIO.write(orImage, "png", baos );

		  byte[] imageBytes=baos.toByteArray();
		  //do something with the byte array
		  return imageBytes;
		  }catch(IOException ie){}
		return null;
		 }
	private CallbackTask getImage(final String user) {
		return new CallbackTask(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				URL url = new URL("https://minotar.net/helm/" + user + "/100");
				byte[] AVATAR = null;
				BufferedImage image = null;


        try {
			Statement s = Launcher.getInstance().db.createStatement();
			ResultSet r = s.executeQuery("SELECT COUNT(ID) AS rowcount FROM Users WHERE USERNAME='"+user+"'");
			r.next();
			int count = r.getInt("rowcount") ;
			r.close() ;

			System.out.println("[Avatar] WdesLaunchers has " + count + " row(s).");


                HttpURLConnection connection = null;
             if(count == 0){
            	 connection = (HttpURLConnection) url.openConnection();
             }
             else{
            	 String query = "SELECT * FROM Users WHERE USERNAME='"+user+"'";
                 Statement statement = Launcher.getInstance().db.createStatement();
                 ResultSet rslt=statement.executeQuery(query);
                 logger.info("Récupération de ta tète de : " + user + "...");
                     rslt.next();
                     String  ETAG = rslt.getString("ETAG");
                     AVATAR=rslt.getBytes("AVATAR");
            	connection = Http.performHead(url, Launcher.getInstance().getProxy(),"If-None-Match", ETAG);
             }

        	if(connection.getResponseCode() == 304 && count > 0){
        		logger.info("Avatar Téléchargé depuis la base de données etag : "+connection.getHeaderField("Etag")+"!!");
        		InputStream in = new ByteArrayInputStream(AVATAR);
        		image = ImageIO.read(in);
                // byte[] imgArr=rslt.getBytes("image");
                // image=Toolkit.getDefaultToolkit().createImage(imgArr);
        	}
        	else if(connection.getResponseCode() == 200){
        		if(count > 0){
        			// HEAD has no body; re-open as GET to actually fetch the new avatar.
        			// Mirrors getLogo() below.
        			connection = (HttpURLConnection) url.openConnection();
        		}
        		logger.info("Avatar Téléchargé !!");
        		InputStream stream = new BufferedInputStream(connection.getInputStream());
			      image = ImageIO.read(stream);
					if (image == null) {
						throw new NullPointerException("No avatar helm downloaded!");
					}

					if(count > 0){
						// Manual upsert: the Users table has no UNIQUE constraint
						// on USERNAME so we can't use INSERT OR REPLACE. Wipe the
						// existing row(s) for this user before the INSERT below
						// to avoid accumulating duplicates each time the avatar
						// is refreshed.
						String deleteSql = "DELETE FROM Users WHERE USERNAME='" + user + "';";
						PreparedStatement deleteStatement = Launcher.getInstance().db.prepareStatement(deleteSql);
						deleteStatement.executeUpdate();
						deleteStatement.close();
						logger.info("Avatar Effacé !!");
					}

					// INSERT the freshly downloaded avatar (paired with the
					// DELETE above when count > 0; on the very first download
					// for this user there's nothing to delete).
					String sql = "INSERT INTO Users (ID,USERNAME,ETAG,AVATAR) " +"VALUES (NULL,'" + user + "', '"+connection.getHeaderField("Etag")+"', ? );";
					PreparedStatement statement = Launcher.getInstance().db.prepareStatement(sql);
					statement.setBytes(1, BufferedImageToByteArray(image));
					statement.executeUpdate();
					statement.close();






					logger.info("Avatar Téléchargé !!");




            }
        	connection.disconnect();
        	return image;

        }
        finally {

        }






			}
		});
	}
	private BufferedImage getLogo(final String uuid) throws Exception {

				URL url = new URL(LauncherConstants.URL_LOGO_BASE + "/logos/" + uuid + ".png");
				byte[] LOGO = null;
				BufferedImage image = null;



			Statement s = Launcher.getInstance().db.createStatement();
			ResultSet r = s.executeQuery("SELECT COUNT(ID) AS rowcount FROM Launchers WHERE UUID='"+uuid+"'");
			r.next();
			int count = r.getInt("rowcount") ;
			r.close() ;

			logger.info("[Logo] WdesLaunchers has " + count + " row(s).");


                HttpURLConnection connection = null;
             if(count == 0){
            	 connection = (HttpURLConnection) url.openConnection();
             }
             else{
            	 String query = "SELECT * FROM Launchers WHERE UUID='"+uuid+"'";
                 Statement statement = Launcher.getInstance().db.createStatement();
                 ResultSet rslt=statement.executeQuery(query);
                 logger.info("[Logo] Récupération du logo du serveur : " + uuid + "...");
                     rslt.next();
                     String  DATE = rslt.getString("DATE");
                     LOGO=rslt.getBytes("LOGO");
            	connection = Http.performHead(url, Launcher.getInstance().getProxy(),"If-Modified-Since", DATE);
             }
        	if(connection.getResponseCode() == 304 && count == 1){
        		logger.info("[Logo] Téléchargé depuis la base de données !!");
        		InputStream in = new ByteArrayInputStream(LOGO);
        		image = ImageIO.read(in);
        	}
        	else if(connection.getResponseCode() == 200){
        		connection = (HttpURLConnection) url.openConnection();
        		InputStream stream = new BufferedInputStream(connection.getInputStream());
			      image = ImageIO.read(stream);
					if (image == null) {
						logger.info("[Logo] Aucun logo n'a pu être téléchargé, le logo par défault serra utilisé !!");
						return getDefaultLogo();
					}

					if(count == 1){
						String sql = "DELETE FROM Launchers WHERE UUID='" + uuid + "';";
						PreparedStatement statement = Launcher.getInstance().db.prepareStatement(sql);
						statement.executeUpdate();
						statement.close();
						logger.info("Logo Effacé !!");
					}

					String sql = "INSERT INTO Launchers (ID,UUID,DATE,LOGO) VALUES (NULL,'" + uuid + "', '"+connection.getHeaderField("Last-Modified")+"', ? );";

					PreparedStatement statement = Launcher.getInstance().db.prepareStatement(sql);
					statement.setBytes(1, BufferedImageToByteArray(image));
					statement.executeUpdate();
					statement.close();
					logger.info("Logo Téléchargé !!");

            }
        	connection.disconnect();
        	return image;




			}


    public LauncherPanel(final Launcher launcher) {
        this.launcher = launcher;
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        launcherhome = new BackgroundImage(FRAME_WIDTH, FRAME_HEIGHT);
        //bottomBar = new BottomBarPanel(launcher);
        tabPanel = new LauncherTabPanel(launcher);

        launcher.getProfileManager().addRefreshedProfilesListener(this);
        launcher.getVersionManager().addRefreshedVersionsListener(this);
        createInterface();
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
    }


    protected void createInterface() {
        add(createLauncherInterface(), "launcher");

    }

    protected JPanel createLauncherInterface() {

        final JPanel result = new JPanel(new BorderLayout());

        //tabPanel.getBlog().setPage(LauncherConstants.URL_BLOG);

        topWrapper = new JLayeredPane();
        CreateHome().setVisible(true);
        getTabPanel().getConsole().setVisible(true);
      	topWrapper.add(close, new Integer(3));
       	topWrapper.add(minimize, new Integer(3));
      	topWrapper.add(options, new Integer(3));
        topWrapper.setLayout(new BorderLayout());
        topWrapper.add(CreateHome(), "Center");


        //getTabPanel().getConsole().setVisible(false);


        result.add(topWrapper, "Center");
        //result.add(bottomBar, "South");

        return result;
    }


/*
    public BottomBarPanel getBottomBar() {
        return bottomBar;
    }
*/
    public Launcher getLauncher() {
        return launcher;
    }

    public LiteProgressBar getProgressBar() {
        return progressBar;
    }

    public LauncherTabPanel getTabPanel() {
        return tabPanel;
    }

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent)e.getSource());
		}
	}
	public void action(String command, JComponent c) {


		logger.info("Action : "+command);
		if(command == CLOSE_ACTION){
			Launcher.getInstance().closeLauncher("Boutton de fermeture");
			//System.exit(0);
		}
		else if(command == MINIMIZE_ACTION){
			Launcher.getInstance().minimizeLauncher();
		}
		else if(command == OPTIONS_ACTION){
			if(console == false){
			CreateHome().setVisible(false);
			getTabPanel().getConsole().setVisible(true);
			topWrapper.remove(CreateHome());
			topWrapper.add(getTabPanel().getConsole(), "Center");
			console = true;
			}
		else if(console == true){

				//getTabPanel().getConsole().setVisible(false);
				topWrapper.remove(getTabPanel().getConsole());
				topWrapper.add(CreateHome(), "Center");
				CreateHome().setVisible(true);
				checkState();
				checkPlayerState();
				console = false;
		}
		}
		else if (command == REMOVE_USER){
			log_out();
			checkState();
			checkPlayerState();
			DynamicButton userButton = removeButtons.get((JButton)c);
			userButton.setVisible(false);
			userButton.setEnabled(false);
			getRootPane().remove(userButton);
			c.setVisible(false);
			c.setEnabled(false);
			getRootPane().remove(c);
			progressBar.setValue(100);
			progressBar.setText("Merci de vous connecter !!");
		}
		else if(command == IMAGE_LOGIN_ACTION){
			ProfileEditorPopup.showEditProfileDialog(launcher, launcher.getProfileManager().getSelectedProfile());
		}
		else if(command == GAME_ACTION){
			getLauncher().getGameLauncher().playGame();
        }
		else if(command == LOGIN_ACTION){
			tryLogIn();
			checkState();
			checkPlayerState();

        }
		else if (command.equals(STEAM_ACTION)) {
			try {
				steamURL = new URI("http://spout.in/steam");
			} catch (URISyntaxException ex) {
				ex.printStackTrace();
			}
			Compatibility.browse(steamURL);
		} else if (command.equals(FACEBOOK_ACTION)) {
			try {
				facebookURL = new URI("http://spout.in/facebook");
			} catch (URISyntaxException ex) {
				ex.printStackTrace();
			}
			Compatibility.browse(facebookURL);
		} else if (command.equals(TWITTER_ACTION)) {
			try {
				twitterURL = new URI("http://spout.in/twitter");
			} catch (URISyntaxException ex) {
				ex.printStackTrace();
			}
			Compatibility.browse(twitterURL);
		}  else if (command.equals(GPLUS_ACTION)) {
			try {
				gplusURL = new URI("http://spout.in/gplus");
			} catch (URISyntaxException ex) {
				ex.printStackTrace();
			}
			Compatibility.browse(gplusURL);
		}  else if (command.equals(YOUTUBE_ACTION)) {
			try {
				youtubeURL = new URI("http://spout.in/youtube");
			} catch (URISyntaxException ex) {
				ex.printStackTrace();
			}
			Compatibility.browse(youtubeURL);
		}
		else{
			logger.info("Action de ?? : "+command);
		}


	}
    public void checkState() {
        final Profile profile = launcher.getProfileManager().getProfiles().isEmpty() ? null : launcher.getProfileManager().getSelectedProfile();
        final AuthenticationService auth = profile == null ? null : launcher.getProfileManager().getAuthDatabase().getByUUID(profile.getPlayerUUID());

        if(auth == null || !auth.isLoggedIn() || launcher.getVersionManager().getVersions(profile.getVersionFilter()).isEmpty()) {
            this.gamebtn.setEnabled(false);
            this.gamebtn.setText("Jouer !");

        }
        else if(auth.getSelectedProfile() == null) {
        	this.gamebtn.setEnabled(true);
            this.gamebtn.setText("Demo");

        }
        else if(auth.canPlayOnline()) {
        	this.gamebtn.setEnabled(true);
        	this.gamebtn.setText("Jouer !");
        }
        else if(auth.canLogIn()) {
        }
        else if(auth.isLoggedIn()) {
        	this.progressBar.setVisible(false);
        }
        else {
        	this.gamebtn.setEnabled(true);
            launcher.closeLauncher("LauncherPanel CheckState #360");
        }

        if(launcher.getGameLauncher().isWorking())
        	this.gamebtn.setEnabled(false);
    }
    public void checkPlayerState() {
        final Profile profile = launcher.getProfileManager().getProfiles().isEmpty() ? null : launcher.getProfileManager().getSelectedProfile();
        final AuthenticationService auth = profile == null ? null : launcher.getProfileManager().getAuthDatabase().getByUUID(profile.getPlayerUUID());
        final List<VersionSyncInfo> versions = profile == null ? null : launcher.getVersionManager().getVersions(profile.getVersionFilter());
        VersionSyncInfo version = profile == null || versions.isEmpty() ? null : (VersionSyncInfo) versions.get(0);

        if(profile != null && profile.getLastVersionId() != null) {
            final VersionSyncInfo requestedVersion = launcher.getVersionManager().getVersionSyncInfo(profile.getLastVersionId());
            if(requestedVersion != null && requestedVersion.getLatestVersion() != null)
                version = requestedVersion;
        }

        if(auth == null || !auth.isLoggedIn()) {
            welcomeText.setText("Bienvenue, visiteur! Connecte toi.");
            //this.logoutbtn.setVisible(false);
            this.gamebtn.setVisible(false);
            this.loginbtn.setVisible(true);
            this.progressBar.setVisible(true);
            this.progressBar.setValue(100);
            this.progressBar.setString("En attente de votre connexion");

        }
        else if(auth.getSelectedProfile() == null) {
            welcomeText.setText("Bienvenue, joueur!");
            //this.logoutbtn.setVisible(true);
        }
        else {
            welcomeText.setText("Bienvenue," + auth.getUsername() + "");

            this.loginbtn.setVisible(false);
            this.gamebtn.setVisible(true);
            this.gamebtn.setEnabled(true);

            //this.logoutbtn.setVisible(true);
            this.logininput.setText(auth.getUsername());
            this.progressBar.setVisible(false);
				// Create callable
				CallbackTask callback = getImage(auth.getUsername());

				// Start callable
				FutureTask<BufferedImage> futureImage = new FutureTask<BufferedImage>(callback);
				Thread downloadThread = new Thread(futureImage, "Avatar");
				downloadThread.setDaemon(true);
				downloadThread.start();

				// Create future image, using default mc avatar for now
				FutureImage userImage = new FutureImage(getDefaultImage());
				callback.setCallback(userImage);

				DynamicButton userButton = new DynamicButton( userImage, 44, auth.getUsername());
				userButton.setFont(largerMinecraft.deriveFont(14F));
				launcherhome.add(userButton.getRemoveLabel());
				launcherhome.add(userButton.getRemoveIcon());
				userImage.setRepaintCallback(userButton);

				userButton.setBounds((FRAME_WIDTH /2) - 90/2, (FRAME_HEIGHT - 110) / 2 , 90, 90);

				userButton.setActionCommand(IMAGE_LOGIN_ACTION);
				userButton.addActionListener(this);
				setIcon(userButton.getRemoveIcon(), "remove.png", 16);
				userButton.getRemoveIcon().addActionListener(this);
				userButton.getRemoveIcon().setActionCommand(REMOVE_USER);
				userButton.getRemoveIcon().setBorder(BorderFactory.createEmptyBorder());
				userButton.getRemoveIcon().setContentAreaFilled(false);
				launcherhome.add(userButton);
				removeButtons.put(userButton.getRemoveIcon(), userButton);
        }

        if(version == null)
            versionText.setText("Chargement des versions...");
        else if(version.isUpToDate())
            versionText.setText("Prèt pour jouer à " + LauncherConstants.SERVER_NAME +" "+ version.getLatestVersion().getId());
        else if(version.isInstalled())
            versionText.setText("Prèt pour mettre à jour " + version.getLatestVersion().getId());
        else if(version.isOnRemote())
            versionText.setText("Prèt pour télécharger " + version.getLatestVersion().getId());
    }
	public void onVersionsRefreshed(VersionManager paramVersionManager) {
		checkState();
		checkPlayerState();
	}

	public void onProfilesRefreshed(ProfileManager paramProfileManager) {
		checkState();
		checkPlayerState();
	}

	public boolean shouldReceiveEventsInUIThread() {

		return true;
	}
    public void tryLogIn() {
        if(authentication.isLoggedIn() && authentication.getSelectedProfile() == null && ArrayUtils.isNotEmpty(authentication.getAvailableProfiles())) {


            GameProfile selectedProfile = null;

            if(selectedProfile == null)
                selectedProfile = authentication.getAvailableProfiles()[0];

            final GameProfile finalSelectedProfile = selectedProfile;
            launcher.getVersionManager().getExecutorService().execute(new Runnable() {
                public void run() {
                    try {
                        authentication.selectGameProfile(finalSelectedProfile);
                        launcher.getProfileManager().getAuthDatabase().register(authentication.getSelectedProfile().getId(), authentication);
                        setLoggedIn(authentication.getSelectedProfile().getId(),authentication.getSelectedProfile().getName());
                    }
                    catch(final InvalidCredentialsException ex) {
                    	logger.warn(ex);
                        versionText.setText("Désolé, mais nous n'avons pu vous identifier dès maintenant."+ "S'il vous plait réessayer plus tard." );

                    }
                    catch(final AuthenticationException ex) {
                    	logger.warn(ex);
                        versionText.setText("Désolé, mais nous ne pouvions pas nous connecter à nos serveurs."+ "S'il vous plait assurez-vous que vous êtes en ligne et que Minecraft n'est pas bloqué." );

                    }
                }
            });
        }
        else {

            authentication.logOut();
            authentication.setUsername(logininput.getText());
            authentication.setPassword(String.valueOf(passwordinput.getPassword()));

            final int passwordLength = passwordinput.getPassword().length;



            launcher.getVersionManager().getExecutorService().execute(new Runnable() {
                public void run() {
                    try {
                        authentication.logIn();
                        final AuthenticationDatabase authDatabase = launcher.getProfileManager().getAuthDatabase();

                        if(authentication.getSelectedProfile() == null) {
                            if(ArrayUtils.isNotEmpty(authentication.getAvailableProfiles())) {


                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                    	logininput.setEditable(false);
                                        passwordinput.setEditable(false);
                                        passwordinput.setText(StringUtils.repeat('*', passwordLength));
                                    }
                                });
                            }
                            else {
                                final String uuid = "demo-" + authentication.getUsername();
                                authDatabase.register(uuid, authentication);
                                setLoggedIn(uuid,"demo");
                            }
                        }
                        else {
                            authDatabase.register(authentication.getSelectedProfile().getId(), authentication);
                            setLoggedIn(authentication.getSelectedProfile().getId(),authentication.getSelectedProfile().getName());
                        }
                    }
                    catch(final UserMigratedException ex) {
                    	logger.warn(ex);
                    	launcher.getLauncherPanel().getProgressBar().setString("Vous devez rentrer votre email car votre compte est migré");
                        versionText.setText( "Désolé, mais nous ne pouvons pas vous connecter avec votre nom d'utilisateur."+ "Vous avez migré votre compte, veuillez utiliser votre adresse e-mail." );
                    }
                    catch(final InvalidCredentialsException ex) {
                    	logger.warn(ex);
                    launcher.getLauncherPanel().getProgressBar().setString("Identifiant ou mot de passe incorrect.");
                        versionText.setText("Désolé, mais votre nom d'utilisateur ou mot de passe est incorrect !"+ "S'il vous plait essayer de nouveau. Si vous avez besoin d'aide, essayez le lien 'Mot de passe oublié'." );
                    }
                    catch(final AuthenticationException ex) {
                    	logger.warn(ex);
                    	launcher.getLauncherPanel().getProgressBar().setString("Impossible de joindre les serveurs de : "+LauncherConstants.SERVER_NAME);
                        versionText.setText("Désolé, mais les serveurs sont OFF."+ "S'il vous plait assurez-vous que vous êtes en ligne et que Minecraft n'est pas OP." );
                    }
                }
            });
        }

    }


    public void setLoggedIn(final String uuid,final String username) {
    	if(!loggedin ){
    		loggedin = true;
    		logger.info("Demande d'enregistrement pour : "+username+" => "+uuid);
	   Profile selectedProfile = launcher.getProfileManager().getSelectedProfile();
        final AuthenticationService auth = launcher.getProfileManager().getAuthDatabase().getByUUID(uuid);

        selectedProfile.setPlayerUUID(uuid);

        if(selectedProfile.getName().equals(LauncherConstants.DEFAULT_PROFILE_NAME) && auth.getSelectedProfile() != null) {
            final String playerName = auth.getSelectedProfile().getName();
            String profileName = auth.getSelectedProfile().getName();
            int count = 1;

            while(launcher.getProfileManager().getProfiles().containsKey(profileName))
                profileName = playerName + " " + ++count;
            logger.info("Profil : "+profileName);
            final Profile newProfile = new Profile(selectedProfile);
            newProfile.setName(profileName);
            launcher.getProfileManager().getProfiles().put(profileName, newProfile);
            launcher.getProfileManager().getProfiles().remove(LauncherConstants.DEFAULT_PROFILE_NAME);
            launcher.getProfileManager().setSelectedProfile(profileName);
            this.progressBar.setString("Connecté !!");
            this.progressBar.setValue(100);
        }
        try {
        	if(remember.isSelected()){
        	logger.info("Sauvegarde du profil.");
        	launcher.getProfileManager().saveProfiles();
        	}
        	else{
        		logger.info("Le profil ne serra pas sauvegardé.");
        	}
        }
        catch(final IOException e) {
        	logger.warn("Couldn't save profiles after logging in!", e);
        }

        if(uuid == null)
            launcher.closeLauncher("LauncherPanel #537");
        else
        	launcher.getProfileManager().fireRefreshEvent();

    	}
    }

}
