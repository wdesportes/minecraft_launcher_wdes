package fr.wdes.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

/**
 * Shared styling helpers for {@link PlaceholderTextField} and
 * {@link PlaceholderPasswordField}. Both wrap stock {@code JTextField}
 * subclasses so they can't share an intermediate parent class - this utility
 * keeps the look consistent in one place.
 *
 * <p>Painting is fully delegated here (fill, outline and a subtle top-edge
 * highlight that suggests a glass meniscus). The {@code Border} returned by
 * {@link #buildBorder} now only carries the inner padding needed to vertically
 * centre the text - the visible outline is drawn by {@link #paintBackground}
 * so it sits at the same anti-aliased rounded edge as the fill.
 */
public final class StyledFieldUtils {
	public static final Color OUTLINE         = new Color(220, 220, 220, 80);
	public static final Color OUTLINE_FOCUS   = new Color(255, 255, 255, 200);
	public static final Color HIGHLIGHT_TOP   = new Color(255, 255, 255, 70);
	public static final Color HIGHLIGHT_FOCUS = new Color(255, 255, 255, 110);
	public static final Color PLACEHOLDER     = new Color(220, 220, 220, 170);
	public static final int   ARC             = 3;
	public static final int   PAD_X           = 8;
	private static final int OUTLINE_THICKNESS = 1;

	private StyledFieldUtils() {}

	/** Apply the one-off styling that doesn't depend on size/font. */
	public static void applyBaseStyle(JTextComponent c) {
		c.setOpaque(false);
		c.setForeground(Color.WHITE);
		c.setCaretColor(Color.WHITE);
		c.setSelectionColor(new Color(255, 255, 255, 80));
		c.setSelectedTextColor(Color.WHITE);
		c.setDisabledTextColor(PLACEHOLDER);
	}

	/**
	 * Build the inner-padding border. Vertical padding is tuned so the field's
	 * text baseline sits centred for the current font and component height.
	 * Falls back to a sane fixed padding when the component hasn't been laid
	 * out yet.
	 */
	public static Border buildBorder(JTextComponent c, boolean focused) {
		final int padY = computeVerticalPadding(c);
		return new EmptyBorder(padY, PAD_X, padY, PAD_X);
	}

	private static int computeVerticalPadding(JTextComponent c) {
		final int height = c.getHeight();
		if (height <= 0 || c.getFont() == null) {
			return 4;
		}
		final FontMetrics fm = c.getFontMetrics(c.getFont());
		final int textH = fm.getAscent() + fm.getDescent();
		return Math.max(1, (height - textH) / 2);
	}

	/**
	 * Paint the rounded translucent fill, a hair-thin glass highlight along the
	 * top edge and the outline. Call before super.paintComponent so the text
	 * renders on top.
	 */
	public static void paintBackground(JTextComponent c, Graphics pG) {
		final Graphics2D g = (Graphics2D) pG.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final int w = c.getWidth();
			final int h = c.getHeight();
			final boolean focused = c.isFocusOwner();

			// Fill
			g.setColor(c.getBackground());
			g.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

			// Top-edge highlight: a 1px line just inside the outline that fades
			// in slightly stronger when focused. Adds a glassy meniscus to the
			// otherwise flat translucent rectangle.
			g.setColor(focused ? HIGHLIGHT_FOCUS : HIGHLIGHT_TOP);
			g.drawLine(ARC, 1, w - ARC - 1, 1);

			// Outline
			g.setStroke(new BasicStroke(OUTLINE_THICKNESS));
			g.setColor(focused ? OUTLINE_FOCUS : OUTLINE);
			g.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);
		} finally {
			g.dispose();
		}
	}

	/**
	 * Draw the placeholder vertically centred inside the field using the same
	 * left padding as the actual text, so the placeholder lines up with what
	 * the user types.
	 */
	public static void paintPlaceholder(JTextComponent c, Graphics pG, String placeholder) {
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
