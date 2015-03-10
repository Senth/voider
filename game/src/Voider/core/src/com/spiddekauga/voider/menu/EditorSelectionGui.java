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
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class EditorSelectionGui extends MenuGui {
	/**
	 * Public constructor
	 */
	public EditorSelectionGui() {
		// Does nothing
	}

	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		initMenu();
		initBackButton();
	}

	private void initBackButton() {
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.popMenu();
			}
		};

		addActor(mUiFactory.button.createBackButton(buttonListener));
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
				mMenuScene.gotoLevelEditor();
			}
		};

		button = mUiFactory.button.addImageWithLabel(SkinNames.General.EDITOR_ENEMY_BIG, "Enemy", Positions.BOTTOM, null, mMainTable, null, null);
		mUiFactory.button.addSound(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoEnemyEditor();
			}
		};
		mUiFactory.button.addPadding(mMainTable);

		button = mUiFactory.button.addImageWithLabel(SkinNames.General.EDITOR_BULLET_BIG, "Bullet", Positions.BOTTOM, null, mMainTable, null, null);
		mUiFactory.button.addSound(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoBulletEditor();
			}
		};
		mUiFactory.button.addPadding(mMainTable);

		if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY_RELEASE)) {
			button = mUiFactory.button.addImageWithLabel(SkinNames.General.EDITOR_SHIP_BIG, "Ship", Positions.BOTTOM, null, mMainTable, null, null);
			mUiFactory.button.addSound(button);
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					mMenuScene.gotoShipEditor();
				}
			};
			mUiFactory.button.addPadding(mMainTable);
		}
	}
}
