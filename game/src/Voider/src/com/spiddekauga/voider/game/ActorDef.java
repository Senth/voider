package com.spiddekauga.voider.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.Textures;

/**
 * Definition of the actor. This include common attribute for a common type of actor.
 * E.g. A specific enemy will have the same variables here. The only thing changed during
 * it's life is the variables in the Actor class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorDef extends Def implements Json.Serializable, Disposable {
	/**
	 * Constructor that sets all variables
	 * @param maxLife maximum life of the actor, also starting amount of life
	 * @param textureTypes all the texture types that are used for the actor
	 * @param name name of the actor
	 * @param fixtureDef physical representation of the object
	 */
	public ActorDef(
			float maxLife,
			Textures.Types[] textureTypes,
			String name,
			FixtureDef fixtureDef
			)
	{
		setName(name);
		mMaxLife = maxLife;
		mTextureTypes = textureTypes;
		mFixtureDef = fixtureDef;
		mBodyDef = new BodyDef();

		setFilterCollisionData();
	}

	/**
	 * Default constructor
	 */
	public ActorDef() {
	}

	/**
	 * Maximum life of the actor, usually starting amount of life
	 * @return maximum life
	 */
	public float getMaxLife() {
		return mMaxLife;
	}

	/**
	 * Checks how many textures this actor has
	 * @return number of textures
	 */
	public int getTextureCount() {
		return mTextureTypes == null ? 0 : mTextureTypes.length;
	}

	/**
	 * Gets the texture region (not the type) with the current id.
	 * @param index texture's index
	 * @return texture region if index was valid, null if index is out of bounds
	 */
	public TextureRegion getTextureRegion(int index) {
		if (index >= 0 && index < mTextureTypes.length) {
			return Textures.getTexture(mTextureTypes[index]);
		} else {
			return null;
		}
	}

	/**
	 * Returns the fixture definition. Includes shape, mass, etc.
	 * @return fixture definition.
	 */
	public final FixtureDef getFixtureDef() {
		return mFixtureDef;
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
		copy.active = mBodyDef.active;
		copy.allowSleep = mBodyDef.allowSleep;
		copy.angularDamping = mBodyDef.angularDamping;
		copy.awake = mBodyDef.awake;
		copy.bullet = mBodyDef.bullet;
		copy.fixedRotation = mBodyDef.fixedRotation;
		copy.gravityScale = mBodyDef.gravityScale;
		copy.linearDamping = mBodyDef.linearDamping;
		copy.type = mBodyDef.type;
		return copy;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Disposable#dispose()
	 */
	@Override
	public void dispose() {
		if (mFixtureDef != null && mFixtureDef.shape != null) {
			mFixtureDef.shape.dispose();
		}
	}

	/**
	 * @return the filter category this actor belongs to
	 */
	protected abstract short getFilterCategory();

	/**
	 * @return the mask bit used for determening who the actor
	 * should collide with
	 */
	protected abstract short getFilterCollidingCategories();

	/**
	 * Sets the filter information based on derived information
	 */
	private void setFilterCollisionData() {
		if (mFixtureDef != null) {
			mFixtureDef.filter.categoryBits = getFilterCategory();
			mFixtureDef.filter.maskBits = getFilterCollidingCategories();
		}
	}


	/** Defines the mass, shape, etc. */
	private FixtureDef mFixtureDef = null;
	/** Maximum life of the actor, usually starting amount of life */
	private float mMaxLife = 0;
	/** All textures for the actor */
	private Textures.Types[] mTextureTypes = null;
	/** The body definition of the actor */
	private BodyDef mBodyDef = null;

	/**
	 * @TODO weapon type
	 * @TODO move type (for enemies)
	 */

	/** For serialization */
	private static final long VERSION = 100;


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("VERSION", VERSION);

		json.writeObjectStart("Def");
		super.write(json);
		json.writeObjectEnd();

		// Write ActorDef's variables first
		json.writeValue("mMaxLife", mMaxLife);
		json.writeValue("mTextureTypes", mTextureTypes);
		json.writeValue("mBodyDef", mBodyDef);


		// Fixture
		if (mFixtureDef == null) {
			json.writeValue("mFixtureDef", mFixtureDef);
		} else {
			json.writeObjectStart("mFixtureDef");
			json.writeValue("density", mFixtureDef.density);
			json.writeValue("friction", mFixtureDef.friction);
			json.writeValue("isSensor", mFixtureDef.isSensor);
			json.writeValue("restitution", mFixtureDef.restitution);


			// Shape
			if (mFixtureDef.shape == null) {
				json.writeValue("shape", mFixtureDef.shape);
			} else {
				json.writeObjectStart("shape");
				json.writeValue("type", mFixtureDef.shape.getType());

				// Shape specific actions
				switch (mFixtureDef.shape.getType()) {
				case Circle:
					CircleShape circle = (CircleShape)mFixtureDef.shape;
					json.writeValue("position", circle.getPosition());
					json.writeValue("radius", circle.getRadius());
					break;

				case Polygon:
					PolygonShape polygon = (PolygonShape)mFixtureDef.shape;
					Vector2[] vertices = new Vector2[polygon.getVertexCount()];
					for (int i = 0; i < polygon.getVertexCount(); i++) {
						vertices[i] = Pools.obtain(Vector2.class);
						polygon.getVertex(i, vertices[i]);
					}
					json.writeValue("vertices", vertices);
					for (Vector2 vertex : vertices) {
						Pools.free(vertex);
					}
					break;

				case Edge:
					EdgeShape edge = (EdgeShape)mFixtureDef.shape;
					Vector2 tempVector = Pools.obtain(Vector2.class);
					edge.getVertex1(tempVector);
					json.writeValue("vertex1", tempVector);
					edge.getVertex2(tempVector);
					json.writeValue("vertex2", tempVector);
					Pools.free(tempVector);
					break;

				case Chain:
					assert("Chains not supported" == null);
					break;

				}
				json.writeObjectEnd();
			} // Shape

			json.writeObjectEnd();
		} // Fixture def
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		long version = json.readValue("VERSION", long.class, jsonData);

		/** @TODO do something when another version... */
		if (version != VERSION) {
			//...
		}

		// Superclass
		@SuppressWarnings("unchecked")
		OrderedMap<String, Object> superMap = json.readValue("Def", OrderedMap.class, jsonData);
		if (superMap != null) {
			super.read(json, superMap);
		}


		// Our variables
		mMaxLife = json.readValue("mMaxLife", float.class, jsonData);
		mTextureTypes = json.readValue("mTextureTypes", Textures.Types[].class, jsonData);
		mBodyDef = json.readValue("mBodyDef", BodyDef.class, jsonData);


		// Fixture definition
		OrderedMap<?,?> fixtureDefMap = json.readValue("mFixtureDef", OrderedMap.class, jsonData);
		if (fixtureDefMap != null) {
			mFixtureDef = new FixtureDef();
			mFixtureDef.density = json.readValue("density", float.class, fixtureDefMap);
			mFixtureDef.friction = json.readValue("friction", float.class, fixtureDefMap);
			mFixtureDef.restitution = json.readValue("restitution", float.class, fixtureDefMap);
			mFixtureDef.isSensor = json.readValue("isSensor",  boolean.class, fixtureDefMap);

			setFilterCollisionData();

			// Shape
			OrderedMap<?,?> shapeMap = json.readValue("shape", OrderedMap.class, fixtureDefMap);
			if (shapeMap != null) {
				Shape.Type shapeType = json.readValue("type", Shape.Type.class, shapeMap);
				switch (shapeType) {
				case Circle:
					float radius = json.readValue("radius", float.class, shapeMap);
					Vector2 position = json.readValue("position", Vector2.class, shapeMap);
					CircleShape circle = new CircleShape();
					mFixtureDef.shape = circle;
					circle.setPosition(position);
					circle.setRadius(radius);
					break;

				case Polygon:
					Vector2[] vertices = json.readValue("vertices", Vector2[].class, shapeMap);
					PolygonShape polygon = new PolygonShape();
					mFixtureDef.shape = polygon;
					polygon.set(vertices);
					break;

				case Edge:
					Vector2 vertex1 = json.readValue("vertex1", Vector2.class, shapeMap);
					Vector2 vertex2 = json.readValue("vertex2", Vector2.class, shapeMap);
					EdgeShape edge = new EdgeShape();
					mFixtureDef.shape = edge;
					edge.set(vertex1, vertex2);
					break;

				case Chain:
					assert("Chains not supported!" == null);
					break;
				}
			} else {
				mFixtureDef.shape = null;
			}
		} else {
			mFixtureDef = null;
		}
	}
}
