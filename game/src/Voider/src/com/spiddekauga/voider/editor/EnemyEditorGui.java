package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
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
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.Config.Editor.Enemy.Movement;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.editor.commands.CGuiCheck;
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
		mAiTable.dispose();
		mWeaponTable.dispose();
		mPathTable.dispose();
		mPathLabels.clear();

		mMovementHider.dispose();
		mWeaponHider.dispose();

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
		mAiTable.setPreferences(mMainTable);
		mPathTable.setPreferences(mMainTable);
		mPathTableWrapper.setPreferences(mMainTable);
		mAiTableWrapper.setPreferences(mMainTable);
		mPathTableWrapper.setName("pathTableWrapper");
		mAiTableWrapper.setName("aiTableWrapper");

		initMenu();
		initWeapon();
		initCollision();
		initMovementMenu();
		initMovement();
		initPathLabels();

		resetValues();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		scalePathLabels();

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
		mWidgets.movement.turnSpeedSlider.setValue(mEnemyEditor.getTurnSpeed());
		if (mEnemyEditor.isTurning()) {
			mWidgets.movement.turnSpeedOn.setChecked(true);
		} else {
			mWidgets.movement.turnSpeedOff.setChecked(false);
		}

		// AI movement
		mWidgets.movement.aiDistanceMax.setValue(mEnemyEditor.getPlayerDistanceMax());
		mWidgets.movement.aiDistanceMin.setValue(mEnemyEditor.getPlayerDistanceMin());
		mWidgets.movement.aiRandomTimeMax.setValue(mEnemyEditor.getRandomTimeMax());
		mWidgets.movement.aiRandomTimeMin.setValue(mEnemyEditor.getRandomTimeMin());
		if (mEnemyEditor.isMovingRandomly()) {
			mWidgets.movement.aiRandomMovementOn.setChecked(true);
		} else {
			mWidgets.movement.aiRandomMovementOff.setChecked(true);
		}

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

	/**
	 * Initializes the menu buttons
	 */
	private void initMenu() {
		ButtonGroup buttonGroup = new ButtonGroup();

		// Movement
		GuiCheckCommandCreator menuChecker = new GuiCheckCommandCreator(mInvoker);
		Button button;
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("Movement", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.MOVEMENT.toString());
		//		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		addToEditorMenu(button);
		mMovementHider.addToggleActor(mMovementTable);
		mMovementHider.setButton(button);
		new TooltipListener(button, "", Messages.Tooltip.Enemy.Menu.MOVEMENT);

		// Weapons
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("Weapons", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.WEAPON.toString());
		//		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		addToEditorMenu(button);
		mWeaponHider.addToggleActor(mWeaponTable);
		mWeaponHider.setButton(button);
		new TooltipListener(button, "", Messages.Tooltip.Enemy.Menu.WEAPON);

		// Visuals
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("Visuals", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.VISUALS.toString());
		//		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		addToEditorMenu(button);
		mVisualHider.setButton(button);
		mVisualHider.addToggleActor(getVisualTable());
		new TooltipListener(button, "Visuals", Messages.replaceName(Messages.Tooltip.Actor.Menu.VISUALS, "enemy"));

		// Collision
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("Collision", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.COLLISION.toString());
		//		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		addToEditorMenu(button);
		mCollisionHider.setButton(button);
		mCollisionHider.addToggleActor(getCollisionTable());
		new TooltipListener(button, "Collision", Messages.replaceName(Messages.Tooltip.Actor.Menu.COLLISION, "enemy"));
	}

	/**
	 * Initializes movement path / AI
	 */
	private void initMovement() {
		// --- MOVEMENT path/AI ---
		// Movement Speed
		mMovementTable.row();
		Label label = new Label("Movement speed", mStyles.label.standard);
		new TooltipListener(label, "Movement speed", Messages.Tooltip.Enemy.Movement.Common.MOVEMENT_SPEED);
		mPathTable.add(label);
		mPathTable.row();
		Slider slider = new Slider(Enemy.Movement.MOVE_SPEED_MIN, Enemy.Movement.MOVE_SPEED_MAX, Enemy.Movement.MOVE_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.movement.speedSlider = slider;
		mPathTable.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
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
		mPathTable.row();
		label = new Label("Turning speed", mStyles.label.standard);
		mPathTable.add(label);

		// ON
		mPathTable.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button;
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("ON", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ON.toString());
		//		}
		buttonGroup.add(button);
		mWidgets.movement.turnSpeedOn = button;
		mPathTable.add(button);
		new TooltipListener(button, "Turning speed", Messages.Tooltip.Enemy.Movement.Common.TURNING_SPEED_BUTTON);
		HideListener hideListener = new HideListener(button, true) {
			@Override
			protected void onShow() {
				if (!mEnemyEditor.isTurning()) {
					mEnemyEditor.setTurning(true);
				}

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, true));
				}
			}

			@Override
			protected void onHide() {
				if (mEnemyEditor.isTurning()) {
					mEnemyEditor.setTurning(false);
				}

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, false));
				}
			}
		};
		mPathHider.addChild(hideListener);

		// OFF
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("OFF", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.OFF.toString());
		//		}
		buttonGroup.add(button);
		mWidgets.movement.turnSpeedOff = button;
		mPathTable.add(button);
		button.setChecked(true);

		// Turn speed
		mPathTable.row();
		slider = new Slider(Movement.TURN_SPEED_MIN, Movement.TURN_SPEED_MAX, Enemy.Movement.TURN_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.movement.turnSpeedSlider = slider;
		mPathTable.add(mWidgets.movement.turnSpeedSlider);
		textField = new TextField("", mStyles.textField.standard);
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
		label = new Label("Distance", mStyles.label.standard);
		mAiTable.add(label);

		// Min
		mAiTable.row();
		label = new Label("Min", mStyles.label.standard);
		new TooltipListener(label, "Minimum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MIN);
		mAiTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		mWidgets.movement.aiDistanceMin = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, mStyles.slider.standard);
		mAiTable.add(mWidgets.movement.aiDistanceMin);
		textField = new TextField("", mStyles.textField.standard);
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

		// Max
		mAiTable.row();
		label = new Label("Max", mStyles.label.standard);
		new TooltipListener(label, "Maximum distance", Messages.Tooltip.Enemy.Movement.Ai.DISTANCE_MAX);
		mAiTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		mWidgets.movement.aiDistanceMax = new Slider(Enemy.Movement.AI_DISTANCE_MIN, Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, false, mStyles.slider.standard);
		mAiTable.add(mWidgets.movement.aiDistanceMax);
		textField = new TextField("", mStyles.textField.standard);
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
		label = new Label("Random Movement", mStyles.label.standard);
		mAiTable.add(label);

		// ON
		mAiTable.row();
		buttonGroup = new ButtonGroup();
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("ON", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ON.toString());
		//		}
		buttonGroup.add(button);
		mWidgets.movement.aiRandomMovementOn = button;
		mAiTable.add(button);
		new TooltipListener(button, "Random Movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT_BUTTON);
		hideListener = new HideListener(button, true) {
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
		mMovementHider.addChild(hideListener);

		// OFF
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("OFF", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.OFF.toString());
		//		}
		buttonGroup.add(button);
		mWidgets.movement.aiRandomMovementOff = button;
		mAiTable.add(button);
		button.setChecked(true);

		// Min
		label = new Label("Min", mStyles.label.standard);
		new TooltipListener(label, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		mAiTable.row();
		mAiTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		mWidgets.movement.aiRandomTimeMin = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, mStyles.slider.standard);
		mAiTable.add(mWidgets.movement.aiRandomTimeMin);
		textField = new TextField("", mStyles.textField.standard);
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

		// Max
		label = new Label("Max", mStyles.label.standard);
		new TooltipListener(label, "Random movement", Messages.Tooltip.Enemy.Movement.Ai.RANDOM_MOVEMENT);
		mAiTable.row();
		mAiTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		mWidgets.movement.aiRandomTimeMax = new Slider(Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX, Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, false, mStyles.slider.standard);
		mAiTable.add(mWidgets.movement.aiRandomTimeMax);
		textField = new TextField("", mStyles.textField.standard);
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

		mPathTableWrapper.add(mPathTable);
		//		mAiTableWrapper.add(mPathTable);
		mAiTableWrapper.row();
		mAiTableWrapper.add(mAiTable);

		mMainTable.row();
		mMainTable.add(mPathTableWrapper);
		mMainTable.add(mAiTableWrapper);
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
		mPathHider.addToggleActor(mPathTableWrapper);
		mMovementHider.addChild(mPathHider);
		HideListener hideListener = new HideListener(checkBox, true);
		hideListener.addToggleActor(mPathLabels);
		TooltipListener tooltipListener = new TooltipListener(checkBox, "Path", Messages.Tooltip.Enemy.Movement.Menu.PATH);
		new ButtonListener(checkBox, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mEnemyEditor.setMovementType(MovementTypes.PATH);
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
				mEnemyEditor.setMovementType(MovementTypes.STATIONARY);
			}
		};
		buttonGroup.add(checkBox);
		mMovementTable.add(checkBox).setPadRight(10);


		// AI
		checkBox = new CheckBox("AI", mStyles.checkBox.radio);
		checkBox.addListener(movementChecker);
		mWidgets.movement.aiBox = checkBox;
		mAiHider.setButton(checkBox);
		mAiHider.addToggleActor(mAiTableWrapper);
		mMovementHider.addChild(mAiHider);
		tooltipListener = new TooltipListener(checkBox, "AI", Messages.Tooltip.Enemy.Movement.Menu.AI);
		new ButtonListener(checkBox, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mEnemyEditor.setMovementType(MovementTypes.AI);
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
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("ON", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ON.toString());
		//		}
		buttonGroup.add(button);
		mWidgets.weapon.on = button;
		mWeaponTable.add(button);
		/** @todo only use hider */
		TooltipListener tooltipListener = new TooltipListener(button, "Weapon", Messages.Tooltip.Enemy.Weapon.WEAPON_BUTTON);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mEnemyEditor.setUseWeapon(checked);
			}
		};
		HideListener weaponInnerHider = new HideListener(button, true);

		// OFF
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("OFF", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.OFF.toString());
		//		}
		buttonGroup.add(button);
		mWidgets.weapon.off = button;
		mWeaponTable.add(button);
		/** @todo only use hider */
		new TooltipListener(button, "Weapon", Messages.Tooltip.Enemy.Weapon.WEAPON_BUTTON);
		mWidgets.weapon.off.setChecked(true);


		// TYPES
		mWeaponTable.row();
		GuiCheckCommandCreator weaponMenuChecker = new GuiCheckCommandCreator(mInvoker);
		buttonGroup = new ButtonGroup();
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("Bullet", mStyles.textButton.toggle);
		//		} else {
		/** @todo default stub image button */
		//			button = new ImageButton(mStyles.skin.editor, "default-toggle");
		//		}
		button.addListener(weaponMenuChecker);
		buttonGroup.add(button);
		mWeaponTable.add(button);
		weaponInnerHider.addToggleActor(button);
		HideListener bulletHider = new HideListener(button, true);
		weaponInnerHider.addChild(bulletHider);

		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("Aim", mStyles.textButton.toggle);
		//		} else {
		/** @todo default stub image button */
		//			button = new ImageButton(mStyles.skin.editor, "default-toggle");
		//		}
		button.addListener(weaponMenuChecker);
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
		Label label = new Label("", mStyles.label.standard);
		mWidgets.weapon.bulletName = label;
		bulletTable.add(label);

		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Select bullet type", mStyles.textButton.standard);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.BULLET_SELECT.toString());
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
		label = new Label("Speed", mStyles.label.standard);
		new TooltipListener(label, "Speed", Messages.Tooltip.Enemy.Weapon.Bullet.SPEED);
		Cell cell = bulletTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		Slider slider = new Slider(Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.bulletSpeed = slider;
		bulletTable.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
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
		label = new Label("Damage", mStyles.label.standard);
		new TooltipListener(label, "Damage", Messages.Tooltip.Enemy.Weapon.Bullet.DAMAGE);
		cell = bulletTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		slider = new Slider(Weapon.DAMAGE_MIN, Weapon.DAMAGE_MAX, Weapon.DAMAGE_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.damage = slider;
		bulletTable.add(slider);
		textField = new TextField("", mStyles.textField.standard);
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
		label = new Label("Cooldown time", mStyles.label.standard);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		bulletTable.row();
		bulletTable.add(label);
		label = new Label("Min", mStyles.label.standard);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		bulletTable.row();
		cell = bulletTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMin = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.cooldownMin = sliderMin;
		bulletTable.add(sliderMin);
		textField = new TextField("", mStyles.textField.standard);
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


		label = new Label("Max", mStyles.label.standard);
		new TooltipListener(label, "Cooldown", Messages.Tooltip.Enemy.Weapon.Bullet.COOLDOWN);
		bulletTable.row();
		cell = bulletTable.add(label);
		cell.setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMax = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.cooldownMax = sliderMax;
		bulletTable.add(sliderMax);
		textField = new TextField("", mStyles.textField.standard);
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
		// On player
		mWeaponTable.row();
		GuiCheckCommandCreator aimChecker = new GuiCheckCommandCreator(mInvoker);
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("On Player", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.AIM_ON_PLAYER.toString());
		//		}
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

		// movement direction
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("Move dir", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.MOVEMENT.toString());
		//		}
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

		// In front of player
		mWeaponTable.row();
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("In front of player", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.AIM_IN_FRONT_PLAYER.toString());
		//		}
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

		// Rotate
		/** @todo set image button */
		//		if (Config.Gui.usesTextButtons()) {
		button = new TextButton("Rotate", mStyles.textButton.toggle);
		//		} else {
		//			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.AIM_ROTATE.toString());
		//		}
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
		label = new Label("Start angle", mStyles.label.standard);
		new TooltipListener(label, "Start angle", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_START_ANGLE);
		rotateTable.add(label);
		rotateTable.row();
		slider = new Slider(Enemy.Weapon.START_ANGLE_MIN, Enemy.Weapon.START_ANGLE_MAX, Enemy.Weapon.START_ANGLE_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.aimRotateStartAngle = slider;
		rotateTable.add(slider);
		textField = new TextField("", mStyles.textField.standard);
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
		label = new Label("Rotate speed", mStyles.label.standard);
		new TooltipListener(label, "Rotation speed", Messages.Tooltip.Enemy.Weapon.Aim.ROTATE_SPEED);
		rotateTable.add(label);
		rotateTable.row();
		slider = new Slider(Enemy.Weapon.ROTATE_SPEED_MIN, Enemy.Weapon.ROTATE_SPEED_MAX, Enemy.Weapon.ROTATE_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.aimRotateSpeed = slider;
		rotateTable.add(slider);
		textField = new TextField("", mStyles.textField.standard);
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

		mMainTable.add(mWeaponTable);
	}

	// Tables
	/** Container for all movement options */
	private AlignTable mMovementTable = new AlignTable();
	/** Container for all weapon options */
	private AlignTable mWeaponTable = new AlignTable();
	/** Table for Path movement */
	private AlignTable mPathTable = new AlignTable();
	/** Path table wrapper, needed for hiding */
	private AlignTable mPathTableWrapper = new AlignTable();
	/** Table for AI movement */
	private AlignTable mAiTable = new AlignTable();
	/** AI table wrapper, needed for hiding */
	private AlignTable mAiTableWrapper = new AlignTable();
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
			Slider speedSlider = null;
			Button turnSpeedOn = null;
			Button turnSpeedOff = null;
			Slider turnSpeedSlider = null;


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
