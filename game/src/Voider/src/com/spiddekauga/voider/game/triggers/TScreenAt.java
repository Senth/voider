package com.spiddekauga.voider.game.triggers;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.game.triggers.TriggerAction.Reasons;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Vector2Pool;
/**
 * Triggers when the right side of the screen is at or beyond a specific position.
 * Equal to the level's current x-coordinate
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TScreenAt extends Trigger implements IResourceBody, IResourcePosition, Disposable {
	/**
	 * @param level checks this level for the x coordinate
	 * @param xCoord the x-coordinate we want the level to be at or beyond.
	 */
	public TScreenAt(Level level, float xCoord) {
		mLevel = level;
		mLevelId = level.getId();
		mPosition.x = xCoord;
		mPosition.y = 0;
	}

	@Override
	protected Reasons getReason() {
		return Reasons.SCREEN_AT;
	}

	@Override
	protected Object getCauseObject() {
		return null;
	}

	@Override
	protected boolean isTriggered() {
		return mLevel.getXCoord() >= mPosition.x;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mLevelId", mLevelId);
		json.writeValue("mPosition.x", mPosition.x);
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		mLevelId = json.readValue("mLevelId", UUID.class, jsonData);
		mPosition.x = json.readValue("mPosition.x", float.class, jsonData);
	}

	@Override
	public void getReferences(ArrayList<UUID> references) {
		super.getReferences(references);
		references.add(mLevelId);
	}

	@Override
	public boolean bindReference(IResource resource) {
		boolean success = super.bindReference(resource);

		if (resource instanceof Level) {
			mLevel = (Level) resource;
			success = true;
		}

		return success;
	}

	@Override
	public boolean addBoundResource(IResource boundResource) {
		boolean success = super.addBoundResource(boundResource);

		if (boundResource instanceof Level) {
			mLevel = (Level) boundResource;
			mLevelId = mLevel.getId();
			success = true;
		}

		return success;
	}

	@Override
	public boolean removeBoundResource(IResource boundResource) {
		boolean success = super.removeBoundResource(boundResource);

		if (boundResource.getId().equals(mLevelId)) {
			mLevel = null;
			mLevelId = null;
			success = true;
		}

		return success;
	}

	@Override
	public void createBody() {
		if (mBody == null) {
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.filter.categoryBits = ActorFilterCategories.NONE;
			fixtureDef.filter.groupIndex = ActorFilterCategories.NONE;

			EdgeShape edgeShape = new EdgeShape();
			float halfHeight = SceneSwitcher.getWorldHeight() * 0.5f;
			edgeShape.set(0, -halfHeight, 0, halfHeight);
			fixtureDef.shape = edgeShape;

			mBody = Actor.getWorld().createBody(new BodyDef());
			mBody.createFixture(fixtureDef);
			mBody.setTransform(mPosition, 0);
			mBody.setUserData(this);

			edgeShape.dispose();
		}
	}

	@Override
	public void destroyBody() {
		if (mBody != null) {
			mBody.getWorld().destroyBody(mBody);
			mBody = null;
		}
	}

	/**
	 * Because we only use the x-coordinate, y will always be set to 0 here.
	 */
	@Override
	public void setPosition(Vector2 position) {
		mPosition.set(position.x, 0);

		if (mBody != null) {
			mBody.setTransform(mPosition, 0);
		}
	}

	@Override
	public Vector2 getPosition() {
		return mPosition;
	}

	@Override
	public void dispose() {
		Vector2Pool.free(mPosition);
		destroyBody();
	}

	/**
	 * Constructor for JSON
	 */
	protected TScreenAt() {
		// Does nothing
	}

	/** Body of the trigger */
	private Body mBody = null;
	/** Level to check for the x-coordinate */
	private Level mLevel = null;
	/** Level id, used for binding the level */
	private UUID mLevelId = null;
	/** Temporary positon, stores x-coord for getting the position */
	private Vector2 mPosition = Vector2Pool.obtain();
}
