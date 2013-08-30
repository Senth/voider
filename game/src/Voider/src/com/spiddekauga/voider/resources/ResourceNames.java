package com.spiddekauga.voider.resources;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.GameSave;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActorDef;

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
	/** Editor button */
	UI_EDITOR_BUTTONS("editor.json", Skin.class),
	/** General UI */
	UI_GENERAL("general.json", Skin.class),
	/** Splash Screen */
	IMAGE_SPLASH_SCREEN("spiddekauga_m.png", Texture.class),
	/** Default vertex shader */
	SHADER_DEFAULT("default", ShaderProgram.class),

	// TESTS
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
	}

	/**
	 * @return file path of this resource
	 */
	String getFilePath() {
		return getDirPath(type) + filename;
	}

	/** Filename of the resource */
	final String filename;
	/** The resource class type */
	final Class<?> type;


	/**
	 * Gets the fully qualified folder name the resource should be in
	 * @param type the class type of the resource. This determines where to look
	 * @return full path to the resource
	 * @throws UndefinedResourceTypeException throws this for all undefined resource types
	 */
	static String getDirPath(Class<?> type) throws UndefinedResourceTypeException {
		if (type == Texture.class) {
			return TEXTURE_PATH;
		} else if (type == BulletActorDef.class) {
			return ACTOR_BULLET_PATH;
		} else if (type == EnemyActorDef.class) {
			return ACTOR_ENEMY_PATH;
		} else if (type == PickupActorDef.class) {
			return ACTOR_PICKUP_PATH;
		} else if (type == PlayerActorDef.class){
			return ACTOR_PLAYER_PATH;
		} else if (type == StaticTerrainActorDef.class) {
			return ACTOR_STATIC_TERRAIN_PATH;
		} else if (type == LevelDef.class) {
			return LEVEL_DEF_PATH;
		} else if (type == Level.class) {
			return LEVEL_PATH;
		} else if (type == ParticleEffect.class){
			return PARTICLE_PATH;
		} else if (type == Skin.class) {
			return UI_PATH;
		} else if (type == Sound.class) {
			return SOUND_PATH;
		} else if (type == ShaderProgram.class) {
			return SHADER_PATH;
		} else if (type == GameSave.class) {
			return GAME_SAVE_PATH;
		} else if (type == GameSaveDef.class) {
			return GAME_SAVE_DEF_PATH;
		} else if (type == PlayerStats.class) {
			return PLAYER_STATS_PATH;
		} else {
			throw new UndefinedResourceTypeException(type);
		}
	}

	/**
	 * Changes the external storage path to the TEST_STORAGE instead
	 */
	public static void useTestPath() {
		LEVEL_DEF_PATH = Config.File.TEST_STORAGE + "levelDefs/";
		LEVEL_PATH = Config.File.TEST_STORAGE + "levels/";
		GAME_SAVE_PATH = Config.File.TEST_STORAGE + "resumeLevels/";
		GAME_SAVE_DEF_PATH = Config.File.TEST_STORAGE + "gameSaveDef/";
		PLAYER_STATS_PATH = Config.File.TEST_STORAGE + "stats/";

		// Actors
		ACTOR_DEF_PATH = Config.File.TEST_STORAGE + "actors/";
		ACTOR_BULLET_PATH = ACTOR_DEF_PATH + "bullets/";
		ACTOR_ENEMY_PATH = ACTOR_DEF_PATH + "enemies/";
		ACTOR_PICKUP_PATH = ACTOR_DEF_PATH + "pickups/";
		ACTOR_PLAYER_PATH = ACTOR_DEF_PATH + "player_ships/";
		ACTOR_STATIC_TERRAIN_PATH = ACTOR_DEF_PATH + "static_terrain/";

	}

	/** Directory for all texture */
	private static final String TEXTURE_PATH = "gfx/";
	/** Directory for all UI */
	private static final String UI_PATH = "ui/";
	/** Directory for all shaders */
	private static final String SHADER_PATH = "shaders/";
	/** Directory for all actor definitions */
	private static String ACTOR_DEF_PATH = Config.File.STORAGE + "actors/";
	/** Directory for all bullet actor definitions */
	private static String ACTOR_BULLET_PATH = ACTOR_DEF_PATH + "bullets/";
	/** Directory for all enemy actor definitions */
	private static String ACTOR_ENEMY_PATH = ACTOR_DEF_PATH + "enemies/";
	/** Directory for all pickup actor definitions */
	private static String ACTOR_PICKUP_PATH = ACTOR_DEF_PATH + "pickups/";
	/** Directory for all player actor definitions */
	private static String ACTOR_PLAYER_PATH = ACTOR_DEF_PATH + "player_ships/";
	/** Directory for all static terrain actor definitions */
	private static String ACTOR_STATIC_TERRAIN_PATH = ACTOR_DEF_PATH + "static_terrain/";
	/** Directory for all level definitions */
	private static String LEVEL_DEF_PATH = Config.File.STORAGE + "levelDefs/";
	/** Directory for all the actual levels */
	private static String LEVEL_PATH = Config.File.STORAGE + "levels/";
	/** Directory for all game saves, i.e. when the player plays and quits a level */
	private static String GAME_SAVE_PATH = Config.File.STORAGE + "gameSave/";
	/** Directory for all game save definitions */
	private static String GAME_SAVE_DEF_PATH = Config.File.STORAGE + "gameSaveDef/";
	/** Directory for saving player stats */
	private static String PLAYER_STATS_PATH = Config.File.STORAGE + "stats/";
	/** Directory for all particle effects */
	private static final String PARTICLE_PATH = "particles/";
	/** Directory for all sound effects */
	private static final String SOUND_PATH = "sound/";

	/** Directory for revisions */
	private static final String REVISION_PATH = "revisions/";

}
