package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * The ship the player controls
 */
public class PlayerActor extends com.spiddekauga.voider.game.actors.Actor {
@Tag(156)
private float mInvulnerableTimeLeft = 0;
private boolean mMoving = false;
private Vector2 mMoveOffset = new Vector2();
private Vector2 mMoveTo = new Vector2();
private MouseJointDef mMouseJointDef = null;
private MouseJoint mMouseJoint = null;

/**
 * Creates a default player
 */
public PlayerActor() {
	this(new PlayerActorDef());
}

/**
 * Creates a player from the specified actor definition
 * @param playerActorDef player actor definition to create the player from
 */
public PlayerActor(PlayerActorDef playerActorDef) {
	super(playerActorDef);
	setDef(playerActorDef);
}

/**
 * Adds a collectible to the player
 * @param collectible the collectible to add to the player
 */
public void addCollectible(Collectibles collectible) {
	switch (collectible) {
	case HEALTH_25:
		increaseHealth(25);
		break;

	case HEALTH_50:
		increaseHealth(50);
		break;
	}
}

@Override
public void update(float deltaTime) {
	super.update(deltaTime);

	if (mInvulnerableTimeLeft > 0) {
		mInvulnerableTimeLeft -= deltaTime;

		if (mInvulnerableTimeLeft <= 0) {
			makeVulnerable();
		}
	}
}

/**
 * Make the player ship vulnerable
 */
private void makeVulnerable() {
	mInvulnerableTimeLeft = 0;

	reloadFixtures();
}

@Override
public void addCollidingActor(Actor actor) {
	if (getCollidingActors().isEmpty()) {
		mEventDispatcher.fire(new GameEvent(EventTypes.GAME_PLAYER_COLLISION_BEGIN));
	}

	super.addCollidingActor(actor);
}

@Override
public void removeCollidingActor(Actor actor) {
	super.removeCollidingActor(actor);

	if (getCollidingActors().isEmpty()) {
		mEventDispatcher.fire(new GameEvent(EventTypes.GAME_PLAYER_COLLISION_END));
	}
}

@Override
public void resetHealth() {
	super.resetHealth();
	makeInvulnerable();
}

/**
 * @return player filter category
 */
@Override
protected short getFilterCategory() {
	if (isInvulnerable()) {
		return ActorFilterCategories.PLAYER;
	} else {
		return ActorFilterCategories.PLAYER;
	}
}

/**
 * @return true if the player is invulnerable
 */
public boolean isInvulnerable() {
	return mInvulnerableTimeLeft > 0;
}

/**
 * Can collide with everything except player and player bullets
 * @return colliding categories
 */
@Override
protected short getFilterCollidingCategories() {
	if (isInvulnerable()) {
		return ActorFilterCategories.SCREEN_BORDER;
	} else {
		return (short) (ActorFilterCategories.ENEMY | ActorFilterCategories.PICKUP | ActorFilterCategories.STATIC_TERRAIN | ActorFilterCategories.SCREEN_BORDER);
	}
}

/**
 * Make the player invulnerable
 */
private void makeInvulnerable() {
	mInvulnerableTimeLeft = ConfigIni.getInstance().game.getInvulnerableTimeOnShipLost();

	reloadFixtures();
}

@Override
public RenderOrders getRenderOrder() {
	return RenderOrders.PLAYER;
}

/**
 * Start moving the ship
 * @param cursorPos cursor position in the world
 */
public void startMoving(Vector2 cursorPos) {
	mMoving = true;

	if (mMouseJointDef == null) {
		Body mouseBody = mWorld.createBody(new BodyDef());
		mMouseJointDef = getDef(PlayerActorDef.class).createMouseJointDef(mouseBody, getBody());
	}

	mMoveOffset.set(cursorPos).sub(getPosition());
	mMouseJointDef.target.set(getPosition());
	mMouseJoint = (MouseJoint) mWorld.createJoint(mMouseJointDef);
	getBody().setAwake(true);
}

/**
 * Move the ship
 * @param cursorPos cursor position in the world
 */
public void move(Vector2 cursorPos) {
	if (mMoving) {
		mMoveTo.set(cursorPos).sub(mMoveOffset);
		mMouseJoint.setTarget(mMoveTo);
	}
}

/**
 * Stop moving the ship
 */
public void stopMoving() {
	mMoving = false;
	mWorld.destroyJoint(mMouseJoint);
	mMouseJoint = null;
}

/**
 * @return true if we're currently moving the player
 */
public boolean isMoving() {
	return mMoving;
}
}
