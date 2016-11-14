package com.spiddekauga.voider.game;

import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Health was changed in an actor
 */
public class HealthChangeEvent extends GameEvent {
/** Life before it was changed */
public float oldHealth;
/** Actor which health was changed */
public Actor actor;
/**
 * @param actor the actor who's health was changed
 * @param oldHealth health before it was changed
 */
public HealthChangeEvent(Actor actor, float oldHealth) {
	super(EventTypes.GAME_ACTOR_HEALTH_CHANGED);
	this.actor = actor;
	this.oldHealth = oldHealth;
}
}
