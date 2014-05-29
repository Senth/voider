package com.spiddekauga.voider.editor;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.UiFactory.SliderMinMaxWrapper;
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
		// Speed
		mUiFactory.addPanelSection("Bullet Properties", mWeaponTable, null);
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setBulletSpeed(newValue);
			}
		};
		mWidgets.weapon.bulletSpeed = mUiFactory.addSlider(
				"Speed",
				Weapon.BULLET_SPEED_MIN,
				Weapon.BULLET_SPEED_MAX,
				Weapon.BULLET_SPEED_STEP_SIZE,
				sliderListener,
				mWeaponTable,
				null,
				null,
				null,
				mInvoker);


		// Cooldown
		SliderListener sliderMinListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setCooldownMin(newValue);
			}
		};
		SliderListener sliderMaxListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setCooldownMax(newValue);
			}
		};
		SliderMinMaxWrapper sliders = mUiFactory.addSliderMinMax(
				"Cooldown Time",
				Weapon.COOLDOWN_MIN,
				Weapon.COOLDOWN_MAX,
				Weapon.COOLDOWN_STEP_SIZE,
				sliderMinListener, sliderMaxListener,
				mWeaponTable,
				null,
				null,
				null,
				mInvoker);

		mWidgets.weapon.cooldownMin = sliders.min;
		mWidgets.weapon.cooldownMax = sliders.max;


		String warningText =
				"These properties are not bound to the " +
						"current bullet. They are only here to " +
						"test how the bullet will appear on " +
						"different weapons.";


		Label label = new Label(warningText, mStyles.label.highlight);
		label.setWrap(true);
		label.setWidth(240);
		label.layout();
		mWeaponTable.setName("weapon");
		mWeaponTable.row().setFillWidth(true).setAlign(Horizontal.CENTER, Vertical.TOP);
		mWeaponTable.add(label).setFillWidth(true).setAlign(Horizontal.CENTER, Vertical.TOP);
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
