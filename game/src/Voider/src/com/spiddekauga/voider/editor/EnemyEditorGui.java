package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.SnapshotArray;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.Config.Editor.Enemy.Movement;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.editor.commands.CGuiCheck;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.resources.SkinNames;
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
		setEditor(enemyEditor);
	}

	@Override
	public void dispose() {
		mMovementTable.dispose();
		mWeaponTable.dispose();
		mPathLabels.clear();

		mMovementHider.dispose();
		mWeaponHider.dispose();
		mPathHider.dispose();
		mAiHider.dispose();

		super.dispose();
	}

	@Override
	public void initGui() {
		super.initGui();

		mMovementHider.setName("movementHider");
		mWeaponHider.setName("weaponHider");
		mAiHider.setName("aiHider");
		mPathHider.setName("pathHider");

		mMovementTable.setName("movementTable");
		mMovementTable.setPreferences(mMainTable);
		mWeaponTable.setPreferences(mMainTable);

		initWeapon();
		initCollision();
		initMovementMenu();
		initMovement();
		initPathLabels();

		resetValues();
	}

	/**
	 * Reset movement variables
	 */
	void resetMovementValues() {
		switch (mEnemyEditor.getMovementType()) {
		case PATH:
			mWidgets.movement.pathBox.setChecked(true);
			if (mEnemyEditor.isTurning()) {
				mWidgets.movement.pathTurnSpeedOn.setChecked(true);
			} else {
				mWidgets.movement.pathTurnSpeedOff.setChecked(true);
			}
			break;

		case STATIONARY:
			mWidgets.movement.stationaryBox.setChecked(true);
			break;

		case AI:
			mWidgets.movement.aiBox.setChecked(true);
			if (mEnemyEditor.isTurning()) {
				mWidgets.movement.aiTurnSpeedOn.setChecked(true);
			} else {
				mWidgets.movement.aiTurnSpeedOff.setChecked(true);
			}
			if (mEnemyEditor.isMovingRandomly()) {
				mWidgets.movement.aiRandomMovementOn.setChecked(true);
			} else {
				mWidgets.movement.aiRandomMovementOff.setChecked(true);
			}
			break;
		}

		mWidgets.movement.pathSpeedSlider.setValue(mEnemyEditor.getSpeed());
		mWidgets.movement.aiSpeedSlider.setValue(mEnemyEditor.getSpeed());
		mWidgets.movement.pathTurnSpeedSlider.setValue(mEnemyEditor.getTurnSpeed());
		mWidgets.movement.aiTurnSpeedSlider.setValue(mEnemyEditor.getTurnSpeed());


		// AI movement
		mWidgets.movement.aiDistanceMax.setValue(mEnemyEditor.getPlayerDistanceMax());
		mWidgets.movement.aiDistanceMin.setValue(mEnemyEditor.getPlayerDistanceMin());
		mWidgets.movement.aiRandomTimeMax.setValue(mEnemyEditor.getRandomTimeMax());
		mWidgets.movement.aiRandomTimeMin.setValue(mEnemyEditor.getRandomTimeMin());
	}

	@Override
	public void resetValues() {
		super.resetValues();

		scalePathLabels();

		resetMovementValues();

		// Weapons
		if (mEnemyEditor.hasWeapon()) {
			mWidgets.weapon.on.setChecked(true);
		} else {
			mWidgets.weapon.off.setChecked(true);
		}
		mWidgets.weapon.bulletSpeed.setValue(mEnemyEditor.getBulletSpeed());
		mWidgets.weapon.damage.setValue(mEnemyEditor.getWeaponDamage());
		mWidgets.weapon.cooldownMax.setValue(mEnemyEditor.getCooldownMax());
		mWidgets.weapon.cooldownMin.setValue(mEnemyEditor.getCooldownMin());

		if (mEnemyEditor.isWeaponBulletsSelected()) {
			mWidgets.weapon.selectBullet.setText("");
		} else {
			mWidgets.weapon.selectBullet.setText(Messages.Enemy.SELECT_BULLET);
		}
		mWidgets.weapon.selectBullet.pack();
		//		mWidgets.weapon.selectBullet.setSize(mWidgets.weapon.selectBullet.getPrefWidth(), mWidgets.weapon.selectBullet.getPrefHeight());

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

	/**
	 * Initializes the path labels
	 */
	private void initPathLabels() {
		Label label = new Label("Back and Forth", mStyles.label.standard);
		Table wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		label = new Label("Loop", mStyles.label.standard);
		wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		label = new Label("Once", mStyles.label.standard);
		wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		getStage().addActor(mPathLabels);
	}

	/**
	 * Scale path labels
	 */
	void scalePathLabels() {
		float spaceBetween = Gdx.graphics.getHeight() * 0.1f;
		float height = Gdx.graphics.getHeight() * 0.2f;
		float initialOffset = spaceBetween + height * 0.5f + spaceBetween + height;

		mPathLabels.setPosition(Gdx.graphics.getWidth() / 3f, initialOffset);


		// Fix padding
		SnapshotArray<com.badlogic.gdx.scenes.scene2d.Actor> actors = mPathLabels.getChildren();
		// Reset padding first
		for (int i = 0; i < actors.size - 1; ++i) {
			if (actors.get(i) instanceof Table) {
				Table table = (Table) actors.get(i);
				table.padBottom(0);
				table.invalidateHierarchy();
			}
		}

		for (int i = 0; i < actors.size - 1; ++i) {
			if (actors.get(i) instanceof Table) {
				Table table = (Table) actors.get(i);
				table.padBottom(spaceBetween + height - table.getPrefHeight());
				table.invalidateHierarchy();
			}
		}
	}

	@Override
	protected String getResourceTypeName() {
		return "enemy";
	}

	@Override
	protected void initSettingsMenu() {
		super.initSettingsMenu();

		ButtonGroup buttonGroup = new ButtonGroup();

		// Movement
		GuiCheckCommandCreator menuChecker = new GuiCheckCommandCreator(mInvoker);
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Movement", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.MOVEMENT.toString());
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mMovementHider.addToggleActor(mMovementTable);
		mMovementHider.setButton(button);
		new TooltipListener(button, "", Messages.Tooltip.Enemy.Menu.MOVEMENT);

		// Weapons
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Weapons", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.WEAPON.toString());
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mWeaponHider.addToggleActor(mWeaponTable);
		mWeaponHider.setButton(button);
		new TooltipListener(button, "", Messages.Tooltip.Enemy.Menu.WEAPON);

		// Visuals
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Visuals", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.VISUALS.toString());
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mVisualHider.setButton(button);
		mVisualHider.addToggleActor(getVisualTable());
		new TooltipListener(button, "Visuals", Messages.replaceName(Messages.Tooltip.Actor.Menu.VISUALS, "enemy"));

		// Collision
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Collision", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.COLLISION.toString());
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		mMainTable.add(button);
		mCollisionHider.setButton(button);
		mCollisionHider.addToggleActor(getCollisionTable());
		new TooltipListener(button, "Collision", Messages.replaceName(Messages.Tooltip.Actor.Menu.COLLISION, "enemy"));

		mMainTable.row();
	}

	/**
	 * Initializes movement path / AI
	 */
	private void initMovement() {
		createMovementUi(MovementTypes.PATH);
		initMovementAi();
	}

	/**
	 * Initializes movement AI
	 */
	private void initMovementAi() {
		createMovementUi(MovementTypes.AI);

		mMainTable.row();
		Label label = new Label("Distance", mStyles.label.standard);
		mMainTable.add(label);
		mAiHider.addToggleActor(label);

		// Min
		mMainTable.row();
		label = new Label("Min", mStyles.label.standard);
		mAiHider.addToggleActor(label);
		new TooltipListener(label, "Minimum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MIN);
		mMainTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		Slider slider = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, mStyles.slider.standard);
		mAiHider.addToggleActor(slider);
		mWidgets.movement.aiDistanceMin = slider;
		mMainTable.add(mWidgets.movement.aiDistanceMin);
		TextField textField = new TextField("", mStyles.textField.standard);
		mAiHider.addToggleActor(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mMainTable.add(textField);
		new TooltipListener(slider, "Minimum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MIN);
		new TooltipListener(textField, "Minimum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MIN);
		SliderListener sliderMinListener = new SliderListener(mWidgets.movement.aiDistanceMin, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMin(newValue);
			}
		};

		// Max
		mMainTable.row();
		label = new Label("Max", mStyles.label.standard);
		mAiHider.addToggleActor(label);
		new TooltipListener(label, "Maximum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MAX);
		mMainTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		slider = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, mStyles.slider.standard);
		mAiHider.addToggleActor(slider);
		mWidgets.movement.aiDistanceMax = slider;
		mMainTable.add(mWidgets.movement.aiDistanceMax);
		textField = new TextField("", mStyles.textField.standard);
		mAiHider.addToggleActor(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mMainTable.add(textField);
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
		mMainTable.row();
		label = new Label("Random Movement", mStyles.label.standard);
		mAiHider.addToggleActor(label);
		mMainTable.add(label);

		// ON
		mMainTable.row();
		Button button;
		ButtonGroup buttonGroup = new ButtonGroup();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("ON", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ON.toString());
		}
		mAiHider.addToggleActor(button);
		buttonGroup.add(button);
		mWidgets.movement.aiRandomMovementOn = button;
		mMainTable.add(button);
		new TooltipListener(button, "Random Movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT_BUTTON);
		HideListener hideListener = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setMoveRandomly(true);

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, true));
				}
			}

			@Override
			protected void onHide() {
				mEnemyEditor.setMoveRandomly(false);

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, false));
				}
			}
		};
		mAiHider.addChild(hideListener);

		// OFF
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("OFF", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.OFF.toString());
		}
		mAiHider.addToggleActor(button);
		buttonGroup.add(button);
		mWidgets.movement.aiRandomMovementOff = button;
		mMainTable.add(button);
		button.setChecked(true);

		// Min
		label = new Label("Min", mStyles.label.standard);
		new TooltipListener(label, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		mMainTable.row();
		mMainTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);

		slider = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.movement.aiRandomTimeMin = slider;
		mMainTable.add(mWidgets.movement.aiRandomTimeMin);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mMainTable.add(textField);
		new TooltipListener(slider, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		new TooltipListener(textField, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		sliderMinListener = new SliderListener(mWidgets.movement.aiRandomTimeMin, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMin(newValue);
			}
		};
		hideListener.addToggleActor(label);
		hideListener.addToggleActor(slider);
		hideListener.addToggleActor(textField);

		// Max
		label = new Label("Max", mStyles.label.standard);
		new TooltipListener(label, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		mMainTable.row();
		mMainTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);

		slider = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.movement.aiRandomTimeMax = slider;
		mMainTable.add(mWidgets.movement.aiRandomTimeMax);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mMainTable.add(textField);
		new TooltipListener(slider, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		new TooltipListener(textField, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		sliderMaxListener = new SliderListener(mWidgets.movement.aiRandomTimeMax, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMax(newValue);
			}
		};
		hideListener.addToggleActor(label);
		hideListener.addToggleActor(slider);
		hideListener.addToggleActor(textField);

		sliderMinListener.setGreaterSlider(mWidgets.movement.aiRandomTimeMax);
		sliderMaxListener.setLesserSlider(mWidgets.movement.aiRandomTimeMin);
	}

	/**
	 * Initializes standard movement variables such as speed and turning for the specified table
	 * @param movementType which table to add the movement UI elements to
	 */
	private void createMovementUi(final MovementTypes movementType) {
		// Movement Speed
		mMainTable.row();
		Label label = new Label("Movement speed", mStyles.label.standard);
		new TooltipListener(label, "Movement speed", Messages.Tooltip.Enemy.Movement.Common.MOVEMENT_SPEED);
		mMainTable.add(label);
		mMainTable.row();
		Slider slider = new Slider(Enemy.Movement.MOVE_SPEED_MIN, Enemy.Movement.MOVE_SPEED_MAX, Enemy.Movement.MOVE_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mMainTable.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mMainTable.add(textField);
		new TooltipListener(slider, "Movement speed", Messages.Tooltip.Enemy.Movement.Common.MOVEMENT_SPEED);
		new TooltipListener(textField, "Movement speed", Messages.Tooltip.Enemy.Movement.Common.MOVEMENT_SPEED);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setSpeed(newValue);
				mWidgets.movement.pathSpeedSlider.setValue(newValue);
				mWidgets.movement.aiSpeedSlider.setValue(newValue);
			}
		};

		if (movementType == MovementTypes.PATH) {
			mPathHider.addToggleActor(label);
			mPathHider.addToggleActor(slider);
			mPathHider.addToggleActor(textField);
			mWidgets.movement.pathSpeedSlider = slider;
		} else if (movementType == MovementTypes.AI) {
			mAiHider.addToggleActor(label);
			mAiHider.addToggleActor(slider);
			mAiHider.addToggleActor(textField);
			mWidgets.movement.aiSpeedSlider = slider;
		}

		// Turning
		mMainTable.row();
		label = new Label("Turning speed", mStyles.label.standard);
		mMainTable.add(label);

		// ON
		mMainTable.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("ON", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ON.toString());
		}

		if (movementType == MovementTypes.PATH) {
			mPathHider.addToggleActor(label);
			mPathHider.addToggleActor(button);
			mWidgets.movement.pathTurnSpeedOn = button;
		} else if (movementType == MovementTypes.AI) {
			mAiHider.addToggleActor(label);
			mAiHider.addToggleActor(button);
			mWidgets.movement.aiTurnSpeedOn = button;
		}

		buttonGroup.add(button);
		mMainTable.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, "Turning speed", Messages.Tooltip.Enemy.Movement.Common.TURNING_SPEED_BUTTON);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mEnemyEditor.setTurning(checked);

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, checked));
				}
			}
		};
		HideListener hideListener = new HideListener(button, true);

		// OFF
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("OFF", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.OFF.toString());
		}
		buttonGroup.add(button);
		mMainTable.add(button);
		button.setChecked(true);

		// Turn speed
		mMainTable.row();
		slider = new Slider(Movement.TURN_SPEED_MIN, Movement.TURN_SPEED_MAX, Enemy.Movement.TURN_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mMainTable.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mMainTable.add(textField);
		new TooltipListener(slider, "Turning speed", Messages.Tooltip.Enemy.Movement.Common.TURNING_SPEED);
		new TooltipListener(textField, "Turning speed", Messages.Tooltip.Enemy.Movement.Common.TURNING_SPEED);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setTurnSpeed(newValue);
				mWidgets.movement.pathTurnSpeedSlider.setValue(newValue);
				mWidgets.movement.aiTurnSpeedSlider.setValue(newValue);
			}
		};
		hideListener.addToggleActor(slider);
		hideListener.addToggleActor(textField);

		if (movementType == MovementTypes.PATH) {
			mPathHider.addChild(hideListener);
			mPathHider.addToggleActor(button);
			mWidgets.movement.pathTurnSpeedOff = button;
			mWidgets.movement.pathTurnSpeedSlider = slider;
		} else if (movementType == MovementTypes.AI) {
			mAiHider.addChild(hideListener);
			mAiHider.addToggleActor(button);
			mWidgets.movement.aiTurnSpeedOff = button;
			mWidgets.movement.aiTurnSpeedSlider = slider;
		}
	}

	/**
	 * Initializes the movement GUI part
	 */
	private void initMovementMenu() {
		// Path
		mMovementTable.row().setAlign(Horizontal.RIGHT, Vertical.TOP);
		GuiCheckCommandCreator movementChecker = new GuiCheckCommandCreator(mInvoker);
		ButtonGroup buttonGroup = new ButtonGroup();
		CheckBox checkBox = new CheckBox("Path", mStyles.checkBox.radio);
		checkBox.addListener(movementChecker);
		mWidgets.movement.pathBox = checkBox;
		mPathHider.setButton(checkBox);
		mMovementHider.addChild(mPathHider);
		HideListener hideListener = new HideListener(checkBox, true);
		hideListener.addToggleActor(mPathLabels);
		TooltipListener tooltipListener = new TooltipListener(checkBox, "Path", Messages.Tooltip.Enemy.Movement.Menu.PATH);
		new ButtonListener(checkBox, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mEnemyEditor.setMovementType(MovementTypes.PATH);

					if (mWidgets.movement.pathTurnSpeedOn != null && mWidgets.movement.pathTurnSpeedOff != null) {
						if (mEnemyEditor.isTurning()) {
							mWidgets.movement.pathTurnSpeedOn.setChecked(true);
						} else {
							mWidgets.movement.pathTurnSpeedOff.setChecked(true);
						}
					}
				}
			}
		};
		buttonGroup.add(checkBox);
		mMovementTable.add(checkBox).setPadRight(10);


		// Stationary
		checkBox = new CheckBox("Stationary", mStyles.checkBox.radio);
		checkBox.addListener(movementChecker);
		mWidgets.movement.stationaryBox = checkBox;
		tooltipListener = new TooltipListener(checkBox, "Stationary", Messages.Tooltip.Enemy.Movement.Menu.STATIONARY);
		new ButtonListener(checkBox, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mEnemyEditor.setMovementType(MovementTypes.STATIONARY);
				}
			}
		};
		buttonGroup.add(checkBox);
		mMovementTable.add(checkBox).setPadRight(10);


		// AI
		checkBox = new CheckBox("AI", mStyles.checkBox.radio);
		checkBox.addListener(movementChecker);
		mWidgets.movement.aiBox = checkBox;
		mAiHider.setButton(checkBox);
		mMovementHider.addChild(mAiHider);
		tooltipListener = new TooltipListener(checkBox, "AI", Messages.Tooltip.Enemy.Movement.Menu.AI);
		new ButtonListener(checkBox, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mEnemyEditor.setMovementType(MovementTypes.AI);

					if (mWidgets.movement.aiTurnSpeedOn != null && mWidgets.movement.aiTurnSpeedOff != null) {
						if (mEnemyEditor.isTurning()) {
							mWidgets.movement.aiTurnSpeedOn.setChecked(true);
						} else {
							mWidgets.movement.aiTurnSpeedOff.setChecked(true);
						}
					}
				}
			}
		};
		buttonGroup.add(checkBox);
		mMovementTable.add(checkBox);

		mMainTable.add(mMovementTable);
	}

	/**
	 * Initializes the weapon GUI part
	 */
	private void initWeapon() {
		// ON
		Button button;
		ButtonGroup buttonGroup = new ButtonGroup();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("ON", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ON.toString());
		}
		buttonGroup.add(button);
		mWidgets.weapon.on = button;
		mWeaponTable.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, "Weapon", Messages.Tooltip.Enemy.Weapon.WEAPON_BUTTON);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mEnemyEditor.setUseWeapon(checked);
			}
		};

		// OFF
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("OFF", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.OFF.toString());
		}
		buttonGroup.add(button);
		mWidgets.weapon.off = button;
		mWeaponTable.add(button);
		new TooltipListener(button, "Weapon", Messages.Tooltip.Enemy.Weapon.WEAPON_BUTTON);
		mWidgets.weapon.off.setChecked(true);

		// BULLET
		mWeaponTable.row();

		// Select type
		Label label = new Label(Messages.Enemy.SELECT_BULLET, mStyles.label.highlight);
		mWidgets.weapon.selectBullet = label;
		mWeaponTable.add(label).setAlign(Horizontal.RIGHT, Vertical.MIDDLE);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select bullet type", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.BULLET_SELECT.toString());
		}
		mWeaponTable.add(button);
		tooltipListener = new TooltipListener(button, "Select bullet", Messages.Tooltip.Enemy.Weapon.Bullet.SELECT_BULLET);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEnemyEditor.selectBulletType();
			}
		};

		// Speed
		mWeaponTable.row();
		label = new Label("Speed", mStyles.label.standard);
		new TooltipListener(label, "Speed", Messages.Tooltip.Enemy.Weapon.Bullet.SPEED);
		Cell cell = mWeaponTable.add(label);
		cell.setPadRight(mStyles.vars.paddingAfterLabel);
		Slider slider = new Slider(Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.bulletSpeed = slider;
		mWeaponTable.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWeaponTable.add(textField);
		new TooltipListener(slider, "Speed", Messages.Tooltip.Enemy.Weapon.Bullet.SPEED);
		new TooltipListener(textField, "Speed", Messages.Tooltip.Enemy.Weapon.Bullet.SPEED);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setBulletSpeed(newValue);
			}
		};

		// Damage
		mWeaponTable.row();
		label = new Label("Damage", mStyles.label.standard);
		new TooltipListener(label, "Damage", Messages.Tooltip.Enemy.Weapon.Bullet.DAMAGE);
		cell = mWeaponTable.add(label);
		cell.setPadRight(mStyles.vars.paddingAfterLabel);
		slider = new Slider(Weapon.DAMAGE_MIN, Weapon.DAMAGE_MAX, Weapon.DAMAGE_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.damage = slider;
		mWeaponTable.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWeaponTable.add(textField);
		new TooltipListener(slider, "Damage", Messages.Tooltip.Enemy.Weapon.Bullet.DAMAGE);
		new TooltipListener(textField, "Damage", Messages.Tooltip.Enemy.Weapon.Bullet.DAMAGE);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setWeaponDamage(newValue);
			}
		};

		// Cooldown
		label = new Label("Cooldown time", mStyles.label.standard);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		mWeaponTable.row();
		mWeaponTable.add(label);
		label = new Label("Min", mStyles.label.standard);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		mWeaponTable.row();
		cell = mWeaponTable.add(label);
		cell.setPadRight(mStyles.vars.paddingAfterLabel);

		Slider sliderMin = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.cooldownMin = sliderMin;
		mWeaponTable.add(sliderMin);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWeaponTable.add(textField);
		new TooltipListener(slider, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		new TooltipListener(textField, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		SliderListener sliderMinListener = new SliderListener(sliderMin, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setCooldownMin(newValue);
			}
		};


		label = new Label("Max", mStyles.label.standard);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		mWeaponTable.row();
		cell = mWeaponTable.add(label);
		cell.setPadRight(mStyles.vars.paddingAfterLabel);

		Slider sliderMax = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.cooldownMax = sliderMax;
		mWeaponTable.add(sliderMax);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWeaponTable.add(textField);
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
		// On player
		mWeaponTable.row();
		GuiCheckCommandCreator aimChecker = new GuiCheckCommandCreator(mInvoker);
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("On Player", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.AIM_ON_PLAYER.toString());
		}
		button.addListener(aimChecker);
		mWidgets.weapon.aimOnPlayer = button;
		mWeaponTable.add(button);
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

		// In front of player
		if (Config.Gui.usesTextButtons()) {
			mWeaponTable.row();
			button = new TextButton("In front of player", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.AIM_IN_FRONT_PLAYER.toString());
		}
		button.addListener(aimChecker);
		mWidgets.weapon.aimInFrontOfPlayer = button;
		mWeaponTable.add(button);
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

		// Movement direction
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move dir", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.AIM_MOVEMENT.toString());
		}
		button.addListener(aimChecker);
		mWidgets.weapon.aimMoveDirection = button;
		mWeaponTable.add(button);
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

		// Direction
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Direction", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.AIM_DIRECTION.toString());
		}
		button.addListener(aimChecker);
		mWidgets.weapon.aimDirection = button;
		mWeaponTable.add(button);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Direction", Messages.Tooltip.Enemy.Weapon.Aim.DIRECTION);
		HideListener directionHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setAimType(AimTypes.DIRECTION);
			}
		};

		// Rotate
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Rotate", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.AIM_ROTATE.toString());
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


		// Direction options
		mWeaponTable.row();
		AlignTable directionTable = new AlignTable();
		directionTable.setPreferences(mWeaponTable);
		directionHider.addToggleActor(directionTable);
		mWeaponTable.add(directionTable);

		label = new Label("Angle", mStyles.label.standard);
		new TooltipListener(label, "Angle", Messages.Tooltip.Enemy.Weapon.Aim.DIRECTION_ANGLE);
		directionTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		slider = new Slider(Enemy.Weapon.START_ANGLE_MIN, Enemy.Weapon.START_ANGLE_MAX, Enemy.Weapon.START_ANGLE_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.aimDirectionAngle = slider;
		directionTable.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		directionTable.add(textField);
		new TooltipListener(slider, "Start angle", Messages.Tooltip.Enemy.Weapon.Aim.DIRECTION_ANGLE);
		new TooltipListener(textField, "Start angle", Messages.Tooltip.Enemy.Weapon.Aim.DIRECTION_ANGLE);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
				mWidgets.weapon.aimRotateStartAngle.setValue(newValue);
			}
		};


		// Rotate options
		mWeaponTable.row();
		AlignTable rotateTable = new AlignTable();
		rotateTable.setPreferences(mWeaponTable);
		rotateHider.addToggleActor(rotateTable);
		mWeaponTable.add(rotateTable);

		rotateTable.row();
		label = new Label("Angle", mStyles.label.standard);
		new TooltipListener(label, "Start angle", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_START_ANGLE);
		rotateTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		slider = new Slider(Enemy.Weapon.START_ANGLE_MIN, Enemy.Weapon.START_ANGLE_MAX, Enemy.Weapon.START_ANGLE_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.aimRotateStartAngle = slider;
		rotateTable.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		rotateTable.add(textField);
		new TooltipListener(slider, "Angle", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_START_ANGLE);
		new TooltipListener(textField, "Angle", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_START_ANGLE);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
				mWidgets.weapon.aimDirectionAngle.setValue(newValue);
			}
		};

		rotateTable.row();
		label = new Label("R-Speed", mStyles.label.standard);
		new TooltipListener(label, "Rotation speed", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_SPEED);
		rotateTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		slider = new Slider(Enemy.Weapon.ROTATE_SPEED_MIN, Enemy.Weapon.ROTATE_SPEED_MAX, Enemy.Weapon.ROTATE_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.aimRotateSpeed = slider;
		rotateTable.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		rotateTable.add(textField);
		new TooltipListener(slider, "Rotation speed", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_SPEED);
		new TooltipListener(textField, "Rotation speed", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_SPEED);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimRotateSpeed(newValue);
			}
		};


		// Create hider for all actors inside weapon table except on/off
		HideListener innerWeaponHider = new HideListener(mWidgets.weapon.on, true);

		innerWeaponHider.addChild(rotateHider);
		innerWeaponHider.addChild(directionHider);

		ArrayList<Actor> actors = mWeaponTable.getActors(false);
		for (Actor actor : actors) {
			if (actor != mWidgets.weapon.on && actor != mWidgets.weapon.off) {
				innerWeaponHider.addToggleActor(actor);
			}
		}


		mMainTable.add(mWeaponTable);
	}

	// Tables
	/** Container for all movement options */
	private AlignTable mMovementTable = new AlignTable();
	/** Container for all weapon options */
	private AlignTable mWeaponTable = new AlignTable();
	/** Table for path labels */
	private Table mPathLabels = new Table();

	// Hiders
	/** Hides weapon options */
	private HideListener mWeaponHider = new HideListener(true);
	/** Hides movement options */
	private HideListener mMovementHider = new HideListener(true);
	/** Hides path table */
	private HideListener mPathHider = new HideListener(true);
	/** Hides ai table */
	private HideListener mAiHider = new HideListener(true);


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
			Slider pathSpeedSlider = null;
			Button pathTurnSpeedOn = null;
			Button pathTurnSpeedOff = null;
			Slider pathTurnSpeedSlider = null;
			Slider aiSpeedSlider = null;
			Button aiTurnSpeedOn = null;
			Button aiTurnSpeedOff = null;
			Slider aiTurnSpeedSlider = null;


			// AI Movement
			Slider aiDistanceMin = null;
			Slider aiDistanceMax = null;
			Button aiRandomMovementOn = null;
			Button aiRandomMovementOff = null;
			Slider aiRandomTimeMin = null;
			Slider aiRandomTimeMax = null;
		}


		static class WeaponWidgets {
			Button on = null;
			Button off = null;

			// Bullet
			Label selectBullet = null;
			Slider bulletSpeed = null;
			Slider damage = null;
			Slider cooldownMin = null;
			Slider cooldownMax = null;


			// Aim
			Button aimOnPlayer = null;
			Button aimMoveDirection = null;
			Button aimDirection = null;
			Button aimInFrontOfPlayer = null;
			Button aimRotate = null;

			// Aim - rotate
			Slider aimDirectionAngle = null;
			Slider aimRotateStartAngle = null;
			Slider aimRotateSpeed = null;
		}
	}
}
