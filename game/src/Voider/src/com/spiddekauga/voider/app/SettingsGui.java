package com.spiddekauga.voider.app;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.SelectBoxListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_General;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;

/**
 * GUI for game settings
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SettingsGui extends Gui {

	/**
	 * Sets the settings scene
	 * @param scene
	 */
	void setScene(SettingsScene scene) {
		mScene = scene;
	}

	@Override
	public void dispose() {
		super.dispose();

		mWidgets.display.table.dispose();
		mWidgets.general.table.dispose();
		mWidgets.sound.table.dispose();
	}

	@Override
	public void initGui() {
		super.initGui();

		setBackground(SkinNames.GeneralImages.BACKGROUND_SPACE, true);

		initHeader();
		initTabs();
		initGeneral();
		initSound();

		if (Gdx.app.getType() == ApplicationType.Desktop) {
			initDisplay();
		}
	}

	private void initHeader() {
		mWidgets.tabWidget = mUiFactory.addSettingsWindow("Game Settings", mMainTable);
	}

	private void initTabs() {
		TabWidget tabWidget = mWidgets.tabWidget;

		if (Gdx.app.getType() == ApplicationType.Desktop) {
			mUiFactory.addTab(SkinNames.General.SETTINGS_DISPLAY, mWidgets.display.table, null, tabWidget);
		}
		mUiFactory.addTab(SkinNames.General.SETTINGS_SOUND, mWidgets.sound.table, null, tabWidget);
		mUiFactory.addTab(SkinNames.General.SETTINGS_GENERAL, mWidgets.general.table, null, tabWidget);
	}

	private void initGeneral() {
		AlignTable table = mWidgets.general.table;
		initTable(table, "General");

		mUiFactory.text.addSection("Date Format", table, null);

		// Date format
		IC_General general = ConfigIni.getInstance().setting.general;
		SelectBoxListener<String> selectBoxListener = new SelectBoxListener<String>() {
			@Override
			protected void onSelectionChanged(int itemIndex) {
				mScene.setDateFormat(mSelectBox.getSelected());
			}
		};
		mWidgets.general.dateFormat = mUiFactory.addSelectBox(null, general.getDateFormats(), selectBoxListener, table, null, null);

		// 24 hours?
		mUiFactory.text.addSection("Time Format", table, null);
		ButtonGroup buttonGroup = new ButtonGroup();
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.set24HourFormat(checked);
			}
		};
		mWidgets.general.time24h = mUiFactory.addCheckBoxRow("24hr", CheckBoxStyles.RADIO, buttonListener, buttonGroup, table);
		mWidgets.general.timeAmPm = mUiFactory.addCheckBoxRow("AM/PM", CheckBoxStyles.RADIO, null, buttonGroup, table);
	}

	private void initSound() {
		AlignTable table = mWidgets.sound.table;
		initTable(table, "Sound");


		// Master
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mScene.setMasterVolume(newValue);
			}
		};
		mWidgets.sound.master = mUiFactory.addSlider("Master", 0, 100, 1, sliderListener, table, null, null);

		// Game Effects
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mScene.setGameVolume(newValue);
			}
		};
		mWidgets.sound.game = mUiFactory.addSlider("Game", 0, 100, 1, sliderListener, table, null, null);

		// Music
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mScene.setMusicVolume(newValue);
			}
		};
		mWidgets.sound.music = mUiFactory.addSlider("Music", 0, 100, 1, sliderListener, table, null, null);

		// Button
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mScene.setUiVolume(newValue);
			}
		};
		mWidgets.sound.ui = mUiFactory.addSlider("Buttons", 0, 100, 1, sliderListener, table, null, null);
	}

	private void initDisplay() {
		AlignTable table = mWidgets.display.table;
		initTable(table, "Display");
	}

	/**
	 * Initializes the table with a header
	 * @param table the table to initialize
	 * @param header the header text
	 */
	private static void initTable(AlignTable table, String header) {
		table.setAlign(Horizontal.LEFT, Vertical.MIDDLE);

		float paddingRow = mUiFactory.getStyles().vars.paddingOuter;
		table.row().setAlign(Vertical.TOP).setFillWidth(true).setPadBottom(mUiFactory.getStyles().vars.paddingInner);
		table.add().setFillWidth(true);
		mUiFactory.text.add(header, table, LabelStyles.HIGHLIGHT);
		table.add().setFillWidth(true);

		table.setPaddingRowDefault(paddingRow, 0, 0, 0);
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetSound();
		resetDisplay();
		resetGeneral();
	}

	private void resetSound() {
		mWidgets.sound.master.setValue(mScene.getMasterVolume());
		mWidgets.sound.game.setValue(mScene.getGameVolume());
		mWidgets.sound.music.setValue(mScene.getMusicVolume());
		mWidgets.sound.ui.setValue(mScene.getUiVolume());
	}

	private void resetDisplay() {

	}

	private void resetGeneral() {
		mWidgets.general.dateFormat.setSelected(mScene.getDateFormat());
		if (mScene.is24HourFormat()) {
			mWidgets.general.time24h.setChecked(true);
		} else {
			mWidgets.general.timeAmPm.setChecked(true);
		}
	}

	private static class InnerWidgets {
		TabWidget tabWidget = null;
		Sound sound = new Sound();
		General general = new General();
		Display display = new Display();


		class Sound {
			AlignTable table = new AlignTable();
			Slider master = null;
			Slider ui = null;
			Slider game = null;
			Slider music = null;
		}

		class General {
			AlignTable table = new AlignTable();
			Button time24h = null;
			Button timeAmPm = null;
			SelectBox<String> dateFormat = null;
		}

		class Display {
			AlignTable table = new AlignTable();
		}
	}

	private InnerWidgets mWidgets = new InnerWidgets();
	private SettingsScene mScene = null;
}