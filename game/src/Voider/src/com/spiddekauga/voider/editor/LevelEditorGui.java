package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.HideSliderValue;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.ResourceTextureButton;
import com.spiddekauga.utils.scene.ui.SelectBoxListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Level;
import com.spiddekauga.voider.editor.LevelEditor.Tools;
import com.spiddekauga.voider.editor.commands.CDefHasValidName;
import com.spiddekauga.voider.editor.commands.CGuiSlider;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.EditorIcons;
import com.spiddekauga.voider.resources.SkinNames.General;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * GUI for the level editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class LevelEditorGui extends EditorGui {
	/**
	 * Sets the level editor this GUI will act on.
	 * 
	 * @param levelEditor
	 *            the scene this GUI will act on
	 */
	public void setLevelEditor(LevelEditor levelEditor) {
		mLevelEditor = levelEditor;
		mInvoker = mLevelEditor.getInvoker();

		setEditor(mLevelEditor);
	}

	@Override
	public void initGui() {
		super.initGui();


		mPickupTable.setPreferences(mMainTable);
		mWidgets.enemy.table.setPreferences(mMainTable);
		mWidgets.info.table.setPreferences(mMainTable);

		mHiders = new Hiders();

		initToolMenu();
		initInfo();
		// initPickup();
		initPathOptions();
		initEnemyOptions();
		initEnemyAddOptions();
	}

	@Override
	public void dispose() {
		mWidgets.enemy.table.dispose();
		mWidgets.path.table.dispose();
		mWidgets.info.table.dispose();

		mHiders.dispose();

		super.dispose();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetPathOptions();
		resetEnemyOptions();
		resetLevelInfo();
		resetEnemyAddTable();
	}

	/**
	 * Reset level info
	 */
	void resetLevelInfo() {
		mWidgets.info.description.setText(mLevelEditor.getLevelDescription());
		mWidgets.info.name.setText(mLevelEditor.getLevelName());
		mWidgets.info.revision.setText(mLevelEditor.getLevelRevision());
		mWidgets.info.storyBefore.setText(mLevelEditor.getPrologue());
		mWidgets.info.epilogue.setText(mLevelEditor.getEpilogue());
		mWidgets.info.speed.setValue(mLevelEditor.getLevelStartingSpeed());
		mWidgets.info.theme.setSelection(mLevelEditor.getTheme().ordinal());

	}

	/**
	 * Reset path options
	 */
	void resetPathOptions() {
		if (mLevelEditor.isPathSelected()) {
			mHiders.pathOptions.show();

			if (mLevelEditor.getPathType() != null) {
				switch (mLevelEditor.getPathType()) {
				case BACK_AND_FORTH:
					mWidgets.path.backAndForth.setChecked(true);
					break;

				case LOOP:
					mWidgets.path.loop.setChecked(true);
					break;

				case ONCE:
					mWidgets.path.once.setChecked(true);
					break;
				}
			}
			// No paths selected or they have different path types -> Uncheck all
			else {
				mWidgets.path.backAndForth.setChecked(false);
				mWidgets.path.loop.setChecked(false);
				mWidgets.path.once.setChecked(false);
			}
		}
		else {
			mHiders.pathOptions.hide();
		}
	}

	/**
	 * Reset enemy add table
	 */
	void resetEnemyAddTable() {
		ArrayList<EnemyActorDef> enemyDefs = mLevelEditor.getAddEnemies();

		mWidgets.enemyAdd.scrollTable.clear();

		int cColumEnemy = 0;
		GuiCheckCommandCreator guiCheckCommandCreator = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		for (EnemyActorDef enemyDef : enemyDefs) {
			Button button = new ResourceTextureButton(enemyDef, mStyles.skin.general, SkinNames.General.IMAGE_BUTTON_TOGGLE.toString());

			int enemiesPerColumn = getEnemiesPerColumnInAddTable();

			if (cColumEnemy == enemiesPerColumn) {
				cColumEnemy = 0;
				mWidgets.enemyAdd.scrollTable.row();
			}

			TooltipListener tooltipListener = new TooltipListener(button, enemyDef.getName(), enemyDef.getDescription());
			new ButtonListener(button, tooltipListener) {
				@Override
				protected void onChecked(boolean checked) {
					if (checked) {
						mLevelEditor.createNewEnemy((EnemyActorDef) ((ResourceTextureButton) mButton).getResource());
					}
				}
			};
			button.addListener(guiCheckCommandCreator);
			buttonGroup.add(button);

			mWidgets.enemyAdd.scrollTable.add(button).size(Config.Editor.Level.Enemy.ADD_BUTTON_SIZE);
			cColumEnemy++;
		}
	}

	/**
	 * Resets enemy option values
	 */
	void resetEnemyOptions() {
		// Scroll pane width
		int scrollPaneWidth = Config.Editor.Level.Enemy.ADD_BUTTON_SIZE * getEnemiesPerColumnInAddTable();
		mWidgets.enemyAdd.scrollTable.setWidth(scrollPaneWidth);
		// Add margin
		scrollPaneWidth += 10;
		mWidgets.enemyAdd.scrollPane.setWidth(scrollPaneWidth);

		// Show/Hide options
		if (mLevelEditor.isEnemySelected()) {
			mHiders.enemyOptions.show();

			// Update enemy count slider values
			if (mWidgets.enemy.cEnemies.getValue() != mLevelEditor.getEnemyCount()) {
				mInvoker.execute(new CGuiSlider(mWidgets.enemy.cEnemies, mLevelEditor.getEnemyCount(), mWidgets.enemy.cEnemies.getValue()), true);
			}

			// Update enemy delay slider values
			if (mLevelEditor.getEnemySpawnDelay() >= 0 && mLevelEditor.getEnemySpawnDelay() != mWidgets.enemy.betweenDelay.getValue()) {
				mInvoker.execute(
						new CGuiSlider(mWidgets.enemy.betweenDelay, mLevelEditor.getEnemySpawnDelay(), mWidgets.enemy.betweenDelay.getValue()), true);
			}

			// Has activate trigger -> Show trigger delay
			if (mLevelEditor.hasSelectedEnemyActivateTrigger()) {
				mHiders.enemyActivateDelay.show();

				float activateDelay = mLevelEditor.getSelectedEnemyActivateTriggerDelay();
				if (activateDelay >= 0 && activateDelay != mWidgets.enemy.activateDelay.getValue()) {
					mInvoker.execute(new CGuiSlider(mWidgets.enemy.activateDelay, activateDelay, mWidgets.enemy.activateDelay.getValue()));
				}
			}
			else {
				mHiders.enemyActivateDelay.hide();
			}


			// Has deactivate trigger -> Show trigger delay
			if (mLevelEditor.hasSelectedEnemyDeactivateTrigger()) {
				mHiders.enemyDeactivateDelay.show();

				float deactivateDelay = mLevelEditor.getSelectedEnemyDeactivateTriggerDelay();
				if (deactivateDelay >= 0 && deactivateDelay != mWidgets.enemy.deactivateDelay.getValue()) {
					mInvoker.execute(new CGuiSlider(mWidgets.enemy.deactivateDelay, deactivateDelay, mWidgets.enemy.deactivateDelay.getValue()));
				}
			}
			else {
				mHiders.enemyDeactivateDelay.hide();
			}

		}
		else {
			mHiders.enemyOptions.hide();
		}
	}

	@Override
	protected void showInfoDialog() {
		resetLevelInfo();
		MsgBoxExecuter msgBox = getFreeMsgBox(true);
		msgBox.setTitle("Level options");
		msgBox.content(mWidgets.info.table);
		msgBox.addCancelOkButtonAndKeys("OK", new CDefHasValidName(msgBox, this, mLevelEditor, getResourceTypeName()));
		showMsgBox(msgBox);
	}

	/**
	 * Initializes the tool menu
	 */
	private void initToolMenu() {
		Button button;

		@SuppressWarnings("unchecked")
		ArrayList<Button> toolButtons = Pools.arrayList.obtain();

		// Select
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.SELECT.toString());
		}
		TooltipListener tooltipListener = new TooltipListener(button, "Select", Messages.replaceName(Messages.Tooltip.Tools.SELECT,
				getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.SELECTION);
				}
			}
		};
		toolButtons.add(button);

		// Cancel
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Cancel", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.CANCEL.toString());
		}
		tooltipListener = new TooltipListener(button, "Cancel", Messages.replaceName(Messages.Tooltip.Tools.CANCEL, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.clearSelection();
				}
			}
		};
		toolButtons.add(button);

		// Move
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.MOVE.toString());
		}
		tooltipListener = new TooltipListener(button, "Move", Messages.replaceName(Messages.Tooltip.Tools.MOVE, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.MOVE);
				}
			}
		};
		toolButtons.add(button);

		// Delete
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Delete", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.DELETE.toString());
		}
		tooltipListener = new TooltipListener(button, "Delete", Messages.replaceName(Messages.Tooltip.Tools.DELETE, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.DELETE);
				}
			}
		};
		toolButtons.add(button);

		// Terrain draw_append
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Terrain, append", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.TERRAIN_DRAW_APPEND.toString());
		}
		tooltipListener = new TooltipListener(button, "Terrain, append", Messages.replaceName(Messages.Tooltip.Tools.DRAW_APPEND, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_DRAW_APPEND);
				}
			}
		};
		toolButtons.add(button);

		// Terrain draw_erase
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Terrain, draw erase", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.TERRAIN_DRAW_ERASE.toString());
		}
		tooltipListener = new TooltipListener(button, "Terrain, draw erase", Messages.replaceName(Messages.Tooltip.Tools.DRAW_ERASE, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_DRAW_ERASE);
				}
			}
		};
		toolButtons.add(button);

		// add_move_corner
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add/move corner", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.ADD_MOVE_CORNER.toString());
		}
		tooltipListener = new TooltipListener(button, "Add/move corner", Messages.replaceName(Messages.Tooltip.Tools.ADJUST_ADD_MOVE_CORNER,
				"terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ADD_MOVE_CORNER);
				}
			}
		};
		toolButtons.add(button);

		// remove_corner
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Remove corner", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.REMOVE_CORNER.toString());
		}
		tooltipListener = new TooltipListener(button, "Remove corner", Messages.replaceName(Messages.Tooltip.Tools.ADJUST_REMOVE_CORNER, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.REMOVE_CORNER);
				}
			}
		};
		toolButtons.add(button);

		// Path add
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add path", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.PATH_ADD.toString());
		}
		tooltipListener = new TooltipListener(button, "Add path", Messages.replaceName(Messages.Tooltip.Tools.PATH_ADD, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PATH_ADD_CORNER);
				}
			}
		};
		toolButtons.add(button);

		// Enemy add
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add enemy", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.ENEMY_ADD.toString());
		}
		mWidgets.tool.enemyAdd = button;
		tooltipListener = new TooltipListener(button, "Add enemy", Messages.replaceName(Messages.Tooltip.Tools.ENEMY_ADD, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_ADD);
				}
			}
		};
		toolButtons.add(button);

		// Enemy - set activate trigger
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Set activate trigger", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.ENEMY_SET_ACTIVATE_TRIGGER.toString());
		}
		tooltipListener = new TooltipListener(button, "Set activate trigger", Messages.replaceName(Messages.Tooltip.Tools.SET_ACTIVATE_TRIGGER,
				getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_SET_ACTIVATE_TRIGGER);
				}
			}
		};
		toolButtons.add(button);

		// Enemy - set deactivate trigger
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Set deactivate trigger", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.ENEMY_SET_DEACTIVATE_TRIGGER.toString());
		}
		tooltipListener = new TooltipListener(button, "Set deactivate trigger", Messages.replaceName(Messages.Tooltip.Tools.SET_DEACTIVATE_DELAY,
				getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_SET_DEACTIVATE_TRIGGER);
				}
			}
		};
		toolButtons.add(button);

		// Trigger add
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add trigger", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.TRIGGER_ADD.toString());
		}
		tooltipListener = new TooltipListener(button, "Add Trigger", Messages.replaceName(Messages.Tooltip.Tools.TRIGGER_ADD, "trigger"));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TRIGGER_ADD);
				}
			}
		};
		toolButtons.add(button);

		/** @todo readd pickup */
		// // Pickup add
		// if (Config.Gui.usesTextButtons()) {
		// button = new TextButton("Add pickup", mStyles.textButton.standard);
		// } else {
		// button = new ImageButton(mStyles.skin.editor, EditorIcons.PICKUP_ADD.toString());
		// }
		// new ButtonListener(button) {
		// @Override
		// protected void onChecked(boolean checked) {
		// if (checked) {
		// mLevelEditor.switchTool(Tools.PICKUP_ADD);
		// }
		// }
		// };
		// toolButtons.add(button);


		// Add buttons to tool
		float maximumToolMenuHeight = getMaximumToolMenuHeight();
		float totalHeight = 0;

		AlignTable column = new AlignTable();
		column.setPreferences(mToolMenu);
		ButtonGroup buttonGroup = new ButtonGroup();

		Iterator<Button> iterator = toolButtons.iterator();
		while (iterator.hasNext()) {
			Button nextButton = iterator.next();

			float buttonHeight = nextButton.getHeight();

			if (totalHeight + buttonHeight > maximumToolMenuHeight) {
				mToolMenu.add(column);
				column = new AlignTable();
				column.setPreferences(mToolMenu);
				totalHeight = 0;

				// Only switch to bottom alignment if one row is full
				mToolMenu.setTableAlign(Horizontal.LEFT, Vertical.BOTTOM);
			}

			totalHeight += buttonHeight;

			column.row();
			column.add(nextButton);
			buttonGroup.add(nextButton);
		}

		// Add last column
		mToolMenu.add(column);

		Pools.arrayList.free(toolButtons);
	}

	@Override
	protected String getResourceTypeName() {
		return "level";
	}

	/**
	 * Initializes level info content for message box
	 */
	private void initInfo() {
		AlignTable left = new AlignTable();
		AlignTable right = new AlignTable();
		mWidgets.info.table.setRowAlign(Horizontal.LEFT, Vertical.TOP);
		left.setPreferences(mWidgets.info.table);
		right.setPreferences(mWidgets.info.table);

		float halfWidth = Gdx.graphics.getWidth() * Config.Editor.Level.OPTIONS_WIDTH * 0.5f;
		float height = Gdx.graphics.getHeight() * Config.Editor.Level.OPTIONS_HEIGHT;

		mWidgets.info.table.row().setPadTop(10);
		mWidgets.info.table.add(left);
		mWidgets.info.table.add(right);

		left.setSize(halfWidth, height);
		right.setSize(halfWidth, height);
		left.setKeepSize(true);
		right.setKeepSize(true);


		// Left side
		// Name
		left.row().setFillWidth(true);
		Label label = new Label("Name", mStyles.label.standard);
		new TooltipListener(label, "Name", Messages.Tooltip.Level.Option.NAME);
		left.add(label).setPadRight(mStyles.vars.paddingAfterLabel);

		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setMaxLength(Config.Editor.NAME_LENGTH_MAX);
		left.add(textField).setFillWidth(true);
		mWidgets.info.name = textField;
		new TooltipListener(textField, "Name", Messages.Tooltip.Level.Option.NAME);
		new TextFieldListener(textField, "Name", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setLevelName(newText);
			}
		};

		// Description
		left.row();
		label = new Label("Description", mStyles.label.standard);
		new TooltipListener(label, "Description", Messages.Tooltip.Level.Option.DESCRIPTION);
		left.add(label);

		left.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", mStyles.textField.standard);
		textField.setMaxLength(Config.Editor.DESCRIPTION_LENGTH_MAX);
		left.add(textField).setFillHeight(true).setFillWidth(true);
		mWidgets.info.description = textField;
		new TooltipListener(textField, "Description", Messages.Tooltip.Level.Option.DESCRIPTION);
		new TextFieldListener(textField, "Set your description...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setLevelDescription(newText);
			}
		};

		// Speed
		left.row().setFillWidth(true);
		label = new Label("Level Speed", mStyles.label.standard);
		new TooltipListener(label, "Level speed", Messages.Tooltip.Level.Option.LEVEL_SPEED);
		left.add(label).setPadRight(mStyles.vars.paddingAfterLabel);

		Slider slider = new Slider(Editor.Level.LEVEL_SPEED_MIN, Editor.Level.LEVEL_SPEED_MAX, Editor.Level.LEVEL_SPEED_STEP_SIZE, false,
				mStyles.slider.standard);
		left.add(slider).setFillWidth(true);
		mWidgets.info.speed = slider;

		textField = new TextField("", mStyles.textField.standard);
		new TooltipListener(textField, "Level speed", Messages.Tooltip.Level.Option.LEVEL_SPEED);
		left.add(textField).setWidth(mStyles.vars.textFieldNumberWidth);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setLevelStartingSpeed(newValue);
			}
		};

		// Theme
		left.row().setFillWidth(true);
		label = new Label("Theme", mStyles.label.standard);
		left.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		/** @todo add tooltip for the level's theme */
		SelectBox selectBox = new SelectBox(Themes.values(), (SelectBoxStyle) SkinNames.getResource(General.SELECT_BOX_DEFAULT));
		mWidgets.info.theme = selectBox;
		left.add(selectBox).setFillWidth(true);
		new SelectBoxListener(selectBox) {
			@Override
			protected void onSelectionChanged(int itemIndex) {
				mLevelEditor.setTheme(Themes.values()[itemIndex]);
			}
		};

		// Revision
		left.row().setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		label = new Label("Revision:", mStyles.label.standard);
		new TooltipListener(label, "Revision", Messages.Tooltip.Level.Option.REVISION);
		left.add(label).setPadRight(mStyles.vars.paddingAfterLabel);

		label = new Label("", mStyles.label.standard);
		new TooltipListener(label, "Revision", Messages.Tooltip.Level.Option.REVISION);
		left.add(label);
		mWidgets.info.revision = label;


		// RIGHT
		right.row();
		label = new Label("Prologue", mStyles.label.standard);
		new TooltipListener(label, "Prologue", Messages.Tooltip.Level.Option.PROLOGUE);
		right.add(label);

		right.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", mStyles.textField.standard);
		textField.setMaxLength(Config.Editor.STORY_LENGTH_MAX);
		right.add(textField).setFillWidth(true).setFillHeight(true);
		mWidgets.info.storyBefore = textField;
		new TooltipListener(textField, "Prologue", Messages.Tooltip.Level.Option.PROLOGUE);
		new TextFieldListener(textField, "Write a story to be displayed when loading the level (optional)...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setPrologue(newText);
			}
		};


		right.row();
		label = new Label("Epilogue", mStyles.label.standard);
		new TooltipListener(label, "Epilogue", Messages.Tooltip.Level.Option.EPILOGUE);
		right.add(label);

		right.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", mStyles.textField.standard);
		textField.setMaxLength(Config.Editor.STORY_LENGTH_MAX);
		right.add(textField).setFillWidth(true).setFillHeight(true);
		mWidgets.info.epilogue = textField;
		new TooltipListener(textField, "Epilogue", Messages.Tooltip.Level.Option.EPILOGUE);
		new TextFieldListener(textField, "Write the story to be displayed when the level is completed (optional)...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setEpilogue(newText);
			}
		};

		mWidgets.info.table.setTransform(true);
		mWidgets.info.table.layout();
		mWidgets.info.table.setKeepSize(true);
	}

	/**
	 * Calculates the amount of enemies per column in the add enemy scroll pane. Uses
	 * Config.Editor.Level.Enemy.ADD_ENEMY_TABLE_MAX_WIDTH for determining the maximum amount of width on the scroll
	 * pane.
	 * 
	 * @return maximum number of buttons
	 */
	private int getEnemiesPerColumnInAddTable() {
		float maxWidth = Gdx.graphics.getWidth() * Config.Editor.Level.Enemy.ADD_ENEMY_TABLE_MAX_WIDTH;

		// How many buttons can we have for this width?
		int maxButtons = (int) (maxWidth / Config.Editor.Level.Enemy.ADD_BUTTON_SIZE);

		return maxButtons;
	}

	/**
	 * Init add enemy options
	 */
	private void initEnemyAddOptions() {
		mHiders.enemyAdd.setButton(mWidgets.tool.enemyAdd);

		Button button;
		TooltipListener tooltipListener;


		// Scroll pane
		mMainTable.row().setFillHeight(true);
		mWidgets.enemyAdd.scrollTable.align(Align.left | Align.top);
		ScrollPane scrollPane = new ScrollPane(mWidgets.enemyAdd.scrollTable, mStyles.scrollPane.windowBackground);
		mWidgets.enemyAdd.scrollPane = scrollPane;

		int scrollPaneWidth = Config.Editor.Level.Enemy.ADD_BUTTON_SIZE * getEnemiesPerColumnInAddTable();
		mWidgets.enemyAdd.scrollTable.setWidth(scrollPaneWidth);
		// Add margin
		mMainTable.add(scrollPane).setFillHeight(true).setWidth(scrollPaneWidth);
		mHiders.enemyAdd.addToggleActor(scrollPane);


		// Add enemy button
		mMainTable.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add enemy to list", mStyles.textButton.press);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ENEMY_SELECT.toString());
		}
		mMainTable.add(button);
		mHiders.enemyAdd.addToggleActor(button);
		tooltipListener = new TooltipListener(button, null, Messages.Tooltip.Level.Enemy.ADD);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mLevelEditor.selectEnemy();
			}
		};
	}

	/**
	 * Initializes Enemy Tool GUI
	 */
	private void initEnemyOptions() {
		mWidgets.enemy.table.setPreferences(mMainTable);
		mMainTable.row();
		mMainTable.add(mWidgets.enemy.table);
		mHiders.enemyOptions.addToggleActor(mWidgets.enemy.table);


		// Enemy options when an enemy is selected
		// # Enemies
		mWidgets.enemy.table.row();
		Label label = new Label("# Enemies", mStyles.label.standard);
		new TooltipListener(label, "No. of enemies", Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		Slider slider = new Slider(Level.Enemy.ENEMIES_MIN, Level.Enemy.ENEMIES_MAX, Level.Enemy.ENEMIES_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.enemy.cEnemies = slider;
		mWidgets.enemy.table.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, "No. of enemies", Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		new TooltipListener(textField, "No. of enemies", Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setEnemyCount((int) (newValue + 0.5f));
			}
		};
		HideSliderValue delayHider = new HideSliderValue(slider, 2, Float.MAX_VALUE);
		mHiders.enemyOptions.addChild(delayHider);


		// Delay
		mWidgets.enemy.table.row();
		label = new Label("Spawn delay between enemies", mStyles.label.standard);
		new TooltipListener(label, "Spawn delay", Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		delayHider.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.DELAY_BETWEEN_MIN, Level.Enemy.DELAY_BETWEEN_MAX, Level.Enemy.DELAY_BETWEEN_STEP_SIZE, false,
				mStyles.slider.standard);
		delayHider.addToggleActor(slider);
		mWidgets.enemy.betweenDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		delayHider.addToggleActor(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, "Spawn delay", Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		new TooltipListener(textField, "Spawn delay", Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setEnemySpawnDelay(newValue);
			}
		};


		// Activation delay
		mWidgets.enemy.table.row();
		label = new Label("Activate delay", mStyles.label.standard);
		new TooltipListener(label, "Activate delay", Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		mHiders.enemyActivateDelay.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.TRIGGER_ACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_ACTIVATE_DELAY_MAX,
				Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, false, mStyles.slider.standard);
		mHiders.enemyActivateDelay.addToggleActor(slider);
		mWidgets.enemy.activateDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		mHiders.enemyActivateDelay.addToggleActor(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, "Activate delay", Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		new TooltipListener(textField, "Activate delay", Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setSelectedEnemyActivateTriggerDelay(newValue);
			}
		};


		// Deactivation delay
		mWidgets.enemy.table.row();
		label = new Label("Deactivate delay", mStyles.label.standard);
		new TooltipListener(label, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		mHiders.enemyDeactivateDelay.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MAX,
				Level.Enemy.TRIGGER_DEACTIVATE_DELAY_STEP_SIZE, false, mStyles.slider.standard);
		mHiders.enemyDeactivateDelay.addToggleActor(slider);
		mWidgets.enemy.deactivateDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		mHiders.enemyDeactivateDelay.addToggleActor(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		new TooltipListener(textField, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setSelectedEnemyDeactivateTriggerDelay(newValue);
			}
		};

		mWidgets.enemy.table.row().setPadTop(mStyles.vars.paddingSeparator);
	}

	/**
	 * Initializes path tool GUI
	 */
	private void initPathOptions() {
		mHiders.pathOptions.addToggleActor(mWidgets.path.table);
		mMainTable.row();
		mMainTable.add(mWidgets.path.table);
		mWidgets.path.table.setPreferences(mMainTable);

		// Path options
		Button button;
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.setMinCheckCount(0);
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Once", mStyles.textButton.toggle);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.PATH_ONCE.toString());
		}
		mWidgets.path.once = button;
		mWidgets.path.table.add(button);
		buttonGroup.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, "Once", Messages.Tooltip.Level.Path.ONCE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.ONCE);
				}
			}
		};


		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Loop", mStyles.textButton.toggle);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.PATH_LOOP.toString());
		}
		mWidgets.path.loop = button;
		mWidgets.path.table.add(button);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Loop", Messages.Tooltip.Level.Path.LOOP);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.LOOP);
				}
			}
		};


		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Back and forth", mStyles.textButton.toggle);
		}
		else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.PATH_BACK_AND_FORTH.toString());
		}
		mWidgets.path.backAndForth = button;
		mWidgets.path.table.add(button);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Back and forth", Messages.Tooltip.Level.Path.BACK_AND_FORTH);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.BACK_AND_FORTH);
				}
			}
		};

		mWidgets.path.table.row().setPadTop(mStyles.vars.paddingSeparator);
	}

	/**
	 * Initializes Pickup tool GUI
	 */
	private void initPickup() {
		// mHiders.pickups.addToggleActor(mPickupTable);
		// Button button;
		//
		// mPickupTable.row();
		// Label label = new Label("", mStyles.label.standard);
		// new TooltipListener(label, "Pickup name", Messages.Tooltip.Level.Pickup.SELECT_NAME);
		// mWidgets.pickup.name = label;
		// mPickupTable.add(label);
		//
		// if (Config.Gui.usesTextButtons()) {
		// button = new TextButton("Select type", mStyles.textButton.press);
		// } else {
		// button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.PICKUP_SELECT.toString());
		// }
		// TooltipListener tooltipListener = new TooltipListener(button, "Select type",
		// Messages.Tooltip.Level.Pickup.SELECT_TYPE);
		// new ButtonListener(button, tooltipListener) {
		// @Override
		// protected void onPressed() {
		// mLevelEditor.selectPickup();
		// }
		// };
		// mPickupTable.add(button);
		//
		//
		// mPickupTable.setTransform(true);
		// mPickupTable.invalidate();
	}

	/** Pickup table */
	private AlignTable mPickupTable = new AlignTable();
	/** Level editor the GUI will act on */
	private LevelEditor mLevelEditor = null;
	/** Invoker for level editor */
	private Invoker mInvoker = null;
	/** Inner widgets */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** All hiders */
	private Hiders mHiders = null;


	/**
	 * Container for all hiders
	 */
	private static class Hiders implements Disposable {
		/**
		 * Sets correct children etc.
		 */
		public Hiders() {
			enemyOptions.addChild(enemyActivateDelay);
			enemyOptions.addChild(enemyDeactivateDelay);
			trigger.addChild(triggerActorActivate);
			trigger.addChild(triggerScreenAt);
		}

		/**
		 * Dispose
		 */
		@Override
		public void dispose() {
			enemyAdd.dispose();
			enemyOptions.dispose();
			enemyActivateDelay.dispose();
			enemyDeactivateDelay.dispose();
			pathOptions.dispose();
			trigger.dispose();
			triggerScreenAt.dispose();
			triggerActorActivate.dispose();
			pickups.dispose();
		}

		/** Enemy hider */
		HideListener enemyAdd = new HideListener(true);
		/** Hides enemy options */
		HideManual enemyOptions = new HideManual();
		/** Hides trigger delay for trigger */
		HideManual enemyActivateDelay = new HideManual();
		/** Hides trigger deactivate delay */
		HideManual enemyDeactivateDelay = new HideManual();
		/** Hides path options */
		HideManual pathOptions = new HideManual();
		/** Trigger hider */
		HideListener trigger = new HideListener(true);
		/** Hides trigger screen at options */
		HideManual triggerScreenAt = new HideManual();
		/** Hides trigger actor activate options */
		HideManual triggerActorActivate = new HideManual();
		/** Hides pickups */
		HideListener pickups = new HideListener(true);
	}

	/**
	 * Container for inner widgets
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		EnemyOptionWidgets enemy = new EnemyOptionWidgets();
		PathOptionWidgets path = new PathOptionWidgets();
		InfoWidgets info = new InfoWidgets();
		EnemyAddWidgets enemyAdd = new EnemyAddWidgets();
		ToolWidgets tool = new ToolWidgets();

		static class ToolWidgets {
			Button enemyAdd = null;
		}

		static class EnemyOptionWidgets {
			AlignTable table = new AlignTable();
			Slider cEnemies = null;
			Slider betweenDelay = null;
			Slider activateDelay = null;
			Slider deactivateDelay = null;
		}

		static class EnemyAddWidgets {
			ScrollPane scrollPane = null;
			Table scrollTable = new Table();
		}

		static class InfoWidgets {
			AlignTable table = new AlignTable();
			TextField name = null;
			TextField description = null;
			Slider speed = null;
			SelectBox theme = null;
			Label revision = null;
			TextField storyBefore = null;
			TextField epilogue = null;
		}

		static class PathOptionWidgets {
			AlignTable table = new AlignTable();
			Button once = null;
			Button loop = null;
			Button backAndForth = null;
		}

	}
}
