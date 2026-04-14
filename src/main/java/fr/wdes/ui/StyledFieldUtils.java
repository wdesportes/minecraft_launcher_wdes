package fr.wdes.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import fr.wdes.ui.lite.LiteBorder;

/**
 * Shared styling helpers for {@link PlaceholderTextField} and
 * {@link PlaceholderPasswordField}. Both wrap stock {@code JTextField}
 * subclasses so they can't share an intermediate parent class - this utility
 * keeps the look consistent in one place.
 */
final class StyledFieldUtils {
	static final Color BORDER         = new Color(255, 255, 255, 110);
	static final Color BORDER_FOCUS   = new Color(255, 255, 255, 210);
	static final Color PLACEHOLDER    = new Color(220, 220, 220, 170);
	static final int   ARC            = 3;
	static final int   PAD_X          = 8;

	private StyledFieldUtils() {}

	/** Apply the one-off styling that doesn't depend on size/font. */
	static void applyBaseStyle(JTextComponent c) {
		c.setOpaque(false);
		c.setForeground(Color.WHITE);
		c.setCaretColor(Color.WHITE);
		c.setSelectionColor(new Color(255, 255, 255, 80));
		c.setSelectedTextColor(Color.WHITE);
		c.setDisabledTextColor(PLACEHOLDER);
	}

	/**
	 * Build a border with the rounded outline and an inner padding tuned so
	 * the field's text baseline sits vertically centred for the current font
	 * and component height. Falls back to a sane fixed padding if the
	 * component hasn't been laid out yet.
	 */
	static Border buildBorder(JTextComponent c, boolean focused) {
		final Color outline = focused ? BORDER_FOCUS : BORDER;
		final int padY = computeVerticalPadding(c);
		return new CompoundBorder(new LiteBorder(2, outline, ARC), new EmptyBorder(padY, PAD_X, padY, PAD_X));
	}

	private static int computeVerticalPadding(JTextComponent c) {
		final int height = c.getHeight();
		if (height <= 0 || c.getFont() == null) {
			return 4;
		}
		final FontMetrics fm = c.getFontMetrics(c.getFont());
		final int textH = fm.getAscent() + fm.getDescent();
		// Subtract the 2x2 outer LiteBorder thickness from the available height
		// so the inner padding is what's left over after the outline.
		final int avail = height - 4;
		return Math.max(1, (avail - textH) / 2);
	}

	/** Fill the rounded translucent background. Call before super.paintComponent. */
	static void paintBackground(JTextComponent c, Graphics pG) {
		final Graphics2D g = (Graphics2D) pG.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(c.getBackground());
			g.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, ARC, ARC);
		} finally {
			g.dispose();
		}
	}

	/**
	 * Draw the placeholder vertically centred inside the field using the same
	 * left padding as the actual text, so the placeholder lines up with what
	 * the user types.
	 */
	static void paintPlaceholder(JTextComponent c, Graphics pG, String placeholder) {
		if (placeholder == null || placeholder.length() == 0) {
			return;
		}
		final Graphics2D g = (Graphics2D) pG.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(c.getDisabledTextColor());
			g.setFont(c.getFont());
			final FontMetrics fm = g.getFontMetrics();
			final Insets insets = c.getInsets();
			final int baseline = (c.getHeight() - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
			g.drawString(placeholder, insets.left, baseline);
		} finally {
			g.dispose();
		}
	}
}
