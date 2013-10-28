package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
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
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.Config.Editor.Enemy.Movement;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.Config.Gui;
import com.spiddekauga.voider.editor.commands.CGuiCheck;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.utils.Messages;

/**
 * GUI for the enemy editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyEditorGui extends ActorGui {
	/**
	 * Takes an enemy editor that will be bound to this GUI
	 * @param enemyEditor the scene that will be bound to this GUI
	 */
	public void setEnemyEditor(EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
		setActorEditor(mEnemyEditor);
	}

	@Override
	public void dispose() {
		mMovementTable.dispose();
		mAiTable.dispose();
		mWeaponTable.dispose();
		mPathTable.dispose();

		super.dispose();
	}

	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		mMainTable.setCellPaddingDefault(1,1,1,1);
		mMovementTable.setPreferences(mMainTable);
		mMovementTable.setName("temp");
		mWeaponTable.setPreferences(mMainTable);
		mAiTable.setPreferences(mMainTable);
		mPathTable.setPreferences(mPathTable);
		mVisualTable.setPreferences(mMainTable);
		mOptionTable.setPreferences(mMainTable);
		mCollisionTable.setPreferences(mMainTable);


		initMovement();
		initWeapon();
		initVisual("enemy", ActorShapeTypes.CIRCLE, ActorShapeTypes.RECTANGLE, ActorShapeTypes.TRIANGLE);
		initOptions("enemy");
		initCollision("enemy");
		initFileMenu("enemy");
		initMenu();

		resetValues();


		mMainTable.setTransform(true);
		mMovementTable.setTransform(true);
		mWeaponTable.setTransform(true);
		mAiTable.setTransform(true);
		mVisualTable.setTransform(true);
		mOptionTable.setTransform(true);
		mCollisionTable.setTransform(true);
		mMainTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mMainTable.invalidate();
	}

	@Override
	public void resetValues() {
		super.resetValues();

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
		mWidgets.weapon.damage.setValue(mEnemyEditor.getWeaponDamage());
		mWidgets.weapon.cooldownMax.setValue(mEnemyEditor.getCooldownMax());
		mWidgets.weapon.cooldownMin.setValue(mEnemyEditor.getCooldownMin());
		mWidgets.weapon.bulletName.setText(mEnemyEditor.getBulletName());
		mWidgets.weapon.bulletName.setSize(mWidgets.weapon.bulletName.getPrefWidth(), mWidgets.weapon.bulletName.getPrefHeight());

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
	}

	@Override
	protected String getResourceTypeName() {
		return "enemy";
	}

	/**
	 * Initializes the menu buttons
	 */
	private void initMenu() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);


		// --- Active options ---
		mMainTable.row().setAlign(Horizontal.RIGHT, Vertical.BOTTOM);
		ButtonGroup buttonGroup = new ButtonGroup();

		// Movement
		GuiCheckCommandCreator menuChecker = new GuiCheckCommandCreator(mInvoker);
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Movement", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mMovementHider.addToggleActor(mMovementTable);
		mMovementHider.setButton(button);
		new TooltipListener(button, "", Messages.Tooltip.Enemy.Menu.MOVEMENT);

		// Weapons
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Weapons", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mWeaponHider.addToggleActor(mWeaponTable);
		mWeaponHider.setButton(button);
		new TooltipListener(button, "", Messages.Tooltip.Enemy.Menu.WEAPON);

		// Visuals
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Visuals", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mVisualHider.setButton(button);
		mVisualHider.addToggleActor(mVisualTable);
		new TooltipListener(button, "Visuals", Messages.replaceName(Messages.Tooltip.Actor.Menu.VISUALS, "enemy"));

		// Options
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Options", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mOptionHider.setButton(button);
		mOptionHider.addToggleActor(mOptionTable);
		new TooltipListener(button, "Options", Messages.replaceName(Messages.Tooltip.Actor.Menu.OPTIONS, "enemy"));

		// Collision
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Collision", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mCollisionHider.setButton(button);
		mCollisionHider.addToggleActor(mCollisionTable);
		new TooltipListener(button, "Collision", Messages.replaceName(Messages.Tooltip.Actor.Menu.COLLISION, "enemy"));


		mMainTable.row().setFillHeight(true).setAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.add(mWeaponTable);
		mMainTable.add(mVisualTable);
		mMainTable.add(mMovementTable);
		mMainTable.add(mOptionTable).setFillWidth(true).setFillHeight(true);
		mMainTable.add(mCollisionTable);
	}

	/**
	 * Initializes the movement GUI part
	 */
	private void initMovement() {
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		CheckBoxStyle checkBoxStyle = generalSkin.get("default", CheckBoxStyle.class);
		LabelStyle labelStyle = generalSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = generalSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = generalSkin.get("default", TextFieldStyle.class);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		// Path
		mMovementTable.row();
		GuiCheckCommandCreator movementChecker = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		CheckBox checkBox = new CheckBox("Path", checkBoxStyle);
		checkBox.addListener(movementChecker);
		mWidgets.movement.pathBox = checkBox;
		TooltipListener tooltipListener = new TooltipListener(checkBox, "Path", Messages.Tooltip.Enemy.Movement.Menu.PATH);
		new ButtonListener(checkBox, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				addInnerTable(mPathTable, mMovementTypeTable);
				mEnemyEditor.setMovementType(MovementTypes.PATH);
			}
		};
		buttonGroup.add(checkBox);
		Cell cell = mMovementTable.add(checkBox);
		cell.setPadRight(10);


		// Stationary
		checkBox = new CheckBox("Stationary", checkBoxStyle);
		checkBox.addListener(movementChecker);
		mWidgets.movement.stationaryBox = checkBox;
		tooltipListener = new TooltipListener(checkBox, "Stationary", Messages.Tooltip.Enemy.Movement.Menu.STATIONARY);
		new ButtonListener(checkBox, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				addInnerTable(null, mMovementTypeTable);
				mEnemyEditor.setMovementType(MovementTypes.STATIONARY);
			}
		};
		buttonGroup.add(checkBox);
		cell = mMovementTable.add(checkBox);
		cell.setPadRight(10);


		// AI
		checkBox = new CheckBox("AI", checkBoxStyle);
		checkBox.addListener(movementChecker);
		mWidgets.movement.aiBox = checkBox;
		tooltipListener = new TooltipListener(checkBox, "AI", Messages.Tooltip.Enemy.Movement.Menu.AI);
		new ButtonListener(checkBox, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				addInnerTable(mPathTable, mMovementTypeTable);
				mMovementTypeTable.row();
				mMovementTypeTable.add(mAiTable);
				mAiTable.invalidate();
				mEnemyEditor.setMovementType(MovementTypes.AI);
			}
		};
		buttonGroup.add(checkBox);
		mMovementTable.add(checkBox);
		mMovementTable.row();
		mMovementTable.add(mMovementTypeTable);


		// --- MOVEMENT path/AI ---
		// Movement Speed
		mMovementTable.row().setScalable(false);
		Label label = new Label("Movement speed", labelStyle);
		new TooltipListener(label, "Movement speed", Messages.Tooltip.Enemy.Movement.Common.MOVEMENT_SPEED);
		mPathTable.add(label);
		mPathTable.row().setScalable(false);
		Slider slider = new Slider(Enemy.Movement.MOVE_SPEED_MIN, Enemy.Movement.MOVE_SPEED_MAX, Enemy.Movement.MOVE_SPEED_STEP_SIZE, false, sliderStyle);
		mWidgets.movement.speedSlider = slider;
		mPathTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mPathTable.add(textField);
		new TooltipListener(slider, "Movement speed", Messages.Tooltip.Enemy.Movement.Common.MOVEMENT_SPEED);
		new TooltipListener(textField, "Movement speed", Messages.Tooltip.Enemy.Movement.Common.MOVEMENT_SPEED);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setSpeed(newValue);
			}
		};

		// Turning
		mPathTable.row().setScalable(false);
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Turning speed OFF", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mWidgets.movement.turnSpeedToggleButton = button;
		mPathTable.add(button);
		new TooltipListener(button, "Turning speed", Messages.Tooltip.Enemy.Movement.Common.TURNING_SPEED_BUTTON);
		HideListener hideListener = new HideListener(button, true) {
			@Override
			protected void onShow() {
				if (mButton instanceof TextButton) {
					((TextButton) mButton).setText("Turning speed ON");
				}
				if (!mEnemyEditor.isTurning()) {
					mEnemyEditor.setTurning(true);
				}

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, true));
				}
			}

			@Override
			protected void onHide() {
				if (mButton instanceof TextButton) {
					((TextButton) mButton).setText("Turning speed OFF");
				}
				if (mEnemyEditor.isTurning()) {
					mEnemyEditor.setTurning(false);
				}

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, false));
				}
			}
		};

		mPathTable.row().setScalable(false);
		slider = new Slider(Movement.TURN_SPEED_MIN, Movement.TURN_SPEED_MAX, Enemy.Movement.TURN_SPEED_STEP_SIZE, false, sliderStyle);
		mWidgets.movement.turnSpeedSlider = slider;
		mPathTable.add(mWidgets.movement.turnSpeedSlider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mPathTable.add(textField);
		new TooltipListener(slider, "Turning speed", Messages.Tooltip.Enemy.Movement.Common.TURNING_SPEED);
		new TooltipListener(textField, "Turning speed", Messages.Tooltip.Enemy.Movement.Common.TURNING_SPEED);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setTurnSpeed(newValue);
			}
		};
		mMovementHider.addChild(hideListener);
		hideListener.addToggleActor(slider);
		hideListener.addToggleActor(textField);


		// --- Movement AI ---
		mAiTable.setScalable(false);
		mAiTable.row();
		label = new Label("Distance", labelStyle);

		mAiTable.add(label);

		mAiTable.row();
		label = new Label("Min", labelStyle);
		new TooltipListener(label, "Minimum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MIN);
		cell = mAiTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		mWidgets.movement.aiDistanceMin = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, sliderStyle);
		mAiTable.add(mWidgets.movement.aiDistanceMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		new TooltipListener(slider, "Minimum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MIN);
		new TooltipListener(textField, "Minimum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MIN);
		SliderListener sliderMinListener = new SliderListener(mWidgets.movement.aiDistanceMin, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMin(newValue);
			}
		};

		mAiTable.row();
		label = new Label("Max", labelStyle);
		new TooltipListener(label, "Maximum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MAX);
		cell = mAiTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		mWidgets.movement.aiDistanceMax = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, sliderStyle);
		mAiTable.add(mWidgets.movement.aiDistanceMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		new TooltipListener(slider, "Maximum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MAX);
		new TooltipListener(textField, "Maximum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MAX);
		SliderListener sliderMaxListener = new SliderListener(mWidgets.movement.aiDistanceMax, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMax(newValue);
			}
		};

		sliderMinListener.setGreaterSlider(mWidgets.movement.aiDistanceMax);
		sliderMaxListener.setLesserSlider(mWidgets.movement.aiDistanceMin);

		// Random movement
		mAiTable.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Random Movement OFF", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mWidgets.movement.aiRandomMovementToggleButton = button;
		mAiTable.add(button);
		new TooltipListener(button, "Random Movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT_BUTTON);
		hideListener = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setMoveRandomly(true);

				if (mButton instanceof TextButton) {
					((TextButton) mButton).setText("Random Movement ON");
				}

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, true));
				}
			}

			@Override
			protected void onHide() {
				mEnemyEditor.setMoveRandomly(false);

				if (mButton instanceof TextButton) {
					((TextButton) mButton).setText("Random Movement OFF");
				}

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, false));
				}
			}
		};
		mMovementHider.addChild(hideListener);

		label = new Label("Min", labelStyle);
		new TooltipListener(label, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		mWidgets.movement.aiRandomTimeMin = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, sliderStyle);
		mAiTable.add(mWidgets.movement.aiRandomTimeMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		new TooltipListener(slider, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		new TooltipListener(textField, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		sliderMinListener = new SliderListener(mWidgets.movement.aiRandomTimeMin, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMin(newValue);
			}
		};
		hideListener.addToggleActor(label);
		hideListener.addToggleActor(mWidgets.movement.aiRandomTimeMin);
		hideListener.addToggleActor(textField);


		label = new Label("Max", labelStyle);
		new TooltipListener(label, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		mAiTable.row();
		cell = mAiTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		mWidgets.movement.aiRandomTimeMax = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, sliderStyle);
		mAiTable.add(mWidgets.movement.aiRandomTimeMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mAiTable.add(textField);
		new TooltipListener(slider, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		new TooltipListener(textField, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		sliderMaxListener = new SliderListener(mWidgets.movement.aiRandomTimeMax, textField, mInvoker) {
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
		Skin generalSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textToggleStyle = generalSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textButtonStyle = generalSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = generalSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = generalSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = generalSkin.get("default", TextFieldStyle.class);
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);

		mWeaponTable.setScalable(false);

		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Weapons OFF", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		mWidgets.weapon.toggleButton = button;
		mWeaponTable.add(button);
		/** @todo only use hider */
		TooltipListener tooltipListener = new TooltipListener(button, "Weapon", Messages.Tooltip.Enemy.Weapon.WEAPON_BUTTON);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					if (mButton instanceof TextButton) {
						((TextButton) mButton).setText("Weapons ON");
					}
				} else {
					if (mButton instanceof TextButton) {
						((TextButton) mButton).setText("Weapons OFF");
					}
				}

				mEnemyEditor.setUseWeapon(checked);
			}
		};
		HideListener weaponInnerHider = new HideListener(button, true);

		// TYPES
		mWeaponTable.row();
		GuiCheckCommandCreator weaponMenuChecker = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Bullet", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(weaponMenuChecker);
		buttonGroup.add(button);
		mWeaponTable.add(button);
		weaponInnerHider.addToggleActor(button);
		new TooltipListener(button, "Bullet", Messages.Tooltip.Enemy.Weapon.BULLET);
		HideListener bulletHider = new HideListener(button, true);
		weaponInnerHider.addChild(bulletHider);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Aim", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(weaponMenuChecker);
		buttonGroup.add(button);
		mWeaponTable.add(button);
		weaponInnerHider.addToggleActor(button);
		new TooltipListener(button, "Aim", Messages.Tooltip.Enemy.Weapon.AIM);
		HideListener aimHider = new HideListener(button, true);
		weaponInnerHider.addChild(aimHider);

		// BULLET
		AlignTable bulletTable = new AlignTable();
		bulletTable.setPreferences(mWeaponTable);
		mWeaponTable.row();
		mWeaponTable.add(bulletTable);
		bulletHider.addToggleActor(bulletTable);

		// Select type
		Label label = new Label("", labelStyle);
		mWidgets.weapon.bulletName = label;
		bulletTable.add(label);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select bullet type", textButtonStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin);
		}
		bulletTable.add(button);
		tooltipListener = new TooltipListener(button, "Select bullet", Messages.Tooltip.Enemy.Weapon.Bullet.SELECT_BULLET);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEnemyEditor.selectBulletType();
			}
		};

		// Speed
		bulletTable.row();
		label = new Label("Speed", labelStyle);
		new TooltipListener(label, "Speed", Messages.Tooltip.Enemy.Weapon.Bullet.SPEED);
		Cell cell = bulletTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		Slider slider = new Slider(Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.bulletSpeed = slider;
		bulletTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		new TooltipListener(slider, "Speed", Messages.Tooltip.Enemy.Weapon.Bullet.SPEED);
		new TooltipListener(textField, "Speed", Messages.Tooltip.Enemy.Weapon.Bullet.SPEED);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setBulletSpeed(newValue);
			}
		};

		// Damage
		bulletTable.row();
		label = new Label("Damage", labelStyle);
		new TooltipListener(label, "Damage", Messages.Tooltip.Enemy.Weapon.Bullet.DAMAGE);
		cell = bulletTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		slider = new Slider(Weapon.DAMAGE_MIN, Weapon.DAMAGE_MAX, Weapon.DAMAGE_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.damage = slider;
		bulletTable.add(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		new TooltipListener(slider, "Damage", Messages.Tooltip.Enemy.Weapon.Bullet.DAMAGE);
		new TooltipListener(textField, "Damage", Messages.Tooltip.Enemy.Weapon.Bullet.DAMAGE);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setWeaponDamage(newValue);
			}
		};

		// Cooldown
		label = new Label("Cooldown time", labelStyle);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		bulletTable.row();
		bulletTable.add(label);
		label = new Label("Min", labelStyle);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		bulletTable.row();
		cell = bulletTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMin = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.cooldownMin = sliderMin;
		bulletTable.add(sliderMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		new TooltipListener(slider, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		new TooltipListener(textField, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		SliderListener sliderMinListener = new SliderListener(sliderMin, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setCooldownMin(newValue);
			}
		};


		label = new Label("Max", labelStyle);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		bulletTable.row();
		cell = bulletTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMax = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.cooldownMax = sliderMax;
		bulletTable.add(sliderMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		bulletTable.add(textField);
		new TooltipListener(slider, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		new TooltipListener(textField, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		SliderListener sliderMaxListener = new SliderListener(sliderMax, textField, mInvoker) {
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
		GuiCheckCommandCreator aimChecker = new GuiCheckCommandCreator(mInvoker);
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("On Player", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(aimChecker);
		mWidgets.weapon.aimOnPlayer = button;
		mWeaponTable.add(button);
		aimHider.addToggleActor(button);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "On player", Messages.Tooltip.Enemy.Weapon.Aim.ON_PLAYER);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mEnemyEditor.setAimType(AimTypes.ON_PLAYER);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move dir", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(aimChecker);
		mWidgets.weapon.aimMoveDirection = button;
		mWeaponTable.add(button);
		aimHider.addToggleActor(button);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Movement direction", Messages.Tooltip.Enemy.Weapon.Aim.MOVE_DIR);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mEnemyEditor.setAimType(AimTypes.MOVE_DIRECTION);
				}
			}
		};

		mWeaponTable.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("In front of player", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(aimChecker);
		mWidgets.weapon.aimInFrontOfPlayer = button;
		mWeaponTable.add(button);
		aimHider.addToggleActor(button);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "In front of player", Messages.Tooltip.Enemy.Weapon.Aim.IN_FRONT_OF_PLAYER);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mEnemyEditor.setAimType(AimTypes.IN_FRONT_OF_PLAYER);
				}
			}
		};

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Rotate", textToggleStyle);
		} else {
			/** @todo default stub image button */
			button = new ImageButton(editorSkin, "default-toggle");
		}
		button.addListener(aimChecker);
		mWidgets.weapon.aimRotate = button;
		mWeaponTable.add(button);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Rotate", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE);
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
		new TooltipListener(label, "Start angle", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_START_ANGLE);
		rotateTable.add(label);
		rotateTable.row();
		slider = new Slider(Enemy.Weapon.START_ANGLE_MIN, Enemy.Weapon.START_ANGLE_MAX, Enemy.Weapon.START_ANGLE_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.aimRotateStartAngle = slider;
		rotateTable.add(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		rotateTable.add(textField);
		new TooltipListener(slider, "Start angle", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_START_ANGLE);
		new TooltipListener(textField, "Start angle", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_START_ANGLE);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
			}
		};

		rotateTable.row();
		label = new Label("Rotate speed", labelStyle);
		new TooltipListener(label, "Rotation speed", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_SPEED);
		rotateTable.add(label);
		rotateTable.row();
		slider = new Slider(Enemy.Weapon.ROTATE_SPEED_MIN, Enemy.Weapon.ROTATE_SPEED_MAX, Enemy.Weapon.ROTATE_SPEED_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.aimRotateSpeed = slider;
		rotateTable.add(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		rotateTable.add(textField);
		new TooltipListener(slider, "Rotation speed", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_SPEED);
		new TooltipListener(textField, "Rotation speed", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_SPEED);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimRotateSpeed(newValue);
			}
		};


		weaponInnerHider.addToggleActor(bulletTable);
		mWeaponHider.addChild(weaponInnerHider);
	}

	// Tables
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

	// Hiders
	/** Hides weapon options */
	private HideListener mWeaponHider = new HideListener(true);
	/** Hides movement options */
	private HideListener mMovementHider = new HideListener(true);


	/** All widgets that needs updating when an actor is changed */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** The actual enemy editor bound to this gui */
	private EnemyEditor mEnemyEditor = null;

	/**
	 * All the widgets which state can be changed and thus reset
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		MovementWidgets movement = new MovementWidgets();
		WeaponWidgets weapon = new WeaponWidgets();

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


		static class WeaponWidgets {
			Button toggleButton = null;


			// Bullet
			Label bulletName = null;
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
	}
}
