package com.spiddekauga.voider.editor;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Messages;

/**
 * GUI for enemy editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EditorSelectionGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setTableAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setRowAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setCellPaddingDefault(Config.Gui.PADDING_DEFAULT);

		initMenu();
	}

	/**
	 * Sets the editor selection scene
	 * @param editorSelectionScene the editor selection scene bound to this GUI.
	 */
	void setEditorSelectionScene(EditorSelectionScene editorSelectionScene) {
		mEditorSelectionScene = editorSelectionScene;
	}

	/**
	 * Initializes the menu
	 */
	private void initMenu() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		Button button = new ImageButton(skin, SkinNames.EditorIcons.LEVEL_EDITOR.toString());
		TooltipListener tooltipListener = new TooltipListener(button, "Level Editor", Messages.replaceName(Messages.Tooltip.Menus.Editor.LEVEL_EDITOR,  "level"));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mEditorSelectionScene.gotoLevelEditor();
			}
		};
		mMainTable.add(button);

		button = new ImageButton(skin, SkinNames.EditorIcons.ENEMY_EDITOR.toString());
		tooltipListener = new TooltipListener(button, "Enemy Editor", Messages.replaceName(Messages.Tooltip.Menus.Editor.ENEMY_EDITOR,  "enemy"));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mEditorSelectionScene.gotoLevelEditor();
			}
		};
		mMainTable.add(button);

		button = new ImageButton(skin, SkinNames.EditorIcons.BULLET_EDITOR.toString());
		tooltipListener = new TooltipListener(button, "Bullet Editor", Messages.replaceName(Messages.Tooltip.Menus.Editor.BULLET_EDITOR,  "bullet"));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mEditorSelectionScene.gotoLevelEditor();
			}
		};
		mMainTable.add(button);
	}

	/** Editor selection scene */
	private EditorSelectionScene mEditorSelectionScene = null;
}
