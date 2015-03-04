package com.spiddekauga.voider.repo.resource;

import java.util.HashMap;
import java.util.Map;

import org.ini4j.Ini;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
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
public enum InternalNames {
	// UI
	/** Editor button */
	UI_EDITOR("ui/editor.json", Skin.class),
	/** General UI */
	UI_GENERAL("ui/general.json", Skin.class),
	/** Game UI */
	UI_GAME("ui/game.json", Skin.class),
	/** Credits UI */
	UI_CREDITS("ui/credits.json", Skin.class),

	// Themes / Backgrounds
	/** Core top */
	LEVEL_THEME_CORE_TOP("level_theme_core_top.png", Texture.class),
	/** Core bottom */
	LEVEL_THEME_CORE_BOTTOM("level_theme_core_bottom.png", Texture.class),
	/** Space top */
	LEVEL_THEME_SPACE_TOP("level_theme_space_top.png", Texture.class),
	/** Space bottom */
	LEVEL_THEME_SPACE_BOTTOM("level_theme_space_bottom.png", Texture.class),
	/** Surface top */
	LEVEL_THEME_SURFACE_TOP("level_theme_surface_top.png", Texture.class),
	/** Surface bottom */
	LEVEL_THEME_SURFACE_BOTTOM("level_theme_surface_bottom.png", Texture.class),
	/** Tunnels top */
	LEVEL_THEME_TUNNELS_TOP("level_theme_tunnels_top.png", Texture.class),
	/** Tunnels bottom */
	LEVEL_THEME_TUNNELS_BOTTOM("level_theme_tunnels_bottom.png", Texture.class),


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
	MUSIC_TUNNELS("tunnels.mp3", Music.class),
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

	/** Directory for all texture */
	private static String TEXTURE_PATH = "gfx/";
	/** Directory for all UI */
	private static String UI_PATH = "";
	/** Directory for all shaders */
	private static String SHADER_PATH = "shaders/";
	/** Directory for all particle effects */
	private static String PARTICLE_PATH = "particles/";
	/** Directory for all sound effects */
	private static String SOUND_PATH = "sfx/";
	/** Directory for all music */
	private static String MUSIC_PATH = "music/";
	/** Directory for all text/ini files */
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
		for (int i = LEVEL_THEME_CORE_TOP.ordinal(); i <= LEVEL_THEME_TUNNELS_BOTTOM.ordinal(); ++i) {
			TextureParameter textureParameter = new TextureParameter();
			textureParameter.genMipMaps = false;
			textureParameter.wrapU = TextureWrap.Repeat;
			textureParameter.wrapV = TextureWrap.Repeat;
			// textureParameter.minFilter = TextureFilter.Nearest;
			// textureParameter.minFilter = TextureFilter.MipMapLinearLinear;
			InternalNames internalName = InternalNames.values()[i];
			internalName.mParameters = textureParameter;
		}
	}
}
