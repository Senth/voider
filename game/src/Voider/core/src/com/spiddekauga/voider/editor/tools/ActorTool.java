package com.spiddekauga.voider.editor.tools;

import java.lang.reflect.Constructor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.scene.Scene;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract public class ActorTool extends TouchTool {

	/**
	 * @param editor
	 * @param selection all selected resources
	 * @param actorType the type of actor this tool uses
	 */
	public ActorTool(IResourceChangeEditor editor, ISelection selection, Class<? extends Actor> actorType) {
		super(editor, selection);
		mActorType = actorType;

		mSelectableResourceTypes.add(mActorType);
	}

	/**
	 * Creates a new actor of the current actor type via the default constructor. If an
	 * actor definition has been set, this will also set that definition, else you need to
	 * set this manually if it hasn't been set through the actor's default constructor.
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
	 * @return the newly created and selected actor
	 */
	protected Actor createNewSelectedActor() {
		Actor actor = newActor();
		actor.setPosition(mTouchOrigin);
		mInvoker.execute(new CResourceAdd(actor, mEditor));
		mInvoker.execute(new CSelectionSet(mSelection, actor), true);
		mCreatedActorThisDown = true;
		return actor;
	}

	/**
	 * Get local position for the specified actor from the specified world position
	 * @param worldPos world position
	 * @param actor the actor to calculate the position from
	 * @return Local position of the actor, copy of worldPos if actor is null.
	 */
	protected static Vector2 getLocalPosition(Vector2 worldPos, Actor actor) {
		Vector2 localPos = new Vector2(worldPos);

		if (actor != null) {
			localPos.sub(actor.getPosition()).sub(actor.getDef().getVisual().getCenterOffset());
		}

		return localPos;
	}

	/**
	 * Get world position from the specified actor
	 * @param localPos local position
	 * @param actor the actor to calculate the position from
	 * @return world position of the actor, copy of localPos if actor is null.
	 */
	protected static Vector2 getWorldPosition(Vector2 localPos, Actor actor) {
		Vector2 worldPos = new Vector2(localPos);

		if (actor != null) {
			worldPos.add(actor.getPosition()).add(actor.getDef().getVisual().getCenterOffset());
		}

		return worldPos;
	}

	/**
	 * Sets the actor definition to use for new actors
	 * @param actorDef actor definition to use
	 */
	public void setActorDef(ActorDef actorDef) {
		mActorDef = actorDef;
	}

	/**
	 * @return the actor def used when creating new actors
	 */
	public ActorDef getActorDef() {
		return mActorDef;
	}

	/**
	 * Helper method to get the visual options for the current editor
	 * @return visual options for the current editor
	 */
	protected IC_Visual getVisualConfig() {
		return ConfigIni.getInstance().editor.actor.getVisual((Scene) mEditor);
	}

	/**
	 * Tests whether the pointer have moved enough to add another corner
	 * @param dragOrigin where we started to drag the
	 * @return true if we shall add another corner.
	 */
	protected boolean haveMovedEnoughToAddAnotherCorner(Vector2 dragOrigin) {
		boolean movedEnough = false;

		float drawNewCornerMinDistSq = getVisualConfig().getDrawNewCornerDistMinSq();

		// If has drawn more than minimum distance, add another corner here
		Vector2 diffVector = new Vector2(mTouchCurrent).sub(dragOrigin);
		if (diffVector.len2() >= drawNewCornerMinDistSq) {
			movedEnough = true;
		}

		return movedEnough;
	}


	/** Created an actor this turn */
	protected boolean mCreatedActorThisDown = false;
	/** Actor definition */
	protected ActorDef mActorDef = null;
	/** Actor type */
	protected Class<? extends Actor> mActorType;
}
