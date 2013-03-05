package com.spiddekauga.voider.game.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.game.IResourceBody;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.triggers.TriggerAction.Reasons;
import com.spiddekauga.voider.resources.IResource;

/**
 * Triggered when an actor is activated, or otherwise active
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TActorActivated extends Trigger implements IResourceBody {
	/**
	 * Triggers when the actor is active (or activated)
	 * @param actor the actor that shall be activate
	 */
	public TActorActivated(Actor actor) {
		mActor = actor;
		mActorId = actor.getId();
	}

	@Override
	public void createBody() {
		if (mBody == null) {
			List<FixtureDef> fixtures = mActor.getDef().getFixtureDefs();

			mBody = Actor.getWorld().createBody(new BodyDef());

			for (FixtureDef fixtureDef : fixtures) {
				mBody.createFixture(fixtureDef);
			}

			mBody.setTransform(mActor.getPosition(), mActor.getBody().getAngle());
			mBody.setUserData(this);
		}
	}

	@Override
	public void destroyBody() {
		if (mBody != null) {
			mBody.getWorld().destroyBody(mBody);
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
	protected boolean isTriggered() {
		return mActor.isActive();
	}

	@Override
	public void write(Json json) {
		super.write(json);
		json.writeValue("mActorId", mActorId);
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);
		mActorId = json.readValue("mActorId", UUID.class, jsonData);
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
		}

		return success;
	}

	@Override
	public boolean addBoundResource(IResource boundResource)  {
		boolean success = super.addBoundResource(boundResource);

		if (boundResource instanceof Actor) {
			mActor = (Actor)boundResource;
			mActorId = mActor.getId();
		}

		return success;
	}

	@Override
	public boolean removeBoundResource(IResource boundResource) {
		boolean success = super.removeBoundResource(boundResource);

		if (boundResource == mActor) {
			mActor = null;
			mActorId = null;
		}

		return success;
	}

	/**
	 * Constructor for JSON
	 */
	protected TActorActivated() {
		// Does nothing
	}

	/** Actor to check if it has been activated */
	private Actor mActor = null;
	/**	Actor id */
	private UUID mActorId = null;
	/** Body for the trigger, used for picking */
	private Body mBody = null;
}
