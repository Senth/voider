package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;
import com.spiddekauga.utils.scene.ui.MessageShower;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.editor.commands.CBugReportSend;
import com.spiddekauga.voider.editor.commands.CGameQuit;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Messages;

/**
 * Base class for all GUI containing windows
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Gui implements Disposable {
	/**
	 * Default constructor
	 */
	public Gui() {
		mStage.addActor(mMainTable);
		mMainTable.setName("MainTable");
		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
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
		mStage.setViewport(width, height, true);
	}

	/**
	 * Adds additional actors to the GUI
	 * @param actor is added to the stage
	 */
	public void addActor(Actor actor) {
		mStage.addActor(actor);
	}

	/**
	 * Resets the GUI and adds the main table again. This
	 * will remove any actors that have been added manually through #addActor(Actor)
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
			MsgBoxExecuter activeMsgBox = mActiveMsgBoxes.get(mActiveMsgBoxes.size()-1);

			// Has queue, waiting for active message box to be hidden
			if (mQueuedMsgBox != null) {
				if (activeMsgBox.isHidden()) {
					mActiveMsgBoxes.add(mQueuedMsgBox);
					mQueuedMsgBox.show(mStage);
					mQueuedMsgBox = null;
				}
			}
			// Else if the active message box becomes hidden. Show the previous one
			else if (activeMsgBox.isHidden()) {
				mActiveMsgBoxes.remove(mActiveMsgBoxes.size()-1);
				mInactiveMsgBoxes.add(activeMsgBox);

				// Show the previous message box if one exists
				if (!mActiveMsgBoxes.isEmpty()) {
					mActiveMsgBoxes.get(mActiveMsgBoxes.size()-1).show(mStage);
				}
			}
		}
	}

	/**
	 * @param useTitleStyle set to true to use a message box with the title style
	 * @return free message box
	 */
	public MsgBoxExecuter getFreeMsgBox(boolean useTitleStyle) {
		MsgBoxExecuter msgBox = null;

		String windowStyleName = useTitleStyle ? SkinNames.General.WINDOW_MODAL_TITLE.toString() : SkinNames.General.WINDOW_MODAL.toString();

		// Find a free existing one
		for (int i = mInactiveMsgBoxes.size() - 1; i >= 0; i--) {
			if (!mInactiveMsgBoxes.get(i).hasParent()) {
				msgBox = mInactiveMsgBoxes.get(i);
				break;
			}
		}

		Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		// No free found, create new
		if (msgBox == null) {
			msgBox = new MsgBoxExecuter(skin, windowStyleName);
			mInactiveMsgBoxes.add(msgBox);
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

				if (msgBox.isHidden()) {
					msgBoxIt.remove();
					mInactiveMsgBoxes.add(msgBox);
				} else {
					msgBox.hide();
				}
			}
		} else if (mActiveMsgBoxes.size() == 1) {
			MsgBoxExecuter msgBox = mActiveMsgBoxes.remove(0);
			mInactiveMsgBoxes.add(msgBox);
			msgBox.hide();
		}
	}

	/**
	 * Shows the specified message box.
	 * This will hide any active message box, remove the specified message box from the
	 * inactive list, and then show the specified active box (once the currently active box
	 * has been fully hidden).
	 * @param msgBox the message box to show
	 */
	public void showMsgBox(MsgBoxExecuter msgBox) {
		// No active message box, add directly
		if (mActiveMsgBoxes.isEmpty()) {
			int index = mInactiveMsgBoxes.indexOf(msgBox);
			if (index != -1) {
				mInactiveMsgBoxes.remove(index);
				mActiveMsgBoxes.add(msgBox);
				msgBox.show(mStage);
			} else {
				Gdx.app.error("Gui", "Could not find the message box in inactive message boxes!");
			}
		}
		// Hide the active message box, and queue the specified one.
		else {
			mActiveMsgBoxes.get(mActiveMsgBoxes.size() - 1).hide();
			mQueuedMsgBox = msgBox;
		}
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

		int fieldWidth = (int) (Gdx.graphics.getWidth() * 0.75f);

		AlignTable content = new AlignTable();
		content.setCellPaddingDefault((Float) SkinNames.getResource(SkinNames.General.PADDING_DEFAULT));

		Label errorLabel = new Label(Messages.Error.BUG_REPORT_INFO, errorStyle);
		errorLabel.setWidth(fieldWidth);
		errorLabel.setWrap(true);
		errorLabel.pack();
		content.add(errorLabel).setPadBottom((Float) SkinNames.getResource(SkinNames.General.PADDING_SEPARATOR));
		content.row();


		TextField lastAction = new TextField("", textFieldStyle);
		lastAction.setWidth(fieldWidth);
		new TextFieldListener(lastAction, "Last action you did before the bug occured", null);
		content.add(lastAction);
		content.row();
		TextField secondLastAction = new TextField("", textFieldStyle);
		secondLastAction.setWidth(fieldWidth);
		new TextFieldListener(secondLastAction, "Second last action...", null);
		content.add(secondLastAction);
		content.row();
		TextField thirdLastAction = new TextField("", textFieldStyle);
		thirdLastAction.setWidth(fieldWidth);
		new TextFieldListener(thirdLastAction, "Third last action...", null);
		content.add(thirdLastAction).setPadBottom((Float) SkinNames.getResource(SkinNames.General.PADDING_SEPARATOR));
		content.row();

		Label descriptionLabel = new Label("Detailed description (optional)", labelStyle);
		content.add(descriptionLabel);
		content.row();
		TextField description = new TextField("", textFieldStyle);
		description.setWidth(fieldWidth);
		int height = (int) (Gdx.graphics.getHeight()*0.35f);
		description.setHeight(height);
		content.add(description);

		CBugReportSend bugReportSend = new CBugReportSend(lastAction, secondLastAction, thirdLastAction, description, exception);
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
		if (mWaitWindow == null) {
			return;
		}

		mWaitWindow.clearChildren();

		// TODO add animation image

		mWaitWindow.add(message);
		mWaitWindow.pack();
		mStage.addActor(mWaitWindow);

		// Center the window
		int xPosition = (int) ((Gdx.graphics.getWidth() - mWaitWindow.getWidth()) * 0.5f);
		int yPosition = (int) ((Gdx.graphics.getHeight() - mWaitWindow.getHeight()) * 0.5f);
		mWaitWindow.setPosition(xPosition, yPosition);

		float fadeInDuration = (Float) SkinNames.getResource(SkinNames.General.WAIT_WINDOW_FADE_IN);
		mWaitWindow.addAction(Actions.fadeIn(fadeInDuration, Interpolation.fade));
	}

	/**
	 * Hides the wait window
	 */
	public void hideWaitWindow() {
		if (mWaitWindow == null || mWaitWindow.getStage() == null) {
			return;
		}

		float fadeOutDuriation = (Float) SkinNames.getResource(SkinNames.General.WAIT_WINDOW_FADE_OUT);
		mWaitWindow.addAction(Actions.sequence(Actions.fadeOut(fadeOutDuriation, Interpolation.fade), Actions.removeActor()));

	}

	/**
	 * Initializes the GUI
	 */
	public void initGui() {
		// Message box and wait window
		MsgBoxExecuter.fadeDuration = 0.01f;
		if (ResourceCacheFacade.isLoaded(InternalNames.UI_GENERAL) && mMessageShower == null) {
			mMessageShower = new MessageShower(mStage);
			mWaitWindow = new Window("", (WindowStyle) SkinNames.getResource(SkinNames.General.WINDOW_MODAL));
			mWaitWindow.setModal(true);
			mWaitWindow.setSkin((Skin) ResourceCacheFacade.get(InternalNames.UI_GENERAL));
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
	 * Displays a message in the message window uses the default label style
	 * @param message the message to display
	 * @see #showMessage(String, LabelStyle)
	 */
	public void showMessage(String message) {
		mMessageShower.addMessage(message);
	}

	/**
	 * Displays a message in the message window with the specified style
	 * @param message the message to display
	 * @param style the label style of the message
	 * @see #showMessage(String)
	 */
	public void showMessage(String message, LabelStyle style) {
		mMessageShower.addMessage(message, style);
	}

	/**
	 * Displays a highlighted message
	 * @param message the message to display as highlighted
	 */
	public void showHighlightMessage(String message) {
		mMessageShower.addMessage(message, (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_HIGHLIGHT));
	}

	/**
	 * Displays an error message
	 * @param message the message to display as an error
	 */
	public void showErrorMessage(String message) {
		mMessageShower.addMessage(message, (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_ERROR));
	}

	/**
	 * Displays a successful message
	 * @param message the message to display as successful
	 */
	public void showSuccessMessage(String message) {
		mMessageShower.addMessage(message, (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_SUCCESS));
	}

	/**
	 * Checks if a button is checked (from the event).
	 * @param event checks if the target inside the event is a button and it's checked
	 * @return checked button. If the target isn't a button or the button isn't checked
	 * it returns null.
	 */
	protected static Button getCheckedButton(Event event) {
		if (event.getTarget() instanceof Button) {
			Button button  = (Button)event.getTarget();
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


	/** Wait window */
	private Window mWaitWindow = null;
	/** If the GUI is visible */
	private boolean mVisible = true;
	/** Main table for the layout */
	protected AlignTable mMainTable = new AlignTable();
	/** True if the GUI has been initialized */
	protected boolean mInitialized = false;
	/** Stage for the GUI */
	private Stage mStage = new Stage();
	/** Error message shower */
	private MessageShower mMessageShower = null;
	/** Active message boxes */
	private ArrayList<MsgBoxExecuter> mActiveMsgBoxes = new ArrayList<MsgBoxExecuter>();
	/** Inactive/free message boxes */
	private ArrayList<MsgBoxExecuter> mInactiveMsgBoxes = new ArrayList<MsgBoxExecuter>();
	/** Queued messages box, will be displayed once the active message box has been hidden */
	private MsgBoxExecuter mQueuedMsgBox = null;
}
