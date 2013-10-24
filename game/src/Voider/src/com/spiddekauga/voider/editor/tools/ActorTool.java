package com.spiddekauga.voider.editor.tools;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.resources.IResource;

/**
 * Common class for all tools which can create actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorTool extends TouchTool implements ISelectTool {
	/**
	 * Creates an actor tool.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param invoker used for undo/redo
	 * @param actorType the actor type to add/remove
	 */
	public ActorTool(Camera camera, World world, Invoker invoker, Class<?> actorType) {
		super(camera, world, invoker);
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

	@Override
	public void addListener(ISelectListener listener) {
		mSelectListeners.add(listener);
	}

	@Override
	public void addListeners(List<ISelectListener> listeners) {
		mSelectListeners.addAll(listeners);
	}

	@Override
	public void removeListener(ISelectListener listener) {
		mSelectListeners.remove(listener);
	}

	@Override
	public void removeListeners(List<ISelectListener> listeners) {
		mSelectListeners.removeAll(listeners);
	}

	@Override
	public void setSelectedResource(IResource selectedResource) {
		if (selectedResource != mSelectedActor) {
			deactivate();

			Actor oldSelected = mSelectedActor;
			mSelectedActor = (Actor) selectedResource;

			for (ISelectListener selectListener : mSelectListeners) {
				selectListener.onResourceSelected(oldSelected, mSelectedActor);
			}

			activate();
		}
	}

	@Override
	public Actor getSelectedResource() {
		return mSelectedActor;
	}

	/**
	 * Deactivates the tool, i.e. it will make any selected actor unselected
	 */
	@Override
	public void deactivate() {
		if (mSelectedActor != null) {
			mSelectedActor.setSelected(false);
		}
	}

	/**
	 * Activates the tool i.e. it will make any selected actor selected
	 */
	@Override
	public void activate() {
		if (mSelectedActor != null) {
			mSelectedActor.setSelected(true);
		}
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
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		// If we hit the selected body, always return that first
		if (!hitBodies.isEmpty()) {
			if (mSelectedActor != null && hitBodies.contains(mSelectedActor.getBody())) {
				return mSelectedActor.getBody();
			} else {
				return hitBodies.get(0);
			}
		} else {
			return null;
		}
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	/** Selected actor */
	protected Actor mSelectedActor = null;
	/** Select listeners */
	private ArrayList<ISelectListener> mSelectListeners = new ArrayList<ISelectListener>();

	/** Picking for current actor type */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();
			// Hit a corner
			if (body.getUserData() instanceof HitWrapper) {
				HitWrapper hitWrapper = (HitWrapper) body.getUserData();
				if (hitWrapper.resource != null && hitWrapper.resource.getClass() == mActorType) {
					mHitBodies.clear();
					mHitBodies.add(fixture.getBody());
					return false;
				}
			}
			// Hit an actor
			else if (body.getUserData() != null && body.getUserData().getClass() == mActorType) {
				mHitBodies.add(body);
			}
			return true;
		}
	};

	/** The actor type that will be created */
	private Class<?> mActorType;
	/** Actor definition to use for the actor, if null it will not set any definition */
	private ActorDef mActorDef = null;
}
