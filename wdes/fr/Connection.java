package wdes.fr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;



import wdes.fr.launch.Wdes;



public class Connection extends T3 implements ActionListener{


  private static final long serialVersionUID = 1L;
  private static final Color LINK_COLOR = Color.WHITE;


  public static JTextField userName = new JTextField(20);
  public static JPasswordField password = new JPasswordField(20);
  private static Checkbox rememberBox = new Checkbox("Se souvenir du mot de passe");
  private static Boutton launchButton = new Boutton("Connection");
  private static Boutton optionsButton = new Boutton("Options");
  private Boutton retryButton = new Boutton("Réessayer");
  private Boutton offlineButton = new Boutton("Jouer Hors Ligne");
  private static Label errorLabel = new Label("", 0);

  private Main launcherFrame;
  private static boolean outdated = false;
private wdes.fr.BT close;
private wdes.fr.BT options;
  public Connection(final Main launcherFrame) throws FontFormatException, IOException
  {
	 
	  Font myFont = null;
	  Font mylFont = null;
	  InputStream is = Wdes.class.getResourceAsStream("/wdes/fr/ressources/font.ttf");
	  InputStream isd = Wdes.class.getResourceAsStream("/wdes/fr/ressources/font.ttf");



		 myFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, 12f);
	  mylFont = Font.createFont(Font.TRUETYPE_FONT, isd).deriveFont(Font.PLAIN, 9f);
	  GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	  ge.registerFont(myFont);
	  Connection.userName.setFont(myFont);
	  Connection.launchButton.setFont(mylFont);
	  Connection.optionsButton.setFont(mylFont);
	  Connection.rememberBox.setFont(mylFont);
	  Connection.errorLabel.setFont(mylFont);
	  
    this.launcherFrame = launcherFrame;

    BorderLayout gbl = new BorderLayout();
    setLayout(gbl);
    
    add(buildMainLoginPanel(), "Center");

