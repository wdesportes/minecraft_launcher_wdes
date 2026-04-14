package fr.wdes.ui.lite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import fr.wdes.ui.SettingsTheme;

/**
 * Combo box styled to match {@link LiteCheckBox} / {@link LiteTextField}: dark
 * translucent body, white outline, antialiased white text and a small custom
 * caret button. Native L&F is replaced with a {@link BasicComboBoxUI} subclass
 * so the look is identical across Windows / macOS / Linux.
 */
public class LiteComboBox<E> extends JComboBox<E> {
	private static final long serialVersionUID = 1L;

	public LiteComboBox() {
		init();
	}

	private void init() {
		setOpaque(false);
		setForeground(SettingsTheme.FG);
		setBackground(SettingsTheme.FIELD_BG);
		setFont(SettingsTheme.font(12f));
		setBorder(BorderFactory.createCompoundBorder(SettingsTheme.fieldOutline(false), new javax.swing.border.EmptyBorder(2, 6, 2, 2)));
		setFocusable(false);
		setUI(new LiteComboUI());
		setRenderer(new LiteRenderer());
	}

	@Override
	protected void paintComponent(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getBackground());
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 3, 3);
		} finally {
			g2.dispose();
		}
		super.paintComponent(g);
	}

	private static final class LiteRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setOpaque(true);
			setForeground(SettingsTheme.FG);
			setBackground(isSelected ? new Color(255, 255, 255, 35) : new Color(28, 28, 30, 240));
			setFont(SettingsTheme.font(12f));
			setBorder(new javax.swing.border.EmptyBorder(4, 8, 4, 8));
			return this;
		}
	}

	/** Minimal UI override: custom arrow button, dark popup body. */
	private static final class LiteComboUI extends BasicComboBoxUI {
		@Override
		protected JButton createArrowButton() {
			final BasicArrowButton btn = new BasicArrowButton(BasicArrowButton.SOUTH,
					new Color(0, 0, 0, 0),
					new Color(0, 0, 0, 0),
					SettingsTheme.MUTED,
					new Color(0, 0, 0, 0)) {
				private static final long serialVersionUID = 1L;
				@Override
				public void paint(Graphics g) {
					final Graphics2D g2 = (Graphics2D) g.create();
					try {
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setColor(getModel().isRollover() || getModel().isPressed() ? SettingsTheme.FG : SettingsTheme.MUTED);
						g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
						final int cx = getWidth() / 2;
						final int cy = getHeight() / 2;
						g2.drawLine(cx - 4, cy - 1, cx, cy + 3);
						g2.drawLine(cx, cy + 3, cx + 4, cy - 1);
					} finally {
						g2.dispose();
					}
				}
			};
			btn.setBorder(BorderFactory.createEmptyBorder());
			btn.setContentAreaFilled(false);
			btn.setOpaque(false);
			btn.setPreferredSize(new Dimension(18, 18));
			return btn;
		}

		@Override
		public void paint(Graphics g, JComponent c) {
			// Skip the L&F's rectangular fill - the combo paints its own
			// rounded background in paintComponent. We still need super to
			// draw the current value.
			super.paint(g, c);
		}

		@Override
		protected ComboPopup createPopup() {
			final BasicComboPopup popup = new BasicComboPopup(comboBox) {
				private static final long serialVersionUID = 1L;
				@Override
				protected JScrollPane createScroller() {
					final JScrollPane sp = super.createScroller();
					sp.setBorder(BorderFactory.createEmptyBorder());
					sp.getViewport().setOpaque(true);
					sp.getViewport().setBackground(new Color(28, 28, 30, 240));
					return sp;
				}
			};
			popup.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1));
			popup.setBackground(new Color(28, 28, 30, 240));
			return popup;
		}
	}

	static {
		// Force a dark popup-list selection background even if the platform
		// L&F has installed something obnoxious before our renderer runs.
		UIManager.put("ComboBox.selectionBackground", new Color(255, 255, 255, 35));
		UIManager.put("ComboBox.selectionForeground", SettingsTheme.FG);
	}
}
