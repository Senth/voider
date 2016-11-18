package com.spiddekauga.voider.app;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.utils.scene.ui.Gui;

import net._01001111.text.LoremIpsum;

/**
 * GUI for the prototype
 */
public class PrototypeGui extends Gui {
private static final String TEXT;
private static final String[] FONTS = {"small_bold", "small_black", "medium_bold", "medium_black", "large", "large_bold", "large_black", "huge",
		"huge_bold", "huge_black"};

static {
	LoremIpsum loremIpsum = new LoremIpsum();
	TEXT = loremIpsum.sentences(1);
}

@Override
public void onDestroy() {
	super.onDestroy();
}

@Override
public void onCreate() {
	super.onCreate();

	initFonts();
}

/**
 * Initialize fonts
 */
private void initFonts() {
	mMainTable.setAlign(Horizontal.LEFT, Vertical.TOP);

	for (String fontName : FONTS) {
		Skin skin = ResourceCacheFacade.get(InternalDeps.UI_GENERAL);
		BitmapFont font = skin.getFont(fontName);
		LabelStyle labelStyle = new LabelStyle(font, Color.WHITE);
		mMainTable.add(new Label(fontName + "    " + TEXT, labelStyle));
		mMainTable.row();
	}
}
}
