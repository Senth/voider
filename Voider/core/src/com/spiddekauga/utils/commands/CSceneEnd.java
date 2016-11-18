package com.spiddekauga.utils.commands;

import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.utils.scene.ui.Scene.Outcomes;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;

/**
 * End the current scene by setting an outcome
 */
public class CSceneEnd extends Command {
private Outcomes mOutcome = Outcomes.NOT_APPLICAPLE;
private Object mMessage = null;

/**
 * End the current active scene with N/A outcome
 */
public CSceneEnd() {
	// Does nothing
}

/**
 * End the current active scene with the specified outcome
 * @param outcome
 */
public CSceneEnd(Outcomes outcome) {
	this(outcome, null);
}

/**
 * End the current active scene with the specified outcome and message
 * @param outcome
 * @param message
 */
public CSceneEnd(Outcomes outcome, Object message) {
	mOutcome = outcome;
	mMessage = message;
}

@Override
public boolean execute() {
	Scene scene = SceneSwitcher.getActiveScene(true);
	if (scene != null) {
		scene.setOutcome(mOutcome, mMessage);
		return true;
	} else {
		return false;
	}
}

@Override
public boolean undo() {
	return false;
}
}
