package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

/**
 * Listener for VisibilityChangeEvent
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class VisibilityChangeListener implements EventListener {

	@Override
	public boolean handle(Event event) {
		if (!(event instanceof VisibilityChangeEvent)) {
			return false;
		}
		onVisibilyChange((VisibilityChangeEvent) event, event.getTarget());
		return false;
	}

	/**
	 * Called when the visibility was changed
	 * @param event the event that fired the visibility change
	 * @param actor visibility was changed in this actor
	 */
	public abstract void onVisibilyChange(VisibilityChangeEvent event, Actor actor);

	/**
	 * Visibility of the actor was changed
	 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
	 */
	public static class VisibilityChangeEvent extends ChangeEvent {

	}
}
