package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.Config.Community;
import com.spiddekauga.voider.network.stat.Tags;
import com.spiddekauga.utils.scene.ui.Gui;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

import java.util.ArrayList;

/**
 * GUI for tagging resources
 */
class TagSceneGui extends Gui {
/** Tag scene */
private TagScene mScene = null;

/**
 * Set scene for GUI
 * @param scene tag scene
 */
void setScene(TagScene scene) {
	mScene = scene;
}

@Override
public void onCreate() {
	super.onCreate();

	mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

	initTags();
	initContinue();
}

/**
 * Init tags
 */
private void initTags() {
	ArrayList<Tags> tags = mScene.getRandomTags();

	for (int i = 0; i < tags.size(); ++i) {
		// Add new row
		if (i % Community.TAGS_PER_ROW == 0) {
			mMainTable.row();
		}

		final Tags tag = tags.get(i);

		// Add tag
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setTag(tag);
				mScene.continueToNextScene();
			}
		};
		mUiFactory.button.addText(tag.toString(), TextButtonStyles.TAG, mMainTable, listener, null, null);
	}
}

/**
 * Init continue button
 */
private void initContinue() {
	mMainTable.row().setHeight(mUiFactory.getStyles().vars.rowHeight);
	mMainTable.row();

	ButtonListener listener = new ButtonListener() {
		@Override
		protected void onPressed(Button button) {
			mScene.continueToNextScene();
		}

		;
	};
	mUiFactory.button.addText("Skip", TextButtonStyles.FILLED_PRESS, mMainTable, listener, null, null);
}
}
