package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
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
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.ActorGui;

/**
 * GUI for the bullet editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletEditorGui extends ActorGui {

	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setCellPaddingDefault(2, 2, 2, 2);
		mVisualTable.setPreferences(mMainTable);
		mWeaponTable.setPreferences(mMainTable);
		mOptionTable.setPreferences(mMainTable);


		initVisual(
				ActorShapeTypes.CIRCLE,
				ActorShapeTypes.TRIANGLE,
				ActorShapeTypes.RECTANGLE,
				ActorShapeTypes.LINE,
				ActorShapeTypes.CUSTOM
				);
		initWeapon();
		initOptions();
		initFileMenu("bullet");
		initMenu();

		resetValues();


		mMainTable.setTransform(true);
		mVisualTable.setTransform(true);
		mOptionTable.setTransform(true);
		mWeaponTable.setTransform(true);
		mMainTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mMainTable.invalidate();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		mWidgets.weapon.bulletSpeed.setValue(mBulletEditor.getBulletSpeed());
		mWidgets.weapon.cooldownMin.setValue(mBulletEditor.getCooldownMin());
		mWidgets.weapon.cooldownMax.setValue(mBulletEditor.getCooldownMax());
	}

	/**
	 * Bind this GUI to the specified bullet editor scene
	 * @param bulletEditor scene to bind this GUI with
	 */
	public void setBulletEditor(BulletEditor bulletEditor) {
		mBulletEditor = bulletEditor;

		setActorEditor(mBulletEditor);
	}

	/**
	 * Initializes the top menu
	 */
	private void initMenu() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle textToggleStyle = skin.get("toggle", TextButtonStyle.class);


		// --- Active options ---
		mMainTable.row().setAlign(Horizontal.CENTER, Vertical.BOTTOM);
		ButtonGroup buttonGroup = new ButtonGroup();

		// Visual
		Button button = new TextButton("Visual", textToggleStyle);
		buttonGroup.add(button);
		mMainTable.add(button);
		mVisualHider.addToggleActor(mVisualTable);
		mVisualHider.setButton(button);

		// Weapon
		button = new TextButton("Weapons", textToggleStyle);
		buttonGroup.add(button);
		mMainTable.add(button);
		mWeaponHider.addToggleActor(mWeaponTable);
		mWeaponHider.setButton(button);

		// Options
		button = new TextButton("Options", textToggleStyle);
		buttonGroup.add(button);
		mMainTable.add(button);
		mOptionHider.setButton(button);
		mOptionHider.addToggleActor(mOptionTable);


		mMainTable.row().setFillHeight(true).setAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.add(mWeaponTable).setFillWidth(true);
		mMainTable.add(mVisualTable);
		mMainTable.add(mOptionTable).setFillWidth(true).setFillHeight(true);
	}

	/**
	 * Initializes test weapon table
	 */
	private void initWeapon() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		LabelStyle labelStyle = skin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = skin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = skin.get("default", TextFieldStyle.class);

		String warningText =
				"These options are not bound to the " +
						"current bullet. They are only here to " +
						"test how the bullet will appear on " +
						"different weapons.";

		Label label = new Label(warningText, labelStyle);
		label.setWrap(true);
		label.setName("warning");
		label.setWidth(0);
		label.setHeight(label.getHeight() * 4);
		mWeaponTable.setName("weapon");

		mWeaponTable.setScalable(false);
		mWeaponTable.row().setFillWidth(true).setAlign(Horizontal.CENTER, Vertical.TOP);
		mWeaponTable.add(label).setFillWidth(true).setAlign(Horizontal.CENTER, Vertical.TOP);


		// Speed
		mWeaponTable.row();
		label = new Label("Speed", labelStyle);
		mWeaponTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
		Slider slider = new Slider(Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.bulletSpeed = slider;
		mWeaponTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mWeaponTable.add(textField);
		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setBulletSpeed(newValue);
			}
		};

		// Cooldown
		label = new Label("Cooldown time", labelStyle);
		mWeaponTable.row();
		mWeaponTable.add(label);
		label = new Label("Min", labelStyle);
		mWeaponTable.row();
		mWeaponTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMin = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.cooldownMin = sliderMin;
		mWeaponTable.add(sliderMin);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mWeaponTable.add(textField);
		SliderListener sliderMinListener = new SliderListener(sliderMin, textField) {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setCooldownMin(newValue);
			}
		};


		label = new Label("Max", labelStyle);
		mWeaponTable.row();
		mWeaponTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMax = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, sliderStyle);
		mWidgets.weapon.cooldownMax = sliderMax;
		mWeaponTable.add(sliderMax);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mWeaponTable.add(textField);
		SliderListener sliderMaxListener = new SliderListener(sliderMax, textField) {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setCooldownMax(newValue);
			}
		};

		sliderMinListener.setGreaterSlider(sliderMax);
		sliderMaxListener.setLesserSlider(sliderMin);
	}

	// Tables
	/** Container for testing bullet */
	private AlignTable mWeaponTable = new AlignTable();

	// Hiders
	/** Hider for weapon table */
	private HideListener mWeaponHider = new HideListener(true);


	/** Bullet editor scene this GUI is bound to */
	private BulletEditor mBulletEditor = null;
	/** All GUI widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	/**
	 * All inner widgets
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		WeaponWidgets weapon = new WeaponWidgets();

		static class WeaponWidgets {
			Slider bulletSpeed = null;
			Slider cooldownMin = null;
			Slider cooldownMax = null;
		}
	}
}
