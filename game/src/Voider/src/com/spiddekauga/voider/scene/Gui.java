package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.AnimationWidget;
import com.spiddekauga.utils.scene.ui.AnimationWidget.AnimationWidgetStyle;
import com.spiddekauga.utils.scene.ui.MessageShower;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.UiFactory;
import com.spiddekauga.voider.editor.commands.CBugReportSend;
import com.spiddekauga.voider.editor.commands.CGameQuit;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.IImageNames;
import com.spiddekauga.voider.utils.Messages;

/**
 * Base class for all GUI containing windows
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class Gui implements Disposable {
	/**
	 * Default constructor
	 */
	public Gui() {
		// Does nothing
	}

	@Override
	public void dispose() {
		if (mMainTable != null) {
			mMainTable.dispose();
		}
		mInitialized = false;
	}

	/**
	 * Resizes the GUI object to the appropriate size
	 * @param width new width of the GUI
	 * @param height new height of the GUI
	 */
	public void resize(int width, int height) {
		if (mStage != null) {
			mStage.getViewport().update(width, height, true);
			updateBackground();
		}
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
	 * Update background images. Should be called when setting a new background or
	 * resizing the window
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
	 * Resets the GUI and adds the main table again. This will remove any actors that have
	 * been added manually through #addActor(Actor)
	 */
	public void reset() {
		mStage.clear();
		mStage.addActor(mMainTable);
	}

	/**
	 * Updates the GUI
	 */
	public void update() {
		// Remove active message box if it has been hidden
		if (!mActiveMsgBoxes.isEmpty()) {
			MsgBoxExecuter activeMsgBox = mActiveMsgBoxes.peek();

			// Else if the active message box becomes hidden. Show the previous one
			if (activeMsgBox.isHidden()) {
				mActiveMsgBoxes.pop();
				mFreeMsgBoxes.push(activeMsgBox);

				// Show the previous message box if one exists
				if (!mActiveMsgBoxes.isEmpty()) {
					mActiveMsgBoxes.peek().show(mStage);
				}
			}
		}

		if (mWidgets.waitWindow.window.getStage() != null) {
			mWidgets.waitWindow.animation.act(Gdx.graphics.getDeltaTime());
		}
	}

	/**
	 * @param useTitleStyle set to true to use a message box with the title style
	 * @return free message box
	 */
	public MsgBoxExecuter getFreeMsgBox(boolean useTitleStyle) {
		String windowStyleName = useTitleStyle ? SkinNames.General.WINDOW_MODAL_TITLE.toString() : SkinNames.General.WINDOW_MODAL.toString();

		// Find a free existing one
		MsgBoxExecuter msgBox = null;
		if (!mFreeMsgBoxes.isEmpty()) {
			msgBox = mFreeMsgBoxes.pop();
		}

		Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		// No free found, create new
		if (msgBox == null) {
			msgBox = new MsgBoxExecuter(skin, windowStyleName);
		} else {
			msgBox.setStyle(skin.get(windowStyleName, WindowStyle.class));
		}


		msgBox.clear();
		return msgBox;
	}

	/**
	 * Hides all active message boxes
	 */
	public void hideMsgBoxes() {
		if (mActiveMsgBoxes.size() >= 2) {
			// Only the latest message box is shown, i.e. just remove all except the
			// last message box which we hide.
			Iterator<MsgBoxExecuter> msgBoxIt = mActiveMsgBoxes.iterator();
			while (msgBoxIt.hasNext()) {
				MsgBoxExecuter msgBox = msgBoxIt.next();
				msgBoxIt.remove();
				mFreeMsgBoxes.push(msgBox);

				if (!msgBox.isHidden()) {
					msgBox.hide();
				}
			}
		} else if (mActiveMsgBoxes.size() == 1) {
			MsgBoxExecuter msgBox = mActiveMsgBoxes.pop();
			mFreeMsgBoxes.push(msgBox);
			msgBox.hide();
		}
	}

	/**
	 * Hide active message box. Will activate the previous message box if one is available
	 */
	public void hideMsgBoxActive() {
		if (!mActiveMsgBoxes.isEmpty()) {
			MsgBoxExecuter msgBox = mActiveMsgBoxes.pop();
			msgBox.hide();
			mFreeMsgBoxes.push(msgBox);

			// Show previous
			if (!mActiveMsgBoxes.isEmpty()) {
				mActiveMsgBoxes.peek().show(mStage);
			}
		}
	}

	/**
	 * Shows the specified message box. This will hide any active message box, remove the
	 * specified message box from the inactive list, and then show the specified active
	 * box (once the currently active box has been fully hidden).
	 * @param msgBox the message box to show
	 */
	public void showMsgBox(MsgBoxExecuter msgBox) {
		// Hide active
		if (!mActiveMsgBoxes.isEmpty()) {
			mActiveMsgBoxes.peek().hide();
		}
		mActiveMsgBoxes.push(msgBox);
		msgBox.show(mStage);
	}

	/**
	 * Shows the bug report window
	 * @param exception the exception that was thrown, null if no exception was thrown
	 */
	public void showBugReportWindow(Exception exception) {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);
		msgBox.setTitle("Bug Report");

		LabelStyle labelStyle = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);
		LabelStyle errorStyle = SkinNames.getResource(SkinNames.General.LABEL_ERROR);
		TextFieldStyle textFieldStyle = SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT);
		float padSeparator = SkinNames.getResource(SkinNames.GeneralVars.PADDING_SEPARATOR);

		int fieldWidth = (int) (Gdx.graphics.getWidth() * 0.75f);

		AlignTable content = new AlignTable();

		Label errorLabel = new Label(Messages.Error.BUG_REPORT_INFO, errorStyle);
		errorLabel.setWidth(fieldWidth);
		errorLabel.setWrap(true);
		errorLabel.pack();
		content.row();
		content.add(errorLabel).setPadBottom(padSeparator);


		// Subject
		TextField subject = new TextField("", textFieldStyle);
		subject.setWidth(fieldWidth);
		subject.setMaxLength(50);
		new TextFieldListener(subject, "Subject", null);
		content.row();
		content.add(subject);


		// Actions
		TextField lastAction = new TextField("", textFieldStyle);
		lastAction.setWidth(fieldWidth);
		lastAction.setMaxLength(100);
		new TextFieldListener(lastAction, "Last action you did before the bug occured", null);
		content.row();
		content.add(lastAction);
		TextField secondLastAction = new TextField("", textFieldStyle);
		secondLastAction.setWidth(fieldWidth);
		secondLastAction.setMaxLength(100);
		new TextFieldListener(secondLastAction, "Second last action...", null);
		content.row();
		content.add(secondLastAction);


		// Description
		Label descriptionLabel = new Label("Detailed description (optional)", labelStyle);
		content.row();
		content.add(descriptionLabel);
		TextField description = new TextField("", textFieldStyle);
		description.setWidth(fieldWidth);
		int height = (int) (Gdx.graphics.getHeight() * 0.35f);
		description.setHeight(height);
		content.row();
		content.add(description);


		CBugReportSend bugReportSend = new CBugReportSend(this, subject, lastAction, secondLastAction, description, exception);
		CGameQuit quit = new CGameQuit();

		msgBox.content(content);
		msgBox.button("Send report and quit", bugReportSend);
		msgBox.button("Quit game", quit);
		msgBox.key(Keys.ESCAPE, quit);
		msgBox.key(Keys.BACK, quit);
		msgBox.key(Keys.ENTER, bugReportSend);

		showMsgBox(msgBox);
	}

	/**
	 * Show wait window
	 * @param message optional message to display
	 */
	public void showWaitWindow(String message) {
		if (mWidgets.waitWindow.window == null) {
			return;
		}

		// Show window if it doesn't belong to a stage
		if (mWidgets.waitWindow.window.getStage() == null) {

			mWidgets.waitWindow.animation.reset();

			// Set text or empty if null
			setWaitWindowText(message);
			mStage.addActor(mWidgets.waitWindow.window);

			// Center the window
			int xPosition = (int) ((Gdx.graphics.getWidth() - mWidgets.waitWindow.window.getWidth()) * 0.5f);
			int yPosition = (int) ((Gdx.graphics.getHeight() - mWidgets.waitWindow.window.getHeight()) * 0.5f);
			mWidgets.waitWindow.window.setPosition(xPosition, yPosition);

			float fadeInDuration = (Float) SkinNames.getResource(SkinNames.GeneralVars.WAIT_WINDOW_FADE_IN);
			mWidgets.waitWindow.window.addAction(Actions.fadeIn(fadeInDuration, Interpolation.fade));
		}
		// Else just change the text
		else {
			setWaitWindowText(message);
		}
	}

	/**
	 * Sets the wait text
	 * @param message the wait message to set, null will set it to empty
	 */
	public void setWaitWindowText(String message) {
		mWidgets.waitWindow.label.setText(message != null ? message : "");
		mWidgets.waitWindow.window.pack();
	}

	/**
	 * Hides the wait window. Does nothing if the wait window isn't shown
	 */
	public void hideWaitWindow() {
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
	public void showProgressBar(String message) {
		if (mWidgets.progressBar.window == null) {
			return;
		}

		mWidgets.progressBar.window.clearActions();

		mStage.addActor(mWidgets.progressBar.window);
		updateProgressBar(0, message);

		float fadeInDuration = (Float) SkinNames.getResource(SkinNames.GeneralVars.WAIT_WINDOW_FADE_IN);
		mWidgets.progressBar.window.addAction(Actions.fadeIn(fadeInDuration, Interpolation.fade));
	}

	/**
	 * Hides the progress bar
	 */
	public void hideProgressBar() {
		if (mWidgets.progressBar.window == null || mWidgets.progressBar.window.getStage() == null) {
			return;
		}

		float fadeOutDuriation = (Float) SkinNames.getResource(SkinNames.GeneralVars.WAIT_WINDOW_FADE_OUT);
		mWidgets.progressBar.window.addAction(Actions.sequence(Actions.fadeOut(fadeOutDuriation, Interpolation.fade), Actions.removeActor()));
	}

	/**
	 * Updates the progress bar, doesn't set the text
	 * @param percentage how many percentage that has been loaded
	 */
	public void updateProgressBar(float percentage) {
		updateProgressBar(percentage, null);
	}

	/**
	 * Updates the progress bar
	 * @param percentage how many percentage that has been loaded
	 * @param message optional message, keeps previous if null
	 */
	public void updateProgressBar(float percentage, String message) {
		if (message != null) {
			mWidgets.progressBar.label.setText(message);
		}
		mWidgets.progressBar.slider.setValue(percentage);
		mWidgets.progressBar.label.pack();
		mWidgets.progressBar.window.pack();

		// Center the window
		int xPosition = (int) ((Gdx.graphics.getWidth() - mWidgets.progressBar.window.getWidth()) * 0.5f);
		int yPosition = (int) ((Gdx.graphics.getHeight() - mWidgets.progressBar.window.getHeight()) * 0.5f);
		mWidgets.progressBar.window.setPosition(xPosition, yPosition);
	}

	/**
	 * Initializes the GUI
	 */
	public void initGui() {
		if (mStage == null) {
			mStage = new Stage();
			resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			mStage.addActor(mMainTable);
			mMainTable.setName("MainTable");
			mMainTable.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
		}

		if (!mUiFactory.isInitialized() && ResourceCacheFacade.isLoaded(InternalNames.UI_GENERAL)) {
			mUiFactory.init();
		}

		// Message box and wait window
		MsgBoxExecuter.fadeDuration = 0.01f;
		if (ResourceCacheFacade.isLoaded(InternalNames.UI_GENERAL) && mMessageShower == null) {
			mMessageShower = new MessageShower(mStage);

			// Wait window
			mWidgets.waitWindow.window = new Window("", (WindowStyle) SkinNames.getResource(SkinNames.General.WINDOW_MODAL));
			mWidgets.waitWindow.window.setModal(true);
			mWidgets.waitWindow.window.setSkin((Skin) ResourceCacheFacade.get(InternalNames.UI_GENERAL));
			mWidgets.waitWindow.animation = new AnimationWidget((AnimationWidgetStyle) SkinNames.getResource(SkinNames.General.ANIMATION_WAIT));
			mWidgets.waitWindow.label = new Label("", (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_DEFAULT));
			mWidgets.waitWindow.window.add(mWidgets.waitWindow.animation).padRight(
					(Float) SkinNames.getResource(SkinNames.GeneralVars.PADDING_SEPARATOR));
			mWidgets.waitWindow.window.add(mWidgets.waitWindow.label);

			// Progress bar
			mWidgets.progressBar.window = new Window("", (WindowStyle) SkinNames.getResource(SkinNames.General.WINDOW_MODAL));
			mWidgets.progressBar.window.setModal(true);
			mWidgets.progressBar.slider = new Slider(0, 100, 0.1f, false, (SliderStyle) SkinNames.getResource(SkinNames.General.SLIDER_LOADING_BAR));
			mWidgets.progressBar.slider.setTouchable(Touchable.disabled);
			mWidgets.progressBar.label = new Label("", (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_DEFAULT));
			mWidgets.progressBar.window.add(mWidgets.progressBar.label);
			mWidgets.progressBar.window.row();
			mWidgets.progressBar.window.add(mWidgets.progressBar.slider);
		}

		mInitialized = true;
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
		// Does nothing
	}

	/**
	 * Renders the GUI
	 */
	public final void render() {
		mStage.act(SceneSwitcher.getGameTime().getDeltaTime());
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
	 * Displays a message in the message window uses the default label style
	 * @param message the message to display
	 * @see #showMessage(String, LabelStyle)
	 */
	public void showMessage(String message) {
		if (mMessageShower != null) {
			mMessageShower.addMessage(message);
		}
	}

	/**
	 * Displays a message in the message window with the specified style
	 * @param message the message to display
	 * @param style the label style of the message
	 * @see #showMessage(String)
	 */
	public void showMessage(String message, LabelStyle style) {
		if (mMessageShower != null) {
			mMessageShower.addMessage(message, style);
		}
	}

	/**
	 * Displays a highlighted message
	 * @param message the message to display as highlighted
	 */
	public void showHighlightMessage(String message) {
		if (mMessageShower != null) {
			mMessageShower.addMessage(message, (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_HIGHLIGHT));
		}
	}

	/**
	 * Displays an error message
	 * @param message the message to display as an error
	 */
	public void showErrorMessage(String message) {
		if (mMessageShower != null) {
			mMessageShower.addMessage(message, (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_ERROR));
		}
	}

	/**
	 * Displays a successful message
	 * @param message the message to display as successful
	 */
	public void showSuccessMessage(String message) {
		if (mMessageShower != null) {
			mMessageShower.addMessage(message, (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_SUCCESS));
		}
	}

	/**
	 * Hide all messages
	 */
	public void hideAllMessages() {
		if (mMessageShower != null) {
			mMessageShower.removeAllMessages();
		}
	}

	/**
	 * Checks if a button is checked (from the event).
	 * @param event checks if the target inside the event is a button and it's checked
	 * @return checked button. If the target isn't a button or the button isn't checked it
	 *         returns null.
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
	 * Set the GUI as visible/invisible. I.e. if the GUI should be drawn or not.
	 * @param visible set to true for visible, false for invisible.
	 */
	public void setVisible(boolean visible) {
		mVisible = visible;
	}

	/**
	 * @return true if the GUI is visible. I.e. should be drawn.
	 */
	public boolean isVisible() {
		return mVisible;
	}

	/** UI Factory for creating UI elements */
	protected static UiFactory mUiFactory = UiFactory.getInstance();
	/** If the GUI is visible */
	private boolean mVisible = true;
	/** Main table for the layout */
	protected AlignTable mMainTable = new AlignTable();
	/** True if the GUI has been initialized */
	protected boolean mInitialized = false;
	/** Stage for the GUI */
	private Stage mStage = null;
	/** Error message shower */
	private MessageShower mMessageShower = null;
	/** Active message boxes */
	private Stack<MsgBoxExecuter> mActiveMsgBoxes = new Stack<>();
	/** Inactive/free message boxes */
	private Stack<MsgBoxExecuter> mFreeMsgBoxes = new Stack<>();
	/** Various widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

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
			Label label = null;

		}
	}
}
