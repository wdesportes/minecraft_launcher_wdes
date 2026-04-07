package fr.wdes.ui.lite;


import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;


public class HyperlinkJLabel extends TransparentJLabel implements MouseListener {
	private static final long CLICK_DELAY = 250L;
	private static final long serialVersionUID = 1L;
	private String url;
	private long lastClick = System.currentTimeMillis();
	public HyperlinkJLabel(String text, String url) {
		super(text);
		this.url = url;
		super.addMouseListener(this);
	}

	public void mouseClicked(MouseEvent e) {
		if (lastClick + CLICK_DELAY > System.currentTimeMillis()) {
			return;
		}
		lastClick = System.currentTimeMillis();
		try {
			URI uri = new java.net.URI(url);
			HyperlinkJLabel.browse(uri);
		} catch (Exception ex) {
			System.err.println("Unable to open browser to " + url);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public static void browse(URI uri) {
		Compatibility.browse(uri);
	}
}