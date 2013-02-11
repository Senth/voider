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
import com.spiddekauga.utils.CommandSequence;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.CheckedListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.Config.Editor.Enemy.Movement;
import com.spiddekauga.voider.Config.Editor.Enemy.Visual;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.ShapeTypes;
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
		super.initGui();

		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mMainTable.setCellPaddingDefault(2, 2, 2, 2);
		mMovementTable.setPreferences(mMainTable);
		mWeaponTable.setPreferences(mMainTable);
		mAiTable.setPreferences(mMainTable);
		mPathTable.setPreferences(mPathTable);
		mVisualTable.setPreferences(mMainTable);
		mOptionTable.setPreferences(mMainTable);


		initMovement();
		initWeapon();
		initVisual();
		initOptions();
		initMenu();

		resetValues();


		mMainTable.setTransform(true);
		mMovementTable.setTransform(true);
		mWeaponTable.setTransform(true);
		mAiTable.setTransform(true);
		mVisualTable.setTransform(true);
		mOptionTable.setTransform(true);
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

		// Aim
		mWidgets.weapon.aimRotateSpeed.setValue(mEnemyEditor.getAimRotateSpeed());
		mWidgets.weapon.aimRotateStartAngle.setValue(mEnemyEditor.getAimStartAngle());
		switch (mEnemyEditor.getAimType()) {
		case ON_PLAYER:
			mWidgets.weapon.aimOnPlayer.setChecked(true);
			break;

		case MOVE_DIRECTION:
			mWidgets.weapon.aimMoveDirection.setChecked(true);
			break;

		case IN_FRONT_OF_PLAYER:
			mWidgets.weapon.aimInFrontOfPlayer.setChecked(true);
			break;

		case ROTATE:
			mWidgets.weapon.aimRotate.setChecked(true);
			break;
		}


		// Visuals
		mWidgets.visual.startAngle.setValue(mEnemyEditor.getStartingAngle());

		// Shape
		mWidgets.visual.shapeCircleRadius.setValue(mEnemyEditor.getShapeRadius());
		mWidgets.visual.shapeRectangleWidth.setValue(mEnemyEditor.getShapeWidth());
		mWidgets.visual.shapeRectangleHeight.setValue(mEnemyEditor.getShapeHeight());
		mWidgets.visual.shapeTriangleWidth.setValue(mEnemyEditor.getShapeWidth());
		mWidgets.visual.shapeTriangleHeight.setValue(mEnemyEditor.getShapeHeight());
		switch (mEnemyEditor.getShapeType()) {
		case CIRCLE:
			mWidgets.visual.shapeCircle.setChecked(true);
			break;

		case RECTANGLE:
			mWidgets.visual.shapeRectangle.setChecked(true);
			break;

		case TRIANGLE:
			mWidgets.visual.shapeTriangle.setChecked(true);
			break;
		}


		// Options
		mWidgets.option.name.setText(mEnemyEditor.getName());
		mWidgets.option.description.setText(mEnemyEditor.getDescription());
		mWidgets.option.description.setTextFieldListener(null);
	}

	/**
	 * Initializes the menu buttons
	 */
	private void initMenu() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		final TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);
		final LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);

		// New Enemy
		Button button = new TextButton("New Enemy", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mEnemyEditor.isUnsaved()) {
						Button yes = new TextButton("Yes", textStyle);
						Button no = new TextButton("No", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						CommandSequence saveAndNew = new CommandSequence(new CeSave(mEnemyEditor), new CeNew(mEnemyEditor));

						mMsgBox.clear();
						mMsgBox.setTitle("New Enemy");
						mMsgBox.content("Your current enemy is unsaved.\n" +
								"Do you want to save it before creating a new enemy?");
						mMsgBox.button(yes, saveAndNew);
						mMsgBox.button(no, new CeNew(mEnemyEditor));
						mMsgBox.button(cancel);
						mMsgBox.show(getStage());
					} else {
						mEnemyEditor.newEnemy();
					}
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
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mEnemyEditor.isUnsaved()) {
						Button yes = new TextButton("Yes", textStyle);
						Button no = new TextButton("No", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						CommandSequence saveAndLoad = new CommandSequence(new CeSave(mEnemyEditor), new CeLoad(mEnemyEditor));

						mMsgBox.clear();
						mMsgBox.setTitle("Load Enemy");
						mMsgBox.content("Your current enemy is unsaved.\n" +
								"Do you want to save it before loading another enemy?");
						mMsgBox.button(yes, saveAndLoad);
						mMsgBox.button(no, new CeLoad(mEnemyEditor));
						mMsgBox.button(cancel);
						mMsgBox.show(getStage());
					} else {
						mEnemyEditor.loadEnemy();
					}
				}
				return true;
			}
		});
		mMainTable.add(button);

		// Duplicate
		button = new TextButton("Duplicate", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mEnemyEditor.isUnsaved()) {
						Button yes = new TextButton("Yes", textStyle);
						Button no = new TextButton("No", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						CommandSequence saveAndDuplicate = new CommandSequence(new CeSave(mEnemyEditor), new CeDuplicate(mEnemyEditor));

						mMsgBox.clear();
						mMsgBox.setTitle("Load Enemy");
						mMsgBox.content("Your current enemy is unsaved.\n" +
								"Do you want to save it before duplicating it?");
						mMsgBox.button(yes, saveAndDuplicate);
						mMsgBox.button(no, new CeDuplicate(mEnemyEditor));
						mMsgBox.button(cancel);
						mMsgBox.show(getStage());
					} else {
						mEnemyEditor.duplicateEnemy();
					}
				}
				return true;
			}
		});
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

		// Options
		button = new TextButton("Options", textToogleStyle);
		buttonGroup.add(button);
		mMainTable.add(button);
		mOptionHider.setButton(button);
		mOptionHider.addToggleActor(mOptionTable);

		mMainTable.row().setFillHeight(true).setAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.add(mWeaponTable);
		mMainTable.add(mVisualTable);
		mMainTable.add(mMovementTable);
		mMainTable.add(mOptionTable).setFillWidth(true).setFillHeight(true);
	}

	/**
	 * Initializes the movement GUI part
	 */
	private void initMovement() {
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
	private void initWeapon() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle toggleButtonStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textButtonStyle = editorSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = editorSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);

		mWeaponTable.setScalable(false);

		Button button = new TextButton("Weapons OFF", toggleButtonStyle);
		mWidgets.weapon.toggleButton = button;
		mWeaponTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					/** @TODO select weapon type */

					if (mButton instanceof TextButton) {
						((TextButton) mButton).setText("Weapons ON");
					}
					mEnemyEditor.setUseWeapon(true);
				} else {
					mEnemyEditor.setUseWeapon(false);
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
		weaponInnerHider.addChild(bulletHider);

		button = new TextButton("Aim", toggleButtonStyle);
		buttonGroup.add(button);
		mWeaponTable.add(button);
		weaponInnerHider.addToggleActor(button);
		HideListener aimHider = new HideListener(button, true);
		weaponInnerHider.addChild(aimHider);

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
				mEnemyEditor.setCooldownMin(newValue);
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
				mEnemyEditor.setCooldownMax(newValue);
			}
		};

		sliderMinListener.setGreaterSlider(sliderMax);
		sliderMaxListener.setLesserSlider(sliderMin);


		// -- Aim --
		// Aim on what?
		mWeaponTable.row();
		button = new TextButton("On Player", toggleButtonStyle);
		mWidgets.weapon.aimOnPlayer = button;
		mWeaponTable.add(button);
		aimHider.addToggleActor(button);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mEnemyEditor.setAimType(AimTypes.ON_PLAYER);
				}
			}
		};

		button = new TextButton("Move Dir", toggleButtonStyle);
		mWidgets.weapon.aimMoveDirection = button;
		mWeaponTable.add(button);
		aimHider.addToggleActor(button);
		buttonGroup.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mEnemyEditor.setAimType(AimTypes.MOVE_DIRECTION);
				}
			}
		};

		mWeaponTable.row();
		button = new TextButton("In front of Player", toggleButtonStyle);
		mWidgets.weapon.aimInFrontOfPlayer = button;
		mWeaponTable.add(button);
		aimHider.addToggleActor(button);
		buttonGroup.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				if (checked) {
					mEnemyEditor.setAimType(AimTypes.IN_FRONT_OF_PLAYER);
				}
			}
		};

		button = new TextButton("Rotate", toggleButtonStyle);
		mWidgets.weapon.aimRotate = button;
		mWeaponTable.add(button);
		buttonGroup.add(button);
		HideListener rotateHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setAimType(AimTypes.ROTATE);
			}
		};
		aimHider.addToggleActor(button);
		aimHider.addChild(rotateHider);


		// Rotate
		mWeaponTable.row();
		AlignTable rotateTable = new AlignTable();
		rotateTable.setPreferences(mWeaponTable);
		rotateTable.setScalable(false);
		rotateHider.addToggleActor(rotateTable);
		mWeaponTable.add(rotateTable);

		rotateTable.row();
		label = new Label("Start angle", labelStyle);
		rotateTable.add(label);
		rotateTable.row();
		slider = new Slider(Enemy.Weapon.START_ANGLE_MIN, Enemy.Weapon.START_ANGLE_MAX, Enemy.Weapon.START_ANGLE_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.aimRotateStartAngle = slider;
		rotateTable.add(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		rotateTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
			}
		};

		rotateTable.row();
		label = new Label("Rotate speed", labelStyle);
		rotateTable.add(label);
		rotateTable.row();
		slider = new Slider(Enemy.Weapon.ROTATE_SPEED_MIN, Enemy.Weapon.ROTATE_SPEED_MAX, Enemy.Weapon.ROTATE_SPEED_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.aimRotateSpeed = slider;
		rotateTable.add(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		rotateTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimRotateSpeed(newValue);
			}
		};


		weaponInnerHider.addToggleActor(bulletTable);
		mWeaponHider.addChild(weaponInnerHider);
	}

	/**
	 * Initializes the visual GUI parts
	 */
	private void initVisual() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = editorSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);
		TextButtonStyle toggleStyle = editorSkin.get("toggle", TextButtonStyle.class);

		mVisualTable.setScalable(false);


		// Starting angle
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


		// Different shapes
		mVisualTable.row();
		Button button = new TextButton("Circle", toggleStyle);
		mWidgets.visual.shapeCircle = button;
		mVisualTable.add(button);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(button);
		HideListener circleHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setShapeType(ShapeTypes.CIRCLE);
			}
		};

		button = new TextButton("Rect", toggleStyle);
		mWidgets.visual.shapeRectangle = button;
		mVisualTable.add(button);
		buttonGroup.add(button);
		HideListener rectangleHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setShapeType(ShapeTypes.RECTANGLE);
			}
		};

		button = new TextButton("Triangle", toggleStyle);
		mWidgets.visual.shapeTriangle = button;
		mVisualTable.add(button);
		buttonGroup.add(button);
		HideListener triangleHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setShapeType(ShapeTypes.TRIANGLE);
			}
		};


		// Circle
		mVisualTable.row();
		label = new Label("Radius", labelStyle);
		Cell cell = mVisualTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		circleHider.addToggleActor(label);

		slider = new Slider(Visual.RADIUS_MIN, Visual.RADIUS_MAX, Visual.RADIUS_STEP_SIZE, false, sliderStyle);
		mWidgets.visual.shapeCircleRadius = slider;
		mVisualTable.add(slider);
		circleHider.addToggleActor(slider);

		textField = new TextField("", textFieldStyle);
		mVisualTable.add(textField);
		textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		circleHider.addToggleActor(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setShapeRadius(newValue);
			}
		};


		// Rectangle
		mVisualTable.row();
		label = new Label("Width", labelStyle);
		cell = mVisualTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		rectangleHider.addToggleActor(label);

		slider = new Slider(Visual.SIZE_MIN, Visual.SIZE_MAX, Visual.SIZE_STEP_SIZE, false, sliderStyle);
		mWidgets.visual.shapeRectangleWidth = slider;
		mVisualTable.add(slider);
		rectangleHider.addToggleActor(slider);

		textField = new TextField("", textFieldStyle);
		mVisualTable.add(textField);
		textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		rectangleHider.addToggleActor(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setShapeWidth(newValue);
				mWidgets.visual.shapeTriangleWidth.setValue(newValue);
			}
		};

		mVisualTable.row();
		label = new Label("Height", labelStyle);
		cell = mVisualTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		rectangleHider.addToggleActor(label);

		slider = new Slider(Visual.SIZE_MIN, Visual.SIZE_MAX, Visual.SIZE_STEP_SIZE, false, sliderStyle);
		mWidgets.visual.shapeRectangleHeight = slider;
		mVisualTable.add(slider);
		rectangleHider.addToggleActor(slider);

		textField = new TextField("", textFieldStyle);
		mVisualTable.add(textField);
		textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		rectangleHider.addToggleActor(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setShapeHeight(newValue);
				mWidgets.visual.shapeTriangleHeight.setValue(newValue);
			}
		};


		// Triangle
		mVisualTable.row();
		label = new Label("Width", labelStyle);
		cell = mVisualTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		triangleHider.addToggleActor(label);

		slider = new Slider(Visual.SIZE_MIN, Visual.SIZE_MAX, Visual.SIZE_STEP_SIZE, false, sliderStyle);
		mWidgets.visual.shapeTriangleWidth = slider;
		mVisualTable.add(slider);
		triangleHider.addToggleActor(slider);

		textField = new TextField("", textFieldStyle);
		mVisualTable.add(textField);
		textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		triangleHider.addToggleActor(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setShapeWidth(newValue);
				mWidgets.visual.shapeRectangleWidth.setValue(newValue);
			}
		};

		mVisualTable.row();
		label = new Label("Height", labelStyle);
		cell = mVisualTable.add(label);
		cell.setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		triangleHider.addToggleActor(label);

		slider = new Slider(Visual.SIZE_MIN, Visual.SIZE_MAX, Visual.SIZE_STEP_SIZE, false, sliderStyle);
		mWidgets.visual.shapeTriangleHeight = slider;
		mVisualTable.add(slider);
		triangleHider.addToggleActor(slider);

		textField = new TextField("", textFieldStyle);
		mVisualTable.add(textField);
		textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
		triangleHider.addToggleActor(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setShapeHeight(newValue);
				mWidgets.visual.shapeTriangleHeight.setValue(newValue);
			}
		};



		circleHider.addChild(mVisualHider);
		rectangleHider.addChild(mVisualHider);
		triangleHider.addChild(mVisualHider);
	}

	/**
	 * Initializes GUI for options
	 */
	private void initOptions() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);

		mOptionTable.setScalable(false);
		mOptionTable.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mOptionTable.setName("optiontable");
		mOptionTable.setKeepSize(true);

		Label label = new Label("Name", labelStyle);
		mOptionTable.add(label);

		mOptionTable.row().setFillWidth(true);
		TextField textField = new TextField("", textFieldStyle);
		mOptionTable.add(textField).setFillWidth(true);
		mWidgets.option.name = textField;
		new TextFieldListener(textField, "Name") {
			@Override
			protected void onChange(String newText) {
				mEnemyEditor.setName(newText);
			}
		};

		mOptionTable.row();
		label = new Label("Description", labelStyle);
		mOptionTable.add(label);

		mOptionTable.row().setFillWidth(true).setFillHeight(true).setAlign(Horizontal.LEFT, Vertical.TOP);
		textField = new TextField("shit", textFieldStyle);
		mOptionTable.add(textField).setFillWidth(true).setFillHeight(true);
		mWidgets.option.description = textField;
		new TextFieldListener(textField, "Write your description here...") {
			@Override
			protected void onChange(String newText) {
				mEnemyEditor.setDescription(newText);
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
	/** Container for all general options, such as name description. */
	private AlignTable mOptionTable = new AlignTable();
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
	/** Hides options options :D:D:D */
	private HideListener mOptionHider = new HideListener(true);

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
		OptionWidgets option = new OptionWidgets();

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
			Button aimOnPlayer = null;
			Button aimMoveDirection = null;
			Button aimInFrontOfPlayer = null;
			Button aimRotate = null;

			// Aim - rotate
			Slider aimRotateStartAngle = null;
			Slider aimRotateSpeed = null;
		}

		/**
		 * Visual widgets
		 */
		static class VisualWidgets {
			Slider startAngle = null;

			// Shapes
			Button shapeCircle = null;
			Button shapeTriangle = null;
			Button shapeRectangle = null;

			Slider shapeCircleRadius = null;
			Slider shapeTriangleWidth = null;
			Slider shapeTriangleHeight = null;
			Slider shapeRectangleWidth = null;
			Slider shapeRectangleHeight = null;
		}

		/**
		 * General options
		 */
		static class OptionWidgets {
			TextField name = null;
			TextField description = null;
		}
	}

	/** The actual enemy editor bound to this gui */
	private EnemyEditor mEnemyEditor = null;
}
