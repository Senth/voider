package com.spiddekauga.voider.game.triggers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.kryo.KryoPostRead;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.triggers.TriggerAction.Reasons;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.IResourceChangeListener;
import com.spiddekauga.voider.resources.IResourcePrepareWrite;
import com.spiddekauga.voider.utils.Geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Triggered when an actor is activated, or otherwise active
 */
public class TActorActivated extends Trigger implements KryoPostRead, Disposable, IResourceBody, IResourcePrepareWrite, IResourceChangeListener {
/** Vertices for drawing the trigger */
private List<Vector2> mVertices = null;
/** Actor to check if it has been activated */
@Tag(33)
private Actor mActor = null;
/** Body for the trigger, used for picking */
private Body mBody = null;

/**
 * Triggers when the actor is active (or activated)
 * @param actor the actor that shall be activate
 */
public TActorActivated(Actor actor) {
	mActor = actor;
	setActorListener();
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
 * Constructor for Kryo
 */
protected TActorActivated() {
	// Does nothing
}

@Override
public void createBody() {
	if (mBody == null && !isHidden()) {
		List<FixtureDef> fixtures = mActor.getDef().getShape().getFixtureDefs();

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
public boolean hasBody() {
	return mBody != null;
}

/**
 * Creates visual representation of the trigger
 */
private void createVertices() {
	if (!isHidden()) {
		destroyVertices();

		List<Vector2> polygon = new ArrayList<Vector2>();
		List<Vector2> actorShape = mActor.getDef().getShape().getPolygonShape();
		if (actorShape != null && !actorShape.isEmpty()) {
			// Copy polygon from actor, so we don't free the actor's vectors when
			// freeing
			// this trigger
			for (Vector2 vertex : actorShape) {
				polygon.add(new Vector2(vertex));
			}
			List<Vector2> borderCorners = Geometry.createdBorderCorners(polygon, false, Config.Editor.Level.Trigger.ENEMY_WIDTH);

			mVertices = Geometry.createBorderVertices(polygon, borderCorners);
		}
	}
}

/**
 * Destroys the visual representation of the trigger
 */
private void destroyVertices() {
	if (mVertices != null) {
		mVertices = null;
	}
}

@Override
public RenderOrders getRenderOrder() {
	return RenderOrders.TRIGGER_ACTOR_ACTIVATE;
}

@Override
public void renderEditor(ShapeRendererEx shapeRenderer) {
	if (mVertices != null && mActor != null) {
		shapeRenderer.setColor(Config.Editor.Level.Trigger.COLOR);
		Vector2 offsetPosition = new Vector2();
		offsetPosition.set(mActor.getPosition()).add(mActor.getDef().getShape().getCenterOffset());
		shapeRenderer.triangles(mVertices, offsetPosition);

		if (isSelected()) {
			shapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.SELECTED_COLOR_UTILITY));
			shapeRenderer.triangles(mVertices, offsetPosition);
		}
	}
}

@Override
public boolean isTriggered() {
	return mActor.isActive();
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
public void removeBoundResource(IResource boundResource, List<Command> commands) {
	super.removeBoundResource(boundResource, commands);

	if (boundResource.equals(mActor)) {
		Command command = new Command() {
			private Actor oldActor = mActor;

			@Override
			public boolean execute() {
				mActor = null;
				return true;
			}

			@Override
			public boolean undo() {
				mActor = oldActor;
				return true;
			}
		};
		commands.add(command);
	}
}

@Override
public void prepareWrite() {
	mActor.removeChangeListener(this);
}

@Override
public void postRead() {
	super.postRead();

	setActorListener();
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
}
