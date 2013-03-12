package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.CommandSequence;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorSave;
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
	/**
	 * Initializes the main menu that is shown when pressed back
	 * @param editor the current editor
	 * @param defTypeName the definition type name to save is unsaved, etc.
	 */
	protected void initMainMenu(final IEditor editor, final String defTypeName) {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		final TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);

		mMainMenuTable.setRowAlign(Horizontal.CENTER, Vertical.MIDDLE);

		Button button = new TextButton("New", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (editor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Discard current", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						Command save = new CEditorSave(editor);
						Command newCommand = new CEditorNew(editor);
						Command saveAndNew = new CommandSequence(save, newCommand);

						MsgBoxExecuter msgBox = getFreeMsgBox();

						msgBox.clear();
						msgBox.setTitle("New Enemy");
						msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.NEW));
						msgBox.button(yes, saveAndNew);
						msgBox.button(no, newCommand);
						msgBox.button(cancel);
						msgBox.key(Keys.BACK, null);
						msgBox.key(Keys.ESCAPE, null);
						showMsgBox(msgBox);
					} else {
						editor.newDef();
					}
				}
				return true;
			}
		});
		mMainMenuTable.add(button);

		mMainMenuTable.row();
		button = new TextButton("Save", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					editor.saveDef();
				}
				return true;
			}
		});
		mMainMenuTable.add(button);

		mMainMenuTable.row();
		button = new TextButton("Load", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (editor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Load anyway", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						Command save = new CEditorSave(editor);
						Command load = new CEditorLoad(editor);
						Command saveAndLoad = new CommandSequence(save, load);

						MsgBoxExecuter msgBox = getFreeMsgBox();

						msgBox.clear();
						msgBox.setTitle("Load Enemy");
						msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.LOAD));
						msgBox.button(yes, saveAndLoad);
						msgBox.button(no, load);
						msgBox.button(cancel);
						msgBox.key(Keys.BACK, null);
						msgBox.key(Keys.ESCAPE, null);
						showMsgBox(msgBox);
					} else {
						editor.loadDef();
					}
				}
				return true;
			}
		});
		mMainMenuTable.add(button);

		mMainMenuTable.row();
		button = new TextButton("Duplicate", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (editor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Duplicate anyway", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						Command save = new CEditorSave(editor);
						Command newCommand = new CEditorNew(editor);
						Command saveAndNew = new CommandSequence(save, newCommand);

						MsgBoxExecuter msgBox = getFreeMsgBox();

						msgBox.clear();
						msgBox.setTitle("New Level");
						msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.NEW));
						msgBox.button(yes, saveAndNew);
						msgBox.button(no, newCommand);
						msgBox.button(cancel);
						msgBox.key(Keys.BACK, null);
						msgBox.key(Keys.ESCAPE, null);
						showMsgBox(msgBox);
					} else {
						editor.duplicateDef();
					}
				}
				return true;
			}
		});
		mMainMenuTable.add(button);


		// Switch editors
		mMainMenuTable.row().setPadTop(Config.Gui.SEPARATE_PADDING);

		// Level editor
		if (editor.getClass() != LevelEditor.class) {
			mMainMenuTable.row();
			button = new TextButton("Level Editor", textStyle);
			mMainMenuTable.add(button);
			button.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (isButtonPressed(event)) {
						if (editor.isUnsaved()) {
							Button yes = new TextButton("Save then switch", textStyle);
							Button no = new TextButton("Switch anyway", textStyle);
							Button cancel = new TextButton("Cancel", textStyle);

							Command save = new CEditorSave(editor);
							Command switchCommand = new CSceneSwitch(LevelEditor.class);
							Command saveAndNew = new CommandSequence(save, switchCommand);

							MsgBoxExecuter msgBox = getFreeMsgBox();

							msgBox.clear();
							msgBox.setTitle("Switch to Level Editor");
							msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.ENEMY_EDITOR));
							msgBox.button(yes, saveAndNew);
							msgBox.button(no, switchCommand);
							msgBox.button(cancel);
							msgBox.key(Keys.BACK, null);
							msgBox.key(Keys.ESCAPE, null);
							showMsgBox(msgBox);
						} else {
							Command switchCommand = new CSceneSwitch(LevelEditor.class);
							switchCommand.execute();
						}
					}
					return true;
				}
			});
		}

		// Enemy editor
		if (editor.getClass() != EnemyEditor.class) {
			mMainMenuTable.row();
			button = new TextButton("Enemy Editor", textStyle);
			mMainMenuTable.add(button);
			button.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (isButtonPressed(event)) {
						if (editor.isUnsaved()) {
							Button yes = new TextButton("Save then switch", textStyle);
							Button no = new TextButton("Switch anyway", textStyle);
							Button cancel = new TextButton("Cancel", textStyle);

							Command save = new CEditorSave(editor);
							Command switchCommand = new CSceneSwitch(EnemyEditor.class);
							Command saveAndNew = new CommandSequence(save, switchCommand);

							MsgBoxExecuter msgBox = getFreeMsgBox();

							msgBox.clear();
							msgBox.setTitle("Switch to Enemy Editor");
							msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.ENEMY_EDITOR));
							msgBox.button(yes, saveAndNew);
							msgBox.button(no, switchCommand);
							msgBox.button(cancel);
							msgBox.key(Keys.BACK, null);
							msgBox.key(Keys.ESCAPE, null);
							showMsgBox(msgBox);
						} else {
							Command switchCommand = new CSceneSwitch(EnemyEditor.class);
							switchCommand.execute();
						}
					}
					return true;
				}
			});
		}

		// Bullet editor
		if (editor.getClass() != BulletEditor.class) {
			mMainMenuTable.row();
			button = new TextButton("Bullet Editor", textStyle);
			mMainMenuTable.add(button);
			button.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (isButtonPressed(event)) {
						if (editor.isUnsaved()) {
							Button yes = new TextButton("Save then switch", textStyle);
							Button no = new TextButton("Switch anyway", textStyle);
							Button cancel = new TextButton("Cancel", textStyle);

							Command save = new CEditorSave(editor);
							Command switchCommand = new CSceneSwitch(BulletEditor.class);
							Command saveAndNew = new CommandSequence(save, switchCommand);

							MsgBoxExecuter msgBox = getFreeMsgBox();

							msgBox.clear();
							msgBox.setTitle("Switch to Bullet Editor");
							msgBox.content(Messages.getUnsavedMessage(defTypeName, UnsavedActions.ENEMY_EDITOR));
							msgBox.button(yes, saveAndNew);
							msgBox.button(no, switchCommand);
							msgBox.button(cancel);
							msgBox.key(Keys.BACK, null);
							msgBox.key(Keys.ESCAPE, null);
							showMsgBox(msgBox);
						} else {
							Command switchCommand = new CSceneSwitch(BulletEditor.class);
							switchCommand.execute();
						}
					}
					return true;
				}
			});
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
