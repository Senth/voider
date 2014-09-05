package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.UiFactory.Positions;
import com.spiddekauga.utils.scene.ui.UiFactory.TextButtonStyles;
import com.spiddekauga.voider.Config.Community;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for tagging resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class TagSceneGui extends Gui {
	/**
	 * Set scene for GUI
	 * @param scene tag scene
	 */
	void setScene(TagScene scene) {
		mScene = scene;
	}

	@Override
	public void initGui() {
		super.initGui();

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
				protected void onPressed() {
					mScene.setTag(tag);
					mScene.continueToNextScene();
				}
			};
			mUiFactory.addTextButton(tag.toString(), TextButtonStyles.TAG, mMainTable, listener, null, null);
		}
	}

	/**
	 * Init continue button
	 */
	private void initContinue() {
		mMainTable.row().setHeight(mUiFactory.getStyles().vars.rowHeight);
		mMainTable.row();

		Button button = mUiFactory.addImageButtonLabel(SkinNames.General.GAME_CONTINUE, "Skip", Positions.BOTTOM, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mScene.continueToNextScene();
			};
		};
	}

	/** Tag scene */
	private TagScene mScene = null;
}
