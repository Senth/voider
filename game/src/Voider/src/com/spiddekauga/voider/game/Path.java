package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRendererEx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Vector2Pool;


/**
 * A path that enemies and maybe something else can follow
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Path extends Resource implements Json.Serializable, Disposable, IResourceCorner, IResourceBody, IResourcePosition, IResourceEditorRender {
	/**
	 * Default constructor, sets the unique id of the path
	 */
	public Path() {
		mUniqueId = UUID.randomUUID();
	}

	@Override
	public void addCorner(Vector2 corner) throws PolygonComplexException, PolygonCornerTooCloseException {
		addCorner(corner, mCorners.size());
	}

	@Override
	public void addCorner(Vector2 corner, int index) throws PolygonComplexException, PolygonCornerTooCloseException {
		mCorners.add(index, corner.cpy());

		if (mWorld != null) {
			createFixture();
			resetBodyFixture();

			if (mSelected) {
				createBodyCorner(corner, index);
			}
		}

		if (index == 0) {
			updateEnemyPositions();
		}

		createVertices();
	}

	/**
	 * This actually moves the position
	 */
	@Override
	public void setPosition(Vector2 position) {
		Vector2 diff = getPosition();

		diff.sub(position);

		// Move all corners
		for (Vector2 corner : mCorners) {
			corner.sub(diff);
		}

		resetBodyCorners();
		createFixture();
		resetBodyFixture();

		updateEnemyPositions();

		createVertices();

		Vector2Pool.free(diff);
	}

	/**
	 * Calculates the center position of all corners. This is calculated everytime this
	 * method is called.
	 * @return center position of all corner. Don't forget to run Pools.free(position) this
	 * return value
	 */
	@Override
	public Vector2 getPosition() {
		Vector2 center = Vector2Pool.obtain();
		center.set(0, 0);

		for (Vector2 corner : mCorners) {
			center.add(corner);
		}

		if (!mCorners.isEmpty()) {
			center.div(mCorners.size());
		}

		return center;
	}

	@Override
	public Vector2 removeCorner(int index) {
		Vector2 removedCorner = null;

		if (index >= 0 && index < mCorners.size()) {
			removedCorner = mCorners.remove(index);
			destroyBodyCorners(index);
			createFixture();
			resetBodyFixture();

			if (index == 0) {
				updateEnemyPositions();
			}
		}

		createVertices();

		return removedCorner;
	}

	@Override
	public void moveCorner(int index, Vector2 newPos) throws PolygonComplexException, PolygonCornerTooCloseException {
		if (index >= 0 && index < mCorners.size()) {
			mCorners.get(index).set(newPos);
			resetBodyCorners();
			createFixture();
			resetBodyFixture();

			if (index == 0) {
				updateEnemyPositions();
			}
		}

		createVertices();
	}

	@Override
	public int getCornerCount() {
		return mCorners.size();
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

	@Override
	public Vector2 getCornerPosition(int index) {
		return mCorners.get(index);
	}

	@Override
	public ArrayList<Vector2> getCorners() {
		return mCorners;
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

	@Override
	public void createBody() {
		if (mWorld != null) {
			// Destroy old body
			if (mBody != null) {
				mBody.getWorld().destroyBody(mBody);
			}

			mBody = mWorld.createBody(new BodyDef());
			mBody.setUserData(this);
			if (mFixtureDef != null) {
				mBody.createFixture(mFixtureDef);
			}

			createVertices();
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
	 * Checks what index the specified position has.
	 * @param position the position of a corner
	 * @return corner index if a corner was found at position, -1 if none was found.
	 */
	public int getCornerIndex(Vector2 position) {
		for (int i = 0; i < mCorners.size(); ++i) {
			if (mCorners.get(i).equals(position)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public void write(Json json) {
		super.write(json);
		json.writeValue("mCorners", mCorners);
		json.writeValue("mPathType", mPathType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);
		mCorners = json.readValue("mCorners", ArrayList.class, jsonData);
		mPathType = json.readValue("mPathType", PathTypes.class, jsonData);
	}

	@Override
	public void dispose() {
		if (mFixtureDef != null && mFixtureDef.shape != null) {
			mFixtureDef.shape.dispose();
			mFixtureDef.shape = null;
		}
		if (mBody != null) {
			destroyBody();
			destroyBodyCorners();
		}
	}

	/**
	 * Creates all the body corners
	 */
	public void createBodyCorners() {
		if (!mBodyCorners.isEmpty()) {
			Gdx.app.error("Path", "Shall only create body corners if empty!");
		}

		for (Vector2 corner : mCorners) {
			createBodyCorner(corner);
		}
	}

	/**
	 * Destroys all body corners
	 */
	public void destroyBodyCorners() {
		while (!mBodyCorners.isEmpty()) {
			destroyBodyCorners(0);
		}
	}

	/**
	 * Set the path as selected, this will render the path differently
	 * @param selected set to true if the path shall be set as selected
	 */
	public void setSelected(boolean selected) {
		mSelected = selected;
	}

	/**
	 * @return true if this path is selected
	 */
	public boolean isSelected() {
		return mSelected;
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		if (mVertices != null) {
			shapeRenderer.setColor(Config.Editor.Level.Path.START_COLOR);
			shapeRenderer.triangles(mVertices);
		}


		if (mSelected) {
			shapeRenderer.setColor(Config.Editor.SELECTED_COLOR);
			shapeRenderer.triangles(mVertices);
		}
	}

	/**
	 * Adds an enemy to the path
	 * @param enemy enemy that uses this path
	 */
	public void addEnemy(EnemyActor enemy) {
		mEnemies.add(enemy);
	}

	/**
	 * Removes an enemy from the path
	 * @param enemy enemy that uses this path
	 */
	public void removeEnemy(EnemyActor enemy) {
		mEnemies.remove(enemy);
	}

	/**
	 * @return all enemies that uses this path
	 */
	public ArrayList<EnemyActor> getEnemies() {
		return mEnemies;
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
		if (mCorners.size() >= 2) {
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
			Vector2[] tempArray = new Vector2[mCorners.size()];
			chainShape.createChain(mCorners.toArray(tempArray));
			mFixtureDef.shape = chainShape;
		} else if (mCorners.size() >= 1) {
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

			ChainShape chainShape = new ChainShape();
			Vector2[] tempArray = new Vector2[2];

			for (int i = 0; i < tempArray.length; ++i) {
				tempArray[i] = Vector2Pool.obtain();
			}

			tempArray[0].set(mCorners.get(0));
			tempArray[1].set(mCorners.get(0)).sub(1,0);

			chainShape.createChain(tempArray);
			mFixtureDef.shape = chainShape;

			for (Vector2 tempPosition : tempArray) {
				Vector2Pool.free(tempPosition);
			}
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
	 * Creates a new body corner at the back
	 * @param position corner position to create the body in
	 */
	private void createBodyCorner(Vector2 position) {
		createBodyCorner(position, mBodyCorners.size());
	}

	/**
	 * Creates a new body corner in the specified position
	 * @param position corner position to create the body in
	 * @param index the index to create the corner in
	 */
	private void createBodyCorner(Vector2 position, int index) {
		Body cornerBody = mWorld.createBody(new BodyDef());
		cornerBody.setTransform(position, 0);
		cornerBody.createFixture(Config.Editor.getPickingFixture());
		HitWrapper hitWrapper = new HitWrapper(this);
		cornerBody.setUserData(hitWrapper);

		mBodyCorners.add(index, cornerBody);
	}

	/**
	 * Destroys the specified body corner and removes it from the list
	 * @param index the body corner to destroy
	 */
	private void destroyBodyCorners(int index) {
		if (index >= 0 && index < mBodyCorners.size()) {
			Body removedBody = mBodyCorners.remove(index);

			removedBody.getWorld().destroyBody(removedBody);
		}
	}

	/**
	 * Resets the position of all body corners
	 */
	private void resetBodyCorners() {
		for (int i = 0; i < mBodyCorners.size(); ++i) {
			Body body = mBodyCorners.get(i);
			body.setTransform(mCorners.get(i), 0);
		}
	}

	/**
	 * Updates the enemies position to the first body corner's position
	 */
	private void updateEnemyPositions() {
		if (mCorners.size() > 0) {
			Vector2 firstCorner = mCorners.get(0);
			for (EnemyActor enemy : mEnemies) {
				enemy.setPosition(firstCorner);
			}
		}
	}

	/**
	 * Recreates the path vertices for drawing
	 */
	private void createVertices() {
		// Dispose of old path
		if (mVertices != null) {
			ArrayList<Vector2> removedVertices = new ArrayList<Vector2>();
			for (Vector2 vertex : mVertices) {
				if (!removedVertices.contains(vertex)) {
					removedVertices.add(vertex);
					Vector2Pool.free(vertex);
				}
			}
		}

		mVertices = Geometry.createLinePolygon(mCorners, Config.Editor.Level.Path.WIDTH);
	}

	/** Path vertices for drawing in editor */
	private ArrayList<Vector2> mVertices = null;
	/** If this path is selected */
	private boolean mSelected = false;
	/** All path nodes */
	private ArrayList<Vector2> mCorners = new ArrayList<Vector2>();
	/** Corner bodies, for picking */
	private ArrayList<Body> mBodyCorners = new ArrayList<Body>();
	/** What type of path type the enemy uses, only applicable if movement type
	 * is set to path */
	private PathTypes mPathType = PathTypes.ONCE;
	/** World the path is bound to */
	private World mWorld = null;
	/** Body of the path */
	private Body mBody = null;
	/** Fixtures for the path */
	private FixtureDef mFixtureDef = null;
	/** Enemies bound to this path */
	private ArrayList<EnemyActor> mEnemies = new ArrayList<EnemyActor>();
}
