package wdes.fr;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;



import wdes.fr.launch.Wdes;


public class Launcher extends Applet
  implements Runnable, AppletStub, MouseListener
{
  private static final long serialVersionUID = 1L;
  public Map<String, String> customParameters = new HashMap<String, String>();
  private Mise_a_jour gameUpdater;
  private boolean gameUpdaterStarted = false;
  private Applet applet;
  private Image bgImage;

  private boolean hasMouseListener = false;
  private VolatileImage img;







  public void init() {
    if (this.applet != null) {
      this.applet.init();
      return;
    }
    try {
        this.bgImage = ImageIO.read(Ressources.getResourceAsStream("/wdes/fr/ressources/6.png")).getScaledInstance(32, 32, 16);
      } catch (IOException e) {
        e.printStackTrace();
      }



      this.gameUpdater = new Mise_a_jour();
      }

  public void start() {
    if (this.applet != null) {
      this.applet.start();
      return;
    }
    if (this.gameUpdaterStarted) return;

    Thread t = new Thread() {
      public void run() {
        Launcher.this.gameUpdater.run_maj();
        
        	try {
        		
							Wdes.launchGame();

        				
			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
      }
    };
    t.setDaemon(true);
    t.start();

    t = new Thread() {
      public void run() {
        while (Launcher.this.applet == null) {
          Launcher.this.repaint();
          try {
            Thread.sleep(10L);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    };
    t.setDaemon(true);
    t.start();

    this.gameUpdaterStarted = true;
  }





  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g2) {
    if (this.applet != null) return;

    int w = getWidth() / 2;
    int h = getHeight() / 2;
    if ((this.img == null) || (this.img.getWidth() != w) || (this.img.getHeight() != h)) {
      this.img = createVolatileImage(w, h);
    }

    Graphics g = this.img.getGraphics();
    for (int x = 0; x <= w / 32; x++) {
      for (int y = 0; y <= h / 32; y++)
        g.drawImage(this.bgImage, x * 32, y * 32, null);
    }

   
    if (Mise_a_jour.pauseAskUpdate) {
      if (!this.hasMouseListener) {
        this.hasMouseListener = true;
        addMouseListener(this);
      }
      g.setColor(Color.LIGHT_GRAY);
      String msg = "Nouvelle maj dispo!";
      g.setFont(new Font(null, 1, 20));
      FontMetrics fm = g.getFontMetrics();
      g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);

      g.setFont(new Font(null, 0, 12));
      fm = g.getFontMetrics();

      g.fill3DRect(w / 2 - 56 - 8, h / 2, 56, 20, true);
      g.fill3DRect(w / 2 + 8, h / 2, 56, 20, true);

      msg = "Voulez vous mettre a jour?";
      g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - 8);

      g.setColor(Color.BLACK);
      msg = "Oui";
      g.drawString(msg, w / 2 - 56 - 8 - fm.stringWidth(msg) / 2 + 28, h / 2 + 14);
      msg = "Non";
      g.drawString(msg, w / 2 + 8 - fm.stringWidth(msg) / 2 + 28, h / 2 + 14);
    }
    else
    {
      g.setColor(Color.LIGHT_GRAY);
     
      String msg = "Initilisation de "+Parametres.getparam("SERVER_NAME");
      
      if (Mise_a_jour.fatalError) {
        msg = "Echec du lancement";
      }

      g.setFont(new Font(null, 1, 20));
      FontMetrics fm = g.getFontMetrics();
      g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);

      g.setFont(new Font(null, 0, 12));
      fm = g.getFontMetrics();
      msg = gameUpdater.update_description();
      if (Mise_a_jour.fatalError) {
        msg = Mise_a_jour.fatalErrorDescription;
      }

      g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 + fm.getHeight() * 1);
      msg = Mise_a_jour.subtaskMessage;
      g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 + fm.getHeight() * 2);

      if (!Mise_a_jour.fatalError) {
        g.setColor(Color.black);
        g.fillRect(64, h - 64, w - 128 + 1, 5);
        g.setColor(new Color(32768));
        g.fillRect(64, h - 64, Mise_a_jour.percentage * (w - 128) / 100, 4);
        g.setColor(new Color(2138144));
        g.fillRect(65, h - 64 + 1, Mise_a_jour.percentage * (w - 128) / 100 - 2, 1);
      }
    }

    g.dispose();

    g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
  }

  public void run() {
  }



  public void appletResize(int width, int height)
  {
  }



  public void mouseClicked(MouseEvent arg0) {
  }

  public void mouseEntered(MouseEvent arg0) {
  }

  public void mouseExited(MouseEvent arg0) {
  }

  public void mousePressed(MouseEvent me) {
    int x = me.getX() / 2;
    int y = me.getY() / 2;
    int w = getWidth() / 2;
    int h = getHeight() / 2;

    if (contains(x, y, w / 2 - 56 - 8, h / 2, 56, 20)) {
      removeMouseListener(this);
      this.gameUpdater.shouldUpdate = true;
      Mise_a_jour.pauseAskUpdate = false;
      this.hasMouseListener = false;
    }
    if (contains(x, y, w / 2 + 8, h / 2, 56, 20)) {
      removeMouseListener(this);
      this.gameUpdater.shouldUpdate = false;
      Mise_a_jour.pauseAskUpdate = false;
      this.hasMouseListener = false;
    }
  }

  private boolean contains(int x, int y, int xx, int yy, int w, int h) {
    return (x >= xx) && (y >= yy) && (x < xx + w) && (y < yy + h);
  }

  public void mouseReleased(MouseEvent arg0)
  {
  }


}