package wdes.fr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.json.JSONObject;




public class Main extends Frame
{
  private static final long serialVersionUID = 1L;
  public Map<String, String> customParameters = new HashMap<String, String>();
  public Launcher launcher;
  public Connection loginForm;

  public Main() throws FontFormatException, IOException
  {
	  
    super("Launcher-"+Parametres.getparam("SERVER_NAME")+"(by Wdes)");
    setUndecorated(true);
    setBackground(Color.BLACK);
    System.setProperty("file.encoding","UTF-8");
    this.loginForm = new Connection(this);
    final JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    p.add(this.loginForm, "Center");
    p.setPreferredSize(new Dimension(854, 480));
    setLayout(new BorderLayout());
    add(p, "Center");

    pack();
    setLocationRelativeTo(null);
    try
    {
      setIconImage(ImageIO.read(Ressources.getResourceAsStream("/wdes/fr/ressources/3.png")));
    } catch (IOException e1) {
      
    }

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent arg0) {
        new Thread() {
          public void run() {
            try {
              Thread.sleep(30000L);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            Console.log("FORCING EXIT!");
            System.exit(0);
          }
        }
        .start();
      } 
    });
  }

  public void JOUER_HORS_LIGNE(String userName) {
    try {
      if ((userName == null) || (userName.length() <= 0)) {
        userName = "Joueur";
      }
      this.launcher = new Launcher();
      this.launcher.init();
      removeAll();
      add(this.launcher, "Center");
      validate();
      this.launcher.start();
      this.loginForm = null;
      setTitle("Launcher-"+Parametres.getparam("SERVER_NAME")+"(by Wdes)");
    } catch (Exception e) {
      e.printStackTrace();
      showError(e.toString());
    }
  }

  public void login(String userName, String password) {
    try {
      String parameters = "username=" + URLEncoder.encode(userName, "UTF-8") + "&password=" + Secure.md5(password) + "&uuid=" + Parametres.getparam("UUID");
      String result = Util.HTTPS_POST(Parametres.getparam("URL_AUTHENTICATION_SERVER"), parameters);
      Console.log(result);
      if (result == null) {
        showError("Connection impossible a "+Parametres.getparam("SERVER_NAME")+"(by Wdes)");
        this.loginForm.setNoNetwork();
        result = "{\"reponse\":\"\"}";
        return;
      }
      Console.log(result);
      JSONObject obj = new JSONObject(result);
      String etat = obj.getString("reponse");
      
      Boolean authentification = obj.getBoolean("authentification");
      if(!authentification){
        if (etat.trim().equals("erreur_de_remplissage")) {
          showError("Mauvais login ou Mot de passe");
        } 
      }
      else{
      this.launcher = new Launcher();
      this.launcher.init();

      removeAll();
      add(this.launcher, "Center");
      validate();
      this.launcher.start();
      this.loginForm.loginOk();
      this.loginForm = null;
      setTitle("Launcher-"+Parametres.getparam("SERVER_NAME")+"(by Wdes)");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      //e.toString()
      showError("Erreur de réponse !!");
      this.loginForm.setNoNetwork();
    }
   
  }
  
  

  private void showError(String error) {
    removeAll();
    add(this.loginForm);
    this.loginForm.setError(error);
    validate();
  }



  public static void main(String[] args) throws FontFormatException, IOException {
	  /*
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception localException) {
    }
    */
    Main launcherFrame = new Main();
    launcherFrame.setVisible(true);

    ComponentMover cm = new ComponentMover();
    cm.registerComponent(launcherFrame);
   
    System.out.print(Ressources.getJarDir(Main.class));

  }
}