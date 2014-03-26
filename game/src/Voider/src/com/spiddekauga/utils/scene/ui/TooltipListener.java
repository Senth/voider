package com.spiddekauga.utils.scene.ui;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;


/**
 * Listens to a GUI actor to display a tooltip for it.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TooltipListener extends InputAdapter implements EventListener {
	/**
	 * Creates a tooltip listener that will listen to the specified
	 * actor. This will automatically adds itself as a listener to
	 * the actor.
	 * @param actor the GUI actor to listen to
	 * @param message the message in the tooltip
	 */
	public TooltipListener(Actor actor, String message) {
		this(actor, message, (String)null);
	}

	/**
	 * Creates a tooltip listener that will listen to the specified actor.
	 * Automatically adds itself as a listener to the actor. In addition to showing a
	 * message a YouTube button will be displayed to link to the tutorial.
	 * @param actor the GUI actor to listen to
	 * @param message optional text message, set to null if not used.
	 * @param youtubeUrl optional tutorial URL, set to null if not used.
	 */
	public TooltipListener(Actor actor, String message, String youtubeUrl) {
		mMessage = message;
		mActor = actor;
		mYoutubeUrl = youtubeUrl;
		mActor.addListener(this);

		mGui = SceneSwitcher.getGui();

		if (mWindow == null) {
			Skin uiSkin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);

			mWindow = new Window("", uiSkin);
			mWindow.setModal(false);
			mMessageLabel = new Label("", uiSkin);
			mMessageLabel.setWrap(true);
			//			mDescriptiveLabel = new Label("", uiSkin);
			mTable = new AlignTable();
			mWindow.add(mTable);

			float leftRightPadding = uiSkin.get(SkinNames.GeneralVars.PADDING_WINDOW_LEFT_RIGHT.toString(), Float.class);
			mWindowLeftRightMargin = leftRightPadding;
			mTable.setPaddingRowDefault(0, leftRightPadding, leftRightPadding, leftRightPadding);
			mTable.setAlignTable(Horizontal.LEFT, Vertical.MIDDLE);
			mTable.setAlignRow(Horizontal.LEFT, Vertical.TOP);
		}

		mcTooltips++;
		mWindowName = String.valueOf(mcTooltips);

		mWindow.addListener(this);
	}

	/**
	 * Creates a tooltip listener that will listen to the specified actor.
	 * This will automatically adds itself as a listener to the actor.
	 * Shows an animation as a tooltip.
	 * @param actor the GUI actor to listen to
	 * @param title the title of the tooltip window
	 * @param animation the animation to show as a tooltip
	 */
	public TooltipListener(Actor actor, String title, AnimationWidget animation) {
		this(actor, title, animation, null, null);
	}

	/**
	 * Creates a tooltip listener that will listen to the specified actor.
	 * This will automatically adds itself as a listener to the actor.
	 * Shows an animation as a tooltip and a message.
	 * @param actor the GUI actor to listen to
	 * @param title the title of the tooltip window
	 * @param animation the animation to show as a tooltip
	 * @param message the text message to show in addition to the animation
	 */
	public TooltipListener(Actor actor, String title, AnimationWidget animation, String message) {
		this(actor, title, animation, message, null);
	}

	/**
	 * Creates a tooltip listener that will listen to the specified actor.
	 * This will automatically adds itself as a listener to the actor.
	 * Shows an animation as a tooltip and a message.
	 * @param actor the GUI actor to listen to
	 * @param title the title of the tooltip window
	 * @param animation the animation to show as a tooltip
	 * @param message the text message to show in addition to the animation
	 * @param youtubeUrl the YouTube URL to a tutorial how to use the tool
	 */
	public TooltipListener(Actor actor, String title, AnimationWidget animation, String message, String youtubeUrl) {
		this(actor, message, youtubeUrl);

		mAnimation = animation;

		//		if (mAnimation != null) {
		//			mDescriptiveText = "A = Toggle animation\n" + mDescriptiveText;
		//		}
	}

	/**
	 * Creates a tooltip listener that will listen to the specified actor.
	 * This will automatically adds itself as a listener to the actor.
	 * Shows an image as a tooltip.
	 * @param actor the GUI actor to listen to
	 * @param title the title of the tooltip window
	 * @param image the image to show as a tooltip
	 */
	public TooltipListener(Actor actor, String title, Image image) {
		this(actor, title, image, null, null);
	}

	/**
	 * Creates a tooltip listener that will listen to the specified actor.
	 * This will automatically adds itself as a listener to the actor.
	 * Shows an image as a tooltip and a message.
	 * @param actor the GUI actor to listen to
	 * @param title the title of the tooltip window
	 * @param image the image to show as a tooltip
	 * @param message the text message to show in addition to the image
	 */
	public TooltipListener(Actor actor, String title, Image image, String message) {
		this(actor, title, image, message, null);
	}

	/**
	 * Creates a tooltip listener that will listen to the specified actor.
	 * This will automatically adds itself as a listener to the actor.
	 * Shows an image as a tooltip and a message.
	 * @param actor the GUI actor to listen to
	 * @param title the title of the tooltip window
	 * @param image the image to show as a tooltip
	 * @param message the text message to show in addition to the image
	 * @param youtubeUrl the YouTube URL to a tutorial how to use the tool
	 */
	public TooltipListener(Actor actor, String title, Image image, String message, String youtubeUrl) {
		this(actor, message, youtubeUrl);

		mImage = image;
	}

	/**
	 * @return true if the message box tooltip is currently active
	 */
	public boolean isMsgBoxActive() {
		return !(mMsgBox == null || mMsgBox.isHidden());
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;

			// WINDOW (Hover events)
			if (inputEvent.getType() == Type.enter) {
				if (!isWindowDisplayingThis() && mActor.isAscendantOf(event.getTarget())) {
					scheduleShowWindowTask();
					return true;
				}
			} else if (inputEvent.getType() == Type.exit) {
				// Only do something if the cursor is outside the actor
				if ((isWindowDisplayingThis() || isWindowScheduled()) && !isCursorInsideActor()) {
					handleHoverExit();
					return true;
				}

			} else if (inputEvent.getType() == Type.mouseMoved) {
				// Update position if just moved
				if (isWindowDisplayingThis()) {
					updateWindowPosition();

					// Remove window, we're outside of the actor
					if (!isCursorInsideActor()) {
						handleHoverExit();
					}
					return true;
				}
				// Reset shown window
				else if (mShowWindowTask != null) {
					cancelShowWindowTask();
					scheduleShowWindowTask();
					return true;
				}
			}

			// MSG BOX (Press events)
			else if (inputEvent.getType() == Type.touchDown) {
				// Always skip if window is shown or scheduled to be shown
				if (!isWindowShown() && mShowWindowTask == null) {
					scheduleShowMsgBoxTask();
				}
				return true;
			}
			else if (inputEvent.getType() == Type.touchUp) {
				cancelShowMsgBoxTask();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		// Next Animation
		if (mAnimation != null && KeyHelper.Tooltip.isNextAnimationPressed(keycode)) {
			nextAnimation();
			return true;
		}
		// YouTube
		else if (mYoutubeUrl != null && KeyHelper.Tooltip.isShowYoutubePressed(keycode)) {
			openYoutubeUrl();
			return true;
		}
		// Toggle animation / image
		else if ((mAnimation != null || mImage != null) && KeyHelper.Tooltip.isToggleAnimationPressed(keycode)) {
			toggleAnimationOrImage();
			return true;
		}

		return false;
	}

	/**
	 * Open the YouTube URL
	 */
	private void openYoutubeUrl() {
		Gdx.app.getNet().openURI(mYoutubeUrl);
	}

	/**
	 * Show next animation
	 */
	private void nextAnimation() {
		/** @todo show next animation */
	}

	/**
	 * Toggle animation or image
	 */
	private void toggleAnimationOrImage() {
		mShowImageOrAnimation = !mShowImageOrAnimation;
		updateWindowContent();
	}

	/**
	 * Creates and schedules a new show window task
	 */
	private void scheduleShowWindowTask() {
		if (mShowWindowTask == null) {
			mShowWindowTask = new ShowWindowTask();
			Timer.schedule(mShowWindowTask, Config.Gui.TOOLTIP_HOVER_SHOW);

			// Remove show message box if it has been scheduled
			cancelShowMsgBoxTask();
		}
	}

	/**
	 * Cancels the window task and sets it to null
	 */
	private void cancelShowWindowTask() {
		if (mShowWindowTask != null) {
			mShowWindowTask.cancel();
			mShowWindowTask = null;
		}
	}

	/**
	 * Creates and schedules a new show msg box task
	 */
	private void scheduleShowMsgBoxTask() {
		if (mShowMsgBoxTask == null) {
			mShowMsgBoxTask = new ShowMsgBoxTask();
			Timer.schedule(mShowMsgBoxTask, Config.Gui.TOOLTIP_PRESS_SHOW);
		}
	}

	/**
	 * Cancels the show msg box task and sets it to null
	 */
	private void cancelShowMsgBoxTask() {
		if (mShowMsgBoxTask != null) {
			mShowMsgBoxTask.cancel();
			mShowMsgBoxTask = null;
		}
	}

	/**
	 * Handles when the cursor moves out from the actor
	 */
	private void handleHoverExit() {
		// Fade out window, then remove it from stage if it is shown.
		if (mWindow.getStage() != null) {
			SceneSwitcher.removeListener(this);
			mWindow.addAction(Actions.sequence(Actions.fadeOut(Config.Gui.TOOLTIP_HOVER_FADE_DURATION, Interpolation.fade), Actions.removeActor()));
		}

		cancelShowWindowTask();
	}

	/**
	 * Updates the window position to the current cursor position. The position
	 * will be one of the corners of the window. The ordering is as follows
	 * \li Lower right corner
	 * \li Lower left corner
	 * \li Upper right corner
	 * \li Upper left corner
	 * If the window would be outside of the screen, it will chose a position with
	 * a lower priority until it finds one where the whole window is inside the screen.
	 */
	private static void updateWindowPosition() {
		if (mWindow.getStage() != null) {
			int windowWidth = (int) mWindow.getWidth();
			int windowHeight = (int) mWindow.getHeight();
			int screenWidth = Gdx.graphics.getWidth();
			int screenHeight = Gdx.graphics.getHeight();

			int cursorX = Gdx.input.getX();
			int cursorY = Gdx.input.getY();

			float leftXMissing = windowWidth - cursorX;
			float lowerYMissing = windowHeight - cursorY;
			float rightXMissing = cursorX + windowWidth - screenWidth;
			float upperYMissing = cursorY + windowHeight - screenHeight;

			// The tooltip fits the screen
			// Lower left corner
			if (leftXMissing <= 0 && lowerYMissing <= 0) {
				mWindow.setPosition(cursorX - windowWidth, screenHeight - cursorY);
			}
			// Lower right corner
			else if (rightXMissing <= 0 && lowerYMissing <= 0) {
				mWindow.setPosition(cursorX, screenHeight - cursorY);
			}
			// Upper left corner
			else if (leftXMissing <= 0 && upperYMissing <= 0) {
				mWindow.setPosition(cursorX - windowWidth, screenHeight - (cursorY + windowHeight + 1));
			}
			// Upper right corner
			else if (rightXMissing <= 0 && upperYMissing <= 0) {
				mWindow.setPosition(cursorX, screenHeight - (cursorY + windowHeight + 1));
			}
			// Tooltip doesn't fit the screen from this position
			else {

				// Left or right side
				int xPosition = 0;
				if (leftXMissing < rightXMissing) {
					xPosition = (int) (leftXMissing > 0 ? 0 : -leftXMissing);
				} else {
					xPosition = (int) (rightXMissing > 0 ? cursorX - rightXMissing : cursorX);
				}

				// Upper or lower
				int yPosition = 0;
				if (lowerYMissing < upperYMissing) {
					yPosition = lowerYMissing > 0 ? 0 : cursorY;
				} else {
					yPosition = (int) (upperYMissing > 0 ? cursorY - upperYMissing : cursorY + windowHeight + 1);
				}

				mWindow.setPosition(xPosition, screenHeight - yPosition);
			}
		}
	}

	/**
	 * @return true if the cursor is inside the current actor
	 */
	private boolean isCursorInsideActor() {
		int cursorX = Gdx.input.getX();
		int cursorY = Gdx.graphics.getHeight() - Gdx.input.getY();

		Vector2 min = Pools.vector2.obtain().set(0, 0);
		mActor.localToStageCoordinates(min);

		int width = (int)mActor.getWidth();
		int height = (int)mActor.getHeight();

		int minX = (int) (min.x);
		int minY = (int) (min.y);
		int maxX = minX + width;
		int maxY = minY + height;

		if (cursorX <= minX || cursorX >= maxX || cursorY <= minY || cursorY >= maxY) {
			return false;
		}

		return true;
	}

	/**
	 * Shows the window
	 */
	private void showWindow() {
		Stage stage = mGui.getStage();
		Scene scene = SceneSwitcher.getActiveScene(false);
		if (stage != null && scene.isInitialized()) {
			stage.addActor(mWindow);
			mWindow.clearActions();
			mWindow.setName(mWindowName);

			if (mImage != null || mAnimation != null) {
				mShowImageOrAnimation = true;
				mShowMessage = false;
			} else {
				mShowImageOrAnimation = false;
				mShowMessage = true;
			}

			updateWindowContent();

			mWindow.addAction(Actions.fadeIn(Config.Gui.TOOLTIP_HOVER_FADE_DURATION, Interpolation.fade));
			updateWindowPosition();
			cancelShowMsgBoxTask();

			SceneSwitcher.addListener(this);
		} else {
			Gdx.app.error("TooltipListener", "Stage is not when showing window!");
		}
	}

	/**
	 * Add content to the window
	 */
	private void updateWindowContent() {
		mTable.dispose(true);
		mMessageLabel.setText(mMessage);
		// Only show image
		if (mImage != null && mShowImageOrAnimation) {
			mTable.row().setAlign(Horizontal.CENTER, Vertical.TOP);
			mTable.add(mImage);
		}

		// Only show animation
		if (mAnimation != null && mShowImageOrAnimation) {
			mTable.row().setAlign(Horizontal.CENTER, Vertical.TOP);
			mTable.add(mAnimation);
		}

		// Show text
		if (mShowMessage && mMessage.length() > 0) {
			mTable.row();
			mTable.add(mMessageLabel);
		}

		//		// Add descriptive text
		//		mDescriptiveLabel.setText(mDescriptiveText);
		//		mTable.row().setPadTop(separatorPadding);
		//		mTable.add(mDescriptiveLabel);

		setWrapWidth();
		mTable.layout();
		mWindow.pack();
	}

	/**
	 * Shows the message box
	 */
	private void showMsgBox() {
		//		mMsgBox = mGui.getFreeMsgBox(mTitle != null && mTitle.length() > 0);
		//		mMsgBox.setTitle(mTitle);
		mMsgBox = mGui.getFreeMsgBox(false);
		mMsgBox.addCancelButtonAndKeys("OK");
		mMessageLabel.setText(mMessage);
		setWrapWidth();
		mMsgBox.content(mMessageLabel);
		mGui.showMsgBox(mMsgBox);
	}

	/**
	 * Sets the wrap width of the current message
	 */
	private void setWrapWidth() {
		mMessageLabel.setWrap(false);
		int prefWidth = (int) mMessageLabel.getPrefWidth();
		int prefHeight = (int) mMessageLabel.getPrefHeight();

		int wrapWidth = (int) (Math.sqrt(prefHeight * prefWidth) * 1.5f);

		float windowMargin = mWindowLeftRightMargin * 2;
		if (wrapWidth > Gdx.graphics.getWidth() - windowMargin) {
			wrapWidth = (int) (Gdx.graphics.getWidth() - windowMargin);
		}

		if (wrapWidth < Config.Gui.TOOLTIP_WIDTH_MIN) {
			if (prefWidth < Config.Gui.TOOLTIP_WIDTH_MIN) {
				wrapWidth = prefWidth;
			} else {
				wrapWidth = Config.Gui.TOOLTIP_WIDTH_MIN;
			}
		}

		mMessageLabel.setWrap(true);
		mMessageLabel.setWidth(wrapWidth);
	}

	/**
	 * @return true if the window is currently shown
	 */
	private boolean isWindowShown() {
		return mWindow.getStage() != null;
	}

	/**
	 * @return true if the window is currently displaying this tooltip
	 */
	private boolean isWindowDisplayingThis() {
		return isWindowShown() && mWindow.getName().equals(mWindowName);
	}

	/**
	 * @return true if the windows is scheduled to be shown
	 */
	private boolean isWindowScheduled() {
		return mShowWindowTask != null;
	}

	/**
	 * Timer task that shows the window
	 */
	private class ShowWindowTask extends Task {
		@Override
		public void run() {
			showWindow();
			mShowWindowTask = null;
		}
	}

	/**
	 * Timer task that shows the message box
	 */
	private class ShowMsgBoxTask extends Task {
		@Override
		public void run() {
			showMsgBox();
			mShowMsgBoxTask = null;
		}
	}

	/** GUI Actor we're listening to */
	private Actor mActor;
	/** Task that shows the window */
	private Task mShowWindowTask = null;
	/** Task that show the message box */
	private Task mShowMsgBoxTask = null;
	/** Gui for the current scene */
	private Gui mGui;
	/** Message box for mobile devices */
	private MsgBoxExecuter mMsgBox = null;

	/** Animation (if used). Either animation or image can be used, not both. */
	private AnimationWidget mAnimation = null;
	/** Drawable for a single image. Either animation or image can be used, not both. */
	private Image mImage = null;
	/** Show image or animation */
	private boolean mShowImageOrAnimation = false;
	/** Message to display in the tooltip */
	private String mMessage = null;
	/** Show message */
	private boolean mShowMessage = false;
	/** YouTube URL, optional */
	private String mYoutubeUrl = null;
	//	/** Descriptive text of button uses */
	//	private String mDescriptiveText = "";
	/** Window name */
	private String mWindowName = null;

	/** Window for all tooltip listeners (as only one tooltip can be
	 * displayed at the same time this is static */
	private static Window mWindow = null;
	/** Label inside the window */
	private static Label mMessageLabel = null;
	//	/** Descriptive text for window how to use control */
	//	private static Label mDescriptiveLabel = null;
	/** The table to show everything in */
	private static AlignTable mTable = null;
	/** Window left/right margin */
	private static float mWindowLeftRightMargin = 0;
	/** Window id counter */
	private static int mcTooltips = 0;
}