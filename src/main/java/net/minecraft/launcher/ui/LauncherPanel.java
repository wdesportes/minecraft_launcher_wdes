package net.minecraft.launcher.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.ui.tabs.LauncherTabPanel;
@SuppressWarnings("serial")
public class LauncherPanel extends JPanel implements ActionListener {
    public static final String CARD_DIRT_BACKGROUND = "loading";
    public static final String CARD_LOGIN = "login";
    public static final String CARD_LAUNCHER = "launcher";
    private final CardLayout cardLayout;
    private final LauncherTabPanel tabPanel;
    private final BottomBarPanel bottomBar;
    private final JProgressBar progressBar;
    private final Launcher launcher;
    private final JPanel loginPanel;
    private net.minecraft.launcher.ui.BT close;
    private net.minecraft.launcher.ui.BT options;
    private net.minecraft.launcher.ui.BT minimize;
    private static final URL minimizeIcon = Launcher.class.getResource("/wdes/fr/ressources/7.png");
    private static final URL optionsIcon = Launcher.class.getResource("/wdes/fr/ressources/2.png");
    private static final URL closeIcon = Launcher.class.getResource("/wdes/fr/ressources/1.png");
    private void setIcon(JLabel label, String iconName, int w, int h)
   {
      try
      {
       label.setIcon(new ImageIcon(Img1.scaleImage(ImageIO.read(Launcher.class.getResourceAsStream( iconName)), w, h)));
     }
      catch (IOException e)
      {
       e.printStackTrace();
      }
   }
    private IDF CreateHome()
    {
    	  this.options = new BT();
	       this.options.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(optionsIcon)));
	       this.options.setBounds(788, 0, 37, 20);
	       this.options.setTransparency(0.7F);
	       this.options.setHoverTransparency(1.0F);
	       this.options.setActionCommand("options");
	       this.options.addActionListener(this);
	       this.options.setBorder(BorderFactory.createEmptyBorder());
	       this.options.setContentAreaFilled(false);
	       
  	       this.minimize = new BT();
  	       this.minimize.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(minimizeIcon)));
  	       this.minimize.setBounds(825, 0, 37, 20);
  	       this.minimize.setTransparency(0.7F);
  	       this.minimize.setHoverTransparency(1.0F);
  	       this.minimize.setActionCommand("minimize");
  	       this.minimize.addActionListener(this);
  	       this.minimize.setBorder(BorderFactory.createEmptyBorder());
  	       this.minimize.setContentAreaFilled(false);
  	  
  	       this.close = new BT();
  	       this.close.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(closeIcon)));
  	       this.close.setBounds(862, 0, 37, 20);
  	       this.close.setTransparency(0.7F);
  	       this.close.setHoverTransparency(1.0F);
  	       this.close.setActionCommand("close");
  	  	   this.close.addActionListener(this);
  	       this.close.setBorder(BorderFactory.createEmptyBorder());
  	       this.close.setContentAreaFilled(false);
  	  
  	       IDF p = new IDF(900, 500);
  	       JLabel logo = new JLabel();
  	       logo.setBounds(100,      10,    700,   109);
  	       //             gauche   hauteur  zoom    grosseur
  	       setIcon(logo, "/wdes/fr/ressources/5.png", logo.getWidth(), logo.getHeight());
  	  p.add(logo);
  	  p.add(close);
  	  p.add(minimize);
  	  p.add(options);
  	return p;
      
    }
    public LauncherPanel(final Launcher launcher) {
        this.launcher = launcher;
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        progressBar = new JProgressBar();
        bottomBar = new BottomBarPanel(launcher);
        tabPanel = new LauncherTabPanel(launcher);
        loginPanel = new TexturedPanel("/dirt.png");
        createInterface();
    }

    protected JPanel createDirtInterface() {
        return new TexturedPanel("/dirt.png");
    }

    protected void createInterface() {
        add(createLauncherInterface(), "launcher");
        add(createDirtInterface(), "loading");
        add(createLoginInterface(), "login");
    }

    protected JPanel createLauncherInterface() {
        final JPanel result = new JPanel(new BorderLayout());

        //tabPanel.getBlog().setPage(LauncherConstants.URL_BLOG);

        final JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BorderLayout());
        //topWrapper.add(tabPanel, "Center");
        topWrapper.add(CreateHome(), "Center");
        
        topWrapper.add(progressBar, "South");

        progressBar.setVisible(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);

        result.add(topWrapper, "Center");
        result.add(bottomBar, "South");

        return result;
    }

    protected JPanel createLoginInterface() {
        loginPanel.setLayout(new GridBagLayout());
        return loginPanel;
    }

    public BottomBarPanel getBottomBar() {
        return bottomBar;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public LauncherTabPanel getTabPanel() {
        return tabPanel;
    }

    public void setCard(final String card, final JPanel additional) {
        if(card.equals("login")) {
            loginPanel.removeAll();
            loginPanel.add(additional);
        }
        cardLayout.show(this, card);
    }
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		String command = arg0.getActionCommand();
		Console.log("On n'a demmandé : "+command);
		if(command == "close"){
			Launcher.getInstance().closeLauncher();
			//System.exit(0);
		}
		if(command == "minimize"){
			Launcher.getInstance().minimizeLauncher();
		}
		
	}
}