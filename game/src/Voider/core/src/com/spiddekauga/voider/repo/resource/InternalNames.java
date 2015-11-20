package com.spiddekauga.voider.repo.resource;

import java.util.HashMap;
import java.util.Map;

import org.ini4j.Ini;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
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
	UI_EDITOR_MDPI("ui/editor-mdpi.json", Skin.class),
	UI_EDITOR_HDPI("ui/editor-hdpi.json", Skin.class),
	UI_EDITOR_XHDPI("ui/editor-xhdpi.json", Skin.class),
	UI_GENERAL_MDPI("ui/general-mdpi.json", Skin.class),
	UI_GENERAL_HDPI("ui/general-hdpi.json", Skin.class),
	UI_GENERAL_XHDPI("ui/general-xhdpi.json", Skin.class),
	UI_GAME_MDPI("ui/game-mdpi.json", Skin.class),
	UI_GAME_HDPI("ui/game-hdpi.json", Skin.class),
	UI_GAME_XHDPI("ui/game-xhdpi.json", Skin.class),
	UI_CREDITS("ui/credits.json", Skin.class),

	// Themes / Backgrounds
	LEVEL_BACKGROUND_CORE_1080("backgrounds/core.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_CORE_720("backgrounds/core_720.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_CORE_480("backgrounds/core_480.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_CORE_120("backgrounds/core_120.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_SURFACE_1080("backgrounds/surface.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_SURFACE_720("backgrounds/surface_720.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_SURFACE_480("backgrounds/surface_480.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_SURFACE_120("backgrounds/surface_120.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_TUNNELS_1080("backgrounds/tunnels.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_TUNNELS_720("backgrounds/tunnels_720.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_TUNNELS_480("backgrounds/tunnels_480.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_TUNNELS_120("backgrounds/tunnels_120.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_SPACE_1080("backgrounds/space.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_SPACE_720("backgrounds/space_720.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_SPACE_480("backgrounds/space_480.atlas", TextureAtlas.class),
	LEVEL_BACKGROUND_SPACE_120("backgrounds/space_120.atlas", TextureAtlas.class),


	// Images
	/** Splash Screen */
	IMAGE_SPLASH_SCREEN("background_splash.png", Texture.class),
	ICON_256("icon-256.png", Texture.class),
	ICON_128("icon-128.png", Texture.class),
	ICON_64("icon-64.png", Texture.class),
	ICON_32("icon-32.png", Texture.class),


	// Shaders
	/** Default vertex shader */
	SHADER_DEFAULT("default", ShaderProgram.class),


	// Ini files
	INI_CREDITS("credits.ini", Ini.class),
	INI_CONFIG("config.ini", Ini.class),


	// Text files
	TXT_TERMS("terms.txt", String.class),


	// Font files
	FONT_ARIAL("arial.ttf", FreeType.class),
	FONT_CHARGEN("chargen_(6809).ttf", FreeType.class),
	FONT_JOYSTIX_MONOSPACE("joystix_monospace.ttf", FreeType.class),


	// Music
	MUSIC_SPACE("space.mp3", Music.class),
	MUSIC_SURFACE("surface.mp3", Music.class),
	MUSIC_TUNNEL("tunnels.mp3", Music.class),
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
	SOUND_SHIP_LOW_HEALTH("ship_low_health.mp3", Sound.class),
	SOUND_BULLET_HIT_PLAYER("bullet_hit_player.mp3", Sound.class),
	SOUND_SHIP_COLLIDE("ship_collide.mp3", Sound.class),
	SOUND_ENEMY_EXLODES("enemy_explodes.mp3", Sound.class),
	SOUND_SHIP_LOST("ship_lost.mp3", Sound.class),
	SOUND_UI_BUTTON_HOVER("ui_button_hover.mp3", Sound.class),
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
	 * @return true if loaded
	 */
	public boolean isLoaded() {
		return ResourceCacheFacade.isLoaded(this);
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
	AssetLoaderParameters<?> getParameters() {
		if (mParameters instanceof IParameterGenerate) {
			if (!isLoaded()) {
				((IParameterGenerate) mParameters).generate();
			}
		}
		return mParameters;
	}

	/** Optional parameters */
	private AssetLoaderParameters<?> mParameters = null;
	private final String mFilename;
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
	private static String FONT_PATH = "fonts/";


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
				FONT_PATH = dir + FONT_PATH;
			}


			mResourcePaths.put(Texture.class, TEXTURE_PATH);
			mResourcePaths.put(ShaderProgram.class, SHADER_PATH);
			mResourcePaths.put(ParticleEffect.class, PARTICLE_PATH);
			mResourcePaths.put(Skin.class, UI_PATH);
			mResourcePaths.put(TextureAtlas.class, "");
			mResourcePaths.put(Sound.class, SOUND_PATH);
			mResourcePaths.put(Ini.class, TEXT_PATH);
			mResourcePaths.put(String.class, TEXT_PATH);
			mResourcePaths.put(Music.class, MUSIC_PATH);
			mResourcePaths.put(FreeType.class, FONT_PATH);
		}


		// -- Parameters --
		// Fonts
		// MDPI
		SkinFontParameter fontParameter = new SkinFontParameter();
		fontParameter.addFont(FONT_ARIAL, "arial", 12, 16, 24, 36);
		fontParameter.addFont(FONT_CHARGEN, "chargen", 12, 16, 24, 36);
		fontParameter.addFont(FONT_JOYSTIX_MONOSPACE, "joystix_monospace", 12, 16, 24, 36);
		UI_GENERAL_MDPI.mParameters = fontParameter;

		// HDPI
		fontParameter = new SkinFontParameter();
		fontParameter.addFont(FONT_ARIAL, "default", 18, 24, 36, 54);
		fontParameter.addFont(FONT_CHARGEN, "chargen", 18, 24, 36, 54);
		fontParameter.addFont(FONT_JOYSTIX_MONOSPACE, "joystix_monospace", 18, 24, 36, 54);
		UI_GENERAL_HDPI.mParameters = fontParameter;

		// HDPI
		fontParameter = new SkinFontParameter();
		fontParameter.addFont(FONT_ARIAL, "default", 24, 32, 48, 72);
		fontParameter.addFont(FONT_CHARGEN, "chargen", 24, 32, 48, 72);
		fontParameter.addFont(FONT_JOYSTIX_MONOSPACE, "joystix_monospace", 24, 32, 48, 72);
		UI_GENERAL_XHDPI.mParameters = fontParameter;

	}
}
