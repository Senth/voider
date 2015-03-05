package com.spiddekauga.voider.repo.resource;

import java.util.HashMap;
import java.util.Map;

import org.ini4j.Ini;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.Path;
import com.spiddekauga.voider.Config.File;

/**
 * All static resources. Name and a corresponding filename This includes: \li Textures \li
 * Music \li Sound
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public enum InternalNames {
	// UI
	UI_EDITOR("ui/editor.json", Skin.class),
	UI_GENERAL("ui/general.json", Skin.class),
	UI_GAME("ui/game.json", Skin.class),
	UI_CREDITS("ui/credits.json", Skin.class),

	// Themes / Backgrounds
	LEVEL_BACKGROUND_CORE_1080("backgrounds/core", Skin.class),
	LEVEL_BACKGROUND_CORE_720("backgrounds/core_720", Skin.class),
	LEVEL_BACKGROUND_CORE_480("backgrounds/core_480", Skin.class),
	LEVEL_BACKGROUND_CORE_120("backgrounds/core_120", Skin.class),
	LEVEL_BACKGROUND_SURFACE_1080("backgrounds/surface", Skin.class),
	LEVEL_BACKGROUND_SURFACE_720("backgrounds/surface_720", Skin.class),
	LEVEL_BACKGROUND_SURFACE_480("backgrounds/surface_480", Skin.class),
	LEVEL_BACKGROUND_SURFACE_120("backgrounds/surface_120", Skin.class),
	LEVEL_BACKGROUND_TUNNEL_1080("backgrounds/tunnels", Skin.class),
	LEVEL_BACKGROUND_TUNNEL_720("backgrounds/tunnels_720", Skin.class),
	LEVEL_BACKGROUND_TUNNEL_480("backgrounds/tunnels_480", Skin.class),
	LEVEL_BACKGROUND_TUNNEL_120("backgrounds/tunnels_120", Skin.class),
	LEVEL_BACKGROUND_SPACE_1080("backgrounds/space", Skin.class),
	LEVEL_BACKGROUND_SPACE_720("backgrounds/space_720", Skin.class),
	LEVEL_BACKGROUND_SPACE_480("backgrounds/space_480", Skin.class),
	LEVEL_BACKGROUND_SPACE_120("backgrounds/space_120", Skin.class),


	// Images
	/** Splash Screen */
	IMAGE_SPLASH_SCREEN("background_splash.jpg", Texture.class),
	/** Game icon 256x256 */
	ICON_256("icon-256.png", Texture.class),
	/** Game icon 128x128 */
	ICON_128("icon-128.png", Texture.class),
	/** Game icon 64x64 */
	ICON_64("icon-64.png", Texture.class),
	/** game icon 32x32 */
	ICON_32("icon-32.png", Texture.class),


	// Shaders
	/** Default vertex shader */
	SHADER_DEFAULT("default", ShaderProgram.class),


	// Text files
	/** Credits to display */
	INI_CREDITS("credits.ini", Ini.class),
	/** Config file */
	INI_CONFIG("config.ini", Ini.class),


	// Music
	/** Space music */
	MUSIC_SPACE("space.mp3", Music.class),
	/** Surface music */
	MUSIC_SURFACE("surface.mp3", Music.class),
	/** Tunnels music */
	MUSIC_TUNNEL("tunnels.mp3", Music.class),
	/** Core music */
	MUSIC_CORE("core.mp3", Music.class),
	/** Game over music */
	MUSIC_GAME_OVER_INTRO("game_over_intro.mp3", Music.class),
	/** Game over loop */
	MUSIC_GAME_OVER_LOOP("game_over_loop.mp3", Music.class),
	/** Level success music */
	MUSIC_LEVEL_COMPLETED("level_completed.mp3", Music.class),
	/** Main theme */
	MUSIC_TITLE("title_theme.mp3", Music.class),

	// Sound
	/** Ship has low health */
	SOUND_SHIP_LOW_HEALTH("ship_low_health.mp3", Sound.class),
	/** Bullet hits a player */
	SOUND_BULLET_HIT_PLAYER("bullet_hit_player.mp3", Sound.class),
	/** Ship collision with terrain or enemy */
	SOUND_SHIP_COLLIDE("ship_collide.mp3", Sound.class),
	/** Enemy ship explodes on player */
	SOUND_ENEMY_EXLODES("enemy_explodes.mp3", Sound.class),
	/** Player ship lost */
	SOUND_SHIP_LOST("ship_lost.mp3", Sound.class),
	/** UI hover over button */
	SOUND_UI_BUTTON_HOVER("ui_button_hover.mp3", Sound.class),
	/** UI button click */
	SOUND_UI_BUTTON_CLICK("ui_button_click.mp3", Sound.class),

	;

	/**
	 * Initializes the enum with a filename
	 * @param filename the filename (not full path)
	 * @param type the class type of resource
	 */
	private InternalNames(String filename, Class<?> type) {
		this.mFilename = filename;
		this.mType = type;
	}

	/**
	 * @return file path of this resource
	 */
	public String getFilePath() {
		return getDirPath(mType) + mFilename;
	}

	/**
	 * @return stored type of the resource
	 */
	Class<?> getType() {
		return mType;
	}

	/**
	 * @return loading parameters
	 */
	@SuppressWarnings("rawtypes")
	AssetLoaderParameters getParameters() {
		return mParameters;
	}

	/** Optional parameters */
	private AssetLoaderParameters<?> mParameters = null;
	/** Filename of the resource */
	private final String mFilename;
	/** The resource class type */
	private final Class<?> mType;


	/**
	 * Gets the fully qualified folder name the resource should be in
	 * @param type the class type of the resource. This determines where to look
	 * @return full path to the resource
	 */
	private static String getDirPath(Class<?> type) {
		return mResourcePaths.get(type);
	}

	/** Map for all resource paths */
	private static Map<Class<?>, String> mResourcePaths = null;

	private static String TEXTURE_PATH = "gfx/";
	private static String UI_PATH = "";
	private static String SHADER_PATH = "shaders/";
	private static String PARTICLE_PATH = "particles/";
	private static String SOUND_PATH = "sfx/";
	private static String MUSIC_PATH = "music/";
	private static String TEXT_PATH = "txt/";


	static {
		// -- Resource names --
		if (mResourcePaths == null) {
			mResourcePaths = new HashMap<Class<?>, String>();

			if (File.USE_EXTERNAL_RESOURCES) {
				String execDir = Path.getExecDir();
				String dir = execDir + "internal_resources/";

				TEXTURE_PATH = dir + TEXTURE_PATH;
				UI_PATH = dir + UI_PATH;
				SHADER_PATH = dir + SHADER_PATH;
				PARTICLE_PATH = dir + PARTICLE_PATH;
				SOUND_PATH = dir + SOUND_PATH;
				TEXT_PATH = dir + TEXT_PATH;
				MUSIC_PATH = dir + MUSIC_PATH;
			}


			mResourcePaths.put(Texture.class, TEXTURE_PATH);
			mResourcePaths.put(ShaderProgram.class, SHADER_PATH);
			mResourcePaths.put(ParticleEffect.class, PARTICLE_PATH);
			mResourcePaths.put(Skin.class, UI_PATH);
			mResourcePaths.put(Sound.class, SOUND_PATH);
			mResourcePaths.put(Ini.class, TEXT_PATH);
			mResourcePaths.put(Music.class, MUSIC_PATH);
		}


		// -- Parameters --
		// Level theme
		for (int i = LEVEL_BACKGROUND_CORE_1080.ordinal(); i <= LEVEL_BACKGROUND_SPACE_120.ordinal(); ++i) {
			TextureParameter textureParameter = new TextureParameter();
			textureParameter.genMipMaps = false;
			textureParameter.wrapU = TextureWrap.Repeat;
			textureParameter.wrapV = TextureWrap.Repeat;
			textureParameter.minFilter = TextureFilter.Nearest;
			textureParameter.minFilter = TextureFilter.Nearest;
			InternalNames internalName = InternalNames.values()[i];
			internalName.mParameters = textureParameter;
		}
	}
}
