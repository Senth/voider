package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.MsgBox;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.EditorGui;
import com.spiddekauga.voider.editor.IActorEditor;
import com.spiddekauga.voider.editor.IEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.utils.Messages;

/**
 * Cancels the hide of the message box if the definition doesn't got a valid name.
 */
public class CDefHasValidName extends Command {
/** Please enter a name message */
String mEnterNameMessage;
/** The message box the name is shown in */
MsgBox mMsgBox;
/** The editor */
IEditor mEditor;
/** The GUI to print the info messages in */
EditorGui mGui;
/**
 * Creates the command where it checks if the definition has a valid name.
 * @param msgBox the message box where the name is shown in
 * @param gui the gui to send info messages to
 * @param editor the editor to if the definition got a name in
 * @param defTypeName the definition type name
 */
public CDefHasValidName(MsgBox msgBox, EditorGui gui, IEditor editor, String defTypeName) {
	mMsgBox = msgBox;
	mGui = gui;
	mEditor = editor;

	mEnterNameMessage = "please enter a";
	if (Strings.beginsWithWovel(defTypeName)) {
		mEnterNameMessage += "n";
	}

	mEnterNameMessage += " " + defTypeName + " name";
}

@Override
public boolean execute() {
	String name = "";
	if (mEditor instanceof LevelEditor) {
		name = ((LevelEditor) mEditor).getName();
	} else if (mEditor instanceof IActorEditor) {
		name = ((IActorEditor) mEditor).getName();
	}

	// Force the player to set a name
	if (name.equals(Config.Actor.NAME_DEFAULT)) {
		mGui.setInfoNameError(mEnterNameMessage);
		mMsgBox.cancel();
	} else if (name.length() < Config.Actor.NAME_LENGTH_MIN) {
		mGui.setInfoNameError(Messages.Error.NAME_CHARACTERS_MIN);
		mMsgBox.cancel();
	}
	// Save was pressed and valid name, save the actor
	else {
		mEditor.saveDef();
	}

	return true;
}

@Override
public boolean undo() {
	// Does nothing
	return false;
}
}
