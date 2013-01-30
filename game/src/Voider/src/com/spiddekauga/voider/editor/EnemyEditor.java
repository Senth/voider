package com.spiddekauga.voider.editor;

import java.lang.reflect.Field;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.WorldScene;

/**
 * Editor for creating and editing enemies
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyEditor extends WorldScene {
	/**
	 * Creates the enemy editor
	 */
	public EnemyEditor() {
		super(new EnemyEditorGui());
		mPlayerActor = new PlayerActor();
		mPlayerActor.createBody();
		resetPlayerPosition();
		setEnemyDef();
		createExamplePaths();

		((EnemyEditorGui)mGui).init(mEnemyActor, mEnemyPathOnce, mEnemyPathLoop, mEnemyPathBackAndForth, mDef, this, mPathLabels);

		try {
			mfEnemyOnceReachEnd = EnemyActor.class.getDeclaredField("mPathOnceReachedEnd");
			mfEnemyOnceReachEnd.setAccessible(true);
		} catch (Exception e) {
			Gdx.app.error("EnemyEditor", "Could not access mPathOnceReachEnd");
		}

		createBorder();


		// Create mouse joint
		BodyDef bodyDef = new BodyDef();
		mMouseBody = mWorld.createBody(bodyDef);
		mMouseJointDef.frequencyHz = Config.Game.MouseJoint.FREQUENCY;
		mMouseJointDef.bodyA = mMouseBody;
		mMouseJointDef.bodyB = mPlayerActor.getBody(); // TODO REMOVE, set in onActivate instead
		mMouseJointDef.collideConnected = true;
		mMouseJointDef.maxForce = Config.Game.MouseJoint.FORCE_MAX;
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			mGui.initGui();
		}
		Actor.setPlayerActor(mPlayerActor);
	}

	@Override
	public void update() {
		super.update();
		mPlayerActor.update(Gdx.graphics.getDeltaTime());

		switch (mDef.getMovementType()) {
		case AI:
		case STATIONARY:
			mEnemyActor.update(Gdx.graphics.getDeltaTime());
			break;

		case PATH:
			mEnemyPathLoop.update(Gdx.graphics.getDeltaTime());
			mEnemyPathOnce.update(Gdx.graphics.getDeltaTime());
			mEnemyPathBackAndForth.update(Gdx.graphics.getDeltaTime());

			// Reset Once enemy ever 4 seconds
			if (mfEnemyOnceReachEnd != null) {
				try {
					if ((Boolean)mfEnemyOnceReachEnd.get(mEnemyPathOnce)) {
						if (mEnemyPathOnceOutOfBoundsTime != 0.0f) {
							if (mEnemyPathOnceOutOfBoundsTime + Config.Editor.Enemy.PATH_ONCE_RESET_TIME <= GameTime.getTotalTimeElapsed()) {
								mEnemyPathOnce.resetPathMovement();
								mEnemyPathOnceOutOfBoundsTime = 0.0f;
							}
						} else {
							mEnemyPathOnceOutOfBoundsTime = GameTime.getTotalTimeElapsed();
						}
					}
				} catch (Exception e) {
					Gdx.app.error("EnemyEditor", "Could not access mPathOnceReachEnd");
				}
			}
			break;
		}
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);
		scalePathLabels();
	}

	@Override
	public void onDisposed() {
		mPlayerActor.dispose();
		mEnemyActor.dispose();
		mEnemyPathBackAndForth.dispose();
		mEnemyPathLoop.dispose();
		mEnemyPathOnce.dispose();
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void loadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.loadAllOf(EnemyActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.unloadAllOf(EnemyActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}

	// --------------------------------
	//		INPUT EVENTS (not gui)
	// --------------------------------
	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// Test if touching player
		if (mPlayerPointer == INVALID_POINTER) {
			screenToWorldCoord(mCamera, x, y, mCursorWorld, true);

			mWorld.QueryAABB(mCallback, mCursorWorld.x - 0.0001f, mCursorWorld.y - 0.0001f, mCursorWorld.x + 0.0001f, mCursorWorld.y + 0.0001f);

			if (mMovingPlayer) {
				mPlayerPointer = pointer;

				mMouseJointDef.target.set(mPlayerActor.getBody().getPosition());
				mMouseJoint = (MouseJoint) mWorld.createJoint(mMouseJointDef);
				mMouseJoint.setTarget(mCursorWorld);
				mPlayerActor.getBody().setAwake(true);

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (mPlayerPointer == pointer && mMovingPlayer) {
			screenToWorldCoord(mCamera, x, y, mCursorWorld, true);
			mMouseJoint.setTarget(mCursorWorld);
			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (mPlayerPointer == pointer && mMovingPlayer) {
			mPlayerPointer = INVALID_POINTER;
			mMovingPlayer = false;

			mWorld.destroyJoint(mMouseJoint);
			mMouseJoint = null;

			return true;
		}

		return false;
	}

	/**
	 * Resets the player position
	 */
	void resetPlayerPosition() {
		Vector2 playerPosition = Pools.obtain(Vector2.class);
		Scene.screenToWorldCoord(mCamera, Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.5f, playerPosition, true);
		mPlayerActor.setPosition(playerPosition);
		mPlayerActor.getBody().setLinearVelocity(0, 0);
	}

	/**
	 * Creates enemy bodies of the paths
	 */
	void createPathBodies() {
		mPathOnce.setWorld(mWorld);
		mPathLoop.setWorld(mWorld);
		mPathBackAndForth.setWorld(mWorld);
		mEnemyPathOnce.createBody();
		mEnemyPathOnce.resetPathMovement();
		mEnemyPathLoop.createBody();
		mEnemyPathLoop.resetPathMovement();
		mEnemyPathBackAndForth.createBody();
		mEnemyPathBackAndForth.resetPathMovement();
		mEnemyPathOnceOutOfBoundsTime = 0f;
	}

	/**
	 * Clears all the example paths
	 */
	void clearExamplePaths() {
		mPathOnce.setWorld(null);
		mPathLoop.setWorld(null);
		mPathBackAndForth.setWorld(null);
		mEnemyPathOnce.destroyBody();
		mEnemyPathLoop.destroyBody();
		mEnemyPathBackAndForth.destroyBody();

		// Clear GUI text
		mGui.reset();
	}

	/**
	 * Scale label for paths
	 */
	void scalePathLabels() {
		float spaceBetween = Gdx.graphics.getHeight() * 0.1f;
		float height = Gdx.graphics.getHeight() * 0.2f;
		float initialOffset = spaceBetween + height * 0.5f + spaceBetween + height;

		mPathLabels.setPosition(Gdx.graphics.getWidth() / 3f, initialOffset);


		// Fix padding
		SnapshotArray<com.badlogic.gdx.scenes.scene2d.Actor> actors = mPathLabels.getChildren();
		// Reset padding first
		for (int i = 0; i < actors.size - 1; ++i) {
			if (actors.get(i) instanceof Table) {
				Table table = (Table) actors.get(i);
				table.padBottom(0);
				table.invalidateHierarchy();
			}
		}

		for (int i = 0; i < actors.size - 1; ++i) {
			if (actors.get(i) instanceof Table) {
				Table table = (Table) actors.get(i);
				table.padBottom(spaceBetween + height - table.getPrefHeight());
				table.invalidateHierarchy();
			}
		}
	}

	/**
	 * Creates a new enemy
	 */
	void newEnemy() {
		mDef = new EnemyActorDef();
		setEnemyDef();
	}

	/** Invalid pointer id */
	private static final int INVALID_POINTER = -1;
	/** Current pointer that moves the player */
	private int mPlayerPointer = INVALID_POINTER;
	/** If we're currently moving the player */
	private boolean mMovingPlayer = false;
	/** Mouse joint definition */
	private MouseJointDef mMouseJointDef = new MouseJointDef();
	/** Mouse joint for player */
	private MouseJoint mMouseJoint = null;
	/** Body of the mouse, for mouse joint */
	private Body mMouseBody = null;
	/** Callback for "ray testing" */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.testPoint(mCursorWorld.x, mCursorWorld.y)) {
				if (fixture.getBody().getUserData() instanceof PlayerActor) {
					mMovingPlayer = true;
					return false;
				}
			}
			return true;
		}
	};
	/** World coordinate for the cursor */
	private Vector2 mCursorWorld = new Vector2();

	/**
	 * Creates the example paths that are used
	 */
	private void createExamplePaths() {
		// All paths should be like each other, so the player clearly sees the
		// difference between how they work

		// Create a shape like this:
		// 2 ------- 1
		// 3 |     | 0
		Vector2[] nodes = new Vector2[4];
		Vector2[] screenPos = new Vector2[4];
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i] = Pools.obtain(Vector2.class);
			nodes[i] = Pools.obtain(Vector2.class);
		}
		// X-area: From middle of screen to 1/6 of the screen width
		// Y-area: Height of each path should be 1/5. Offset it with 1/20 so it doesn't touch the borders
		float spaceBetween = Gdx.graphics.getHeight() * 0.1f;
		float height = Gdx.graphics.getHeight() * 0.2f;
		float heightOffset = height + spaceBetween;
		float initialOffset = spaceBetween;
		// 0
		screenPos[0].set(Gdx.graphics.getWidth() * 0.5f, initialOffset + height);
		// 1
		screenPos[1].set(screenPos[0]);
		screenPos[1].y = initialOffset;
		// 2
		screenPos[2].set(screenPos[1]);
		screenPos[2].x = Gdx.graphics.getWidth() / 6f;
		// 3
		screenPos[3].set(screenPos[2]);
		screenPos[3].y = initialOffset + height;


		// BACK AND FORTH
		mPathBackAndForth.setPathType(PathTypes.BACK_AND_FORTH);
		mEnemyPathBackAndForth.setPath(mPathBackAndForth);
		for (int i = 0; i < nodes.length; ++i) {
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			mPathBackAndForth.addNodeToBack(nodes[i]);
		}

		// LOOP
		mPathLoop.setPathType(PathTypes.LOOP);
		mEnemyPathLoop.setPath(mPathLoop);
		// Offset all y values so we don't get same path
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i].y += heightOffset;
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			mPathLoop.addNodeToBack(nodes[i]);
		}

		// ONCE
		mPathOnce.setPathType(PathTypes.ONCE);
		mEnemyPathOnce.setPath(mPathOnce);
		// Offset all y values so we don't get same path
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i].y += heightOffset;
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			mPathOnce.addNodeToBack(nodes[i]);
		}

		// Free stuff
		for (int i = 0; i < nodes.length; ++i) {
			Pools.free(nodes[i]);
			Pools.free(screenPos[i]);
		}
	}

	/**
	 * Sets the definition for the enemy actors. Uses the definition from
	 * mDef.
	 */
	private void setEnemyDef() {
		mEnemyActor.setDef(mDef);
		mEnemyPathOnce.setDef(mDef);
		mEnemyPathLoop.setDef(mDef);
		mEnemyPathBackAndForth.setDef(mDef);
	}

	/** Current enemy actor */
	private EnemyActor mEnemyActor = new EnemyActor();
	/** Enemy actor for path once */
	private EnemyActor mEnemyPathOnce = new EnemyActor();
	/** Enemy actor for path loop */
	private EnemyActor mEnemyPathLoop = new EnemyActor();
	/** Enemy actor for path back and forth */
	private EnemyActor mEnemyPathBackAndForth = new EnemyActor();
	/** Current enemy actor definition */
	private EnemyActorDef mDef = new EnemyActorDef();
	/** If actor has been saved since edit */
	private boolean mActorSavedSinceLastEdit = false;
	/** Display path how once works */
	private Path mPathOnce = new Path();
	/** Display path how loop works */
	private Path mPathLoop = new Path();
	/** Display path how back and forth works */
	private Path mPathBackAndForth = new Path();
	/** When the ONCE enemy path actor was removed */
	private float mEnemyPathOnceOutOfBoundsTime = 0.0f;
	/** Field for accessing when the ONCE enemy actor reached the end */
	private Field mfEnemyOnceReachEnd = null;
	/** Player actor, for the enemies to work properly */
	private PlayerActor mPlayerActor = null;

	/** Table for path lables, these are added directly to the stage */
	private Table mPathLabels = new Table();
}
