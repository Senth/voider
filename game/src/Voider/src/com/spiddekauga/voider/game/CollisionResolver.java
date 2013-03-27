package com.spiddekauga.voider.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.PlayerActor;

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

			// If one of the actors is a bullet, destroy the bullet and directly
			// decrease the life
			BulletActor bullet = null;
			Actor actor = null;
			if (actorA instanceof BulletActor) {
				bullet = (BulletActor)actorA;
				actor = actorB;
			}
			else if (actorB instanceof BulletActor) {
				bullet = (BulletActor)actorB;
				actor = actorA;
			}

			if (bullet != null) {
				actor.decreaseLife(bullet.getHitDamage());
				bullet.deactivate();
				return;
			}


			// Transfer collectible to player
			// A collectible, B player
			PlayerActor playerActor = null;
			Actor collectibleActor = null;
			if (actorA.getDef().getCollectible() != null && actorB instanceof PlayerActor) {
				playerActor = (PlayerActor)actorB;
				collectibleActor = actorA;
			}
			// A player, B collectible
			else if (actorA instanceof PlayerActor && actorB.getDef().getCollectible() != null) {
				playerActor = (PlayerActor)actorA;
				collectibleActor = actorB;
			}

			// Transfer then destroy the collectible
			if (playerActor != null && collectibleActor != null) {
				playerActor.addCollectible(collectibleActor.getDef().getCollectible());
				collectibleActor.dispose();
				return;
			}


			// Have not been handled yet
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

			// Bullet, has been handled differently
			if (actorA instanceof BulletActor || actorB instanceof BulletActor) {
				return;
			}

			// Collectible
			if ((actorA instanceof PlayerActor && actorB.getDef().getCollectible() != null) ||
					(actorB instanceof PlayerActor && actorA.getDef().getCollectible() != null)) {
				return;
			}

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
