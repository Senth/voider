package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.MessageShower;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;

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
	 * @return free message box
	 */
	public MsgBoxExecuter getFreeMsgBox() {
		MsgBoxExecuter msgBox = null;

		// Find a free existing one
		for (int i = mInactiveMsgBoxes.size() - 1; i >= 0; i--) {
			if (!mInactiveMsgBoxes.get(i).hasParent()) {
				msgBox = mInactiveMsgBoxes.get(i);
				break;
			}
		}

		// No free found, create new
		if (msgBox == null) {
			Skin skin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
			msgBox = new MsgBoxExecuter(skin, "modal");
			mInactiveMsgBoxes.add(msgBox);
		}

		msgBox.clear();
		return msgBox;
	}

	/**
	 * Hides all active message boxes
	 */
	public void hideMsgBoxes() {
		if (mActiveMsgBoxes.size() >= 2) {
			// Remove all already hidden once, start hiding shown
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
	 * Initializes the GUI
	 */
	public void initGui() {
		MsgBoxExecuter.fadeDuration = 0.01f;
		if (ResourceCacheFacade.isLoaded(ResourceNames.UI_GENERAL) && mErrorMessageShower == null) {
			mErrorMessageShower = new MessageShower(mStage);
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
	 * Will display an error message
	 * @param message the error message to display
	 */
	public void showErrorMessage(String message) {
		mErrorMessageShower.addMessage(message);
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
	 * Adds inner table to the outer table
	 * @param innerTable the table to add to the outerTable, if null outerTable will
	 * only be cleared.
	 * @param outerTable the table to clear and then add innerTable to.
	 */
	protected static void addInnerTable(AlignTable innerTable, AlignTable outerTable) {
		outerTable.dispose(false);

		if (innerTable != null) {
			outerTable.add(innerTable);
			innerTable.invalidateHierarchy();
		}

		outerTable.invalidateHierarchy();
	}


	/** Main table for the layout */
	protected AlignTable mMainTable = new AlignTable();
	/** True if the GUI has been initialized */
	protected boolean mInitialized = false;
	/** Stage for the GUI */
	private Stage mStage = new Stage();
	/** Error message shower */
	private MessageShower mErrorMessageShower = null;
	/** Active message boxes */
	private ArrayList<MsgBoxExecuter> mActiveMsgBoxes = new ArrayList<MsgBoxExecuter>();
	/** Inactive/free message boxes */
	private ArrayList<MsgBoxExecuter> mInactiveMsgBoxes = new ArrayList<MsgBoxExecuter>();
	/** Queued messages box, will be displayed once the active message box has been hidden */
	private MsgBoxExecuter mQueuedMsgBox = null;
}
