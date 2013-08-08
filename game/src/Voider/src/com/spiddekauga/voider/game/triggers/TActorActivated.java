package com.spiddekauga.voider.game.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.utils.JsonWrapper; import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.triggers.TriggerAction.Reasons;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.IResourceChangeListener;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

/**
 * Triggered when an actor is activated, or otherwise active
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TActorActivated extends Trigger implements Disposable, IResourceBody, IResourceChangeListener {
	/**
	 * Triggers when the actor is active (or activated)
	 * @param actor the actor that shall be activate
	 */
	public TActorActivated(Actor actor) {
		mActor = actor;
		mActorId = actor.getId();
		setActorListener();
	}

	@Override
	public void createBody() {
		if (mBody == null && !isHidden()) {
			List<FixtureDef> fixtures = mActor.getDef().getVisualVars().getFixtureDefs();

			mBody = Actor.getWorld().createBody(new BodyDef());

			for (FixtureDef fixtureDef : fixtures) {
				mBody.createFixture(fixtureDef);
			}

			mBody.setTransform(mActor.getPosition(), mActor.getDef().getBodyDef().angle);
			mBody.setUserData(this);

			createVertices();
		}
	}

	@Override
	public void destroyBody() {
		if (mBody != null) {
			mBody.getWorld().destroyBody(mBody);
		}
		destroyVertices();
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		if (mVertices != null && mActor != null) {
			shapeRenderer.setColor(Config.Editor.Level.Trigger.COLOR);
			Vector2 offsetPosition = Pools.vector2.obtain();
			offsetPosition.set(mActor.getPosition()).add(mActor.getDef().getVisualVars().getCenterOffset());
			shapeRenderer.triangles(mVertices, offsetPosition);

			if (isSelected()) {
				shapeRenderer.setColor(Config.Editor.SELECTED_COLOR);
				shapeRenderer.triangles(mVertices, offsetPosition);
			}

			Pools.vector2.free(offsetPosition);
		}
	}

	@Override
	protected Reasons getReason() {
		return Reasons.ACTOR_ACTIVATED;
	}

	@Override
	protected Object getCauseObject() {
		return mActor;
	}

	@Override
	public boolean isTriggered() {
		return mActor.isActive();
	}

	@Override
	public void write(Json json) {
		super.write(json);
		json.writeValue("mActorId", mActorId);
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		super.read(json, jsonValue);
		mActorId = json.readValue("mActorId", UUID.class, jsonValue);
	}

	@Override
	public void getReferences(ArrayList<UUID> references) {
		super.getReferences(references);

		references.add(mActorId);
	}

	@Override
	public boolean bindReference(IResource resource) {
		boolean success = super.bindReference(resource);

		if (resource.equals(mActorId)) {
			mActor = (Actor) resource;
			success = true;
			setActorListener();
		}

		return success;
	}

	@Override
	public boolean addBoundResource(IResource boundResource)  {
		boolean success = super.addBoundResource(boundResource);

		if (boundResource instanceof Actor) {
			mActor = (Actor)boundResource;
			mActorId = mActor.getId();

			setActorListener();
		}

		return success;
	}

	@Override
	public boolean removeBoundResource(IResource boundResource) {
		boolean success = super.removeBoundResource(boundResource);

		if (boundResource.getId().equals(mActorId)) {
			mActor = null;
			mActorId = null;

			if (Actor.isEditorActive()) {
				boundResource.removeChangeListener(this);
			}
		}

		return success;
	}

	@Override
	public void onResourceChanged(IResource resource, EventTypes type) {
		if (resource == mActor && type == EventTypes.POSITION && mBody != null) {
			mBody.setTransform(mActor.getPosition(), mActor.getBody().getAngle());
		}
	}

	@Override
	public void dispose() {
		destroyBody();
	}

	/**
	 * Constructor for JSON
	 */
	protected TActorActivated() {
		// Does nothing
	}

	/**
	 * Adds the listener if the editor is active
	 */
	private void setActorListener() {
		if (Actor.isEditorActive()) {
			mActor.addChangeListener(this);
		}
	}

	/**
	 * Creates visual representation of the trigger
	 */
	private void createVertices() {
		if (!isHidden()) {
			destroyVertices();

			ArrayList<Vector2> polygon = new ArrayList<Vector2>();
			ArrayList<Vector2> actorShape = mActor.getDef().getVisualVars().getPolygonShape();
			if (actorShape != null) {
				// Copy polygon from actor, so we don't free the actor's vectors when freeing
				// this trigger
				for (Vector2 vertex : actorShape) {
					polygon.add(Pools.vector2.obtain().set(vertex));
				}
				ArrayList<Vector2> borderCorners = Geometry.createdBorderCorners(polygon, false, Config.Editor.Level.Trigger.ENEMY_WIDTH);

				mVertices = Geometry.createBorderVertices(polygon, borderCorners);
			}
		}
	}

	/**
	 * Destroys the visual representation of the trigger
	 */
	private void destroyVertices() {
		if (mVertices != null) {
			Pools.vector2.freeDuplicates(mVertices);
			mVertices = null;
		}
	}

	/** Vertices for drawing the trigger */
	private ArrayList<Vector2> mVertices = null;
	/** Actor to check if it has been activated */
	private Actor mActor = null;
	/**	Actor id */
	private UUID mActorId = null;
	/** Body for the trigger, used for picking */
	private Body mBody = null;
}
