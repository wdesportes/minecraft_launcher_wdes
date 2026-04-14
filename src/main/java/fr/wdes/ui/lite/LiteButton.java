package fr.wdes.ui.lite;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

/**
 * Flat-style button matching the look of {@link LiteCheckBox} and the
 * placeholder fields: a slightly rounded translucent dark fill with a thin
 * white outline that brightens on hover and dims on press. Text is rendered
 * with antialiasing in the foreground colour (white by default) so it stays
 * crisp.
 */
public class LiteButton extends JButton implements MouseListener {
	private static final long serialVersionUID = 1L;
	private static final Color FILL         = new Color(0, 0, 0, 150);
	private static final Color FILL_HOVER   = new Color(0, 0, 0, 190);
	private static final Color FILL_PRESS   = new Color(0, 0, 0, 220);
	private static final Color OUTLINE      = new Color(255, 255, 255, 110);
	private static final Color OUTLINE_HOVER = new Color(255, 255, 255, 210);
	private static final Color DISABLED_FG  = new Color(220, 220, 220, 110);
	private static final int   ARC          = 3;

	private boolean clicked = false;
	private boolean hover = false;

	public LiteButton(String label) {
		super(label);
		setForeground(Color.WHITE);
		setBackground(FILL);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusPainted(false);
		setBorder(null);
		addMouseListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			final boolean enabled = isEnabled();
			final Composite previous = g2.getComposite();
			if (!enabled) {
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
			}

			// Fill
			final Color fill = !enabled ? FILL : (clicked ? FILL_PRESS : (hover ? FILL_HOVER : FILL));
			g2.setColor(fill);
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

			// Outline
			g2.setStroke(new BasicStroke(1f));
			g2.setColor(hover && enabled ? OUTLINE_HOVER : OUTLINE);
			g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

			// Text
			g2.setComposite(previous);
			g2.setFont(getFont());
			final FontMetrics fm = g2.getFontMetrics();
			final String text = getText() == null ? "" : getText();
			final int textW = fm.stringWidth(text);
			final int x = (getWidth() - textW) / 2;
			final int baseline = (getHeight() - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
			g2.setColor(enabled ? getForeground() : DISABLED_FG);
			g2.drawString(text, x, baseline);
		} finally {
			g2.dispose();
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		clicked = true;
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		clicked = false;
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
		hover = true;
		repaint();
	}

	public void mouseExited(MouseEvent e) {
		hover = false;
		clicked = false;
		repaint();
	}
}
