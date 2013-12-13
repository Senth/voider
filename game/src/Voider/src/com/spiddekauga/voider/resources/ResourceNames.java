package com.spiddekauga.voider.resources;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.Path;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.File;
import com.spiddekauga.voider.game.GameSave;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;

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
	// UI
	/** Editor button */
	UI_EDITOR_BUTTONS("ui/editor.json", Skin.class),
	/** General UI */
	UI_GENERAL("ui/general.json", Skin.class),
	/** Editor tooltip images */
	UI_EDITOR_TOOLTIPS("ui/editor_tooltips.json", Skin.class),
	/** Game UI */
	UI_GAME("ui/game.json", Skin.class),

	// Themes / Backgrounds
	/** Space theme, first level */
	THEME_SPACE("themes/Space.json", Skin.class),
	/** Abandoned planet */
	THEME_RED_PLANET_SURFACE("themes/RedPlanet-Surface.json", Skin.class),
	/** Inner lava level */
	THEME_RED_PLANET_TUNNELS("themes/RedPlanet-Tunnels.json", Skin.class),
	/** BioCyber level */
	THEME_RED_PLANET_CORE("themes/RedPlanet-Core.json", Skin.class),

	// Images
	/** Splash Screen */
	IMAGE_SPLASH_SCREEN("spiddekauga_m.png", Texture.class),

	// Shaders
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
		String path = mResourcePaths.get(type);

		if (path != null) {
			return path;
		} else {
			throw new UndefinedResourceTypeException(type);
		}
	}

	/**
	 * Changes the external storage path to the TEST_STORAGE instead.
	 * Needs to be called before {@link #init()}.
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
	}

	/**
	 * Initializes the resource names
	 */
	static void init() {
		if (File.USE_EXTERNAL_RESOURCES) {
			String execDir = Path.getExecDir();
			String dir = execDir + "internal_resources/";

			TEXTURE_PATH = dir + TEXTURE_PATH;
			UI_PATH = dir + UI_PATH;
			SHADER_PATH = dir + SHADER_PATH;
			PARTICLE_PATH = dir + PARTICLE_PATH;
			SOUND_PATH = dir + SOUND_PATH;
		}


		mResourcePaths.put(Texture.class, TEXTURE_PATH);
		mResourcePaths.put(BulletActorDef.class, ACTOR_BULLET_PATH);
		mResourcePaths.put(EnemyActorDef.class, ACTOR_ENEMY_PATH);
		mResourcePaths.put(PickupActorDef.class, ACTOR_PICKUP_PATH);
		mResourcePaths.put(PlayerActorDef.class, ACTOR_PLAYER_PATH);
		mResourcePaths.put(LevelDef.class, LEVEL_DEF_PATH);
		mResourcePaths.put(Level.class, LEVEL_PATH);
		mResourcePaths.put(ShaderProgram.class, SHADER_PATH);
		mResourcePaths.put(ParticleEffect.class, PARTICLE_PATH);
		mResourcePaths.put(Skin.class, UI_PATH);
		mResourcePaths.put(Sound.class, SOUND_PATH);
		mResourcePaths.put(GameSave.class, GAME_SAVE_PATH);
		mResourcePaths.put(GameSaveDef.class, GAME_SAVE_DEF_PATH);
		mResourcePaths.put(PlayerStats.class, PLAYER_STATS_PATH);
	}

	/**
	 * @return all resource paths
	 */
	static Map<Class<?>, String> getResourcePaths() {
		return mResourcePaths;
	}

	/** Map for all resource paths */
	private static Map<Class<?>, String> mResourcePaths = new HashMap<Class<?>, String>();

	/** Directory for all texture */
	private static String TEXTURE_PATH = "gfx/";
	/** Directory for all UI */
	private static String UI_PATH = "";
	/** Directory for all shaders */
	private static String SHADER_PATH = "shaders/";
	/** Directory for all particle effects */
	private static String PARTICLE_PATH = "particles/";
	/** Directory for all sound effects */
	private static String SOUND_PATH = "sound/";

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
}
