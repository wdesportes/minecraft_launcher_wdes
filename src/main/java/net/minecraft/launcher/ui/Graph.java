package net.minecraft.launcher.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;

import net.minecraft.launcher.Launcher;






public class Graph {

}
class BT
extends JButton
implements Transparent
{

private static final long serialVersionUID = 1L;
private final T2 transparency = new T2(this);

public BT()
{
  setBorder(null);
  setRolloverEnabled(true);
  setFocusable(false);
 setContentAreaFilled(false);
  setOpaque(false);
}

public void setIcon(Icon icon)
{
 super.setIcon(icon);
  setRolloverIcon(getIcon());
  setSelectedIcon(getIcon());
 setDisabledIcon(getIcon());
  setPressedIcon(getIcon());
}

public void paint(Graphics g)
{
  g = this.transparency.setup(g);
 super.paint(g);
this.transparency.cleanup(g);
}

public float getTransparency()
{
 return this.transparency.getTransparency();
}

public void setTransparency(float t)
{
  this.transparency.setTransparency(t);
}

public float getHoverTransparency()
{
 return this.transparency.getHoverTransparency();
}

public void setHoverTransparency(float t)
{
  this.transparency.setHoverTransparency(t);
}
}
class B2
extends AbstractBorder
{
private static final long serialVersionUID = 1L;
private final int thickness;
private final Color color;

public B2(int thick, Color color)
{
  this.thickness = thick;
 this.color = color;
}
public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
{
  Graphics2D g2d = (Graphics2D)g;
 g2d.setColor(this.color);
 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  g2d.drawRect(x, y, width, height);
}

public Insets getBorderInsets(Component c)
{
  return new Insets(this.thickness, this.thickness, this.thickness, this.thickness);
}
public Insets getBorderInsets(Component c, Insets insets)
{
  insets.left = (insets.top = insets.right = insets.bottom = this.thickness);
  return insets;
}
}
class IDF
extends JLabel
{
private static final long serialVersionUID = 1L;

public IDF(int width, int height)
{

  setVerticalAlignment(0);
 setHorizontalAlignment(0);
 setBounds(0, 0, width, height);

  setIcon(new ImageIcon(fond().getScaledInstance(width, height, 4)));
  setVerticalAlignment(1);
  setHorizontalAlignment(2);

}

private BufferedImage fond()
{
 List<File> images = new ArrayList<File>();
  File backgroundDir = new File(new File(fr.wdes.launchers.Util.getWorkingDirectory(), "fonds"), heure());
  if (backgroundDir.exists()) {
   for (File f : backgroundDir.listFiles()) {
     if ((f.getName().endsWith(".png")) || (f.getName().endsWith(".jpg"))) {
       images.add(f);
      }
    }
  }
 InputStream stream = null;
 try
 {
    try
   {
      stream = new FileInputStream((File)images.get(new Random().nextInt(images.size())));
    }
   catch (Exception io)
   {
     if (images.size() > 0) {
       io.printStackTrace();
      }

      stream = Launcher.class.getResourceAsStream("/wdes/fr/ressources/4.jpg");
      
    }
    BufferedImage image = ImageIO.read(stream);
    image = Blurd.applyGaussianBlur(image, 10, 1.0F, true);

   return image;
  }
  catch (Exception e)
  {
    
 }
 finally
  {
   try
   {
      stream.close();
    }
    catch (IOException e)
   {
      
    }
  }
  return new BufferedImage(getWidth(), getHeight(), 2);
}

private String heure()
{
	  
  int h = Calendar.getInstance().get(11);
  int m = Calendar.getInstance().get(12);
  Console.log("Il est : "+h +" heures et "+m+" minutes.");
  if (h < 6) {
    Console.log("Mode choisi : nuit.");
    return "nuit";
  }
  if (h < 12) {
 Console.log("Mode choisi : jour.");
   return "jour";
 }
 if (h > 20) {
 	Console.log("Mode choisi : soirée.");
    return "soiree";
  }
  return "";
}

}
class Img1
{
   
