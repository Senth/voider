package com.spiddekauga.utils.commands;

import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.Scene.Outcomes;

/**
 * End the current scene by setting an outcome
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CSceneEnd extends Command {
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

	private Outcomes mOutcome = Outcomes.NOT_APPLICAPLE;
	private Object mMessage = null;
}
