package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.scenes.scene2d.ui.MsgBox;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.IActorEditor;
import com.spiddekauga.voider.editor.IEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.scene.Gui;

/**
 * Cancels the hide of the message box if the definition doesn't got a valid name.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CDefHasValidName extends Command {
	/**
	 * Creates the command where it checks if the definition has a valid name.
	 * @param msgBox the message box where the name is shown in
	 * @param gui the gui to send info messages to
	 * @param editor the editor to if the definition got a name in
	 * @param defTypeName the definition type name
	 */
	public CDefHasValidName(MsgBox msgBox, Gui gui, IEditor editor, String defTypeName) {
		mMsgBox = msgBox;
		mGui = gui;
		mEditor = editor;

		mEnterNameMessage = "Please enter a";
		if (Strings.beginsWithWovel(defTypeName)) {
			mEnterNameMessage += "n";
		}

		mEnterNameMessage += " " + defTypeName + " name";
	}


	@Override
	public boolean execute() {
		String name = null;
		if (mEditor instanceof LevelEditor) {
			name = ((LevelEditor) mEditor).getLevelName();
		}
		else if (mEditor instanceof IActorEditor) {
			name = ((IActorEditor) mEditor).getName();
		}

		// Force the player to set a name
		if (name.equals(Config.Actor.NAME_DEFAULT)) {
			mGui.showErrorMessage(mEnterNameMessage);
			mMsgBox.cancel();
		} else if (name.length() < Config.Actor.NAME_LENGTH_MIN) {
			mGui.showErrorMessage("Name must contain at least " + Config.Actor.NAME_LENGTH_MIN + " characters");
			mMsgBox.cancel();
		}

		return true;
	}

	@Override
	public boolean undo() {
		// Does nothing
		return false;
	}

	/** Please enter a name message */
	String mEnterNameMessage;
	/** The message box the name is shown in */
	MsgBox mMsgBox;
	/** The editor */
	IEditor mEditor;
	/** The gui to print the info messages in */
	Gui mGui;
}
