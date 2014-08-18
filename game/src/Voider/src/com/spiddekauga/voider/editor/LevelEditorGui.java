package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.commands.CGuiSlider;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.HideSliderValue;
import com.spiddekauga.utils.scene.ui.ResourceTextureButton;
import com.spiddekauga.utils.scene.ui.SelectBoxListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Level;
import com.spiddekauga.voider.editor.LevelEditor.Tools;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.EditorIcons;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * GUI for the level editor
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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


		mPickupTable.setPreferences(mMainTable);
		mWidgets.enemy.table.setPreferences(mMainTable);
		mInfoTable.setPreferences(mMainTable);

		mHiders = new Hiders();

		initSettingsMenu();
		initToolMenu();
		initInfo();
		initPathOptions();
		initEnemyOptions();
		initEnemyAddOptions();
	}

	@Override
	public void dispose() {
		mWidgets.enemy.table.dispose();
		mWidgets.path.table.dispose();
		mInfoTable.dispose();

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
		resetTools();
		resetImage();
	}

	/**
	 * Reset tools
	 */
	void resetTools() {
		switch (mLevelEditor.getSelectedTool()) {
		case ADD_MOVE_CORNER:
			mWidgets.tool.cornerAdd.setChecked(true);
			break;

		case DELETE:
			mWidgets.tool.delete.setChecked(true);
			break;

		case ENEMY_ADD:
			mWidgets.tool.enemyAdd.setChecked(true);
			break;

		case ENEMY_SET_ACTIVATE_TRIGGER:
			mWidgets.tool.triggerActivate.setChecked(true);
			break;

		case ENEMY_SET_DEACTIVATE_TRIGGER:
			mWidgets.tool.triggerDeactivate.setChecked(true);
			break;

		case MOVE:
			mWidgets.tool.move.setChecked(true);
			break;

		case PAN:
			mWidgets.tool.pan.setChecked(true);
			break;

		case PATH_ADD:
			mWidgets.tool.pathAdd.setChecked(true);
			break;

		case PICKUP_ADD:
			// TODO
			break;

		case REMOVE_CORNER:
			mWidgets.tool.cornerRemove.setChecked(true);
			break;

		case SELECTION:
			mWidgets.tool.select.setChecked(true);
			break;

		case TERRAIN_DRAW_APPEND:
			mWidgets.tool.drawAppend.setChecked(true);
			break;

		case TERRAIN_DRAW_ERASE:
			mWidgets.tool.drawErase.setChecked(true);
			break;

		default:
			break;

		}
	}

	/**
	 * Reset level info
	 */
	void resetLevelInfo() {
		mWidgets.info.description.setText(mLevelEditor.getDescription());
		mWidgets.info.name.setText(mLevelEditor.getName());
		mWidgets.info.prologue.setText(mLevelEditor.getPrologue());
		mWidgets.info.epilogue.setText(mLevelEditor.getEpilogue());
		mWidgets.info.speed.setValue(mLevelEditor.getLevelStartingSpeed());
		mWidgets.info.theme.setSelectedIndex(mLevelEditor.getTheme().ordinal());
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
	 * Reset enemy add table
	 */
	void resetEnemyAddTable() {
		ArrayList<EnemyActorDef> enemyDefs = mLevelEditor.getAddEnemies();

		mWidgets.enemyAdd.scrollTable.clear();

		int cColumEnemy = 0;
		GuiCheckCommandCreator guiCheckCommandCreator = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		int enemiesPerColumn = Config.Editor.Level.Enemy.LIST_COLUMNS;

		for (EnemyActorDef enemyDef : enemyDefs) {
			Button button = new ResourceTextureButton(enemyDef, (ImageButtonStyle) SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_TOGGLE));


			if (cColumEnemy == enemiesPerColumn) {
				cColumEnemy = 0;
				mWidgets.enemyAdd.scrollTable.row();
			}

			new ButtonListener(button) {
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
		// // Scroll pane width
		// int scrollPaneWidth = Config.Editor.Level.Enemy.ADD_BUTTON_SIZE *
		// getEnemiesPerColumnInAddTable();
		// mWidgets.enemyAdd.scrollTable.setWidth(scrollPaneWidth);
		// // Add margin
		// scrollPaneWidth += 10;
		// mWidgets.enemyAdd.scrollPane.setWidth(scrollPaneWidth);

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
			} else {
				mHiders.enemyActivateDelay.hide();
			}


			// Has deactivate trigger -> Show trigger delay
			if (mLevelEditor.hasSelectedEnemyDeactivateTrigger()) {
				mHiders.enemyDeactivateDelay.show();

				float deactivateDelay = mLevelEditor.getSelectedEnemyDeactivateTriggerDelay();
				if (deactivateDelay >= 0 && deactivateDelay != mWidgets.enemy.deactivateDelay.getValue()) {
					mInvoker.execute(new CGuiSlider(mWidgets.enemy.deactivateDelay, deactivateDelay, mWidgets.enemy.deactivateDelay.getValue()));
				}
			} else {
				mHiders.enemyDeactivateDelay.hide();
			}

		} else {
			mHiders.enemyOptions.hide();
		}
	}

	/**
	 * Initializes the tool menu
	 */
	private void initToolMenu() {
		ButtonGroup buttonGroup = new ButtonGroup();

		// Select
		mToolMenu.row();

		mWidgets.tool.select = mUiFactory.addToolButton(EditorIcons.SELECT, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.select, Messages.EditorTooltips.TOOL_SELECTION);
		new ButtonListener(mWidgets.tool.select) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.SELECTION);
				}
			}
		};


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Pan
		mWidgets.tool.pan = mUiFactory.addToolButton(EditorIcons.PAN, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.pan, Messages.EditorTooltips.TOOL_PAN_LEVEL);
		new ButtonListener(mWidgets.tool.pan) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PAN);
				}
			}
		};

		// Move
		mWidgets.tool.move = mUiFactory.addToolButton(EditorIcons.MOVE, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.move, Messages.EditorTooltips.TOOL_MOVE_LEVEL);
		new ButtonListener(mWidgets.tool.move) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.MOVE);
				}
			}
		};

		// Delete
		mToolMenu.row();
		mWidgets.tool.delete = mUiFactory.addToolButton(EditorIcons.DELETE, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.delete, Messages.EditorTooltips.TOOL_DELETE_LEVEL);
		new ButtonListener(mWidgets.tool.delete) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.DELETE);
				}
			}
		};

		// Cancel
		Button button = mUiFactory.addToolButton(EditorIcons.CANCEL, null, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.TOOL_CLEAR_SELECTION);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mLevelEditor.clearSelection();
			}
		};


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Terrain draw_append
		mWidgets.tool.drawAppend = mUiFactory.addToolButton(EditorIcons.TERRAIN_DRAW_APPEND, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.drawAppend, Messages.EditorTooltips.TOOL_DRAW_APPEND_TERRAIN);
		new ButtonListener(mWidgets.tool.drawAppend) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_DRAW_APPEND);
				}
			}
		};


		// Terrain draw_erase
		mWidgets.tool.drawErase = mUiFactory.addToolButton(EditorIcons.DRAW_ERASE, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.drawErase, Messages.EditorTooltips.TOOL_DRAW_ERASE_TERRAIN);
		new ButtonListener(mWidgets.tool.drawErase) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_DRAW_ERASE);
				}
			}
		};

		// add_move_corner
		mToolMenu.row();
		mWidgets.tool.cornerAdd = mUiFactory.addToolButton(EditorIcons.ADD_MOVE_CORNER, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.cornerAdd, Messages.EditorTooltips.TOOL_DRAW_CORNER_ADD_TERRAIN);
		new ButtonListener(mWidgets.tool.cornerAdd) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ADD_MOVE_CORNER);
				}
			}
		};

		// remove_corner
		mWidgets.tool.cornerRemove = mUiFactory.addToolButton(EditorIcons.REMOVE_CORNER, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.cornerRemove, Messages.EditorTooltips.TOOL_DRAW_CORNER_REMOVE_TERRAIN);
		new ButtonListener(mWidgets.tool.cornerRemove) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.REMOVE_CORNER);
				}
			}
		};


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Enemy add
		mWidgets.tool.enemyAdd = mUiFactory.addToolButton(EditorIcons.ENEMY_ADD, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.enemyAdd, Messages.EditorTooltips.TOOL_ENEMY_ADD);
		new ButtonListener(mWidgets.tool.enemyAdd) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_ADD);
				}
			}
		};

		// Path add
		mWidgets.tool.pathAdd = mUiFactory.addToolButton(EditorIcons.PATH_ADD, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.pathAdd, Messages.EditorTooltips.TOOL_PATH);
		new ButtonListener(mWidgets.tool.pathAdd) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PATH_ADD);
				}
			}
		};


		// Enemy - set activate trigger
		mToolMenu.row();
		mWidgets.tool.triggerActivate = mUiFactory.addToolButton(EditorIcons.ENEMY_SET_ACTIVATE_TRIGGER, buttonGroup, mToolMenu,
				mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.triggerActivate, Messages.EditorTooltips.TOOL_TRIGGER_ACTIVATE);
		new ButtonListener(mWidgets.tool.triggerActivate) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_SET_ACTIVATE_TRIGGER);
				}
			}
		};

		// Enemy - set deactivate trigger
		mWidgets.tool.triggerDeactivate = mUiFactory.addToolButton(EditorIcons.ENEMY_SET_DEACTIVATE_TRIGGER, buttonGroup, mToolMenu,
				mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.triggerDeactivate, Messages.EditorTooltips.TOOL_TRIGGER_DEACTIVATE);
		new ButtonListener(mWidgets.tool.triggerDeactivate) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_SET_DEACTIVATE_TRIGGER);
				}
			}
		};
	}

	@Override
	protected String getResourceTypeName() {
		return "level";
	}

	/**
	 * Initializes level info content for message box
	 */
	private void initInfo() {
		mInfoTable.setAlignTable(Horizontal.CENTER, Vertical.MIDDLE);
		mInfoTable.setAlignRow(Horizontal.LEFT, Vertical.TOP);

		float paddingInner = mUiFactory.getStyles().vars.paddingInner;
		mInfoTable.setPadding(0, paddingInner, 0, paddingInner);

		AlignTable left = new AlignTable();
		AlignTable right = new AlignTable();
		left.setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		right.setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mInfoTable.add(left).setPadRight(mUiFactory.getStyles().vars.paddingInner);
		mInfoTable.add(right);


		TextFieldListener textFieldListener;


		// --- Left side ---
		// Name
		textFieldListener = new TextFieldListener(mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setName(newText);
			}
		};
		mWidgets.info.name = mUiFactory.addTextField("Name", true, Messages.replaceName(Messages.Editor.NAME_FIELD_DEFAULT, getResourceTypeName()),
				textFieldListener, left, mDisabledWhenPublished);
		mWidgets.info.name.setMaxLength(Config.Editor.NAME_LENGTH_MAX);
		mWidgets.info.nameError = mUiFactory.getLastCreatedErrorLabel();

		// Theme
		SelectBoxListener selectBoxListener = new SelectBoxListener() {
			@Override
			protected void onSelectionChanged(int itemIndex) {
				mLevelEditor.setTheme(Themes.values()[itemIndex]);
			}
		};
		mWidgets.info.theme = mUiFactory.addSelectBox("Theme", Themes.values(), selectBoxListener, left, mDisabledWhenPublished);

		// Speed
		mUiFactory.addSection("Level Speed", left, null);
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setLevelStartingSpeed(newValue);
			}
		};
		mWidgets.info.speed = mUiFactory.addSlider(null, Editor.Level.LEVEL_SPEED_MIN, Editor.Level.LEVEL_SPEED_MAX,
				Editor.Level.LEVEL_SPEED_STEP_SIZE, sliderListener, left, null, mDisabledWhenPublished, mInvoker);

		// Screenshot image
		mUiFactory.addSection("Level/Screenshot Image", left, null);
		left.row().setAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mWidgets.info.image = new Image();
		left.add(mWidgets.info.image).setWidth(mUiFactory.getStyles().vars.textFieldWidth);


		// --- Right side ---
		// Description
		textFieldListener = new TextFieldListener(mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setDescription(newText);
			}
		};
		mWidgets.info.description = mUiFactory.addTextArea("Description",
				Messages.replaceName(Messages.Editor.DESCRIPTION_FIELD_DEFAULT, getResourceTypeName()), textFieldListener, right,
				mDisabledWhenPublished);


		// Prologue
		textFieldListener = new TextFieldListener(mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setPrologue(newText);
			}
		};
		mWidgets.info.prologue = mUiFactory
				.addTextArea("Prologue", Messages.Level.PROLOGUE_DEFAULT, textFieldListener, right, mDisabledWhenPublished);

		// Epilogue
		textFieldListener = new TextFieldListener(mInvoker) {
			@Override
			protected void onChange(String newText) {
				mLevelEditor.setEpilogue(newText);
			}
		};
		mWidgets.info.epilogue = mUiFactory
				.addTextArea("Epilogue", Messages.Level.EPILOGUE_DEFAULT, textFieldListener, right, mDisabledWhenPublished);

		mInfoTable.layout();
	}

	/**
	 * Update screenshot image
	 */
	void resetImage() {
		float width = mUiFactory.getStyles().vars.textFieldWidth;
		float height = width / Config.Level.SAVE_TEXTURE_RATIO;

		mWidgets.info.image.setDrawable(mLevelEditor.getImage());
		mWidgets.info.image.setSize(width, height);
		mWidgets.info.image.setVisible(true);
		mWidgets.info.image.invalidate();
	}

	@Override
	protected void initSettingsMenu() {
		super.initSettingsMenu();

		// Add enemy list
		ImageButtonStyle buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.ENEMY_ADD_TAB);
		Button button = mSettingTabs.addTab(buttonStyle, mWidgets.enemyAdd.table, mWidgets.enemyAdd.hider);
		mHiders.enemyAdd.addToggleActor(button);
		mHiders.enemyAdd.addChild(mWidgets.enemyAdd.hider);
		mTooltip.add(button, Messages.EditorTooltips.TAB_ENEMY_ADD);

		// Enemy settings
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.ENEMY_INFO);
		button = mSettingTabs.addTab(buttonStyle, mWidgets.enemy.table, mWidgets.enemy.hider);
		mHiders.enemyOptions.addToggleActor(button);
		mHiders.enemyOptions.addChild(mWidgets.enemy.hider);
		mTooltip.add(button, Messages.EditorTooltips.TAB_ENEMY);

		// TODO fix tabs
	}

	/**
	 * Init add enemy options
	 */
	private void initEnemyAddOptions() {
		AlignTable table = mWidgets.enemyAdd.table;
		table.row().setFillHeight(true);

		// Hiders
		mHiders.enemyAdd.setButton(mWidgets.tool.enemyAdd);

		Button button;

		// Scroll pane
		mWidgets.enemyAdd.scrollTable.align(Align.left | Align.top);
		ScrollPane scrollPane = new ScrollPane(mWidgets.enemyAdd.scrollTable, mUiFactory.getStyles().scrollPane.windowBackground);
		mWidgets.enemyAdd.scrollPane = scrollPane;

		float scrollPaneWidth = mUiFactory.getStyles().vars.rightPanelWidth - mUiFactory.getStyles().vars.paddingOuter * 2;
		table.add(scrollPane).setFillHeight(true).setWidth(scrollPaneWidth);

		// Add enemy button
		table.row();
		button = mUiFactory.addImageButton(SkinNames.EditorIcons.ENEMY_ADD, table, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.ENEMY_ADD);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mLevelEditor.selectEnemy();
			}
		};
	}

	/**
	 * Initializes Enemy Options tab
	 */
	private void initEnemyOptions() {
		AlignTable table = mWidgets.enemy.table;
		@SuppressWarnings("unchecked")
		ArrayList<Actor> createdActors = Pools.arrayList.obtain();


		// Enemy count
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setEnemyCount((int) (newValue + 0.5f));
			}
		};
		mUiFactory.addPanelSection("Enemy count", table, null);
		mWidgets.enemy.cEnemies = mUiFactory.addSlider(null, Level.Enemy.ENEMIES_MIN, Level.Enemy.ENEMIES_MAX, Level.Enemy.ENEMIES_STEP_SIZE,
				sliderListener, table, null, mDisabledWhenPublished, mInvoker);

		HideSliderValue delayHider = new HideSliderValue(mWidgets.enemy.cEnemies, 2, Float.MAX_VALUE);
		mHiders.enemyOptions.addChild(delayHider);


		// Spawn delay between enemies
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setEnemySpawnDelay(newValue);
			}
		};
		mUiFactory.addPanelSection("Spawn delay", table, delayHider);
		mWidgets.enemy.betweenDelay = mUiFactory.addSlider(null, Level.Enemy.DELAY_BETWEEN_MIN, Level.Enemy.DELAY_BETWEEN_MAX,
				Level.Enemy.DELAY_BETWEEN_STEP_SIZE, sliderListener, table, delayHider, createdActors, mInvoker);
		mTooltip.add(createdActors, Messages.EditorTooltips.ENEMY_SPAWN_DELAY);
		mDisabledWhenPublished.addAll(createdActors);
		createdActors.clear();


		// Activation delay
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setSelectedEnemyActivateTriggerDelay(newValue);
			}
		};
		mUiFactory.addPanelSection("Activation delay", table, mHiders.enemyActivateDelay);
		mWidgets.enemy.activateDelay = mUiFactory.addSlider(null, Level.Enemy.TRIGGER_ACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_ACTIVATE_DELAY_MAX,
				Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, sliderListener, table, mHiders.enemyActivateDelay, createdActors, mInvoker);
		mTooltip.add(createdActors, Messages.EditorTooltips.ENEMY_ACTIVATION_DELAY);
		mDisabledWhenPublished.addAll(createdActors);
		createdActors.clear();


		// Deactivation delay;
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setSelectedEnemyDeactivateTriggerDelay(newValue);
			}
		};
		mUiFactory.addPanelSection("Deactivation delay", table, mHiders.enemyDeactivateDelay);
		mWidgets.enemy.deactivateDelay = mUiFactory.addSlider(null, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MIN,
				Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MAX, Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, sliderListener, table,
				mHiders.enemyDeactivateDelay, createdActors, mInvoker);
		mTooltip.add(createdActors, Messages.EditorTooltips.ENEMY_DEACTIVATION_DELAY);
		mDisabledWhenPublished.addAll(createdActors);
		createdActors.clear();


		Pools.arrayList.free(createdActors);
	}

	/**
	 * Initializes path tool GUI
	 */
	private void initPathOptions() {
		// TODO Use tabs

		mHiders.pathOptions.addToggleActor(mWidgets.path.table);
		mMainTable.row();
		mMainTable.add(mWidgets.path.table);
		mWidgets.path.table.setPreferences(mMainTable);

		AlignTable table = mWidgets.path.table;

		// Path options
		Button button;
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.setMinCheckCount(0);

		// Once
		button = mUiFactory.addImageButton(SkinNames.EditorIcons.PATH_ONCE, table, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.PATH_ONCE);
		mWidgets.path.once = button;
		buttonGroup.add(button);
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.ONCE);
				}
			}
		};

		// Loop
		button = mUiFactory.addImageButton(SkinNames.EditorIcons.PATH_LOOP, table, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.PATH_LOOP);
		mWidgets.path.loop = button;
		buttonGroup.add(button);
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.LOOP);
				}
			}
		};

		// Back and forth
		button = mUiFactory.addImageButton(SkinNames.EditorIcons.PATH_BACK_AND_FORTH, table, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.PATH_BACK_AND_FORTH);
		mWidgets.path.backAndForth = button;
		buttonGroup.add(button);
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.BACK_AND_FORTH);
				}
			}
		};

		mWidgets.path.table.row();
	}

	@Override
	public void setInfoNameError(String errorText) {
		mWidgets.info.nameError.setText(errorText);
		mWidgets.info.nameError.pack();
	}

	@Override
	ITooltip getFileNewTooltip() {
		return Messages.EditorTooltips.FILE_NEW_LEVEL;
	}

	@Override
	ITooltip getFileDuplicateTooltip() {
		return Messages.EditorTooltips.FILE_DUPLICATE_LEVEL;
	}

	@Override
	ITooltip getFilePublishTooltip() {
		return Messages.EditorTooltips.FILE_PUBLISH_LEVEL;
	}

	@Override
	ITooltip getFileInfoTooltip() {
		return Messages.EditorTooltips.FILE_INFO_LEVEL;
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
			Button select = null;
			Button pan = null;
			Button move = null;
			Button delete = null;
			Button drawAppend = null;
			Button drawErase = null;
			Button cornerAdd = null;
			Button cornerRemove = null;
			Button pathAdd = null;
			Button enemyAdd = null;
			Button triggerActivate = null;
			Button triggerDeactivate = null;
		}

		static class EnemyOptionWidgets {
			AlignTable table = new AlignTable();
			HideListener hider = new HideListener(true);
			Slider cEnemies = null;
			Slider betweenDelay = null;
			Slider activateDelay = null;
			Slider deactivateDelay = null;
		}

		static class EnemyAddWidgets {
			ScrollPane scrollPane = null;
			Table scrollTable = new Table();
			AlignTable table = new AlignTable();
			HideListener hider = new HideListener(true);
		}

		static class InfoWidgets {
			Label nameError = null;
			TextField name = null;
			TextField description = null;
			Slider speed = null;
			SelectBox<Themes> theme = null;
			TextField prologue = null;
			TextField epilogue = null;
			Image image = null;
		}

		static class PathOptionWidgets {
			AlignTable table = new AlignTable();
			Button once = null;
			Button loop = null;
			Button backAndForth = null;
		}

	}

}
