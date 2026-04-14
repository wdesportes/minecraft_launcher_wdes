package fr.wdes.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import fr.wdes.Launcher;
import fr.wdes.ui.lite.LiteBorder;

/**
 * Shared visual constants and small widget helpers for the popup settings
 * panels (profile editor and friends) so they look like part of the same
 * launcher rather than a stock Swing dialog.
 */
public final class SettingsTheme {
	public static final Color BG            = new Color(28, 28, 30, 245);
	public static final Color SECTION_BG    = new Color(255, 255, 255, 12);
	public static final Color SEPARATOR     = new Color(255, 255, 255, 50);
	public static final Color FG            = Color.WHITE;
	public static final Color MUTED         = new Color(220, 220, 220, 170);
	public static final Color FIELD_BG      = new Color(0, 0, 0, 150);
	public static final Color FIELD_OUTLINE       = new Color(255, 255, 255, 90);
	public static final Color FIELD_OUTLINE_FOCUS = new Color(255, 255, 255, 200);

	private static Font cachedFont;

	private SettingsTheme() {}

	/** Return the bundled minecraft.ttf font at the given size, cached. */
	public static Font font(float size) {
		if (cachedFont == null) {
			InputStream is = Launcher.class.getResourceAsStream("/fr/wdes/ressources/minecraft.ttf");
			try {
				cachedFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, 12f);
			} catch (FontFormatException e) {
				cachedFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
			} catch (IOException e) {
				cachedFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
			}
		}
		return cachedFont.deriveFont(size);
	}

	public static LiteBorder fieldOutline(boolean focused) {
		return new LiteBorder(1, focused ? FIELD_OUTLINE_FOCUS : FIELD_OUTLINE, 3);
	}

	/**
	 * Paint a small section header: bold-ish minecraft text with a hairline
	 * underneath spanning the full width.
	 */
	public static JComponent header(String text) {
		final JPanel wrap = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				final Graphics2D g2 = (Graphics2D) g.create();
				try {
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(SEPARATOR);
					g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
				} finally {
					g2.dispose();
				}
			}
		};
		wrap.setOpaque(false);
		wrap.setLayout(new java.awt.BorderLayout());
		wrap.setBorder(new EmptyBorder(2, 0, 4, 0));

		final JLabel label = new JLabel(text);
		label.setFont(font(13f));
		label.setForeground(FG);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		wrap.add(label, java.awt.BorderLayout.CENTER);

		return wrap;
	}

	/** A solid dark panel suitable as a popup content pane. */
	public static JPanel darkPanel() {
		final JPanel p = new JPanel();
		p.setBackground(BG);
		p.setOpaque(true);
		return p;
	}

	/** Vertical strut for spacing in BoxLayouts. */
	public static Component vGap(int px) {
		return javax.swing.Box.createRigidArea(new Dimension(0, px));
	}

	/** Apply a solid dark-translucent panel background and a rounded inner padding. */
	public static void styleSection(JPanel panel) {
		panel.setOpaque(false);
		panel.setBorder(new EmptyBorder(8, 12, 12, 12));
	}

	public static GridBagConstraints labelConstraints(int row) {
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = row;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new java.awt.Insets(4, 0, 4, 8);
		return c;
	}

	public static GridBagConstraints fieldConstraints(int row) {
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = row;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new java.awt.Insets(4, 0, 4, 0);
		return c;
	}

	/** A label suitable for a settings form (left column). */
	public static JLabel formLabel(String text) {
		final JLabel l = new JLabel(text);
		l.setFont(font(12f));
		l.setForeground(MUTED);
		return l;
	}
}
