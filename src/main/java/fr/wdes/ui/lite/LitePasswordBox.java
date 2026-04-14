/*
 * This file is part of Spoutcraft Launcher.
 *
 * Copyright (c) 2011 Spout LLC <http://www.spout.org/>
 * Spoutcraft Launcher is licensed under the Spout License Version 1.
 *
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license,
 * including the MIT license.
 */
package fr.wdes.ui.lite;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class LitePasswordBox extends JPasswordField implements FocusListener {
	private static final long serialVersionUID = 1L;
	private static final Color FIELD_BG = new Color(0, 0, 0, 150);
	private static final Color FIELD_BG_FOCUS = new Color(0, 0, 0, 190);
	private static final Color FIELD_BORDER = new Color(255, 255, 255, 90);
	private static final Color FIELD_BORDER_FOCUS = new Color(255, 255, 255, 200);
	private static final Color PLACEHOLDER = new Color(220, 220, 220, 170);
	private static final int ARC = 12;
	private final JLabel label;
	public LitePasswordBox(JFrame parent, String label) {
		this.label = new JLabel(label);
		addFocusListener(this);
		parent.getContentPane().add(this.label);
		setOpaque(false);
		setForeground(Color.WHITE);
		setCaretColor(Color.WHITE);
		setSelectionColor(new Color(255, 255, 255, 80));
		setSelectedTextColor(Color.WHITE);
		setBackground(FIELD_BG);
		setBorder(new CompoundBorder(new LiteBorder(2, FIELD_BORDER, ARC), new EmptyBorder(0, 8, 0, 8)));
		setEchoChar('\u2022');
		this.label.setForeground(PLACEHOLDER);
	}

	@Override
	protected void paintComponent(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getBackground());
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
		} finally {
			g2.dispose();
		}
		super.paintComponent(g);
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (label != null) {
			label.setFont(font);
		}
	}

	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		label.setBounds(x + 5, y + 3, w - 5, h - 5);
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		label.setVisible((text != null && text.length() > 0) ? false : true);
	}

	public void focusGained(FocusEvent e) {
		label.setVisible(false);
		setBackground(FIELD_BG_FOCUS);
		setBorder(new CompoundBorder(new LiteBorder(2, FIELD_BORDER_FOCUS, ARC), new EmptyBorder(0, 8, 0, 8)));
		repaint();
	}

	public void focusLost(FocusEvent e) {
		if (getPassword().length == 0) {
			label.setVisible(true);
		}
		setBackground(FIELD_BG);
		setBorder(new CompoundBorder(new LiteBorder(2, FIELD_BORDER, ARC), new EmptyBorder(0, 8, 0, 8)));
		repaint();
	}
}
