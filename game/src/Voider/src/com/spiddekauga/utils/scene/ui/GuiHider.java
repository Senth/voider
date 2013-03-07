package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

/**
 * Class for hiding GUI elements
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class GuiHider {
	/**
	 * Adds a child hide listener. This allows child hide listeners
	 * to remain hidden when this listener is shown.
	 * @param child hide listener, its button shall be either a toggle actor
	 * in this class or a child of ones toggle actor.
	 */
	public void addChild(GuiHider child) {
		mChildren.add(child);
	}

	/**
	 * Adds an actor to show/hide when the button is checked/unchecked
	 * (depending on how the listener was created (showWhenChecked)).
	 * @param toggleActor the actor to show/hide
	 */
	public void addToggleActor(Actor toggleActor) {
		mToggles.add(toggleActor);
		toggleActor.setVisible(shallShowActors());
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
	protected void updateToggleActors() {
		if (shallShowActors()) {
			for (Actor toggleActor : mToggles) {
				toggleActor.setVisible(true);

				if (toggleActor instanceof Layout) {
					((Layout) toggleActor).invalidateHierarchy();
				}
			}

			// Shall children remain hidden?
			for (GuiHider hideListener : mChildren) {
				hideListener.updateToggleActors();
			}

			onShow();
		} else {
			hideAll();
			onHide();
		}
	}

	/**
	 * @return true if the actors should be shown
	 */
	protected abstract boolean shallShowActors();

	/**
	 * Hides all actors including children's actors
	 */
	protected void hideAll() {
		for (Actor toggleActor : mToggles) {
			toggleActor.setVisible(false);

			if (toggleActor instanceof Layout) {
				((Layout) toggleActor).invalidateHierarchy();
			}
		}

		for (GuiHider hideListener : mChildren) {
			hideListener.hideAll();
		}
	}

	/** List of actors to activate/deactivate */
	protected ArrayList<Actor> mToggles = new ArrayList<Actor>();
	/** Children */
	private ArrayList<GuiHider> mChildren = new ArrayList<GuiHider>();
}
