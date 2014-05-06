package com.spiddekauga.voider.editor;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.resources.SkinNames;

/**
 * GUI for the bullet editor
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BulletEditorGui extends ActorGui {

	@Override
	public void initGui() {
		super.initGui();

		mWeaponTable.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		mWeaponTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);

		initWeapon();
		resetValues();
	}

	@Override
	public void dispose() {
		mMainTable.dispose();
		mWeaponTable.dispose();

		super.dispose();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		mWidgets.weapon.bulletSpeed.setValue(mBulletEditor.getBulletSpeed());
		mWidgets.weapon.cooldownMin.setValue(mBulletEditor.getCooldownMin());
		mWidgets.weapon.cooldownMax.setValue(mBulletEditor.getCooldownMax());
	}


	@Override
	protected void initSettingsMenu() {
		super.initSettingsMenu();

		// Visual
		ImageButtonStyle buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.VISUALS);
		mSettingTabs.addTab(buttonStyle, getVisualTable(), getVisualHider());

		// Weapon
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.WEAPON);
		mSettingTabs.addTab(buttonStyle, mWeaponTable);
	}

	/**
	 * Bind this GUI to the specified bullet editor scene
	 * @param bulletEditor scene to bind this GUI with
	 */
	public void setBulletEditor(BulletEditor bulletEditor) {
		mBulletEditor = bulletEditor;
		setActorEditor(mBulletEditor);
		setEditor(bulletEditor);
	}

	@Override
	protected String getResourceTypeName() {
		return "bullet";
	}

	/**
	 * Initializes test weapon table
	 */
	private void initWeapon() {
		String warningText =
				"These options are not bound to the " +
						"current bullet. They are only here to " +
						"test how the bullet will appear on " +
						"different weapons.";
		//		String warningText = "warning";

		Label label = new Label(warningText, mStyles.label.highlight);
		label.setWrap(true);
		label.setName("warning");
		label.setWidth(200);
		//		label.setHeight(label.getHeight() * 4);
		mWeaponTable.setName("weapon");
		mWeaponTable.row().setFillWidth(true).setAlign(Horizontal.CENTER, Vertical.TOP);
		mWeaponTable.add(label).setFillWidth(true).setAlign(Horizontal.CENTER, Vertical.TOP);



		// Speed
		mWeaponTable.row();
		label = new Label("Speed", mStyles.label.standard);
		mWeaponTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		Slider slider = new Slider(Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.bulletSpeed = slider;
		mWeaponTable.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWeaponTable.add(textField);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setBulletSpeed(newValue);
			}
		};

		// Cooldown
		label = new Label("Cooldown time", mStyles.label.standard);
		mWeaponTable.row();
		mWeaponTable.add(label);
		label = new Label("Min", mStyles.label.standard);
		mWeaponTable.row();
		mWeaponTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);

		Slider sliderMin = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.cooldownMin = sliderMin;
		mWeaponTable.add(sliderMin);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWeaponTable.add(textField);
		SliderListener sliderMinListener = new SliderListener(sliderMin, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setCooldownMin(newValue);
			}
		};


		label = new Label("Max", mStyles.label.standard);
		mWeaponTable.row();
		mWeaponTable.add(label).setPadRight(mStyles.vars.paddingAfterLabel);

		Slider sliderMax = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.cooldownMax = sliderMax;
		mWeaponTable.add(sliderMax);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWeaponTable.add(textField);
		SliderListener sliderMaxListener = new SliderListener(sliderMax, textField, mInvoker) {
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
