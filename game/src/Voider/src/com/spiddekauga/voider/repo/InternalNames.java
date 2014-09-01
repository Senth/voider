package com.spiddekauga.voider.repo;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
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
	/** Icon 256x256 */
	ICON_256("icon-256.png", Texture.class),
	/** Icon 64x64 */
	ICON_64("icon-64.png", Texture.class),
	/** Icon 32x32 */
	ICON_32("icon-32.png", Texture.class),

	// Shaders
	/** Default vertex shader */
	SHADER_DEFAULT("default", ShaderProgram.class),

	;

	/**
	 * Initializes the enum with a filename
	 * @param filename the filename (not full path)
	 * @param type the class type of resource
	 */
	private InternalNames(String filename, Class<?> type) {
		this.filename = filename;
		this.type = type;
	}

	/**
	 * @return file path of this resource
	 */
	public String getFilePath() {
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
	private static String SOUND_PATH = "sound/";

	/**
	 * Initializes the resource names
	 */
	static {
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
			}


			mResourcePaths.put(Texture.class, TEXTURE_PATH);
			mResourcePaths.put(ShaderProgram.class, SHADER_PATH);
			mResourcePaths.put(ParticleEffect.class, PARTICLE_PATH);
			mResourcePaths.put(Skin.class, UI_PATH);
			mResourcePaths.put(Sound.class, SOUND_PATH);
		}
	}
}
