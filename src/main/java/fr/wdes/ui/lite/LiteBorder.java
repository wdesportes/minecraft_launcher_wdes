package fr.wdes.ui.lite;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.border.AbstractBorder;

class LiteBorder extends AbstractBorder {
	private static final long serialVersionUID = 1L;
	private final int thickness;
	private final Color color;
	private final int arc;

	public LiteBorder(int thick, Color color) {
		this(thick, color, 0);
	}

	public LiteBorder(int thick, Color color, int arc) {
		this.thickness = thick;
		this.color = color;
		this.arc = arc;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		final Graphics2D g2d = (Graphics2D) g.create();
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final Stroke previous = g2d.getStroke();
			g2d.setStroke(new BasicStroke(Math.max(1, thickness / 2f)));
			g2d.setColor(color);
			if (arc > 0) {
				g2d.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
			} else {
				g2d.drawRect(x, y, width - 1, height - 1);
			}
			g2d.setStroke(previous);
		} finally {
			g2d.dispose();
		}
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(thickness, thickness, thickness, thickness);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.left = insets.top = insets.right = insets.bottom = thickness;
		return insets;
	}
}