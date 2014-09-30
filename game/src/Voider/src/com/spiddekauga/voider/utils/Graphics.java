package com.spiddekauga.voider.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * General graphics helper methods
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Graphics {
	/**
	 * Convert PNG image to a drawable texture
	 * @param pngBytes PNG image in bytes
	 * @return drawable PNG image
	 */
	public static TextureRegionDrawable pngToDrawable(byte[] pngBytes) {
		Pixmap pixmap = new Pixmap(pngBytes, 0, pngBytes.length);
		Texture texture = new Texture(pixmap);
		TextureRegion textureRegion = new TextureRegion(texture);
		return new TextureRegionDrawable(textureRegion);
	}

	/**
	 * Convert all pixels with a specific color to another
	 * @param pixmap
	 * @param fromColor should be same format as the pixmap
	 * @param toColor should be same format as the pixmap
	 */
	public static void pixmapReplaceColor(Pixmap pixmap, int fromColor, int toColor) {
		Blending previousBlending = Pixmap.getBlending();
		Pixmap.setBlending(Blending.None);

		for (int x = 0; x < pixmap.getWidth(); ++x) {
			for (int y = 0; y < pixmap.getHeight(); ++y) {
				if (fromColor == pixmap.getPixel(x, y)) {
					pixmap.drawPixel(x, y, toColor);
				}
			}
		}

		Pixmap.setBlending(previousBlending);
	}
}
