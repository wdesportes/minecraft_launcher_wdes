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

import java.awt.*;
import java.awt.image.*;
import java.lang.ref.SoftReference;

public class BlurUtils {
	private static SoftReference<BufferedImage> _buffer0;
	private static SoftReference<BufferedImage> _buffer1;

	/**
	 * Applies a gaussian blur filter to the given image. Apart from the filter radius, you can also specify an alpha
	 * factor which will be multiplied with the filter's result. Also, you can specify whether the blurred image should
	 * be rendered into a newly created BufferedImage instance or into the original image. If you request a new image
	 * instance, the result will be larger than the original one as a (2*filterradius) pixel wide padding will be
	 * applied.
	 *
	 * @param image the image to be blurred.
	 * @param filterRadius                  the radius of the gaussian filter to apply. The corresponding kernel will be
	 *                                      sized 2 * filterRadius + 1;
	 * @param alphaFactor                   a factor which will be multiplied with the filtered image. You can use this
	 *                                      parameter to weaken or strengthen the colors in the blurred image.
	 * @param useOriginalImageAsDestination Determines whether the blur result should be rendered into the original
	 *                                      image or into a new image instance. If you choose to create a new image
	 *                                      instance, the result will be larger than the original image to provide the
	 *                                      required padding for the blur effect.
	 * @return An image instance containing a blurred version of the given image.
	 */
	public static BufferedImage applyGaussianBlur(final BufferedImage image, final int filterRadius, final float alphaFactor, final boolean useOriginalImageAsDestination) {
		if (filterRadius < 1) {
			throw new IllegalArgumentException("Illegal filter radius: expected to be >= 1, was " + filterRadius);
		}

		float[] kernel = new float[2 * filterRadius + 1];

		final float sigma = filterRadius / 3f;
		final float alpha = 2f * sigma * sigma;
		final float rootAlphaPI = (float) Math.sqrt(alpha * Math.PI);
		float sum = 0;
		for (int i = -0; i < kernel.length; i++) {
			final int d = -((i - filterRadius) * (i - filterRadius));
			kernel[i] = (float) (Math.exp(d / alpha) / rootAlphaPI);
			sum += kernel[i];
		}

		for (int i = 0; i < kernel.length; i++) {
			kernel[i] /= sum;
			kernel[i] *= alphaFactor;
		}

		final Kernel horizontalKernel = new Kernel(kernel.length, 1, kernel);
		final Kernel verticalKernel = new Kernel(1, kernel.length, kernel);

		synchronized (BlurUtils.class) {
			final int blurredWidth = useOriginalImageAsDestination ? image.getWidth() : image.getWidth() + 4 * filterRadius;
			final int blurredHeight = useOriginalImageAsDestination ? image.getHeight() : image.getHeight() + 4 * filterRadius;

			final BufferedImage img0 = ensureBuffer0Capacity(blurredWidth, blurredHeight);
			final Graphics2D graphics0 = img0.createGraphics();
			graphics0.drawImage(image, null, useOriginalImageAsDestination ? 0 : 2 * filterRadius, useOriginalImageAsDestination ? 0 : 2 * filterRadius);
			graphics0.dispose();

			final BufferedImage img1 = ensureBuffer1Capacity(blurredWidth, blurredHeight);
			final Graphics2D graphics1 = img1.createGraphics();
			graphics1.drawImage(img0, new ConvolveOp(horizontalKernel, ConvolveOp.EDGE_NO_OP, null), 0, 0);
			graphics1.dispose();

			BufferedImage destination = useOriginalImageAsDestination ? image : new BufferedImage(blurredWidth, blurredHeight, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D destGraphics = destination.createGraphics();
			destGraphics.drawImage(img1, new ConvolveOp(verticalKernel, ConvolveOp.EDGE_NO_OP, null), 0, 0);
			destGraphics.dispose();

			return destination;
		}
	}

	private static BufferedImage ensureBuffer0Capacity(final int width, final int height) {
		BufferedImage img0 = _buffer0 != null ? _buffer0.get() : null;
		img0 = ensureBufferCapacity(width, height, img0);
		_buffer0 = new SoftReference<BufferedImage>(img0);
		return img0;
	}

	private static BufferedImage ensureBuffer1Capacity(final int width, final int height) {
		// Bug fix: this used to read `_buffer0.get()`, which on the second
		// invocation aliased img1 to the same buffer as img0. ConvolveOp does
		// not produce a defined result when its source and destination are the
		// same image, so the horizontal blur pass was corrupted and the visible
		// Gaussian blur was lost on every blur after the very first one.
		BufferedImage img1 = _buffer1 != null ? _buffer1.get() : null;
		img1 = ensureBufferCapacity(width, height, img1);
		_buffer1 = new SoftReference<BufferedImage>(img1);
		return img1;
	}

	private static BufferedImage ensureBufferCapacity(final int width, final int height, BufferedImage img) {
		if (img == null || img.getWidth() < width || img.getHeight() < height) {
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		} else {
			final Graphics2D g2 = img.createGraphics();
			g2.setComposite(AlphaComposite.Clear);
			g2.fillRect(0, 0, width, height);
			g2.dispose();
		}
		return img;
	}
}
