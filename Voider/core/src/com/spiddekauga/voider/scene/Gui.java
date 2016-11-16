package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.AnimationWidget;
import com.spiddekauga.utils.scene.ui.AnimationWidget.AnimationWidgetStyle;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.NotificationShower;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * Base class for all GUI containing windows
 */
public abstract class Gui implements Disposable {
/** UI Factory for creating UI elements */
protected static UiFactory mUiFactory = UiFactory.getInstance();
/** Main table for the layout */
protected AlignTable mMainTable = new AlignTable();
/** True if the GUI has been initialized */
protected boolean mInitialized = false;
/** Notification messages */
protected NotificationShower mNotification = null;
private boolean mDisposeAfterResize = false;
private boolean mIsResizing = false;
/** If the GUI is visible */
private boolean mVisible = true;
/** Stage for the GUI */
private Stage mStage = null;
/** Active message boxes */
private Stack<MsgBoxExecuter> mActiveMsgBoxes = new Stack<>();
/** Various widgets */
private InnerWidgets mWidgets = new InnerWidgets();

/**
 * Default constructor
 */
public Gui() {
	// Does nothing
}

/**
 * Checks if a button is checked (from the event).
 * @param event checks if the target inside the event is a button and it's checked
 * @return checked button. If the target isn't a button or the button isn't checked it returns null.
 */
protected static Button getCheckedButton(Event event) {
	if (event.getTarget() instanceof Button) {
		Button button = (Button) event.getTarget();
		if (button.isChecked()) {
			return button;
		}
	}
	return null;
}

@Override
public void dispose() {
	if (mStage != null) {
		// Remove notification shower
		if (mNotification != null && mNotification.getStage() == mStage) {
			mNotification.setStage(null);
		}

		ArrayList<Actor> actorsToRemove = new ArrayList<>();
		for (Actor actor : mStage.getRoot().getChildren()) {
			if (actor instanceof AlignTable) {
				((AlignTable) actor).dispose(true);
			} else if (actor instanceof Disposable) {
				((Disposable) actor).dispose();
			} else {
				actorsToRemove.add(actor);
			}
		}
		for (Actor actor : actorsToRemove) {
			actor.remove();
		}
	}

	mInitialized = false;
}

/**
 * Resizes the GUI object to the appropriate size
 * @param width new width of the GUI
 * @param height new height of the GUI
 */
public void resize(int width, int height) {
	if (isInitialized()) {
		mIsResizing = true;
		if (mStage != null) {
			mStage.getViewport().setWorldSize(width, height);
			mStage.getViewport().update(width, height, true);
			updateBackground();
		}
		if (isDisposedAfterResize()) {
			dispose();
			initGui();
			resetValues();
		} else {
			invalidateAllActors();
			onResize(width, height);
		}
		mIsResizing = false;
	}
}

/**
 * Called whenever the UI has been resized, but only if {@link #isDisposedAfterResize()} returns
 * false.
 * @param width new window width
 * @param height new window height
 */
protected void onResize(int width, int height) {
	// Does nothing
}

/**
 * Invalidate all actors
 */
protected void invalidateAllActors() {
	if (mStage != null) {
		invalidateAllActors(mStage.getRoot());
	}
}

/**
 * Invalidate all actors in the group and all children (recursive)
 * @param group invalidate all actors in this group and all its children
 */
protected void invalidateAllActors(Group group) {
	for (Actor actor : group.getChildren()) {
		if (actor instanceof Layout) {
			((Layout) actor).invalidate();
		}

		if (actor instanceof Group) {
			invalidateAllActors((Group) actor);
		}
	}
}

/**
 * @return true if you want the UI to be disposed and reinitialized after a window resize
 */
protected boolean isDisposedAfterResize() {
	return mDisposeAfterResize;
}

/**
 * Set if the UI should be disposed and reinitialized after a window resize
 * @param disposeAfterResize true if the UI should be disposed and reinitialized after a window
 * resize.
 */
protected void setDisposeAfterResize(boolean disposeAfterResize) {
	mDisposeAfterResize = disposeAfterResize;
}

/**
 * Sets a background for this GUI
 * @param imageName a background image
 * @param wrap true if the background should wrapped
 */
protected void setBackground(IImageNames imageName, boolean wrap) {
	Drawable drawable = SkinNames.getDrawable(imageName);

	if (drawable instanceof TextureRegionDrawable) {
		mWidgets.background.drawable = (TextureRegionDrawable) drawable;
		mWidgets.background.wrap = wrap;
	} else {
		mWidgets.background.drawable = null;
	}
	updateBackground();
}

/**
 * Update background images. Should be called when setting a new background or resizing the window
 */
private void updateBackground() {
	if (mWidgets.background.drawable == null) {
		return;
	}

	// Remove and clear old images
	for (Image image : mWidgets.background.images) {
		image.remove();
	}
	mWidgets.background.images.clear();

	if (mWidgets.background.wrap) {
		updateWrappedBackground();
	} else {
		Image image = new Image(mWidgets.background.drawable);
		mWidgets.background.images.add(image);
		image.setPosition(0, 0);
		image.setWidth(Gdx.graphics.getWidth());
		image.setHeight(Gdx.graphics.getHeight());
		addActor(image);
		image.setZIndex(0);
	}
}

/**
 * Update wrapped (thus centered) background images
 */
private void updateWrappedBackground() {
	// Calculate where the centered image should start
	float backgroundHeight = mWidgets.background.drawable.getRegion().getRegionHeight();
	float centerYStart = (int) ((Gdx.graphics.getHeight() / 2) - (backgroundHeight / 2));

	// Repeat above
	int cRepeatY = 1;
	float diffHeight = Gdx.graphics.getHeight() - centerYStart - backgroundHeight;
	if (diffHeight > 0) {
		float yTimes = diffHeight / backgroundHeight;
		int tempRepeat = (int) yTimes;
		if (yTimes - tempRepeat != 0f) {
			tempRepeat++;
		}
		cRepeatY += tempRepeat;
	}

	// Repeat below
	diffHeight = centerYStart;
	float lowYStart = centerYStart;
	if (diffHeight > 0) {
		float yTimes = diffHeight / backgroundHeight;
		int tempRepeat = (int) yTimes;
		if (yTimes - tempRepeat != 0f) {
			tempRepeat++;
		}
		cRepeatY += tempRepeat;
		lowYStart -= tempRepeat * backgroundHeight;
	}

	// Repeat right
	int cRepeatX = 1;
	float backgroundWidth = mWidgets.background.drawable.getRegion().getRegionWidth();
	float xTimes = Gdx.graphics.getWidth() / backgroundWidth;
	cRepeatX = (int) xTimes;
	if (xTimes - cRepeatX != 0f) {
		cRepeatX++;
	}


	// Allocate images in the correct position
	for (int x = 0; x < cRepeatX; ++x) {
		for (int y = 0; y < cRepeatY; ++y) {
			Image image = new Image(mWidgets.background.drawable);
			mWidgets.background.images.add(image);
			image.setPosition(x * backgroundWidth, lowYStart + (y * backgroundHeight));
			addActor(image);
			image.setZIndex(0);
		}
	}
}

/**
 * Adds additional actors to the GUI
 * @param actor is added to the stage
 */
public void addActor(Actor actor) {
	mStage.addActor(actor);
}

/**
 * Resets the GUI and adds the main table again. This will remove any actors that have been added
 * manually through #addActor(Actor)
 */
public void reset() {
	mStage.clear();
	mStage.addActor(mMainTable);
}

/**
 * Updates the GUI
 */
public void update() {
	if (mNotification != null) {
		mNotification.pushToFront();
	}

	// Remove active message box if it has been hidden
	if (!mActiveMsgBoxes.isEmpty() && !isWaitOrProgressShowing()) {
		MsgBoxExecuter activeMsgBox = mActiveMsgBoxes.peek();

		// If the active message box was hidden. Show the previous one
		if (activeMsgBox.isHidden()) {
			mActiveMsgBoxes.pop();
			mUiFactory.msgBox.free(activeMsgBox);

			// Show the previous message box if one exists
			if (!mActiveMsgBoxes.isEmpty()) {
				mActiveMsgBoxes.peek().show(mStage);
			}
		}
		// Fade in the message box as wait or progress window has been hidden
		else if (!activeMsgBox.isVisible() && activeMsgBox.getActions().size == 0) {
			fadeInMessageBox();
		}
	}

	if (mWidgets.waitWindow.window != null && mWidgets.waitWindow.window.getStage() != null) {
		mWidgets.waitWindow.animation.act(Gdx.graphics.getDeltaTime());
	}
}

/**
 * @return true if either wait window or progress bar is showing
 */
private boolean isWaitOrProgressShowing() {
	return (mWidgets.waitWindow.window != null && mWidgets.waitWindow.window.getStage() != null)
			|| (mWidgets.progressBar.window != null && mWidgets.progressBar.window.getStage() != null);
}

/**
 * Fade in message box after wait window or progress bar is removed.
 */
private void fadeInMessageBox() {
	if (!mActiveMsgBoxes.isEmpty()) {
		float waitDelay = Config.Gui.MSG_BOX_SHOW_WAIT_TIME;
		final MsgBoxExecuter msgBox = mActiveMsgBoxes.peek();
		msgBox.addAction(Actions.sequence(Actions.delay(waitDelay), Actions.show(), MsgBoxExecuter.fadeInActionDefault()));
	}
}

/**
 * Remove all active message boxes
 */
public void popMsgBoxes() {
	if (mActiveMsgBoxes.size() >= 2) {
		// Only the latest message box is shown, i.e. just remove all except the
		// last message box which we hide.
		Iterator<MsgBoxExecuter> msgBoxIt = mActiveMsgBoxes.iterator();
		while (msgBoxIt.hasNext()) {
			MsgBoxExecuter msgBox = msgBoxIt.next();
			msgBoxIt.remove();
			mUiFactory.msgBox.free(msgBox);

			if (!msgBox.isHidden()) {
				msgBox.hide();
			}
		}
	} else if (mActiveMsgBoxes.size() == 1) {
		MsgBoxExecuter msgBox = mActiveMsgBoxes.pop();
		mUiFactory.msgBox.free(msgBox);
		msgBox.hide();
	}
}

/**
 * Pop/Remove active message box. Will activate the previous message box if one is available
 */
public void popMsgBoxActive() {
	if (!mActiveMsgBoxes.isEmpty()) {
		MsgBoxExecuter msgBox = mActiveMsgBoxes.pop();
		msgBox.hide();
		mUiFactory.msgBox.free(msgBox);

		// Show previous
		if (!mActiveMsgBoxes.isEmpty()) {
			mActiveMsgBoxes.peek().clearActions();
			mActiveMsgBoxes.peek().show(mStage);
		}
	}
}

/**
 * Shows the specified message box. This will hide any active message box, remove the specified
 * message box from the inactive list, and then show the specified active box (once the currently
 * active box has been fully hidden).
 * @param msgBox the message box to show
 */
public void showMsgBox(MsgBoxExecuter msgBox) {
	// Progress bar or wait window is showing
	if (isWaitOrProgressShowing()) {
		mActiveMsgBoxes.push(msgBox);
		msgBox.setVisible(false);
	} else {
		// Hide active
		if (!mActiveMsgBoxes.isEmpty()) {
			mActiveMsgBoxes.peek().hide();
		}
		msgBox.setVisible(true);
		mActiveMsgBoxes.push(msgBox);
	}

	// Wait for the stage to be initialized
	while (mStage == null) {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
	}

	msgBox.show(mStage);
}

/**
 * Show wait window
 * @param message optional message to display
 */
public synchronized void showWaitWindow(String message) {
	if (mWidgets.waitWindow.window == null) {
		return;
	}

	// Hide message box
	hideMsgBoxActive();

	// Show window if it doesn't belong to a stage
	if (mWidgets.waitWindow.window.getStage() == null) {

		mWidgets.waitWindow.animation.reset();

		// Set text or empty if null
		setWaitWindowText(message);
		mStage.addActor(mWidgets.waitWindow.window);

		// Center
		centerWindow(mWidgets.waitWindow.window);

		float fadeInDuration = (Float) SkinNames.getResource(SkinNames.GeneralVars.WAIT_WINDOW_FADE_IN);
		mWidgets.waitWindow.window.addAction(Actions.fadeIn(fadeInDuration, Interpolation.fade));
	}
	// Else just change the text
	else {
		setWaitWindowText(message);
	}
}

/**
 * Hide the current message box by making it invisible. This also clear any actions of the message
 * box
 */
private void hideMsgBoxActive() {
	if (!mActiveMsgBoxes.isEmpty()) {
		MsgBoxExecuter msgBox = mActiveMsgBoxes.peek();
		msgBox.clearActions();
		msgBox.setVisible(false);
	}
}

/**
 * Sets the wait text
 * @param message the wait message to set, null will set it to empty
 */
public synchronized void setWaitWindowText(String message) {
	mWidgets.waitWindow.label.setText(message != null ? message : "");
	mWidgets.waitWindow.window.pack();
}

private static void centerWindow(Window window) {
	int xPosition = (int) ((Gdx.graphics.getWidth() - window.getWidth()) * 0.5f);
	int yPosition = (int) ((Gdx.graphics.getHeight() - window.getHeight()) * 0.5f);
	window.setPosition(xPosition, yPosition);
}

/**
 * Hides the wait window. Does nothing if the wait window isn't shown
 */
public synchronized void hideWaitWindow() {
	if (mWidgets.waitWindow.window == null || mWidgets.waitWindow.window.getStage() == null) {
		return;
	}

	float fadeOutDuriation = (Float) SkinNames.getResource(SkinNames.GeneralVars.WAIT_WINDOW_FADE_OUT);
	mWidgets.waitWindow.window.addAction(Actions.sequence(Actions.fadeOut(fadeOutDuriation, Interpolation.fade), Actions.removeActor()));
}

/**
 * Shows the a progress bar for loading/downloading/uploading window
 * @param message the message to display
 */
public synchronized void showProgressBar(String message) {
	if (mWidgets.progressBar.window == null) {
		return;
	}

	// Hide message box
	hideMsgBoxActive();

	mWidgets.progressBar.window.clearActions();

	mStage.addActor(mWidgets.progressBar.window);
	mWidgets.progressBar.label.setText(message);
	updateProgressBar(0, "");
	mWidgets.progressBar.window.pack();
	mWidgets.progressBar.label.pack();
	mWidgets.progressBar.window.pack();

	float fadeInDuration = (Float) SkinNames.getResource(SkinNames.GeneralVars.WAIT_WINDOW_FADE_IN);
	mWidgets.progressBar.window.addAction(Actions.fadeIn(fadeInDuration, Interpolation.fade));

	centerWindow(mWidgets.progressBar.window);
}

/**
 * Updates the progress bar
 * @param percentage how many percentage that has been loaded
 * @param progressText optional message, keeps previous if null
 */
public synchronized void updateProgressBar(float percentage, String progressText) {
	if (progressText != null) {
		mWidgets.progressBar.progressLabel.setText(progressText);
	}
	mWidgets.progressBar.slider.setValue(percentage);
	mWidgets.progressBar.window.pack();

	centerWindow(mWidgets.progressBar.window);
}

/**
 * Hides the progress bar
 */
public synchronized void hideProgressBar() {
	if (mWidgets.progressBar.window == null || mWidgets.progressBar.window.getStage() == null) {
		return;
	}

	// Fade out
	float fadeOutDuriation = (Float) SkinNames.getResource(SkinNames.GeneralVars.WAIT_WINDOW_FADE_OUT);
	mWidgets.progressBar.window.clearActions();
	mWidgets.progressBar.window.addAction(Actions.sequence(Actions.fadeOut(fadeOutDuriation, Interpolation.fade), Actions.removeActor()));
}

/**
 * Updates the progress bar, doesn't set the text
 * @param percentage how many percentage that has been loaded
 */
public synchronized void updateProgressBar(float percentage) {
	updateProgressBar(percentage, null);
}

/**
 * Initializes the GUI
 */
public void initGui() {
	if (mStage == null) {
		mStage = new Stage();
		mStage.addActor(mMainTable);
		mMainTable.setName("MainTable");
		mMainTable.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
	}

	if (!mUiFactory.isInitialized() && ResourceCacheFacade.isLoaded(InternalDeps.UI_GENERAL)) {
		mUiFactory.init();
	}

	if (ResourceCacheFacade.isLoaded(InternalDeps.UI_GENERAL)) {
		// Notification messages
		if (!mIsResizing) {
			mNotification = NotificationShower.getInstance();
		}
		// Wait Window
		if (mWidgets.waitWindow.window == null) {
			initWaitWindow();
		}
		// Loading progress bar
		if (mWidgets.progressBar.window == null) {
			initProgressBar();
		}
	}

	mInitialized = true;
}

/**
 * Initialize wait window
 */
private void initWaitWindow() {
	mWidgets.waitWindow.window = new Window("", (WindowStyle) SkinNames.getResource(SkinNames.General.WINDOW_MODAL));
	mWidgets.waitWindow.window.setModal(true);
	mWidgets.waitWindow.window.setSkin((Skin) ResourceCacheFacade.get(InternalDeps.UI_GENERAL));
	mWidgets.waitWindow.animation = new AnimationWidget((AnimationWidgetStyle) SkinNames.getResource(SkinNames.General.ANIMATION_WAIT));
	mWidgets.waitWindow.label = new Label("", LabelStyles.DEFAULT.getStyle());
	mWidgets.waitWindow.window.add(mWidgets.waitWindow.animation).padRight(mUiFactory.getStyles().vars.paddingSeparator);
	mWidgets.waitWindow.window.add(mWidgets.waitWindow.label).padRight(mUiFactory.getStyles().vars.paddingInner);
}

/**
 * Initialize progress bar
 */
private void initProgressBar() {
	Window window = new Window("", (WindowStyle) SkinNames.getResource(SkinNames.General.WINDOW_MODAL));
	mWidgets.progressBar.window = window;
	window.setModal(true);
	window.align(Align.center);

	window.pad(mUiFactory.getStyles().vars.paddingInner);

	// Text
	mWidgets.progressBar.label = mUiFactory.text.create("");
	window.row();
	window.add(mWidgets.progressBar.label);

	// Progress bar
	window.row().padTop(mUiFactory.getStyles().vars.paddingInner);
	mWidgets.progressBar.slider = new Slider(0, 100, 0.1f, false, (SliderStyle) SkinNames.getResource(SkinNames.General.SLIDER_LOADING_BAR));
	mWidgets.progressBar.slider.setTouchable(Touchable.disabled);
	window.add(mWidgets.progressBar.slider);

	// Progress text
	window.row().padTop(mUiFactory.getStyles().vars.paddingInner);
	mWidgets.progressBar.progressLabel = mUiFactory.text.create("");
	window.add(mWidgets.progressBar.progressLabel);
	mWidgets.progressBar.label.pack();
	window.pack();
}

/**
 * @return true if the GUI has been initialized
 */
public boolean isInitialized() {
	return mInitialized;
}

/**
 * Resets the value of the GUI
 */
public void resetValues() {
}

/**
 * Renders the GUI
 */
public final synchronized void render() {
	mStage.act(Gdx.graphics.getDeltaTime());
	mStage.draw();
}

/**
 * @return scene of this GUI
 */
public Stage getStage() {
	return mStage;
}

/**
 * @return true if a message box is currently shown
 */
public boolean isMsgBoxActive() {
	return !mActiveMsgBoxes.isEmpty();
}

/**
 * @return true if wait window is active
 */
public boolean isWaitWindowActive() {
	return mWidgets.waitWindow.window != null && mWidgets.waitWindow.window.getStage() != null;
}

/**
 * @return true if the GUI is visible. I.e. should be drawn.
 */
public boolean isVisible() {
	return mVisible;
}

/**
 * Set the GUI as visible/invisible. I.e. if the GUI should be drawn or not.
 * @param visible set to true for visible, false for invisible.
 */
public void setVisible(boolean visible) {
	mVisible = visible;
}

/** Inner widgets */
private static class InnerWidgets {
	WaitWindow waitWindow = new WaitWindow();
	ProgressBar progressBar = new ProgressBar();
	Background background = new Background();

	static class Background {
		TextureRegionDrawable drawable = null;
		boolean wrap = false;
		ArrayList<Image> images = new ArrayList<>();
	}

	static class WaitWindow {
		Window window = null;
		Label label = null;
		AnimationWidget animation = null;
	}

	static class ProgressBar {
		Slider slider = null;
		Window window = null;
		Label progressLabel = null;
		Label label = null;

	}
}
}
