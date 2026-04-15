package fr.wdes.ui.lite;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;



/**
 * Static utility class meant to allow Java 1.6 calls while maining 1.5 compability
 */
public class Compatibility {
	/**
	 * Replaces Desktop.getDesktop().browse(uri)
	 *
	 * @param uri
	 */
	static Desktop desktop;
	public static void browse(URI uri) {

	        if (desktop.isSupported(Desktop.Action.BROWSE)) {
	        	try {
					desktop.browse(uri);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }

	}

	public static void open(File file) {
        if (desktop.isSupported(Desktop.Action.OPEN)) {
        	try {
				desktop.open(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}

	@SuppressWarnings("rawtypes")
	public static void setIconImage(Window window, Image image) {
		try {
			Class[] params = {Image.class};
			Method setIconImage = Window.class.getMethod("setIconImage", params);
			setIconImage.invoke(window, image);
		} catch (Exception e) {

				e.printStackTrace();

		}
	}

	@SuppressWarnings("rawtypes")
	public static boolean setExecutable(File file, boolean executable, boolean owner) {
		try {
			Class[] params = {boolean.class, boolean.class};
			Method setExecutable = File.class.getMethod("setExecutable", params);
			return (Boolean)setExecutable.invoke(file, executable, owner);
		} catch (Exception e) {

				e.printStackTrace();

		}
		return false;
	}
}