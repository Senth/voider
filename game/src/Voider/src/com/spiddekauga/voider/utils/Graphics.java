package com.spiddekauga.voider.utils;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.spiddekauga.voider.utils.Geometry.PointIndex;

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

	/**
	 * Create contour points from a picture.
	 * @param region the texture region to create the ship from
	 * @return Contour of the texture region.
	 */
	public static ArrayList<Vector2> getContour(TextureRegion region) {
		return getContour(region, 0, 1);
	}

	/**
	 * Create contour points from a picture. Based on <a href=
	 * "http://www.imageprocessingplace.com/downloads_V3/root_downloads/tutorials/contour_tracing_Abeer_George_Ghuneim/square.html"
	 * >square tracing algorithm</a>
	 * @param region the texture region to create the ship from
	 * @param angleMin adjacent points that have less or equal angle (in degrees) to this
	 *        will be removed. 0 keeps the structure of the contour while removing all
	 *        unnecessary points.
	 * @param scale scale the points with this value.
	 * @return Contour of the texture region. Center?
	 */
	public static ArrayList<Vector2> getContour(TextureRegion region, float angleMin, float scale) {
		ArrayList<Vector2> points = null;

		TextureData textureData = region.getTexture().getTextureData();
		if (textureData.getType() == TextureDataType.Pixmap) {
			if (!textureData.isPrepared()) {
				textureData.prepare();
			}
			Pixmap pixmap = textureData.consumePixmap();
			Vector2 startPos = getContourStartPos(pixmap, region.getRegionX(), region.getRegionY(), region.getRegionWidth(), region.getRegionHeight());
			points = getRawContourPoints(pixmap, startPos, region.getRegionX(), region.getRegionY(), region.getRegionWidth(),
					region.getRegionHeight());
			pixmap.dispose();

			// Remove points that have less or equal to angleMin degrees
			ArrayList<PointIndex> removedPoints = Geometry.removeExcessivePoints(0, angleMin, points);
			// for (PointIndex pointIndex : removedPoints) {
			// Pools.vector2.free(pointIndex.point);
			// }

			// Scale
			if (scale != 1) {
				for (Vector2 point : points) {
					point.scl(scale);
				}
			}

			// Center
			Vector2 center = Geometry.calculateCenter(points);
			center.scl(-1);

			// Update positions relative to the center
			Geometry.moveVertices(points, center, false);
		}

		return points;
	}

	/**
	 * Checks if the specified pixel position is inside specified bounds
	 * @param testPixel the test position
	 * @param x starting position of the texture region
	 * @param y starting position of the texture region
	 * @param width width of the texture region
	 * @param height height of the texture region
	 * @return true if inside the bounds, false if out of bounds
	 */
	private static boolean isPixelInsideBounds(Vector2 testPixel, int x, int y, int width, int height) {
		int testX = (int) testPixel.x;
		if (testX < x || testX >= x + width) {
			return false;
		}

		int testY = (int) testPixel.y;
		if (testY < y || testY >= y + height) {
			return false;
		}

		return true;
	}

	/**
	 * Get raw contour points
	 * @param pixmap
	 * @param startPos starting position
	 * @param x starting position of texture region
	 * @param y starting position of texture region
	 * @param width width of texture region
	 * @param height height of texture region
	 * @return all found points
	 */
	private static ArrayList<Vector2> getRawContourPoints(Pixmap pixmap, Vector2 startPos, int x, int y, int width, int height) {
		Directions direction = Directions.UP;
		Vector2 currentPos = startPos;
		ArrayList<Vector2> points = new ArrayList<>();

		do {
			if (isPixelInsideBounds(currentPos, x, y, width, height) && isPixelOpaque(pixmap, currentPos)) {
				// Only add if the previously added isn't the same
				if (points.isEmpty() || !points.get(points.size() - 1).equals(currentPos)) {
					points.add(currentPos);
				}
				direction = direction.left();
			} else {
				direction = direction.right();
			}

			// Get next position
			currentPos = direction.nextPixel(currentPos);
		} while (!(currentPos.equals(startPos) && direction == Directions.UP));

		return points;
	}

	/**
	 * Checks if the pixel is fully opaque
	 * @param pixmap the pixmap
	 * @param pos the position to check
	 * @return true if the pixel is fully opaque
	 */
	private static boolean isPixelOpaque(Pixmap pixmap, Vector2 pos) {
		return isPixelOpaque(pixmap, (int) pos.x, (int) pos.y);
	}

	/**
	 * Checks if the pixel is fully opaque
	 * @param pixmap the pixmap
	 * @param x
	 * @param y
	 * @return true if the pixel is fully opaque
	 */
	private static boolean isPixelOpaque(Pixmap pixmap, int x, int y) {
		int pixelColor = pixmap.getPixel(x, y);
		int alpha = pixelColor & 0x000000ff;
		return alpha == 0xff;
	}

	/**
	 * Find a starting position for the contour algorithm. Finds the first fully opaque
	 * pixel.
	 * @param pixmap pixmap to find a starting position from
	 * @param startX start position of x
	 * @param startY start position of y
	 * @param width width of texture region
	 * @param height height of the texture region
	 * @return start position for the contour algorithm.
	 */
	public static Vector2 getContourStartPos(Pixmap pixmap, int startX, int startY, int width, int height) {
		for (int x = startX; x < width + startX; ++x) {
			for (int y = startY; y < height + startY; ++y) {
				if (isPixelOpaque(pixmap, x, y)) {
					// return Pools.vector2.obtain().set(x, y);
					return new Vector2(x, y);
				}
			}
		}

		return null;
	}

	/**
	 * Directions for finding the contour
	 */
	private enum Directions {
		// Should always be in clockwise order
		LEFT,
		UP,
		RIGHT,
		DOWN,

		;

		/**
		 * Calculate next pixel if we move in this direction
		 * @param currentPixel current pixel position
		 * @return nextPixel the next pixel to set
		 */
		Vector2 nextPixel(Vector2 currentPixel) {
			// Vector2 nextPixel = Pools.vector2.obtain().set(currentPixel);
			Vector2 nextPixel = new Vector2(currentPixel);

			switch (this) {
			case DOWN:
				nextPixel.sub(0, 1);
				break;

			case LEFT:
				nextPixel.sub(1, 0);
				break;

			case RIGHT:
				nextPixel.add(1, 0);
				break;

			case UP:
				nextPixel.add(0, 1);
				break;
			}

			return nextPixel;
		}

		/**
		 * @return Left of the current direction
		 */
		Directions left() {
			int leftOrdinal = ordinal() - 1;

			// Wrap
			if (leftOrdinal < 0) {
				leftOrdinal = values().length - 1;
			}

			return values()[leftOrdinal];
		}

		/**
		 * @return right of the current direction
		 */
		Directions right() {
			int rightOrdinal = ordinal() + 1;

			// Wrap
			if (rightOrdinal >= values().length) {
				rightOrdinal = 0;
			}

			return values()[rightOrdinal];
		}
	}
}
