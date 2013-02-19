package com.spiddekauga.voider.scene;

import java.util.List;

import com.spiddekauga.voider.game.actors.Actor;

/**
 * Tool that can select an actor, and thus add select listeners.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface ISelectTool {
	/**
	 * Adds one select listener
	 * @param listener called when an actor is selected
	 */
	void addListener(ISelectListener listener);

	/**
	 * Adds many select listeners
	 * @param listeners list of listeners to call when an actor is selected
	 */
	void addListeners(List<ISelectListener> listeners);

	/**
	 * Removes the specified listener. Does nothing if the listener hasn't been
	 * added.
	 * @param listener the listener to remove.
	 */
	void removeListener(ISelectListener listener);

	/**
	 * Removes all the specified listeners. If a listener in the list hasn't been
	 * added nothing will happen for that listener.
	 * @param listeners list of listeners to remove
	 */
	void removeListeners(List<ISelectListener> listeners);

	/**
	 * Selects the specified actor
	 * @param selectedActor the new actor to select
	 */
	void setSelectedActor(Actor selectedActor);

	/**
	 * @return the selected actor
	 */
	Actor getSelectedActor();
}