   public static BufferedImage scaleImage(BufferedImage img, int width, int height)
    {
   int imgWidth = img.getWidth();
   int imgHeight = img.getHeight();
    if (imgWidth * height < imgHeight * width) {
      width = imgWidth * height / imgHeight;
    } else {
     height = imgHeight * width / imgWidth;
   }
  BufferedImage newImage = new BufferedImage(width, height, 2);
 Graphics2D g = newImage.createGraphics();
     try
    {
   g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.drawImage(img, 0, 0, width, height, null);
     }
     finally
     {
     g.dispose();
       }
   return newImage;
 }

  public static JButton drawCropped(JPanel contentPane, ActionListener listener, BufferedImage img, int type, int sx1, int sy1, int sx2, int sy2, int x, int y, int scale)
    {
  return drawCropped(contentPane, listener, img, type, sx1, sy1, sx2, sy2, x, y, scale, false);
  }
   
   public static JButton drawCropped(JPanel contentPane, ActionListener listener, BufferedImage img, int type, int sx1, int sy1, int sx2, int sy2, int x, int y, int scale, boolean reflect)
   {
  BufferedImage resizedImage = new BufferedImage((sx2 - sx1) * scale, (sy2 - sy1) * scale, type);
  Graphics2D g = resizedImage.createGraphics();
  int asx2 = sx2;int asx1 = sx1;
 if (reflect)
  {
   asx2 = sx1;
    asx1 = sx2;
     }
  g.drawImage(img, 0, 0, (sx2 - sx1) * scale, (sy2 - sy1) * scale, asx1, sy1, asx2, sy2, null);
  g.dispose();
    
  JButton tmp = new JButton(new ImageIcon(resizedImage));
  tmp.setRolloverEnabled(true);
  tmp.setRolloverIcon(tmp.getIcon());
  tmp.setSelectedIcon(tmp.getIcon());
  tmp.setDisabledIcon(tmp.getIcon());
  tmp.setPressedIcon(tmp.getIcon());
  tmp.setFocusable(false);
  tmp.setContentAreaFilled(false);
  tmp.setBorderPainted(false);
     
   tmp.setBounds(x, y, (sx2 - sx1) * scale, (sy2 - sy1) * scale);
  if (listener != null) {
    tmp.addActionListener(listener);
      }
   contentPane.add(tmp);
  return tmp;
   }
   


}

class Wool extends JPanel
{
  private static final long serialVersionUID = 1L;
  private Image img;
  private Image bgImage;

  public Wool()
  {
    
    setOpaque(true);
    try
    {
      this.bgImage = ImageIO.read(Launcher.class.getResourceAsStream("6.png")).getScaledInstance(32, 32, 16);
      
    } catch (IOException e) {
      
    }
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paintComponent(Graphics g2) {
    int w = getWidth() / 2 + 1;
    int h = getHeight() / 2 + 1;
    if ((this.img == null) || (this.img.getWidth(null) != w) || (this.img.getHeight(null) != h)) {
      this.img = createImage(w, h);

      Graphics g = this.img.getGraphics();
      for (int x = 0; x <= w / 32; x++) {
        for (int y = 0; y <= h / 32; y++)
          g.drawImage(this.bgImage, x * 32, y * 32, null);
      }
      if ((g instanceof Graphics2D)) {
        Graphics2D gg = (Graphics2D)g;
        int gh = 1;
        gg.setPaint(new GradientPaint(new Point2D.Float(0.0F, 0.0F), new Color(553648127, true), new Point2D.Float(0.0F, gh), new Color(0, true)));
        gg.fillRect(0, 0, w, gh);

        gh = h;
        gg.setPaint(new GradientPaint(new Point2D.Float(0.0F, 0.0F), new Color(0, true), new Point2D.Float(0.0F, gh), new Color(1610612736, true)));
        gg.fillRect(0, 0, w, gh);
      }
      g.dispose();
    }
    g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
  }
 
}

class Label extends JLabel
{
  private static final long serialVersionUID = 1L;

  public Label(String string, int center)
  {
    super(string, center);
    setForeground(Color.WHITE);
  }

  public Label(String string) {
    super(string);
    setForeground(Color.WHITE);
    
  }

  public boolean isOpaque() {
    return false;
  }
}
class Blurd
{
  private static SoftReference<BufferedImage> _buffer0;
  private static SoftReference<BufferedImage> _buffer1;
  
