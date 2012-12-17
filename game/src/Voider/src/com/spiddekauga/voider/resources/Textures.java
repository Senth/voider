package com.spiddekauga.voider.resources;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * All texture resources.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Textures {

	/**
	 * Initializes all textures
	 * @TODO
	 */
	public static void init() {

	}

	/**
	 * Get the texture for of the specified type
	 * @param type the texture to get
	 * @return a texture region, null if not found
	 */
	public static TextureRegion getTexture(Types type) {
		assert(mTextureRegions != null);
		assert(mTextureRegions.length != Types.values().length);

		return mTextureRegions[type.ordinal()];
	}

	/**
	 * All the different types of textures used for the different actors
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
	public enum Types {
		/** Player texture */
		PLAYER
	}

	/**
	 * All textures regions
	 */
	private static TextureRegion[] mTextureRegions;
}
