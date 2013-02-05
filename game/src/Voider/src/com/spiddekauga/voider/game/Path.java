package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.resources.IResource;


/**
 * A path that enemies and maybe something else can follow
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Path implements IResource, Json.Serializable, Disposable {

	/**
	 * Adds a path node to the back
	 * @param nodePos the position of the node
	 */
	public void addNodeToBack(Vector2 nodePos) {
		mNodes.add(Pools.obtain(Vector2.class).set(nodePos));

		if (mWorld != null) {
			createFixture();
			resetBodyFixture();
		}
	}

	/**
	 * Removes the node at the back if one exists
	 */
	public void removeNodeFromBack() {
		if (!mNodes.isEmpty()) {
			mNodes.remove(mNodes.size() - 1);
		}

		if (mWorld != null) {
			createFixture();
			resetBodyFixture();
		}
	}

	/**
	 * Sets the world for the path, so that a body can be created. This function
	 * also sets activates all body etc, so that it gets drawn.
	 * @param world the world for the path, if null it will disable drawing
	 */
	public void setWorld(World world) {
		mWorld = world;

		if (mWorld != null) {
			createFixture();
			createBody();
		}
		// NULL: Dispose everything
		else {
			if (mBody != null) {
				mBody.getWorld().destroyBody(mBody);
				mBody = null;
			}

			if (mFixtureDef != null && mFixtureDef.shape != null) {
				mFixtureDef.shape.dispose();
				mFixtureDef = null;
			}
		}
	}

	/**
	 * @return number of nodes in the path
	 */
	public int getNodeCount() {
		return mNodes.size();
	}

	/**
	 * @param index index of the node we want to get
	 * @return node at the specified index
	 */
	public Vector2 getNodeAt(int index) {
		return mNodes.get(index);
	}

	/**
	 * @return all nodes of the path. These node are used by reference, thus
	 * the returned array will be invalid after a #clearNodes() call.
	 * @see #getNodesCopy() if you want to save the nodes after a #clearNodes() call
	 */
	public ArrayList<Vector2> getNodes() {
		return mNodes;
	}

	/**
	 * @return a copy of all nodes of the path. This is necessary if you want to save
	 * the array after a #clearNodes() call. The Vectors are allocated through Pools, thus
	 * they should be freed to the same pool when discarding them.
	 * @see #getNodes() if you just want a temporary list
	 */
	public ArrayList<Vector2> getNodesCopy() {
		ArrayList<Vector2> copyList = new ArrayList<Vector2>();
		for (Vector2 node : mNodes) {
			copyList.add(Pools.obtain(Vector2.class).set(node));
		}
		return copyList;
	}

	/**
	 * Removes and frees all nodes from the path. Any returned array from #getNodes()
	 * gets invalidated.
	 */
	public void clearNodes() {
		for (Vector2 node : mNodes) {
			Pools.free(node);
		}
		mNodes.clear();
	}

	/**
	 * Sets how the enemy shall follow the path. Only applicable if
	 * the enemy movement is set to follow a path.
	 * @param pathType how shall the enemy follow the path
	 */
	public void setPathType(PathTypes pathType) {
		mPathType = pathType;
	}

	/**
	 * @return how the enemy is following a path. Only applicable if the
	 * enemy movement is set to follow a path.
	 */
	public PathTypes getPathType() {
		return mPathType;
	}

	/**
	 * Renders the path
	 * @param spriteBatch the batch to use for rendering
	 */
	public void render(SpriteBatch spriteBatch) {

	}

	@Override
	public UUID getId() {
		return mUniqueId;
	}

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("mNodes", mNodes);
		json.writeValue("mPathType", mPathType);
		json.writeValue("mUniqueId", mUniqueId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		mNodes = json.readValue("mNodes", ArrayList.class, jsonData);
		mPathType = json.readValue("mPathType", PathTypes.class, jsonData);
		mUniqueId = json.readValue("mUniqueId", UUID.class, jsonData);
	}

	@Override
	public void dispose() {
		if (mFixtureDef != null && mFixtureDef.shape != null) {
			mFixtureDef.shape.dispose();
			mFixtureDef.shape = null;
		}
	}

	/**
	 * Different path types, i.e. how the enemy shall follow a path
	 * (if it is set to follow a path)
	 */
	public enum PathTypes {
		/** Goes back and forth on the path */
		BACK_AND_FORTH,
		/** Once the enemy comes to the last node in the path it walks
		 * towards the first node and goes around in a loop */
		LOOP,
		/** Follows the path once and then continues in its last direction
		 * after it has come to the last node */
		ONCE
	}

	/**
	 * Creates the fixtures for the path
	 */
	private void createFixture() {
		if (mNodes.size() >= 2) {
			if (mFixtureDef == null) {
				mFixtureDef = new FixtureDef();
				mFixtureDef.filter.categoryBits = ActorFilterCategories.NONE;
				mFixtureDef.filter.groupIndex = ActorFilterCategories.NONE;
			} else {
				if (mFixtureDef.shape != null) {
					mFixtureDef.shape.dispose();
					mFixtureDef.shape = null;
				}
			}

			// Create shape
			ChainShape chainShape = new ChainShape();
			Vector2[] tempArray = new Vector2[mNodes.size()];
			chainShape.createChain(mNodes.toArray(tempArray));
			mFixtureDef.shape = chainShape;
		}
	}

	/**
	 * Removes existing body fixture and adds the current fixture
	 */
	@SuppressWarnings("unchecked")
	private void resetBodyFixture() {
		if (mFixtureDef != null && mBody != null) {
			ArrayList<Fixture> fixtures = (ArrayList<Fixture>) mBody.getFixtureList().clone();
			for (Fixture fixture : fixtures) {
				mBody.destroyFixture(fixture);
			}

			mBody.createFixture(mFixtureDef);
		}
	}

	/**
	 * Creates the body for the world, so the player can click on it
	 */
	private void createBody() {
		// Destroy old body
		if (mBody != null) {
			mBody.getWorld().destroyBody(mBody);
		}

		mBody = mWorld.createBody(new BodyDef());
		if (mFixtureDef != null) {
			mBody.createFixture(mFixtureDef);
		}
	}

	/** All path nodes */
	private ArrayList<Vector2> mNodes = new ArrayList<Vector2>();
	/** What type of path type the enemy uses, only applicable if movement type
	 * is set to path */
	private PathTypes mPathType = PathTypes.ONCE;
	/** Unique id for the path */
	private UUID mUniqueId = UUID.randomUUID();
	/** World the path is bound to */
	private World mWorld = null;
	/** Body of the path */
	private Body mBody = null;
	/** Fixtures for the path */
	private FixtureDef mFixtureDef = null;
}
