package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

/**
 * Hides specified GUI elements when a button is enabled/disabled
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class HideListener implements EventListener {
	/**
	 * Creates a hide listener, listens to the specified button if it's enabled
	 * or not. Calls onHide() after the actors get hidden, and onShow() after
	 * they are shown.
	 * @param button the button to listen for (checked/unchecked)
	 * @param showWhenChecked set to true to show the actors when this is checked.
	 * Set to false to show the actors when the button is unchecked.
	 */
	public HideListener(Button button, boolean showWhenChecked) {
		setButton(button);
		mShowWhenChecked = showWhenChecked;
	}

	/**
	 * Creates a hide listener. Calls onHide() after the actors get hidden, and onShow() after
	 * they are shown.
	 * @param showWhenChecked set to true to show the actors when this is checked.
	 * @note remember to set a button via #setButton(Button)
	 */
	public HideListener(boolean showWhenChecked) {
		mShowWhenChecked = showWhenChecked;
	}

	/**
	 * Sets the button to listen to. Also corrects the state of the actors to toggle
	 * @param button the button to listen for (checked/unchecked)
	 */
	public void setButton(Button button) {
		if (mButton != null) {
			mButton.removeListener(this);
		}

		mButton = button;

		if (mButton != null) {
			mButton.addListener(this);
			mCheckedLast = mButton.isChecked();
			updateToggleActors();
		}
	}

	/**
	 * Adds a child hide listener. This allows child hide listeners
	 * to remain hidden when this listener is shown.
	 * @param child hide listener, its button shall be either a toggle actor
	 * in this class or a child of ones toggle actor.
	 */
	public void addChild(HideListener child) {
		mChildren.add(child);
	}

	/**
	 * Adds an actor to show/hide when the button is checked/unchecked
	 * (depending on how the listener was created (showWhenChecked)).
	 * @param toggleActor the actor to show/hide
	 */
	public void addToggleActor(Actor toggleActor) {
		mToggles.add(toggleActor);
		if (mButton != null) {
			toggleActor.setVisible(shallShowActors());
		}
	}

	@Override
	public final boolean handle(Event event) {
		if (mButton.isChecked() != mCheckedLast) {
			mCheckedLast = mButton.isChecked();
			updateToggleActors();
		}
		return true;
	}

	/**
	 * Called after objects are hidden
	 */
	protected void onHide() {
		// Does nothing
	}

	/**
	 * Called after objects are shown
	 */
	protected void onShow() {
		// Does nothing
	}

	/**
	 * Updates all toggle actors
	 */
	private void updateToggleActors() {
		if (shallShowActors()) {
			for (Actor toggleActor : mToggles) {
				toggleActor.setVisible(true);
			}

			// Shall children remain hidden?
			for (HideListener hideListener : mChildren) {
				hideListener.updateToggleActors();
			}

			onShow();
		} else {
			for (Actor toggleActor : mToggles) {
				toggleActor.setVisible(false);
			}
			onHide();
		}
	}

	/**
	 * @return true if the actors should be shown
	 */
	private boolean shallShowActors() {
		return (mButton.isChecked() && mShowWhenChecked) || (!mButton.isChecked() && !mShowWhenChecked);
	}

	/** Button to listen for */
	protected Button mButton = null;
	/** List of actors to activate/deactivate */
	protected ArrayList<Actor> mToggles = new ArrayList<Actor>();
	/** Children */
	private ArrayList<HideListener> mChildren = new ArrayList<HideListener>();
	/** Shows the actors when the button is checked */
	private boolean mShowWhenChecked;
	/** Last state */
	private boolean mCheckedLast;
}
