package com.spiddekauga.voider.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneListener;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.AnimationWidget;
import com.spiddekauga.utils.scene.ui.AnimationWidget.AnimationWidgetStyle;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.IRatingListener;
import com.spiddekauga.utils.scene.ui.MsgBox;
import com.spiddekauga.utils.scene.ui.RatingWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.utils.scene.ui.Gui;
import com.spiddekauga.voider.scene.ui.UiFactory;

/**
 * All UI elements.
 */
public class TestUiGui extends Gui {
/** Animation widget */
AnimationWidget mAnimationWidget = null;
/** Top right table */
AlignTable mTopRight = new AlignTable();
/** Skin for general */
Skin mGeneralSkin = null;
/** Skin for game */
Skin mGameSkin = null;
/** Loading bar */
Slider mLoadingBar = null;
/** Health bar */
Slider mHealthBar = null;

@Override
public void onDestroy() {
	super.onDestroy();
	if (mTopRight != null) {
		mTopRight.dispose();
	}
}

@Override
public void update() {
	super.update();
	mAnimationWidget.act(Gdx.graphics.getDeltaTime());
}

@Override
public void onCreate() {
	super.onCreate();

	mGeneralSkin = ResourceCacheFacade.get(InternalDeps.UI_GENERAL);
	mGameSkin = ResourceCacheFacade.get(InternalDeps.UI_GAME);
	mMainTable.setAlign(Horizontal.LEFT, Vertical.TOP);
	mTopRight.setPreferences(mMainTable);
	mTopRight.setAlign(Horizontal.RIGHT, Vertical.TOP);
	getStage().addActor(mTopRight);

	initButtons();
	initTextFields();
	initCheckBoxRadioButtons();
	initRating();
	initSliders();
	initWindows();
	initScrollPane();
	initList();
	initSelectionBox();
	initWaitAnimation();

}

/**
 * Initialize buttons
 */
private void initButtons() {
	mMainTable.row();

	// Default
	Button button = new TextButton("Test default", mGeneralSkin, SkinNames.General.TEXT_BUTTON_TRANSPARENT_PRESS.toString());
	mMainTable.add(button);

	// Checkable
	button = new TextButton("Test checkable", mGeneralSkin, SkinNames.General.TEXT_BUTTON_TRANSPARENT_TOGGLE.toString());
	mMainTable.add(button);
}

/**
 * Initialize text fields
 */
private void initTextFields() {
	mMainTable.row();

	TextField textField = new TextField("", mGeneralSkin);
	new TextFieldListener(textField, "Default Text", null);
	mMainTable.add(textField).setPadRight(10);

	textField = new TextField("", mGeneralSkin);
	new TextFieldListener(textField, "", null);
	mMainTable.add(textField);
}

/**
 * Initialize check box / radio buttons
 */
private void initCheckBoxRadioButtons() {
	mMainTable.row();

	// Checkboxes
	CheckBox button = new CheckBox("Checkbox 1", mGeneralSkin, SkinNames.General.CHECK_BOX_DEFAULT.toString());
	mMainTable.add(button);

	button = new CheckBox("Checkbox 2", mGeneralSkin, SkinNames.General.CHECK_BOX_DEFAULT.toString());
	button.setChecked(true);
	mMainTable.add(button).setPadRight(50);

	// Radio buttons
	ButtonGroup<CheckBox> buttonGroup = new ButtonGroup<>();
	button = new CheckBox("Radio 1", mGeneralSkin, SkinNames.General.CHECK_BOX_RADIO.toString());
	buttonGroup.add(button);
	mMainTable.add(button);

	button = new CheckBox("Radio 2", mGeneralSkin, SkinNames.General.CHECK_BOX_RADIO.toString());
	buttonGroup.add(button);
	mMainTable.add(button);
}

/**
 * Initialize ratings
 */
private void initRating() {
	mMainTable.row();
	RatingWidget ratingWidget = UiFactory.getInstance().addRatingWidget(Touchable.enabled, mMainTable, null);
	IRatingListener listener = new IRatingListener() {
		@Override
		public void onRatingChange(int newRating) {
			mNotification.show("Change rating to: " + newRating);
		}
	};
	ratingWidget.addListener(listener);
}

/**
 * Initialize sliders
 */
private void initSliders() {
	mMainTable.row();

	// Default
	Label label = new Label("Slider:", mGeneralSkin);
	mMainTable.add(label).setPadRight(10);
	Slider slider = new Slider(0, 100, 1, false, mGeneralSkin, SkinNames.General.SLIDER_DEFAULT.toString());
	mMainTable.add(slider);

	// Loading bar
	mMainTable.row();
	label = new Label("Loading bar:", mGeneralSkin);
	mMainTable.add(label).setPadRight(10);
	slider = new Slider(0, 100, 1, false, mGeneralSkin, SkinNames.General.SLIDER_LOADING_BAR.toString());
	mLoadingBar = slider;
	mMainTable.add(slider).setPadRight(20);

	// Health bar
	label = new Label("Health bar:", mGeneralSkin);
	mMainTable.add(label).setPadRight(10);
	slider = new Slider(0, 100, 1, false, mGameSkin, SkinNames.Game.HEALTH_BAR.toString());
	mHealthBar = slider;
	mMainTable.add(slider);
}

/**
 * Initialize windows
 */
private void initWindows() {
	mMainTable.row();

	// No title
	Window window = new Window("", mGeneralSkin, "default");
	window.add("No title");
	mMainTable.add(window);

	// Title
	window = new Window("Title", mGeneralSkin, "title");
	window.add("Has title");
	mMainTable.add(window);

	// Modal window, button
	Button button = new TextButton("Modal window (press)", mGeneralSkin);
	mMainTable.add(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			MsgBox msgBox = mUiFactory.msgBox.add(null);
			msgBox.content("Modal window");
			msgBox.addCancelButtonAndKeys("OK");
		}
	};

