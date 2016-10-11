package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Ship;
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

	/**
	 * Create a mouse joint definition
	 * @param mouseBody body of the mouse
	 * @param playerBody body of the player
	 * @return new mouse joint definition
	 */
	MouseJointDef createMouseJointDef(Body mouseBody, Body playerBody) {
		IC_Ship.IC_Settings icSettings = ConfigIni.getInstance().editor.ship.settings;
		MouseJointDef mouseJointDef = new MouseJointDef();

		mouseJointDef.frequencyHz = icSettings.getFrequencyDefault();
		mouseJointDef.bodyA = mouseBody;
		mouseJointDef.bodyB = playerBody;
		mouseJointDef.collideConnected = true;
		mouseJointDef.maxForce = getMouseJointForceMax();

		return mouseJointDef;
	}


	/** Maximum force a mouse joint can have on this player actor */
	@Tag(125) private float mMouseJointForceMax = 10000;
}
