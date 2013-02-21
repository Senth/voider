package com.spiddekauga.voider.editor;

import com.spiddekauga.voider.game.actors.Actor;

/**
 * Interface for all tools that can create and destroy actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IActorChangeEditor {
	/**
	 * Called when an actor is added
	 * @param actor the actor that was created
	 */
	void onActorAdded(Actor actor);

	/**
	 * Called when an actor is removed
	 * @param actor the actor that was removed
	 */
	void onActorRemoved(Actor actor);

	/**
	 * Called whenever an actor has been changed
	 * @param actor the actor that was changed
	 */
	void onActorChanged(Actor actor);

	/**
	 * Called whenever an actor has been selected/deselected
	 * @param actor the actor that was selected
	 */
	void onActorSelected(Actor actor);
}
