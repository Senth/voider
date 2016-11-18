package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.ui.UiFactory;

/**
 * Progress bar, both as Spinner and Horizontal bar.
 */
public class ProgressBar extends Window implements IDialog {
private static final ProgressBar mInstance = new ProgressBar();
private static final float mFadeInTime;
private static final float mFadeOutTime;

static {
	mFadeInTime = SkinNames.getResource(SkinNames.GeneralVars.PROGRESSBAR_FADE_IN);
	mFadeOutTime = SkinNames.getResource(SkinNames.GeneralVars.PROGRESSBAR_FADE_OUT);
}

private Label mProgressLabel;
private Slider mHorizontalSlider;
private Label mMessageHorizontal;
private Label mMessageSpinner;
private AlignTable mSpinnerTable = new AlignTable();
private AlignTable mHorizontalTable = new AlignTable();
private HideManual mSpinnerShower = new HideManual();
private HideManual mHorizontalShower = new HideManual();

/**
 * Protected constructor to singleton pattern
 */
protected ProgressBar() {
	super("", (Window.WindowStyle) SkinNames.getResource(SkinNames.General.WINDOW_MODAL));

	UiFactory uiFactory = UiFactory.getInstance();
	float paddingInner = uiFactory.getStyles().vars.paddingInner;
	float paddingSeparator = uiFactory.getStyles().vars.paddingSeparator;

	setModal(true);
	setSkin((Skin) ResourceCacheFacade.get(InternalDeps.UI_GENERAL));

	// Spinner
	mSpinnerTable = new AlignTable();
	mSpinnerShower.addToggleActor(mSpinnerTable);

	// Spinner animation
	AnimationWidget animationWidget = new AnimationWidget((AnimationWidget.AnimationWidgetStyle) SkinNames.getResource(SkinNames.General.ANIMATION_WAIT));
	mSpinnerTable.add(animationWidget).setPadRight(paddingSeparator);

	// Message
	mMessageSpinner = uiFactory.text.create("");
	mSpinnerTable.add(mMessageSpinner);


	// Horizontal (Progress) Bar
	mHorizontalTable = new AlignTable();
	mHorizontalShower.addToggleActor(mHorizontalTable);

	// Message
	Align align = new Align(Align.Horizontal.CENTER, Align.Vertical.MIDDLE);
	mHorizontalTable.row(align);
	mMessageHorizontal = uiFactory.text.create("");
	mHorizontalTable.add(mMessageHorizontal);

	// Progress bar
	mHorizontalTable.row(align).setPadTop(paddingInner);
	mHorizontalSlider = new Slider(0, 100, 0.1f, false, (Slider.SliderStyle) SkinNames.getResource(SkinNames.General.SLIDER_LOADING_BAR));
	mHorizontalSlider.setTouchable(Touchable.disabled);
	mHorizontalTable.add(mHorizontalSlider);

	// Progress label
	mHorizontalTable.row(align).setPadTop(paddingInner);
	mProgressLabel = uiFactory.text.create("");
	mHorizontalTable.add(mProgressLabel);
	mProgressLabel.pack();


	// Window
	add(mSpinnerTable, mHorizontalTable);
	pack();
}

/**
 * Show a horizontal progress bar. The progress text is set to 0% by default.
 * @param message the message to show above the progress bar
 */
public static void showProgress(String message) {
	showProgress(message, "0 %");
}

/**
 * Show a horizontal progress bar
 * @param message the message to show above the progress bar
 * @param progressText the text to display the progress of the bar.
 */
public static void showProgress(String message, String progressText) {
	mInstance.mSpinnerShower.hide();
	mInstance.mHorizontalShower.show();

	mInstance.mMessageHorizontal.setText(message);
	mInstance.mMessageHorizontal.pack();

	String displayText = progressText;
	if (displayText == null) {
		displayText = "0 %";
	}

	mInstance.mProgressLabel.setText(displayText);
	mInstance.mProgressLabel.pack();
	mInstance.mHorizontalSlider.setValue(0);

	mInstance.pad(UiFactory.getInstance().getStyles().vars.paddingInner);

	DialogShower.show(mInstance);
}

/**
 * Update the progress of the progress bar. Automatically sets the progress text below the progress
 * bar to the percentage that has been completed. E.g. "50 %"
 * @param percentage percentage that has been completed
 */
public static void updateProgress(float percentage) {
	updateProgress(percentage, null);
}

/**
 * Update the progress of the progress bar
 * @param percent percentage that has been completed
 * @param progressText the text to be displayed below the progress bar. Set to null to display the
 * percentage completed.
 */
public static void updateProgress(float percent, String progressText) {
	String displayText = progressText;

	if (progressText == null) {
		displayText = Integer.toString((int) percent) + " %";
	}

	mInstance.mProgressLabel.setText(displayText);
	mInstance.mHorizontalSlider.setValue(percent);
}

/**
 * Show a spinner progress bar
 * @param message the message to show beside the spinner
 */
public static void showSpinner(String message) {
	mInstance.mSpinnerShower.show();
	mInstance.mHorizontalShower.hide();

	mInstance.mMessageSpinner.setText(message);
	mInstance.mMessageSpinner.pack();

	mInstance.padRight(UiFactory.getInstance().getStyles().vars.paddingSeparator);

	DialogShower.show(mInstance);
}

/**
 * Hide the progress bar. Works for both spinner and horizontal bar
 */
public static void hide() {
	mInstance.addAction(Actions.sequence(
			Actions.fadeOut(mFadeOutTime, Interpolation.linear),
			new DialogEvent.PostEventAction(DialogEvent.EventTypes.REMOVE),
			Actions.removeActor()
	));
}

/**
 * Show the progress bar
 */
void show(Stage stage) {
	clearActions();

	// Add to stage
	if (getStage() != stage) {
		stage.addActor(this);
		stage.setKeyboardFocus(this);
		stage.setScrollFocus(this);
	}

	// Fade in
	addAction(Actions.sequence(
			Actions.fadeIn(mFadeInTime, Interpolation.linear),
			new DialogEvent.PostEventAction(DialogEvent.EventTypes.SHOW)
	));

	mInstance.mHorizontalTable.layout();
	mInstance.mSpinnerTable.layout();
	pack();
	centerWindow();
}

/**
 * Center the window
 */
private void centerWindow() {
	int xPosition = (int) ((Gdx.graphics.getWidth() - getWidth()) * 0.5f);
	int yPosition = (int) ((Gdx.graphics.getHeight() - getHeight()) * 0.5f);
	setPosition(xPosition, yPosition);
}
}
