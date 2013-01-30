package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.CheckedListener;
import com.spiddekauga.utils.scene.ui.DisableListener;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the enemy editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class EnemyEditorGui extends Gui {
	/**
	 * Initializes the variables needed for this GUI class
	 * @param enemyActor
	 * @param enemyPathOnce
	 * @param enemyPathLoop
	 * @param enemyPathBackAndForth
	 * @param def
	 * @param enemyEditor
	 * @param pathLabels
	 */
	public void init(
			EnemyActor enemyActor,
			EnemyActor enemyPathOnce,
			EnemyActor enemyPathLoop,
			EnemyActor enemyPathBackAndForth,
			EnemyActorDef def,
			EnemyEditor enemyEditor,
			Table pathLabels)
	{
		mEnemyActor = enemyActor;
		mEnemyPathOnce = enemyPathOnce;
		mEnemyPathLoop = enemyPathLoop;
		mEnemyPathBackAndForth = enemyPathBackAndForth;
		mDef = def;
		mEnemyEditor = enemyEditor;
		mPathLabels = pathLabels;
	}

	@Override
	public void initGui() {
		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mMainTable.setCellPaddingDefault(2, 2, 2, 2);
		mMovementTable.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mMovementTable.setCellPaddingDefault(2, 2, 2, 2);
		mWeaponTable.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mWeaponTable.setCellPaddingDefault(2, 2, 2, 2);
		mAiTable.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mAiTable.setCellPaddingDefault(2, 2, 2, 2);
		mPathTable.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mPathTable.setCellPaddingDefault(2, 2, 2, 2);


		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = editorSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);

		// New Enemy
		Button button = new TextButton("New Enemy", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					/** @TODO Check if player want to save old actor */

					mEnemyEditor.newEnemy();
				}
				return true;
			}
		});
		mMainTable.add(button);

		// Save
		button = new TextButton("Save", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					ResourceSaver.save(mDef);
				}
				return true;
			}
		});
		mMainTable.add(button);

		// Load
		button = new TextButton("Load", textStyle);
		/** @TODO load enemy actor, use browser */
		mMainTable.add(button);

		// Duplicate
		button = new TextButton("Duplicate", textStyle);
		/** @TODO duplicate enemy actor, use browser */
		mMainTable.add(button);


		// --- Type (Movement OR Weapons) ---
		// Movement
		Row row = mMainTable.row();
		row.setAlign(Horizontal.CENTER, Vertical.BOTTOM);
		ButtonGroup buttonGroup = new ButtonGroup();
		button = new TextButton("Movement", textToogleStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					addInnerTable(mMovementTable, mTypeTable);
				}
			}
		};
		buttonGroup.add(button);
		mMainTable.add(button);

		// Weapons
		button = new TextButton("Weapons", textToogleStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					addInnerTable(mWeaponTable, mTypeTable);
				}
			}
		};
		buttonGroup.add(button);
		mMainTable.add(button);

		mMainTable.row();
		mMainTable.add(mTypeTable);


		// Type of movement?
		MovementTypes movementType = mDef.getMovementType();
		mDef.setMovementType(null);
		// Path
		row = mMovementTable.row();
		row.setScalable(false);
		buttonGroup = new ButtonGroup();
		CheckBoxStyle checkBoxStyle = editorSkin.get("default", CheckBoxStyle.class);
		CheckBox checkBox = new CheckBox("Path", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event) && mDef.getMovementType() != MovementTypes.PATH) {
					addInnerTable(mPathTable, mMovementTypeTable);
					addActor(mPathLabels);
					mDef.setMovementType(MovementTypes.PATH);
					mEnemyActor.destroyBody();
					mEnemyEditor.createPathBodies();
					mEnemyEditor.resetPlayerPosition();
				}

				return true;
			}
		});
		buttonGroup.add(checkBox);
		checkBox.setChecked(movementType == MovementTypes.PATH);
		Cell cell = mMovementTable.add(checkBox);
		cell.setPadRight(10);


		// Stationary
		checkBox = new CheckBox("Stationary", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event) && mDef.getMovementType() != MovementTypes.STATIONARY) {
					addInnerTable(null, mMovementTypeTable);
					mDef.setMovementType(MovementTypes.STATIONARY);
					mEnemyEditor.clearExamplePaths();
					mEnemyActor.destroyBody();
					createEnemyActor();
					mEnemyEditor.resetPlayerPosition();
				}
				return true;
			}
		});
		buttonGroup.add(checkBox);
		checkBox.setChecked(movementType == MovementTypes.STATIONARY);
		cell = mMovementTable.add(checkBox);
		cell.setPadRight(10);


		// AI
		checkBox = new CheckBox("AI", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event) && mDef.getMovementType() != MovementTypes.AI) {
					addInnerTable(mPathTable, mMovementTypeTable);
					mMovementTypeTable.row();
					mMovementTypeTable.add(mAiTable);
					mAiTable.invalidate();
					mDef.setMovementType(MovementTypes.AI);
					mEnemyEditor.clearExamplePaths();
					mEnemyActor.destroyBody();
					createEnemyActor();
					mEnemyEditor.resetPlayerPosition();
				}
				return true;
			}
		});
		buttonGroup.add(checkBox);
		checkBox.setChecked(movementType == MovementTypes.AI);
		mMovementTable.add(checkBox);
		mMovementTable.row();
		mMovementTable.add(mMovementTypeTable);


		// --- MOVEMENT path/AI ---
		// Movement Speed
		row = mMovementTable.row();
		row.setScalable(false);
		Label label = new Label("Movement speed", labelStyle);
		mPathTable.add(label);
		row = mPathTable.row();
		row.setScalable(false);
		Slider slider = new Slider(Config.Editor.Enemy.MOVE_SPEED_MIN, Config.Editor.Enemy.MOVE_SPEED_MAX, Config.Editor.Enemy.MOVE_SPEED_STEP_SIZE, false, sliderStyle);
		mPathTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Config.Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mPathTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			public void onChange(float newValue) {
				mDef.setSpeed(newValue);
				mEnemyActor.setSpeed(newValue);
				mEnemyPathBackAndForth.setSpeed(newValue);
				mEnemyPathLoop.setSpeed(newValue);
				mEnemyPathOnce.setSpeed(newValue);
			}
		};
		slider.setValue(Config.Editor.Enemy.MOVE_SPEED_DEFAULT);

		// Turning
		row = mPathTable.row();
		row.setScalable(false);
		button = new TextButton("On", textToogleStyle);
		button.setChecked(false);
		mPathTable.add(button);
		DisableListener disableListener = new DisableListener(button) {
			@Override
			public void onChange(boolean disabled) {
				if (mButton instanceof TextButton) {
					if (disabled) {
						((TextButton)mButton).setText("Off");
					} else {
						((TextButton)mButton).setText("On");
					}
					mDef.setTurn(!disabled);
					mEnemyActor.resetPathMovement();
					mEnemyPathBackAndForth.resetPathMovement();
					mEnemyPathLoop.resetPathMovement();
					mEnemyPathOnce.resetPathMovement();
				}
			}
		};
		label = new Label("Turning speed", labelStyle);
		mPathTable.add(label);
		row = mPathTable.row();
		row.setScalable(false);
		slider = new Slider(Config.Editor.Enemy.TURN_SPEED_MIN, Config.Editor.Enemy.TURN_SPEED_MAX, Config.Editor.Enemy.TURN_SPEED_STEP_SIZE, false, sliderStyle);
		mPathTable.add(slider);
		disableListener.addToggleActor(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Config.Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mPathTable.add(textField);
		disableListener.addToggleActor(textField);
		new SliderListener(slider, textField) {
			@Override
			public void onChange(float newValue) {
				mDef.setTurnSpeed(newValue);
			}
		};
		slider.setValue(Config.Editor.Enemy.TURN_SPEED_DEFAULT);

		// Labels for path movement
		label = new Label("Back and Forth", labelStyle);
		Table wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		label = new Label("Loop", labelStyle);
		wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		label = new Label("Once", labelStyle);
		wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();


		// --- Movement AI ---
		mAiTable.setScalable(false);
		mAiTable.row();
		label = new Label("Minimum distance", labelStyle);
		mAiTable.add(label);
		mAiTable.row();
		Slider sliderMin = new Slider(Config.Editor.Enemy.AI_DISTANCE_MIN, Config.Editor.Enemy.AI_DISTANCE_MAX, Config.Editor.Enemy.AI_DISTANCE_STEP_SIZE, false, sliderStyle);
		sliderMin.setValue(Config.Editor.Enemy.AI_DISTANCE_MIN_DEFAULT);
		mAiTable.add(sliderMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Config.Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		SliderListener sliderMinListener = new SliderListener(sliderMin, textField) {
			@Override
			protected boolean isValidValue(float newValue) {
				if (mValidingObject instanceof Slider) {
					return ((Slider) mValidingObject).getValue() >= mSlider.getValue();
				}
				return false;
			}

			@Override
			public void onChange(float newValue) {
				mDef.setPlayerDistanceMin(newValue);
			}
		};

		mAiTable.row();
		label = new Label("Maximum distance", labelStyle);
		mAiTable.add(label);
		mAiTable.row();
		Slider sliderMax = new Slider(Config.Editor.Enemy.AI_DISTANCE_MIN, Config.Editor.Enemy.AI_DISTANCE_MAX, Config.Editor.Enemy.AI_DISTANCE_STEP_SIZE, false, sliderStyle);
		sliderMax.setValue(Config.Editor.Enemy.AI_DISTANCE_MAX_DEFAULT);
		mAiTable.add(sliderMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Config.Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		SliderListener sliderMaxListener = new SliderListener(sliderMax, textField) {
			@Override
			protected boolean isValidValue(float newValue) {
				if (mValidingObject instanceof Slider) {
					return ((Slider) mValidingObject).getValue() <= mSlider.getValue();
				}
				return false;
			}

			@Override
			public void onChange(float newValue) {
				mDef.setPlayerDistanceMax(newValue);
			}
		};

		sliderMinListener.setValidatingObject(sliderMax);
		sliderMaxListener.setValidatingObject(sliderMin);

		mEnemyEditor.scalePathLabels();
		mMainTable.setTransform(true);
		mMovementTable.setTransform(true);
		mWeaponTable.setTransform(true);
		mAiTable.setTransform(true);
		mMainTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/**
	 * Creates the enemy actor and resets its position
	 */
	private void createEnemyActor() {
		mEnemyActor.setPosition(0, 0);
		mEnemyActor.createBody();
	}



	// Tables
	/** Wrapping table for the activate type */
	private AlignTable mTypeTable = new AlignTable();
	/** Container for all movement options */
	private AlignTable mMovementTable = new AlignTable();
	/** Container for all weapon options */
	private AlignTable mWeaponTable = new AlignTable();
	/** Container for the different movement variables */
	private AlignTable mMovementTypeTable = new AlignTable();
	/** Table for Path movement */
	private AlignTable mPathTable = new AlignTable();
	/** Table for AI movement */
	private AlignTable mAiTable = new AlignTable();

	// EnemyEditor variables
	/** The actual enemy editor bound to this gui */
	private EnemyEditor mEnemyEditor = null;
	/** Current enemy actor */
	private EnemyActor mEnemyActor = null;
	/** Enemy actor for path once */
	private EnemyActor mEnemyPathOnce = null;
	/** Enemy actor for path loop */
	private EnemyActor mEnemyPathLoop = null;
	/** Enemy actor for path back and forth */
	private EnemyActor mEnemyPathBackAndForth = null;
	/** Current enemy actor definition */
	private EnemyActorDef mDef = null;
	/** Table for path lables, these are added directly to the stage */
	private Table mPathLabels = null;
}
