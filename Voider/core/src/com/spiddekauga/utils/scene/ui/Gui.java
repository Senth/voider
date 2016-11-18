package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.EventBus;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.ui.UiFactory;

import java.util.ArrayList;

/**
 * Base class for all GUI containing windows
 */
public abstract class Gui {
protected static final UiFactory mUiFactory = UiFactory.getInstance();
private static final EventBus mEventBus = EventBus.getInstance();
private static Gui mActiveGui = null;
protected AlignTable mMainTable = new AlignTable();
protected boolean mInitialized = false;
protected NotificationShower mNotification = null;
private boolean mDisposeAfterResize = false;
private boolean mIsResizing = false;
private boolean mVisible = true;
private Stage mStage = null;
private InnerWidgets mWidgets = new InnerWidgets();
private DialogShower mDialogShower = null;

/**
 * Default constructor
 */
public Gui() {
	mDialogShower = new DialogShower(this);
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

/**
 * @return the current active GUI
 */
static Gui getActiveGui() {
	return mActiveGui;
}

DialogShower getDialogShower() {
	return mDialogShower;
}

/**
 * Remove all message boxes
 */
public void removeAllMsgBoxes() {
	mDialogShower.removeAllMsgBoxes();
}

/**
 * @return true if an dialog is active
 */
public boolean isDialogActive() {
	return mDialogShower.isActive();
}

/**
 * Resume the GUI. Will call {@link #onResume()}.
 */
void resume() {
	onResume();
	mActiveGui = this;
	mEventBus.post(new GuiEvent(GuiEvent.EventTypes.RESUME, this));
}

/**
 * Called when the GUI is resumed. {@link Scene} instances should call {@link #resume()} instead of
 * calling this method directly.
 */
protected void onResume() {
	resetValues();
}

/**
 * Resets the value of the GUI
 */
public void resetValues() {
}

/**
 * Pause the GUI. Will call {@link #onPause()}.
 */
void pause() {
	onPause();
	mEventBus.post(new GuiEvent(GuiEvent.EventTypes.PAUSE, this));
}

/**
 * Called when the GUI is paused. Called before the next GUI is activated. {@link Scene} instances
 * should call {@link #pause()} instead of calling this method directly.
 */
protected void onPause() {
}

/**
 * Destroy the GUI instance. Will call {@link #onDestroy()}.
 */
void destroy() {
	onDestroy();
	mEventBus.post(new GuiEvent(GuiEvent.EventTypes.DESTROY, this));
	mActiveGui = null;
}

/**
 * Called when the GUI should be destroyed and not used anymore. {@link Scene} instances should call
 * {@link #destroy()} instead of calling this method directly.
 */
protected void onDestroy() {
	if (mStage != null) {
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

	mDialogShower.dispose();

	mInitialized = false;
}

/**
 * Resize the GUI object to the appropriate size
 * @param width new width of the GUI
 * @param height new height of the GUI
 */
protected void resize(int width, int height) {
	if (isInitialized()) {
		mIsResizing = true;
		if (mStage != null) {
			mStage.getViewport().setWorldSize(width, height);
			mStage.getViewport().update(width, height, true);
			updateBackground();
		}
		if (isDisposedAfterResize()) {
			onDestroy();
			onCreate();
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
}

/**
 * Create the GUI instance. Will call {@link #onCreate()}.
 */
void create() {
	onCreate();
	mEventBus.post(new GuiEvent(GuiEvent.EventTypes.CREATE, this));
}

/**
 * Called first time the GUI is activated/created. {@link Scene} instances should call {@link
 * #create()} instead of calling this method directly.
 */
protected void onCreate() {
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
	}

	mInitialized = true;

	resetValues();
}


/**
 * @return true if the GUI has been initialized
 */
public boolean isInitialized() {
	return mInitialized;
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
	Background background = new Background();

	static class Background {
		TextureRegionDrawable drawable = null;
		boolean wrap = false;
		ArrayList<Image> images = new ArrayList<>();
	}
}
}
