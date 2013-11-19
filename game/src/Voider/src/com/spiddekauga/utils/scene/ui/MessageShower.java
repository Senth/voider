package com.spiddekauga.utils.scene.ui;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * Shows error messages on the screen
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class MessageShower {
	/**
	 * Creates the error message shower for the specified stage
	 * @param stage the stage to show the messages in
	 */
	public MessageShower(Stage stage) {
		Skin skin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		mWindow = new Window("", skin);
		mWindow.setPosition(0, Gdx.graphics.getHeight());
		mWindow.setWidth(Gdx.graphics.getWidth() * 0.35f);
		mWindow.add(mAlignTable);
		mAlignTable.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mAlignTable.setRowAlign(Horizontal.LEFT, Vertical.TOP);
		mAlignTable.setRowPaddingDefault(Config.Gui.PADDING_DEFAULT);
		mStage = stage;
	}

	/**
	 * Adds a new error message to the screen
	 * @param message the error message to display on the screen
	 */
	public void addMessage(String message) {
		// Add window to stage, if it's not in the stage
		if (mWindow.getStage() == null) {
			mStage.addActor(mWindow);
			mWindow.addAction(fadeIn(Config.Gui.MESSAGE_FADE_IN_DURATION));
		}
		// Make sure window isn't fading out and set it to correct alpha
		else if (mWindow.getActions().size > 0) {
			mWindow.clearActions();
			if (mWindow.getColor().a < 1) {
				mWindow.addAction(fadeIn(Config.Gui.MESSAGE_FADE_IN_DURATION));
			}
		}

		// Get free label
		Label messageLabel = Pools.label.obtain();

		// Reset label
		messageLabel.setText(message);
		messageLabel.setWidth(Gdx.graphics.getWidth() * 0.3f);
		messageLabel.setWrap(true);
		mcMessages++;
		mAlignTable.row();
		mAlignTable.add(messageLabel);
		packAndPlaceWindow();

		// Set timer for fadeIn - display - fadeOut - remove - free
		float showDuration = Messages.calculateTimeToShowMessage(message);
		Action fadeOutAction = Actions.parallel(fadeOut(Config.Gui.MESSAGE_FADE_OUT_DURATION), Actions.run(mWindowFadeOut));
		messageLabel.addAction(sequence(fadeIn(Config.Gui.MESSAGE_FADE_IN_DURATION), delay(showDuration), fadeOutAction, removeActor(), Actions.run(new FreeLabel(messageLabel))));
	}

	/**
	 * Packs and positions the window at the correct place
	 */
	private void packAndPlaceWindow() {
		mAlignTable.layout();
		mWindow.pack();

		float heightOffset = Gdx.graphics.getHeight() - mWindow.getPrefHeight();
		mWindow.setPosition(0, heightOffset);
	}

	/**
	 * A runnable that tests whether only one label is left in the message window
	 * when its about to fade out. If true, the window will also fade out.
	 */
	private Runnable mWindowFadeOut = new Runnable() {
		@Override
		public void run() {
			mcMessages--;
			if (mcMessages == 0) {
				mWindow.addAction(sequence(fadeOut(Config.Gui.MESSAGE_FADE_OUT_DURATION),delay(0.1f),Actions.run(mWindowReset),removeActor()));
				// Pack the window...
			} else {
				mWindow.addAction(sequence(delay(Config.Gui.MESSAGE_FADE_OUT_DURATION), Actions.run(mWindowPack)));
			}
		}
	};

	/**
	 * Packs the window
	 */
	private Runnable mWindowPack = new Runnable() {
		@Override
		public void run() {
			packAndPlaceWindow();
		}
	};

	/**
	 * Resets the window
	 */
	private Runnable mWindowReset = new Runnable() {
		@Override
		public void run() {
			if (!mWindow.hasChildren()) {
				mWindow.reset();
			}
		}
	};

	/**
	 * Frees the label
	 */
	private class FreeLabel implements Runnable {
		/**
		 * Which label to free
		 * @param label the label to free
		 */
		public FreeLabel(Label label) {
			mLabel = label;
		}

		@Override
		public void run() {
			Pools.label.free(mLabel);
		}

		/** The label to free */
		private Label mLabel;
	}

	/** Number of active messages */
	private int mcMessages = 0;
	/** Window where the messages are shown */
	private Window mWindow;
	/** Stage to show the messages in */
	private Stage mStage;
	/** Align table that the messages are displayed in */
	private AlignTable mAlignTable = new AlignTable();
}
