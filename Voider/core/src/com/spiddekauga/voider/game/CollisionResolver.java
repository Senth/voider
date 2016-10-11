package com.spiddekauga.voider.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Resolves collisions between two objects, generally a player and something else.
 * @note This does not change the physics of the objects.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CollisionResolver implements ContactListener {

	@Override
	public void beginContact(Contact contact) {
		Body bodyA = contact.getFixtureA().getBody();
		Body bodyB = contact.getFixtureB().getBody();


		// Add collision actors, so they take damage (if possible)
		if (bodyA.getUserData() instanceof Actor && bodyB.getUserData() instanceof Actor) {
			Actor actorA = (Actor) bodyA.getUserData();
			Actor actorB = (Actor) bodyB.getUserData();


			// Do nothing if body is being destroyed
			if (actorA.shallBodyBeDestroyed() || actorB.shallBodyBeDestroyed()) {
				return;
			}

			// If one shall be destroyed directly
			Actor destroyActor = null;
			Actor actor = null;
			if (actorA.getDef().isDestroyedOnCollide()) {
				destroyActor = actorA;
				actor = actorB;
			} else if (actorB.getDef().isDestroyedOnCollide()) {
				destroyActor = actorB;
				actor = actorA;
			}

			if (destroyActor != null) {
				float damage = destroyActor.getDef().getCollisionDamage();

				// Use hit damage from bullet instead
				if (destroyActor instanceof BulletActor) {
					damage = ((BulletActor) destroyActor).getHitDamage();
					if (actor instanceof PlayerActor) {
						mEventDispatcher.fire(new GameEvent(EventTypes.GAME_PLAYER_HIT_BY_BULLET));
					}
				} else if (destroyActor instanceof EnemyActor) {
					mEventDispatcher.fire(new GameEvent(EventTypes.GAME_ENEMY_EXPLODED));
				}

				actor.decreaseHealth(damage);
				destroyActor.destroyBodySafe();
				return;
			}


			// Transfer collectible to player
			// A collectible, B player
			PlayerActor playerActor = null;
			Actor collectibleActor = null;
			if (actorA.getDef().getCollectible() != null && actorB instanceof PlayerActor) {
				playerActor = (PlayerActor) actorB;
				collectibleActor = actorA;
			}
			// A player, B collectible
			else if (actorA instanceof PlayerActor && actorB.getDef().getCollectible() != null) {
				playerActor = (PlayerActor) actorA;
				collectibleActor = actorB;
			}

			// Transfer then destroy the collectible
			if (playerActor != null && collectibleActor != null) {
				playerActor.addCollectible(collectibleActor.getDef().getCollectible());
				collectibleActor.dispose();
				return;
			}

			// Have not been handled yet
			actorA.addCollidingActor(actorB);
			actorB.addCollidingActor(actorA);
		}
	}


	@Override
	public void endContact(Contact contact) {
		if (contact.getFixtureA() == null || contact.getFixtureA().getBody() == null || contact.getFixtureB() == null
				|| contact.getFixtureB().getBody() == null) {
			return;
		}

		Body bodyA = contact.getFixtureA().getBody();
		Body bodyB = contact.getFixtureB().getBody();

		// Remove collision from actors
		if (bodyA.getUserData() instanceof Actor && bodyB.getUserData() instanceof Actor) {
			Actor actorA = (Actor) bodyA.getUserData();
			Actor actorB = (Actor) bodyB.getUserData();

			// If one shall be destroyed on collide...
			if (actorA.getDef().isDestroyedOnCollide() || actorB.getDef().isDestroyedOnCollide()) {
				return;
			}

			// Collectible
			if ((actorA instanceof PlayerActor && actorB.getDef().getCollectible() != null)
					|| (actorB instanceof PlayerActor && actorA.getDef().getCollectible() != null)) {
				return;
			}

			actorA.removeCollidingActor(actorB);
			actorB.removeCollidingActor(actorA);
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

	private static EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
}
