package fr.wdes.ui.lite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
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
		// Dim grey by default - LauncherPanel reuses the same translucent
		// placeholder colour the inputs use so the label sits at the same
		// visual weight as everything around it.
		setForeground(new Color(220, 220, 220, 170));
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

				// Flat translucent fill matching bottomRectangle / Jouer button.
				g2.setColor(new Color(30, 30, 30, rollover ? 220 : 180));
				g2.fillRoundRect(x, y, BOX_SIZE - 1, BOX_SIZE - 1, 3, 3);

				// Subtle hairline outline; rollover only nudges it brighter.
				g2.setStroke(new BasicStroke(1f));
				g2.setColor(new Color(220, 220, 220, rollover ? 130 : 90));
				g2.drawRoundRect(x, y, BOX_SIZE - 1, BOX_SIZE - 1, 3, 3);

				if (checked) {
					// Tick at the same dimmed grey as the "Retenir" label so
					// the box doesn't visually shout next to the inputs.
					g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g2.setColor(new Color(220, 220, 220, 200));
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