  public static BufferedImage applyGaussianBlur(BufferedImage image, int filterRadius, float alphaFactor, boolean useOriginalImageAsDestination)
 {
    if (filterRadius < 1) {
      throw new IllegalArgumentException("doit être >= 1, il est  " + filterRadius);
   }
    float[] kernel = new float[2 * filterRadius + 1];
    
   float sigma = filterRadius / 3.0F;
    float alpha = 2.0F * sigma * sigma;
    float rootAlphaPI = (float)Math.sqrt(alpha * 3.141592653589793D);
   float sum = 0.0F;
   for (int i = 0; i < kernel.length; i++)
   {
     int d = -((i - filterRadius) * (i - filterRadius));
     kernel[i] = ((float)(Math.exp(d / alpha) / rootAlphaPI));
     sum += kernel[i];
    }
    for (int i = 0; i < kernel.length; i++)
    {
      kernel[i] /= sum;
      kernel[i] *= alphaFactor;
    }
    Kernel horizontalKernel = new Kernel(kernel.length, 1, kernel);
   Kernel verticalKernel = new Kernel(1, kernel.length, kernel);
   synchronized (Blurd.class)
  {
      int blurredWidth = useOriginalImageAsDestination ? image.getWidth() : image.getWidth() + 4 * filterRadius;
     int blurredHeight = useOriginalImageAsDestination ? image.getHeight() : image.getHeight() + 4 * filterRadius;
      
    BufferedImage img0 = ensureBuffer0Capacity(blurredWidth, blurredHeight);
      Graphics2D graphics0 = img0.createGraphics();
      graphics0.drawImage(image, null, useOriginalImageAsDestination ? 0 : 2 * filterRadius, useOriginalImageAsDestination ? 0 : 2 * filterRadius);
      graphics0.dispose();
     
      BufferedImage img1 = ensureBuffer1Capacity(blurredWidth, blurredHeight);
      Graphics2D graphics1 = img1.createGraphics();
      graphics1.drawImage(img0, new ConvolveOp(horizontalKernel, 1, null), 0, 0);
      graphics1.dispose();
     
      BufferedImage destination = useOriginalImageAsDestination ? image : new BufferedImage(blurredWidth, blurredHeight, 2);
      Graphics2D destGraphics = destination.createGraphics();
      destGraphics.drawImage(img1, new ConvolveOp(verticalKernel, 1, null), 0, 0);
      destGraphics.dispose();
      
      return destination;
    }
  }
  
  private static BufferedImage ensureBuffer0Capacity(int width, int height)
  {
    BufferedImage img0 = _buffer0 != null ? (BufferedImage)_buffer0.get() : null;
    img0 = ensureBufferCapacity(width, height, img0);
   _buffer0 = new SoftReference<BufferedImage>(img0);
    return img0;
  }
 
 private static BufferedImage ensureBuffer1Capacity(int width, int height)
 {
   BufferedImage img1 = _buffer1 != null ? (BufferedImage)_buffer0.get() : null;
   img1 = ensureBufferCapacity(width, height, img1);
   _buffer1 = new SoftReference<BufferedImage>(img1);
    return img1;
  }
  
  private static BufferedImage ensureBufferCapacity(int width, int height, BufferedImage img)
  {
    if ((img == null) || (img.getWidth() < width) || (img.getHeight() < height))
   {
      img = new BufferedImage(width, height, 2);
 }
    else
    {
     Graphics2D g2 = img.createGraphics();
      g2.setComposite(AlphaComposite.Clear);
     g2.fillRect(0, 0, width, height);
      g2.dispose();
   }
   return img;
  }
}


class Hover
extends DefaultButtonModel
 {
private static final long serialVersionUID = 1L;
private final List<JButton> buttons;
private boolean previous = false;

public Hover(List<JButton> buttons)
{
this.buttons = buttons;
}

public boolean isRollover()
{
boolean current = isRolloverImpl();
if (current != this.previous)
{
 this.previous = current;
 fireStateChanged();
}
return current;
}

public boolean isRolloverImpl()
{
for (JButton button : this.buttons) {
 if (button.getModel().isRollover()) {
return true;
 }
}
return false;
}
 }

abstract interface Transparent
{
  public abstract float getTransparency();
  
