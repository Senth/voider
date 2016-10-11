package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.spiddekauga.utils.ColorArray;
import com.spiddekauga.utils.commands.CGuiCheck;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.ResourceTextureImage;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual;
import com.spiddekauga.voider.config.IC_Editor.IC_Enemy.IC_Movement;
import com.spiddekauga.voider.config.IC_Editor.IC_Enemy.IC_Weapon;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.ButtonFactory.TabImageWrapper;
import com.spiddekauga.voider.scene.ui.ButtonFactory.TabWrapper;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiFactory.SliderMinMaxWrapper;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.utils.Messages;
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
	protected void onResize(int width, int height) {
		super.onResize(width, height);

		updatePathLabelsPositions();
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
		if (mEnemyEditor.getDef() == null) {
			return;
		}

		switch (mEnemyEditor.getMovementType()) {
		case PATH:
			mWidgets.movement.pathBox.setChecked(true);
			mWidgets.movement.currentType.setText("Path");
			break;

		case STATIONARY:
			mWidgets.movement.stationaryBox.setChecked(true);
			mWidgets.movement.currentType.setText("Stationary");
			break;

		case AI:
			mWidgets.movement.aiBox.setChecked(true);
			mWidgets.movement.currentType.setText("AI");
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

		mWidgets.movement.speedListener.setValue(mEnemyEditor.getSpeed());
		mWidgets.movement.turnSpeedListener.setValue(mEnemyEditor.getTurnSpeed());


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
		mWidgets.weapon.relativeToLevelSpeed.setChecked(mEnemyEditor.isBulletSpeedRelativeToLevelSpeed());
		mWidgets.weapon.bulletImage.setResource(mEnemyEditor.getSelectedBulletDef());

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
		LabelStyles labelStyle = LabelStyles.PATH;

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

		ArrayList<AlignTable> labelTables = new ArrayList<>();
		labelTables.add(mWidgets.path.once);
		labelTables.add(mWidgets.path.loop);
		labelTables.add(mWidgets.path.backForth);

		fixPathLabelOnZoom(centerPositions);

		for (int i = 0; i < centerPositions.length; ++i) {
			AlignTable table = labelTables.get(i);
			Vector2 pos = centerPositions[i];

			table.layout();
			pos.x -= (int) (table.getWidth() / 2);
			pos.y -= (int) (table.getHeight() / 2);

			table.setPosition(pos);
		}
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
			Vector2 cameraPos = new Vector2(camera.position.x, camera.position.y);
			cameraPos.scl(worldScale * zoom);
			Vector2 screenCenter = new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);


			Vector2 deltaPos = new Vector2();
			Vector2 labelPosOriginal = new Vector2();

			for (Vector2 pos : positions) {
				labelPosOriginal.set(pos);
				deltaPos.set(labelPosOriginal).sub(screenCenter);
				deltaPos.scl(zoom);
				pos.set(screenCenter).sub(cameraPos).add(deltaPos);
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
		ImageButton button = mUiFactory.button.createImage(SkinNames.EditorIcons.MOVEMENT);
		mSettingTabs.addTabScroll(button, mMovementTable, mMovementHider);
		mTooltip.add(button, Messages.EditorTooltips.TAB_MOVEMENT);

		// Weapons
		button = mUiFactory.button.createImage(SkinNames.EditorIcons.WEAPON);
		mSettingTabs.addTab(button, mWeaponTable, mWeaponHider);
		mTooltip.add(button, Messages.EditorTooltips.TAB_WEAPON);

		// Visual
		button = mUiFactory.button.createImage(SkinNames.EditorIcons.VISUALS);
		mSettingTabs.addTab(button, getVisualTable(), getVisualHider());
		mTooltip.add(button, Messages.EditorTooltips.TAB_VISUAL);

		// Color
		button = mUiFactory.button.createImage(SkinNames.EditorIcons.COLOR);
		mSettingTabs.addTab(button, getColorTable());
		mTooltip.add(button, Messages.EditorTooltips.TAB_COLOR_ACTOR);

		// Collision
		button = mUiFactory.button.createImage(SkinNames.EditorIcons.COLLISION);
		mSettingTabs.addTab(button, getCollisionTable());
		mTooltip.add(button, Messages.EditorTooltips.TAB_COLLISION);
	}

	/**
	 * Initializes movement path / AI
	 */
	private void initMovement() {
		// Create speed slider listeners
		mWidgets.movement.speedListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setSpeed(newValue);
			}
		};

		mWidgets.movement.turnSpeedListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setTurnSpeed(newValue);
			}
		};


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
		SliderMinMaxWrapper sliders = mUiFactory.addSliderMinMax("Distance From Player", "EnemyMovement_AiDistanceFromPlayer",
				icMovement.getAiDistanceMin(), icMovement.getAiDistanceMax(), icMovement.getAiDistanceStepSize(), minSliderListener,
				maxSliderListener, table, hider, mDisabledWhenPublished);

		// Set sliders
		mWidgets.movement.aiDistanceMin = sliders.min;
		mWidgets.movement.aiDistanceMax = sliders.max;


		// Random movement
		mUiFactory.text.addPanelSection("Random Movement", table, hider);

		// ON/OFF tabs
		// ON
		TabImageWrapper onTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.ON);
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
		TabImageWrapper offTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.OFF);

		// Create tabs
		ArrayList<TabWrapper> tabs = new ArrayList<>();
		tabs.add(onTab);
		tabs.add(offTab);
		mUiFactory.button.addTabs(table, hider, false, mDisabledWhenPublished, mInvoker, tabs);
		tabs = null;

		// Set buttons
		mWidgets.movement.aiRandomMovementOn = onTab.getButton();
		mWidgets.movement.aiRandomMovementOff = offTab.getButton();


		// Sliders
		ArrayList<Actor> createdActors = new ArrayList<>();
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
		sliders = mUiFactory.addSliderMinMax(null, "EnemyMovement_AiRandomTime", icMovement.getRandomMovementTimeMin(),
				icMovement.getRandomMovementTimeMax(), icMovement.getRandomMovementTimeStepSize(), minSliderListener, maxSliderListener, table,
				onTab.getHider(), createdActors);
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
		mUiFactory.addSlider(null, "EnemyMovement_Speed", icMovement.getMoveSpeedMin(), icMovement.getMoveSpeedMax(),
				icMovement.getMoveSpeedStepSize(), mWidgets.movement.speedListener, table, hider, mDisabledWhenPublished);


		// Turning
		mUiFactory.text.addPanelSection("Turning Speed", table, hider);

		// Create ON/OFF tabs
		// ON
		TabImageWrapper onTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.ON);

		// OFF
		TabImageWrapper offTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.OFF);

		// Create tabs
		ArrayList<TabWrapper> tabs = new ArrayList<>();
		tabs.add(onTab);
		tabs.add(offTab);
		mUiFactory.button.addTabs(table, hider, false, mDisabledWhenPublished, mInvoker, tabs);
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
		mUiFactory.addSlider(null, "EnemyMovement_TurnSpeed", icMovement.getTurnSpeedMin(), icMovement.getTurnSpeedMax(),
				icMovement.getTurnSpeedStepSize(), mWidgets.movement.turnSpeedListener, table, onTab.getHider(), mDisabledWhenPublished);
	}

	/**
	 * Initializes the movement GUI part
	 */
	private void initMovementMenu() {
		// Movement type label
		mUiFactory.text.addPanelSection("Movement Type:", mMovementTable, mMovementHider);


		// Create radio tabs
		// Path
		TabImageWrapper pathTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.MOVEMENT_PATH);
		pathTab.setHider(new HideListener(true) {
			@Override
			protected void onShow() {
				updatePathLabelsPositions();
			}
		});
		pathTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setMovementType(MovementTypes.PATH);
				mWidgets.movement.currentType.setText("Path");
			}
		});
		mPathHider = pathTab.getHider();

		// Stationary
		TabImageWrapper stationaryTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.MOVEMENT_STATIONARY);
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
		TabImageWrapper aiTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.MOVEMENT_AI);
		aiTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setMovementType(MovementTypes.AI);
				mWidgets.movement.currentType.setText("AI");
			}
		});
		mAiHider = aiTab.getHider();


		// Create buttons
		ArrayList<TabWrapper> tabs = new ArrayList<>();
		tabs.add(pathTab);
		tabs.add(aiTab);
		tabs.add(stationaryTab);
		mUiFactory.button.addTabs(mMovementTable, mMovementHider, false, mDisabledWhenPublished, mInvoker, tabs);

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
		mPathHider.addToggleActor(mWidgets.path.backForth);
		mPathHider.addToggleActor(mWidgets.path.once);
		mPathHider.addToggleActor(mWidgets.path.loop);
	}

	/**
	 * Initializes the weapon GUI part
	 */
	private void initWeapon() {
		AlignTable table = mWeaponTable;

		mUiFactory.text.addPanelSection("Enemy Weapon", table, mWeaponHider);

		// Toggle weapon ON/OFF
		// ON
		TabImageWrapper onTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.ON);
		onTab.setListener(new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mEnemyEditor.setUseWeapon(checked);
			};
		});

		// OFF
		TabImageWrapper offTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.OFF);

		// Create tabs
		ArrayList<TabWrapper> tabs = new ArrayList<>();
		tabs.add(onTab);
		tabs.add(offTab);
		mUiFactory.button.addTabs(table, mWeaponHider, false, mDisabledWhenPublished, mInvoker, tabs);
		tabs = null;

		// Set buttons
		mWidgets.weapon.on = onTab.getButton();
		mWidgets.weapon.off = offTab.getButton();


		// Select bullet type
		mUiFactory.text.addPanelSection("Select Bullet Type", table, onTab.getHider());

		// Select bullet button
		table.row().setFillWidth(true);
		ImageButton imageButton = mUiFactory.button.addImage(SkinNames.EditorIcons.BULLET_SELECT, table, onTab.getHider(), mDisabledWhenPublished);
		new ButtonListener(imageButton) {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.selectBulletType();
			}
		};

		// Bullet image
		table.add().setFillWidth(true);
		ResourceTextureImage image = new ResourceTextureImage();
		mWidgets.weapon.bulletImage = image;
		table.add(image).setSize(mUiFactory.getStyles().vars.rowHeightSection, mUiFactory.getStyles().vars.rowHeightSection);

		// Bullet settings
		mUiFactory.text.addPanelSection("Bullet Settings", table, onTab.getHider());

		// Speed
		SliderListener sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setBulletSpeed(newValue);
			}
		};
		mWidgets.weapon.bulletSpeed = mUiFactory.addSlider("Speed", "EnemyWeapon_BulletSpeed", Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX,
				Weapon.BULLET_SPEED_STEP_SIZE, sliderListener, table, onTab.getHider(), mDisabledWhenPublished);

		// Speed is relative to the level speed
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mEnemyEditor.setBulletSpeedRelativeToLevelSpeed(checked);
			}
		};
		mWidgets.weapon.relativeToLevelSpeed = mUiFactory.button.addPanelCheckBox("Relative to level speed?", buttonListener, table,
				onTab.getHider(), mDisabledWhenPublished);
		table.getRow().setHeight(mUiFactory.getStyles().vars.rowHeight);

		// Damage
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setWeaponDamage(newValue);
			}
		};
		mWidgets.weapon.damage = mUiFactory.addSlider("Damage", "EnemyWeapon_Damage", Weapon.DAMAGE_MIN, Weapon.DAMAGE_MAX, Weapon.DAMAGE_STEP_SIZE,
				sliderListener, table, onTab.getHider(), mDisabledWhenPublished);


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
		SliderMinMaxWrapper sliders = mUiFactory
				.addSliderMinMax("Weapon Cooldown Time", "EnemyWeapon_CooldownTime", Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX,
						Weapon.COOLDOWN_STEP_SIZE, minSliderListener, maxSliderListener, table, onTab.getHider(), mDisabledWhenPublished);

		// Set sliders
		mWidgets.weapon.cooldownMin = sliders.min;
		mWidgets.weapon.cooldownMax = sliders.max;


		// -- Aim Tabs --
		// On Player
		TabImageWrapper onPlayerTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.AIM_ON_PLAYER);
		onPlayerTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.ON_PLAYER);
			}
		});

		// In front of player
		TabImageWrapper inFrontPlayerTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.AIM_IN_FRONT_PLAYER);
		inFrontPlayerTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.IN_FRONT_OF_PLAYER);
			}
		});

		// Movement direction
		TabImageWrapper moveDirTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.AIM_MOVEMENT);
		moveDirTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.MOVE_DIRECTION);
			}
		});

		// Direction
		TabImageWrapper directionTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.AIM_DIRECTION);
		directionTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.DIRECTION);
			}
		});

		// Rotate
		TabImageWrapper rotateTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.AIM_ROTATE);
		rotateTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mEnemyEditor.setAimType(AimTypes.ROTATE);
			}
		});

		// Create tabs
		tabs = new ArrayList<>();
		tabs.add(onPlayerTab);
		tabs.add(inFrontPlayerTab);
		tabs.add(moveDirTab);
		tabs.add(directionTab);
		tabs.add(rotateTab);
		mUiFactory.button.addTabs(table, onTab.getHider(), false, mDisabledWhenPublished, mInvoker, tabs);

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


		// Specific settings
		IC_Weapon icWeapon = ConfigIni.getInstance().editor.enemy.weapon;

		// Direction angle
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimStartAngle(newValue);
			}
		};
		mWidgets.weapon.aimDirectionAngle = mUiFactory
				.addSlider("Angle", "EnemyWeapon_AimDirection", icWeapon.getStartAngleMin(), icWeapon.getStartAngleMax(),
						icWeapon.getStartAngleStepSize(), sliderListener, table, directionTab.getHider(), mDisabledWhenPublished);

		// Rotate options
		// Angle
		mWidgets.weapon.aimRotateStartAngle = mUiFactory.addSlider("Angle", "EnemyWeapon_AimRotateDirection", icWeapon.getStartAngleMin(),
				icWeapon.getStartAngleMax(), icWeapon.getStartAngleStepSize(), sliderListener, table, rotateTab.getHider(), mDisabledWhenPublished);

		// Rotation speed
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mEnemyEditor.setAimRotateSpeed(newValue);
			}
		};
		mWidgets.weapon.aimRotateSpeed = mUiFactory.addSlider("Speed", "EnemyWeapon_AimRotateSpeed", icWeapon.getRotateSpeedMin(),
				icWeapon.getRotateSpeedMax(), icWeapon.getRotateSpeedStepSize(), sliderListener, table, rotateTab.getHider(), mDisabledWhenPublished);
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
				once.setName("path_once");
				backForth.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
				backForth.setName("path_backForth");
				loop.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
				loop.setName("path_loop");
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
			Button pathTurnSpeedOn = null;
			Button pathTurnSpeedOff = null;
			Button aiTurnSpeedOn = null;
			Button aiTurnSpeedOff = null;
			SliderListener speedListener = null;
			SliderListener turnSpeedListener = null;


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
			ResourceTextureImage bulletImage = null;
			Slider bulletSpeed = null;
			Slider damage = null;
			Slider cooldownMin = null;
			Slider cooldownMax = null;
			Button relativeToLevelSpeed = null;

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
