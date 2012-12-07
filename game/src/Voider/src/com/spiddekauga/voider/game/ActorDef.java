package com.spiddekauga.voider.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.voider.game.actors.Types;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.Textures;

/**
 * Definition of the actor. This include common attribute for a common type of actor.
 * E.g. A specific enemy will have the same variables here. The only thing changed during
 * it's life is the variables in the Actor class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorDef extends Def implements Json.Serializable {
	/**
	 * Constructor that sets all variables
	 * @param maxLife maximum life of the actor, also starting amount of life
	 * @param type the actor type
	 * @param textureTypes all the texture types that are used for the actor
	 * @param name name of the actor
	 * @param fixtureDef physical representation of the object
	 */
	public ActorDef(
			float maxLife,
			com.spiddekauga.voider.game.actors.Types type,
			Textures.Types[] textureTypes,
			String name,
			FixtureDef fixtureDef
			)
	{
		mMaxLife = maxLife;
		mType = type;
		mTextureTypes = textureTypes;
		mName = name;
		mFixtureDef = fixtureDef;
	}

	/**
	 * Default constructor
	 */
	public ActorDef() {
		mName = "unknown";
	}

	/**
	 * Maximum life of the actor, usually starting amount of life
	 * @return maximum life
	 */
	public float getMaxLife() {
		return mMaxLife;
	}

	/**
	 * What kind of actor this is, e.g. enemy, player, bullet
	 * @return actor type
	 */
	public com.spiddekauga.voider.game.actors.Types getType() {
		return mType;
	}

	/**
	 * The name of the actor. This is not the same as actor type. I.e.
	 * enemies have different names
	 * @return common name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Checks how many textures this actor has
	 * @return number of textures
	 */
	public int getTextureCount() {
		return mTextureTypes.length;
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
	public FixtureDef getFixtureDef() {
		return mFixtureDef;
	}

	/**
	 * Defines the mass, shape, etc.
	 */
	private FixtureDef mFixtureDef = null;
	/**
	 * Maximum life of the actor, usually starting amount of life
	 */
	private float mMaxLife = 0;
	/**
	 * The actor type this definition belongs to
	 */
	private com.spiddekauga.voider.game.actors.Types mType = Types.INVALID;
	/**
	 * All textures for the actor
	 */
	private Textures.Types[] mTextureTypes = null;
	/**
	 * Name of the actor
	 */
	private String mName = null;
	/**
	 * @todo weapon type
	 * @todo move type (for enemies)
	 */

	/**
	 * For serialization
	 */
	private static final long VERSION = 100;

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("VERSIONO", VERSION);

		json.writeObjectStart("Def");
		super.write(json);
		json.writeObjectEnd();

		// Write ActorDef's variables first
		json.writeValue("mMaxLife", mMaxLife);
		json.writeValue("mType", mType);
		json.writeValue("mName", mName);
		json.writeValue("mTextureTypes", mTextureTypes);


		// Fixture
		json.writeObjectStart("FixtureDef");
		json.writeValue("density", mFixtureDef.density);
		json.writeValue("filter", mFixtureDef.filter);
		json.writeValue("friction", mFixtureDef.friction);
		json.writeValue("isSensor", mFixtureDef.isSensor);
		json.writeValue("restitution", mFixtureDef.restitution);


		// Shape
		json.writeObjectStart("Shape");
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
				polygon.getVertex(i, vertices[i]);
			}
			json.writeValue("vertices", vertices);
			break;

		case Edge:
			EdgeShape edge = (EdgeShape)mFixtureDef.shape;
			Vector2 tempVector = new Vector2();
			edge.getVertex1(tempVector);
			json.writeValue("vertex1", tempVector);
			edge.getVertex2(tempVector);
			json.writeValue("vertex2", tempVector);
			break;

		case Chain:
			assert("Chains not supported" == null);
			break;

		}

		json.writeObjectEnd();
		json.writeObjectEnd();
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		// Actor
		int version = json.readValue("object_version", int.class, jsonData);

		/** @TODO do something when another version... */
		if (version != VERSION) {
			//...
		}

		@SuppressWarnings("unchecked")
		OrderedMap<String, Object> superMap = json.readValue("Def", OrderedMap.class, jsonData);
		super.read(json, superMap);

		mMaxLife = json.readValue("mMaxLife", float.class, jsonData);
		mType = json.readValue("mType", Types.class, jsonData);
		mName = json.readValue("mName", String.class, jsonData);
		mTextureTypes = json.readValue("mTextureTypes", Textures.Types[].class, jsonData);


		// Fixture definition
		OrderedMap<?,?> fixtureDefMap = json.readValue("FixtureDef", OrderedMap.class, jsonData);
		mFixtureDef = new FixtureDef();
		mFixtureDef.density = json.readValue("density", float.class, fixtureDefMap);
		mFixtureDef.friction = json.readValue("friction", float.class, fixtureDefMap);
		mFixtureDef.restitution = json.readValue("restitution", float.class, fixtureDefMap);
		mFixtureDef.isSensor = json.readValue("isSensor",  boolean.class, fixtureDefMap);


		// Filter
		Filter tempFilter = json.readValue("filter", Filter.class, fixtureDefMap);
		mFixtureDef.filter.categoryBits = tempFilter.categoryBits;
		mFixtureDef.filter.groupIndex = tempFilter.groupIndex;
		mFixtureDef.filter.maskBits = tempFilter.maskBits;


		// Shape
		OrderedMap<?,?> shapeMap = json.readValue("Shape", OrderedMap.class, fixtureDefMap);
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
	}
}
