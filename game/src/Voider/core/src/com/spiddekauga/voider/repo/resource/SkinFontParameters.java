package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.SkinLoader.SkinParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.ObjectMap;


/**
 * Generate font bitmaps for skins.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class SkinFontParameter extends SkinParameter implements IParameterGenerate {
	/**
	 * Default constructor
	 */
	SkinFontParameter() {
		super(new ObjectMap<String, Object>());
	}

	/**
	 * Add a font with various sizes
	 * @param font name/file of the font
	 * @param fontName name of the font in the skin file. Will be skinName16 for size 16.
	 * @param sizes all sizes the font should have
	 */
	void addFont(InternalNames font, String fontName, int... sizes) {
		mFonts.add(new FontWrapper(font, fontName, sizes));
	}

	/**
	 * Generate fonts and add these to the parameter. Will only do this once.
	 */
	@Override
	public void generate() {
		for (FontWrapper fontWrapper : mFonts) {
			generate(fontWrapper);
		}
	}


	/**
	 * Generate all sizes for a specified font
	 * @param fontWrapper the font to generate the bitmaps for
	 */
	private void generate(FontWrapper fontWrapper) {
		FileHandle file = Gdx.files.internal(fontWrapper.font.getFilePath());
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(file);

		for (int i = 0; i < fontWrapper.sizes.length; i++) {
			String name = fontWrapper.skinName + fontWrapper.sizes[i];
			mFontParameter.size = fontWrapper.sizes[i];
			BitmapFont bitmapFont = generator.generateFont(mFontParameter);
			resources.put(name, bitmapFont);
		}

		generator.dispose();
	}

	/**
	 * Wrapper for font name, size
	 */
	private class FontWrapper {
		FontWrapper(InternalNames font, String skinName, int... sizes) {
			this.font = font;
			this.skinName = skinName;
			this.sizes = sizes;
		}

		InternalNames font;
		String skinName;
		int[] sizes;
	}

	private FreeTypeFontParameter mFontParameter = new FreeTypeFontParameter();
	private ArrayList<FontWrapper> mFonts = new ArrayList<>();
}
