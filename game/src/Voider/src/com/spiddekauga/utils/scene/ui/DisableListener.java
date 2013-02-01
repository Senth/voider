package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

/**
 * Disables specified GUI elements when a button enabled (checked)
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DisableListener implements EventListener{
	/**
	 * Creates a disable listener that listens to the specified button
	 * if it is enabled or not. Calls onChange(boolean)
	 * @param button listens to this button and activates/deactivates all
	 * actors added to the list
	 */
	public DisableListener(Button button) {
		mButton = button;
		mButton.addListener(this);
		mCheckedLast = mButton.isChecked();
		onChange(!mCheckedLast);
	}

	/**
	 * Adds a actor to activate/deactivate when the button is checked/unchecked
	 * @param toggleActor the actor to activate/deactivate
	 */
	public void addToggleActor(Actor toggleActor) {
		mToggles.add(toggleActor);
		updateActorState(toggleActor);
	}

	@Override
	public final boolean handle(Event event) {
		if (mButton.isChecked() != mCheckedLast) {
			mCheckedLast = mButton.isChecked();
			for (Actor toggleActor : mToggles) {
				updateActorState(toggleActor);
			}
			onChange(!mCheckedLast);
		}
		return true;
	}

	/**
	 * Override this if you want to change something
	 * @param disabled true if the button isn't checked
	 */
	public void onChange(boolean disabled) {
		// Does nothing
	}

	/**
	 * Changes the state of a specified actor to the current state of the button
	 * @param actor the actor to enable disable
	 */
	private void updateActorState(Actor actor) {
		if (actor instanceof Button) {
			((Button) actor).setDisabled(!mCheckedLast);
		} else if (actor instanceof TextField) {
			((TextField) actor).setDisabled(!mCheckedLast);
		} else {
			actor.setTouchable(mCheckedLast ? Touchable.enabled : Touchable.disabled);
		}
	}

	/** Button to listen for */
	protected Button mButton = null;
	/** List of actors to activate/deactivate */
	protected ArrayList<Actor> mToggles = new ArrayList<Actor>();
	/** Last state */
	private boolean mCheckedLast;
}
