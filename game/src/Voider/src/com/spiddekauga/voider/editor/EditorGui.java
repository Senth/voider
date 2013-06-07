package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.CommandSequence;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.app.MainMenu;
import com.spiddekauga.voider.editor.commands.CEditorDuplicate;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorSave;
import com.spiddekauga.voider.editor.commands.CSceneReturn;
import com.spiddekauga.voider.editor.commands.CSceneSwitch;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Messages.UnsavedActions;

/**
 * Common methods for all editors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class EditorGui extends Gui {
	@Override
	public void dispose() {
		mMainMenuTable.dispose();

		super.dispose();
	}

	/**
	 * Initializes the main menu that is shown when pressed back
	 * @param editor the current editor
	 * @param defTypeName the definition type name to save is unsaved, etc.
	 */
	protected void initMainMenu(final IEditor editor, final String defTypeName) {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		final TextButtonStyle textStyle = generalSkin.get("default", TextButtonStyle.class);

		mMainMenuTable.setRowAlign(Horizontal.CENTER, Vertical.MIDDLE);

		mMainMenuTable.row();
		Button button = new TextButton("New", textStyle);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				if (editor.isUnsaved()) {
					Button yes = new TextButton("Save first", textStyle);
					Button no = new TextButton("Discard current", textStyle);

					Command save = new CEditorSave(editor);
					Command newCommand = new CEditorNew(editor);
					Command saveAndNew = new CommandSequence(save, newCommand);

					MsgBoxExecuter msgBox = getFreeMsgBox();

					msgBox.clear();
					msgBox.setTitle("New Enemy");
					msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.NEW));
					msgBox.button(yes, saveAndNew);
					msgBox.button(no, newCommand);
					msgBox.addCancelButtonAndKeys();
					showMsgBox(msgBox);
				} else {
					editor.newDef();
				}
			}
		};
		mMainMenuTable.add(button);

		// Save
		mMainMenuTable.row();
		button = new TextButton("Save", textStyle);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				editor.saveDef();
			}
		};
		mMainMenuTable.add(button);

		// Load
		mMainMenuTable.row();
		button = new TextButton("Load", textStyle);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				if (editor.isUnsaved()) {
					Button yes = new TextButton("Save first", textStyle);
					Button no = new TextButton("Load anyway", textStyle);

					CommandSequence saveAndLoad = new CommandSequence(new CEditorSave(editor), new CEditorLoad(editor));

					MsgBoxExecuter msgBox = getFreeMsgBox();

					msgBox.clear();
					msgBox.setTitle("Load Enemy");
					msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.LOAD));
					msgBox.button(yes, saveAndLoad);
					msgBox.button(no, new CEditorLoad(editor));
					msgBox.addCancelButtonAndKeys();
					showMsgBox(msgBox);
				} else {
					editor.loadDef();
				}
			}
		};
		mMainMenuTable.add(button);

		// Duplicate
		mMainMenuTable.row();
		button = new TextButton("Duplicate", textStyle);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				if (editor.isUnsaved()) {
					Button yes = new TextButton("Save first", textStyle);
					Button no = new TextButton("Duplicate anyway", textStyle);

					CommandSequence saveAndDuplicate = new CommandSequence(new CEditorSave(editor), new CEditorDuplicate(editor));

					MsgBoxExecuter msgBox = getFreeMsgBox();

					msgBox.clear();
					msgBox.setTitle("Duplicate Enemy");
					msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.DUPLICATE));
					msgBox.button(yes, saveAndDuplicate);
					msgBox.button(no, new CEditorDuplicate(editor));
					msgBox.addCancelButtonAndKeys();
					showMsgBox(msgBox);
				} else {
					editor.duplicateDef();
				}
			}
		};
		mMainMenuTable.add(button);


		// Switch editors
		mMainMenuTable.row().setPadTop(Config.Gui.SEPARATE_PADDING);

		// Level editor
		if (editor.getClass() != LevelEditor.class) {
			mMainMenuTable.row();
			button = new TextButton("Level Editor", textStyle);
			mMainMenuTable.add(button);
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Level Editor", new CSceneSwitch(LevelEditor.class), UnsavedActions.LEVEL_EDITOR, editor, defTypeName);
				}
			};
		}

		// Enemy editor
		if (editor.getClass() != EnemyEditor.class) {
			mMainMenuTable.row();
			button = new TextButton("Enemy Editor", textStyle);
			mMainMenuTable.add(button);
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Enemy Editor", new CSceneSwitch(EnemyEditor.class), UnsavedActions.ENEMY_EDITOR, editor, defTypeName);
				}
			};
		}

		// Bullet editor
		if (editor.getClass() != BulletEditor.class) {
			mMainMenuTable.row();
			button = new TextButton("Bullet Editor", textStyle);
			mMainMenuTable.add(button);
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Bullet Editor", new CSceneSwitch(BulletEditor.class), UnsavedActions.BULLET_EDITOR, editor, defTypeName);
				}
			};
		}

		// Return to main menu
		mMainMenuTable.row();
		button = new TextButton("Main Menu", textStyle);
		mMainMenuTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				switchReturnTo("Main Menu", new CSceneReturn(MainMenu.class), UnsavedActions.MAIN_MENU, editor, defTypeName);
			}
		};
	}

	/**
	 * Switches or returns to the specified editor or menu.
	 * @param switchReturnTo what we're switching or returning to.
	 * @param command the command to be executed when switching or returning
	 * @param unsavedAction message to be displayed in the message box
	 * @param editor the editor this GUI is bound to
	 * @param defTypeName name of the editor.
	 */
	private void switchReturnTo(String switchReturnTo, Command command, UnsavedActions unsavedAction, IEditor editor, String defTypeName) {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		final TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);

		boolean switching = command instanceof CSceneSwitch;

		if (editor.isUnsaved()) {
			String yesMessage = switching ? "Save then switch" : "Save then return";
			String noMessage = switching ? "Switch anyway" : "Return anymay";
			Button yes = new TextButton(yesMessage, textStyle);
			Button no = new TextButton(noMessage, textStyle);
			Button cancel = new TextButton("Cancel", textStyle);

			Command save = new CEditorSave(editor);
			Command saveAndNew = new CommandSequence(save, command);

			String msgBoxTitle = switching ? "Switch" : "Return";
			msgBoxTitle += " to " + switchReturnTo;
			MsgBoxExecuter msgBox = getFreeMsgBox();

			msgBox.clear();
			msgBox.setTitle(msgBoxTitle);
			msgBox.content(Messages.getUnsavedMessage(defTypeName, unsavedAction));
			msgBox.button(yes, saveAndNew);
			msgBox.button(no, command);
			msgBox.button(cancel);
			msgBox.key(Keys.BACK, null);
			msgBox.key(Keys.ESCAPE, null);
			showMsgBox(msgBox);
		} else {
			command.execute();
		}
	}

	/**
	 * Shows the main menu of the level editor
	 */
	void showMainMenu() {
		MsgBoxExecuter msgBox = getFreeMsgBox();

		msgBox.content(mMainMenuTable);
		msgBox.button("Return");
		msgBox.key(Keys.BACK, null);
		msgBox.key(Keys.ESCAPE, null);
		showMsgBox(msgBox);
	}

	/** Main menu table */
	protected AlignTable mMainMenuTable = new AlignTable();
}
