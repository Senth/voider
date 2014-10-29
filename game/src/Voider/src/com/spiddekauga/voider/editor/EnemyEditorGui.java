package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.spiddekauga.utils.ColorArray;
import com.spiddekauga.utils.commands.CGuiCheck;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual;
import com.spiddekauga.voider.config.IC_Editor.IC_Enemy.IC_Movement;
import com.spiddekauga.voider.config.IC_Editor.IC_Enemy.IC_Weapon;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiFactory.SliderMinMaxWrapper;
import com.spiddekauga.voider.scene.ui.UiFactory.TabImageWrapper;
import com.spiddekauga.voider.scene.ui.UiFactory.TabWrapper;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

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

		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.connect(EventTypes.CAMERA_ZOOM_CHANGE, mCameraListener);
		eventDispatcher.connect(EventTypes.CAMERA_MOVED, mCameraListener);
	}

	@Override
	public void dispose() {
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.disconnect(EventTypes.CAMERA_ZOOM_CHANGE, mCameraListener);
		eventDispatcher.disconnect(EventTypes.CAMERA_MOVED, mCameraListener);

		mMovementTable.dispose();
		mWeaponTable.dispose();
		mWidgets.path.dispose();

		mMovementHider.dispose();
		mWeaponHider.dispose();
		mPathHider.dispose();
		mAiHider.dispose();

		super.dispose();
	}

	@Override
	void resetCollisionBoxes() {
		if (mSettingTabs == null) {
			return;
		}

		super.resetCollisionBoxes();

		// Tab widget
		createCollisionBoxes(mSettingTabs);

		// Tool
		createCollisionBoxes(mToolMenu);

		// Upper/Lower borders
		float width = Gdx.graphics.getWidth();
		float height = mUiFactory.getStyles().vars.barUpperLowerHeight;
		createCollisionBox(0, 0, width, height);
		createCollisionBox(0, Gdx.graphics.getHeight() - height, width, height);
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
		ColorArray colorArray = SkinNames.getResource(SkinNames.EditorVars.ENEMY_COLOR_PICKER);
		initColor(colorArray.arr);

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
			break;
		}

		if (mEnemyEditor.isMovingRandomly()) {
			mWidgets.movement.aiRandomMovementOn.setChecked(true);
		} else {
			mWidgets.movement.aiRandomMovementOff.setChecked(true);
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

		updatePathLabelsPositions();

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

		mSettingTabs.layout();
		mToolMenu.layout();

		if (mEditor.getScreenToWorldScale() != 0) {
			resetCollisionBoxes();
		}
	}

	/**
	 * Initializes the path labels
	 */
	private void initPathLabels() {
		LabelStyle labelStyle = new LabelStyle(LabelStyles.DEFAULT.getStyle());
		labelStyle.fontColor = SkinNames.getResource(SkinNames.EditorVars.PATH_COLOR);

		mUiFactory.addIconLabel(SkinNames.EditorImages.PATH_BACK_AND_FORTH, "Back and Forth", Positions.BOTTOM, labelStyle, mWidgets.path.backForth,
				null, null);

		mUiFactory.addIconLabel(SkinNames.EditorImages.PATH_LOOP, "Loop", Positions.BOTTOM, labelStyle, mWidgets.path.loop, null, null);

		mUiFactory.addIconLabel(SkinNames.EditorImages.PATH_ONCE, "Once", Positions.BOTTOM, labelStyle, mWidgets.path.once, null, null);

		addActor(mWidgets.path.once);
		addActor(mWidgets.path.backForth);
		addActor(mWidgets.path.loop);

		// Move to back
		mWidgets.path.once.setZIndex(0);
		mWidgets.path.backForth.setZIndex(0);
		mWidgets.path.loop.setZIndex(0);
	}

	/**
	 * Update path labels
	 */
	void updatePathLabelsPositions() {
		Vector2[] centerPositions = mEnemyEditor.getPathPositions();

		@SuppressWarnings("unchecked")
		ArrayList<AlignTable> labelTables = Pools.arrayList.obtain();
		labelTables.add(mWidgets.path.once);
		labelTables.add(mWidgets.path.loop);
		labelTables.add(mWidgets.path.backForth);


		Vector2 offset = Pools.vector2.obtain();

		fixPathLabelOnZoom(centerPositions);

		for (int i = 0; i < centerPositions.length; ++i) {
			AlignTable table = labelTables.get(i);
			Vector2 pos = centerPositions[i];

			table.layout();
			pos.x -= (int) (table.getWidth() / 2);
			pos.y -= (int) (table.getHeight() / 2);

			table.setPosition(pos);
		}

		Pools.vector2.freeAll(centerPositions);
		Pools.vector2.free(offset);
		Pools.arrayList.free(labelTables);
	}

	/**
	 * Scale paths depending on current zoom
	 * @param positions center positions to update
	 */
	private void fixPathLabelOnZoom(Vector2[] positions) {
		OrthographicCamera camera = mEditor.getCamera();

		if (camera != null) {
			// label_0 = original label position
			// zoom = 1 / camera.zoom
			// camera_1 = camera center after movement & zoom (screen coordinates)
			// screen_0 = center of screen
			// delta_0 = Label diff from center of screen
			// delta_1 = New label diff from center after zoom
			// label_1 = New label position


			float zoom = 1 / camera.zoom;
			float worldScale = Gdx.graphics.getWidth() / camera.viewportWidth;
			Vector2 cameraPos = Pools.vector2.obtain().set(camera.position.x, camera.position.y);
			cameraPos.scl(worldScale * zoom);
			Vector2 screenCenter = Pools.vector2.obtain().set(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);


			Vector2 deltaPos = Pools.vector2.obtain();
			Vector2 labelPosOriginal = Pools.vector2.obtain();

			for (Vector2 pos : positions) {
				labelPosOriginal.set(pos);
				deltaPos.set(labelPosOriginal).sub(screenCenter);
				deltaPos.scl(zoom);
				pos.set(screenCenter).sub(cameraPos).add(deltaPos);
			}

			Pools.vector2.freeAll(screenCenter, cameraPos, deltaPos, labelPosOriginal);
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
		Button button = mSettingTabs.addTabScroll(buttonStyle, mMovementTable, mMovementHider);
		mTooltip.add(button, Messages.EditorTooltips.TAB_MOVEMENT);

		// Weapons
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.WEAPON);
		button = mSettingTabs.addTab(buttonStyle, mWeaponTable, mWeaponHider);
		mTooltip.add(button, Messages.EditorTooltips.TAB_WEAPON);

		// Visual
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.VISUALS);
		button = mSettingTabs.addTab(buttonStyle, getVisualTable(), getVisualHider());
		mTooltip.add(button, Messages.EditorTooltips.TAB_VISUAL);

		// Color
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.COLOR);
		button = mSettingTabs.addTab(buttonStyle, getColorTable());
		mTooltip.add(button, Messages.EditorTooltips.TAB_COLOR_ACTOR);

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
		IC_Movement icMovement = ConfigIni.getInstance().editor.enemy.movement;


		// Distance from player
		SliderListener minSliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMin(newValue);
			}
		};
		SliderListener maxSliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setPlayerDistanceMax(newValue);
			}
		};
		SliderMinMaxWrapper sliders = mUiFactory.addSliderMinMax("Distance From Player", icMovement.getAiDistanceMin(),
				icMovement.getAiDistanceMax(), icMovement.getAiDistanceStepSize(), minSliderListener, maxSliderListener, table, hider,
				mDisabledWhenPublished);

		// Set sliders
		mWidgets.movement.aiDistanceMin = sliders.min;
		mWidgets.movement.aiDistanceMax = sliders.max;


		// Random movement
		mUiFactory.text.addPanelSection("Random Movement", table, hider);

		// ON/OFF tabs
		// ON
		TabImageWrapper onTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.ON);
		onTab.setListener(new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mEnemyEditor.setMoveRandomly(checked);

				// Send command for undo
				if (button.getName() == null || !button.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiCheck(button, checked));
				}
			}
		});

		// OFF
		TabImageWrapper offTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.OFF);

		// Create tabs
		@SuppressWarnings("unchecked")
		ArrayList<TabWrapper> tabs = Pools.arrayList.obtain();
		tabs.add(onTab);
		tabs.add(offTab);
		mUiFactory.addTabs(table, hider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);
		tabs = null;

		// Set buttons
		mWidgets.movement.aiRandomMovementOn = onTab.getButton();
		mWidgets.movement.aiRandomMovementOff = offTab.getButton();


		// Sliders
		@SuppressWarnings("unchecked")
		ArrayList<Actor> createdActors = Pools.arrayList.obtain();
		minSliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMin(newValue);
			}
		};
		maxSliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setRandomTimeMax(newValue);
			}
		};
		sliders = mUiFactory.addSliderMinMax(null, icMovement.getRandomMovementTimeMin(), icMovement.getRandomMovementTimeMax(),
				icMovement.getRandomMovementTimeStepSize(), minSliderListener, maxSliderListener, table, onTab.getHider(), createdActors);
		mTooltip.add(createdActors, Messages.EditorTooltips.MOVEMENT_AI_RANDOM_COOLDOWN);
		mDisabledWhenPublished.addAll(createdActors);

		// Set sliders
		mWidgets.movement.aiRandomTimeMin = sliders.min;
		mWidgets.movement.aiRandomTimeMax = sliders.max;
	}

	/**
	 * Initializes standard movement variables such as speed and turning for the specified table
	 * @param movementType which table to add the movement UI elements to
	 */
	private void createMovementUi(final MovementTypes movementType) {
		IC_Movement icMovement = ConfigIni.getInstance().editor.enemy.movement;

		AlignTable table = mMovementTable;
		GuiHider hider = null;
		if (movementType == MovementTypes.PATH) {
			hider = mPathHider;
		} else if (movementType == MovementTypes.AI) {
			hider = mAiHider;
		}


		// Movement Speed
		mUiFactory.text.addPanelSection("Movement Speed", table, hider);
		SliderListener sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setSpeed(newValue);
				mWidgets.movement.pathSpeedSlider.setValue(newValue);
				mWidgets.movement.aiSpeedSlider.setValue(newValue);
			}
		};
		Slider slider = mUiFactory.addSlider(null, icMovement.getMoveSpeedMin(), icMovement.getMoveSpeedMax(), icMovement.getMoveSpeedStepSize(),
				sliderListener, table, hider, mDisabledWhenPublished);
		if (movementType == MovementTypes.PATH) {
			mWidgets.movement.pathSpeedSlider = slider;
		} else if (movementType == MovementTypes.AI) {
			mWidgets.movement.aiSpeedSlider = slider;
		}


		// Turning
		mUiFactory.text.addPanelSection("Turning Speed", table, hider);

		// Create ON/OFF tabs
		// ON
		TabImageWrapper onTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.ON);

		// OFF
		TabImageWrapper offTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.OFF);

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
			mWidgets.movement.pathTurnSpeedOn = onTab.getButton();
			mWidgets.movement.pathTurnSpeedOff = offTab.getButton();
		} else if (movementType == MovementTypes.AI) {
			mWidgets.movement.aiTurnSpeedOn = onTab.getButton();
			mWidgets.movement.aiTurnSpeedOff = offTab.getButton();
		}
		new ButtonListener(onTab.getButton()) {
			/**
			 * Sets the correct state for the button
			 * @param checked true if ON is checked
			 */
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (mEnemyEditor.isTurning() != checked) {
					mEnemyEditor.setTurning(checked);

					// Send command for undo
					if (button.getName() == null || !button.getName().equals(Config.Gui.GUI_INVOKER_TEMP_NAME)) {
						mInvoker.execute(new CGuiCheck(button, checked));
					}

					if (checked) {
						mWidgets.movement.pathTurnSpeedOn.setChecked(true);
						mWidgets.movement.aiTurnSpeedOn.setChecked(true);
					} else {
						mWidgets.movement.pathTurnSpeedOff.setChecked(true);
						mWidgets.movement.aiTurnSpeedOff.setChecked(true);
					}
				}
			}
		};

		// Slider
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setTurnSpeed(newValue);
				mWidgets.movement.pathTurnSpeedSlider.setValue(newValue);
				mWidgets.movement.aiTurnSpeedSlider.setValue(newValue);
			}
		};
		slider = mUiFactory.addSlider(null, icMovement.getTurnSpeedMin(), icMovement.getTurnSpeedMax(), icMovement.getTurnSpeedStepSize(),
				sliderListener, table, onTab.getHider(), mDisabledWhenPublished);
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
		// Movement type label
		mUiFactory.text.addPanelSection("Movement Type:", mMovementTable, mMovementHider);


		// Create radio tabs
		// Path
		TabImageWrapper pathTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.MOVEMENT_PATH);
		pathTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setMovementType(MovementTypes.PATH);
				mWidgets.movement.currentType.setText("Path");
				updatePathLabelsPositions();
			}
		});
		mPathHider = pathTab.getHider();

		// Stationary
		TabImageWrapper stationaryTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.MOVEMENT_STATIONARY);
		stationaryTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				if (mEnemyEditor.getMovementType() != MovementTypes.STATIONARY) {
					mEnemyEditor.setMovementType(MovementTypes.STATIONARY);
					mWidgets.movement.currentType.setText("Stationary");
				}
			}
		});

		// AI
		TabImageWrapper aiTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.MOVEMENT_AI);
		aiTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setMovementType(MovementTypes.AI);
				mWidgets.movement.currentType.setText("AI");
			}
		});
		mAiHider = aiTab.getHider();


		// Create buttons
		@SuppressWarnings("unchecked")
		ArrayList<TabWrapper> tabs = Pools.arrayList.obtain();
		tabs.add(pathTab);
		tabs.add(aiTab);
		tabs.add(stationaryTab);
		mUiFactory.addTabs(mMovementTable, mMovementHider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);

		mMovementTable.row().setHeight(mUiFactory.getStyles().vars.rowHeightSection).setAlign(Vertical.MIDDLE);
		mWidgets.movement.currentType = mUiFactory.text.add("Path", mMovementTable);

		// Set buttons
		mWidgets.movement.pathBox = pathTab.getButton();
		mWidgets.movement.stationaryBox = stationaryTab.getButton();
		mWidgets.movement.aiBox = aiTab.getButton();

		// Set tooltips
		mTooltip.add(pathTab.getButton(), Messages.EditorTooltips.MOVEMENT_PATH);
		mTooltip.add(aiTab.getButton(), Messages.EditorTooltips.MOVEMENT_AI);
		mTooltip.add(stationaryTab.getButton(), Messages.EditorTooltips.MOVEMENT_STATIONARY);

		// Hider for path labels
		HideListener pathLabelHider = new HideListener(pathTab.getButton(), true);
		pathLabelHider.addToggleActor(mWidgets.path.backForth);
		pathLabelHider.addToggleActor(mWidgets.path.once);
		pathLabelHider.addToggleActor(mWidgets.path.loop);
	}

	/**
	 * Initializes the weapon GUI part
	 */
	@SuppressWarnings("unchecked")
	private void initWeapon() {
		AlignTable table = mWeaponTable;

		mUiFactory.text.addPanelSection("Enemy Weapon", table, mWeaponHider);

		// Toggle weapon ON/OFF
		// ON
		TabImageWrapper onTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.ON);
		onTab.setListener(new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mEnemyEditor.setUseWeapon(checked);
			};
		});

		// OFF
		TabImageWrapper offTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.OFF);

		// Create tabs
		ArrayList<TabWrapper> tabs = Pools.arrayList.obtain();
		tabs.add(onTab);
		tabs.add(offTab);
		mUiFactory.addTabs(table, mWeaponHider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);
		tabs = null;

		// Set buttons
		mWidgets.weapon.on = onTab.getButton();
		mWidgets.weapon.off = offTab.getButton();


		// Select bullet type
		mUiFactory.text.addPanelSection("Select Bullet Type", table, onTab.getHider());

		// Bullet image
		table.row();
		ImageButton imageButton = mUiFactory.addImageButton(SkinNames.EditorIcons.BULLET_SELECT, table, onTab.getHider(), mDisabledWhenPublished);
		new ButtonListener(imageButton) {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.selectBulletType();
			}
		};


		// Bullet settings
		mUiFactory.text.addPanelSection("Bullet Settings", table, onTab.getHider());

		// Speed
		SliderListener sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setBulletSpeed(newValue);
			}
		};
		mWidgets.weapon.bulletSpeed = mUiFactory.addSlider("Speed", Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE,
				sliderListener, table, onTab.getHider(), mDisabledWhenPublished);

		// Damage
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setWeaponDamage(newValue);
			}
		};
		mWidgets.weapon.damage = mUiFactory.addSlider("Damage", Weapon.DAMAGE_MIN, Weapon.DAMAGE_MAX, Weapon.DAMAGE_STEP_SIZE, sliderListener, table,
				onTab.getHider(), mDisabledWhenPublished);


		// Cooldown
		SliderListener minSliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setCooldownMin(newValue);
			}
		};
		SliderListener maxSliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setCooldownMax(newValue);
			}
		};
		SliderMinMaxWrapper sliders = mUiFactory.addSliderMinMax("Weapon Cooldown Time", Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX,
				Weapon.COOLDOWN_STEP_SIZE, minSliderListener, maxSliderListener, table, onTab.getHider(), mDisabledWhenPublished);

		// Set sliders
		mWidgets.weapon.cooldownMin = sliders.min;
		mWidgets.weapon.cooldownMax = sliders.max;


		// -- Aim Tabs --
		// On Player
		TabImageWrapper onPlayerTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.AIM_ON_PLAYER);
		onPlayerTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.ON_PLAYER);
			}
		});

		// In front of player
		TabImageWrapper inFrontPlayerTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.AIM_IN_FRONT_PLAYER);
		inFrontPlayerTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.IN_FRONT_OF_PLAYER);
			}
		});

		// Movement direction
		TabImageWrapper moveDirTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.AIM_MOVEMENT);
		moveDirTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.MOVE_DIRECTION);
			}
		});

		// Direction
		TabImageWrapper directionTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.AIM_DIRECTION);
		directionTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.DIRECTION);
			}
		});

		// Rotate
		TabImageWrapper rotateTab = mUiFactory.createTabImageWrapper(SkinNames.EditorIcons.AIM_ROTATE);
		rotateTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.ROTATE);
			}
		});

		// Create tabs
		tabs = Pools.arrayList.obtain();
		tabs.add(onPlayerTab);
		tabs.add(inFrontPlayerTab);
		tabs.add(moveDirTab);
		tabs.add(directionTab);
		tabs.add(rotateTab);
		mUiFactory.addTabs(table, onTab.getHider(), tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);

		// Set buttons
		mWidgets.weapon.aimOnPlayer = onPlayerTab.getButton();
		mWidgets.weapon.aimInFrontOfPlayer = inFrontPlayerTab.getButton();
		mWidgets.weapon.aimMoveDirection = moveDirTab.getButton();
		mWidgets.weapon.aimDirection = directionTab.getButton();
		mWidgets.weapon.aimRotate = rotateTab.getButton();

		// Set tooltips
		mTooltip.add(onPlayerTab.getButton(), Messages.EditorTooltips.AIM_ON_PLAYER);
		mTooltip.add(inFrontPlayerTab.getButton(), Messages.EditorTooltips.AIM_IN_FRONT_OF_PLAYER);
		mTooltip.add(moveDirTab.getButton(), Messages.EditorTooltips.AIM_MOVEMENT_DIRECTION);
		mTooltip.add(directionTab.getButton(), Messages.EditorTooltips.AIM_DIRECTION);
		mTooltip.add(rotateTab.getButton(), Messages.EditorTooltips.AIM_ROTATE);

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
		IC_Weapon icWeapon = ConfigIni.getInstance().editor.enemy.weapon;

		// Direction angle
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
				mWidgets.weapon.aimRotateStartAngle.setValue(newValue);
			}
		};
		mWidgets.weapon.aimDirectionAngle = mUiFactory.addSlider("Angle", icWeapon.getStartAngleMin(), icWeapon.getStartAngleMax(),
				icWeapon.getStartAngleStepSize(), sliderListener, table, directionTab.getHider(), mDisabledWhenPublished);

		// Rotate options
		// Angle
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
				mWidgets.weapon.aimDirectionAngle.setValue(newValue);
			}
		};
		mWidgets.weapon.aimRotateStartAngle = mUiFactory.addSlider("Angle", icWeapon.getStartAngleMin(), icWeapon.getStartAngleMax(),
				icWeapon.getStartAngleStepSize(), sliderListener, table, rotateTab.getHider(), mDisabledWhenPublished);

		// Rotation speed
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimRotateSpeed(newValue);
			}
		};
		mWidgets.weapon.aimRotateSpeed = mUiFactory.addSlider("Speed", icWeapon.getRotateSpeedMin(), icWeapon.getRotateSpeedMax(),
				icWeapon.getRotateSpeedStepSize(), sliderListener, table, rotateTab.getHider(), mDisabledWhenPublished);
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

	@Override
	protected IC_Visual getVisualConfig() {
		return ConfigIni.getInstance().editor.enemy.visual;
	}

	/** Zoom listener that fixes label locations */
	private IEventListener mCameraListener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			updatePathLabelsPositions();
		}
	};

	// Tables
	/** Container for all movement options */
	private AlignTable mMovementTable = new AlignTable();
	/** Container for all weapon options */
	private AlignTable mWeaponTable = new AlignTable();

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
	private static class InnerWidgets {
		MovementWidgets movement = new MovementWidgets();
		WeaponWidgets weapon = new WeaponWidgets();
		PathLabels path = new PathLabels();

		static class PathLabels {
			AlignTable once = new AlignTable();
			AlignTable backForth = new AlignTable();
			AlignTable loop = new AlignTable();

			{
				once.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
				backForth.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
				loop.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
				once.setPositionManually(true);
				backForth.setPositionManually(true);
				loop.setPositionManually(true);
			}

			void dispose() {
				once.dispose();
				backForth.dispose();
				loop.dispose();
			}
		}

		static class MovementWidgets {
			// Current movement type
			Label currentType = null;

			// Movement type
			Button pathBox = null;
			Button stationaryBox = null;
			Button aiBox = null;

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
