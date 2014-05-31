package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
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
import com.spiddekauga.utils.scene.ui.TooltipListener;
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
		for (EnemyActorDef enemyDef : enemyDefs) {
			Button button = new ResourceTextureButton(enemyDef, mStyles.skin.general, SkinNames.General.IMAGE_BUTTON_TOGGLE.toString());

			int enemiesPerColumn = getEnemiesPerColumnInAddTable();

			if (cColumEnemy == enemiesPerColumn) {
				cColumEnemy = 0;
				mWidgets.enemyAdd.scrollTable.row();
			}

			TooltipListener tooltipListener = new TooltipListener(button, enemyDef.getDescription());
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
		ButtonListener buttonListener;
		ButtonGroup buttonGroup = new ButtonGroup();

		// Select
		mToolMenu.row();
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.SELECTION);
				}
			}
		};
		mWidgets.tool.select = mUiFactory.addToolButton(EditorIcons.SELECT, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.SELECT, getResourceTypeName()), mDisabledWhenPublished);


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Pan
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PAN);
				}
			}
		};
		mWidgets.tool.pan = mUiFactory.addToolButton(EditorIcons.PAN, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.PAN, getResourceTypeName()), mDisabledWhenPublished);

		// Move
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.MOVE);
				}
			}
		};
		mWidgets.tool.move = mUiFactory.addToolButton(EditorIcons.MOVE, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.MOVE, getResourceTypeName()), mDisabledWhenPublished);


		// Delete
		mToolMenu.row();
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.DELETE);
				}
			}
		};
		mWidgets.tool.delete = mUiFactory.addToolButton(EditorIcons.DELETE, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.DELETE, getResourceTypeName()), mDisabledWhenPublished);

		// Cancel
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.clearSelection();
				}
			}
		};
		mUiFactory.addToolButton(EditorIcons.CANCEL, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.CANCEL, getResourceTypeName()), mDisabledWhenPublished);


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Terrain draw_append
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_DRAW_APPEND);
				}
			}
		};
		mWidgets.tool.drawAppend = mUiFactory.addToolButton(EditorIcons.TERRAIN_DRAW_APPEND, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.DRAW_APPEND, "terrain"), mDisabledWhenPublished);


		// Terrain draw_erase
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.TERRAIN_DRAW_ERASE);
				}
			}
		};
		mWidgets.tool.drawErase = mUiFactory.addToolButton(EditorIcons.DRAW_ERASE, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.DRAW_ERASE, "terrain"), mDisabledWhenPublished);

		// add_move_corner
		mToolMenu.row();
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ADD_MOVE_CORNER);
				}
			}
		};
		mWidgets.tool.cornerAdd = mUiFactory.addToolButton(EditorIcons.ADD_MOVE_CORNER, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.ADJUST_ADD_MOVE_CORNER, "terrain"), mDisabledWhenPublished);

		// remove_corner
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.REMOVE_CORNER);
				}
			}
		};
		mWidgets.tool.cornerRemove = mUiFactory.addToolButton(EditorIcons.REMOVE_CORNER, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.ADJUST_REMOVE_CORNER, "terrain"), mDisabledWhenPublished);


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Enemy add
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_ADD);
				}
			}
		};
		mWidgets.tool.enemyAdd = mUiFactory.addToolButton(EditorIcons.ENEMY_ADD, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.ENEMY_ADD, getResourceTypeName()), mDisabledWhenPublished);

		// Path add
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PATH_ADD);
				}
			}
		};
		mWidgets.tool.pathAdd = mUiFactory.addToolButton(EditorIcons.PATH_ADD, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.PATH_ADD, getResourceTypeName()), mDisabledWhenPublished);


		// Enemy - set activate trigger
		mToolMenu.row();
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_SET_ACTIVATE_TRIGGER);
				}
			}
		};
		mWidgets.tool.triggerActivate = mUiFactory.addToolButton(EditorIcons.ENEMY_SET_ACTIVATE_TRIGGER, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.SET_ACTIVATE_TRIGGER, getResourceTypeName()), mDisabledWhenPublished);

		// Enemy - set deactivate trigger
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.ENEMY_SET_DEACTIVATE_TRIGGER);
				}
			}
		};
		mWidgets.tool.triggerDeactivate = mUiFactory.addToolButton(EditorIcons.ENEMY_SET_DEACTIVATE_TRIGGER, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.SET_DEACTIVATE_DELAY, getResourceTypeName()), mDisabledWhenPublished);


		/** @todo readd pickup */
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
		mInfoTable.add(left).setPadRight(mStyles.vars.paddingInner);
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
				Editor.Level.LEVEL_SPEED_STEP_SIZE, sliderListener, left, null, null, mDisabledWhenPublished, mInvoker);

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
		float width = mStyles.vars.textFieldWidth;
		float height = width / Config.Level.SAVE_TEXTURE_RATIO;

		mWidgets.info.image.setDrawable(mLevelEditor.getImage());
		mWidgets.info.image.setSize(width, height);
		mWidgets.info.image.setVisible(true);
		mWidgets.info.image.invalidate();
	}

	/**
	 * Calculates the amount of enemies per column in the add enemy scroll pane. Uses
	 * Config.Editor.Level.Enemy.ADD_ENEMY_TABLE_MAX_WIDTH for determining the maximum
	 * amount of width on the scroll pane.
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
		button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ENEMY_SELECT.toString());
		mMainTable.add(button);
		mHiders.enemyAdd.addToggleActor(button);
		tooltipListener = new TooltipListener(button, Messages.Tooltip.Level.Enemy.ADD);
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
		new TooltipListener(label, Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		Slider slider = new Slider(Level.Enemy.ENEMIES_MIN, Level.Enemy.ENEMIES_MAX, Level.Enemy.ENEMIES_STEP_SIZE, false, mStyles.slider.standard);
		mDisabledWhenPublished.add(slider);
		mWidgets.enemy.cEnemies = slider;
		mWidgets.enemy.table.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
		mDisabledWhenPublished.add(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
		new TooltipListener(textField, Messages.Tooltip.Level.Enemy.ENEMY_COUNT);
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
		new TooltipListener(label, Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		delayHider.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.DELAY_BETWEEN_MIN, Level.Enemy.DELAY_BETWEEN_MAX, Level.Enemy.DELAY_BETWEEN_STEP_SIZE, false,
				mStyles.slider.standard);
		mDisabledWhenPublished.add(slider);
		delayHider.addToggleActor(slider);
		mWidgets.enemy.betweenDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		mDisabledWhenPublished.add(textField);
		delayHider.addToggleActor(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		new TooltipListener(textField, Messages.Tooltip.Level.Enemy.ENEMY_SPAWN_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setEnemySpawnDelay(newValue);
			}
		};


		// Activation delay
		mWidgets.enemy.table.row();
		label = new Label("Activate delay", mStyles.label.standard);
		new TooltipListener(label, Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		mHiders.enemyActivateDelay.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.TRIGGER_ACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_ACTIVATE_DELAY_MAX,
				Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, false, mStyles.slider.standard);
		mDisabledWhenPublished.add(slider);
		mHiders.enemyActivateDelay.addToggleActor(slider);
		mWidgets.enemy.activateDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		mDisabledWhenPublished.add(textField);
		mHiders.enemyActivateDelay.addToggleActor(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		new TooltipListener(textField, Messages.Tooltip.Level.Enemy.ACTIVATE_DELAY);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setSelectedEnemyActivateTriggerDelay(newValue);
			}
		};


		// Deactivation delay
		mWidgets.enemy.table.row();
		label = new Label("Deactivate delay", mStyles.label.standard);
		new TooltipListener(label, Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		mHiders.enemyDeactivateDelay.addToggleActor(label);
		mWidgets.enemy.table.add(label);

		mWidgets.enemy.table.row();
		slider = new Slider(Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MAX,
				Level.Enemy.TRIGGER_DEACTIVATE_DELAY_STEP_SIZE, false, mStyles.slider.standard);
		mDisabledWhenPublished.add(slider);
		mHiders.enemyDeactivateDelay.addToggleActor(slider);
		mWidgets.enemy.deactivateDelay = slider;
		mWidgets.enemy.table.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		mDisabledWhenPublished.add(textField);
		mHiders.enemyDeactivateDelay.addToggleActor(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.enemy.table.add(textField);
		new TooltipListener(slider, Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
		new TooltipListener(textField, Messages.Tooltip.Level.Enemy.DEACTIVATE_DELAY);
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
		button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.PATH_ONCE.toString());
		mDisabledWhenPublished.add(button);
		mWidgets.path.once = button;
		mWidgets.path.table.add(button);
		buttonGroup.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, Messages.Tooltip.Level.Path.ONCE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.ONCE);
				}
			}
		};


		button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.PATH_LOOP.toString());
		mDisabledWhenPublished.add(button);
		mWidgets.path.loop = button;
		mWidgets.path.table.add(button);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, Messages.Tooltip.Level.Path.LOOP);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.LOOP);
				}
			}
		};


		button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.PATH_BACK_AND_FORTH.toString());
		mDisabledWhenPublished.add(button);
		mWidgets.path.backAndForth = button;
		mWidgets.path.table.add(button);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, Messages.Tooltip.Level.Path.BACK_AND_FORTH);
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
		// new TooltipListener(label, "Pickup name",
		// Messages.Tooltip.Level.Pickup.SELECT_NAME);
		// mWidgets.pickup.name = label;
		// mPickupTable.add(label);
		//
		// if (Config.Gui.usesTextButtons()) {
		// button = new TextButton("Select type", mStyles.textButton.press);
		// } else {
		// button = new ImageButton(mStyles.skin.editor,
		// SkinNames.EditorIcons.PICKUP_SELECT.toString());
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

	@Override
	public void setInfoNameError(String errorText) {
		mWidgets.info.nameError.setText(errorText);
		mWidgets.info.nameError.setWidth(mWidgets.info.nameError.getPrefWidth());
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
