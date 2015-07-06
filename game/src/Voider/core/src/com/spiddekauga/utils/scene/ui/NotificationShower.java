package com.spiddekauga.utils.scene.ui;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;

/**
 * Shows error messages on the screen
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class NotificationShower implements Disposable {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private NotificationShower() {
		// Does nothing
	}

	/**
	 * Initializes the notification shower. Will only succeed if all required resources
	 * have been loaded
	 */
	private void init() {
		if (!mInitialized && SkinNames.isLoaded(SkinNames.GeneralVars.NOTIFICATION_BACKGROUND_COLOR)) {
			mOuterTable.setAlign(Horizontal.LEFT, Vertical.BOTTOM);
			mOuterTable.setMargin(mUiFactory.getStyles().vars.paddingOuter);
			mOuterTable.setWidth((float) SkinNames.getResource(SkinNames.GeneralVars.NOTIFICATION_WIDTH));
			mOuterTable.setKeepWidth(true);
			mOuterTable.setTouchable(Touchable.childrenOnly);

			mRowPad = mUiFactory.getStyles().vars.paddingOuter;

			mFadeIn = SkinNames.getResource(SkinNames.GeneralVars.NOTIFICATION_FADE_IN);
			mFadeOut = SkinNames.getResource(SkinNames.GeneralVars.NOTIFICATION_FADE_OUT);
			mDisplayTime = SkinNames.getResource(SkinNames.GeneralVars.NOTIFICATION_TIME);
			mBackgroundColor = SkinNames.getResource(SkinNames.GeneralVars.NOTIFICATION_BACKGROUND_COLOR);

			mInitialized = true;
		}
	}

	@Override
	public void dispose() {
		mInitialized = false;
	}

	/**
	 * @return instance of this class
	 */
	public static NotificationShower getInstance() {
		if (mInstance == null) {
			mInstance = new NotificationShower();
		}

		if (!mInstance.mInitialized) {
			mInstance.init();
		}

		return mInstance;
	}

	/**
	 * @return background imagae for the table
	 */
	public Background createBackground() {
		return new Background(mBackgroundColor);
	}

	/**
	 * Sets the stage of the message shower. Will move all existing message to the new
	 * stage
	 * @param stage new stage
	 */
	public void setStage(Stage stage) {
		if (mStage != stage) {
			mStage = stage;
			mStage.addActor(mOuterTable);
		}
	}

	/**
	 * Reset the position of the notifications
	 */
	public void resetPosition() {
		mOuterTable.invalidate();
	}

	/**
	 * Remove all messages directly
	 */
	public void removeAllMessages() {
		mOuterTable.dispose(true);
	}

	/**
	 * Adds a new message with the specified label style
	 * @param style the label style to use for the message
	 * @param message the message to display on the screen
	 */
	public void show(final NotificationTypes style, final String message) {
		if (!mInitialized) {
			init();
		}

		if (mInitialized) {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					pushToFront();

					// Create row table with background
					final AlignTable messageTable = new AlignTable();
					messageTable.setBackgroundImage(createBackground());
					messageTable.setPad(mRowPad);
					messageTable.row().setFillWidth(true);
					messageTable.setName("message-table");
					mOuterTable.row().setFillWidth(true);
					mOuterTable.add(messageTable).setFillWidth(true);
					Image image = style.createIcon();

					// Icon
					messageTable.add(image).setPadRight(mUiFactory.getStyles().vars.paddingInner);

					// Label
					Label label = style.createMessage(message);
					label.pack();
					messageTable.add(label).setFillWidth(true);

					// If pressed remove directly
					messageTable.addListener(new InputListener() {
						@Override
						public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
							messageTable.clearActions();
							messageTable.addAction(sequence(fadeOut(mFadeIn), removeActor()));
							return false;
						}
					});


					// Set timer for fadeIn - display - fadeOut - remove
					messageTable.addAction(sequence(fadeIn(mFadeIn), delay(mDisplayTime), fadeOut(mFadeOut), removeActor()));
				}
			});
		}
	}

	/**
	 * Shows an information message
	 * @param message the message to display on the screen
	 */
	public void showInfo(String message) {
		show(NotificationTypes.INFO, message);
	}

	/**
	 * Shows an error message
	 * @param message the message to display on the screen
	 */
	public void showError(String message) {
		show(NotificationTypes.ERROR, message);
	}

	/**
	 * Shows a highlighted message
	 * @param message the message to display on the screen
	 */
	public void showHighlight(String message) {
		show(NotificationTypes.HIGHLIGHT, message);
	}

	/**
	 * Shows an success message
	 * @param message the message to display on the screen
	 */
	public void showSuccess(String message) {
		show(NotificationTypes.SUCCESS, message);
	}

	/**
	 * Push message to the front of the UI
	 */
	public void pushToFront() {
		mOuterTable.setZIndex(Integer.MAX_VALUE);
	}

	/**
	 * Adds a new message to the screen, uses the default/standard label style
	 * @param message the message to display on the screen
	 */
	public void show(String message) {
		show(NotificationTypes.INFO, message);
	}

	/**
	 * Adds a new notification message to the screen
	 * @param notificationMessage
	 */
	public void show(NotificationMessage notificationMessage) {
		show(notificationMessage.mType, notificationMessage.mMessage);
	}

	/**
	 * Wrapper for storing notification messages
	 */
	public static class NotificationMessage {
		/**
		 * Default constructor
		 */
		public NotificationMessage() {
		}


		/**
		 * Set a message with INFO type
		 * @param message the message to display
		 */
		public NotificationMessage(String message) {
			setMessage(message);
		}

		/**
		 * Set the message and type directly
		 * @param type the type of message
		 * @param message the message to display
		 */
		public NotificationMessage(NotificationTypes type, String message) {
			setMessage(type, message);
		}

		/**
		 * Set the message
		 * @param message
		 */
		public void setMessage(String message) {
			mMessage = message;
		}

		/**
		 * Set the message and type
		 * @param type
		 * @param message
		 */
		public void setMessage(NotificationTypes type, String message) {
			mMessage = message;
			mType = type;
		}

		/**
		 * Shows the notification message
		 */
		public void show() {
			NotificationShower.getInstance().show(this);
		}

		private String mMessage = "";
		private NotificationTypes mType = NotificationTypes.INFO;
	}

	/**
	 * All message styles
	 */
	public enum NotificationTypes {
		/** Information message */
		INFO(LabelStyles.HIGHLIGHT, SkinNames.GeneralImages.MESSAGE_INFO),
		/** Highlight message */
		HIGHLIGHT(LabelStyles.WARNING, SkinNames.GeneralImages.MESSAGE_HIGHLIGHT),
		/** Success message */
		SUCCESS(LabelStyles.SUCCESS, SkinNames.GeneralImages.MESSAGE_SUCCESS),
		/** Error message */
		ERROR(LabelStyles.ERROR, SkinNames.GeneralImages.MESSAGE_ERROR),

		;
		/**
		 * Sets which label style the message is bound to
		 * @param labelStyle
		 * @param icon the icon to display
		 */
		private NotificationTypes(LabelStyles labelStyle, IImageNames icon) {
			mLabelStyle = labelStyle;
			mIcon = icon;
		}

		/**
		 * Create label from the this style
		 * @param message the message to display
		 * @return a label with the message
		 */
		private Label createMessage(String message) {
			return mUiFactory.text.create(message, true, mLabelStyle);
		}

		/**
		 * Create icon for the message
		 * @return icon for this message type
		 */
		private Image createIcon() {
			return new Image(SkinNames.getDrawable(mIcon));
		}

		private IImageNames mIcon;
		private LabelStyles mLabelStyle;
	}


	private AlignTable mOuterTable = new AlignTable();
	private Stage mStage;
	private boolean mInitialized = false;

	// Style variables
	/** Padding inside the row */
	private float mRowPad = 0;
	private float mFadeIn = 0;
	private float mFadeOut = 0;
	private float mDisplayTime = 0;
	private Color mBackgroundColor = null;

	private static UiFactory mUiFactory = UiFactory.getInstance();
	private static NotificationShower mInstance = null;
}
