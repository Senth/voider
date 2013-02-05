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

		resetValues();


		mMainTable.setTransform(true);
		mMovementTable.setTransform(true);
		mWeaponTable.setTransform(true);
		mAiTable.setTransform(true);
		mVisualTable.setTransform(true);
		mMainTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mMainTable.invalidate();
	}

	@Override
	public void resetValues() {
		// Movement
		switch (mEnemyEditor.getMovementType()) {
		case PATH:
			mWidgets.movement.pathBox.setChecked(true);
			break;

		case STATIONARY:
			mWidgets.movement.stationaryBox.setChecked(true);
			break;

		case AI:
			mWidgets.movement.aiBox.setChecked(true);
			break;
		}

		mWidgets.movement.speedSlider.setValue(mEnemyEditor.getSpeed());
		mWidgets.movement.turnSpeedToggleButton.setChecked(mEnemyEditor.isTurning());
		mWidgets.movement.turnSpeedSlider.setValue(mEnemyEditor.getTurnSpeed());

		// AI movement
		mWidgets.movement.aiDistanceMax.setValue(mEnemyEditor.getPlayerDistanceMax());
		mWidgets.movement.aiDistanceMin.setValue(mEnemyEditor.getPlayerDistanceMin());
		mWidgets.movement.aiRandomMovementToggleButton.setChecked(mEnemyEditor.isMovingRandomly());
		mWidgets.movement.aiRandomTimeMax.setValue(mEnemyEditor.getRandomTimeMax());
		mWidgets.movement.aiRandomTimeMin.setValue(mEnemyEditor.getRandomTimeMin());


		// Weapons
		mWidgets.weapon.toggleButton.setChecked(mEnemyEditor.hasWeapon());
		mWidgets.weapon.bulletSpeed.setValue(mEnemyEditor.getBulletSpeed());
		mWidgets.weapon.damage.setValue(mEnemyEditor.getDamage());
		mWidgets.weapon.cooldownMax.setValue(mEnemyEditor.getCooldownMax());
		mWidgets.weapon.cooldownMin.setValue(mEnemyEditor.getCooldownMin());


		// Visuals
		mWidgets.visual.startAngle.setValue(mEnemyEditor.getStartingAngle());
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

		// Path
		Row row = mMovementTable.row();
		row.setScalable(false);
		ButtonGroup buttonGroup = new ButtonGroup();
		mWidgets.movement.pathBox = new CheckBox("Path", checkBoxStyle);
		mWidgets.movement.pathBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event)) {
					addInnerTable(mPathTable, mMovementTypeTable);
					mEnemyEditor.setMovementType(MovementTypes.PATH);
				}

				return true;
			}
		});
		buttonGroup.add(mWidgets.movement.pathBox);
		Cell cell = mMovementTable.add(mWidgets.movement.pathBox);
		cell.setPadRight(10);


		// Stationary
		mWidgets.movement.stationaryBox = new CheckBox("Stationary", checkBoxStyle);
		mWidgets.movement.stationaryBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event)) {
					addInnerTable(null, mMovementTypeTable);
					mEnemyEditor.setMovementType(MovementTypes.STATIONARY);
				}
				return true;
			}
		});
		buttonGroup.add(mWidgets.movement.stationaryBox);
		cell = mMovementTable.add(mWidgets.movement.stationaryBox);
		cell.setPadRight(10);


		// AI
		mWidgets.movement.aiBox = new CheckBox("AI", checkBoxStyle);
		mWidgets.movement.aiBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event)) {
					addInnerTable(mPathTable, mMovementTypeTable);
					mMovementTypeTable.row();
					mMovementTypeTable.add(mAiTable);
					mAiTable.invalidate();
					mEnemyEditor.setMovementType(MovementTypes.AI);
				}
				return true;
			}
		});
		buttonGroup.add(mWidgets.movement.aiBox);
		mMovementTable.add(mWidgets.movement.aiBox);
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
		mWidgets.movement.speedSlider = new Slider(Enemy.Movement.MOVE_SPEED_MIN, Enemy.Movement.MOVE_SPEED_MAX, Enemy.Movement.MOVE_SPEED_STEP_SIZE, false, sliderStyle);
		mPathTable.add(mWidgets.movement.speedSlider );
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mPathTable.add(textField);
		new SliderListener(mWidgets.movement.speedSlider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setSpeed(newValue);
			}
		};

		// Turning
		row = mPathTable.row();
		row.setScalable(false);
		mWidgets.movement.turnSpeedToggleButton = new TextButton("Turning speed OFF", textToogleStyle);
		mPathTable.add(mWidgets.movement.turnSpeedToggleButton);
		HideListener hideListener = new HideListener(mWidgets.movement.turnSpeedToggleButton, true) {
			@Override
			protected void onShow() {
				if (mButton instanceof TextButton) {
					((TextButton) mButton).setText("Turning speed ON");
				}
				mEnemyEditor.setTurning(true);
			}

			@Override
			protected void onHide() {
				if (mButton instanceof TextButton) {
					((TextButton) mButton).setText("Turning speed OFF");
				}
				mEnemyEditor.setTurning(false);
			}
		};

		row = mPathTable.row();
		row.setScalable(false);
		mWidgets.movement.turnSpeedSlider = new Slider(Movement.TURN_SPEED_MIN, Movement.TURN_SPEED_MAX, Enemy.Movement.TURN_SPEED_STEP_SIZE, false, sliderStyle);
		mPathTable.add(mWidgets.movement.turnSpeedSlider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mPathTable.add(textField);
		new SliderListener(mWidgets.movement.turnSpeedSlider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setTurnSpeed(newValue);
			}
		};
		mMovementHider.addChild(hideListener);
		hideListener.addToggleActor(mWidgets.movement.turnSpeedSlider);
		hideListener.addToggleActor(textField);


		// --- Movement AI ---
		mAiTable.setScalable(false);
		mAiTable.row();
		label = new Label("Distance", labelStyle);
		mAiTable.add(label);

		label = new Label("Min", labelStyle);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		mWidgets.movement.aiDistanceMin = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, sliderStyle);
		mAiTable.add(mWidgets.movement.aiDistanceMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		SliderListener sliderMinListener = new SliderListener(mWidgets.movement.aiDistanceMin, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMin(newValue);
			}
		};

		mAiTable.row();
		label = new Label("Max", labelStyle);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		mWidgets.movement.aiDistanceMax = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, sliderStyle);
		mAiTable.add(mWidgets.movement.aiDistanceMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		SliderListener sliderMaxListener = new SliderListener(mWidgets.movement.aiDistanceMax, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMax(newValue);
			}
		};

		sliderMinListener.setGreaterSlider(mWidgets.movement.aiDistanceMax);
		sliderMaxListener.setLesserSlider(mWidgets.movement.aiDistanceMin);


		mAiTable.row();
		mWidgets.movement.aiRandomMovementToggleButton = new TextButton("Random Movement OFF", textToogleStyle);
		mAiTable.add(mWidgets.movement.aiRandomMovementToggleButton);
		hideListener = new HideListener(mWidgets.movement.aiRandomMovementToggleButton, true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setMoveRandomly(true);

				if (mButton instanceof TextButton) {
					((TextButton) mButton).setText("Random Movement ON");
				}
			}

			@Override
			protected void onHide() {
				mEnemyEditor.setMoveRandomly(false);

				if (mButton instanceof TextButton) {
					((TextButton) mButton).setText("Random Movement OFF");
				}
			}
		};
		mMovementHider.addChild(hideListener);

		label = new Label("Min", labelStyle);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		mWidgets.movement.aiRandomTimeMin = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, sliderStyle);
		mAiTable.add(mWidgets.movement.aiRandomTimeMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		sliderMinListener = new SliderListener(mWidgets.movement.aiRandomTimeMin, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMin(newValue);
			}
		};
		hideListener.addToggleActor(label);
		hideListener.addToggleActor(mWidgets.movement.aiRandomTimeMin);
		hideListener.addToggleActor(textField);


		label = new Label("Max", labelStyle);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		mWidgets.movement.aiRandomTimeMax = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, sliderStyle);
		mAiTable.add(mWidgets.movement.aiRandomTimeMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		sliderMaxListener = new SliderListener(mWidgets.movement.aiRandomTimeMax, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMax(newValue);
			}
		};
		hideListener.addToggleActor(label);
		hideListener.addToggleActor(mWidgets.movement.aiRandomTimeMax);
		hideListener.addToggleActor(textField);

		sliderMinListener.setGreaterSlider(mWidgets.movement.aiRandomTimeMax);
		sliderMaxListener.setLesserSlider(mWidgets.movement.aiRandomTimeMin);
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

		mWidgets.weapon.toggleButton = new TextButton("Weapons OFF", toggleButtonStyle);
		mWeaponTable.add(mWidgets.weapon.toggleButton);
		new CheckedListener(mWidgets.weapon.toggleButton) {
			@Override
			protected void onChange(boolean checked) {
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
		HideListener weaponInnerHider = new HideListener(mWidgets.weapon.toggleButton, true);

		// TYPES
		mWeaponTable.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button = new TextButton("Bullet", toggleButtonStyle);
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
		mWidgets.weapon.bulletSpeed = slider;
		bulletTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setBulletSpeed(newValue);
			}
		};

		// Damage
		bulletTable.row();
		label = new Label("Damage", labelStyle);
		cell = bulletTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		slider = new Slider(Weapon.DAMAGE_MIN, Weapon.DAMAGE_MAX, Weapon.DAMAGE_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.damage = slider;
		bulletTable.add(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
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
		mWidgets.weapon.cooldownMin = sliderMin;
		bulletTable.add(sliderMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		SliderListener sliderMinListener = new SliderListener(sliderMin, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMin(newValue);
			}
		};


		label = new Label("Max", labelStyle);
		bulletTable.row();
		cell = bulletTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMax = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.cooldownMax = sliderMax;
		bulletTable.add(sliderMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		SliderListener sliderMaxListener = new SliderListener(sliderMax, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMax(newValue);
			}
		};

		sliderMinListener.setGreaterSlider(sliderMax);
		sliderMaxListener.setLesserSlider(sliderMin);



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
		mWidgets.visual.startAngle = slider;
		mVisualTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mVisualTable.add(textField);

		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
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

	/** All widgets that needs updating when an actor is changed */
	private InnerWidgets mWidgets = new InnerWidgets();

	/**
	 * All the widgets which state can be changed and thus reset
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		MovementWidgets movement = new MovementWidgets();
		WeaponWidgets weapon = new WeaponWidgets();
		VisualWidgets visual = new VisualWidgets();

		/**
		 * Movement widget
		 */
		static class MovementWidgets {
			// Movement type
			CheckBox pathBox = null;
			CheckBox stationaryBox = null;
			CheckBox aiBox = null;

			// Generic movement variables
			Slider speedSlider = null;
			Button turnSpeedToggleButton = null;
			Slider turnSpeedSlider = null;


			// AI Movement
			Slider aiDistanceMin = null;
			Slider aiDistanceMax = null;
			Button aiRandomMovementToggleButton = null;
			Slider aiRandomTimeMin = null;
			Slider aiRandomTimeMax = null;
		}


		/**
		 * Weapon widgets
		 */
		static class WeaponWidgets {
			Button toggleButton = null;


			// Bullet
			Slider bulletSpeed = null;
			Slider damage = null;
			Slider cooldownMin = null;
			Slider cooldownMax = null;


			// Aim
		}

		/**
		 * Visual vidgets
		 */
		static class VisualWidgets {
			Slider startAngle = null;
		}
	}

	/** The actual enemy editor bound to this gui */
	private EnemyEditor mEnemyEditor = null;
}