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
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Level;
import com.spiddekauga.voider.editor.LevelEditor.ToolGroups;
import com.spiddekauga.voider.editor.LevelEditor.Tools;
import com.spiddekauga.voider.editor.commands.CGuiSlider;
import com.spiddekauga.voider.editor.commands.CLevelRun;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.SkinNames.EditorIcons;
import com.spiddekauga.voider.scene.AddActorTool;
import com.spiddekauga.voider.scene.AddEnemyTool;
import com.spiddekauga.voider.scene.DrawActorTool;
import com.spiddekauga.voider.scene.PathTool;
import com.spiddekauga.voider.scene.TriggerTool;
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


		mMenuTable.setPreferences(mMainTable);
		mMenuTable.setName("Menu");
		mPickupTable.setPreferences(mMainTable);
		mPickupTable.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mStaticTerrainTable.setPreferences(mMainTable);
		mStaticTerrainTable.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mEnemyTable.setPreferences(mMainTable);
		mEnemyTable.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mOptionTable.setPreferences(mMainTable);
		//		mFileMenuTable.setPreferences(mMainTable);

		mOldHiders = new OldHiders();

		//		initMainMenu(mLevelEditor, "level");
		//		initOptions();
		//		initStaticTerrain();
		//		initPickup();
		//		initMenu();
		//		initEnemyMenu();
		//		initEnemy();
		//		initPath();
		//		initTrigger();

		initToolMenu();

		mMainTable.setTransform(true);
		mMainTable.invalidate();
		mMainTable.add(mMenuTable);
		mMainTable.row();
		mMainTable.add(mStaticTerrainTable);
		mMainTable.add(mPickupTable);
	}

	@Override
	public void dispose() {
		mPickupTable.dispose();
		mStaticTerrainTable.dispose();
		mEnemyTable.dispose();
		mOptionTable.dispose();
		mMenuTable.dispose();

		super.dispose();
	}

	@Override
	public void resetValues() {

		//		// Main menu
		//		switch (mLevelEditor.getSelectedTool()) {
		//		case ENEMY:
		//			mOldWidgets.menu.enemy.setChecked(true);
		//			mOldWidgets.enemyMenu.enemy.setChecked(true);
		//			break;
		//
		//		case PATH:
		//			mOldWidgets.menu.enemy.setChecked(true);
		//			mOldWidgets.enemyMenu.path.setChecked(true);
		//			break;
		//
		//		case TRIGGER:
		//			mOldWidgets.menu.enemy.setChecked(true);
		//			mOldWidgets.enemyMenu.trigger.setChecked(true);
		//			break;
		//
		//		case PICKUP:
		//			mOldWidgets.menu.pickup.setChecked(true);
		//			break;
		//
		//		case STATIC_TERRAIN:
		//			mOldWidgets.menu.terrain.setChecked(true);
		//			break;
		//		}
		//
		//
		//		// Enemy
		//		if (mLevelEditor.getSelectedTool() == ToolGroups.ENEMY) {
		//			switch (mLevelEditor.getEnemyState()) {
		//			case ADD:
		//				mOldWidgets.enemy.add.setChecked(true);
		//				break;
		//
		//			case SELECT:
		//				mOldWidgets.enemy.select.setChecked(true);
		//				break;
		//
		//			case MOVE:
		//				mOldWidgets.enemy.move.setChecked(true);
		//				break;
		//
		//			case REMOVE:
		//				mOldWidgets.enemy.remove.setChecked(true);
		//				break;
		//
		//			case SET_ACTIVATE_TRIGGER:
		//				mOldWidgets.enemy.activateTrigger.setChecked(true);
		//				break;
		//
		//			case SET_DEACTIVATE_TRIGGER:
		//				mOldWidgets.enemy.deactivateTrigger.setChecked(true);
		//				break;
		//			}
		//		}
		//
		//		// Enemy options
		//		resetEnemyOptions();
		//
		//
		//		// General options
		//		mOldWidgets.option.description.setText(mLevelEditor.getLevelDescription());
		//		mOldWidgets.option.name.setText(mLevelEditor.getLevelName());
		//		mOldWidgets.option.revision.setText(mLevelEditor.getLevelRevision());
		//		mOldWidgets.option.storyBefore.setText(mLevelEditor.getPrologue());
		//		mOldWidgets.option.epilogue.setText(mLevelEditor.getEpilogue());
		//		mOldWidgets.option.speed.setValue(mLevelEditor.getLevelStartingSpeed());
		//
		//
		//		// Path
		//		if (mLevelEditor.getSelectedTool() == ToolGroups.PATH) {
		//			switch (mLevelEditor.getPathState()) {
		//			case ADD_CORNER:
		//				mOldWidgets.path.add.setChecked(true);
		//				break;
		//
		//			case SELECT:
		//				mOldWidgets.path.select.setChecked(true);
		//				break;
		//
		//			case MOVE:
		//				mOldWidgets.path.move.setChecked(true);
		//				break;
		//
		//			case REMOVE:
		//				mOldWidgets.path.remove.setChecked(true);
		//				break;
		//			}
		//		}
		//
		//		// Path options
		//		if (mLevelEditor.isPathSelected()) {
		//			mOldHiders.pathOptions.show();
		//			setPathType(mLevelEditor.getPathType());
		//		} else {
		//			mOldHiders.pathOptions.hide();
		//		}
		//
		//
		//		// Pickup
		//		switch (mLevelEditor.getPickupState()) {
		//		case ADD:
		//			mOldWidgets.pickup.add.setChecked(true);
		//			break;
		//
		//		case SELECT:
		//			// Does nothing
		//			break;
		//
		//		case MOVE:
		//			mOldWidgets.pickup.move.setChecked(true);
		//			break;
		//
		//		case REMOVE:
		//			mOldWidgets.pickup.remove.setChecked(true);
		//			break;
		//		}
		//
		//		String pickupName = mLevelEditor.getSelectedPickupName();
		//		if (pickupName == null) {
		//			pickupName = Messages.getNoDefSelected("pickup");
		//		}
		//		mOldWidgets.pickup.name.setText(pickupName);
		//		mOldWidgets.pickup.name.setSize(mOldWidgets.pickup.name.getPrefWidth(), mOldWidgets.pickup.name.getPrefHeight());
		//
		//
		//		// Terrain
		//		switch (mLevelEditor.getStaticTerrainState()) {
		//		case DRAW_APPEND:
		//			mOldWidgets.terrain.append.setChecked(true);
		//			break;
		//
		//		case ADJUST_ADD_CORNER:
		//			mOldWidgets.terrain.addCorner.setChecked(true);
		//			break;
		//
		//		case ADJUST_MOVE_CORNER:
		//			mOldWidgets.terrain.moveCorner.setChecked(true);
		//			break;
		//
		//		case ADJUST_REMOVE_CORNER:
		//			mOldWidgets.terrain.removeCorner.setChecked(true);
		//			break;
		//
		//		case DRAW_ERASE:
		//			mOldWidgets.terrain.drawErase.setChecked(true);
		//			break;
		//
		//		case MOVE:
		//			mOldWidgets.terrain.move.setChecked(true);
		//			break;
		//
		//		case SET_CENTER:
		//			// Does nothing
		//			break;
		//
		//		default:
		//			break;
		//		}
		//
		//
		//		// Trigger
		//		if (mLevelEditor.getSelectedTool() == ToolGroups.TRIGGER) {
		//			switch (mLevelEditor.getTriggerState()) {
		//			case ADD:
		//				mOldWidgets.trigger.add.setChecked(true);
		//				break;
		//
		//			case MOVE:
		//				mOldWidgets.trigger.move.setChecked(true);
		//				break;
		//
		//			case REMOVE:
		//				mOldWidgets.trigger.remove.setChecked(true);
		//				break;
		//
		//			case SELECT:
		//				// Does nothing
		//				break;
		//			}
		//		}
	}

	/**
	 * Resets enemy option values
	 */
	void resetEnemyOptions() {
		//		String enemyName = mLevelEditor.getSelectedEnemyName();
		//		if (enemyName == null) {
		//			enemyName = Messages.getNoDefSelected("enemy");
		//		}
		//		mOldWidgets.enemy.name.setText(enemyName);
		//		mOldWidgets.enemy.name.setSize(mOldWidgets.enemy.name.getPrefWidth(), mOldWidgets.enemy.name.getPrefHeight());
		//
		//
		//		if (mLevelEditor.isEnemySelected()) {
		//			mOldHiders.enemyOptions.show();
		//		} else {
		//			mOldHiders.enemyOptions.hide();
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
		//		if (mOldWidgets.enemy.cEnemies.getValue() != mLevelEditor.getEnemyCount()) {
		//			mInvoker.execute(new CGuiSlider(mOldWidgets.enemy.cEnemies, mLevelEditor.getEnemyCount(), mOldWidgets.enemy.cEnemies.getValue()), true);
		//		}
		//
		//		if (mLevelEditor.getEnemySpawnDelay() >= 0 &&  mLevelEditor.getEnemySpawnDelay() != mOldWidgets.enemy.betweenDelay.getValue()) {
		//			mInvoker.execute(new CGuiSlider(mOldWidgets.enemy.betweenDelay, mLevelEditor.getEnemySpawnDelay(), mOldWidgets.enemy.betweenDelay.getValue()), true);
		//		}
		//
		//
		//		// Has activate trigger -> Show trigger delay
		//		if (mLevelEditor.hasSelectedEnemyActivateTrigger()) {
		//			mOldHiders.enemyActivateDelay.show();
		//
		//			float activateDelay = mLevelEditor.getSelectedEnemyActivateTriggerDelay();
		//			if (activateDelay >= 0 && activateDelay != mOldWidgets.enemy.activateDelay.getValue()) {
		//				mInvoker.execute(new CGuiSlider(mOldWidgets.enemy.activateDelay, activateDelay, mOldWidgets.enemy.activateDelay.getValue()));
		//			}
		//		} else {
		//			mOldHiders.enemyActivateDelay.hide();
		//		}
		//
		//
		//		// Has deactivate trigger -> Show trigger delay
		//		if (mLevelEditor.hasSelectedEnemyDeactivateTrigger()) {
		//			mOldHiders.enemyDeactivateDelay.show();
		//
		//			float deactivateDelay = mLevelEditor.getSelectedEnemyDeactivateTriggerDelay();
		//			if (deactivateDelay >= 0 && deactivateDelay!= mOldWidgets.enemy.deactivateDelay.getValue()) {
		//				mInvoker.execute(new CGuiSlider(mOldWidgets.enemy.deactivateDelay, deactivateDelay, mOldWidgets.enemy.deactivateDelay.getValue()));
		//			}
		//		} else {
		//			mOldHiders.enemyDeactivateDelay.hide();
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

		// Terrain add_move_corner
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Terrain, add/move corner", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.TERRAIN_ADD_MOVE_CORNER.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_ADJUST_ADD_MOVE_CORNER);
				}
			}
		};
		toolButtons.add(button);

		// Terrain remove_corner
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Terrain, remove corner", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.TERRAIN_REMOVE_CORNER.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_ADJUST_REMOVE_CORNER);
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
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_ADD);
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

		// Pickup add
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add pickup", mTextToggleStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.PICKUP_ADD.toString());
		}
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PICKUP_ADD);
				}
			}
		};
		toolButtons.add(button);


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
			mOldWidgets.path.once.setChecked(true);
			break;

		case LOOP:
			mOldWidgets.path.loop.setChecked(true);
			break;

		case BACK_AND_FORTH:
			mOldWidgets.path.backAndForth.setChecked(true);
			break;
		}
	}

	@Override
	protected String getResourceTypeName() {
		return "level";
	}

	/**
	 * Initializes the top menu
	 */
	private void initMenu() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		final TextButtonStyle textButtonStyle = generalSkin.get("default", TextButtonStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		ButtonGroup toggleGroup = new ButtonGroup();

		Button button;
		// Save
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Save", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mLevelEditor.saveDef();
			}
		};
		mMenuTable.add(button);

		// Undo
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Undo", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mLevelEditor.undo();
			}
		};
		mMenuTable.add(button);

		// Redo
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Redo", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mLevelEditor.redo();
			}
		};
		mMenuTable.add(button);

		// Run
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Run", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin);
		}
		TooltipListener tooltipListener = new TooltipListener(button, "Run", Messages.Tooltip.Level.Menu.RUN);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				MsgBoxExecuter msgBox = getFreeMsgBox();

				msgBox.setTitle(Messages.Level.RUN_INVULNERABLE_TITLE);
				msgBox.content(Messages.Level.RUN_INVULNERABLE_CONTENT);
				msgBox.button("Can die", new CLevelRun(false, mLevelEditor));
				msgBox.button("Invulnerable", new CLevelRun(true, mLevelEditor));
				msgBox.addCancelButtonAndKeys();
				showMsgBox(msgBox);
			}
		};
		mMenuTable.add(button);

		GuiCheckCommandCreator menuChecker = new GuiCheckCommandCreator(mInvoker);
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Terrain", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.setName("static");
		mOldWidgets.menu.terrain = button;
		button.addListener(menuChecker);
		tooltipListener = new TooltipListener(button, "Terrain", Messages.Tooltip.Level.Menu.TERRAIN);
		mOldHiders.terrain.setButton(button);
		//		new ButtonListener(button, tooltipListener) {
		//			@Override
		//			public void onChecked(boolean checked) {
		//				if (checked) {
		//					mLevelEditor.switchTool(Tools.STATIC_TERRAIN);
		//					switchTool(mStaticTerrainTable);
		//				}
		//			}
		//		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Pickup", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.menu.pickup = button;
		button.addListener(menuChecker);
		tooltipListener = new TooltipListener(button, "Pickup", Messages.Tooltip.Level.Menu.PICKUP);
		mOldHiders.pickups.setButton(button);
		//		new ButtonListener(button, tooltipListener) {
		//			@Override
		//			public void onChecked(boolean checked) {
		//				if (checked) {
		//					mLevelEditor.switchTool(Tools.PICKUP);
		//					switchTool(mPickupTable);
		//				}
		//			}
		//		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Enemy", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.menu.enemy = button;
		button.addListener(menuChecker);
		tooltipListener = new TooltipListener(button, "Enemy", Messages.Tooltip.Level.Menu.ENEMY);
		//		new ButtonListener(button, tooltipListener) {
		//			@Override
		//			protected void onChecked(boolean checked) {
		//				if (checked) {
		//					switchTool(mEnemyTable);
		//
		//					// Which is currently active from the enemy table?
		//					if (mWidgets.enemyMenu.enemy.isChecked()) {
		//						mLevelEditor.switchTool(Tools.ENEMY);
		//						resetEnemyOptions();
		//					} else if (mWidgets.enemyMenu.path.isChecked()) {
		//						mLevelEditor.switchTool(Tools.PATH);
		//					} else if (mWidgets.enemyMenu.trigger.isChecked()) {
		//						mLevelEditor.switchTool(Tools.TRIGGER);
		//					}
		//				}
		//			}
		//		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Options", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		tooltipListener = new TooltipListener(button, "Options", Messages.Tooltip.Level.Menu.OPTION);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				AlignTable test = new AlignTable();
				Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
				LabelStyle labelStyle = editorSkin.get(LabelStyle.class);
				Label label = new Label("Test label", labelStyle);
				test.add(label);
				test.invalidate();

				MsgBoxExecuter msgBox = getFreeMsgBox();

				msgBox.clear();
				msgBox.setTitle("Level options");
				msgBox.content(mOptionTable);
				msgBox.addCancelButtonAndKeys("OK");
				showMsgBox(msgBox);
			}
		};
		mMenuTable.add(button);
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
		mOldWidgets.option.name = textField;
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
		mOldWidgets.option.description = textField;
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
		mOldWidgets.option.speed = slider;

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
		mOldWidgets.option.revision = label;


		// RIGHT
		right.row();
		label = new Label("Prologue", labelStyle);
		new TooltipListener(label, "Prologue", Messages.Tooltip.Level.Option.STORY_BEFORE);
		right.add(label);

		right.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", textFieldStyle);
		textField.setMaxLength(Config.Editor.STORY_LENGTH_MAX);
		right.add(textField).setFillWidth(true).setFillHeight(true);
		mOldWidgets.option.storyBefore = textField;
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
		mOldWidgets.option.epilogue = textField;
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
	 * Initializes the enemy menu where you can switch between, enemy/path/trigger
	 */
	private void initEnemyMenu() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);


		mEnemyTable.row();
		GuiCheckCommandCreator enemyOuterMenu = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Enemy", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.enemyMenu.enemy = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyOuterMenu);
		TooltipListener tooltipListener = new TooltipListener(button, "Enemy", Messages.Tooltip.Level.EnemyMenu.ENEMY);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchToolGroup(ToolGroups.ENEMY);
				}
			}
		};
		mOldHiders.enemy.setButton(button);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Path", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.enemyMenu.path = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyOuterMenu);
		tooltipListener = new TooltipListener(button, "Path", Messages.Tooltip.Level.EnemyMenu.PATH);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchToolGroup(ToolGroups.PATH);
				}
			}
		};
		mOldHiders.path.setButton(button);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Trigger", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.enemyMenu.trigger = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyOuterMenu);
		tooltipListener = new TooltipListener(button, "Trigger", Messages.Tooltip.Level.EnemyMenu.TRIGGER);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchToolGroup(ToolGroups.TRIGGER);
				}
			}
		};
		mOldHiders.trigger.setButton(button);
	}

	/**
	 * Initializes Enemy Tool GUI
	 */
	private void initEnemy() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textButtonStyle = generalSkin.get("default", TextButtonStyle.class);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		SliderStyle sliderStyle = generalSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = generalSkin.get("default", TextFieldStyle.class);
		LabelStyle labelStyle = generalSkin.get("default", LabelStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		mEnemyTable.row();
		GuiCheckCommandCreator enemyInnerMenu = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup menuGroup = new ButtonGroup();
		Button button = null;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.enemy.select = button;
		mOldHiders.enemy.addToggleActor(button);
		menuGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyInnerMenu);
		TooltipListener tooltipListener = new TooltipListener(button, "Select enemy", Messages.Tooltip.Level.Enemy.SELECT);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(AddEnemyTool.States.SELECT);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.enemy.add = button;
		mOldHiders.enemy.addToggleActor(button);
		menuGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyInnerMenu);
		tooltipListener = new TooltipListener(button, "Add enemy", Messages.Tooltip.Level.Enemy.ADD);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(AddEnemyTool.States.ADD);
				}
			}
		};
		HideListener enemyAddHider = new HideListener(button, true);
		mOldHiders.enemy.addChild(enemyAddHider);


		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Remove", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.enemy.remove = button;
		mOldHiders.enemy.addToggleActor(button);
		menuGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyInnerMenu);
		tooltipListener = new TooltipListener(button, "Remove enemy", Messages.Tooltip.Level.Enemy.REMOVE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(AddEnemyTool.States.REMOVE);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.enemy.move = button;
		mOldHiders.enemy.addToggleActor(button);
		menuGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyInnerMenu);
		tooltipListener = new TooltipListener(button, "Move enemy", Messages.Tooltip.Level.Enemy.MOVE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(AddEnemyTool.States.MOVE);
				}
			}
		};

		// Select type
		mEnemyTable.row();
		Label label = new Label("", labelStyle);
		new TooltipListener(label, "Enemy type name", Messages.Tooltip.Level.Enemy.SELECT_NAME);
		enemyAddHider.addToggleActor(label);
		mOldWidgets.enemy.name = label;
		mEnemyTable.add(label).setAlign(Horizontal.RIGHT, Vertical.MIDDLE);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select type", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		enemyAddHider.addToggleActor(button);
		mEnemyTable.add(button);
		tooltipListener = new TooltipListener(button, "Select enemy type", Messages.Tooltip.Level.Enemy.SELECT_TYPE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mLevelEditor.selectEnemy();
			}
		};


		// Enemy options when an enemy is selected
		// # Enemies
		mEnemyTable.row();
		label = new Label("# Enemies", labelStyle);
		new TooltipListener(label, "No. of enemies", Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		mOldHiders.enemyOptions.addToggleActor(label);
		mEnemyTable.add(label);

		mEnemyTable.row();
		Slider slider = new Slider(Level.Enemy.ENEMIES_MIN, Level.Enemy.ENEMIES_MAX, Level.Enemy.ENEMIES_STEP_SIZE, false, sliderStyle);
		mOldHiders.enemyOptions.addToggleActor(slider);
		mOldWidgets.enemy.cEnemies = slider;
		mEnemyTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		mOldHiders.enemyOptions.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
		mEnemyTable.add(textField);
		new TooltipListener(slider, "No. of enemies", Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		new TooltipListener(textField, "No. of enemies", Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mInvoker.execute(new CGuiSlider(mSlider, newValue, mLevelEditor.getEnemyCount()));
				mLevelEditor.setEnemyCount((int) (newValue + 0.5f));
			}
		};
		HideSliderValue delayHider = new HideSliderValue(slider, 2, Float.MAX_VALUE);
		mOldHiders.enemyOptions.addChild(delayHider);

		// Delay
		mEnemyTable.row();
		label = new Label("Spawn delay between enemies", labelStyle);
		new TooltipListener(label, "Spawn delay", Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		delayHider.addToggleActor(label);
		mEnemyTable.add(label);

		mEnemyTable.row();
		slider = new Slider(Level.Enemy.DELAY_BETWEEN_MIN, Level.Enemy.DELAY_BETWEEN_MAX, Level.Enemy.DELAY_BETWEEN_STEP_SIZE, false, sliderStyle);
		delayHider.addToggleActor(slider);
		mOldWidgets.enemy.betweenDelay = slider;
		mEnemyTable.add(slider);
		textField = new TextField("", textFieldStyle);
		delayHider.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
		mEnemyTable.add(textField);
		new TooltipListener(slider, "Spawn delay", Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		new TooltipListener(textField, "Spawn delay", Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mInvoker.execute(new CGuiSlider(mSlider, newValue, mLevelEditor.getEnemySpawnDelay()));
				mLevelEditor.setEnemySpawnDelay(newValue);
			}
		};

		// Activate trigger
		mEnemyTable.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Set activate trigger", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		menuGroup.add(button);
		mOldHiders.enemyOptions.addToggleActor(button);
		mOldWidgets.enemy.activateTrigger = button;
		mEnemyTable.add(button);
		button.addListener(enemyInnerMenu);
		tooltipListener = new TooltipListener(button, "Set activate trigger", Messages.Tooltip.Level.Enemy.SET_ACTIVATE_TRIGGER);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(AddEnemyTool.States.SET_ACTIVATE_TRIGGER);
				}
			}
		};

		mEnemyTable.row();
		label = new Label("Activate delay", labelStyle);
		new TooltipListener(label, "Activate delay", Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		mOldHiders.enemyActivateDelay.addToggleActor(label);

		mEnemyTable.row();
		slider = new Slider(Level.Enemy.TRIGGER_ACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_ACTIVATE_DELAY_MAX, Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, false, sliderStyle);
		mOldHiders.enemyActivateDelay.addToggleActor(slider);
		mOldWidgets.enemy.activateDelay = slider;
		mEnemyTable.add(slider);
		textField = new TextField("", textFieldStyle);
		mOldHiders.enemyActivateDelay.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
		mEnemyTable.add(textField);
		new TooltipListener(slider, "Activate delay", Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		new TooltipListener(textField, "Activate delay", Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setSelectedEnemyActivateTriggerDelay(newValue);
			}
		};

		// Deactivate trigger
		mEnemyTable.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Set deactivate trigger", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		menuGroup.add(button);
		mOldHiders.enemyOptions.addToggleActor(button);
		mOldWidgets.enemy.deactivateTrigger = button;
		mEnemyTable.add(button);
		button.addListener(enemyInnerMenu);
		tooltipListener = new TooltipListener(button, "Set deactivate trigger", Messages.Tooltip.Level.Enemy.SET_DEACTIVATE_DELAY);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(AddEnemyTool.States.SET_DEACTIVATE_TRIGGER);
				}
			}
		};

		mEnemyTable.row();
		label = new Label("Deactivation delay", labelStyle);
		new TooltipListener(label, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		mOldHiders.enemyDeactivateDelay.addToggleActor(label);

		mEnemyTable.row();
		slider = new Slider(Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MAX, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_STEP_SIZE, false, sliderStyle);
		mOldHiders.enemyDeactivateDelay.addToggleActor(slider);
		mOldWidgets.enemy.deactivateDelay = slider;
		mEnemyTable.add(slider);
		textField = new TextField("", textFieldStyle);
		mOldHiders.enemyDeactivateDelay.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
		mEnemyTable.add(textField);
		new TooltipListener(slider, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		new TooltipListener(textField, "Deactivate delay", Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setSelectedEnemyDeactivateTriggerDelay(newValue);
			}
		};
	}

	/**
	 * Initializes path tool GUI
	 */
	private void initPath() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		// ---- PATH -----
		mEnemyTable.row();
		GuiCheckCommandCreator pathMenu = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.path.select = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mOldHiders.path.addToggleActor(button);
		button.addListener(pathMenu);
		TooltipListener tooltipListener = new TooltipListener(button, "Select path", Messages.Tooltip.Level.Path.SELECT);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathState(PathTool.States.SELECT);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.path.add = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mOldHiders.path.addToggleActor(button);
		button.addListener(pathMenu);
		tooltipListener = new TooltipListener(button, "Add path", Messages.Tooltip.Level.Path.ADD);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathState(PathTool.States.ADD_CORNER);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Remove", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.path.remove = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mOldHiders.path.addToggleActor(button);
		button.addListener(pathMenu);
		tooltipListener = new TooltipListener(button, "Remove path", Messages.Tooltip.Level.Path.REMOVE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathState(PathTool.States.REMOVE);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.path.move = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mOldHiders.path.addToggleActor(button);
		button.addListener(pathMenu);
		tooltipListener = new TooltipListener(button, "Move path", Messages.Tooltip.Level.Path.MOVE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathState(PathTool.States.MOVE);
				}
			}
		};


		// Path options
		mEnemyTable.row();
		buttonGroup = new ButtonGroup();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Once", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.path.once = button;
		mEnemyTable.add(button);
		buttonGroup.add(button);
		mOldHiders.pathOptions.addToggleActor(button);
		tooltipListener = new TooltipListener(button, "Once", Messages.Tooltip.Level.Path.ONCE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.ONCE);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Loop", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.path.loop = button;
		mEnemyTable.add(button);
		buttonGroup.add(button);
		mOldHiders.pathOptions.addToggleActor(button);
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
			button = new TextButton("Back and forth", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.path.backAndForth = button;
		mEnemyTable.add(button);
		buttonGroup.add(button);
		mOldHiders.pathOptions.addToggleActor(button);
		tooltipListener = new TooltipListener(button, "Back and forth", Messages.Tooltip.Level.Path.BACK_AND_FORTH);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.BACK_AND_FORTH);
				}
			}
		};
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

		mOldHiders.pickups.addToggleActor(mPickupTable);

		ButtonGroup toggleGroup = new ButtonGroup();
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.pickup.add = button;
		TooltipListener tooltipListener = new TooltipListener(button, "Add pickup", Messages.Tooltip.Level.Pickup.ADD);
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupState(AddActorTool.States.ADD);
				}
			}
		};
		HideListener addHider = new HideListener(button, true);
		toggleGroup.add(button);
		mPickupTable.add(button);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Remove", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.pickup.remove = button;
		tooltipListener = new TooltipListener(button, "Remove pickup", Messages.Tooltip.Level.Pickup.REMOVE);
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupState(AddActorTool.States.REMOVE);
				}
			}
		};
		toggleGroup.add(button);
		mPickupTable.add(button);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.pickup.move = button;
		tooltipListener = new TooltipListener(button, "Move pickup", Messages.Tooltip.Level.Pickup.MOVE);
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupState(AddActorTool.States.MOVE);
				}
			}
		};
		toggleGroup.add(button);
		mPickupTable.add(button);

		mPickupTable.row();
		Label label = new Label("", labelStyle);
		new TooltipListener(label, "Pickup name", Messages.Tooltip.Level.Pickup.SELECT_NAME);
		addHider.addToggleActor(label);
		mOldWidgets.pickup.name = label;
		mPickupTable.add(label);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select type", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		tooltipListener = new TooltipListener(button, "Select type", Messages.Tooltip.Level.Pickup.SELECT_TYPE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mLevelEditor.selectPickup();
			}
		};
		addHider.addToggleActor(button);
		mPickupTable.add(button);


		mPickupTable.setTransform(true);
		mPickupTable.invalidate();
	}

	/**
	 * Initializes the static terrain
	 */
	private void initStaticTerrain() {
		Skin genaralSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = genaralSkin.get("toggle", TextButtonStyle.class);
		GuiCheckCommandCreator terrainShapeChecker = new GuiCheckCommandCreator(mInvoker);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		mOldHiders.terrain.addToggleActor(mStaticTerrainTable);

		// Draw/Append
		ButtonGroup toggleGroup = new ButtonGroup();
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Draw/Append", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(terrainShapeChecker);
		mOldWidgets.terrain.append = button;
		TooltipListener tooltipListener = new TooltipListener(button, "Draw/Append", Messages.replaceName(Messages.Tooltip.Actor.Visuals.APPEND, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainState(DrawActorTool.States.DRAW_APPEND);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);

		// Add corner
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add corner", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(terrainShapeChecker);
		mOldWidgets.terrain.addCorner = button;
		tooltipListener = new TooltipListener(button, "Add corner", Messages.replaceName(Messages.Tooltip.Actor.Visuals.ADJUST_ADD_CORNER, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainState(DrawActorTool.States.ADJUST_ADD_CORNER);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);

		// Move corner
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move corner", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(terrainShapeChecker);
		mOldWidgets.terrain.moveCorner = button;
		tooltipListener = new TooltipListener(button, "Move corner", Messages.replaceName(Messages.Tooltip.Actor.Visuals.ADJUST_MOVE_CORNER, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainState(DrawActorTool.States.ADJUST_MOVE_CORNER);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);

		// Remove corner
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Remove corner", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(terrainShapeChecker);
		mOldWidgets.terrain.removeCorner = button;
		tooltipListener = new TooltipListener(button, "Remove corner", Messages.replaceName(Messages.Tooltip.Actor.Visuals.ADJUST_REMOVE_CORNER, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainState(DrawActorTool.States.ADJUST_REMOVE_CORNER);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);

		// Draw/Erase
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Draw/Erase", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(terrainShapeChecker);
		mOldWidgets.terrain.drawErase = button;
		tooltipListener = new TooltipListener(button, "Draw/Erase", Messages.replaceName(Messages.Tooltip.Actor.Visuals.ADD_REMOVE, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainState(DrawActorTool.States.DRAW_ERASE);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);

		// Move shape
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(terrainShapeChecker);
		mOldWidgets.terrain.move = button;
		tooltipListener = new TooltipListener(button, "Move shape", Messages.replaceName(Messages.Tooltip.Actor.Visuals.MOVE, "terrain"));
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainState(DrawActorTool.States.MOVE);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);


		mStaticTerrainTable.setTransform(true);
		mStaticTerrainTable.invalidate();
	}

	/**
	 * Initializes GUI for the trigger tool
	 */
	private void initTrigger() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		// ---- Trigger -----
		mEnemyTable.row();
		GuiCheckCommandCreator triggerMenu = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.trigger.add = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mOldHiders.trigger.addToggleActor(button);
		button.addListener(triggerMenu);
		TooltipListener tooltipListener = new TooltipListener(button, "Add trigger", Messages.Tooltip.Level.Trigger.ADD);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setTriggerState(TriggerTool.States.ADD);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Remove", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.trigger.remove = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mOldHiders.trigger.addToggleActor(button);
		button.addListener(triggerMenu);
		tooltipListener = new TooltipListener(button, "Remove trigger", Messages.Tooltip.Level.Trigger.REMOVE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setTriggerState(TriggerTool.States.REMOVE);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mOldWidgets.trigger.move = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mOldHiders.trigger.addToggleActor(button);
		button.addListener(triggerMenu);
		tooltipListener = new TooltipListener(button, "Move trigger", Messages.Tooltip.Level.Trigger.MOVE);
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setTriggerState(TriggerTool.States.MOVE);
				}
			}
		};
	}


	/** Wrapper for what tool is currently active */
	private AlignTable mMenuTable = new AlignTable();
	/** Pickup table */
	private AlignTable mPickupTable = new AlignTable();
	/** Static terrain table */
	private AlignTable mStaticTerrainTable = new AlignTable();
	/** Enemy table */
	private AlignTable mEnemyTable = new AlignTable();
	/** Options table */
	private AlignTable mOptionTable = new AlignTable();
	/** Level editor the GUI will act on */
	private LevelEditor mLevelEditor = null;
	/** Invoker for level editor */
	private Invoker mInvoker = null;
	/** Inner widgets */
	private OldInnerWidgets mOldWidgets = new OldInnerWidgets();
	/** All hiders  */
	private OldHiders mOldHiders = null;


	/**
	 * Container for inner widgets
	 */
	private static class InnerWidgets {

	}

	/**
	 * Container for all hiders
	 */
	private static class OldHiders {
		/**
		 * Sets correct children etc.
		 */
		public OldHiders() {
			enemy.addChild(enemyOptions);
			enemyOptions.addChild(enemyActivateDelay);
			enemyOptions.addChild(enemyDeactivateDelay);
			path.addChild(pathOptions);
			trigger.addChild(triggerActorActivate);
			trigger.addChild(triggerScreenAt);
		}

		/** Enemy hider */
		HideListener enemy = new HideListener(true);
		/** Hides enemy options */
		HideManual enemyOptions = new HideManual();
		/** Hides trigger delay for trigger */
		HideManual enemyActivateDelay = new HideManual();
		/** Hides trigger deactivate delay */
		HideManual enemyDeactivateDelay = new HideManual();
		/** Hides the path */
		HideListener path = new HideListener(true);
		/** Hides path options */
		HideManual pathOptions = new HideManual();
		/** Trigger hider */
		HideListener trigger = new HideListener(true);
		/** Hides trigger screen at options */
		HideManual triggerScreenAt = new HideManual();
		/** Hides trigger actor activate options */
		HideManual triggerActorActivate = new HideManual();
		/** Hides static terrain */
		HideListener terrain = new HideListener(true);
		/** Hides pickups */
		HideListener pickups = new HideListener(true);
	}

	/**
	 * Container for inner widgets
	 */
	@SuppressWarnings("javadoc")
	private static class OldInnerWidgets {
		MenuWidgets menu = new MenuWidgets();
		EnemyMenuWidget enemyMenu = new EnemyMenuWidget();
		EnemyWidgets enemy = new EnemyWidgets();
		PathWidgets path = new PathWidgets();
		PickupWidgets pickup = new PickupWidgets();
		TerrainWidgets terrain = new TerrainWidgets();
		TriggerWidgets trigger = new TriggerWidgets();
		OptionWidgets option = new OptionWidgets();

		static class MenuWidgets {
			Button enemy = null;
			Button pickup = null;
			Button terrain = null;
		}

		static class EnemyMenuWidget {
			Button enemy = null;
			Button path = null;
			Button trigger = null;
		}

		static class EnemyWidgets {
			Button select = null;
			Button add = null;
			Button remove = null;
			Button move = null;
			Button activateTrigger = null;
			Button deactivateTrigger = null;

			Slider cEnemies = null;
			Slider betweenDelay = null;
			Slider activateDelay = null;
			Slider deactivateDelay = null;
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

		static class PathWidgets {
			Button select = null;
			Button add = null;
			Button remove = null;
			Button move = null;

			Button once = null;
			Button loop = null;
			Button backAndForth = null;
		}

		static class PickupWidgets {
			Button add = null;
			Button remove = null;
			Button move = null;

			Label name =  null;
		}

		static class TerrainWidgets {
			Button append = null;
			Button addCorner = null;
			Button removeCorner = null;
			Button moveCorner = null;
			Button drawErase = null;
			Button move = null;
		}

		static class TriggerWidgets {
			Button add = null;
			Button remove = null;
			Button move = null;
		}
	}
}
