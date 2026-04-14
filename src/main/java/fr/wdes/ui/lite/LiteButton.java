package fr.wdes.ui.lite;

import java.awt.AlphaComposite;
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
 * Flat translucent button matching the launcher's bottom bar
 * ({@code Color(30, 30, 30, 180)} fill, no outline, no highlight). Hover and
 * press shift the alpha so the button still feels interactive without
 * breaking the flat look. Crisp antialiased white text, centred.
 */
public class LiteButton extends JButton implements MouseListener {
	private static final long serialVersionUID = 1L;
	private static final Color FILL        = new Color(30, 30, 30, 180);
	private static final Color FILL_HOVER  = new Color(30, 30, 30, 220);
	private static final Color FILL_PRESS  = new Color(15, 15, 15, 230);
	private static final Color DISABLED_FG = new Color(220, 220, 220, 110);
	private static final int   ARC         = 4;

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
			final int w = getWidth();
			final int h = getHeight();

			final Composite previous = g2.getComposite();
			if (!enabled) {
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
			}

			// Single flat fill, same colour family as bottomRectangle.
			g2.setColor(clicked ? FILL_PRESS : (hover ? FILL_HOVER : FILL));
			g2.fillRoundRect(0, 0, w, h, ARC, ARC);

			// Text
			g2.setComposite(previous);
			g2.setFont(getFont());
			final FontMetrics fm = g2.getFontMetrics();
			final String text = getText() == null ? "" : getText();
			final int textW = fm.stringWidth(text);
			final int x = (w - textW) / 2;
			final int baseline = (h - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
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
