package fr.wdes.ui.lite;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JCheckBox;

/**
 * A {@link JCheckBox} with a hand-painted, semi-translucent box and tick mark
 * that fits the launcher's overlay aesthetic. The native L&F checkbox is hidden
 * behind a custom {@link Icon} so the look stays consistent across Windows,
 * macOS and Linux.
 */
public class LiteCheckBox extends JCheckBox {
	private static final long serialVersionUID = 1L;
	private static final int BOX_SIZE = 16;

	public LiteCheckBox(String text) {
		super(text);
		setOpaque(false);
		setContentAreaFilled(false);
		setFocusPainted(false);
		setBorderPainted(false);
		setForeground(Color.WHITE);
		setIcon(new BoxIcon(false));
		setSelectedIcon(new BoxIcon(true));
		setRolloverIcon(new BoxIcon(false));
		setRolloverSelectedIcon(new BoxIcon(true));
		setPressedIcon(new BoxIcon(isSelected()));
		setIconTextGap(8);
	}

	private static final class BoxIcon implements Icon {
		private final boolean checked;

		BoxIcon(boolean checked) {
			this.checked = checked;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			final Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				final boolean rollover = c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover();
				final Composite previous = g2.getComposite();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rollover ? 0.85f : 0.7f));
				g2.setColor(new Color(0, 0, 0, 200));
				g2.fillRoundRect(x, y, BOX_SIZE - 1, BOX_SIZE - 1, 5, 5);
				g2.setComposite(previous);

				g2.setStroke(new BasicStroke(1.4f));
				g2.setColor(rollover ? Color.WHITE : new Color(255, 255, 255, 200));
				g2.drawRoundRect(x, y, BOX_SIZE - 1, BOX_SIZE - 1, 5, 5);

				if (checked) {
					g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g2.setColor(Color.WHITE);
					final int x1 = x + 3;
					final int x2 = x + 6;
					final int x3 = x + BOX_SIZE - 3;
					final int y1 = y + BOX_SIZE / 2;
					final int y2 = y + BOX_SIZE - 4;
					final int y3 = y + 4;
					g2.drawLine(x1, y1, x2, y2);
					g2.drawLine(x2, y2, x3, y3);
				}
			} finally {
				g2.dispose();
			}
		}

		public int getIconWidth() {
			return BOX_SIZE;
		}

		public int getIconHeight() {
			return BOX_SIZE;
		}
	}
}
