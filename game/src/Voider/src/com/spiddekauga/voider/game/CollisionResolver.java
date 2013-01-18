package com.spiddekauga.voider.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * Resolves collisions between two objects, generally a player
 * and something else.
 * @note This does not change the physics of the objects.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CollisionResolver implements ContactListener {

	@Override
	public void beginContact(Contact contact) {
		Body bodyA = contact.getFixtureA().getBody();
		Body bodyB = contact.getFixtureB().getBody();


		// Add collision actors, so they take damage (if possible)
		if (bodyA.getUserData() instanceof Actor && bodyB.getUserData() instanceof Actor) {
			Actor actorA = (Actor)bodyA.getUserData();
			Actor actorB = (Actor)bodyB.getUserData();
			actorA.addCollidingActor(actorB.getDef());
			actorB.addCollidingActor(actorA.getDef());
		}
	}

	@Override
	public void endContact(Contact contact) {
		Body bodyA = contact.getFixtureA().getBody();
		Body bodyB = contact.getFixtureB().getBody();

		// Remove collision from actors
		if (bodyA.getUserData() instanceof Actor && bodyB.getUserData() instanceof Actor) {
			Actor actorA = (Actor)bodyA.getUserData();
			Actor actorB = (Actor)bodyB.getUserData();
			actorA.removeCollidingActor(actorB.getDef());
			actorB.removeCollidingActor(actorA.getDef());
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// Does nothing
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// Does nothing
	}
}
