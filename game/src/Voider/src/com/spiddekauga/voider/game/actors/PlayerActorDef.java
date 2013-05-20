package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Player actor definition, does nothing more than specify
 * that the actor is a player
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActorDef extends ActorDef {
	/**
	 * Default constructor
	 */
	public PlayerActorDef() {
		super(ActorTypes.PLAYER);
		setName("Default");
		setMaxLife(100f);
		getBodyDef().type = BodyType.DynamicBody;
		getBodyDef().fixedRotation = true;

		mVisualVars.setShapeType(ActorShapeTypes.CIRCLE);
	}
}
