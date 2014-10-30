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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.utils.Messages;

/**
 * Shows error messages on the screen
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MessageShower {
	/**
	 * Creates the error message shower for the specified stage
	 * @param stage the stage to show the messages in
	 */
	public MessageShower(Stage stage) {
		Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		mWindow = new Window("", skin);
		mWidth = (int) (Gdx.graphics.getWidth() * 0.35f);
		mWindow.setPosition(Gdx.graphics.getWidth() - mWidth, 0);
		mWindow.setWidth(mWidth);
		mWindow.add(mAlignTable);
		mAlignTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		float windowPadding = skin.get(SkinNames.GeneralVars.PADDING_INNER.toString(), Float.class);
		mAlignTable.setPaddingRowDefault(windowPadding);
		mStage = stage;
	}

	/**
	 * Remove all messages directly
	 */
	public void removeAllMessages() {
		mAlignTable.dispose();
		mWindow.remove();
	}

	/**
	 * Adds a new message with the specified label style
	 * @param message the message to display on the screen
	 * @param style the label style to use for the message
	 */
	public void addMessage(final String message, final LabelStyle style) {
		Gdx.app.postRunnable(new Runnable() {

			@Override
			public void run() {
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
				Label messageLabel = UiFactory.getInstance().text.create(message);

				// Reset label
				messageLabel.setStyle(style);
				messageLabel.setWidth(Gdx.graphics.getWidth() * 0.3f);
				messageLabel.setWrap(true);
				mcMessages++;
				mAlignTable.row();
				mAlignTable.add(messageLabel);
				messageLabel.invalidate();
				messageLabel.layout();
				packAndPlaceWindow();

				// Set timer for fadeIn - display - fadeOut - remove - free
				float showDuration = Messages.calculateTimeToShowMessage(message);
				Action fadeOutAction = Actions.parallel(fadeOut(Config.Gui.MESSAGE_FADE_OUT_DURATION), Actions.run(mWindowFadeOut));
				messageLabel.addAction(sequence(fadeIn(Config.Gui.MESSAGE_FADE_IN_DURATION), delay(showDuration), fadeOutAction, removeActor()));

			}
		});
	}

	/**
	 * Adds a new message to the screen, uses the default/standard label style
	 * @param message the message to display on the screen
	 */
	public void addMessage(String message) {
		addMessage(message, (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_DEFAULT));
	}

	/**
	 * Packs and positions the window at the correct place
	 */
	private void packAndPlaceWindow() {
		mAlignTable.layout();
		mWindow.pack();


		float actualWidth = mWindow.getPrefWidth();
		mWindow.setPosition(Gdx.graphics.getWidth() - actualWidth, 0);
	}

	/**
	 * A runnable that tests whether only one label is left in the message window when its
	 * about to fade out. If true, the window will also fade out.
	 */
	private Runnable mWindowFadeOut = new Runnable() {
		@Override
		public void run() {
			mcMessages--;
			if (mcMessages == 0) {
				mWindow.addAction(sequence(fadeOut(Config.Gui.MESSAGE_FADE_OUT_DURATION), delay(0.1f), Actions.run(mWindowReset), removeActor()));
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


	/** Number of active messages */
	private int mcMessages = 0;
	/** Window where the messages are shown */
	private Window mWindow;
	/** Stage to show the messages in */
	private Stage mStage;
	/** Align table that the messages are displayed in */
	private AlignTable mAlignTable = new AlignTable();

	/** Width of the message window */
	private static int mWidth = 0;
}
