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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;

import fr.wdes.Launcher;





public class BackgroundImage extends JLabel {
	private static final long serialVersionUID = 1L;
	private final int width;
	private final int height;

	public BackgroundImage(int width, int height) {
		this.width = width;
		this.height = height;
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalAlignment(SwingConstants.CENTER);
		setBounds(0, 0, width, height);

		setIcon(new ImageIcon(getBackgroundImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
		setVerticalAlignment(SwingConstants.TOP);
		setHorizontalAlignment(SwingConstants.LEFT);
	}

	/**
	 * Re-reads the fonds directory and updates the displayed background.
	 * Safe to call from any thread; image loading, Gaussian blur and scaling
	 * all run off the EDT and the resulting fully-rendered BufferedImage is
	 * swapped in on the EDT. We scale into a BufferedImage explicitly rather
	 * than going through {@link Image#getScaledInstance} so the blurred pixels
	 * are guaranteed to be rendered before the icon is shown.
	 */
	public void refresh() {
		new Thread(new Runnable() {
			public void run() {
				final BufferedImage src = getBackgroundImage();
				final BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g = scaled.createGraphics();
				try {
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					g.drawImage(src, 0, 0, width, height, null);
				} finally {
					g.dispose();
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setIcon(new ImageIcon(scaled));
						repaint();
					}
				});
			}
		}, "BackgroundRefresh").start();
	}

	private BufferedImage getBackgroundImage() {
		final List<File> images = collectImages();
		InputStream stream = null;
		BufferedImage image;
		try {
			try {
				stream = new FileInputStream(images.get((new Random()).nextInt(images.size())));
			} catch (Exception io) {
				if (images.size() > 0) {
					io.printStackTrace();
				}
				stream = ResourceUtils.getResourceAsStream("/fr/wdes/ressources/background.jpg");
			}
			image = ImageIO.read(stream);
			image = BlurUtils.applyGaussianBlur(image, 10, 1, true);
			return image;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(stream);
		}
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Collect candidate background images. Prefer the time-of-day folder
	 * (nuit / jour / soiree) but fall back to any sibling folder under
	 * fonds/ when the current one is empty - otherwise running the launcher
	 * outside of "jour" hours when only daytime fonds were published would
	 * always show the bundled default.
	 */
	private List<File> collectImages() {
		final List<File> images = new ArrayList<File>();
		final File fondsDir = new File(Launcher.getInstance().getWorkingDirectory(), "fonds");
		final File timeDir = new File(fondsDir, getTimeFolder());
		appendImagesFrom(timeDir, images);
		if (!images.isEmpty()) {
			return images;
		}
		// Fall back to anything else under fonds/.
		if (fondsDir.isDirectory()) {
			final File[] subs = fondsDir.listFiles();
			if (subs != null) {
				for (File sub : subs) {
					if (sub.isDirectory()) {
						appendImagesFrom(sub, images);
					} else {
						addIfImage(sub, images);
					}
				}
			}
		}
		return images;
	}

	private static void appendImagesFrom(File dir, List<File> out) {
		if (dir == null || !dir.isDirectory()) {
			return;
		}
		final File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			addIfImage(f, out);
		}
	}

	private static void addIfImage(File f, List<File> out) {
		if (f == null || !f.isFile()) {
			return;
		}
		final String name = f.getName().toLowerCase(Locale.ROOT);
		if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
			out.add(f);
		}
	}

	private String getTimeFolder() {
		int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hours < 6) {
			return "nuit";
		}
		if (hours < 12) {
			return "jour";
		}
		if (hours < 20) {
			return "soiree";
		}
		return "nuit";
	}
}
