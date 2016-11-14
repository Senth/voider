package com.spiddekauga.voider.editor;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.spiddekauga.utils.ColorArray;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory.SliderMinMaxWrapper;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.utils.Messages;

/**
 * GUI for the bullet editor
 */
public class BulletEditorGui extends ActorGui {

/** Container for testing bullet */
private AlignTable mWeaponTable = new AlignTable();
/** Bullet editor scene this GUI is bound to */
private BulletEditor mBulletEditor = null;
/** All GUI widgets */
private InnerWidgets mWidgets = new InnerWidgets();

@Override
public void dispose() {
	mWeaponTable.dispose();

	super.dispose();
}

@Override
public void initGui() {
	super.initGui();

	mWeaponTable.setAlignTable(Horizontal.LEFT, Vertical.TOP);
	mWeaponTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);

	initWeapon();
	ColorArray colorArray = SkinNames.getResource(SkinNames.EditorVars.BULLET_COLOR_PICKER);
	initColor(colorArray.arr);

	resetValues();
}

@Override
public void resetValues() {
	super.resetValues();

	mWidgets.weapon.bulletSpeed.setValue(mBulletEditor.getBulletSpeed());
	mWidgets.weapon.cooldownMin.setValue(mBulletEditor.getCooldownMin());
	mWidgets.weapon.cooldownMax.setValue(mBulletEditor.getCooldownMax());
}

@Override
protected IC_Visual getVisualConfig() {
	return ConfigIni.getInstance().editor.bullet.visual;
}

/**
 * Initializes test weapon table
 */
private void initWeapon() {
	// Speed
	mUiFactory.text.addPanelSection("Bullet Properties", mWeaponTable, null);
	SliderListener sliderListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mBulletEditor.setBulletSpeed(newValue);
		}
	};
	mWidgets.weapon.bulletSpeed = mUiFactory.addSlider("Speed", "Bullet_Speed", Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX,
			Weapon.BULLET_SPEED_STEP_SIZE, sliderListener, mWeaponTable, null, null);


	// Cooldown
	SliderListener sliderMinListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mBulletEditor.setCooldownMin(newValue);
		}
	};
	SliderListener sliderMaxListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mBulletEditor.setCooldownMax(newValue);
		}
	};
	SliderMinMaxWrapper sliders = mUiFactory.addSliderMinMax("Cooldown Time", "Bullet_Cooldown", Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX,
			Weapon.COOLDOWN_STEP_SIZE, sliderMinListener, sliderMaxListener, mWeaponTable, null, null);

	mWidgets.weapon.cooldownMin = sliders.min;
	mWidgets.weapon.cooldownMax = sliders.max;


	String warningText = "These properties are not bound to the " + "current bullet. They are only here to "
			+ "test how the bullet will appear on " + "different weapons.";


	mUiFactory.text.addPanel(warningText, mWeaponTable, LabelStyles.HIGHLIGHT);
}

@Override
protected void initSettingsMenu() {
	super.initSettingsMenu();

	// Visual
	ImageButton button = mUiFactory.button.createImage(SkinNames.EditorIcons.VISUALS);
	mSettingTabs.addTab(button, getVisualTable(), getVisualHider());
	mTooltip.add(button, Messages.EditorTooltips.TAB_VISUAL);

	// Color
	button = mUiFactory.button.createImage(SkinNames.EditorIcons.COLOR);
	mSettingTabs.addTab(button, getColorTable());
	mTooltip.add(button, Messages.EditorTooltips.TAB_COLOR_ACTOR);

	// Weapon
	button = mUiFactory.button.createImage(SkinNames.EditorIcons.WEAPON);
	mSettingTabs.addTab(button, mWeaponTable);
	mTooltip.add(button, Messages.EditorTooltips.TAB_BULLET_TEST);
}

@Override
ITooltip getFileNewTooltip() {
	return Messages.EditorTooltips.FILE_NEW_BULLET;
}

@Override
ITooltip getFileDuplicateTooltip() {
	return Messages.EditorTooltips.FILE_DUPLICATE_BULLET;
}

@Override
ITooltip getFilePublishTooltip() {
	return Messages.EditorTooltips.FILE_PUBLISH_BULLET;
}

// Tables

@Override
ITooltip getFileInfoTooltip() {
	return Messages.EditorTooltips.FILE_INFO_BULLET;
}

@Override
protected String getResourceTypeName() {
	return "bullet";
}

/**
 * Bind this GUI to the specified bullet editor scene
 * @param bulletEditor scene to bind this GUI with
 */
void setBulletEditor(BulletEditor bulletEditor) {
	mBulletEditor = bulletEditor;
	setActorEditor(mBulletEditor);
}

/**
 * All inner widgets
 */
private static class InnerWidgets {
	WeaponWidgets weapon = new WeaponWidgets();

	static class WeaponWidgets {
		Slider bulletSpeed = null;
		Slider cooldownMin = null;
		Slider cooldownMax = null;
	}
}

}
