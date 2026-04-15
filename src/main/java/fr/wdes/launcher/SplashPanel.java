package fr.wdes.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import fr.wdes.ui.lite.BlurUtils;

/**
 * Replacement for the legacy startup {@code JTextArea} window that flashed
 * white while the launcher was bootstrapping. Paints the bundled
 * {@code background.jpg} with the same Gaussian blur the live launcher uses
 * for its background, draws the product wordmark up top and shows a single
 * rolling status line near the bottom (latest message wins; older lines
 * still go to {@code System.out} for debugging).
 *
 * <p>Deliberately does NOT depend on {@link fr.wdes.Launcher} - this panel
 * exists precisely because the {@code Launcher} singleton hasn't been
 * constructed yet when it's first painted.
 */
@SuppressWarnings("serial")
public class SplashPanel extends JPanel {
    private static final Color OVERLAY        = new Color(0, 0, 0, 110);
    private static final Color STATUS_FG      = new Color(220, 220, 220, 220);
    private static final Color TITLE_FG       = Color.WHITE;
    private static final Color TITLE_SHADOW   = new Color(0, 0, 0, 150);

    private BufferedImage cachedBlurred;
    private int cachedW = -1;
    private int cachedH = -1;
    private volatile String status = "Lancement de votre launcher...";

    private final Font titleFont;
    private final Font statusFont;

    public SplashPanel(int w, int h) {
        setPreferredSize(new java.awt.Dimension(w, h));
        setOpaque(true);
        setBackground(new Color(20, 20, 20));
        Font mc;
        try {
            final InputStream is = SplashPanel.class.getResourceAsStream("/fr/wdes/ressources/minecraft.ttf");
            mc = is == null
                ? new Font(Font.SANS_SERIF, Font.BOLD, 28)
                : Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, 28f);
        } catch (Exception e) {
            mc = new Font(Font.SANS_SERIF, Font.BOLD, 28);
        }
        this.titleFont  = mc;
        this.statusFont = mc.deriveFont(13f);
    }

    /** Update the rolling status line and request a repaint. Thread-safe. */
    public void setStatus(String text) {
        if (text == null) text = "";
        // Don't churn the UI for trailing newlines from the existing
        // print() / println() call sites.
        text = text.replaceAll("\\s+$", "");
        if (text.isEmpty()) {
            return;
        }
        this.status = text;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        final Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Background (cached at the current size to avoid redoing the
            // expensive Gaussian blur on every repaint).
            final BufferedImage bg = ensureBlurredBackground(w, h);
            if (bg != null) {
                g2.drawImage(bg, 0, 0, w, h, null);
            }

            // Dim overlay so text always reads regardless of the underlying
            // background image content.
            g2.setColor(OVERLAY);
            g2.fillRect(0, 0, w, h);

            // Title near the top, mock drop-shadow for legibility.
            g2.setFont(titleFont);
            final FontMetrics tfm = g2.getFontMetrics();
            final String title = "WdesLaunchers";
            final int tw = tfm.stringWidth(title);
            final int tx = (w - tw) / 2;
            final int ty = Math.max(tfm.getAscent() + 40, h / 3);
            g2.setColor(TITLE_SHADOW);
            g2.drawString(title, tx + 1, ty + 2);
            g2.setColor(TITLE_FG);
            g2.drawString(title, tx, ty);

            // Status line, centred near the bottom.
            g2.setFont(statusFont);
            final FontMetrics sfm = g2.getFontMetrics();
            final String s = status;
            final int sw = sfm.stringWidth(s);
            final int sx = (w - sw) / 2;
            final int sy = h - 32;
            g2.setColor(STATUS_FG);
            g2.drawString(s, sx, sy);
        } finally {
            g2.dispose();
        }
    }

    private BufferedImage ensureBlurredBackground(int w, int h) {
        if (cachedBlurred != null && cachedW == w && cachedH == h) {
            return cachedBlurred;
        }
        try {
            final InputStream in = SplashPanel.class.getResourceAsStream("/fr/wdes/ressources/background.jpg");
            if (in == null) {
                return null;
            }
            BufferedImage img;
            try {
                img = ImageIO.read(in);
            } finally {
                try { in.close(); } catch (IOException ignore) { }
            }
            if (img == null) {
                return null;
            }
            // Scale into a TYPE_INT_ARGB destination at the target size first
            // so the blur runs on a manageable buffer and the result aligns
            // pixel-for-pixel with what we'll draw.
            final BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D sg = scaled.createGraphics();
            try {
                sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                sg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                sg.drawImage(img, 0, 0, w, h, null);
            } finally {
                sg.dispose();
            }
            cachedBlurred = BlurUtils.applyGaussianBlur(scaled, 10, 1, true);
            cachedW = w;
            cachedH = h;
            return cachedBlurred;
        } catch (Exception ignored) {
            return null;
        }
    }
}
