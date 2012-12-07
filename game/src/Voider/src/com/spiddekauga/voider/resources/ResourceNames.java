package com.spiddekauga.voider.resources;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;

/**
 * All static resources. Name and a corresponding filename
 * This includes:
 * \li Textures
 * \li Music
 * \li Sound
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public enum ResourceNames {
	/** Texture player */
	TEXTURE_PLAYER("libgdx.png", Texture.class),
	PARTICLE_TEST("test", ParticleEffect.class),
	SOUND_TEST("test2", Sound.class);
	;

	/**
	 * Initializes the enum with a filename
	 * @param filename the filename (not full path)
	 * @param type the class type of resource
	 */
	private ResourceNames(String filename, Class<?> type) {
		this.filename = filename;
		this.type = type;
	}

	/** Filename of the resource */
	public final String filename;
	/** The resource class type */
	public final Class<?> type;
}