	// Modal title window, button
	button = new TextButton("Modal title window (press)", mGeneralSkin);
	mMainTable.add(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			MsgBox msgBox = mUiFactory.msgBox.add("Title");
			msgBox.content("Modal window");
			msgBox.addCancelButtonAndKeys("OK");
		}
	};
}

/**
 * Initialize scroll pane
 */
private void initScrollPane() {
	mMainTable.row();

	Label label = new Label("", mGeneralSkin);
	label.setText("Scroll pane\n" + "With background\n" + "Uses same background as window\n" + "aeu aseus a saosuaheu aseou\n"
			+ "aoseuthaoes ase atoeuh saoseuhaoesn auh\n" + "aoeusah  sTHUenuhoeusSNusoruc.sjkbkt \n"
			+ "aoesuasoe tuo uasouarnc.uhoa.ucasuasoethu .usao.cuh.\n" + "aoseunat hao.uh.cuchasoetu\n"
			+ "aoneuthaose .ruca,huaosetuhaoetkmaenkh\n" + "aoneu.sa,oc  sa has rh.usa,o.ucas c  u-e-isk\n" + "aoseutah a.ruchktqjhk-soeu\n");
	ScrollPane scrollPane = new ScrollPane(label, mGeneralSkin, "background");
	mMainTable.add(scrollPane);
	ScrollPaneListener listener = new ScrollPaneListener() {
		@Override
		public void hitEdge(ScrollPane scrollPane, Edge edge) {
			mNotification.show(edge.toString());
		}
	};
	scrollPane.addListener(listener);

	label = new Label("", mGeneralSkin);
	label.setText("Scroll pane\n" + "Force scrollbars\n" + "Without background\n" + "aeu aseus a saosuaheu aseou\n"
			+ "aoseuthaoes ase atoeuh saoseuhaoesn auh\n" + "aoeusah  sTHUenuhoeusSNusoruc.sjkbkt \n"
			+ "aoesuasoe tuo uasouarnc.uhoa.ucasuasoethu .usao.cuh.\n" + "aoseunat hao.uh.cuchasoetu\n"
			+ "aoneuthaose .ruca,huaosetuhaoetkmaenkh\n" + "aoneu.sa,oc  sa has rh.usa,o.ucas c  u-e-isk\n" + "aoseutah a.ruchktqjhk-soeu\n");
	scrollPane = new ScrollPane(label, mGeneralSkin);
	scrollPane.setForceScroll(true, true);
	scrollPane.setFadeScrollBars(false);
	mMainTable.add(scrollPane);
}

/**
 * Initialize list style
 */
private void initList() {
	String[] items = new String[10];

	for (int i = 0; i < items.length; ++i) {
		items[i] = "List item " + (i + 1);
	}

	List<String> list = new List<String>(mGeneralSkin);
	list.setItems(items);
	ScrollPane scrollPane = new ScrollPane(list, mGeneralSkin, "background");
	mMainTable.add(scrollPane);
}

/**
 * Initialize selection box
 */
private void initSelectionBox() {
	String[] items = new String[40];

	for (int i = 0; i < items.length; ++i) {
		items[i] = "Select item " + (i + 1);
	}

	SelectBox<String> selectBox = new SelectBox<String>(mGeneralSkin);
	selectBox.setItems(items);
	mMainTable.add(selectBox);
}

/**
 * Initialize wait animation
 */
private void initWaitAnimation() {
	AnimationWidgetStyle animationWidgetStyle = SkinNames.getResource(SkinNames.General.ANIMATION_WAIT);
	mAnimationWidget = new AnimationWidget(animationWidgetStyle);
	mTopRight.add(mAnimationWidget);
}

/**
 * Sets the loading bar to the specified value
 * @param loadedValue the amount loaded
 */
void setLoadingBar(float loadedValue) {
	mLoadingBar.setValue(loadedValue);
}

/**
 * Sets the health bar to the specified value
 * @param health the health to set
 */
void setHealthBar(float health) {
	mHealthBar.setValue(health);
}
}
