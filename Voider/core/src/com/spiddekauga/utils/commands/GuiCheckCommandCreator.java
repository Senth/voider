package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.voider.Config.Gui;

/**
 * Listens to a group of buttons and creates appropriate check commands that can be undone. This GUI
 * check saves the previous state of the buttons, meaning it always knows which button was
 * previously checked even after it has been unchecked.
 * @section Usage To add buttons to listen to, use Button.addListener(GuiCheckCommandCreator) and it
 * will handle the rest.
 */
public class GuiCheckCommandCreator implements EventListener {
/** Last button that was checked */
private Button mCheckedLast = null;
/** The invoker to send the commands to */
private Invoker mInvoker;

/**
 * Creates an empty GuiCheckeCommandCreator with the specified invoker to send commands to.
 * @param invoker the invoker to send the new commands to
 */
public GuiCheckCommandCreator(Invoker invoker) {
	mInvoker = invoker;
}

@Override
public boolean handle(Event event) {
	Actor target = event.getTarget();
	if (target instanceof Button) {
		// Send command if is checked and not same as last checked button
		if (((Button) target).isChecked() && target != mCheckedLast) {
			sendCommand((Button) target);
		}
	}

	return false;
}

/**
 * Creates a new checked command
 * @param checkedButton the new button that is checked
 */
private void sendCommand(Button checkedButton) {
	Button oldChecked = mCheckedLast;
	mCheckedLast = checkedButton;
	if (oldChecked != null) {
		// Don't send a command if the invoker checked the button
		if (checkedButton.getName() == null || !checkedButton.getName().equals(Gui.GUI_INVOKER_TEMP_NAME)) {
			mInvoker.execute(new CGuiCheck(mCheckedLast, oldChecked));
		}
	}
}
}