  public abstract void setTransparency(float paramFloat);
  
  public abstract float getHoverTransparency();
  
  public abstract void setHoverTransparency(float paramFloat);
}
class T2
implements MouseListener
{
private final JComponent parent;
private float transparency = 1.0F;
private float hoverTransparency = 1.0F;
private boolean hovering = false;
private final boolean repaint;

public T2(JComponent component)
{
  this.parent = component;
  this.parent.addMouseListener(this);
 this.repaint = true;
}

public T2(JComponent component, boolean repaint)
{
 this.parent = component;
 this.parent.addMouseListener(this);
  this.repaint = repaint;
}

public float getTransparency()
{
 return this.transparency;
}

public void setTransparency(float t)
{
  if ((t > 1.0F) || (t < 0.0F)) {
    throw new IllegalArgumentException("erreur de valeur");
  }
  this.transparency = t;
}

public float getHoverTransparency()
{
  return this.hoverTransparency;
}

public void setHoverTransparency(float t)
{
  if ((t > 1.0F) || (t < 0.0F)) {
    throw new IllegalArgumentException("erreur de valeur");
  }
  this.hoverTransparency = t;
}

public Graphics setup(Graphics g)
{
 float t;
 
  if (this.hovering) {
    t = getHoverTransparency();
 } else {
  t = getTransparency();
  }
 Graphics2D copy = (Graphics2D)g.create();
  copy.setComposite(AlphaComposite.getInstance(3, t));
 return copy;
}

public Graphics cleanup(Graphics g)
{
  g.dispose();
  return g;
}

public void mouseClicked(MouseEvent e) {}

public void mousePressed(MouseEvent e) {}

public void mouseReleased(MouseEvent e) {}

public void mouseEntered(MouseEvent e)
{
  if (e.getComponent() == this.parent)
  {
   this.hovering = true;
    if (this.repaint) {
      this.parent.repaint();
   }
  }
}

public void mouseExited(MouseEvent e)
{
  if (e.getComponent() == this.parent)
  {
    this.hovering = false;
    if (this.repaint) {
      this.parent.repaint();
    }
  }
}
}
class T3 extends JPanel
{
  private static final long serialVersionUID = 1L;
  private Insets insets;

  public T3()
  {
  }

  public T3(LayoutManager layout)
  {
    setLayout(layout);
  }

  public boolean isOpaque() {
    return false;
  }

  public void setInsets(int a, int b, int c, int d) {
    this.insets = new Insets(a, b, c, d);
  }

  public Insets getInsets() {
    if (this.insets == null) return super.getInsets();
    return this.insets;
  }
}
class Boutton
extends JButton
 implements MouseListener
{
private static final long serialVersionUID = 1L;
 private boolean clicked = false;

 public Boutton(String label)
 {
   setText(label);
   setBackground(new Color(220, 220, 220));
   setBorder(new B2(5, getBackground()));
   addMouseListener(this);
}

 public void paint(Graphics g)
 {
   Graphics2D g2d = (Graphics2D)g;
   Color old = g2d.getColor();
  
   g2d.setColor(this.clicked ? Color.BLACK : getBackground());
   g2d.fillRect(0, 0, getWidth(), getHeight());
  
   g2d.setColor(this.clicked ? getBackground() : Color.BLACK);
   g2d.setFont(getFont());
   int width = g2d.getFontMetrics().stringWidth(getText());
   g2d.drawString(getText(), (getWidth() - width) / 2, getFont().getSize() + 4);
   
   g2d.setColor(old);
 }
 
 public void mouseClicked(MouseEvent e) {}
 
 public void mousePressed(MouseEvent e)
 {
  this.clicked = true;
 }

 public void mouseReleased(MouseEvent e)
 {
   this.clicked = false;
 }
 
public void mouseEntered(MouseEvent e) {}
 
 public void mouseExited(MouseEvent e) {}
}

class Checkbox extends JCheckBox
{
  private static final long serialVersionUID = 1L;

  public Checkbox(String string)
  {
    super(string);
    setForeground(Color.WHITE);
  }

  public boolean isOpaque() {
    return false;
  }
}
class Console {
public static void log(String text){
	System.out.println("[Wdes]"+text);
}
}