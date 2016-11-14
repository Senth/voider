package com.spiddekauga.voider.backup;

/**
 * Main application for the Voider backup

 */
public class Application {
	/**
	 * Main method
	 * @param args all arguments
	 */
	public static void main(String[] args) {
		ActionCreator actionCreator = new ActionCreator(args);
		Action action = actionCreator.createAction();

		if (action != null) {
			action.execute();
		} else {
			showHelp();
		}
	}

	/**
	 * Show help
	 */
	public static void showHelp() {
		//@formatter:off
		String help = "Backup:\n"
				+ "voider-backup backup URL BACKUP_DIR\n\n"
				+ "Restore:\n"
				+ "voider-backup restore DATE URL BACKUP_DIR\n"
				+ "DATE should be in ISO format. E.g. 2015-08-24T22:23:00";
		//@formatter:on

		System.out.println(help);
	}
}
