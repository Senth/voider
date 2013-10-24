package com.spiddekauga.voider.editor.tools;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CResourceSelect;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.utils.Pools;

/**
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
abstract public class ActorTool extends TouchTool {

	/**
	 * @param camera
	 * @param world
	 * @param invoker
	 * @param selection
	 * @param editor can be null
	 * @param actorType the type of actor this tool uses
	 */
	public ActorTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor, Class<? extends Actor> actorType) {
		super(camera, world, invoker, selection, editor);
	}

	@Override
	protected QueryCallback getCallback() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		// TODO Auto-generated method stub
		return null;
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
			Gdx.app.error("ActorTool", e.toString());
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a new selected actor
	 */
	protected void createNewSelectedActor() {
		Actor actor = newActor();
		if (mActorDef != null) {
			actor.setDef(mActorDef);
		}
		actor.setPosition(mTouchOrigin);
		mInvoker.execute(new CResourceAdd(actor, mEditor));
		mInvoker.execute(new CResourceSelect(actor, mSelection));
	}

	/**
	 * Get local position for the specified actor from the specified world position
	 * @param worldPos world position
	 * @param actor the actor to calculate the position from
	 * @return Local position of the actor, copy of worldPos if actor is null.
	 * Don't forget to free the localPos using Pools.vector2.free(localPos)
	 */
	protected static Vector2 getLocalPosition(Vector2 worldPos, Actor actor) {
		Vector2 localPos = Pools.vector2.obtain();
		localPos.set(worldPos);

		if (actor != null) {
			localPos.sub(actor.getPosition()).sub(actor.getDef().getVisualVars().getCenterOffset());
		}

		return localPos;
	}

	/**
	 * Get world position from the specified actor
	 * @param localPos local position
	 * @param actor the actor to calculate the position from
	 * @return world position of the actor, copy of localPos if actor is null.
	 * Don't forget to free the worldPos using Pools.vector2.free(worldPos);
	 */
	protected static Vector2 getWorldPosition(Vector2 localPos, Actor actor) {
		Vector2 worldPos = Pools.vector2.obtain();
		worldPos.set(localPos);

		if (actor != null) {
			worldPos.add(actor.getPosition()).add(actor.getDef().getVisualVars().getCenterOffset());
		}

		return worldPos;
	}

	/** Actor definition, used when only drawing on one actor */
	protected ActorDef mActorDef = null;
	/** Actor type */
	protected Class<? extends Actor> mActorType;
}
