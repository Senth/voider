package com.spiddekauga.voider.game.triggers;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.game.triggers.TriggerAction.Reasons;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;
/**
 * Triggers when the right side of the screen is at or beyond a specific position.
 * Equal to the level's current x-coordinate
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TScreenAt extends Trigger implements IResourceBody, IResourcePosition, Disposable {
	/**
	 * @param level checks this level for the x coordinate
	 * @param xCoord the x-coordinate we want the level to be at or beyond.
	 */
	public TScreenAt(Level level, float xCoord) {
		mLevel = level;
		mPosition.x = xCoord;
		mPosition.y = 0;
	}

	@Override
	protected Reasons getReason() {
		return Reasons.SCREEN_AT;
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		if (mVertices != null) {
			RenderOrders.offsetZValueEditor(shapeRenderer, this);

			shapeRenderer.setColor(Config.Editor.Level.Trigger.COLOR);
			shapeRenderer.triangles(mVertices);

			if (isSelected()) {
				shapeRenderer.translate(0, 0, Config.Graphics.DEPTH_STEP_SIZE);
				shapeRenderer.setColor(Config.Editor.SELECTED_COLOR);
				shapeRenderer.triangles(mVertices);
				shapeRenderer.translate(0, 0, -Config.Graphics.DEPTH_STEP_SIZE);
			}

			RenderOrders.resetZValueOffsetEditor(shapeRenderer, this);
		}
	}

	@Override
	public float getBoundingRadius() {
		return 0;
	}

	@Override
	protected Object getCauseObject() {
		return null;
	}

	@Override
	public boolean isTriggered() {
		return mLevel.getXCoord() >= mPosition.x;
	}

	@Override
	public void createBody() {
		if (mBody == null && !isHidden()) {
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

			createVertices();
		}
	}

	@Override
	public void destroyBody() {
		if (mBody != null) {
			mBody.getWorld().destroyBody(mBody);
			mBody = null;
		}

		destroyVertices();
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

		createVertices();
	}

	@Override
	public Vector2 getPosition() {
		return mPosition;
	}

	@Override
	public void dispose() {
		Pools.vector2.free(mPosition);
		mPosition = null;
		destroyBody();
	}

	@Override
	public RenderOrders getRenderOrder() {
		return RenderOrders.TRIGGER_SCREEN_AT;
	}

	/**
	 * Constructor for Kryo
	 */
	protected TScreenAt() {
		// Does nothing
	}

	/**
	 * Creates the vertices for the graphical version of the trigger
	 */
	private void createVertices() {
		if (!isHidden()) {
			destroyVertices();

			float halfHeight = SceneSwitcher.getWorldHeight() * 0.5f;

			// Create vertices for the body
			Vector2 upperVertex = Pools.vector2.obtain();
			Vector2 lowerVertex = Pools.vector2.obtain();
			upperVertex.set(0, -halfHeight).add(mPosition);
			lowerVertex.set(0, halfHeight).add(mPosition);

			ArrayList<Vector2> line = new ArrayList<Vector2>();
			line.add(upperVertex);
			line.add(lowerVertex);

			mVertices = Geometry.createLinePolygon(line, Config.Editor.Level.Trigger.SCREEN_AT_WIDTH);

			Pools.vector2.freeAll(line);
		}
	}

	/**
	 * Destroys the graphical version of the trigger
	 */
	private void destroyVertices() {
		if (mVertices != null) {
			Pools.vector2.freeDuplicates(mVertices);
			Pools.arrayList.free(mVertices);
			mVertices = null;
		}
	}

	@Override
	public void setIsBeingMoved(boolean isBeingMoved) {
		mIsBeingMoved = isBeingMoved;
	}

	@Override
	public boolean isBeingMoved() {
		return mIsBeingMoved;
	}

	/** If the trigger is being moved */
	private boolean mIsBeingMoved = false;
	/** Vertices for the trigger */
	private ArrayList<Vector2> mVertices = null;
	/** Body of the trigger */
	private Body mBody = null;
	/** Level to check for the x-coordinate */
	@Tag(34) private Level mLevel = null;
	/** Temporary position, stores x-coord for getting the position */
	@Tag(35) private Vector2 mPosition = Pools.vector2.obtain();
}
