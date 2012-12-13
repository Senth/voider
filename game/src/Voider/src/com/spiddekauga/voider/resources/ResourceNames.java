package com.spiddekauga.voider.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.ActorDef;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;

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
	/** test */
	PARTICLE_TEST("test", ParticleEffect.class),
	/** test */
	SOUND_TEST("test2", Sound.class);


	/**
	 * Initializes the enum with a filename
	 * @param filename the filename (not full path)
	 * @param type the class type of resource
	 */
	private ResourceNames(String filename, Class<?> type) {
		this.filename = filename;
		this.type = type;

		String name = "";
		try {
			name = getDirPath(type) + filename;
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("UndefinedResourceType", e.toString());
		}
		fullName = name;
	}

	/** Filename of the resource */
	final String filename;
	/** The resource class type */
	final Class<?> type;
	/** Full path of the resource */
	final String fullName;

	/**
	 * Gets the fully qualified folder name the resource should be in
	 * @param type the class type of the resource. This determines where to look
	 * @return full path to the resource
	 * @throws UndefinedResourceTypeException throws this for all undefined resource types
	 */
	static String getDirPath(Class<?> type) throws UndefinedResourceTypeException {
		if (type == Texture.class) {
			return TEXTURE_PATH;
		} else if (type == ActorDef.class) {
			return ACTOR_DEF_PATH;
		} else if (type == LevelDef.class) {
			return LEVEL_DEF_PATH;
		} else if (type == Level.class) {
			return LEVEL_PATH;
		} else if (type == ParticleEffect.class){
			return PARTICLE_PATH;
		} else if (type == Sound.class) {
			return SOUND_PATH;
		} else {
			throw new UndefinedResourceTypeException(type);
		}
	}

	/** Directory for all texture */
	private static final String TEXTURE_PATH = "gfx/";
	/** Directory for all actor definitions */
	private static final String ACTOR_DEF_PATH = Config.File.STORAGE + "actors/";
	/** Directory for all level definitions */
	private static final String LEVEL_DEF_PATH = Config.File.STORAGE + "levelDefs/";
	/** Directory for all the actual levels */
	private static final String LEVEL_PATH = Config.File.STORAGE + "levels/";
	/** Directory for all particle effects */
	private static final String PARTICLE_PATH = "particles/";
	/** Directory for all sound effects */
	private static final String SOUND_PATH = "sound/";

}
