package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.kryo.KryoPostRead;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.resources.IResourceSelectable;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Geometry;


/**
 * A path that enemies and maybe something else can follow
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Path extends Resource implements Disposable, IResourceCorner, IResourceBody, IResourcePosition, IResourceEditorRender,
		IResourceSelectable, KryoPostRead {
	/**
	 * Default constructor, sets the unique id of the path
	 */
	public Path() {
		mUniqueId = UUID.randomUUID();
	}

	@Override
	public RenderOrders getRenderOrder() {
		return RenderOrders.ENEMY_PATH;
	}

	/**
	 * @return rightest corner from the path
	 */
	public Vector2 getRightestCorner() {
		return mRightestCorner;
	}

	@Override
	public void addCorners(java.util.List<Vector2> corners) {
		for (Vector2 corner : corners) {
			addCorner(corner);
		}
	}

	@Override
	public void addCorners(Vector2[] corners) {
		for (Vector2 corner : corners) {
			addCorner(corner);
		}
	}

	@Override
	public void addCorner(Vector2 corner) {
		addCorner(corner, mCorners.size());
	}

	@Override
	public void addCorner(Vector2 corner, int index) {
		mCorners.add(index, new Vector2(corner));

		if (mWorld != null) {
			updateVerticesBodyFixtures();

			if (mSelected) {
				createBodyCorner(corner, index);
			}
		}

		if (index == 0) {
			updateEnemyPositions();
		}

		calculateRightestCorner();
	}

	/**
	 * Updates the path's vertices, body/fixtures
	 */
	private void updateVerticesBodyFixtures() {
		if (mWorld != null) {
			createVertices();
			destroyBodyFixture();
			createFixture();
		}
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

		if (mWorld != null) {
			resetBodyCorners();
			updateVerticesBodyFixtures();
		}

		updateEnemyPositions();

		calculateRightestCorner();
	}

	/**
	 * Calculates the center position of all corners. This is calculated everytime this
	 * method is called.
	 * @return center position of all corner. Don't forget to run Pools.free(position)
	 *         this return value
	 */
	@Override
	public Vector2 getPosition() {
		Vector2 center = new Vector2();

		for (Vector2 corner : mCorners) {
			center.add(corner);
		}

		if (!mCorners.isEmpty()) {
			center.scl(1f / mCorners.size());
		}

		return center;
	}

	@Override
	public float getBoundingRadius() {
		// Calculate the bounding radius
		float maxLengthSq = Float.MIN_VALUE;
		Vector2 diffVector = new Vector2();
		Vector2 center = getPosition();

		for (Vector2 corner : mCorners) {
			diffVector.set(center).sub(corner);
			float lengthSq = diffVector.len2();

			if (lengthSq > maxLengthSq) {
				maxLengthSq = lengthSq;
			}
		}

		float maxLength = 0;
		if (maxLengthSq > 0) {
			maxLength = (float) Math.sqrt(maxLengthSq);
		}

		return maxLength;
	}

	@Override
	public Vector2 removeCorner(int index) {
		Vector2 removedCorner = null;

		if (index >= 0 && index < mCorners.size()) {
			removedCorner = mCorners.remove(index);
			destroyBodyCorners(index);

			updateVerticesBodyFixtures();

			if (index == 0) {
				updateEnemyPositions();
			}

			if (removedCorner.equals(mRightestCorner)) {
				calculateRightestCorner();
			}
		}

		return removedCorner;
	}

	@Override
	public void clearCorners() {
		mCorners.clear();

		destroyBodyCorners();
		updateVerticesBodyFixtures();
	}

	@Override
	public void moveCorner(int index, Vector2 newPos) {
		if (index >= 0 && index < mCorners.size()) {
			mCorners.get(index).set(newPos);
			resetBodyCorners();
			updateVerticesBodyFixtures();

			if (index == 0) {
				updateEnemyPositions();
			}
		}

		calculateRightestCorner();
	}

	@Override
	public int getCornerCount() {
		return mCorners.size();
	}

	/**
	 * Sets the world for the path, so that a body can be created. This function also sets
	 * activates all body etc, so that it gets drawn.
	 * @param world the world for the path, if null it will disable drawing
	 */
	public void setWorld(World world) {
		mWorld = world;

		if (mWorld != null) {
			createBody();
		}
		// NULL: Dispose everything
		else {
			if (mBody != null) {
				mBody.getWorld().destroyBody(mBody);
				mBody = null;
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
	 * Sets how the enemy shall follow the path. Only applicable if the enemy movement is
	 * set to follow a path.
	 * @param pathType how shall the enemy follow the path
	 */
	public void setPathType(PathTypes pathType) {
		mPathType = pathType;
	}

	/**
	 * @return how the enemy is following a path. Only applicable if the enemy movement is
	 *         set to follow a path.
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

			updateVerticesBodyFixtures();
		}
	}

	@Override
	public void destroyBody() {
		if (mBody != null) {
			mBody.getWorld().destroyBody(mBody);
			mBody = null;

			destroyVertices();
		}
	}

	/**
	 * Checks what index the specified position has.
	 * @param position the position of a corner
	 * @return corner index if a corner was found at position, -1 if none was found.
	 */
	@Override
	public int getCornerIndex(Vector2 position) {
		for (int i = 0; i < mCorners.size(); ++i) {
			if (mCorners.get(i).equals(position)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public void dispose() {
		if (mBody != null) {
			destroyBody();
			destroyBodyCorners();
		}
		destroyVertices();
		mCorners.clear();
	}

	/**
	 * Creates all the body corners
	 */
	@Override
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
	@Override
	public void destroyBodyCorners() {
		while (!mBodyCorners.isEmpty()) {
			destroyBodyCorners(0);
		}
	}

	@Override
	public void setSelected(boolean selected) {
		mSelected = selected;
	}

	@Override
	public boolean isSelected() {
		return mSelected;
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		if (DRAW_COLOR.equals(INITAL_COLOR)) {
			Color color = SkinNames.getResource(SkinNames.EditorVars.PATH_COLOR);
			DRAW_COLOR.set(color);
		}

		if (mVertices != null) {
			RenderOrders.offsetZValueEditor(shapeRenderer, this);

			shapeRenderer.setColor(DRAW_COLOR);
			shapeRenderer.triangles(mVertices);

			if (mSelected) {
				shapeRenderer.translate(0, 0, Config.Graphics.DEPTH_STEP_SIZE);
				shapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.SELECTED_COLOR_UTILITY));
				shapeRenderer.triangles(mVertices);


				// Render corners
				if (!mBodyCorners.isEmpty()) {
					shapeRenderer.push(ShapeType.Line);
					shapeRenderer.translate(0, 0, Config.Graphics.DEPTH_STEP_SIZE);

					shapeRenderer.setColor(Config.Editor.CORNER_COLOR);
					for (Vector2 corner : mCorners) {
						shapeRenderer.polyline(SceneSwitcher.getPickingVertices(), true, corner);
					}

					shapeRenderer.translate(0, 0, -Config.Graphics.DEPTH_STEP_SIZE);
					shapeRenderer.pop();
				}
				shapeRenderer.translate(0, 0, -Config.Graphics.DEPTH_STEP_SIZE);
			}

			RenderOrders.resetZValueOffsetEditor(shapeRenderer, this);
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
	 * Different path types, i.e. how the enemy shall follow a path (if it is set to
	 * follow a path)
	 */
	public enum PathTypes {
		// !!!NEVER EVER remove or change order of these!!!
		/** Goes back and forth on the path */
		BACK_AND_FORTH,
		/**
		 * Once the enemy comes to the last node in the path it walks towards the first
		 * node and goes around in a loop
		 */
		LOOP,
		/**
		 * Follows the path once and then continues in its last direction after it has
		 * come to the last node
		 */
		ONCE
	}

	/**
	 * Creates the fixtures for the path
	 */
	private void createFixture() {
		// Assertion test (to skip crash)
		if (mCorners.size() >= 2) {
			Vector2 diff = new Vector2();
			for (int i = 0; i < mCorners.size() - 1; ++i) {
				diff.set(mCorners.get(i)).sub(mCorners.get(i + 1));
				if (diff.len2() <= CHAIN_CORNER_DISTANCE_MIN) {
					return;
				}
			}
		}

		if (mBody == null || mWorld == null || mVertices == null) {
			return;
		}


		// Temporary vertices for creating polygon shape
		Vector2[] triangleVertices = Collections.fillNew(new Vector2[3], Vector2.class);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.filter.categoryBits = ActorFilterCategories.NONE;
		fixtureDef.filter.groupIndex = ActorFilterCategories.NONE;

		// Create (triangle) polygon shapes and fixture defs
		for (int triangle = 0; triangle < mVertices.size() - 2; triangle += 3) {
			// Copy values so that we have an array instead
			for (int i = 0; i < 3; ++i) {
				triangleVertices[i].set(mVertices.get(triangle + i));
			}

			PolygonShape polygonShape = new PolygonShape();
			polygonShape.set(triangleVertices);
			fixtureDef.shape = polygonShape;

			// Create fixtures
			mBody.createFixture(fixtureDef);

			// Dispose
			polygonShape.dispose();
		}
	}

	/**
	 * Removes existing body fixture and adds the current fixture
	 */
	private void destroyBodyFixture() {
		if (mBody != null) {
			Array<Fixture> originalList = mBody.getFixtureList();
			Array<Fixture> fixtures = new Array<>(originalList);
			for (Fixture fixture : fixtures) {
				mBody.destroyFixture(fixture);
			}
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
		cornerBody.createFixture(SceneSwitcher.getPickingFixtureDef());
		HitWrapper hitWrapper = new HitWrapper(this);
		hitWrapper.data = "picking";
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
			destroyVertices();
		}

		if (mCorners.size() >= 2) {
			mVertices = Geometry.createLinePolygon(mCorners, Config.Editor.Level.Path.WIDTH);
		} else if (mCorners.size() >= 1) {
			ArrayList<Vector2> corners = new ArrayList<>();
			corners.addAll(mCorners);
			Vector2 tempCorner = new Vector2();
			tempCorner.set(mCorners.get(0)).add(Config.Editor.Path.DEFAULT_ADD_PATH);
			corners.add(tempCorner);
			mVertices = Geometry.createLinePolygon(corners, Config.Editor.Level.Path.WIDTH);
			corners = null;
		}
	}

	/**
	 * Destroys all the vertices
	 */
	private void destroyVertices() {
		if (mVertices != null) {
			mVertices = null;
		}
	}

	/**
	 * Update the corner furthest to the right
	 */
	private void calculateRightestCorner() {
		float xCoordMax = Float.NEGATIVE_INFINITY;
		mRightestCorner = null;

		for (Vector2 corner : mCorners) {
			if (corner.x > xCoordMax) {
				xCoordMax = corner.x;
				mRightestCorner = corner;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mCorners == null) ? 0 : mCorners.hashCode());
		result = prime * result + ((mEnemies == null) ? 0 : mEnemies.hashCode());
		result = prime * result + ((mPathType == null) ? 0 : mPathType.hashCode());
		result = prime * result + ((mRightestCorner == null) ? 0 : mRightestCorner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Path other = (Path) obj;
		if (mCorners == null) {
			if (other.mCorners != null) {
				return false;
			}
		} else if (!mCorners.equals(other.mCorners)) {
			return false;
		}
		if (mEnemies == null) {
			if (other.mEnemies != null) {
				return false;
			}
		} else if (!mEnemies.equals(other.mEnemies)) {
			return false;
		}
		if (mPathType != other.mPathType) {
			return false;
		}
		if (mRightestCorner == null) {
			if (other.mRightestCorner != null) {
				return false;
			}
		} else if (!mRightestCorner.equals(other.mRightestCorner)) {
			return false;
		}
		return true;
	}

	@Override
	public void setIsBeingMoved(boolean isBeingMoved) {
		mIsBeingMoved = isBeingMoved;
	}

	@Override
	public boolean isBeingMoved() {
		return mIsBeingMoved;
	}

	@Override
	public void postRead() {
		calculateRightestCorner();
	}


	/** If the resource is being moved */
	private boolean mIsBeingMoved = false;
	/** Path vertices for drawing in editor */
	private ArrayList<Vector2> mVertices = null;
	/** If this path is selected */
	private boolean mSelected = false;
	/** All path nodes */
	@Tag(17) private ArrayList<Vector2> mCorners = new ArrayList<Vector2>();
	/** Corner bodies, for picking */
	private ArrayList<Body> mBodyCorners = new ArrayList<Body>();
	/** Corner farthest to the right */
	private Vector2 mRightestCorner = null;
	/**
	 * What type of path type the enemy uses, only applicable if movement type is set to
	 * path
	 */
	@Tag(18) private PathTypes mPathType = PathTypes.ONCE;
	/** World the path is bound to */
	private World mWorld = null;
	/** Body of the path */
	private Body mBody = null;
	/** Enemies bound to this path */
	@Tag(19) private ArrayList<EnemyActor> mEnemies = new ArrayList<EnemyActor>();

	/** Initial color */
	private static final Color INITAL_COLOR = new Color(0, 0, 0, 0);
	/** Path draw color */
	private static final Color DRAW_COLOR = new Color(INITAL_COLOR);
	/** Minimum distance between chain corners, less than this will assert the program */
	private final static float CHAIN_CORNER_DISTANCE_MIN = 0.005f * 0.005f;
}
