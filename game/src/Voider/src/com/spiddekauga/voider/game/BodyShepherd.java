package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;

/**
 * Creates/Destroys bodies of the actors in the level. When an actor's body
 * is destroyed the actor will also be killed.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BodyShepherd {


	/**
	 * Sets the actors to create/destroy bodies of
	 * @param actors all actors to create destroy the bodies of
	 */
	public void setActors(ArrayList<Actor> actors) {
		mActors = actors;
	}

	/**
	 * Checks whether the actor's body shall be killed or not
	 * All actors between minPos and maxPos will have their bodies created.
	 * This method takes into account the size of the actor's fixtures.
	 * @param minPos minimum position where actor's bodies shall be created/destroyed
	 * @param maxPos maximum position where actor's bodies shall be created/destroyed
	 */
	public void update(Vector2 minPos, Vector2 maxPos) {
		// Create bodies
		for (Actor actor : mActors) {
			// Skip enemies
			if (actor instanceof EnemyActor) {
				continue;
			}

			// Test to create body
			if (actor.getBody() == null) {
				if (isActorWithinMinMax(minPos, maxPos, actor)) {
					actor.createBody();
				}
			}
			// Test to destroy body
			else {
				if (!isActorWithinMinMax(minPos, maxPos, actor)) {
					actor.destroyBody();
				}
			}
		}
	}

	/**
	 * Tests if some part of the actor is within the min-max positions
	 * @param minPos minimum position
	 * @param maxPos maximum position
	 * @param actor the actor to test if it's between minimum and maximum positions
	 * @return true if some part of the actor might be between minimum and maximum position
	 */
	private boolean isActorWithinMinMax(Vector2 minPos, Vector2 maxPos, Actor actor) {
		Vector2 actorPos = actor.getPosition();
		float actorRadius = actor.getDef().getVisualVars().getBoundingRadius();

		// Left
		if (actorPos.x + actorRadius < minPos.x) {
			return false;
		}
		// Right
		else if (actorPos.x - actorRadius > maxPos.x) {
			return false;
		}
		// Bottom
		else if (actorPos.y + actorRadius < minPos.y) {
			return false;
		}
		// Top
		else if (actorPos.y - actorRadius > maxPos.y) {
			return false;
		}

		return true;
	}


	/** All the actors which bodies to create/destroy */
	private ArrayList<Actor> mActors;
}
