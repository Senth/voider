package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;

/**
 * GUI for enemy editor
 */
class EditorSelectGui extends MenuGui {
private EditorSelectScene mScene = null;

/**
 * Set the editor selection scene
 * @param scene
 */
void setScene(EditorSelectScene scene) {
	mScene = scene;
}

@Override
public void initGui() {
	super.initGui();

	mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

	initMenu();

	addBackButton();
}

/**
 * Initializes the menu
 */
private void initMenu() {
	Button button = mUiFactory.button.addImageWithLabel(SkinNames.General.EDITOR_LEVEL_BIG, "Level", Positions.BOTTOM, null, mMainTable, null,
			null);
	mUiFactory.button.addSound(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mScene.gotoLevelEditor();
		}
	};

	button = mUiFactory.button.addImageWithLabel(SkinNames.General.EDITOR_ENEMY_BIG, "Enemy", Positions.BOTTOM, null, mMainTable, null, null);
	mUiFactory.button.addSound(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mScene.gotoEnemyEditor();
		}
	};
	mUiFactory.button.addPadding(mMainTable);

	button = mUiFactory.button.addImageWithLabel(SkinNames.General.EDITOR_BULLET_BIG, "Bullet", Positions.BOTTOM, null, mMainTable, null, null);
	mUiFactory.button.addSound(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mScene.gotoBulletEditor();
		}
	};
	mUiFactory.button.addPadding(mMainTable);

	if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY_RELEASE)) {
		button = mUiFactory.button.addImageWithLabel(SkinNames.General.EDITOR_SHIP_BIG, "Ship", Positions.BOTTOM, null, mMainTable, null, null);
		mUiFactory.button.addSound(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScene.gotoShipEditor();
			}
		};
		mUiFactory.button.addPadding(mMainTable);
	}
}
}
