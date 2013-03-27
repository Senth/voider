package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.voider.Config.Editor.Bullet;

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
		super(ActorTypes.BULLET);

		getBodyDef().type = BodyType.DynamicBody;

		setShapeType(Bullet.Visual.SHAPE_DEFAULT);
	}

	@Override
	protected FixtureDef getDefaultFixtureDef() {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.1f;
		fixtureDef.density = 0.001f;
		return fixtureDef;
	}
}
