package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.HideSliderValue;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Level;
import com.spiddekauga.voider.Config.Gui;
import com.spiddekauga.voider.editor.LevelEditor.Tools;
import com.spiddekauga.voider.editor.commands.CGuiSlider;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.EditorIcons;
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
	 * @param levelEditor the scene this GUI will act on
	 */
	public void setLevelEditor(LevelEditor levelEditor) {
		mLevelEditor = levelEditor;
		mInvoker = mLevelEditor.getInvoker();

		setEditor(mLevelEditor);
	}

	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);


		mPickupTable.setPreferences(mMainTable);
		mPickupTable.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mWidgets.enemy.table.setPreferences(mMainTable);
		mWidgets.enemy.table.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mOptionTable.setPreferences(mMainTable);

		mHiders = new Hiders();

		initToolMenu();
		//		initOptions();
		//		initPickup();
		initPathOptions();
		initEnemyAddOptions();
		initEnemyOptions();
	}

	@Override
	public void dispose() {
		mPickupTable.dispose();
		mWidgets.enemy.table.dispose();
		mOptionTable.dispose();

		super.dispose();
	}

	@Override
	public void resetValues() {

		resetPathOptions();
		resetEnemyOptions();




		//
		//
		//		// General options
		//		mWidgets.option.description.setText(mLevelEditor.getLevelDescription());
		//		mWidgets.option.name.setText(mLevelEditor.getLevelName());
		//		mWidgets.option.revision.setText(mLevelEditor.getLevelRevision());
		//		mWidgets.option.storyBefore.setText(mLevelEditor.getPrologue());
		//		mWidgets.option.epilogue.setText(mLevelEditor.getEpilogue());
		//		mWidgets.option.speed.setValue(mLevelEditor.getLevelStartingSpeed());
		//
		//
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
		} else {
			mHiders.pathOptions.hide();
		}
	}

	/**
	 * Resets enemy option values
	 */
	void resetEnemyOptions() {

		// Add name
		String enemyName = mLevelEditor.getSelectedEnemyName();
		if (enemyName == null) {
			enemyName = Messages.getNoDefSelected("enemy");
		}
		mWidgets.enemyAdd.name.setText(enemyName);
		mWidgets.enemyAdd.name.setSize(mWidgets.enemyAdd.name.getPrefWidth(), mWidgets.enemyAdd.name.getPrefHeight());


		// Show/Hide options
		if (mLevelEditor.isEnemySelected()) {
			mHiders.enemyOptions.show();

			// Update enemy count slider values
			if (mWidgets.enemy.cEnemies.getValue() != mLevelEditor.getEnemyCount()) {
				mInvoker.execute(new CGuiSlider(mWidgets.enemy.cEnemies, mLevelEditor.getEnemyCount(), mWidgets.enemy.cEnemies.getValue()), true);
			}

			// Update enemy delay slider values
			if (mLevelEditor.getEnemySpawnDelay() >= 0 &&  mLevelEditor.getEnemySpawnDelay() != mWidgets.enemy.betweenDelay.getValue()) {
				mInvoker.execute(new CGuiSlider(mWidgets.enemy.betweenDelay, mLevelEditor.getEnemySpawnDelay(), mWidgets.enemy.betweenDelay.getValue()), true);
			}

			// Has activate trigger -> Show trigger delay
			if (mLevelEditor.hasSelectedEnemyActivateTrigger()) {
				mHiders.enemyActivateDelay.show();

				float activateDelay = mLevelEditor.getSelectedEnemyActivateTriggerDelay();
				if (activateDelay >= 0 && activateDelay != mWidgets.enemy.activateDelay.getValue()) {
					mInvoker.execute(new CGuiSlider(mWidgets.enemy.activateDelay, activateDelay, mWidgets.enemy.activateDelay.getValue()));
				}
			} else {
				mHiders.enemyActivateDelay.hide();
			}


			// Has deactivate trigger -> Show trigger delay
			if (mLevelEditor.hasSelectedEnemyDeactivateTrigger()) {
				mHiders.enemyDeactivateDelay.show();

				float deactivateDelay = mLevelEditor.getSelectedEnemyDeactivateTriggerDelay();
				if (deactivateDelay >= 0 && deactivateDelay!= mWidgets.enemy.deactivateDelay.getValue()) {
					mInvoker.execute(new CGuiSlider(mWidgets.enemy.deactivateDelay, deactivateDelay, mWidgets.enemy.deactivateDelay.getValue()));
				}
			} else {
				mHiders.enemyDeactivateDelay.hide();
			}

		} else {
			mHiders.enemyOptions.hide();
		}
		//
		//
		//		if (mLevelEditor.isEnemySelected()) {
		//			mHiders.enemyOptions.show();
		//		} else {
		//			mHiders.enemyOptions.hide();
		//
		//			// Current state is set active/deactive trigger, change to select instead
		//			if (mLevelEditor.getEnemyState() == AddEnemyTool.States.SET_ACTIVATE_TRIGGER ||
		//					mLevelEditor.getEnemyState() == AddEnemyTool.States.SET_DEACTIVATE_TRIGGER) {
		//				mLevelEditor.setEnemyState(AddEnemyTool.States.SELECT);
		//			}
		//
		//			return;
		//		}
		//
		//		if (mWidgets.enemy.cEnemies.getValue() != mLevelEditor.getEnemyCount()) {
		//			mInvoker.execute(new CGuiSlider(mWidgets.enemy.cEnemies, mLevelEditor.getEnemyCount(), mWidgets.enemy.cEnemies.getValue()), true);
		//		}
		//
		//		if (mLevelEditor.getEnemySpawnDelay() >= 0 &&  mLevelEditor.getEnemySpawnDelay() != mWidgets.enemy.betweenDelay.getValue()) {
		//			mInvoker.execute(new CGuiSlider(mWidgets.enemy.betweenDelay, mLevelEditor.getEnemySpawnDelay(), mWidgets.enemy.betweenDelay.getValue()), true);
		//		}
		//
		//
		//		// Has activate trigger -> Show trigger delay
		//		if (mLevelEditor.hasSelectedEnemyActivateTrigger()) {
		//			mHiders.enemyActivateDelay.show();
		//
		//			float activateDelay = mLevelEditor.getSelectedEnemyActivateTriggerDelay();
		//			if (activateDelay >= 0 && activateDelay != mWidgets.enemy.activateDelay.getValue()) {
		//				mInvoker.execute(new CGuiSlider(mWidgets.enemy.activateDelay, activateDelay, mWidgets.enemy.activateDelay.getValue()));
		//			}
		//		} else {
		//			mHiders.enemyActivateDelay.hide();
		//		}
		//
		//
		//		// Has deactivate trigger -> Show trigger delay
		//		if (mLevelEditor.hasSelectedEnemyDeactivateTrigger()) {
		//			mHiders.enemyDeactivateDelay.show();
		//
		//			float deactivateDelay = mLevelEditor.getSelectedEnemyDeactivateTriggerDelay();
		//			if (deactivateDelay >= 0 && deactivateDelay!= mWidgets.enemy.deactivateDelay.getValue()) {
		//				mInvoker.execute(new CGuiSlider(mWidgets.enemy.deactivateDelay, deactivateDelay, mWidgets.enemy.deactivateDelay.getValue()));
		//			}
		//		} else {
		//			mHiders.enemyDeactivateDelay.hide();
		//		}
	}

	/**
	 * Initializes the tool menu
	 */
	private void initToolMenu() {
		Button button;

		@SuppressWarnings("unchecked")
		ArrayList<Button> toolButtons = Pools.arrayList.obtain();

		// Select
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.SELECT.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.SELECTION);
				}
			}
		};
		toolButtons.add(button);

		// Cancel
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Cancel", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.CANCEL.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					/** @todo remove selection */
				}
			}
		};
		toolButtons.add(button);

		// Move
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.MOVE.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.MOVE);
				}
			}
		};
		toolButtons.add(button);

		// Delete
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Delete", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.DELETE.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.DELETE);
				}
			}
		};
		toolButtons.add(button);

		// Terrain draw_append
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Terrain, append", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.TERRAIN_DRAW_APPEND.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_DRAW_APPEND);
				}
			}
		};
		toolButtons.add(button);

		// Terrain draw_erase
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Terrain, draw erase", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.TERRAIN_DRAW_ERASE.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_DRAW_ERASE);
				}
			}
		};
		toolButtons.add(button);

		// add_move_corner
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add/move corner", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.ADD_MOVE_CORNER.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ADD_MOVE_CORNER);
				}
			}
		};
		toolButtons.add(button);

		// remove_corner
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Remove corner", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.REMOVE_CORNER.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.REMOVE_CORNER);
				}
			}
		};
		toolButtons.add(button);

		// Path add
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add path", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.PATH_ADD.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PATH_ADD_CORNER);
				}
			}
		};
		toolButtons.add(button);

		// Enemy add
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add enemy", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.ENEMY_ADD.toString());
		}
		mWidgets.tool.enemyAdd = button;
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_ADD);
				}
			}
		};
		toolButtons.add(button);

		// Enemy - set activate trigger
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Set activate trigger", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.ENEMY_SET_ACTIVATE_TRIGGER.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_SET_ACTIVATE_TRIGGER);
				}
			}
		};
		toolButtons.add(button);

		// ENemy - set deactivate trigger
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Set deactivate trigger", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.ENEMY_SET_DEACTIVATE_TRIGGER.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_SET_DEACTIVATE_TRIGGER);
				}
			}
		};
		toolButtons.add(button);

		// Trigger add
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add trigger", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.TRIGGER_ADD.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TRIGGER_ADD);
				}
			}
		};
		toolButtons.add(button);

		//		// Pickup add
		//		/** @todo REMOVE text button */
		//		if (Config.Gui.usesTextButtons()) {
		//			button = new TextButton("Add pickup", mTextToggleStyle);
		//		} else {
		//			button = new ImageButton(mEditorSkin, EditorIcons.PICKUP_ADD.toString());
		//		}
		//		new ButtonListener(button) {
		//			@Override
		//			protected void onChecked(boolean checked) {
		//				if (checked) {
		//					mLevelEditor.switchTool(Tools.PICKUP_ADD);
		//				}
		//			}
		//		};
		//		toolButtons.add(button);


		// Add buttons to tool
		float maximumToolMenuHeight = getMaximumToolMenuHeight();
		float paddingHeight = Config.Gui.PADDING_DEFAULT * 2;
		float totalHeight = 0;

		AlignTable column = new AlignTable();
		column.setPreferences(mToolMenu);
		ButtonGroup buttonGroup = new ButtonGroup();

		Iterator<Button> iterator = toolButtons.iterator();
		while (iterator.hasNext()) {
			Button nextButton = iterator.next();

			float buttonHeight = nextButton.getHeight() + paddingHeight;

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

	/**
	 * Set the path type
	 * @param pathType the path type to be set in GUI buttons
	 */
	private void setPathType(PathTypes pathType) {
		switch (pathType) {
		case ONCE:
			mWidgets.path.once.setChecked(true);
			break;

		case LOOP:
			mWidgets.path.loop.setChecked(true);
			break;

		case BACK_AND_FORTH:
			mWidgets.path.backAndForth.setChecked(true);
			break;
		}
	}

	@Override
	protected String getResourceTypeName() {
		return "level";
	}

	/**
	 * Initializes options content for message box
	 */
	private void initOptions() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		SliderStyle sliderStyle = generalSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = generalSkin.get("default", TextFieldStyle.class);
		LabelStyle labelStyle = generalSkin.get("default", LabelStyle.class);

		AlignTable left = new AlignTable();
		AlignTable right = new AlignTable();
		mOptionTable.setRowAlign(Horizontal.LEFT, Vertical.TOP);
		left.setPreferences(mOptionTable);
		right.setPreferences(mOptionTable);

		float halfWidth = Gdx.graphics.getWidth() * Config.Editor.Level.OPTIONS_WIDTH * 0.5f;
		float height = Gdx.graphics.getHeight() * Config.Editor.Level.OPTIONS_HEIGHT;

		mOptionTable.row().setPadTop(10);
		mOptionTable.add(left);
		mOptionTable.add(right);

		left.setSize(halfWidth, height);
		right.setSize(halfWidth, height);
		left.setKeepSize(true);
		right.setKeepSize(true);

		// Left side
		left.row().setFillWidth(true);
		Label label = new Label("Name", labelStyle);
		new TooltipListener(label, "Name", Messages.Tooltip.Level.Option.NAME);
		left.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		TextField textField = new TextField("", textFieldStyle);
		textField.setMaxLength(Config.Editor.NAME_LENGTH_MAX);
		left.add(textField).setFillWidth(true);
		mWidgets.option.name = textField;
		new TooltipListener(textField, "Name", Messages.Tooltip.Level.Option.NAME);
		new TextFieldListener(textField, "Name", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setLevelName(newText);
			}
		};

		left.row();
		label = new Label("Description", labelStyle);
		new TooltipListener(label, "Description", Messages.Tooltip.Level.Option.DESCRIPTION);
		left.add(label);

		left.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", textFieldStyle);
		textField.setMaxLength(Config.Editor.DESCRIPTION_LENGTH_MAX);
		left.add(textField).setFillHeight(true).setFillWidth(true);
		mWidgets.option.description = textField;
		new TooltipListener(textField, "Description", Messages.Tooltip.Level.Option.DESCRIPTION);
		new TextFieldListener(textField, "Set your description...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setLevelDescription(newText);
			}
		};



		left.row().setFillWidth(true);
		label = new Label("Level Speed", labelStyle);
		new TooltipListener(label, "Level speed", Messages.Tooltip.Level.Option.LEVEL_SPEED);
		left.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		Slider slider = new Slider(Editor.Level.LEVEL_SPEED_MIN, Editor.Level.LEVEL_SPEED_MAX, Editor.Level.LEVEL_SPEED_STEP_SIZE, false, sliderStyle);
		left.add(slider).setFillWidth(true);
		mWidgets.option.speed = slider;

		textField = new TextField("", textFieldStyle);
		new TooltipListener(textField, "Level speed", Messages.Tooltip.Level.Option.LEVEL_SPEED);
		left.add(textField).setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setLevelStartingSpeed(newValue);
			}
		};


		left.row().setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		label = new Label("Revision:", labelStyle);
		new TooltipListener(label, "Revision", Messages.Tooltip.Level.Option.REVISION);
		left.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		label = new Label("", labelStyle);
		new TooltipListener(label, "Revision", Messages.Tooltip.Level.Option.REVISION);
		left.add(label);
		mWidgets.option.revision = label;


		// RIGHT
		right.row();
		label = new Label("Prologue", labelStyle);
		new TooltipListener(label, "Prologue", Messages.Tooltip.Level.Option.STORY_BEFORE);
		right.add(label);

		right.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", textFieldStyle);
		textField.setMaxLength(Config.Editor.STORY_LENGTH_MAX);
		right.add(textField).setFillWidth(true).setFillHeight(true);
		mWidgets.option.storyBefore = textField;
		new TooltipListener(textField, "Prologue", Messages.Tooltip.Level.Option.STORY_BEFORE);
		new TextFieldListener(textField, "Write a story to be displayed when loading the level (optional)...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setPrologue(newText);
			}
		};


		right.row();
		label = new Label("Epilogue", labelStyle);
		new TooltipListener(label, "Epilogue", Messages.Tooltip.Level.Option.STORY_AFTER);
		right.add(label);

		right.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", textFieldStyle);
		textField.setMaxLength(Config.Editor.STORY_LENGTH_MAX);
		right.add(textField).setFillWidth(true).setFillHeight(true);
		mWidgets.option.epilogue = textField;
		new TooltipListener(textField, "Epilogue", Messages.Tooltip.Level.Option.STORY_AFTER);
		new TextFieldListener(textField, "Write the story to be displayed when the level is completed (optional)...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setEpilogue(newText);
			}
		};

		mOptionTable.setTransform(true);
		mOptionTable.layout();
		mOptionTable.setKeepSize(true);
	}

	/**
	 * Init add enemy options
	 */
	private void initEnemyAddOptions() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textButtonStyle = generalSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = generalSkin.get("default", LabelStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		mWidgets.enemyAdd.table.setPreferences(mMainTable);
		mWidgets.enemyAdd.table.row();
		mHiders.enemyAdd.addToggleActor(mWidgets.enemyAdd.table);
		mHiders.enemyAdd.setButton(mWidgets.tool.enemyAdd);
		mMainTable.row();
		mMainTable.add(mWidgets.enemyAdd.table);

		Button button;
		TooltipListener tooltipListener;

		// Select type
		Label label = new Label("", labelStyle);
		new TooltipListener(label, "Enemy type name", Messages.Tooltip.Level.Enemy.SELECT_NAME);
		mWidgets.enemyAdd.name = label;
		mWidgets.enemyAdd.table.add(label).setAlign(Horizontal.RIGHT, Vertical.MIDDLE);

		mWidgets.enemyAdd.table.row();
		/** @todo remove text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select type", textButtonStyle);
		} else {
			button = new ImageButton(editorSkin, SkinNames.EditorIcons.ENEMY_SELECT.toString());
		}
		mWidgets.enemyAdd.table.add(button);
		tooltipListener = new TooltipListener(button, "Select enemy type", Messages.Tooltip.Level.Enemy.SELECT_TYPE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mLevelEditor.selectEnemy();
			}
		};

		mWidgets.enemyAdd.table.row().setPadTop(Gui.SEPARATE_PADDING);
	}

	/**
	 * Initializes Enemy Tool GUI
	 */
	private void initEnemyOptions() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		SliderStyle sliderStyle = generalSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = generalSkin.get("default", TextFieldStyle.class);
		LabelStyle labelStyle = generalSkin.get("default", LabelStyle.class);

		mWidgets.enemy.table.setPreferences(mMainTable);
		mMainTable.row();
		mMainTable.add(mWidgets.enemy.table);
		mHiders.enemyOptions.addToggleActor(mWidgets.enemy.table);


		// Enemy options when an enemy is selected
		// # Enemies
		mWidgets.enemy.table.row();
		Label label = new Label("# Enemies", labelStyle);
		new TooltipListener(label, "No. of enemies", Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		Slider slider = new Slider(Level.Enemy.ENEMIES_MIN, Level.Enemy.ENEMIES_MAX, Level.Enemy.ENEMIES_STEP_SIZE, false, sliderStyle);
		mWidgets.enemy.cEnemies = slider;
		mWidgets.enemy.table.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
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
		label = new Label("Spawn delay between enemies", labelStyle);
		new TooltipListener(label, "Spawn delay", Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		delayHider.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.DELAY_BETWEEN_MIN, Level.Enemy.DELAY_BETWEEN_MAX, Level.Enemy.DELAY_BETWEEN_STEP_SIZE, false, sliderStyle);
		delayHider.addToggleActor(slider);
		mWidgets.enemy.betweenDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", textFieldStyle);
		delayHider.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
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
		label = new Label("Activate delay", labelStyle);
		new TooltipListener(label, "Activate delay", Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		mHiders.enemyActivateDelay.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.TRIGGER_ACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_ACTIVATE_DELAY_MAX, Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, false, sliderStyle);
		mHiders.enemyActivateDelay.addToggleActor(slider);
		mWidgets.enemy.activateDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", textFieldStyle);
		mHiders.enemyActivateDelay.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
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
		label = new Label("Deactivate delay", labelStyle);
		new TooltipListener(label, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		mHiders.enemyDeactivateDelay.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MAX, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_STEP_SIZE, false, sliderStyle);
		mHiders.enemyDeactivateDelay.addToggleActor(slider);
		mWidgets.enemy.deactivateDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", textFieldStyle);
		mHiders.enemyDeactivateDelay.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		new TooltipListener(textField, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setSelectedEnemyDeactivateTriggerDelay(newValue);
			}
		};

		mWidgets.enemy.table.row().setPadTop(Gui.SEPARATE_PADDING);
	}

	/**
	 * Initializes path tool GUI
	 */
	private void initPathOptions() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		mHiders.pathOptions.addToggleActor(mWidgets.path.table);
		mMainTable.row();
		mMainTable.add(mWidgets.path.table);
		mWidgets.path.table.setPreferences(mMainTable);

		// Path options
		Button button;
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.setMinCheckCount(0);
		/** @todo remove text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Once", textToggleStyle);
		} else {
			button = new ImageButton(editorSkin, SkinNames.EditorIcons.PATH_ONCE.toString());
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


		mWidgets.path.table.row();
		/** @todo remove text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Loop", textToggleStyle);
		} else {
			button = new ImageButton(editorSkin, SkinNames.EditorIcons.PATH_LOOP.toString());
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


		mWidgets.path.table.row();
		/** @todo remove text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Back and forth", textToggleStyle);
		} else {
			button = new ImageButton(editorSkin, SkinNames.EditorIcons.PATH_BACK_AND_FORTH.toString());
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

		mWidgets.path.table.row().setPadTop(Gui.SEPARATE_PADDING);
	}

	/**
	 * Initializes Pickup tool GUI
	 */
	private void initPickup() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textButtonStyle = generalSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = generalSkin.get("default", LabelStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		mHiders.pickups.addToggleActor(mPickupTable);
		Button button;


		mPickupTable.row();
		Label label = new Label("", labelStyle);
		new TooltipListener(label, "Pickup name", Messages.Tooltip.Level.Pickup.SELECT_NAME);
		mWidgets.pickup.name = label;
		mPickupTable.add(label);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select type", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		TooltipListener tooltipListener = new TooltipListener(button, "Select type", Messages.Tooltip.Level.Pickup.SELECT_TYPE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mLevelEditor.selectPickup();
			}
		};
		mPickupTable.add(button);


		mPickupTable.setTransform(true);
		mPickupTable.invalidate();
	}

	/** Pickup table */
	private AlignTable mPickupTable = new AlignTable();
	/** Options table */
	private AlignTable mOptionTable = new AlignTable();
	/** Level editor the GUI will act on */
	private LevelEditor mLevelEditor = null;
	/** Invoker for level editor */
	private Invoker mInvoker = null;
	/** Inner widgets */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** All hiders  */
	private Hiders mHiders = null;



	/**
	 * Container for all hiders
	 */
	private static class Hiders {
		/**
		 * Sets correct children etc.
		 */
		public Hiders() {
			enemyOptions.addChild(enemyActivateDelay);
			enemyOptions.addChild(enemyDeactivateDelay);
			trigger.addChild(triggerActorActivate);
			trigger.addChild(triggerScreenAt);
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
		PickupWidgets pickup = new PickupWidgets();
		OptionWidgets option = new OptionWidgets();
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
			AlignTable table = new AlignTable();
			Label name = null;
		}

		static class OptionWidgets {
			TextField name = null;
			TextField description = null;
			Slider speed = null;
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

		static class PickupWidgets {
			Label name =  null;
		}
	}
}