    readUsername();

    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        Connection.this.doLogin();
      }
    };
    Connection.userName.setBackground(new Color(220, 220, 220));
    Connection.userName.setBorder(new B2(5, getBackground()));
    Connection.password.setBorder(new B2(5, getBackground()));
    Connection.password.setBackground(new Color(220, 220, 220));
    Connection.userName.addActionListener(al);
    password.addActionListener(al);

    this.retryButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Connection.errorLabel.setText("");
        Connection.this.removeAll();
        Connection.this.add(Connection.this.buildMainLoginPanel(), "Center");
        Connection.this.validate();
      }
    });
    //                    OFFLINE                              //
    this.offlineButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        launcherFrame.JOUER_HORS_LIGNE(Connection.userName.getText());
      }
    });
    //\\                    OFFLINE                              //\\
    launchButton.addActionListener(al);

    optionsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        new Options(launcherFrame).setVisible(true);
      }
    });
  }

  public void doLogin() {
    setLoggingIn();
    new Thread() {
      public void run() {
        try {
          Connection.this.launcherFrame.login(Connection.userName.getText(), new String(Connection.password.getPassword()));
        } catch (Exception e) {
        //e.toString()
          Connection.this.setError("Réponse non valide merci de réessayer");
        }
      }
    }
    .start();
    
  }

  private void readUsername() {
    try {
      File lastLogin = new File(Util.getWorkingDirectory(Parametres.getparam("APPDATA")), "login.wdes");

      Cipher cipher = Secure.getCipher(2, "wdeslauncher");
      DataInputStream dis;
      if (cipher != null)
        dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
      else {
        dis = new DataInputStream(new FileInputStream(lastLogin));
      }
      Connection.userName.setText(dis.readUTF());
      password.setText(dis.readUTF());
      rememberBox.setSelected(password.getPassword().length > 0);
      //Console.log(password.getText());
      dis.close();
    } catch (Exception e) {
    
    }
  }

  private void writeUsername() {
    try {
      File lastLogin = new File(Util.getWorkingDirectory(Parametres.getparam("APPDATA")), "login.wdes");

      Cipher cipher = Secure.getCipher(1, "wdeslauncher");
      DataOutputStream dos;
      if (cipher != null)
        dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
      else {
        dos = new DataOutputStream(new FileOutputStream(lastLogin));
      }
      dos.writeUTF(Connection.userName.getText());
      dos.writeUTF(rememberBox.isSelected() ? new String(password.getPassword()) : "");
      dos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static final URL optionsIcon = Connection.class.getResource("/wdes/fr/ressources/2.png");
  private static final URL closeIcon = Connection.class.getResource("/wdes/fr/ressources/1.png");
  private IDF getUpdateNews()
  {
	  
	       this.options = new BT();
	       this.options.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(optionsIcon)));
	       this.options.setBounds(777, 0, 37, 20);
	       this.options.setTransparency(0.7F);
	       this.options.setHoverTransparency(1.0F);
	       this.options.setActionCommand("options");
	       this.options.addActionListener(this);
	       this.options.setBorder(BorderFactory.createEmptyBorder());
	       this.options.setContentAreaFilled(false);
	       this.close = new BT();
	       this.close.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(closeIcon)));
	       this.close.setBounds(816, 0, 37, 20);
	       this.close.setTransparency(0.7F);
	       this.close.setHoverTransparency(1.0F);
	       this.close.setActionCommand("close");
	  	   this.close.addActionListener(this);
	       this.close.setBorder(BorderFactory.createEmptyBorder());
	       this.close.setContentAreaFilled(false);
	       IDF p = new IDF(880, 520);
	       JLabel logo = new JLabel();
	       logo.setBounds(100,      10,    700,   109);
	       //             gauche   hauteur  zoom    grosseur
	       setIcon(logo, "5.png", logo.getWidth(), logo.getHeight());
	  p.add(logo);
	  p.add(close);
	  p.add(options);
	return p;
    
  }
     public void actionPerformed(ActionEvent e)
    {
        if ((e.getSource() instanceof JComponent)) {
	       try {
			action(e.getActionCommand(), (JComponent)e.getSource());
		} catch (FontFormatException | IOException e1) {
		
			e1.printStackTrace();
		}
     }
    }
  private void action(String action, JComponent c) throws FontFormatException, IOException
    {
      if (action.equals("close"))
     {
       System.exit(3);
       }
      if (action.equals("options"))
     {
    	  new Options(launcherFrame).setVisible(true);
       }
      
  }
     private void setIcon(JLabel label, String iconName, int w, int h)
    {
       try
       {
        label.setIcon(new ImageIcon(Img1.scaleImage(ImageIO.read(Ressources.getResourceAsStream("/wdes/fr/ressources/" + iconName)), w, h)));
      }
       catch (IOException e)
       {
        e.printStackTrace();
       }
    }
  private JPanel buildMainLoginPanel() {
	  
    JPanel p = new T3(new BorderLayout());
    
    
    p.add(getUpdateNews(), "Center");

    JPanel southPanel = new Wool();
    southPanel.setLayout(new BorderLayout());
    southPanel.add(new T3(), "Center");
    southPanel.add(center(buildLoginPanel()), "Center");
    southPanel.setPreferredSize(new Dimension(100, 100));
    
    p.add(southPanel, "South");
    return p;
  }

  private JPanel buildLoginPanel() {
    T3 panel = new T3();
    panel.setInsets(4, 0, 4, 0);

    BorderLayout layout = new BorderLayout();
    layout.setHgap(0);
    layout.setVgap(8);
    panel.setLayout(layout);

    GridLayout gl1 = new GridLayout(0, 1);
    gl1.setVgap(2);
    GridLayout gl2 = new GridLayout(0, 1);
    gl2.setVgap(2);
    GridLayout gl3 = new GridLayout(0, 1);
    gl3.setVgap(2);

    T3 titles = new T3(gl1);
    T3 values = new T3(gl2);

    titles.add(new Label("Identifiant:", 4));
    titles.add(new Label("Mot de passe:", 4));
    titles.add(new Label("", 4));

    values.add(Connection.userName);
    values.add(password);
    values.add(rememberBox);

    panel.add(titles, "West");
    panel.add(values, "Center");

    T3 loginPanel = new T3(new BorderLayout());

    T3 third = new T3(gl3);
    titles.setInsets(0, 0, 0, 4);
    third.setInsets(0, 10, 0, 10);
   
    third.add(optionsButton);
    third.add(launchButton);
    try
    {
      if (outdated) {
        Label accountLink = getUpdateLink();
        third.add(accountLink);
      }
      else
      {
        Label accountLink = new Label("Besoin d'un compte?") {
          private static final long serialVersionUID = 0L;

          public void paint(Graphics g) { super.paint(g);

            int x = 0;
            int y = 0;

            FontMetrics fm = g.getFontMetrics();
            int width = fm.stringWidth(getText());
            int height = fm.getHeight();

            if (getAlignmentX() == 2.0F) x = 0;
            else if (getAlignmentX() == 0.0F) x = getBounds().width / 2 - width / 2;
            else if (getAlignmentX() == 4.0F) x = getBounds().width - width;
            y = getBounds().height / 2 + height / 2 - 1;

            g.drawLine(x + 2, y, x + width - 2, y); }

          public void update(Graphics g)
          {
            paint(g);
          }
        };
        accountLink.setCursor(Cursor.getPredefinedCursor(12));
        
        accountLink.addMouseListener(new MouseAdapter() {
          public void mousePressed(MouseEvent arg0) {
            try {
              Util.openLink(new URL(Parametres.getparam("URL_INSCRIPTION")).toURI());
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        accountLink.setForeground(LINK_COLOR);
        third.add(accountLink);
      }

    }
    catch (Error localError)
    {
    }

    loginPanel.add(third, "Center");
    panel.add(loginPanel, "East");

    errorLabel.setFont(new Font(null, 2, 16));
    errorLabel.setForeground(new Color(16728128));
    errorLabel.setText("");
    panel.add(errorLabel, "North");

    return panel;
  }

  private Label getUpdateLink() {
    Label accountLink = new Label("Vous avez besoin de mettre a jour le launcher!") {
      private static final long serialVersionUID = 0L;

      public void paint(Graphics g) { super.paint(g);

        int x = 0;
        int y = 0;

        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(getText());
        int height = fm.getHeight();

        if (getAlignmentX() == 2.0F) x = 0;
        else if (getAlignmentX() == 0.0F) x = getBounds().width / 2 - width / 2;
        else if (getAlignmentX() == 4.0F) x = getBounds().width - width;
        y = getBounds().height / 2 + height / 2 - 1;

        g.drawLine(x + 2, y, x + width - 2, y); }

      public void update(Graphics g)
      {
        paint(g);
      }
    };
    accountLink.setCursor(Cursor.getPredefinedCursor(12));

    accountLink.setForeground(LINK_COLOR);
    return accountLink;
  }

  private JPanel buildMainOfflinePanel() {
    JPanel p = new T3(new BorderLayout());
    p.add(getUpdateNews(), "Center");
   
    JPanel southPanel = new Wool();
    southPanel.setLayout(new BorderLayout());
    southPanel.add(new T3(), "Center");
    southPanel.add(center(buildOfflinePanel()), "Center");
    southPanel.setPreferredSize(new Dimension(100, 100));

    p.add(southPanel, "South");
    return p;
  }

  private Component center(Component c) {
    T3 tp = new T3(new GridBagLayout());
    tp.add(c);
    return tp;
  }

  private T3 buildOfflinePanel()
  {
    T3 panel = new T3();
    panel.setInsets(0, 0, 0, 20);

    BorderLayout layout = new BorderLayout();
    panel.setLayout(layout);

    T3 loginPanel = new T3(new BorderLayout());

    GridLayout gl = new GridLayout(0, 1);
    gl.setVgap(2);
    T3 pp = new T3(gl);
    pp.setInsets(0, 8, 0, 0);

    pp.add(this.retryButton);
    pp.add(this.offlineButton);

    loginPanel.add(pp, "East");

    boolean canPlayOffline = Connection.userName.getText() != null;
    this.offlineButton.setEnabled(canPlayOffline);
    if (!canPlayOffline) {
      loginPanel.add(new Label("(Pas Téléchargé)", 4), "Center");
    }
    panel.add(loginPanel, "Center");

    T3 p2 = new T3(new GridLayout(0, 1));
    errorLabel.setFont(new Font(null, 2, 16));
    errorLabel.setForeground(new Color(16728128));
    p2.add(errorLabel);
    if (outdated) {
      Label accountLink = getUpdateLink();
      p2.add(accountLink);
    }

    loginPanel.add(p2, "Center");

    return panel;
  }

  public void setError(String errorMessage) {
    removeAll();
    add(buildMainLoginPanel(), "Center");
   errorLabel.setText(errorMessage);
    validate();
  }

  public void loginOk() {
    writeUsername();
  }

  public void setLoggingIn() {
    removeAll();
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(getUpdateNews(), "Center");

    JPanel southPanel = new Wool();
    southPanel.setLayout(new BorderLayout());
    southPanel.add(new T3(), "Center");
    JLabel label = new Label("Connection a "+Parametres.getparam("SERVER_NAME")+"...                      ", 0);
    label.setFont(new Font(null, 1, 16));
    southPanel.add(center(label), "East");
    southPanel.setPreferredSize(new Dimension(100, 100));

    panel.add(southPanel, "South");

    add(panel, "Center");
    validate();
  }

  public void setNoNetwork() {
    removeAll();
    add(buildMainOfflinePanel(), "Center");
    validate();
  }


  public static void setOutdated()
  {
    outdated = true;
  }



}