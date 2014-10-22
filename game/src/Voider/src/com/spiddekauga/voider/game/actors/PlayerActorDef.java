package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.Resource;

/**
 * Player actor definition, does nothing more than specify that the actor is a player
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PlayerActorDef extends ActorDef {
	/**
	 * Default constructor
	 */
	public PlayerActorDef() {
		super(ActorTypes.PLAYER);
		setHealthMax(Config.Actor.Player.HEALTH_MAX);
		getBodyDef().type = BodyType.DynamicBody;
		getBodyDef().fixedRotation = true;

		mVisualVars.setShapeType(ActorShapeTypes.CIRCLE);
	}

	/**
	 * Sets the maximum force the mouse joint can have on this object
	 * @param force maximum mouse joint force
	 */
	public void setMouseJointForceMax(float force) {
		mMouseJointForceMax = force;
	}

	/**
	 * @return maximum mouse joint force on this player ship
	 */
	public float getMouseJointForceMax() {
		return mMouseJointForceMax;
	}

	@Override
	public void set(Resource resource) {
		super.set(resource);

		PlayerActorDef def = (PlayerActorDef) resource;
		mMouseJointForceMax = def.mMouseJointForceMax;
	}

	/** Maximum force a mouse joint can have on this player actor */
	@Tag(125) private float mMouseJointForceMax = 10000;
}
