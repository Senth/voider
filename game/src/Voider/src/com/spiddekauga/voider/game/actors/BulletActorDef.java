package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.spiddekauga.voider.network.resource.BulletDefEntity;
import com.spiddekauga.voider.network.resource.DefEntity;

/**
 * Bullet actor definition, does nothing more than specify that the actor is a bullet
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BulletActorDef extends ActorDef {
	/**
	 * Default constructor
	 */
	public BulletActorDef() {
		super(ActorTypes.BULLET);

		getBodyDef().type = BodyType.DynamicBody;

		setDestroyOnCollide(true);
	}

	@Override
	protected DefEntity newDefEntity() {
		return new BulletDefEntity();
	}
}
