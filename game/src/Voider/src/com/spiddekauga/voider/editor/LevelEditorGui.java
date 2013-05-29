package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
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
import com.spiddekauga.voider.editor.LevelEditor.Tools;
import com.spiddekauga.voider.editor.commands.CGuiSlider;
import com.spiddekauga.voider.editor.commands.CLevelRun;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.AddActorTool;
import com.spiddekauga.voider.scene.AddEnemyTool;
import com.spiddekauga.voider.scene.DrawActorTool;
import com.spiddekauga.voider.scene.PathTool;
import com.spiddekauga.voider.scene.TriggerTool;
import com.spiddekauga.voider.utils.Messages;

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
	}

	@Override
	public void initGui() {
		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setCellPaddingDefault(1, 1, 1, 1);
		mMenuTable.setPreferences(mMainTable);
		mPickupTable.setPreferences(mMainTable);
		mPickupTable.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mStaticTerrainTable.setPreferences(mMainTable);
		mStaticTerrainTable.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mEnemyTable.setPreferences(mMainTable);
		mEnemyTable.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mOptionTable.setPreferences(mMainTable);
		mMainMenuTable.setPreferences(mMainTable);

		initMainMenu(mLevelEditor, "level");
		initOptions();
		initPickup();
		initStaticTerrain();
		initMenu();
		initEnemy();
		initPath();
		initTrigger();

		mMainTable.setTransform(true);
		mMainTable.invalidate();

		super.initGui();
		switchTool(mStaticTerrainTable);
	}

	@Override
	public void resetValues() {
		// Main menu
		switch (mLevelEditor.getSelectedTool()) {
		case ENEMY:
			mWidgets.menu.enemy.setChecked(true);
			mWidgets.enemyMenu.enemy.setChecked(true);
			break;

		case PATH:
			mWidgets.menu.enemy.setChecked(true);
			mWidgets.enemyMenu.path.setChecked(true);
			break;

		case TRIGGER:
			mWidgets.menu.enemy.setChecked(true);
			mWidgets.enemyMenu.trigger.setChecked(true);
			break;

		case PICKUP:
			mWidgets.menu.pickup.setChecked(true);
			break;

		case STATIC_TERRAIN:
			mWidgets.menu.terrain.setChecked(true);
			break;
		}


		// Enemy
		switch (mLevelEditor.getEnemyState()) {
		case ADD:
			mWidgets.enemy.add.setChecked(true);
			break;

		case SELECT:
			mWidgets.enemy.select.setChecked(true);
			break;

		case MOVE:
			mWidgets.enemy.move.setChecked(true);
			break;

		case REMOVE:
			mWidgets.enemy.remove.setChecked(true);
			break;

		case SET_ACTIVATE_TRIGGER:
			mWidgets.enemy.activateTrigger.setChecked(true);
			break;

		case SET_DEACTIVATE_TRIGGER:
			mWidgets.enemy.deactivateTrigger.setChecked(true);
			break;
		}

		// Enemy options
		resetEnemyOptions();


		// General options
		mWidgets.option.description.setText(mLevelEditor.getLevelDescription());
		mWidgets.option.name.setText(mLevelEditor.getLevelName());
		mWidgets.option.revision.setText(mLevelEditor.getLevelRevision());
		mWidgets.option.version.setText(mLevelEditor.getLevelVersion());
		mWidgets.option.storyBefore.setText(mLevelEditor.getStoryBefore());
		mWidgets.option.storyAfter.setText(mLevelEditor.getStoryAfter());
		mWidgets.option.speed.setValue(mLevelEditor.getLevelStartingSpeed());


		// Path
		switch (mLevelEditor.getPathState()) {
		case ADD_CORNER:
			mWidgets.path.add.setChecked(true);
			break;

		case SELECT:
			mWidgets.path.select.setChecked(true);
			break;

		case MOVE:
			mWidgets.path.move.setChecked(true);
			break;

		case REMOVE:
			mWidgets.path.remove.setChecked(true);
			break;
		}

		// Path options
		if (mLevelEditor.isPathSelected()) {
			mHiders.pathOptions.show();
			setPathType(mLevelEditor.getPathType());
		} else {
			mHiders.pathOptions.hide();
		}


		// Pickup
		switch (mLevelEditor.getPickupState()) {
		case ADD:
			mWidgets.pickup.add.setChecked(true);
			break;

		case SELECT:
			// Does nothing
			break;

		case MOVE:
			mWidgets.pickup.move.setChecked(true);
			break;

		case REMOVE:
			mWidgets.pickup.remove.setChecked(true);
			break;
		}

		String pickupName = mLevelEditor.getSelectedPickupName();
		if (pickupName == null) {
			pickupName = Messages.getNoDefSelected("pickup");
		}
		mWidgets.pickup.name.setText(pickupName);
		mWidgets.pickup.name.setSize(mWidgets.pickup.name.getPrefWidth(), mWidgets.pickup.name.getPrefHeight());


		// Terrain
		switch (mLevelEditor.getStaticTerrainState()) {
		case DRAW_APPEND:
			mWidgets.terrain.append.setChecked(true);
			break;

		case ADJUST_ADD_CORNER:
			mWidgets.terrain.addCorner.setChecked(true);
			break;

		case ADJUST_MOVE_CORNER:
			mWidgets.terrain.moveCorner.setChecked(true);
			break;

		case ADJUST_REMOVE_CORNER:
			mWidgets.terrain.removeCorner.setChecked(true);
			break;

		case DRAW_ERASE:
			mWidgets.terrain.drawErase.setChecked(true);
			break;

		case MOVE:
			mWidgets.terrain.move.setChecked(true);
			break;

		case SET_CENTER:
			// Does nothing
			break;

		default:
			break;
		}


		// Trigger
		switch (mLevelEditor.getTriggerState()) {
		case ADD:
			mWidgets.trigger.add.setChecked(true);
			break;

		case MOVE:
			mWidgets.trigger.move.setChecked(true);
			break;

		case REMOVE:
			mWidgets.trigger.remove.setChecked(true);
			break;

		case SELECT:
			// Does nothing
			break;
		}
	}

	/**
	 * Resets enemy option values
	 */
	void resetEnemyOptions() {
		String enemyName = mLevelEditor.getSelectedEnemyName();
		if (enemyName == null) {
			enemyName = Messages.getNoDefSelected("enemy");
		}
		mWidgets.enemy.name.setText(enemyName);
		mWidgets.enemy.name.setSize(mWidgets.enemy.name.getPrefWidth(), mWidgets.enemy.name.getPrefHeight());


		if (mLevelEditor.isEnemySelected()) {
			mHiders.enemyOptions.show();
		} else {
			mHiders.enemyOptions.hide();

			// Current state is set active/deactive trigger, change to select instead
			if (mLevelEditor.getEnemyState() == AddEnemyTool.States.SET_ACTIVATE_TRIGGER ||
					mLevelEditor.getEnemyState() == AddEnemyTool.States.SET_DEACTIVATE_TRIGGER) {
				mLevelEditor.setEnemyState(AddEnemyTool.States.SELECT);
			}

			return;
		}

		if (mWidgets.enemy.cEnemies.getValue() != mLevelEditor.getEnemyCount()) {
			mInvoker.execute(new CGuiSlider(mWidgets.enemy.cEnemies, mLevelEditor.getEnemyCount(), mWidgets.enemy.cEnemies.getValue()), true);
		}

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



	/**
	 * Initializes the top menu
	 */
	private void initMenu() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		final TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);

		ButtonGroup toggleGroup = new ButtonGroup();

		Button button  = new TextButton("Save", textStyle);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mLevelEditor.saveDef();
			}
		};
		mMenuTable.add(button);

		button = new TextButton("Run", textStyle);
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
		button = new TextButton("Static Terrain", textToogleStyle);
		button.setName("static");
		mWidgets.menu.terrain = button;
		button.addListener(menuChecker);
		tooltipListener = new TooltipListener(button, "Static Terrain", Messages.Tooltip.Level.Menu.TERRAIN);
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.STATIC_TERRAIN);
					switchTool(mStaticTerrainTable);
				}
			}
		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		button = new TextButton("Pickup", textToogleStyle);
		mWidgets.menu.pickup = button;
		button.addListener(menuChecker);
		tooltipListener = new TooltipListener(button, "Pickup", Messages.Tooltip.Level.Menu.PICKUP);
		new ButtonListener(button, tooltipListener) {
			@Override
			public void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PICKUP);
					switchTool(mPickupTable);
				}
			}
		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		button = new TextButton("Enemy", textToogleStyle);
		mWidgets.menu.enemy = button;
		button.addListener(menuChecker);
		tooltipListener = new TooltipListener(button, "Enemy", Messages.Tooltip.Level.Menu.ENEMY);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					switchTool(mEnemyTable);

					// Which is currently active from the enemy table?
					if (mWidgets.enemyMenu.enemy.isChecked()) {
						mLevelEditor.switchTool(Tools.ENEMY);
						resetEnemyOptions();
					} else if (mWidgets.enemyMenu.path.isChecked()) {
						mLevelEditor.switchTool(Tools.PATH);
					} else if (mWidgets.enemyMenu.trigger.isChecked()) {
						mLevelEditor.switchTool(Tools.TRIGGER);
					}
				}
			}
		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		button = new TextButton("Options", textStyle);
		tooltipListener = new TooltipListener(button, "Options", Messages.Tooltip.Level.Menu.OPTION);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				AlignTable test = new AlignTable();
				Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
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

		mMainTable.add(mMenuTable);
		mMainTable.setTransform(true);

		mMainTable.invalidate();
	}

	/**
	 * Initializes options content for message box
	 */
	private void initOptions() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		SliderStyle sliderStyle = skin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = skin.get("default", TextFieldStyle.class);
		LabelStyle labelStyle = skin.get("default", LabelStyle.class);

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


		left.row();
		label = new Label("Revision:", labelStyle);
		new TooltipListener(label, "Revision", Messages.Tooltip.Level.Option.REVISION);
		left.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		label = new Label("", labelStyle);
		new TooltipListener(label, "Revision", Messages.Tooltip.Level.Option.REVISION);
		left.add(label);
		mWidgets.option.revision = label;


		left.row();
		label = new Label("Current version:", labelStyle);
		new TooltipListener(label, "Version", Messages.Tooltip.Level.Option.VERSION);
		left.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		label = new Label("", labelStyle);
		new TooltipListener(label, "Version", Messages.Tooltip.Level.Option.VERSION);
		left.add(label);
		mWidgets.option.version = label;


		// RIGHT
		right.row();
		label = new Label("Story before level", labelStyle);
		new TooltipListener(label, "Story before", Messages.Tooltip.Level.Option.STORY_BEFORE);
		right.add(label);

		right.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", textFieldStyle);
		textField.setMaxLength(Config.Editor.STORY_LENGTH_MAX);
		right.add(textField).setFillWidth(true).setFillHeight(true);
		mWidgets.option.storyBefore = textField;
		new TooltipListener(textField, "Story before", Messages.Tooltip.Level.Option.STORY_BEFORE);
		new TextFieldListener(textField, "Write a story (optional)...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setStoryBefore(newText);
			}
		};


		right.row();
		label = new Label("Afterstory", labelStyle);
		new TooltipListener(label, "Afterstory", Messages.Tooltip.Level.Option.STORY_AFTER);
		right.add(label);

		right.row().setFillWidth(true).setFillHeight(true);
		textField = new TextField("", textFieldStyle);
		textField.setMaxLength(Config.Editor.STORY_LENGTH_MAX);
		right.add(textField).setFillWidth(true).setFillHeight(true);
		mWidgets.option.storyAfter = textField;
		new TooltipListener(textField, "Afterstory", Messages.Tooltip.Level.Option.STORY_AFTER);
		new TextFieldListener(textField, "Write the story that will be displayed when the level is completed (optional)", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setStoryAfter(newText);
			}
		};

		mOptionTable.setTransform(true);
		mOptionTable.layout();
		mOptionTable.setKeepSize(true);
	}

	/**
	 * Initializes Enemy tool GUI
	 */
	private void initEnemy() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle textStyle = skin.get("default", TextButtonStyle.class);
		TextButtonStyle toggleStyle = skin.get("toggle", TextButtonStyle.class);
		SliderStyle sliderStyle = skin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = skin.get("default", TextFieldStyle.class);
		LabelStyle labelStyle = skin.get("default", LabelStyle.class);

		mEnemyTable.row();
		GuiCheckCommandCreator enemyOuterMenu = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button = new TextButton("Enemy", toggleStyle);
		mWidgets.enemyMenu.enemy = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyOuterMenu);
		TooltipListener tooltipListener = new TooltipListener(button, "Enemy", Messages.Tooltip.Level.EnemyMenu.ENEMY);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mLevelEditor.switchTool(Tools.ENEMY);
			}
		};
		mHiders.enemy.setButton(button);

		button = new TextButton("Path", toggleStyle);
		mWidgets.enemyMenu.path = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyOuterMenu);
		tooltipListener = new TooltipListener(button, "Path", Messages.Tooltip.Level.EnemyMenu.PATH);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mLevelEditor.switchTool(Tools.PATH);
			}
		};
		mHiders.path.setButton(button);

		button = new TextButton("Trigger", toggleStyle);
		mWidgets.enemyMenu.trigger = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyOuterMenu);
		tooltipListener = new TooltipListener(button, "Trigger", Messages.Tooltip.Level.EnemyMenu.TRIGGER);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mLevelEditor.switchTool(Tools.TRIGGER);
			}
		};
		mHiders.trigger.setButton(button);


		// ---- Enemy ----
		mEnemyTable.row();
		GuiCheckCommandCreator enemyInnerMenu = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup menuGroup = new ButtonGroup();
		button = new TextButton("Select", toggleStyle);
		mWidgets.enemy.select = button;
		mHiders.enemy.addToggleActor(button);
		menuGroup.add(button);
		mEnemyTable.add(button);
		button.addListener(enemyInnerMenu);
		tooltipListener = new TooltipListener(button, "Select enemy", Messages.Tooltip.Level.Enemy.SELECT);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(AddEnemyTool.States.SELECT);
				}
			}
		};

		button = new TextButton("Add", toggleStyle);
		mWidgets.enemy.add = button;
		mHiders.enemy.addToggleActor(button);
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
		mHiders.enemy.addChild(enemyAddHider);


		button = new TextButton("Remove", toggleStyle);
		mWidgets.enemy.remove = button;
		mHiders.enemy.addToggleActor(button);
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

		button = new TextButton("Move", toggleStyle);
		mWidgets.enemy.move = button;
		mHiders.enemy.addToggleActor(button);
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
		mWidgets.enemy.name = label;
		mEnemyTable.add(label).setAlign(Horizontal.RIGHT, Vertical.MIDDLE);

		button = new TextButton("Select type", textStyle);
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
		mHiders.enemyOptions.addToggleActor(label);
		mEnemyTable.add(label);

		mEnemyTable.row();
		Slider slider = new Slider(Level.Enemy.ENEMIES_MIN, Level.Enemy.ENEMIES_MAX, Level.Enemy.ENEMIES_STEP_SIZE, false, sliderStyle);
		mHiders.enemyOptions.addToggleActor(slider);
		mWidgets.enemy.cEnemies = slider;
		mEnemyTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		mHiders.enemyOptions.addToggleActor(textField);
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
		mHiders.enemyOptions.addChild(delayHider);

		// Delay
		mEnemyTable.row();
		label = new Label("Spawn delay between enemies", labelStyle);
		new TooltipListener(label, "Spawn delay", Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		delayHider.addToggleActor(label);
		mEnemyTable.add(label);

		mEnemyTable.row();
		slider = new Slider(Level.Enemy.DELAY_BETWEEN_MIN, Level.Enemy.DELAY_BETWEEN_MAX, Level.Enemy.DELAY_BETWEEN_STEP_SIZE, false, sliderStyle);
		delayHider.addToggleActor(slider);
		mWidgets.enemy.betweenDelay = slider;
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
		button = new TextButton("Set activate trigger", toggleStyle);
		menuGroup.add(button);
		mHiders.enemyOptions.addToggleActor(button);
		mWidgets.enemy.activateTrigger = button;
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
		mHiders.enemyActivateDelay.addToggleActor(label);

		mEnemyTable.row();
		slider = new Slider(Level.Enemy.TRIGGER_ACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_ACTIVATE_DELAY_MAX, Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, false, sliderStyle);
		mHiders.enemyActivateDelay.addToggleActor(slider);
		mWidgets.enemy.activateDelay = slider;
		mEnemyTable.add(slider);
		textField = new TextField("", textFieldStyle);
		mHiders.enemyActivateDelay.addToggleActor(textField);
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
		button = new TextButton("Set deactivate trigger", toggleStyle);
		menuGroup.add(button);
		mHiders.enemyOptions.addToggleActor(button);
		mWidgets.enemy.deactivateTrigger = button;
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
		mHiders.enemyDeactivateDelay.addToggleActor(label);

		mEnemyTable.row();
		slider = new Slider(Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MAX, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_STEP_SIZE, false, sliderStyle);
		mHiders.enemyDeactivateDelay.addToggleActor(slider);
		mWidgets.enemy.deactivateDelay = slider;
		mEnemyTable.add(slider);
		textField = new TextField("", textFieldStyle);
		mHiders.enemyDeactivateDelay.addToggleActor(textField);
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
		Skin skin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle toggleStyle = skin.get("toggle", TextButtonStyle.class);

		// ---- PATH -----
		mEnemyTable.row();
		GuiCheckCommandCreator pathMenu = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button = new TextButton("Select", toggleStyle);
		mWidgets.path.select = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mHiders.path.addToggleActor(button);
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

		button = new TextButton("Add", toggleStyle);
		mWidgets.path.add = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mHiders.path.addToggleActor(button);
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

		button = new TextButton("Remove", toggleStyle);
		mWidgets.path.remove = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mHiders.path.addToggleActor(button);
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

		button = new TextButton("Move", toggleStyle);
		mWidgets.path.move = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mHiders.path.addToggleActor(button);
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
		button = new TextButton("Once", toggleStyle);
		mWidgets.path.once = button;
		mEnemyTable.add(button);
		buttonGroup.add(button);
		mHiders.pathOptions.addToggleActor(button);
		tooltipListener = new TooltipListener(button, "Once", Messages.Tooltip.Level.Path.ONCE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.ONCE);
				}
			}
		};

		button = new TextButton("Loop", toggleStyle);
		mWidgets.path.loop = button;
		mEnemyTable.add(button);
		buttonGroup.add(button);
		mHiders.pathOptions.addToggleActor(button);
		tooltipListener = new TooltipListener(button, "Loop", Messages.Tooltip.Level.Path.LOOP);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.LOOP);
				}
			}
		};

		button = new TextButton("Back & Forth", toggleStyle);
		mWidgets.path.backAndForth = button;
		mEnemyTable.add(button);
		buttonGroup.add(button);
		mHiders.pathOptions.addToggleActor(button);
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
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle toggleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);
		ImageButtonStyle addStyle = editorSkin.get("add", ImageButtonStyle.class);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);

		ButtonGroup toggleGroup = new ButtonGroup();
		Button button = new ImageButton(addStyle);
		mWidgets.pickup.add = button;
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

		button = new TextButton("Remove", toggleStyle);
		mWidgets.pickup.remove = button;
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

		button = new TextButton("Move", toggleStyle);
		mWidgets.pickup.move = button;
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
		mWidgets.pickup.name = label;
		mPickupTable.add(label);

		button = new TextButton("Select type", textStyle);
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
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle textStyle = editorSkin.get("toggle", TextButtonStyle.class);
		GuiCheckCommandCreator terrainShapeChecker = new GuiCheckCommandCreator(mInvoker);


		ButtonGroup toggleGroup = new ButtonGroup();
		Button button = new TextButton("Draw/Append", textStyle);
		button.addListener(terrainShapeChecker);
		mWidgets.terrain.append = button;
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

		button = new TextButton("Add corner", textStyle);
		button.addListener(terrainShapeChecker);
		mWidgets.terrain.addCorner = button;
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

		button = new TextButton("Move corner", textStyle);
		button.addListener(terrainShapeChecker);
		mWidgets.terrain.moveCorner = button;
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

		button = new TextButton("Remove corner", textStyle);
		button.addListener(terrainShapeChecker);
		mWidgets.terrain.removeCorner = button;
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

		button = new TextButton("Draw/Erase", textStyle);
		button.addListener(terrainShapeChecker);
		mWidgets.terrain.drawErase = button;
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

		button = new TextButton("Move", textStyle);
		button.addListener(terrainShapeChecker);
		mWidgets.terrain.move = button;
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
		Skin skin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle toggleStyle = skin.get("toggle", TextButtonStyle.class);

		// ---- Trigger -----
		mEnemyTable.row();
		GuiCheckCommandCreator triggerMenu = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button = new TextButton("Add", toggleStyle);
		mWidgets.trigger.add = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mHiders.trigger.addToggleActor(button);
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

		button = new TextButton("Remove", toggleStyle);
		mWidgets.trigger.remove = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mHiders.trigger.addToggleActor(button);
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

		button = new TextButton("Move", toggleStyle);
		mWidgets.trigger.move = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		mHiders.trigger.addToggleActor(button);
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

	/**
	 * Switches the GUI to the selected tool
	 * @param toolTable the tool's table we want to activate
	 */
	private void switchTool(AlignTable toolTable) {
		mMainTable.clear();
		toolTable.invalidate();
		mMainTable.add(mMenuTable);
		mMainTable.row();
		mMainTable.add(toolTable);
		mMainTable.invalidate();
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
	private InnerWidgets mWidgets = new InnerWidgets();
	/** All hiders  */
	private Hiders mHiders = new Hiders();

	/**
	 * Container for all hiders
	 */
	private static class Hiders {
		/**
		 * Sets correct children etc.
		 */
		public Hiders() {
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
	}

	/**
	 * Container for inner widgets
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
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
			Label version = null;
			TextField storyBefore = null;
			TextField storyAfter = null;
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
