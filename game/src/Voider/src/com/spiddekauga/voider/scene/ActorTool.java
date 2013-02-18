package com.spiddekauga.voider.scene;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Common class for all tools which can create actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorTool extends TouchTool {
	/**
	 * Creates an actor tool.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param actorType the actor type to add/remove
	 */
	public ActorTool(Camera camera, World world, Class<?> actorType) {
		super(camera, world);
		mActorType = actorType;
	}

	/**
	 * Sets the definition to use for creating the actor using #newActor()
	 * @param actorDef set this to the actor definition you want to use. If set
	 * to null no actorDef will be set. This can be OK for actors which create
	 * their own actor definitions through the default constructor.
	 */
	public void setNewActorDef(ActorDef actorDef) {
		mActorDef = actorDef;
	}

	/**
	 * @return current actor definition used when creating new actors
	 */
	public ActorDef getNewActorDef() {
		return mActorDef;
	}

	/**
	 * Creates a new actor of the current actor type via the default constructor.
	 * If an actor definition has been set, this will also set that definition,
	 * else you need to set this manually if it hasn't been set through the
	 * actor's default constructor.
	 * @return new actor of the current actor type.
	 */
	protected Actor newActor() {
		try {
			Constructor<?> constructor = mActorType.getConstructor();
			Actor actor = (Actor) constructor.newInstance();
			actor.setSkipRotating(true);

			if (mActorDef != null) {
				actor.setDef(mActorDef);
			}

			return actor;

		} catch (Exception e) {
			Gdx.app.error("DrawActorTool", e.toString());
		}

		return null;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		// Because the query only will return one body, set the hit body
		// as the first from hit bodies
		if (!hitBodies.isEmpty()) {
			return hitBodies.get(0);
		} else {
			return null;
		}
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	/** Picking for current actor type */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.testPoint(mTouchCurrent)) {
				Body body = fixture.getBody();
				// Hit a corner
				if (body.getUserData() instanceof HitWrapper) {
					HitWrapper hitWrapper = (HitWrapper) body.getUserData();
					if (hitWrapper.actor != null && hitWrapper.actor.getClass() == mActorType) {
						mHitBodies.clear();
						mHitBodies.add(fixture.getBody());
						return false;
					}
				}
				// Hit an actor
				else if (body.getUserData() != null && body.getUserData().getClass() == mActorType) {
					mHitBodies.add(body);
				}
			}
			return true;
		}
	};

	/** The actor type that will be created */
	private Class<?> mActorType;
	/** Actor definition to use for the actor, if null it will not set any definition */
	private ActorDef mActorDef = null;
}
