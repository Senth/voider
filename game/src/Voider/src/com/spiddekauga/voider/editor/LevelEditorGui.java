package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
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
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.CommandSequence;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.CheckedListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.HideSliderValue;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Level;
import com.spiddekauga.voider.editor.LevelEditor.Tools;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorSave;
import com.spiddekauga.voider.editor.commands.CGuiSlider;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.AddActorTool;
import com.spiddekauga.voider.scene.AddActorTool.States;
import com.spiddekauga.voider.scene.DrawActorTool;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.PathTool;

/**
 * GUI for the level editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class LevelEditorGui extends Gui {
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
		super.initGui();

		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);
		mMenuTable.setPreferences(mMainTable);
		mPickupTable.setPreferences(mMainTable);
		mStaticTerrainTable.setPreferences(mMainTable);
		mEnemyTable.setPreferences(mMainTable);

		initPickup();
		initStaticTerrain();
		initMenu();
		initEnemy();

		mMainTable.setTransform(true);
		mMainTable.invalidate();

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

			// TODO add trigger

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
		}

		// Enemy options
		if (mLevelEditor.isEnemySelected()) {
			mEnemyOptionsHider.show();
		} else {
			mEnemyOptionsHider.hide();
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

		// Terrain
		switch (mLevelEditor.getStaticTerrainState()) {
		case ADD_CORNER:
			mWidgets.terrain.add.setChecked(true);
			break;

		case MOVE:
			mWidgets.terrain.move.setChecked(true);
			break;

		case REMOVE:
			mWidgets.terrain.remove.setChecked(true);
			break;

		case SET_CENTER:
			// Does nothing
			break;
		}
	}

	/**
	 * Shows the enemy options
	 */
	void showEnemyOptions() {
		mEnemyOptionsHider.show();
	}

	/**
	 * Hides the enemy options
	 */
	void hideEnemyOptions() {
		mEnemyOptionsHider.hide();
	}

	/**
	 * Set slider values for enemy count and delay
	 * @param cEnemies number of enemies
	 * @param delay the delay between enemies, if none is available set it to negative
	 */
	void setEnemyOptions(int cEnemies, float delay) {
		mWidgets.enemy.cEnemies.setValue(cEnemies);
		if (delay >= 0) {
			mWidgets.enemy.delay.setValue(delay);
		}
	}

	/**
	 * Initializes the main menu
	 */
	private void initMenu() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		final TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);

		ButtonGroup toggleGroup = new ButtonGroup();

		mMenuTable.row();
		Button button = new TextButton("New", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mLevelEditor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Discard current", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						Command save = new CEditorSave(mLevelEditor);
						Command newCommand = new CEditorNew(mLevelEditor);
						Command saveAndNew = new CommandSequence(save, newCommand);

						mMsgBox.clear();
						mMsgBox.setTitle("New Enemy");
						mMsgBox.content("Your current level is unsaved.\n" +
								"Do you want to save it before creating a new level?");
						mMsgBox.button(yes, saveAndNew);
						mMsgBox.button(no, newCommand);
						mMsgBox.button(cancel);
						mMsgBox.key(Keys.BACK, null);
						mMsgBox.key(Keys.ESCAPE, null);
						mMsgBox.show(getStage());
					} else {
						mLevelEditor.newDef();
					}
				}
				return true;
			}
		});
		mMenuTable.add(button);

		mMenuTable.row();
		button = new TextButton("Save", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mLevelEditor.saveDef();
				}
				return true;
			}
		});
		mMenuTable.add(button);

		mMenuTable.row();
		button = new TextButton("Load", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mLevelEditor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Load anyway", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						Command save = new CEditorSave(mLevelEditor);
						Command load = new CEditorLoad(mLevelEditor);
						Command saveAndLoad = new CommandSequence(save, load);

						mMsgBox.clear();
						mMsgBox.setTitle("New Enemy");
						mMsgBox.content("Your current level is unsaved.\n" +
								"Do you want to save it before loading another level?");
						mMsgBox.button(yes, saveAndLoad);
						mMsgBox.button(no, load);
						mMsgBox.button(cancel);
						mMsgBox.key(Keys.BACK, null);
						mMsgBox.key(Keys.ESCAPE, null);
						mMsgBox.show(getStage());
					} else {
						mLevelEditor.loadDef();
					}
				}
				return true;
			}
		});
		mMenuTable.add(button);

		mMenuTable.row();
		button = new TextButton("Duplicate", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mLevelEditor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Duplicate anyway", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						Command save = new CEditorSave(mLevelEditor);
						Command newCommand = new CEditorNew(mLevelEditor);
						Command saveAndNew = new CommandSequence(save, newCommand);

						mMsgBox.clear();
						mMsgBox.setTitle("New Enemy");
						mMsgBox.content("Your current level is unsaved.\n" +
								"Do you want to save it before duplicating it?");
						mMsgBox.button(yes, saveAndNew);
						mMsgBox.button(no, newCommand);
						mMsgBox.button(cancel);
						mMsgBox.key(Keys.BACK, null);
						mMsgBox.key(Keys.ESCAPE, null);
						mMsgBox.show(getStage());
					} else {
						mLevelEditor.newDef();
					}
				}
				return true;
			}
		});
		mMenuTable.add(button);

		mMenuTable.row();
		button = new TextButton("Run", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mLevelEditor.runFromHere();
				}
				return true;
			}
		});
		mMenuTable.add(button);

		mMenuTable.row();
		button = new TextButton("Static Terrain", textToogleStyle);
		mWidgets.menu.terrain = button;
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.STATIC_TERRAIN);
					switchTool(mStaticTerrainTable);
				}
			}
		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		mMenuTable.row();
		button = new TextButton("Pickup", textToogleStyle);
		mWidgets.menu.pickup = button;
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PICKUP);
					switchTool(mPickupTable);
				}
			}
		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		mMenuTable.row();
		button = new TextButton("Enemy", textToogleStyle);
		mWidgets.menu.enemy = button;
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				mLevelEditor.switchTool(Tools.ENEMY);
				switchTool(mEnemyTable);
			}
		};
		toggleGroup.add(button);
		mMenuTable.add(button);

		mMainTable.add(mMenuTable);
		mMainTable.setTransform(true);

		mMainTable.invalidate();
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
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button = new TextButton("Enemy", toggleStyle);
		mWidgets.enemyMenu.enemy = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				mLevelEditor.switchTool(Tools.ENEMY);
			}
		};
		HideListener enemyHider = new HideListener(button, true);

		mEnemyTable.row();
		button = new TextButton("Path", toggleStyle);
		mWidgets.enemyMenu.path = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				// TODO set tool as path
				mLevelEditor.switchTool(Tools.PATH);
			}
		};
		HideListener pathHider = new HideListener(button, true);

		mEnemyTable.row();
		button = new TextButton("Trigger", toggleStyle);
		mWidgets.enemyMenu.trigger = button;
		buttonGroup.add(button);
		mEnemyTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				mLevelEditor.switchTool(null);
			}
		};
		HideListener triggerHider = new HideListener(button, true);


		// ---- Enemy ----
		mEnemyTable.row();
		buttonGroup = new ButtonGroup();
		button = new TextButton("Select", toggleStyle);
		mWidgets.enemy.select = button;
		enemyHider.addToggleActor(button);
		buttonGroup.add(button);
		mEnemyTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(States.SELECT);
				}
			}
		};

		mEnemyTable.row();
		button = new TextButton("Add", toggleStyle);
		mWidgets.enemy.add = button;
		enemyHider.addToggleActor(button);
		buttonGroup.add(button);
		mEnemyTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(States.ADD);
				}
			}
		};
		HideListener enemyAddHider = new HideListener(button, true);
		enemyHider.addChild(enemyAddHider);


		mEnemyTable.row();
		button = new TextButton("Remove", toggleStyle);
		mWidgets.enemy.remove = button;
		enemyHider.addToggleActor(button);
		buttonGroup.add(button);
		mEnemyTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(States.REMOVE);
				}
			}
		};

		mEnemyTable.row();
		button = new TextButton("Move", toggleStyle);
		mWidgets.enemy.move = button;
		enemyHider.addToggleActor(button);
		buttonGroup.add(button);
		mEnemyTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setEnemyState(States.MOVE);
				}
			}
		};

		// Select type
		mEnemyTable.row();
		button = new TextButton("Select type", textStyle);
		enemyAddHider.addToggleActor(button);
		mEnemyTable.add(button);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mLevelEditor.selectEnemy();
				}
				return true;
			}
		});


		// Enemy options when an enemy is selected
		// # Enemies
		mEnemyTable.row();
		Label label = new Label("# Enemies", labelStyle);
		mEnemyOptionsHider.addToggleActor(label);
		mEnemyTable.add(label);

		mEnemyTable.row();
		Slider slider = new Slider(Level.Enemy.ENEMIES_MIN, Level.Enemy.ENEMIES_MAX, Level.Enemy.ENEMIES_STEP_SIZE, false, sliderStyle);
		mEnemyOptionsHider.addToggleActor(slider);
		mWidgets.enemy.cEnemies = slider;
		mEnemyTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		mEnemyOptionsHider.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
		mEnemyTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mInvoker.execute(new CGuiSlider(mSlider, newValue, mLevelEditor.getEnemyCount()));
				mLevelEditor.setEnemyCount((int) (newValue + 0.5f));
			}
		};
		HideSliderValue delayHider = new HideSliderValue(slider, 2, Float.MAX_VALUE);
		mEnemyOptionsHider.addChild(delayHider);

		// Delay
		mEnemyTable.row();
		label = new Label("Spawn delay between enemies", labelStyle);
		delayHider.addToggleActor(label);
		mEnemyTable.add(label);

		mEnemyTable.row();
		slider = new Slider(Level.Enemy.DELAY_MIN, Level.Enemy.DELAY_MAX, Level.Enemy.DELAY_STEP_SIZE, false, sliderStyle);
		delayHider.addToggleActor(slider);
		mWidgets.enemy.delay = slider;
		mEnemyTable.add(slider);
		textField = new TextField("", textFieldStyle);
		delayHider.addToggleActor(textField);
		textField.setWidth(Config.Editor.TEXT_FIELD_NUMBER_WIDTH);
		mEnemyTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mInvoker.execute(new CGuiSlider(mSlider, newValue, mLevelEditor.getEnemySpawnDelay()));
				mLevelEditor.setEnemySpawnDelay(newValue);
			}
		};


		// ---- PATH -----
		mEnemyTable.row();
		buttonGroup = new ButtonGroup();
		button = new TextButton("Select", toggleStyle);
		buttonGroup.add(button);
		mEnemyTable.add(button);
		pathHider.addToggleActor(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPathState(null);
				}
			}
		};

		mEnemyTable.row();
		button = new TextButton("Add", toggleStyle);
		buttonGroup.add(button);
		mEnemyTable.add(button);
		pathHider.addToggleActor(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPathState(PathTool.States.ADD_CORNER);
				}
			}
		};

		mEnemyTable.row();
		button = new TextButton("Remove", toggleStyle);
		buttonGroup.add(button);
		mEnemyTable.add(button);
		pathHider.addToggleActor(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPathState(PathTool.States.REMOVE);
				}
			}
		};

		mEnemyTable.row();
		button = new TextButton("Move", toggleStyle);
		buttonGroup.add(button);
		mEnemyTable.add(button);
		pathHider.addToggleActor(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPathState(PathTool.States.MOVE);
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

		ButtonGroup toggleGroup = new ButtonGroup();
		Button button = new ImageButton(addStyle);
		mWidgets.pickup.add = button;
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupState(AddActorTool.States.ADD);
				}
			}
		};
		HideListener addHider = new HideListener(button, true);
		toggleGroup.add(button);
		mPickupTable.add(button);

		mPickupTable.row();
		button = new TextButton("Remove", toggleStyle);
		mWidgets.pickup.remove = button;
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupState(AddActorTool.States.REMOVE);
				}
			}
		};
		toggleGroup.add(button);
		mPickupTable.add(button);

		mPickupTable.row();
		button = new TextButton("Move", toggleStyle);
		mWidgets.pickup.move = button;
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupState(AddActorTool.States.MOVE);
				}
			}
		};
		toggleGroup.add(button);
		mPickupTable.add(button);

		mPickupTable.row();
		button = new TextButton("Select type", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mLevelEditor.selectPickup();
				}
				return true;
			}
		});
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
		ImageButtonStyle imageStyle = editorSkin.get("add", ImageButtonStyle.class);

		ButtonGroup toggleGroup = new ButtonGroup();
		Button button = new ImageButton(imageStyle);
		mWidgets.terrain.add = button;
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainState(DrawActorTool.States.ADD_CORNER);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);
		mStaticTerrainTable.row();

		button = new TextButton("Remove", textStyle);
		mWidgets.terrain.remove = button;
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainState(DrawActorTool.States.REMOVE);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);
		mStaticTerrainTable.row();

		button = new TextButton("Move", textStyle);
		mWidgets.terrain.move = button;
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
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
	 * Switches the GUI to the selected tool
	 * @param toolTable the tool's table we want to activate
	 */
	private void switchTool(AlignTable toolTable) {
		mMainTable.clear();
		toolTable.invalidate();
		mMainTable.add(toolTable);
		mMainTable.add(mMenuTable);
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

	/** Hiders */
	private HideManual mEnemyOptionsHider = new HideManual();

	/** Level editor the GUI will act on */
	private LevelEditor mLevelEditor = null;
	/** Invoker for level editor */
	private Invoker mInvoker = null;
	/** Inner widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	/**
	 * Container for inner widgets
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		MenuWidgets menu = new MenuWidgets();
		EnemyMenuWidget enemyMenu = new EnemyMenuWidget();
		EnemyWidgets enemy = new EnemyWidgets();
		PickupWidgets pickup = new PickupWidgets();
		TerrainWidgets terrain = new TerrainWidgets();

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

			Slider cEnemies = null;
			Slider delay = null;
		}

		static class PickupWidgets {
			Button add = null;
			Button remove = null;
			Button move = null;
		}

		static class TerrainWidgets {
			Button add = null;
			Button remove = null;
			Button move = null;
		}
	}
}
