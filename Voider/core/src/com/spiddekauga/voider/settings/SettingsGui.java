package com.spiddekauga.voider.settings;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.Resolution;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonEnumListener;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.SelectBoxListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_General;
import com.spiddekauga.voider.menu.MenuGui;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.settings.SettingRepo.IconSizes;

/**
 * GUI for game settings
 */
public class SettingsGui extends MenuGui {

private final static Builds SHOW_DEBUG = Builds.BETA;
private Widgets mWidgets = new Widgets();
private SettingsScene mScene = null;

/**
 * Sets the settings scene
 * @param scene
 */
void setScene(SettingsScene scene) {
	mScene = scene;
}

@Override
public void onCreate() {
	super.onCreate();

	initHeader();
	initTabs();
	initGeneral();
	initSound();

	if (Gdx.app.getType() == ApplicationType.Desktop) {
		initDisplay();
	}

	if (Gdx.app.getType() == ApplicationType.Android) {
		// initNetwork();
	}

	// Debug
	if (Config.Debug.isBuildOrBelow(SHOW_DEBUG)) {
		initDebug();
	}

	addBackButton();

	resetValues();
}

private void initHeader() {
	mWidgets.tabWidget = mUiFactory.addSettingsWindow("Game Settings", mMainTable);
}

private void initTabs() {
	TabWidget tabWidget = mWidgets.tabWidget;

	if (Gdx.app.getType() == ApplicationType.Desktop) {
		mUiFactory.button.addTab(SkinNames.General.SETTINGS_DISPLAY, mWidgets.display.table, mWidgets.display.hider, tabWidget);
	}
	mUiFactory.button.addTab(SkinNames.General.SETTINGS_SOUND, mWidgets.sound.table, tabWidget);
	if (Gdx.app.getType() == ApplicationType.Android) {
		// mUiFactory.button.addTab(SkinNames.General.SETTINGS_NETWORK,
		// mWidgets.network.table, tabWidget);
	}
	mUiFactory.button.addTab(SkinNames.General.SETTINGS_GENERAL, mWidgets.general.table, tabWidget);

	if (Config.Debug.isBuildOrBelow(SHOW_DEBUG)) {
		mUiFactory.button.addTab(SkinNames.General.SETTINGS_DEBUG, mWidgets.debug.table, tabWidget);
	}
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
	table.getRow().setAlign(Vertical.BOTTOM);
	table.getCell().setAlign(Vertical.BOTTOM);
	ButtonGroup<CheckBox> buttonGroup = new ButtonGroup<>();
	ButtonListener buttonListener = new ButtonListener() {
		@Override
		protected void onChecked(Button button, boolean checked) {
			mScene.set24HourFormat(checked);
		}
	};
	mWidgets.general.time24h = mUiFactory.button.addCheckBoxRow("24hr", CheckBoxStyles.RADIO, buttonListener, buttonGroup, table);
	mWidgets.general.timeAmPm = mUiFactory.button.addCheckBoxRow("AM/PM", CheckBoxStyles.RADIO, null, buttonGroup, table);
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
	mWidgets.sound.master = mUiFactory.addSlider("Master", "Sound_Master", 0, 100, 1, sliderListener, table, null, null);

	// Game Effects
	sliderListener = new SliderListener() {
		@Override
		protected void onChange(float newValue) {
			mScene.setGameVolume(newValue);
		}
	};
	mWidgets.sound.game = mUiFactory.addSlider("Game", "Sound_Game", 0, 100, 1, sliderListener, table, null, null);

	// Music
	sliderListener = new SliderListener() {
		@Override
		protected void onChange(float newValue) {
			mScene.setMusicVolume(newValue);
		}
	};
	mWidgets.sound.music = mUiFactory.addSlider("Music", "Sound_Music", 0, 100, 1, sliderListener, table, null, null);

	// Button
	sliderListener = new SliderListener() {
		@Override
		protected void onChange(float newValue) {
			mScene.setUiVolume(newValue);
		}
	};
	mWidgets.sound.ui = mUiFactory.addSlider("Buttons", "Sound_Buttons", 0, 100, 1, sliderListener, table, null, null);
}

private void initDisplay() {
	AlignTable table = mWidgets.display.table;
	initTable(table, "Display");

	// Windowed Resolution
	SelectBoxListener<Resolution> selectBoxListener = new SelectBoxListener<Resolution>() {
		@Override
		protected void onSelectionChanged(int itemIndex) {
			mScene.setResolutionWindowed(mSelectBox.getSelected());
		}
	};
	mWidgets.display.resolutionWindowed = mUiFactory.addSelectBox("Window Resolution", Resolution.getWindowedResolutions(), selectBoxListener,
			table, mWidgets.display.showWindowedResolution, null);

	// Fullscreen resolution
	selectBoxListener = new SelectBoxListener<Resolution>() {
		@Override
		protected void onSelectionChanged(int itemIndex) {
			mScene.setResolutionFullscreen(mSelectBox.getSelected());
		}
	};
	mWidgets.display.resolutionFullscreen = mUiFactory.addSelectBox("Window Resolution", Resolution.getFullscreenResolutions(),
			selectBoxListener, table, mWidgets.display.showFullscreenResolution, null);


	// Fullscreen
	table.row();
	ButtonListener buttonListener = new ButtonListener() {
		@Override
		protected void onChecked(Button button, boolean checked) {
			mScene.setFullscreen(checked);
		}
	};
	mWidgets.display.fullscreen = mUiFactory.button.addCheckBoxRow("Fullscreen (Alt+Enter)", CheckBoxStyles.CHECK_BOX, buttonListener, null,
			table);
	mWidgets.display.showFullscreenResolution.addButton(mWidgets.display.fullscreen);
	mWidgets.display.showWindowedResolution.addButton(mWidgets.display.fullscreen);


	// Icon sizes
	mUiFactory.text.addSection("UI & Icon Size", table, null);
	table.row();
	mUiFactory.button.addEnumButton(IconSizes.SMALL, SkinNames.General.ICON_SIZE_SMALL, null, table, mWidgets.display.iconSizes);
	mUiFactory.button.addEnumButton(IconSizes.MEDIUM, SkinNames.General.ICON_SIZE_MEDIUM, null, table, mWidgets.display.iconSizes);
	mUiFactory.button.addEnumButton(IconSizes.LARGE, SkinNames.General.ICON_SIZE_LARGE, null, table, mWidgets.display.iconSizes);
	new ButtonGroup<ImageButton>(mWidgets.display.iconSizes);
	new ButtonEnumListener<IconSizes>(mWidgets.display.iconSizes, IconSizes.values()) {
		@Override
		protected void onPressed(Button button) {
			IconSizes iconSize = getCheckedFirst();
			if (iconSize != null) {
				mScene.setIconSize(iconSize);
			}
		}
	};
}

/**
 * Initialize debug settings
 */
private void initDebug() {
	AlignTable table = mWidgets.debug.table;
	initTable(table, "Debug");

	ButtonListener buttonListener = new ButtonListener() {
		@Override
		protected void onPressed(Button button) {
			mScene.clearData();
		}
	};
	table.row().setFillWidth(true);
	mUiFactory.button.addText("Clear all data and logoutAndGotoLogin", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null).setFillWidth(true);

	table.row();
	mUiFactory.text.add("This clears all local user data and logs out the user.", true, table, LabelStyles.HIGHLIGHT);
}

@Override
public void resetValues() {
	super.resetValues();

	resetSound();
	resetDisplay();
	resetGeneral();
	resetNetwork();
}

@Override
public void onDestroy() {
	super.onDestroy();
	mWidgets.dispose();
}

/**
 * Initializes the table with a header
 * @param table the table to initialize
 * @param header the header text
 */
private static void initTable(AlignTable table, String header) {
	table.setAlign(Horizontal.LEFT, Vertical.MIDDLE);
	table.setName(header);

	table.row().setAlign(Vertical.TOP).setFillWidth(true).setPadBottom(mUiFactory.getStyles().vars.paddingInner);
	table.add().setFillWidth(true);
	mUiFactory.text.add(header, table, LabelStyles.HIGHLIGHT);
	table.add().setFillWidth(true);
}

private void resetSound() {
	mWidgets.sound.master.setValue(mScene.getMasterVolume());
	mWidgets.sound.game.setValue(mScene.getGameVolume());
	mWidgets.sound.music.setValue(mScene.getMusicVolume());
	mWidgets.sound.ui.setValue(mScene.getUiVolume());
}

private void resetDisplay() {
	if (mWidgets.display.fullscreen != null) {
		mWidgets.display.fullscreen.setChecked(mScene.isFullscreen());
		mWidgets.display.resolutionFullscreen.setSelected(mScene.getResolutionFullscreen());
		mWidgets.display.resolutionWindowed.setSelected(mScene.getResolutionWindowed());
		mWidgets.display.iconSizes[mScene.getIconSize().ordinal()].setChecked(true);
	}
}

private void resetGeneral() {
	mWidgets.general.dateFormat.setSelected(mScene.getDateFormat());
	if (mScene.is24HourFormat()) {
		mWidgets.general.time24h.setChecked(true);
	} else {
		mWidgets.general.timeAmPm.setChecked(true);
	}
}

private void resetNetwork() {
	if (mWidgets.network.mobileDataAllow != null) {
		mWidgets.network.mobileDataAllow.setChecked(mScene.isMobileDataAllowed());
	}
}

/**
 * Initialize network settings
 */
@SuppressWarnings("unused")
private void initNetwork() {
	AlignTable table = mWidgets.network.table;
	initTable(table, "Network");

	// Only connect to the Internet through WIFI
	ButtonListener buttonListener = new ButtonListener() {
		@Override
		protected void onChecked(Button button, boolean checked) {
			mScene.setMobileDataAllowed(checked);
		}
	};
	mWidgets.network.mobileDataAllow = mUiFactory.button.addCheckBoxRow("Allow Mobile Data", CheckBoxStyles.CHECK_BOX, buttonListener, null,
			table);
	table.row();
	mUiFactory.text.add("(If unchecked, this device will only use WIFI connection)", true, table, LabelStyles.INFO_EXTRA);
}

private class Widgets implements Disposable {
	TabWidget tabWidget = null;
	Sound sound = new Sound();
	General general = new General();
	Display display = new Display();
	Network network = new Network();
	Debug debug = new Debug();

