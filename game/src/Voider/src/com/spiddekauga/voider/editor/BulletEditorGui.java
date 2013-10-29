package com.spiddekauga.voider.editor;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Weapon;
import com.spiddekauga.voider.Config.Gui;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Messages;

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
		mMainTable.setCellPaddingDefault(Gui.PADDING_DEFAULT);

		initWeapon();
		initMainMenu();
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

	/**
	 * Initializes menu for switching between visuals and weapon
	 */
	private void initMainMenu() {
		// Visual
		GuiCheckCommandCreator menuChecker = new GuiCheckCommandCreator(mInvoker);
		Button button;
		ButtonGroup buttonGroup = new ButtonGroup();
		/** @todo remove text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Visuals", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.VISUALS.toString());
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		addToEditorMenu(button);
		mVisualHider.addToggleActor(getVisualTable());
		mVisualHider.setButton(button);
		new TooltipListener(button, "Visuals", Messages.replaceName(Messages.Tooltip.Actor.Menu.VISUALS, "bullet"));

		// Weapon
		/** @todo remove text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Weapon", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.WEAPON.toString());
		}
		button.addListener(menuChecker);
		buttonGroup.add(button);
		addToEditorMenu(button);
		mWeaponHider.addToggleActor(mWeaponTable);
		mWeaponHider.setButton(button);
	}

	@Override
	protected void showInfoDialog() {
		// TODO
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

		Label label = new Label(warningText, mStyles.label.standard);
		label.setWrap(true);
		label.setName("warning");
		label.setWidth(10);
		label.setHeight(label.getHeight() * 4);
		mWeaponTable.setName("weapon");
		mWeaponTable.row().setFillWidth(true).setAlign(Horizontal.CENTER, Vertical.TOP);
		mWeaponTable.add(label).setFillWidth(true).setAlign(Horizontal.CENTER, Vertical.TOP);
		mMainTable.add(mWeaponTable);



		// Speed
		mWeaponTable.row();
		label = new Label("Speed", mStyles.label.standard);
		mWeaponTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
		Slider slider = new Slider(Weapon.BULLET_SPEED_MIN, Weapon.BULLET_SPEED_MAX, Weapon.BULLET_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.bulletSpeed = slider;
		mWeaponTable.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
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
		mWeaponTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMin = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.cooldownMin = sliderMin;
		mWeaponTable.add(sliderMin);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mWeaponTable.add(textField);
		SliderListener sliderMinListener = new SliderListener(sliderMin, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mBulletEditor.setCooldownMin(newValue);
			}
		};


		label = new Label("Max", mStyles.label.standard);
		mWeaponTable.row();
		mWeaponTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);

		Slider sliderMax = new Slider(Weapon.COOLDOWN_MIN, Weapon.COOLDOWN_MAX, Weapon.COOLDOWN_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.weapon.cooldownMax = sliderMax;
		mWeaponTable.add(sliderMax);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
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
