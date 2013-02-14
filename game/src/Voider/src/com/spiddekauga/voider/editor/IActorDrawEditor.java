package com.spiddekauga.voider.editor;

import com.spiddekauga.voider.game.actors.Actor;

/**
 * Interface for the actor draw tool
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IActorDrawEditor {
	/**
	 * Called when an actor is added (not same as #newActor())
	 * @param actor the actor that was created
	 */
	void onActorAdded(Actor actor);

	/**
	 * Called when an actor is removed
	 * @param actor the actor that was removed
	 */
	void onActorRemoved(Actor actor);
}
