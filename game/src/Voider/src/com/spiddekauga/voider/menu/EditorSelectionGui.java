package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.resources.SkinNames;
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
	}

	/**
	 * Initializes the menu
	 */
	private void initMenu() {
		Button button = mUiFactory.addImageButtonLabel(SkinNames.General.EDITOR_LEVEL_BIG, "Level", Positions.BOTTOM, null, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoLevelEditor();
			}
		};

		button = mUiFactory.addImageButtonLabel(SkinNames.General.EDITOR_ENEMY_BIG, "Enemy", Positions.BOTTOM, null, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoEnemyEditor();
			}
		};
		mUiFactory.addButtonPadding(mMainTable);

		button = mUiFactory.addImageButtonLabel(SkinNames.General.EDITOR_BULLET_BIG, "Bullet", Positions.BOTTOM, null, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoBulletEditor();
			}
		};
		mUiFactory.addButtonPadding(mMainTable);
	}
}
