package com.spiddekauga.voider.scene;

import com.spiddekauga.voider.game.actors.Actor;

/**
 * Interface for listening to when a tool selects/deselects an actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface ISelectListener {
	/**
	 * Called when an actor is selected
	 * @param deselectedActor the previous selected actor that will be deselected.
	 * null if no actor was previously selected
	 * @param selectedActor the selected actor, null if an actor was deselected
	 */
	void onActorSelect(Actor deselectedActor, Actor selectedActor);
}
