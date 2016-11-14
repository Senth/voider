package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.VisibilityChangeListener.VisibilityChangeEvent;
import com.spiddekauga.voider.Config;

import java.util.ArrayList;

/**
 * Class for hiding GUI elements
 */
public abstract class GuiHider implements Disposable {
/** List of actors to activate/deactivate */
protected ArrayList<Actor> mToggles = new ArrayList<Actor>();
/** True if the actors are visible */
private boolean mVisible = true;
/** Name of the hider, used for debugging purposes */
private String mName = "";
/** Children */
private ArrayList<GuiHider> mChildren = new ArrayList<GuiHider>();
/** Parent hider */
private GuiHider mParent = null;

@Override
public void dispose() {
	mToggles.clear();
	mChildren.clear();
}

/**
 * Adds a child hide listener. This allows child hide listeners to remain hidden when this listener
 * is shown.
 * @param child hide listener, its button shall be either a toggle actor in this class or a child of
 * ones toggle actor.
 */
public void addChild(GuiHider child) {
	mChildren.add(child);
	child.setParent(this);

	if (isVisible()) {
		child.updateToggleActors();
	} else {
		child.mVisible = false;
		child.updateActorVisibility();
	}
}

/**
 * Adds an actor to show/hide when the button is checked/unchecked (depending on how the listener
 * was created (showWhenChecked)).
 * @param toggleActor the actor to show/hide
 */
public void addToggleActor(Actor toggleActor) {
	mToggles.add(toggleActor);
	toggleActor.setVisible(mVisible);
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
 * Called after object visibility has changed
 * @param visible if the object is visible or not
 */
protected void onChange(boolean visible) {
	// Does nothing
}

/**
 * Updates all toggle actors
 */
protected void updateToggleActors() {
	if (shallShowActors() && (mParent == null || mParent.isVisible())) {
		mVisible = true;
		updateActorVisibility();
		onShow();
		onChange(true);
	} else {
		mVisible = false;
		updateActorVisibility();
		onHide();
		onChange(false);
	}
}

/**
 * @return true if the actors should be shown
 */
protected abstract boolean shallShowActors();

/**
 * Hide/show all toggle actors and children, including GuiHider children
 */
protected void updateActorVisibility() {
	for (Actor toggleActor : mToggles) {
		updateActorVisibility(toggleActor);
	}

	for (GuiHider hideListener : mChildren) {
		hideListener.updateToggleActors();
	}
}

/**
 * Hide/Show the specified actor
 * @param actor the actor to hide/show (depends on the state of the hider)
 */
private void updateActorVisibility(Actor actor) {
	actor.setVisible(mVisible);
	fireVisiblilityEvent(actor);

	if (actor instanceof Group) {
		for (Actor child : ((Group) actor).getChildren()) {
			if (!isActorInChild(child)) {
				updateActorVisibility(child);
			}
		}
	}

	if (actor instanceof TextField) {
		if (!mVisible || actor.getName() == null || !actor.getName().equals(Config.Gui.TEXT_FIELD_DISABLED_NAME)) {
			((TextField) actor).setDisabled(!mVisible);
		}
	}

	if (actor instanceof Layout) {
		((Layout) actor).invalidateHierarchy();
	}
}

/**
 * Fire event for the specified actor and all children
 * @param actor the actor to fire the event for
 */
private void fireVisiblilityEvent(Actor actor) {
	actor.fire(new VisibilityChangeEvent());
}

/**
 * @param actor test if the actor is in a child hider (not recursive, no need)
 * @return true if the actor is in a child hider
 */
private boolean isActorInChild(Actor actor) {
	for (GuiHider hideChild : mChildren) {
		for (Actor actorChild : hideChild.mToggles) {
			if (actor == actorChild) {
				return true;
			}
		}
	}

	return false;
}

/**
 * @return true if the actors are visible
 */
public boolean isVisible() {
	return mVisible;
}

/**
 * @return name of the hider
 */
public String getName() {
	return mName;
}

/**
 * Sets the name of the hider
 * @param name the name of the hider
 */
public void setName(String name) {
	mName = name;
}

/**
 * Sets the parent hider
 * @param parent parent hider
 */
private void setParent(GuiHider parent) {
	mParent = parent;
}
}
