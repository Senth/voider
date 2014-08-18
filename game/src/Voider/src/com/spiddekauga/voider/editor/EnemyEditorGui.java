package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.SnapshotArray;
import com.spiddekauga.utils.commands.CGuiCheck;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.utils.scene.ui.UiFactory.SliderMinMaxWrapper;
import com.spiddekauga.utils.scene.ui.UiFactory.TabImageWrapper;
import com.spiddekauga.utils.scene.ui.UiFactory.TabRadioWrapper;
import com.spiddekauga.utils.scene.ui.UiFactory.TabWrapper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.Config.Editor.Enemy.Movement;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * GUI for the enemy editor
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

		mMovementTable.setName("movementTable");
		mMovementTable.setPreferences(getVisualTable());
		mWeaponTable.setPreferences(getVisualTable());

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

		if (mEnemyEditor.isTurning()) {
			mWidgets.movement.pathTurnSpeedOn.setChecked(true);
			mWidgets.movement.aiTurnSpeedOn.setChecked(true);
		} else {
			mWidgets.movement.pathTurnSpeedOff.setChecked(true);
			mWidgets.movement.aiTurnSpeedOff.setChecked(true);
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

		// TODO set bullet image

		// Aim
		mWidgets.weapon.aimRotateSpeed.setValue(mEnemyEditor.getAimRotateSpeed());
		mWidgets.weapon.aimRotateStartAngle.setValue(mEnemyEditor.getAimStartAngle());
		mWidgets.weapon.aimDirectionAngle.setValue(mEnemyEditor.getAimStartAngle());
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

		case DIRECTION:
			mWidgets.weapon.aimDirection.setChecked(true);
			break;
		}
	}

	/**
	 * Initializes the path labels
	 */
	private void initPathLabels() {
		Label label = new Label("Back and Forth", mUiFactory.getStyles().label.standard);
		Table wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		label = new Label("Loop", mUiFactory.getStyles().label.standard);
		wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		label = new Label("Once", mUiFactory.getStyles().label.standard);
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

		// Movement
		ImageButtonStyle buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.MOVEMENT);
		Button button = mSettingTabs.addTab(buttonStyle, mMovementTable, mMovementHider);
		mTooltip.add(button, Messages.EditorTooltips.TAB_MOVEMENT);

		// Weapons
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.WEAPON);
		button = mSettingTabs.addTab(buttonStyle, mWeaponTable, mWeaponHider);
		mTooltip.add(button, Messages.EditorTooltips.TAB_WEAPON);

		// Visual
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.VISUALS);
		button = mSettingTabs.addTab(buttonStyle, getVisualTable(), getVisualHider());
		mTooltip.add(button, Messages.EditorTooltips.TAB_VISUAL);

		// Collision
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.COLLISION);
		button = mSettingTabs.addTab(buttonStyle, getCollisionTable());
		mTooltip.add(button, Messages.EditorTooltips.TAB_COLLISION);
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

		AlignTable table = mMovementTable;
		GuiHider hider = mAiHider;


		// Distance from player
		SliderListener minSliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMin(newValue);
			}
		};
		SliderListener maxSliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMax(newValue);
			}
		};
		SliderMinMaxWrapper sliders = mUiFactory.addSliderMinMax("Distance From Player", Enemy.Movement.AI_DISTANCE_MIN,
				Enemy.Movement.AI_DISTANCE_MAX, Enemy.Movement.AI_DISTANCE_STEP_SIZE, minSliderListener, maxSliderListener, table, hider,
				mDisabledWhenPublished, mInvoker);

		// Set sliders
		mWidgets.movement.aiDistanceMin = sliders.min;
		mWidgets.movement.aiDistanceMax = sliders.max;


		// Random movement
		mUiFactory.addPanelSection("Random Movement", table, hider);

		// ON/OFF tabs
		// ON
		TabImageWrapper onTab = mUiFactory.createTabImageWrapper();
		onTab.imageName = SkinNames.EditorIcons.ON;
		onTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				onChecked(true);
			}

			@Override
			protected void onHide() {
				onChecked(false);
			}

			/**
			 * Called when either checked or unchecked
			 * @param checked if the button is checked
			 */
			private void onChecked(boolean checked) {
				mEnemyEditor.setMoveRandomly(checked);

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, checked));
				}
			}
		};

		// OFF
		TabImageWrapper offTab = mUiFactory.createTabImageWrapper();
		offTab.imageName = SkinNames.EditorIcons.OFF;

		// Create tabs
		@SuppressWarnings("unchecked")
		ArrayList<TabWrapper> tabs = Pools.arrayList.obtain();
		tabs.add(onTab);
		tabs.add(offTab);
		mUiFactory.addTabs(table, hider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);
		tabs = null;

		// Set buttons
		mWidgets.movement.aiRandomMovementOn = onTab.button;
		mWidgets.movement.aiRandomMovementOff = offTab.button;

		// Sliders
		@SuppressWarnings("unchecked")
		ArrayList<Actor> createdActors = Pools.arrayList.obtain();
		minSliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMin(newValue);
			}
		};
		maxSliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMax(newValue);
			}
		};
		sliders = mUiFactory.addSliderMinMax(null, Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN, Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX,
				Enemy.Movement.RANDOM_MOVEMENT_TIME_STEP_SIZE, minSliderListener, maxSliderListener, table, onTab.hider, createdActors, mInvoker);
		mTooltip.add(createdActors, Messages.EditorTooltips.MOVEMENT_AI_RANDOM_COOLDOWN);
		mDisabledWhenPublished.addAll(createdActors);

		// Set sliders
		mWidgets.movement.aiRandomTimeMin = sliders.min;
		mWidgets.movement.aiRandomTimeMax = sliders.max;
	}

	/**
	 * Initializes standard movement variables such as speed and turning for the specified
	 * table
	 * @param movementType which table to add the movement UI elements to
	 */
	private void createMovementUi(final MovementTypes movementType) {
		AlignTable table = mMovementTable;
		GuiHider hider = null;
		if (movementType == MovementTypes.PATH) {
			hider = mPathHider;
		} else if (movementType == MovementTypes.AI) {
			hider = mAiHider;
		}


		// Movement Speed
		mUiFactory.addPanelSection("Movement Speed", table, hider);
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setSpeed(newValue);
				mWidgets.movement.pathSpeedSlider.setValue(newValue);
				mWidgets.movement.aiSpeedSlider.setValue(newValue);
			}
		};
		Slider slider = mUiFactory.addSlider(null, Enemy.Movement.MOVE_SPEED_MIN, Enemy.Movement.MOVE_SPEED_MAX, Enemy.Movement.MOVE_SPEED_STEP_SIZE,
				sliderListener, table, hider, mDisabledWhenPublished, mInvoker);
		if (movementType == MovementTypes.PATH) {
			mWidgets.movement.pathSpeedSlider = slider;
		} else if (movementType == MovementTypes.AI) {
			mWidgets.movement.aiSpeedSlider = slider;
		}


		// Turning
		mUiFactory.addPanelSection("Turning Speed", table, hider);

		// Create ON/OFF tabs
		// ON
		TabImageWrapper onTab = mUiFactory.createTabImageWrapper();
		onTab.imageName = SkinNames.EditorIcons.ON;
		onTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				onChecked(true);
			}

			@Override
			protected void onHide() {
				onChecked(false);
			}

			/**
			 * Sets the correct state for the button
			 * @param checked true if ON is checked
			 */
			private void onChecked(boolean checked) {
				mEnemyEditor.setTurning(checked);

				// Send command for undo
				if (mButton.getName() == null || !mButton.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(mButton, checked));
				}
			}
		};
		onTab.hider.setName("on");

		// OFF
		TabImageWrapper offTab = mUiFactory.createTabImageWrapper();
		offTab.imageName = SkinNames.EditorIcons.OFF;

		// Create tabs
		@SuppressWarnings("unchecked")
		ArrayList<TabWrapper> tabs = Pools.arrayList.obtain();
		tabs.add(onTab);
		tabs.add(offTab);
		mUiFactory.addTabs(table, hider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);
		tabs = null;

		// Set buttons
		if (movementType == MovementTypes.PATH) {
			mWidgets.movement.pathTurnSpeedOn = onTab.button;
			mWidgets.movement.pathTurnSpeedOff = offTab.button;
		} else if (movementType == MovementTypes.AI) {
			mWidgets.movement.aiTurnSpeedOn = onTab.button;
			mWidgets.movement.aiTurnSpeedOff = offTab.button;
		}

		// Slider
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setTurnSpeed(newValue);
				mWidgets.movement.pathTurnSpeedSlider.setValue(newValue);
				mWidgets.movement.aiTurnSpeedSlider.setValue(newValue);
			}
		};
		slider = mUiFactory.addSlider(null, Movement.TURN_SPEED_MIN, Movement.TURN_SPEED_MAX, Movement.TURN_SPEED_STEP_SIZE, sliderListener, table,
				onTab.hider, mDisabledWhenPublished, mInvoker);
		if (movementType == MovementTypes.PATH) {
			mWidgets.movement.pathTurnSpeedSlider = slider;
		} else if (movementType == MovementTypes.AI) {
			mWidgets.movement.aiTurnSpeedSlider = slider;
		}
	}

	/**
	 * Initializes the movement GUI part
	 */
	private void initMovementMenu() {
		// Create radio tabs
		// Path
		TabRadioWrapper pathTab = mUiFactory.createTabRadioWrapper();
		pathTab.text = "Path";
		pathTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setMovementType(MovementTypes.PATH);

				// if (mWidgets.movement.pathTurnSpeedOn != null &&
				// mWidgets.movement.pathTurnSpeedOff != null) {
				// if (mEnemyEditor.isTurning()) {
				// mWidgets.movement.pathTurnSpeedOn.setChecked(true);
				// } else {
				// mWidgets.movement.pathTurnSpeedOff.setChecked(true);
				// }
				// }
			}
		};
		mPathHider = pathTab.hider;
		mPathHider.addToggleActor(mPathLabels);

		// Stationary
		TabRadioWrapper stationaryTab = mUiFactory.createTabRadioWrapper();
		stationaryTab.text = "Stationary";
		stationaryTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setMovementType(MovementTypes.STATIONARY);
			}
		};

		// AI
		TabRadioWrapper aiTab = mUiFactory.createTabRadioWrapper();
		aiTab.text = "AI";
		aiTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setMovementType(MovementTypes.AI);

				// if (mWidgets.movement.aiTurnSpeedOn != null &&
				// mWidgets.movement.aiTurnSpeedOff != null) {
				// if (mEnemyEditor.isTurning()) {
				// mWidgets.movement.aiTurnSpeedOn.setChecked(true);
				// } else {
				// mWidgets.movement.aiTurnSpeedOff.setChecked(true);
				// }
				// }
			}
		};
		mAiHider = aiTab.hider;


		// Create buttons
		@SuppressWarnings("unchecked")
		ArrayList<TabWrapper> tabs = Pools.arrayList.obtain();
		tabs.add(pathTab);
		tabs.add(aiTab);
		tabs.add(stationaryTab);
		mUiFactory.addTabs(mMovementTable, mMovementHider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);


		// Set buttons
		mWidgets.movement.pathBox = (CheckBox) pathTab.button;
		mWidgets.movement.stationaryBox = (CheckBox) stationaryTab.button;
		mWidgets.movement.aiBox = (CheckBox) aiTab.button;

		// Set tooltips
		mTooltip.add(pathTab.button, Messages.EditorTooltips.MOVEMENT_PATH);
		mTooltip.add(aiTab.button, Messages.EditorTooltips.MOVEMENT_AI);
		mTooltip.add(stationaryTab.button, Messages.EditorTooltips.MOVEMENT_STATIONARY);
	}

	/**
	 * Initializes the weapon GUI part
	 */
	@SuppressWarnings("unchecked")
	private void initWeapon() {
		AlignTable table = mWeaponTable;

		mUiFactory.addPanelSection("Enemy Weapon", table, mWeaponHider);

		// Toggle weapon ON/OFF
		// ON
		TabImageWrapper onTab = mUiFactory.createTabImageWrapper();
		onTab.imageName = SkinNames.EditorIcons.ON;
		onTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setUseWeapon(true);
			}

			@Override
			protected void onHide() {
				mEnemyEditor.setUseWeapon(false);
			}
		};

		// OFF
		TabImageWrapper offTab = mUiFactory.createTabImageWrapper();
		offTab.imageName = SkinNames.EditorIcons.OFF;

		// Create tabs
		ArrayList<TabWrapper> tabs = Pools.arrayList.obtain();
		tabs.add(onTab);
		tabs.add(offTab);
		mUiFactory.addTabs(table, mWeaponHider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);
		tabs = null;

		// Set buttons
		mWidgets.weapon.on = onTab.button;
		mWidgets.weapon.off = offTab.button;


		// Select bullet type
		mUiFactory.addPanelSection("Select Bullet Type", table, onTab.hider);

		// Bullet image
		table.row();
		ImageButton imageButton = mUiFactory.addImageButton(SkinNames.EditorIcons.BULLET_SELECT, table, onTab.hider, mDisabledWhenPublished);
		new ButtonListener(imageButton) {
			@Override
			protected void onPressed() {
				mEnemyEditor.selectBulletType();
			}
		};


		// Bullet settings
		mUiFactory.addPanelSection("Bullet Settings", table, onTab.hider);

		// Speed
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setBulletSpeed(newValue);
			}
		};
		mWidgets.weapon.bulletSpeed = mUiFactory.addSlider("Speed", Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE,
				sliderListener, table, onTab.hider, mDisabledWhenPublished, mInvoker);

		// Damage
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setWeaponDamage(newValue);
			}
		};
		mWidgets.weapon.damage = mUiFactory.addSlider("Damage", Weapon.DAMAGE_MIN, Weapon.DAMAGE_MAX, Weapon.DAMAGE_STEP_SIZE, sliderListener, table,
				onTab.hider, mDisabledWhenPublished, mInvoker);


		// Cooldown
		SliderListener minSliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setCooldownMin(newValue);
			}
		};
		SliderListener maxSliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setCooldownMax(newValue);
			}
		};
		SliderMinMaxWrapper sliders = mUiFactory.addSliderMinMax("Weapon Cooldown Time", Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX,
				Weapon.COOLDOWN_STEP_SIZE, minSliderListener, maxSliderListener, table, onTab.hider, mDisabledWhenPublished, mInvoker);

		// Set sliders
		mWidgets.weapon.cooldownMin = sliders.min;
		mWidgets.weapon.cooldownMax = sliders.max;


		// -- Aim Tabs --
		// On Player
		TabImageWrapper onPlayerTab = mUiFactory.createTabImageWrapper();
		onPlayerTab.imageName = SkinNames.EditorIcons.AIM_ON_PLAYER;
		onPlayerTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setAimType(AimTypes.ON_PLAYER);
			}
		};

		// In front of player
		TabImageWrapper inFrontPlayerTab = mUiFactory.createTabImageWrapper();
		inFrontPlayerTab.imageName = SkinNames.EditorIcons.AIM_IN_FRONT_PLAYER;
		inFrontPlayerTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setAimType(AimTypes.IN_FRONT_OF_PLAYER);
			}
		};

		// Movement direction
		TabImageWrapper moveDirTab = mUiFactory.createTabImageWrapper();
		moveDirTab.imageName = SkinNames.EditorIcons.AIM_MOVEMENT;
		moveDirTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setAimType(AimTypes.MOVE_DIRECTION);
			}
		};

		// Direction
		TabImageWrapper directionTab = mUiFactory.createTabImageWrapper();
		directionTab.imageName = SkinNames.EditorIcons.AIM_DIRECTION;
		directionTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setAimType(AimTypes.DIRECTION);
			}
		};

		// Rotate
		TabImageWrapper rotateTab = mUiFactory.createTabImageWrapper();
		rotateTab.imageName = SkinNames.EditorIcons.AIM_ROTATE;
		rotateTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				mEnemyEditor.setAimType(AimTypes.ROTATE);
			}
		};

		// Create tabs
		tabs = Pools.arrayList.obtain();
		tabs.add(onPlayerTab);
		tabs.add(inFrontPlayerTab);
		tabs.add(moveDirTab);
		tabs.add(directionTab);
		tabs.add(rotateTab);
		mUiFactory.addTabs(table, onTab.hider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);

		// Set buttons
		mWidgets.weapon.aimOnPlayer = onPlayerTab.button;
		mWidgets.weapon.aimInFrontOfPlayer = inFrontPlayerTab.button;
		mWidgets.weapon.aimMoveDirection = moveDirTab.button;
		mWidgets.weapon.aimDirection = directionTab.button;
		mWidgets.weapon.aimRotate = rotateTab.button;

		// Set tooltips
		mTooltip.add(onPlayerTab.button, Messages.EditorTooltips.AIM_ON_PLAYER);
		mTooltip.add(inFrontPlayerTab.button, Messages.EditorTooltips.AIM_IN_FRONT_OF_PLAYER);
		mTooltip.add(moveDirTab.button, Messages.EditorTooltips.AIM_MOVEMENT_DIRECTION);
		mTooltip.add(directionTab.button, Messages.EditorTooltips.AIM_DIRECTION);
		mTooltip.add(rotateTab.button, Messages.EditorTooltips.AIM_ROTATE);

		// // Labels
		// // On Player
		// mUiFactory.addPanelSection("On Player", table, onPlayerTab.hider);
		//
		// // In front of player
		// mUiFactory.addPanelSection("In Front Of Player", table,
		// inFrontPlayerTab.hider);
		//
		// // Movement direction
		// mUiFactory.addPanelSection("Enemy Movement Direction", table,
		// moveDirTab.hider);
		//
		// // Specific direction
		// mUiFactory.addPanelSection("Fixed Direction", table, directionTab.hider);
		//
		// // Rotate
		// mUiFactory.addPanelSection("Rotate", table, rotateTab.hider);


		// Specific settings
		// Direction angle
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
				mWidgets.weapon.aimRotateStartAngle.setValue(newValue);
			}
		};
		mWidgets.weapon.aimDirectionAngle = mUiFactory.addSlider("Angle", Enemy.Weapon.START_ANGLE_MIN, Enemy.Weapon.START_ANGLE_MAX,
				Enemy.Weapon.START_ANGLE_STEP_SIZE, sliderListener, table, directionTab.hider, mDisabledWhenPublished, mInvoker);

		// Rotate options
		// Angle
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
				mWidgets.weapon.aimDirectionAngle.setValue(newValue);
			}
		};
		mWidgets.weapon.aimRotateStartAngle = mUiFactory.addSlider("Angle", Enemy.Weapon.START_ANGLE_MIN, Enemy.Weapon.START_ANGLE_MAX,
				Enemy.Weapon.START_ANGLE_STEP_SIZE, sliderListener, table, rotateTab.hider, mDisabledWhenPublished, mInvoker);

		// Rotation speed
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimRotateSpeed(newValue);
			}
		};
		mWidgets.weapon.aimRotateSpeed = mUiFactory.addSlider("Speed", Enemy.Weapon.ROTATE_SPEED_MIN, Enemy.Weapon.ROTATE_SPEED_MAX,
				Enemy.Weapon.ROTATE_SPEED_STEP_SIZE, sliderListener, table, rotateTab.hider, mDisabledWhenPublished, mInvoker);
	}

	@Override
	ITooltip getFileNewTooltip() {
		return Messages.EditorTooltips.FILE_NEW_ENEMY;
	}

	@Override
	ITooltip getFileDuplicateTooltip() {
		return Messages.EditorTooltips.FILE_DUPLICATE_ENEMY;
	}

	@Override
	ITooltip getFilePublishTooltip() {
		return Messages.EditorTooltips.FILE_PUBLISH_ENEMY;
	}

	@Override
	ITooltip getFileInfoTooltip() {
		return Messages.EditorTooltips.FILE_INFO_ENEMY;
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
	private HideListener mPathHider = null;
	/** Hides ai table */
	private HideListener mAiHider = null;


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
			// Cell bulletImage = null;
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
