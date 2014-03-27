package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.spiddekauga.voider.Config;

/**
 * Class for hiding GUI elements
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class GuiHider implements Disposable {
	@Override
	public void dispose() {
		mToggles.clear();
		mChildren.clear();
	}

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

				if (toggleActor instanceof Group) {
					enableDisableActorTextFields((Group) toggleActor, false);
				}

				if (toggleActor instanceof TextField) {
					if (toggleActor.getName() == null || !toggleActor.getName().equals(Config.Gui.TEXT_FIELD_DISABLED_NAME)) {
						((TextField) toggleActor).setDisabled(false);
					}
				}

				if (toggleActor instanceof Layout) {
					((Layout) toggleActor).invalidateHierarchy();
				}
			}

			// Shall children remain hidden?
			for (GuiHider hideListener : mChildren) {
				hideListener.updateToggleActors();
			}

			mVisible = true;
			onShow();
		} else {
			hideAll();
			mVisible = false;
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

			if (toggleActor instanceof Group) {
				enableDisableActorTextFields((Group) toggleActor, true);
			}

			if (toggleActor instanceof TextField) {
				((TextField) toggleActor).setDisabled(true);
			}

			if (toggleActor instanceof Layout) {
				((Layout) toggleActor).invalidateHierarchy();
			}
		}

		for (GuiHider hideListener : mChildren) {
			hideListener.hideAll();
		}
	}

	/**
	 * Enable/Disable the actor's text fields if the actor has any children
	 * @param actor the actor to enable/disable the text fields of
	 * @param disable set to true to disable the text fields
	 */
	private void enableDisableActorTextFields(Group actor, boolean disable) {
		SnapshotArray<Actor> children = actor.getChildren();

		for (Actor child : children) {
			if (child instanceof TextField) {
				if (disable || child.getName() == null || !child.getName().equals(Config.Gui.TEXT_FIELD_DISABLED_NAME)) {
					((TextField) child).setDisabled(disable);
				}
			}

			if (child instanceof Group) {
				enableDisableActorTextFields((Group) child, disable);
			}
		}
	}

	/**
	 * @return true if the actors are visible
	 */
	public boolean isVisible() {
		return mVisible;
	}

	/**
	 * Sets the name of the hider
	 * @param name the name of the hider
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * @return name of the hider
	 */
	public String getName() {
		return mName;
	}

	/** True if the actors are visible */
	private boolean mVisible = true;
	/** Name of the hider, used for debugging purposes */
	private String mName = "";
	/** List of actors to activate/deactivate */
	protected ArrayList<Actor> mToggles = new ArrayList<Actor>();
	/** Children */
	private ArrayList<GuiHider> mChildren = new ArrayList<GuiHider>();
}
