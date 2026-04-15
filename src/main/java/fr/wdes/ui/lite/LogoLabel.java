package fr.wdes.ui.lite;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Pure-text wordmark for the launcher: paints {@link #getText()} centred in
 * the bundled Minecraft TTF with a soft black drop shadow for contrast over
 * arbitrary backgrounds. Used both on the boot splash and on the main
 * launcher page so the look is identical.
 *
 * <p>Deliberately self-contained - loads the font directly via
 * {@code getResourceAsStream} so it works during boot before
 * {@code Launcher.getInstance()} exists.
 */
public class LogoLabel extends JLabel {
    private static final long serialVersionUID = 1L;

    private static final Color SHADOW = new Color(0, 0, 0, 170);
    private static Font cachedFont;

    private float fontSize;

    public LogoLabel(String text, float fontSize) {
        super(text);
        this.fontSize = fontSize;
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setForeground(Color.WHITE);
        setFont(loadFont(fontSize));
    }

    /** Re-derive the bundled font at a new size. */
    public void setFontSize(float size) {
        this.fontSize = size;
        setFont(loadFont(size));
        repaint();
    }

    public float getFontSize() {
        return fontSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Skip super.paintComponent entirely - JLabel's own painter would
        // draw the text without our shadow, then we'd paint over it. Just
        // own the whole rendering.
        final String text = getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        final Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            final FontMetrics fm = g2.getFontMetrics();
            final int textW = fm.stringWidth(text);
            final int x = (getWidth() - textW) / 2;
            // Centre the text vertically using ascent + descent so the
            // baseline lands on the optical mid-line of the component.
            final int baseline = (getHeight() - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
            g2.setColor(SHADOW);
            g2.drawString(text, x + 1, baseline + 2);
            g2.setColor(getForeground());
            g2.drawString(text, x, baseline);
        } finally {
            g2.dispose();
        }
    }

    private static synchronized Font loadFont(float size) {
        if (cachedFont == null) {
            try {
                final InputStream is = LogoLabel.class.getResourceAsStream("/fr/wdes/ressources/minecraft.ttf");
                if (is != null) {
                    cachedFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, 12f);
                } else {
                    cachedFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
                }
            } catch (FontFormatException e) {
                cachedFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
            } catch (IOException e) {
                cachedFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
            }
        }
        return cachedFont.deriveFont(size);
    }
}
