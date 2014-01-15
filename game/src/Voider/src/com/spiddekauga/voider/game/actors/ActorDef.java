package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResourceTexture;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Definition of the actor. This include common attribute for a common type of actor.
 * E.g. A specific enemy will have the same variables here. The only thing changed during
 * it's life is the variables in the Actor class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorDef extends Def implements Disposable, IResourceTexture {
	/**
	 * Sets the visual variable to the specified type
	 * @param actorType the actor type to which set the default values of
	 * the visual variables
	 */
	protected ActorDef(ActorTypes actorType) {
		mVisualVars = new VisualVars(actorType);

		/** @todo remove default color */
		switch (actorType) {
		case BULLET:
			mVisualVars.setColor(new Color(0.8f, 0.5f, 0, 1));
			break;

		case ENEMY:
			mVisualVars.setColor(new Color(1, 0, 0, 1));
			break;

		case PICKUP:
			mVisualVars.setColor(new Color(1, 1, 0, 1));
			break;

		case PLAYER:
			mVisualVars.setColor(new Color(0.25f, 1, 0.25f, 1));
			break;

		case STATIC_TERRAIN:
			mVisualVars.setColor(new Color(0, 0.5f, 0.1f, 1));
			break;

		default:
			mVisualVars.setColor(new Color(1, 1, 1, 1));
			break;
		}
	}

	@Override
	public void set(Resource resource) {
		super.set(resource);

		ActorDef def = (ActorDef) resource;
		mBodyChangeTime = GameTime.getTotalGlobalTimeElapsed();
		mBodyDef = def.mBodyDef;
		mCollisionDamage = def.mCollisionDamage;
		mDestroyOnCollide = def.mDestroyOnCollide;
		mMaxLife = def.mMaxLife;
		mPngBytes = def.mPngBytes;
		mTextureDrawable = def.mTextureDrawable;
		mVisualVars = def.mVisualVars;
	}

	/**
	 * Sets the starting angle of the actor
	 * @param angle the starting angle, in radians
	 */
	public void setStartAngleRad(float angle) {
		getBodyDef().angle = angle;
		mBodyChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Sets the starting angle of the actor
	 * @param angle the starting angle, in degrees
	 */
	public void setStartAngleDeg(float angle) {
		setStartAngleRad(angle * MathUtils.degreesToRadians);
	}

	/**
	 * @return starting angle of the actor (in radians)
	 */
	public float getStartAngleRad() {
		return getBodyDef().angle;
	}

	/**
	 * @return starting angle of the actor (in degrees)
	 */
	public float getStartAngleDeg() {
		return getStartAngleRad() * MathUtils.radiansToDegrees;
	}

	/**
	 * Calculates the height of the actor definition. Only works if the actor
	 * has created vertices. This takes into account the starting angle of the
	 * actor.
	 * @return actual height of the actor. 0 if no vertices has been created.
	 */
	public float getHeight() {
		if (!getVisualVars().isPolygonShapeValid()) {
			return 0;
		}

		ArrayList<Vector2> vertices = getVisualVars().getPolygonShape();

		float rotation = getStartAngleDeg();

		float highest = Float.MIN_VALUE;
		float lowest = Float.MAX_VALUE;

		// Rotate vertex and check if it's the highest lowest y-value
		Vector2 tempRotatedVertex = Pools.vector2.obtain();
		for (Vector2 vertex : vertices) {
			tempRotatedVertex.set(vertex);

			// Rotate vertex
			tempRotatedVertex.rotate(rotation);

			// Check highest and lowest
			if (tempRotatedVertex.y > highest) {
				highest = tempRotatedVertex.y;
			}
			if (tempRotatedVertex.y < lowest) {
				lowest = tempRotatedVertex.y;
			}
		}

		return highest - lowest;
	}

	/**
	 * Calculates the width of the actor definition. Only works if the actor
	 * has created vertices. This takes into account the starting angle of the
	 * actor.
	 * @return actual width of the actor. 0 if no vertices has been created.
	 */
	public float getWidth() {
		if (!getVisualVars().isPolygonShapeValid()) {
			return 0;
		}

		ArrayList<Vector2> vertices = getVisualVars().getPolygonShape();

		float rotation = getStartAngleDeg();

		float highest = Float.MIN_VALUE;
		float lowest = Float.MAX_VALUE;

		// Rotate vertex and check if it's the highest lowest y-value
		Vector2 tempRotatedVertex = Pools.vector2.obtain();
		for (Vector2 vertex : vertices) {
			tempRotatedVertex.set(vertex);

			// Rotate vertex
			tempRotatedVertex.rotate(rotation);

			// Check highest and lowest
			if (tempRotatedVertex.x > highest) {
				highest = tempRotatedVertex.x;
			}
			if (tempRotatedVertex.x < lowest) {
				lowest = tempRotatedVertex.x;
			}
		}

		return highest - lowest;
	}

	/**
	 * Sets the collision damage of the actor
	 * If the actor is set to be destroyed on collision ({@link #setDestroyOnCollide(boolean)})
	 * it will decrease the full collisionDamage from the other actor and not per second.
	 * @param collisionDamage damage (per second) this actor will make to another when
	 * colliding actor.
	 * @return this for chaining commands
	 */
	public ActorDef setCollisionDamage(float collisionDamage) {
		mCollisionDamage = collisionDamage;
		return this;
	}

	/**
	 * @return the collision damage (per second) this actor will make to another
	 * colliding actor.
	 */
	public float getCollisionDamage() {
		return mCollisionDamage;
	}

	/**
	 * Sets the maximum life of the actor. I.e. starting amount of
	 * life.
	 * @param maxLife the maximum/starting amount of life.
	 * @return this for chaining commands
	 */
	public ActorDef setHealthMax(float maxLife) {
		mMaxLife = maxLife;
		return this;
	}

	/**
	 * @return Maximum life of the actor. I.e. starting amount of life
	 */
	public float getHealthMax() {
		return mMaxLife;
	}

	/**
	 * @return collectible of the actor def. Only works for PickupActorDef other
	 * actors defs returns null.
	 */
	public Collectibles getCollectible() {
		return null;
	}

	/**
	 * @return body definition of the actor
	 */
	public final BodyDef getBodyDef() {
		return mBodyDef;
	}

	/**
	 * @return a copy of the body definition
	 */
	public BodyDef getBodyDefCopy() {
		BodyDef copy = new BodyDef();
		copy.angle = mBodyDef.angle;
		copy.active = mBodyDef.active;
		copy.allowSleep = mBodyDef.allowSleep;
		copy.angularDamping = mBodyDef.angularDamping;
		copy.angularVelocity = mBodyDef.angularVelocity;
		copy.awake = mBodyDef.awake;
		copy.bullet = mBodyDef.bullet;
		copy.fixedRotation = mBodyDef.fixedRotation;
		copy.gravityScale = mBodyDef.gravityScale;
		copy.linearDamping = mBodyDef.linearDamping;
		copy.linearVelocity.set(mBodyDef.linearVelocity);
		copy.position.set(mBodyDef.position);
		copy.type = mBodyDef.type;
		return copy;
	}

	@Override
	public void dispose() {
		if (mVisualVars != null) {
			mVisualVars.dispose();
			mVisualVars = null;
		}

		disposeTexture();
	}

	/**
	 * Disposes the texture
	 */
	protected void disposeTexture() {
		if (mTextureDrawable != null) {
			mTextureDrawable.getRegion().getTexture().dispose();
			mTextureDrawable = null;
		}
	}


	/**
	 * Sets whether this actor shall be destroyed on collision.
	 * If this actor has any collision damage set to it, it will decrease the
	 * other actors health with the whole amount instead of per second if this
	 * is set to true.
	 * @param destroyOnCollision set to true to destroy the actor on collision
	 */
	public void setDestroyOnCollide(boolean destroyOnCollision) {
		mDestroyOnCollide = destroyOnCollision;
	}

	/**
	 * @return true if this actor shall be destroyed on collision
	 */
	public boolean isDestroyedOnCollide() {
		return mDestroyOnCollide;
	}

	/**
	 * @return when this definition was changed that affects the body.
	 */
	float getBodyChangeTime() {
		return mBodyChangeTime;
	}

	/**
	 * @return visual variables/parameters of the actor
	 */
	public VisualVars getVisualVars() {
		return mVisualVars;
	}

	/**
	 * Sets the rotation speed of the actor. This might not work for
	 * some actors that rotate the actor on their own...
	 * @param rotationSpeed new rotation speed of the actor. In radians.
	 */
	public void setRotationSpeedRad(float rotationSpeed) {
		getBodyDef().angularVelocity = rotationSpeed;
		mBodyChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Sets the rotation speed of the actor. This might not work for
	 * some actors that rotate the actor on their own...
	 * @param rotationSpeed new rotation speed of the actor. In degrees
	 */
	public void setRotationSpeedDeg(float rotationSpeed) {
		setRotationSpeedRad(rotationSpeed * MathUtils.degreesToRadians);
	}

	/**
	 * @return rotation speed of the actor, in radians
	 */
	public float getRotationSpeedRad() {
		return getBodyDef().angularVelocity;
	}

	/**
	 * @return rotation speed of the actor, in degrees
	 */
	public float getRotationSpeedDeg() {
		return getRotationSpeedRad() * MathUtils.radiansToDegrees;
	}

	/**
	 * Sets the PNG image for the actor definition. This will also create a
	 * texture for this actor.
	 * @param pngBytes bytes for the png image
	 */
	public void setPngImage(byte[] pngBytes) {
		mPngBytes = pngBytes;

		disposeTexture();
		createTexture();
	}

	/**
	 * Create the texture from the PNG file
	 */
	private void createTexture() {
		if (mPngBytes != null) {
			Pixmap pixmap = new Pixmap(mPngBytes, 0, mPngBytes.length);
			TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
			mTextureDrawable = new TextureRegionDrawable(textureRegion);
		}
	}

	@Override
	public TextureRegionDrawable getTextureRegionDrawable() {
		if (mPngBytes != null && mTextureDrawable == null) {
			createTexture();
		}

		return mTextureDrawable;
	}

	/** When the body was changed last time */
	protected float mBodyChangeTime = 0;
	/** The possible texture of the image, used if mPngBytes are set.
	 * DON'T SAVE THIS as it is automatically generated when this actor def is loaded */
	private TextureRegionDrawable mTextureDrawable = null;

	/** Maximum life of the actor, usually starting amount of life */
	@Tag(44) private float mMaxLife = 0;
	/** The body definition of the actor */
	@Tag(45) private BodyDef mBodyDef = new BodyDef();
	/** The PNG bytes for the images, null if not used */
	@Tag(108) private byte[] mPngBytes = null;

	/** Collision damage (per second) */
	@Tag(46) private float mCollisionDamage = 0;
	/** If this actor shall be destroy on collision */
	@Tag(47) private boolean mDestroyOnCollide = false;
	/** Visual variables */
	@Tag(48) protected VisualVars mVisualVars = null;

	// DON'T FORGET TO ADD TO JUNIT TEST! ActorDefTest

}
