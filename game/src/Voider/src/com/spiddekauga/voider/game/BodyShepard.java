package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Creates/Destroys bodies of the actors in the level. When an actor's body
 * is destroyed the actor will also be killed.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BodyShepard {


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
		// TODO
	}


	/** All the actors which bodies to create/destroy */
	private ArrayList<Actor> mActors;
}
