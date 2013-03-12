package com.spiddekauga.voider.utils;

/**
 * Class containing all messages for voider, including help function
 * for retrieving messages
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Messages {

	/**
	 * Unsaved message, do you want to continue?
	 * @param unsavedType what is it that is unsaved
	 * @param action the action that will be taken.
	 * @return message for the unsaved type with the specified action
	 */
	public static String getUnsavedMessage(String unsavedType, UnsavedActions action) {
		String message = "Your current " + unsavedType + " is unsaved.\n" +
				"Do you want to save it before " + action.getMessage(unsavedType) + "?";
		return message;
	}


	/**
	 * Enumeration of unsaved actions
	 */
	public enum UnsavedActions {
		/** When creating a new definition */
		NEW("creating a new UNSAVED_TYPE"),
		/** When loading another definition */
		LOAD("loading another UNSAVED_TYPE"),
		/** When duplicating the existing definition */
		DUPLICATE("duplicating it"),
		/** Switching to the level editor */
		LEVEL_EDITOR("switching to the level editor"),
		/** Switching to the enemy editor */
		ENEMY_EDITOR("switching to the enemy editor"),
		/** Switching to bullet editor */
		BULLET_EDITOR("switching to the bullet editor"),
		/** Switching to campaign editor */
		CAMPAIGN_EDITOR("switching to the campaign editor")

		;
		/**
		 * @param unsavedType the unsaved type, which will replace any
		 * instance of UNSAVED_TYPE within the message.
		 * @return the message of the action
		 */
		public String getMessage(String unsavedType) {
			return mMessage.replace(UNSAVED_TYPE, unsavedType);
		}

		/**
		 * Creates an unsaved action with the message
		 * @param message the message to use for the action. Any instance
		 * of UNSAVED_TYPE in the message will later be replaced with the actual
		 * unsaved type.
		 */
		private UnsavedActions(String message) {
			mMessage = message;
		}

		/** The message that will be used for the action */
		private String mMessage;
		/** replacement variable */
		public final static String UNSAVED_TYPE = "UNSAVED_TYPE";
	}
}
