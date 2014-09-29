package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Messages;

/**
 * GUI for enemy editor
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class EditorSelectionGui extends MenuGui {
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
		Skin skin = ResourceCacheFacade.get(InternalNames.UI_EDITOR);

		// Button button = new ImageButton(skin,
		// SkinNames.EditorIcons.CAMPAIGN_EDITOR_BIG.toString());
		// new TooltipListener(button,
		// Messages.replaceName(Messages.Tooltip.Menus.Editor.CAMPAIGN, "campaign"));
		// new ButtonListener(button) {
		// @Override
		// protected void onPressed(Button button) {
		// mMenuScene.gotoCampaignEditor();
		// }
		// };
		// mMainTable.add(button);

		Button button = new ImageButton(skin, SkinNames.EditorIcons.LEVEL_EDITOR_BIG.toString());
		new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.Editor.LEVEL, "level"));
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoLevelEditor();
			}
		};
		mMainTable.add(button);

		button = new ImageButton(skin, SkinNames.EditorIcons.ENEMY_EDITOR_BIG.toString());
		new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.Editor.ENEMY, "enemy"));
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoEnemyEditor();
			}
		};
		mMainTable.add(button);

		button = new ImageButton(skin, SkinNames.EditorIcons.BULLET_EDITOR_BIG.toString());
		new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.Editor.BULLET, "bullet"));
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoBulletEditor();
			}
		};
		mMainTable.add(button);
	}
}
