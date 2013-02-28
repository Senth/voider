package com.spiddekauga.voider.game.triggers;

import java.util.List;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.voider.game.IResourceBody;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.triggers.TriggerAction.Reasons;

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

	/** Actor to check if it has been activated */
	private Actor mActor;
	/** Body for the trigger, used for picking */
	private Body mBody = null;
}
