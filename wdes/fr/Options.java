package wdes.fr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import wdes.fr.launch.Wdes;



public class Options extends JDialog
{
  private static final long serialVersionUID = 1L;

  public Options(Frame parent)
  {
	  
    super(parent);

    setModal(true);

    JPanel panel = new JPanel(new BorderLayout());
    JLabel label = new JLabel("Options du Launcher", 0);
    label.setBorder(new EmptyBorder(0, 0, 16, 0));
    label.setFont(new Font("Default", 1, 16));
    panel.add(label, "North");

    JPanel optionsPanel = new JPanel(new BorderLayout());
    JPanel labelPanel = new JPanel(new GridLayout(0, 1));
    JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
    optionsPanel.add(labelPanel, "West");
    optionsPanel.add(fieldPanel, "Center");
    final JButton forceButton = new JButton("Forcer la mise a jour!");
    forceButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			Mise_a_jour.forceUpdate = 1;
			forceButton.setText("Ce sera forcé!");
			forceButton.setEnabled(false);
		}
	});
    if(Mise_a_jour.forceUpdate == 1){
		forceButton.setText("Ce sera forcé!");
		forceButton.setEnabled(false);
    }
    labelPanel.add(new JLabel("Forcer la mise a jour : ", 4));
    fieldPanel.add(forceButton);
    
    final JButton noclose = new JButton("Empécher !");
    noclose.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			Wdes.noclose = 1;
			noclose.setText("Il ne se fermera pas !");
			noclose.setEnabled(false);
		}
	});
    if(Wdes.noclose == 1){
		noclose.setText("Il ne se fermera pas !");
		noclose.setEnabled(false);
    }
    labelPanel.add(new JLabel("Empécher la fermeture : ", 4));
    fieldPanel.add(noclose);
    final JButton keeplog = new JButton("Garder !");
    keeplog.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			Wdes.keeplog = 1;
			keeplog.setText("Il serra gardé !");
			keeplog.setEnabled(false);
		}
	});
    if(Wdes.keeplog == 1){
		keeplog.setText("Il serra gardé !");
		keeplog.setEnabled(false);
    }
    labelPanel.add(new JLabel("Garder le log : ", 4));
    fieldPanel.add(keeplog);
    
    labelPanel.add(new JLabel("Appdata du jeu: ", 4));
    Label dirLink = new Label(Util.getWorkingDirectory().toString()) {
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
    dirLink.setCursor(Cursor.getPredefinedCursor(12));
    dirLink.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent arg0) {
        try {
          Util.openLink(new URL("file://" + Util.getWorkingDirectory().getAbsolutePath()).toURI());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    dirLink.setForeground(new Color(2105599));

    fieldPanel.add(dirLink);

    panel.add(optionsPanel, "Center");

    JPanel buttonsPanel = new JPanel(new BorderLayout());
    buttonsPanel.add(new JPanel(), "Center");
    JButton doneButton = new JButton("Done");
    doneButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Options.this.setVisible(false);
      }
    });
    buttonsPanel.add(doneButton, "East");
    buttonsPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

    panel.add(buttonsPanel, "South");

    add(panel);
    panel.setBorder(new EmptyBorder(16, 24, 24, 24));
    pack();
    setLocationRelativeTo(parent);
  }
}