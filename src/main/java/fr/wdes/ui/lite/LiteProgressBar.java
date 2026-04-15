package fr.wdes.ui.lite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JProgressBar;



public class LiteProgressBar extends JProgressBar implements Transparent {
	private static final long serialVersionUID = 1L;
	private final TransparentComponent transparency = new TransparentComponent(this, false);

	public LiteProgressBar() {
		setFocusable(false);
		setOpaque(true);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) transparency.setup(g);

		g2d.clearRect(0, 0, getWidth(), getHeight());

		// Draw bar
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		// Draw progress
		g2d.setColor(Color.BLUE);
		int x = (int) (getWidth() * getPercentComplete());
		g2d.fillRect(0, 0, x, getHeight());

		transparency.cleanup(g2d);
		g2d = (Graphics2D) g;

		// Snapshot getString() once - download threads call setString()
		// concurrently, so evaluating it more than once in a single paint
		// can return a shorter string the second time and trip
		// StringIndexOutOfBoundsException in the substring() below.
		final String text = getString();
		if (this.isStringPainted() && text != null && text.length() > 0) {
			// Without these hints the bitmap font renders aliased and looks
			// horrible compared to the rest of the UI.
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setFont(getFont());

			final int startWidth = (getWidth() - g2d.getFontMetrics().stringWidth(text)) / 2;
			String white = "";
			int whiteWidth = 0;
			int chars = 0;
			for (int i = 0; i < text.length(); i++) {
				white += text.charAt(i);
				whiteWidth = g2d.getFontMetrics().stringWidth(white);
				if (startWidth + whiteWidth > x) {
					break;
				}
				chars++;
			}
			if (chars != text.length()) {
				white = white.substring(0, white.length() - 1);
				whiteWidth = g2d.getFontMetrics().stringWidth(white);
			}
			// Guard against the unlikely case where chars somehow ran past
			// the snapshot length (shouldn't happen with the loop above
			// checking i < text.length(), but cheap insurance).
			final int safeChars = Math.min(chars, text.length());
			float height = getFont().getSize();
			g2d.setColor(Color.WHITE);
			g2d.drawString(white, startWidth, height * 1.5F);
			g2d.setColor(Color.BLACK);
			g2d.drawString(text.substring(safeChars), whiteWidth + startWidth, height * 1.5F);
		}

		transparency.cleanup(g2d);
	}

	public float getTransparency() {
		return transparency.getTransparency();
	}

	public void setTransparency(float t) {
		transparency.setTransparency(t);
	}

	public float getHoverTransparency() {
		return transparency.getHoverTransparency();
	}

	public void setHoverTransparency(float t) {
		transparency.setHoverTransparency(t);
	}
	public void setText(String text) {
		this.setString(text);
		this.setVisible(true);
	}
	public void setWaiting(boolean value) {
		this.setVisible(true);

	}
}