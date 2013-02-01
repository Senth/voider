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
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.Config.Editor.Enemy.Movement;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the enemy editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class EnemyEditorGui extends Gui {
	/**
	 * Takes an enemy editor that will be bound to this GUI
	 * @param enemyEditor the scene that will be bound to this GUI
	 */
	public void setEnemyEditor(EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
	}

	@Override
	public void initGui() {
		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mMainTable.setCellPaddingDefault(2, 2, 2, 2);
		mMovementTable.setPreferences(mMainTable);
		mWeaponTable.setPreferences(mMainTable);
		mAiTable.setPreferences(mMainTable);
		mPathTable.setPreferences(mPathTable);
		mVisualTable.setPreferences(mMainTable);


		initMovement();
		initWeapon();
		initVisual();
		initMenu();


		mMainTable.setTransform(true);
		mMovementTable.setTransform(true);
		mWeaponTable.setTransform(true);
		mAiTable.setTransform(true);
		mVisualTable.setTransform(true);
		mMainTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mMainTable.invalidate();
	}

	/**
	 * Initializes the menu buttons
	 */
	void initMenu() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);


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
					mEnemyEditor.saveEnemy();
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


		// --- Active options ---
		Row row = mMainTable.row();
		row.setAlign(Horizontal.CENTER, Vertical.BOTTOM);
		ButtonGroup buttonGroup = new ButtonGroup();

		// Movement
		button = new TextButton("Movement", textToogleStyle);
		buttonGroup.add(button);
		mMainTable.add(button);
		mMovementHider.addToggleActor(mMovementTable);
		mMovementHider.setButton(button);

		// Weapons
		button = new TextButton("Weapons", textToogleStyle);
		buttonGroup.add(button);
		mMainTable.add(button);
		mWeaponHider.addToggleActor(mWeaponTable);
		mWeaponHider.setButton(button);

		// Visuals
		button = new TextButton("Visuals", textToogleStyle);
		buttonGroup.add(button);
		mMainTable.add(button);
		mVisualHider.setButton(button);
		mVisualHider.addToggleActor(mVisualTable);

		mMainTable.row();
		mMainTable.add(mWeaponTable);
		mMainTable.add(mVisualTable);
		mMainTable.add(mMovementTable);
	}

	/**
	 * Initializes the movement GUI part
	 */
	void initMovement() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		CheckBoxStyle checkBoxStyle = editorSkin.get("default", CheckBoxStyle.class);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = editorSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);
		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);

		// Type of movement?
		MovementTypes movementType = mEnemyEditor.getMovementType();
		// Path
		Row row = mMovementTable.row();
		row.setScalable(false);
		ButtonGroup buttonGroup = new ButtonGroup();
		CheckBox checkBox = new CheckBox("Path", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event) && mEnemyEditor.getMovementType() != MovementTypes.PATH) {
					addInnerTable(mPathTable, mMovementTypeTable);
					mEnemyEditor.setMovementType(MovementTypes.PATH);
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
				if (isButtonChecked(event) && mEnemyEditor.getMovementType() != MovementTypes.STATIONARY) {
					addInnerTable(null, mMovementTypeTable);
					mEnemyEditor.setMovementType(MovementTypes.STATIONARY);
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
				if (isButtonChecked(event) && mEnemyEditor.getMovementType() != MovementTypes.AI) {
					addInnerTable(mPathTable, mMovementTypeTable);
					mMovementTypeTable.row();
					mMovementTypeTable.add(mAiTable);
					mAiTable.invalidate();
					mEnemyEditor.setMovementType(MovementTypes.AI);
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
		Slider slider = new Slider(Enemy.Movement.MOVE_SPEED_MIN, Enemy.Movement.MOVE_SPEED_MAX, Enemy.Movement.MOVE_SPEED_STEP_SIZE, false, sliderStyle);
		mPathTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mPathTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			public void onChange(float newValue) {
				mEnemyEditor.setSpeed(newValue);
			}
		};
		slider.setValue(Enemy.Movement.MOVE_SPEED_DEFAULT);

		// Turning
		row = mPathTable.row();
		row.setScalable(false);
		Button button = new TextButton("On", textToogleStyle);
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
					mEnemyEditor.setTurning(disabled);
				}
			}
		};
		label = new Label("Turning speed", labelStyle);
		mPathTable.add(label);
		row = mPathTable.row();
		row.setScalable(false);
		slider = new Slider(Movement.TURN_SPEED_MIN, Movement.TURN_SPEED_MAX, Enemy.Movement.TURN_SPEED_STEP_SIZE, false, sliderStyle);
		mPathTable.add(slider);
		disableListener.addToggleActor(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mPathTable.add(textField);
		disableListener.addToggleActor(textField);
		new SliderListener(slider, textField) {
			@Override
			public void onChange(float newValue) {
				mEnemyEditor.setTurnSpeed(newValue);
			}
		};
		slider.setValue(mEnemyEditor.getTurnSpeed());


		// --- Movement AI ---
		mAiTable.setScalable(false);
		mAiTable.row();
		label = new Label("Distance", labelStyle);
		mAiTable.add(label);

		label = new Label("Min", labelStyle);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		Slider sliderMin = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, sliderStyle);
		sliderMin.setValue(mEnemyEditor.getPlayerDistanceMin());
		mAiTable.add(sliderMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
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
				mEnemyEditor.setPlayerDistanceMin(newValue);
			}
		};

		mAiTable.row();
		label = new Label("Max", labelStyle);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		Slider sliderMax = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, sliderStyle);
		sliderMax.setValue(mEnemyEditor.getPlayerDistanceMax());
		mAiTable.add(sliderMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
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
				mEnemyEditor.setPlayerDistanceMax(newValue);
			}
		};

		sliderMinListener.setValidatingObject(sliderMax);
		sliderMaxListener.setValidatingObject(sliderMin);


		mAiTable.row();
		button = new TextButton("Random Movement", textToogleStyle);
		button.setChecked(false);
		mAiTable.add(button);
		disableListener = new DisableListener(button) {
			@Override
			public void onChange(boolean disabled) {
				if (mButton instanceof TextButton) {
					mEnemyEditor.setMoveRandomly(!disabled);
				}
			}
		};

		label = new Label("Min", labelStyle);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		sliderMin = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, sliderStyle);
		mAiTable.add(sliderMin);
		disableListener.addToggleActor(sliderMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		disableListener.addToggleActor(textField);
		sliderMin.setValue(mEnemyEditor.getRandomTimeMin());
		sliderMinListener = new SliderListener(sliderMin, textField) {
			@Override
			protected boolean isValidValue(float newValue) {
				if (mValidingObject instanceof Slider) {
					return ((Slider) mValidingObject).getValue() >= mSlider.getValue();
				}
				return false;
			}

			@Override
			public void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMin(newValue);
			}
		};


		label = new Label("Max", labelStyle);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		sliderMax = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, sliderStyle);
		mAiTable.add(sliderMax);
		disableListener.addToggleActor(sliderMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		disableListener.addToggleActor(textField);
		sliderMax.setValue(mEnemyEditor.getRandomTimeMax());
		sliderMaxListener = new SliderListener(sliderMax, textField) {
			@Override
			protected boolean isValidValue(float newValue) {
				if (mValidingObject instanceof Slider) {
					return ((Slider) mValidingObject).getValue() <= mSlider.getValue();
				}
				return false;
			}

			@Override
			public void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMax(newValue);
			}
		};

		sliderMinListener.setValidatingObject(sliderMax);
		sliderMaxListener.setValidatingObject(sliderMin);
	}

	/**
	 * Initializes the weapon GUI part
	 */
	void initWeapon() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle toggleButtonStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textButtonStyle = editorSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = editorSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);

		mWeaponTable.setScalable(false);

		Button button = new TextButton("Weapons OFF", toggleButtonStyle);
		mWeaponTable.add(button);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					/** @TODO select weapon type */

					if (mButton instanceof TextButton) {
						((TextButton) mButton).setText("Weapons ON");
					}
				} else {
					mEnemyEditor.setBulletActorDef(null);
					if (mButton instanceof TextButton) {
						((TextButton) mButton).setText("Weapons OFF");
					}
				}
			}
		};
		HideListener weaponInnerHider = new HideListener(button, true);

		// TYPES
		mWeaponTable.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		button = new TextButton("Bullet", toggleButtonStyle);
		buttonGroup.add(button);
		mWeaponTable.add(button);
		weaponInnerHider.addToggleActor(button);
		HideListener bulletHider = new HideListener(button, true);

		button = new TextButton("Aim", toggleButtonStyle);
		buttonGroup.add(button);
		mWeaponTable.add(button);
		weaponInnerHider.addToggleActor(button);
		weaponInnerHider.addChild(bulletHider);


		// BULLET
		AlignTable bulletTable = new AlignTable();
		bulletTable.setPreferences(mWeaponTable);
		mWeaponTable.row();
		mWeaponTable.add(bulletTable);
		bulletHider.addToggleActor(bulletTable);

		// Select type
		button = new TextButton("Select bullet type", textButtonStyle);
		bulletTable.add(button);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					/** @TODO select weapon type */
				}
				return true;
			}
		});

		/** @TODO set weapon name */

		// Speed
		bulletTable.row();
		Label label = new Label("Speed", labelStyle);
		Cell cell = bulletTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		Slider slider = new Slider(Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE, false, sliderStyle);
		slider.setValue(mEnemyEditor.getBulletSpeed());
		bulletTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			public void onChange(float newValue) {
				mEnemyEditor.setBulletSpeed(newValue);
			}
		};

		// Damage
		bulletTable.row();
		label = new Label("Damage", labelStyle);
		cell = bulletTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		slider = new Slider(Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE, false, sliderStyle);
		slider.setValue(mEnemyEditor.getDamage());
		bulletTable.add(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			public void onChange(float newValue) {
				mEnemyEditor.setBulletSpeed(newValue);
			}
		};

		// Cooldown
		label = new Label("Cooldown time", labelStyle);
		bulletTable.row();
		bulletTable.add(label);
		label = new Label("Min", labelStyle);
		bulletTable.row();
		cell = bulletTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMin = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, sliderStyle);
		bulletTable.add(sliderMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		sliderMin.setValue(mEnemyEditor.getCooldownMin());
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
				mEnemyEditor.setRandomTimeMin(newValue);
			}
		};


		label = new Label("Max", labelStyle);
		bulletTable.row();
		cell = bulletTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMax = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, sliderStyle);
		bulletTable.add(sliderMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		sliderMax.setValue(mEnemyEditor.getCooldownMax());
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
				mEnemyEditor.setRandomTimeMax(newValue);
			}
		};

		sliderMinListener.setValidatingObject(sliderMax);
		sliderMaxListener.setValidatingObject(sliderMin);



		weaponInnerHider.addToggleActor(bulletTable);
		mWeaponHider.addChild(weaponInnerHider);
	}

	/**
	 * Initializes the visual GUI parts
	 */
	void initVisual() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = editorSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);

		mVisualTable.setScalable(false);

		Label label = new Label("Starting angle", labelStyle);
		mVisualTable.add(label);


		mVisualTable.row();
		Slider slider = new Slider(0, 360, 1, false, sliderStyle);
		mVisualTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mVisualTable.add(textField);

		new SliderListener(slider, textField) {
			@Override
			public void onChange(float newValue) {
				mEnemyEditor.setStartingAngle(newValue);
			}
		};
	}

	// Tables
	/** Container for all movement options */
	private AlignTable mMovementTable = new AlignTable();
	/** Container for all weapon options */
	private AlignTable mWeaponTable = new AlignTable();
	/** Container for all visual options */
	private AlignTable mVisualTable = new AlignTable();
	/** Container for the different movement variables */
	private AlignTable mMovementTypeTable = new AlignTable();
	/** Table for Path movement */
	private AlignTable mPathTable = new AlignTable();
	/** Table for AI movement */
	private AlignTable mAiTable = new AlignTable();

	// Hiders
	/** Hides weapon options */
	private HideListener mWeaponHider = new HideListener(true);
	/** Hides visual options */
	private HideListener mVisualHider = new HideListener(true);
	/** Hides movement options */
	private HideListener mMovementHider = new HideListener(true);

	/** The actual enemy editor bound to this gui */
	private EnemyEditor mEnemyEditor = null;
}
