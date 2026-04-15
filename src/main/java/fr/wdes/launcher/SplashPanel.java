package fr.wdes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fr.wdes.ui.lite.BlurUtils;
import fr.wdes.ui.lite.LogoLabel;

/**
 * Replacement for the legacy startup {@code JTextArea} window that flashed
 * white while the launcher was bootstrapping. Paints the bundled
 * {@code background.jpg} with the same Gaussian blur the live launcher uses
 * for its background, draws the product wordmark up top via {@link LogoLabel}
 * and shows a single rolling status line near the bottom (latest message
 * wins; older lines still go to {@code System.out} for debugging).
 *
 * <p>Includes its own minimize / close buttons because the splash window is
 * undecorated - without them there's no native title bar to click. Both
 * buttons act on the splash's containing {@link Window}.
 *
 * <p>Deliberately does NOT depend on {@link fr.wdes.Launcher} - this panel
 * exists precisely because the {@code Launcher} singleton hasn't been
 * constructed yet when it's first painted.
 */
@SuppressWarnings("serial")
public class SplashPanel extends JPanel {
    private static final Color OVERLAY        = new Color(0, 0, 0, 110);
    private static final Color STATUS_FG      = new Color(220, 220, 220, 220);
    private static final Color BTN_FG         = new Color(220, 220, 220, 200);
    private static final Color BTN_FG_HOVER   = Color.WHITE;
    private static final Color CLOSE_FG_HOVER = new Color(255, 90, 90, 255);

    private BufferedImage cachedBlurred;
    private int cachedW = -1;
    private int cachedH = -1;
    private volatile String status = "Lancement de votre launcher...";

    private final Font statusFont;
    private final LogoLabel title;

    public SplashPanel(int w, int h) {
        setLayout(new BorderLayout());
        setPreferredSize(new java.awt.Dimension(w, h));
        setOpaque(true);
        setBackground(new Color(20, 20, 20));

        title = new LogoLabel("WdesLaunchers", 28f);
        // Reserve roughly the top third of the splash for the title.
        title.setBorder(BorderFactory.createEmptyBorder(60, 0, 0, 0));
        add(title, BorderLayout.NORTH);

        // Window controls float in the top-right. They stay above the title
        // because they're added LAST in z-order and BoxLayout doesn't paint
        // children over each other - we use absolute bounds via setBounds
        // inside componentResized.
        final JPanel windowControls = buildWindowControls();
        add(windowControls, BorderLayout.PAGE_START); // gets reset below
        setLayout(null);
        title.setBounds(0, 0, w, h);
        windowControls.setBounds(w - windowControls.getPreferredSize().width - 8, 6,
                windowControls.getPreferredSize().width, windowControls.getPreferredSize().height);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                final int W = getWidth();
                final int H = getHeight();
                title.setBounds(0, 0, W, H);
                windowControls.setBounds(W - windowControls.getPreferredSize().width - 8, 6,
                        windowControls.getPreferredSize().width, windowControls.getPreferredSize().height);
            }
        });

        statusFont = title.getFont().deriveFont(13f);
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

            final BufferedImage bg = ensureBlurredBackground(w, h);
            if (bg != null) {
                g2.drawImage(bg, 0, 0, w, h, null);
            }

            // Dim overlay so text always reads regardless of the underlying
            // background image content.
            g2.setColor(OVERLAY);
            g2.fillRect(0, 0, w, h);

            // Status line, centred near the bottom. Title is now a child
            // LogoLabel so we don't draw it here.
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

    private JPanel buildWindowControls() {
        final JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));

        final JButton minimize = chromeButton("\u2013"); // en dash
        minimize.setToolTipText("Réduire");
        minimize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Window w = SwingUtilities.getWindowAncestor(SplashPanel.this);
                if (w instanceof JFrame) {
                    ((JFrame) w).setExtendedState(JFrame.ICONIFIED);
                }
            }
        });

        final JButton close = chromeButton("\u2715"); // multiplication sign
        close.setToolTipText("Quitter");
        close.putClientProperty("isClose", Boolean.TRUE);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        row.add(minimize);
        row.add(close);
        return row;
    }

    private static JButton chromeButton(final String glyph) {
        final JButton b = new JButton(glyph) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                final Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    final boolean hover = getModel().isRollover() || getModel().isPressed();
                    final boolean isClose = Boolean.TRUE.equals(getClientProperty("isClose"));
                    if (hover) {
                        g2.setColor(new Color(255, 255, 255, 30));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    }
                    g2.setColor(hover && isClose ? CLOSE_FG_HOVER : (hover ? BTN_FG_HOVER : BTN_FG));
                    g2.setFont(getFont());
                    final FontMetrics fm = g2.getFontMetrics();
                    final int tw = fm.stringWidth(getText());
                    final int x = (getWidth() - tw) / 2;
                    final int baseline = (getHeight() - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
                    g2.drawString(getText(), x, baseline);
                } finally {
                    g2.dispose();
                }
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setForeground(BTN_FG);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 14f));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setPreferredSize(new java.awt.Dimension(28, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setRolloverEnabled(true);
        return b;
    }
}
