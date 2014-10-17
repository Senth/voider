package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.commands.CGuiSlider;
import com.spiddekauga.utils.commands.CInvokerUndoToDelimiter;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.HideSliderValue;
import com.spiddekauga.utils.scene.ui.ImageScrollButton;
import com.spiddekauga.utils.scene.ui.ImageScrollButton.ScrollWhen;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.ResourceTextureButton;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget.CustomTooltip;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.utils.scene.ui.UiFactory.ButtonStyles;
import com.spiddekauga.utils.scene.ui.UiFactory.Positions;
import com.spiddekauga.utils.scene.ui.UiFactory.ThemeSelectorData;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Level;
import com.spiddekauga.voider.editor.LevelEditor.Tools;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.EditorIcons;
import com.spiddekauga.voider.scene.SceneSwitcher;
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


		mInfoTable.setPreferences(mMainTable);

		initToolMenu();
		initInfo();
		initPathOptions();
		initEnemyOptions();
		initEnemyAddOptions();
		initSettingsMenu();
	}

	@Override
	public void dispose() {
		mWidgets.enemy.table.dispose();
		mWidgets.enemyAdd.table.dispose();
		mWidgets.enemyAdd.scrollTable.dispose();
		mWidgets.path.table.dispose();
		mInfoTable.dispose();

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
		resetTheme();
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
		// TODO set theme
	}

	/**
	 * Reset path options
	 */
	void resetPathOptions() {
		if (mLevelEditor.isPathSelected()) {
			mWidgets.path.hiderTab.show();

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
			mWidgets.path.hiderTab.hide();
		}
	}

	/**
	 * Reset enemy add table
	 */
	void resetEnemyAddTable() {
		// Skip if invisible or not initialized
		if (!mWidgets.enemyAdd.hiderTable.isVisible() && mWidgets.enemyAdd.scrollTable == null) {
			return;
		}

		ArrayList<EnemyActorDef> enemyDefs = mLevelEditor.getAddEnemies();


		mWidgets.enemyAdd.scrollTable.dispose(true);


		ButtonGroup buttonGroup = new ButtonGroup();
		int enemiesPerColumn = Config.Editor.Level.Enemy.LIST_COLUMNS;
		int cColumEnemy = enemiesPerColumn;
		float maxScrollPaneHeight = getEnemyScrollListMaxHeight();

		Cell enemyButtonCell = null;

		for (EnemyActorDef enemyDef : enemyDefs) {
			Button button = new ResourceTextureButton(enemyDef, (ImageButtonStyle) SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_TOGGLE));

			// Create tooltip
			CustomTooltip tooltip = new CustomTooltip(getEnemyTooltip(enemyDef), null, Messages.EditorTooltips.TOOL_ENEMY_ADD, 3);
			tooltip.setHideWhenHidden(false);
			mTooltip.add(button, tooltip);

			if (cColumEnemy == enemiesPerColumn) {
				cColumEnemy = 0;
				mWidgets.enemyAdd.scrollTable.row().setFillWidth(true).setEqualCellSize(true);
			}

			new ButtonListener(button) {
				@Override
				protected void onChecked(Button button, boolean checked) {
					if (checked) {
						mLevelEditor.createNewEnemy((EnemyActorDef) ((ResourceTextureButton) button).getResource());
					}
				}
			};
			buttonGroup.add(button);

			enemyButtonCell = mWidgets.enemyAdd.scrollTable.add(button).setKeepAspectRatio(true).setFillWidth(true);
			cColumEnemy++;
		}

		// Fill rest of row with empty cell
		if (cColumEnemy > 0 && cColumEnemy < enemiesPerColumn) {
			mWidgets.enemyAdd.scrollTable.add(enemiesPerColumn - cColumEnemy);
		}

		// Fix height of scroll pane
		mWidgets.enemyAdd.table.layout();
		float innerHeight = mWidgets.enemyAdd.scrollTable.getHeight();
		// OK height
		if (innerHeight < maxScrollPaneHeight) {
			mWidgets.enemyAdd.scrollPane.setHeight(innerHeight);
		} else {
			// Calculate maximum number of rows
			if (enemyButtonCell != null) {
				int rows = (int) (maxScrollPaneHeight / enemyButtonCell.getHeight());
				mWidgets.enemyAdd.scrollPane.setHeight(rows * enemyButtonCell.getHeight());
			}
		}
	}

	/**
	 * @return calculate and get max scroll table height for the enemy list.
	 * @note scrollPane needs to be set to height 0 and wrapper table must call layout()
	 *       as it calculates the height of the rest of the widgets.
	 */
	private float getEnemyScrollListMaxHeight() {
		mWidgets.enemyAdd.scrollPane.setHeight(0);
		mWidgets.enemyAdd.table.invalidate();
		mSettingTabs.invalidate();
		mSettingTabs.layout();


		// Calculate maximum size
		float availableHeight = mSettingTabs.getAvailableHeight();

		// Decrease with other widgets in the tab
		availableHeight -= mWidgets.enemyAdd.table.getHeight();

		return availableHeight;
	}

	/**
	 * Get tooltip text for an enemy
	 * @param enemyDef definition of the enemy
	 * @return get tooltip of the specified enemy
	 */
	private String getEnemyTooltip(EnemyActorDef enemyDef) {
		String text = "Click on level to add enemy '" + enemyDef.getName() + "'";

		return text;
	}

	/**
	 * Resets enemy option values
	 */
	void resetEnemyOptions() {
		// Show/Hide options
		if (mLevelEditor.isEnemySelected()) {
			mWidgets.enemy.hiderTab.show();

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
				mWidgets.enemy.hiderActivateDelay.show();

				float activateDelay = mLevelEditor.getSelectedEnemyActivateTriggerDelay();
				if (activateDelay >= 0 && activateDelay != mWidgets.enemy.activateDelay.getValue()) {
					mInvoker.execute(new CGuiSlider(mWidgets.enemy.activateDelay, activateDelay, mWidgets.enemy.activateDelay.getValue()));
				}
			} else {
				mWidgets.enemy.hiderActivateDelay.hide();
			}


			// Has deactivate trigger -> Show trigger delay
			if (mLevelEditor.hasSelectedEnemyDeactivateTrigger()) {
				mWidgets.enemy.hiderDeactivateDelay.show();

				float deactivateDelay = mLevelEditor.getSelectedEnemyDeactivateTriggerDelay();
				if (deactivateDelay >= 0 && deactivateDelay != mWidgets.enemy.deactivateDelay.getValue()) {
					mInvoker.execute(new CGuiSlider(mWidgets.enemy.deactivateDelay, deactivateDelay, mWidgets.enemy.deactivateDelay.getValue()));
				}
			} else {
				mWidgets.enemy.hiderDeactivateDelay.hide();
			}

		} else {
			mWidgets.enemy.hiderTab.hide();
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
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.SELECTION);
				}
			}
		};

		// Cancel
		Button button = mUiFactory.addToolButton(EditorIcons.CANCEL, null, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.TOOL_CLEAR_SELECTION);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mLevelEditor.clearSelection();
			}
		};


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Zoom in
		mToolMenu.row();
		mWidgets.tool.zoomIn = mUiFactory.addToolButton(EditorIcons.ZOOM_IN, buttonGroup, mToolMenu, null);
		mTooltip.add(mWidgets.tool.zoomIn, Messages.EditorTooltips.TOOL_ZOOM_IN_LEVEL);
		new ButtonListener(mWidgets.tool.zoomIn) {
			@Override
			protected void onPressed(Button button) {
				mLevelEditor.switchTool(Tools.ZOOM_IN);
			}
		};

		// Zoom out
		mWidgets.tool.zoomOut = mUiFactory.addToolButton(EditorIcons.ZOOM_OUT, buttonGroup, mToolMenu, null);
		mTooltip.add(mWidgets.tool.zoomOut, Messages.EditorTooltips.TOOL_ZOOM_OUT_LEVEL);
		new ButtonListener(mWidgets.tool.zoomOut) {
			@Override
			protected void onPressed(Button button) {
				mLevelEditor.switchTool(Tools.ZOOM_OUT);
			}
		};

		// Pan
		mToolMenu.row();
		mWidgets.tool.pan = mUiFactory.addToolButton(EditorIcons.PAN, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.pan, Messages.EditorTooltips.TOOL_PAN_LEVEL);
		new ButtonListener(mWidgets.tool.pan) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PAN);
				}
			}
		};

		// Reset zoom
		button = mUiFactory.addToolButton(EditorIcons.ZOOM_RESET, null, mToolMenu, null);
		mTooltip.add(button, Messages.EditorTooltips.TOOL_ZOOM_RESET_LEVEL);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mLevelEditor.resetZoom();
			}
		};


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Move
		mToolMenu.row();
		mWidgets.tool.move = mUiFactory.addToolButton(EditorIcons.MOVE, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.move, Messages.EditorTooltips.TOOL_MOVE_LEVEL);
		new ButtonListener(mWidgets.tool.move) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.MOVE);
				}
			}
		};

		// Delete
		mWidgets.tool.delete = mUiFactory.addToolButton(EditorIcons.DELETE, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.delete, Messages.EditorTooltips.TOOL_DELETE_LEVEL);
		new ButtonListener(mWidgets.tool.delete) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.DELETE);
				}
			}
		};


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Terrain draw_append
		mWidgets.tool.drawAppend = mUiFactory.addToolButton(EditorIcons.TERRAIN_DRAW_APPEND, buttonGroup, mToolMenu, mDisabledWhenPublished);
		mTooltip.add(mWidgets.tool.drawAppend, Messages.EditorTooltips.TOOL_DRAW_APPEND_TERRAIN);
		new ButtonListener(mWidgets.tool.drawAppend) {
			@Override
			protected void onChecked(Button button, boolean checked) {
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
			protected void onChecked(Button button, boolean checked) {
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
			protected void onChecked(Button button, boolean checked) {
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
			protected void onChecked(Button button, boolean checked) {
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
			protected void onChecked(Button button, boolean checked) {
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
			protected void onChecked(Button button, boolean checked) {
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
			protected void onChecked(Button button, boolean checked) {
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
			protected void onChecked(Button button, boolean checked) {
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
		mInfoTable.setPad(0, paddingInner, 0, paddingInner);

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
		mUiFactory.addSection("Theme", left, null);
		left.row().setFillWidth(true);
		mUiFactory.addLabel("Select Theme", false, left, SkinNames.General.LABEL_TEXT_FIELD_DEFAULT);
		left.add().setFillWidth(true);
		float buttonWidth = mUiFactory.getStyles().vars.textButtonWidth;
		float buttonHeight = mUiFactory.getStyles().vars.textButtonHeight;
		mWidgets.info.theme = mUiFactory.addImageScrollButton(ScrollWhen.NEVER, buttonWidth, buttonHeight, ButtonStyles.PRESS, left,
				mDisabledWhenPublished);
		new ButtonListener(mWidgets.info.theme) {
			@Override
			protected void onPressed(Button button) {
				showThemeSelectWindow();
			}
		};

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

	/**
	 * Update theme image button
	 */
	void resetTheme() {
		ImageScrollButton button = mWidgets.info.theme;
		Themes theme = mLevelEditor.getTheme();

		button.clearLayers();
		Texture bottomLayer = ResourceCacheFacade.get(theme.getBottomLayer());
		Texture topLayer = ResourceCacheFacade.get(theme.getTopLayer());
		button.addLayer(bottomLayer);
		button.addLayer(topLayer);
	}

	/**
	 * Show theme selection message box
	 */
	private void showThemeSelectWindow() {
		// Show message box
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			final String THEME_DELIMETER = "theme-select";

			MsgBoxExecuter msgBox = getFreeMsgBox(true);
			mInvoker.pushDelimiter(THEME_DELIMETER);
			msgBox.setTitle("Select Theme");

			// Listener that set the theme
			ButtonListener listener = new ButtonListener() {
				@Override
				protected void onChecked(Button button, boolean checked) {
					Object userObject = button.getUserObject();

					if (userObject instanceof ThemeSelectorData) {
						ThemeSelectorData themeData = (ThemeSelectorData) userObject;

						if (checked) {
							mLevelEditor.setTheme(themeData.theme);
							themeData.label.setStyle(mUiFactory.getStyles().label.highlight);
						} else {
							themeData.label.setStyle(mUiFactory.getStyles().label.standard);
						}
					}
				}
			};

			// Calculate width/height for scroll pane
			// Try to fit 3 themes on one screen
			float ratio = SkinNames.getResource(SkinNames.EditorVars.THEME_DISPLAY_RATIO);
			float width = Gdx.graphics.getWidth() - 2 * mUiFactory.getStyles().vars.paddingSeparator;

			float buttonWidth = (width - (Themes.values().length - 1) * mUiFactory.getStyles().vars.paddingInner) / Themes.values().length;
			float buttonHeight = buttonWidth / ratio;
			float height = buttonHeight + mUiFactory.getStyles().vars.rowHeight * 2;

			// Create scroll pane
			AlignTable content = new AlignTable();
			ScrollPane scrollPane = mUiFactory.createThemeList(width, height, true, listener, mLevelEditor.getTheme());
			content.add(scrollPane).setSize(width, height);
			content.setSize(width, height);
			msgBox.content(content);

			// Cancel button and undo theme settings
			msgBox.addCancelButtonAndKeys(new CInvokerUndoToDelimiter(mInvoker, THEME_DELIMETER, false));
			msgBox.button("Select");
			msgBox.key(Input.Keys.ENTER, null);

			showMsgBox(msgBox);
		}

		// Mobile device, show scene instead
		else if (Gdx.app.getType() == ApplicationType.Android) {
			MsgBoxExecuter msgBox = getFreeMsgBox(true);
			msgBox.setTitle("Select Theme");

			// Listener to open full screen theme scene
			ButtonListener listener = new ButtonListener() {
				@Override
				protected void onPressed(Button button) {
					Object userObject = button.getUserObject();

					if (userObject instanceof ThemeSelectorData) {
						ThemeSelectorData data = (ThemeSelectorData) userObject;
						SceneSwitcher.switchTo(new ThemeSelectScene(data.theme));
					}
				}
			};

			// Calculate width/height for scroll pane
			// Try to fit all 4 themes on one screen
			float ratio = SkinNames.getResource(SkinNames.EditorVars.THEME_DISPLAY_RATIO);
			float width = Gdx.graphics.getWidth() - 2 * mUiFactory.getStyles().vars.paddingSeparator;

			float buttonWidth = (width - (Themes.values().length - 1) * mUiFactory.getStyles().vars.paddingInner) / Themes.values().length;
			float buttonHeight = buttonWidth / ratio;
			float height = buttonHeight + mUiFactory.getStyles().vars.rowHeight * 2;

			// Create scroll pane
			AlignTable content = new AlignTable();
			ScrollPane scrollPane = mUiFactory.createThemeList(width, height, false, listener, mLevelEditor.getTheme());
			content.add(scrollPane).setSize(width, height);
			content.setSize(width, height);
			msgBox.content(content);

			// Back buttons
			msgBox.addCancelButtonAndKeys("Back");

			showMsgBox(msgBox);
		}
	}

	@Override
	protected void initSettingsMenu() {
		super.initSettingsMenu();

		// Add enemy list
		ImageButtonStyle buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.ENEMY_ADD_TAB);
		Button button = mSettingTabs.addTab(buttonStyle, mWidgets.enemyAdd.table, mWidgets.enemyAdd.hiderTable);
		mWidgets.enemyAdd.hiderTab.addToggleActor(button);
		mTooltip.add(button, Messages.EditorTooltips.TAB_ENEMY_ADD);

		// Enemy settings
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.ENEMY_INFO);
		button = mSettingTabs.addTab(buttonStyle, mWidgets.enemy.table, mWidgets.enemy.hiderTable);
		mWidgets.enemy.hiderTab.addToggleActor(button);
		mTooltip.add(button, Messages.EditorTooltips.TAB_ENEMY);

		// Path settings
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.PATH_TAB);
		button = mSettingTabs.addTab(buttonStyle, mWidgets.path.table, mWidgets.path.hiderTable);
		mWidgets.path.hiderTab.addToggleActor(button);
		mTooltip.add(button, Messages.EditorTooltips.TAB_PATH);
	}

	/**
	 * Init add enemy options
	 */
	private void initEnemyAddOptions() {
		mWidgets.enemyAdd.scrollTable.setHasPreferredWidth(false).setHasPreferredHeight(false);
		mWidgets.enemyAdd.scrollTable.setName("scroll-table");
		mWidgets.enemyAdd.scrollTable.setBackgroundImage(new Background(mUiFactory.getStyles().color.widgetInnerBackground));
		AlignTable table = mWidgets.enemyAdd.table;
		table.row().setFillHeight(true);

		// Hiders
		mWidgets.enemyAdd.hiderTab.setButton(mWidgets.tool.enemyAdd);

		Button button;

		// Scroll pane
		mWidgets.enemyAdd.scrollTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		ScrollPane scrollPane = new ScrollPane(mWidgets.enemyAdd.scrollTable, mUiFactory.getStyles().scrollPane.noBackground);
		mWidgets.enemyAdd.scrollPane = scrollPane;
		table.add(scrollPane).setWidth(getInnerRightPanelWidth()).setFixedHeight(true);

		// Add enemy button
		table.row().setAlign(Horizontal.RIGHT).setPadTop(mUiFactory.getStyles().vars.paddingInner);
		button = mUiFactory.addImageButton(SkinNames.EditorIcons.ENEMY_ADD_TO_LIST, table, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.ENEMY_ADD_TO_LIST);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mLevelEditor.addEnemyToList();
			}
		};
	}

	/**
	 * @return available width inside the right panel
	 */
	private float getInnerRightPanelWidth() {
		return mUiFactory.getStyles().vars.rightPanelWidth;// -
		// mUiFactory.getStyles().vars.paddingOuter
		// * 2;
	}

	/**
	 * Initializes Enemy Options tab
	 */
	private void initEnemyOptions() {
		AlignTable table = mWidgets.enemy.table;
		table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		@SuppressWarnings("unchecked")
		ArrayList<Actor> createdActors = Pools.arrayList.obtain();


		// Enemy count
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setEnemyCount((int) (newValue + 0.5f));
			}
		};
		mUiFactory.addPanelSection("Enemy", table, null);
		mWidgets.enemy.cEnemies = mUiFactory.addSlider("Copies", Level.Enemy.ENEMIES_MIN, Level.Enemy.ENEMIES_MAX, Level.Enemy.ENEMIES_STEP_SIZE,
				sliderListener, table, null, mDisabledWhenPublished, mInvoker);

		HideSliderValue delayHider = new HideSliderValue(mWidgets.enemy.cEnemies, 2, Float.MAX_VALUE);
		mWidgets.enemy.hiderTable.addChild(delayHider);


		// Spawn delay between enemies
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mLevelEditor.setEnemySpawnDelay(newValue);
			}
		};
		mUiFactory.addPanelSection("Spawn", table, delayHider);
		mWidgets.enemy.betweenDelay = mUiFactory.addSlider("Delay", Level.Enemy.DELAY_BETWEEN_MIN, Level.Enemy.DELAY_BETWEEN_MAX,
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
		mUiFactory.addPanelSection("Activation", table, mWidgets.enemy.hiderActivateDelay);
		mWidgets.enemy.activateDelay = mUiFactory.addSlider("Delay", Level.Enemy.TRIGGER_ACTIVATE_DELAY_MIN, Level.Enemy.TRIGGER_ACTIVATE_DELAY_MAX,
				Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, sliderListener, table, mWidgets.enemy.hiderActivateDelay, createdActors, mInvoker);
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
		mUiFactory.addPanelSection("Deactivation", table, mWidgets.enemy.hiderDeactivateDelay);
		mWidgets.enemy.deactivateDelay = mUiFactory.addSlider("Delay", Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MIN,
				Level.Enemy.TRIGGER_DEACTIVATE_DELAY_MAX, Level.Enemy.TRIGGER_ACTIVATE_DELAY_STEP_SIZE, sliderListener, table,
				mWidgets.enemy.hiderDeactivateDelay, createdActors, mInvoker);
		mTooltip.add(createdActors, Messages.EditorTooltips.ENEMY_DEACTIVATION_DELAY);
		mDisabledWhenPublished.addAll(createdActors);
		createdActors.clear();


		Pools.arrayList.free(createdActors);
	}

	/**
	 * Initializes path tool GUI
	 */
	private void initPathOptions() {
		AlignTable table = mWidgets.path.table;
		table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);

		mUiFactory.addPanelSection("Enemy path movement", table, null);

		// Buttons
		Button button;
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.setMinCheckCount(0);

		GuiCheckCommandCreator checkCommandCreator = new GuiCheckCommandCreator(mInvoker);

		// Once
		button = mUiFactory.addImageButtonLabel(SkinNames.EditorIcons.PATH_ONCE, "Once", Positions.RIGHT, null, table, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.PATH_ONCE);
		mWidgets.path.once = button;
		buttonGroup.add(button);
		button.addListener(checkCommandCreator);
		new ButtonListener(button) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.ONCE);
				}
			}
		};

		// Loop
		button = mUiFactory.addImageButtonLabel(SkinNames.EditorIcons.PATH_LOOP, "Loop", Positions.RIGHT, null, table, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.PATH_LOOP);
		mWidgets.path.loop = button;
		buttonGroup.add(button);
		button.addListener(checkCommandCreator);
		new ButtonListener(button) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.LOOP);
				}
			}
		};

		// Back and forth
		button = mUiFactory.addImageButtonLabel(SkinNames.EditorIcons.PATH_BACK_AND_FORTH, "Back and Forth", Positions.RIGHT, null, table, null,
				mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.PATH_BACK_AND_FORTH);
		mWidgets.path.backAndForth = button;
		buttonGroup.add(button);
		button.addListener(checkCommandCreator);
		new ButtonListener(button) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mLevelEditor.setPathType(PathTypes.BACK_AND_FORTH);
				}
			}
		};
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

	/** Level editor the GUI will act on */
	private LevelEditor mLevelEditor = null;
	/** Invoker for level editor */
	private Invoker mInvoker = null;
	/** Inner widgets */
	private InnerWidgets mWidgets = new InnerWidgets();


	/**
	 * Container for inner widgets
	 */
	private class InnerWidgets implements Disposable {
		EnemyOptionWidgets enemy = new EnemyOptionWidgets();
		PathOptionWidgets path = new PathOptionWidgets();
		InfoWidgets info = new InfoWidgets();
		EnemyAddWidgets enemyAdd = new EnemyAddWidgets();
		ToolWidgets tool = new ToolWidgets();

		@Override
		public void dispose() {
			enemy.dispose();
			enemyAdd.dispose();
			path.dispose();
		}

		class ToolWidgets {
			Button select = null;
			Button pan = null;
			Button zoomIn = null;
			Button zoomOut = null;
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

		class EnemyOptionWidgets implements Disposable {
			AlignTable table = new AlignTable();
			Slider cEnemies = null;
			Slider betweenDelay = null;
			Slider activateDelay = null;
			Slider deactivateDelay = null;

			// Hiders
			HideListener hiderTable = new HideListener(true);
			HideManual hiderTab = new HideManual();
			HideManual hiderActivateDelay = new HideManual();
			HideManual hiderDeactivateDelay = new HideManual();

			/**
			 * Set hider children
			 */
			EnemyOptionWidgets() {
				hiderTab.addChild(hiderTable);
				hiderTable.addChild(hiderActivateDelay);
				hiderTable.addChild(hiderDeactivateDelay);
			}

			@Override
			public void dispose() {
				table.dispose();
				hiderActivateDelay.dispose();
				hiderTab.dispose();
				hiderActivateDelay.dispose();
				hiderDeactivateDelay.dispose();
			}
		}

		class EnemyAddWidgets implements Disposable {
			ScrollPane scrollPane = null;
			AlignTable scrollTable = new AlignTable();
			AlignTable table = new AlignTable();

			// Hiders
			HideListener hiderTable = new HideListener(true) {
				@Override
				protected void onShow() {
					resetEnemyAddTable();
				}
			};
			HideListener hiderTab = new HideListener(true);

			/**
			 * Set hider children
			 */
			EnemyAddWidgets() {
				hiderTab.addChild(hiderTable);
			}

			@Override
			public void dispose() {
				table.dispose();
				hiderTab.dispose();
				hiderTable.dispose();
			}
		}

		class InfoWidgets {
			Label nameError = null;
			TextField name = null;
			TextField description = null;
			Slider speed = null;
			TextField prologue = null;
			TextField epilogue = null;
			Image image = null;
			ImageScrollButton theme = null;
		}

		class PathOptionWidgets implements Disposable {
			AlignTable table = new AlignTable();
			Button once = null;
			Button loop = null;
			Button backAndForth = null;

			// Hiders
			HideListener hiderTable = new HideListener(true);
			HideManual hiderTab = new HideManual();

			/**
			 * Set hider children
			 */
			PathOptionWidgets() {
				hiderTab.addChild(hiderTable);
			}

			@Override
			public void dispose() {
				table.dispose();
				hiderTab.dispose();
				hiderTable.dispose();
			}
		}
	}

}