	private class Sound implements Disposable {
		AlignTable table = new AlignTable();
		Slider master = null;
		Slider ui = null;
		Slider game = null;
		Slider music = null;

		@Override
		public void dispose() {
			table.dispose();
		}
	}

	private class General implements Disposable {
		AlignTable table = new AlignTable();
		Button time24h = null;
		Button timeAmPm = null;
		SelectBox<String> dateFormat = null;

		@Override
		public void dispose() {
			table.dispose();
		}
	}

	private class Display implements Disposable {
		AlignTable table = new AlignTable();
		HideListener hider = new HideListener(true);
		HideListener showFullscreenResolution = new HideListener(true);
		HideListener showWindowedResolution = new HideListener(false);
		SelectBox<Resolution> resolutionFullscreen = null;
		SelectBox<Resolution> resolutionWindowed = null;
		Button fullscreen = null;
		ImageButton[] iconSizes = new ImageButton[IconSizes.values().length];

		private Display() {
			init();
		}

		private void init() {
			hider.addChild(showFullscreenResolution);
			hider.addChild(showWindowedResolution);
		}

		@Override
		public void dispose() {
			table.dispose();
			hider.dispose();
			showFullscreenResolution.dispose();
			showWindowedResolution.dispose();

			init();
		}


	}

	private class Network implements Disposable {
		AlignTable table = new AlignTable();
		Button mobileDataAllow = null;

		@Override
		public void dispose() {
			table.dispose();
		}
	}

	private class Debug implements Disposable {
		AlignTable table = new AlignTable();

		@Override
		public void dispose() {
			table.dispose();
		}
	}

	@Override
	public void dispose() {
		display.dispose();
		sound.dispose();
		general.dispose();
	}
}
}