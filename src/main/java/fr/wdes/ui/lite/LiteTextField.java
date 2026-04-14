package fr.wdes.ui.lite;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;

import fr.wdes.ui.SettingsTheme;
import fr.wdes.ui.StyledFieldUtils;

/**
 * Plain styled text field matching the launcher's glass aesthetic - same
 * dark translucent fill, white outline, top-edge highlight and antialiased
 * white text as the login fields, but without the placeholder mechanism
 * (use {@link fr.wdes.ui.PlaceholderTextField} when you want a placeholder).
 */
public class LiteTextField extends JTextField {
	private static final long serialVersionUID = 1L;

	public LiteTextField() {
		init();
	}

	public LiteTextField(String text) {
		super(text);
		init();
	}

	public LiteTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		init();
	}

	private void init() {
		setOpaque(false);
		setForeground(Color.WHITE);
		setCaretColor(Color.WHITE);
		setSelectionColor(new Color(255, 255, 255, 80));
		setSelectedTextColor(Color.WHITE);
		setDisabledTextColor(SettingsTheme.MUTED);
		setBackground(SettingsTheme.FIELD_BG);
		setBorder(new CompoundBorder(SettingsTheme.fieldOutline(false), new EmptyBorder(4, 8, 4, 8)));
		setFont(SettingsTheme.font(12f));
	}

	@Override
	protected void paintComponent(Graphics g) {
		// Reuse the input painter so the rounded fill + glass top-edge match
		// the placeholder fields exactly. paintBackground reads getBackground
		// so the dimmed disabled state still works.
		StyledFieldUtils.paintBackground(this, g);
		super.paintComponent(g);
	}
}
