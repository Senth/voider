package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.voider.game.ActorDef;

/**
 * Bullet actor definition, does nothing more than specify that
 * the actor is a bullet
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletActorDef extends ActorDef {
	/**
	 * Default constructor
	 */
	public BulletActorDef() {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.1f;
		fixtureDef.density = 0.001f;
		EdgeShape shape = new EdgeShape();
		shape.set(0, 0, 1, 0);
		fixtureDef.shape = shape;
		addFixtureDef(fixtureDef);
	}

	/**
	 * @return category of the bullet, can be either enemy or player, depends on
	 * who shot the bullet
	 */
	@Override
	protected short getFilterCategory() {
		return 0;
	}

	/**
	 * Collides with static terrain and either player or enemy, dependending who
	 * shot the bullet.
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return FixtureFilterCategories.STATIC_TERRAIN;
	}
}
