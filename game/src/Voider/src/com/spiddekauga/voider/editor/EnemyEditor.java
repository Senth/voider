package com.spiddekauga.voider.editor;

import java.lang.reflect.Field;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.DrawActorTool;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.scene.WorldScene;

/**
 * Editor for creating and editing enemies
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyEditor extends WorldScene implements IActorEditor, IActorDrawEditor {
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

		((EnemyEditorGui)mGui).setEnemyEditor(this);

		try {
			mfEnemyOnceReachEnd = EnemyActor.class.getDeclaredField("mPathOnceReachedEnd");
			mfEnemyOnceReachEnd.setAccessible(true);
		} catch (Exception e) {
			Gdx.app.error("EnemyEditor", "Could not access mPathOnceReachEnd");
		}

		createBorder();

		mDef.setMovementType(null);

		// Create mouse joint
		BodyDef bodyDef = new BodyDef();
		mMouseBody = mWorld.createBody(bodyDef);
		mMouseJointDef.frequencyHz = Config.Game.MouseJoint.FREQUENCY;
		mMouseJointDef.bodyA = mMouseBody;
		mMouseJointDef.bodyB = mPlayerActor.getBody();
		mMouseJointDef.collideConnected = true;
		mMouseJointDef.maxForce = Config.Game.MouseJoint.FORCE_MAX;
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);

		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			mGui.initGui();

			// Path labels
			Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
			LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
			Label label = new Label("Back and Forth", labelStyle);
			Table wrapTable = new Table();
			wrapTable.add(label);
			mPathLabels.add(wrapTable);
			mPathLabels.row();

			label = new Label("Loop", labelStyle);
			wrapTable = new Table();
			wrapTable.add(label);
			mPathLabels.add(wrapTable);
			mPathLabels.row();

			label = new Label("Once", labelStyle);
			wrapTable = new Table();
			wrapTable.add(label);
			mPathLabels.add(wrapTable);
			mPathLabels.row();

			scalePathLabels();

			mActorSavedSinceLastEdit = true;
		}
		else if (outcome == Outcomes.DEF_SELECTED) {
			switch (mSelectionAction) {
			case BULLET_TYPE:
				try {
					BulletActorDef bulletDef = ResourceCacheFacade.get(UUID.fromString(message), BulletActorDef.class);
					setBulletActorDef(bulletDef);
					mGui.resetValues();
				} catch (UndefinedResourceTypeException e1) {
					Gdx.app.error("EnemyEditor", e1.toString());
				}
				break;

			case LOAD_ENEMY:
				try {
					mDef = ResourceCacheFacade.get(UUID.fromString(message), EnemyActorDef.class);
					setEnemyDef();
					mGui.resetValues();
					mActorSavedSinceLastEdit = true;
				} catch (UndefinedResourceTypeException e) {
					Gdx.app.error("EnemyEditor", e.toString());
				}
				break;
			}

			mSelectionAction = null;
		}

		Actor.setEditorActive(true);
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
							if (mEnemyPathOnceOutOfBoundsTime + Enemy.Movement.PATH_ONCE_RESET_TIME <= SceneSwitcher.getGameTime().getTotalTimeElapsed()) {
								mEnemyPathOnce.resetPathMovement();
								mEnemyPathOnceOutOfBoundsTime = 0.0f;
							}
						} else {
							mEnemyPathOnceOutOfBoundsTime = SceneSwitcher.getGameTime().getTotalTimeElapsed();
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

		super.onDisposed();
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
			ResourceCacheFacade.loadAllOf(BulletActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.unloadAllOf(EnemyActorDef.class, true);
			ResourceCacheFacade.unloadAllOf(BulletActorDef.class, true);
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
	 * Saves the current enemy actor
	 */
	public void saveActor() {
		ResourceSaver.save(mDef);

		// Load the saved actor and use it instead
		if (!ResourceCacheFacade.isLoaded(mDef.getId(), mDef.getClass())) {
			try {
				ResourceCacheFacade.load(mDef.getId(), mDef.getClass(), true);
				ResourceCacheFacade.finishLoading();

				mDef = ResourceCacheFacade.get(mDef.getId(), mDef.getClass());
			} catch (Exception e) {
				Gdx.app.error("EnemyEditor", "Loading of saved actor failed! " + e.toString());
			}
		}

		mActorSavedSinceLastEdit = true;
	}

	/**
	 * Creates a new enemy
	 */
	public void newActor() {
		mDef = new EnemyActorDef();
		setEnemyDef();
		mGui.resetValues();
		mActorSavedSinceLastEdit = true;
	}

	/**
	 * Sets the movement speed of the enemy
	 * @param speed new movement speed of the enemy
	 */
	void setSpeed(float speed) {
		mDef.setSpeed(speed);
		mEnemyActor.setSpeed(speed);
		mEnemyPathBackAndForth.setSpeed(speed);
		mEnemyPathLoop.setSpeed(speed);
		mEnemyPathOnce.setSpeed(speed);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return movement speed of the enemy
	 */
	float getSpeed() {
		return mDef.getSpeed();
	}

	/**
	 * Sets the turning of the enemy
	 * @param enabled true if enemy turning is enabled
	 */
	void setTurning(boolean enabled) {
		mDef.setTurn(enabled);
		mEnemyActor.resetPathMovement();
		mEnemyPathBackAndForth.resetPathMovement();
		mEnemyPathLoop.resetPathMovement();
		mEnemyPathOnce.resetPathMovement();
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return true if the enemy is turning
	 */
	boolean isTurning() {
		return mDef.isTurning();
	}

	@Override
	public boolean isUnsaved() {
		return !mActorSavedSinceLastEdit;
	}

	@Override
	public boolean hasUndo() {
		return false;
	}

	@Override
	public void undo() {
		mInvoker.undo();
	}

	@Override
	public void redo() {
		mInvoker.redo();
	}

	@Override
	public Invoker getInvoker() {
		return mInvoker;
	}

	/**
	 * Sets the minimum distance from the player the enemy want to be
	 * @param minDistance minimum distance from the player
	 */
	void setPlayerDistanceMin(float minDistance) {
		mDef.setPlayerDistanceMin(minDistance);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return minimum distance from the player the enemy wants to be
	 */
	float getPlayerDistanceMin() {
		return mDef.getPlayerDistanceMin();
	}

	/**
	 * @return maximum distance from the player the enemy wants to be
	 */
	float getPlayerDistanceMax() {
		return mDef.getPlayerDistanceMax();
	}

	/**
	 * Sets the maximum distance from the player the enemy want to be
	 * @param maxDistance maximum distance from the player
	 */
	void setPlayerDistanceMax(float maxDistance) {
		mDef.setPlayerDistanceMax(maxDistance);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return movement type of the enemy
	 */
	MovementTypes getMovementType() {
		return mDef.getMovementType();
	}

	/**
	 * Sets the movement type for the enemy
	 * @param movementType new movement type
	 */
	void setMovementType(MovementTypes movementType) {
		mEnemyActor.destroyBody();
		mDef.setMovementType(movementType);

		switch (movementType) {
		case PATH:
			mGui.addActor(mPathLabels);
			createPathBodies();
			resetPlayerPosition();
			break;

		case STATIONARY:
			clearExamplePaths();
			createEnemyActor();
			resetPlayerPosition();
			break;

		case AI:
			clearExamplePaths();
			createEnemyActor();
			resetPlayerPosition();
			break;
		}
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * Sets the turning speed of the enemy
	 * @param turnSpeed how fast the enemy shall turn
	 */
	void setTurnSpeed(float turnSpeed) {
		mDef.setTurnSpeed(turnSpeed);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return turning speed of the enemy
	 */
	float getTurnSpeed() {
		return mDef.getTurnSpeed();
	}

	@Override
	public void setStartingAngle(float angle) {
		mDef.setStartAngle(angle);
		mActorSavedSinceLastEdit = false;
	}

	@Override
	public void setRotationSpeed(float rotationSpeed) {
		mDef.setRotationSpeed(rotationSpeed);
		mActorSavedSinceLastEdit = false;
	}

	@Override
	public float getRotationSpeed() {
		return mDef.getRotationSpeed();
	}

	@Override
	public void setDrawActorToolState(DrawActorTool.States state) {
		// Does nothing
	}

	@Override
	public DrawActorTool.States getDrawActorToolState() {
		return null;
	}

	@Override
	public float getStartingAngle() {
		return mDef.getStartAngle();
	}

	/**
	 * Sets if the enemy shall move randomly using the random spread set through
	 * #setRandomSpread(float).
	 * @param moveRandomly true if the enemy shall move randomly.
	 */
	void setMoveRandomly(boolean moveRandomly) {
		mDef.setMoveRandomly(moveRandomly);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return true if the enemy shall move randomly.
	 * @see #setRandomTimeMin(float) to set how random the enemy shall move
	 */
	boolean isMovingRandomly() {
		return mDef.isMovingRandomly();
	}

	/**
	 * Sets the minimum time that must have passed until the enemy will decide
	 * on another direction.
	 * @param minTime how many degrees it will can move
	 * @see #setMoveRandomly(boolean) to activate/deactivate the random movement
	 */
	void setRandomTimeMin(float minTime) {
		mDef.setRandomTimeMin(minTime);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return Minimum time until next random move
	 */
	float getRandomTimeMin() {
		return mDef.getRandomTimeMin();
	}

	/**
	 * Sets the maximum time that must have passed until the enemy will decide
	 * on another direction.
	 * @param maxTime how many degrees it will can move
	 * @see #setMoveRandomly(boolean) to activate/deactivate the random movement
	 */
	void setRandomTimeMax(float maxTime) {
		mDef.setRandomTimeMax(maxTime);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return Maximum time until next random move
	 */
	float getRandomTimeMax() {
		return mDef.getRandomTimeMax();
	}

	/**
	 * @return the bullet actor definition of this enemy, null if it doesn't shoot.
	 */
	BulletActorDef getBulletActorDef() {
		return mDef.getWeaponDef().getBulletActorDef();
	}

	/**
	 * Sets if the enemy shall use weapons or not
	 * @param useWeapon true if the enemy shall use weapons
	 */
	void setUseWeapon(boolean useWeapon) {
		mDef.setUseWeapon(useWeapon);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return true if the enemy shall use weapons
	 */
	boolean hasWeapon() {
		return mDef.hasWeapon();
	}

	/**
	 * Sets the bullet speed
	 * @param speed new bullet speed
	 */
	void setBulletSpeed(float speed) {
		mDef.getWeaponDef().setBulletSpeed(speed);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return the bullet speed
	 */
	float getBulletSpeed() {
		return mDef.getWeaponDef().getBulletSpeed();
	}

	/**
	 * Sets the weapon damage
	 * @param damage how much damage the bullets will take when they hit something
	 */
	void setWeaponDamage(float damage) {
		mDef.getWeaponDef().setDamage(damage);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return weapon damage
	 */
	float getWeaponDamage() {
		return mDef.getWeaponDef().getDamage();
	}

	/**
	 * Sets the minimum weapon cooldown. If this is equal to the max value set
	 * through #setCooldownMax(float) it will always have the same cooldown; if not
	 * it will get a random cooldown between min and max time.
	 * @param minCooldown minimum cooldown.
	 */
	void setCooldownMin(float minCooldown) {
		mDef.getWeaponDef().setCooldownMin(minCooldown);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return minimum cooldown time
	 */
	float getCooldownMin() {
		return mDef.getWeaponDef().getCooldownMin();
	}

	/**
	 * Sets the maximum weapon cooldown. If this is equal to the min value set
	 * through #setCooldownMin(float) it will always have the same cooldown; if not
	 * it will get a random cooldown between min and max time.
	 * @param maxCooldown maximum cooldown.
	 */
	void setCooldownMax(float maxCooldown) {
		mDef.getWeaponDef().setCooldownMax(maxCooldown);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return minimum cooldown time
	 */
	float getCooldownMax() {
		return mDef.getWeaponDef().getCooldownMax();
	}

	/**
	 * Sets the aim type of the enemy
	 * @param aimType new aim type
	 */
	void setAimType(AimTypes aimType) {
		mDef.setAimType(aimType);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return the aim type of the enemy
	 */
	AimTypes getAimType() {
		return mDef.getAimType();
	}

	/**
	 * Sets the starting aim angle, when rotating
	 * @param angle starting angle of aim.
	 */
	void setAimStartAngle(float angle) {
		mDef.setAimStartAngle(angle);
		mEnemyActor.resetWeapon();
		mEnemyPathBackAndForth.resetWeapon();
		mEnemyPathOnce.resetWeapon();
		mEnemyPathLoop.resetWeapon();
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return starting aim angle.
	 */
	float getAimStartAngle() {
		return mDef.getAimStartAngle();
	}

	/**
	 * Sets the aim's rotation speed. Only applicable when aim is set
	 * to rotating.
	 * @param rotateSpeed new rotation speed
	 */
	void setAimRotateSpeed(float rotateSpeed) {
		mDef.setAimRotateSpeed(rotateSpeed);
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return aim's rotation speed.
	 */
	float getAimRotateSpeed() {
		return mDef.getAimRotateSpeed();
	}

	/**
	 * Switches scene to select a bullet type for the weapon
	 */
	void selectBulletType() {
		mSelectionAction = SelectionActions.BULLET_TYPE;

		Scene selectionScene = new SelectDefScene(BulletActorDef.class, false, false);
		SceneSwitcher.switchTo(selectionScene);
	}

	@Override
	public void setShapeType(ActorShapeTypes shapeType) {
		mDef.setShapeType(shapeType);

		resetBodyShapes();
	}

	@Override
	public ActorShapeTypes getShapeType() {
		return mDef.getShapeType();
	}

	@Override
	public void setShapeRadius(float radius) {
		mDef.setShapeRadius(radius);

		resetBodyShapes();
	}

	@Override
	public float getShapeRadius() {
		return mDef.getShapeRadius();
	}

	@Override
	public void setShapeWidth(float width) {
		mDef.setShapeWidth(width);

		resetBodyShapes();
	}

	@Override
	public float getShapeWidth() {
		return mDef.getShapeWidth();
	}

	@Override
	public void setShapeHeight(float height) {
		mDef.setShapeHeight(height);

		resetBodyShapes();
	}

	@Override
	public float getShapeHeight() {
		return mDef.getShapeHeight();
	}

	@Override
	public void resetCenterOffset() {
		// Save diff offset and move the actor in the opposite direction...
		//		Vector2 diffOffset = null;
		//		if (mBulletActor != null) {
		//			mBulletActor.destroyBody();
		//
		//			diffOffset = Pools.obtain(Vector2.class);
		//			diffOffset.set(mDef.getCenterOffset());
		//		}

		mDef.resetCenterOffset();

		//		if (mBulletActor != null) {
		//			diffOffset.sub(mDef.getCenterOffset());
		//			diffOffset.add(mBulletActor.getPosition());
		//			mBulletActor.setPosition(diffOffset);
		//			mBulletActor.createBody();
		//			Pools.free(diffOffset);
		//		}
	}

	@Override
	public void setCenterOffset(Vector2 newCenter) {
		// Save diff offset and move the actor in the opposite direction...
		//		Vector2 diffOffset = null;
		//		if (mBulletActor != null) {
		//			mBulletActor.destroyBody();
		//
		//			diffOffset = Pools.obtain(Vector2.class);
		//			diffOffset.set(mDef.getCenterOffset());
		//		}

		mDef.setCenterOffset(newCenter);

		//		if (mBulletActor != null) {
		//			diffOffset.sub(mDef.getCenterOffset());
		//			diffOffset.add(mBulletActor.getPosition());
		//			mBulletActor.setPosition(diffOffset);
		//			mBulletActor.createBody();
		//			Pools.free(diffOffset);
		//		}
	}

	@Override
	public Vector2 getCenterOffset() {
		return mDef.getCenterOffset();
	}

	@Override
	public void setName(String name) {
		mDef.setName(name);
	}

	@Override
	public String getName() {
		return mDef.getName();
	}

	@Override
	public void setDescription(String description) {
		mDef.setDescription(description);
	}

	@Override
	public String getDescription() {
		return mDef.getDescription();
	}

	/**
	 * Switches scene to load an enemy
	 */
	@Override
	public void loadActor() {
		mSelectionAction = SelectionActions.LOAD_ENEMY;

		Scene selectionScene = new SelectDefScene(EnemyActorDef.class, true, true);
		SceneSwitcher.switchTo(selectionScene);
	}

	/**
	 * Duplicates the current enemy
	 */
	@Override
	public void duplicateActor() {
		mDef = (EnemyActorDef) mDef.copy();
		setEnemyDef();
		mGui.resetValues();
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * @return name of the bullet actor definition the enemies use, "" if they aren't
	 * using weapons or has no bullet actor definition set.
	 */
	String getBulletName() {
		if (mDef.getWeaponDef() != null && mDef.getWeaponDef().getBulletActorDef() != null) {
			return mDef.getWeaponDef().getBulletActorDef().getName();
		} else {
			return "";
		}
	}

	/**
	 * Sets colliding damage of the enemy
	 * @param damage how much damage the enemy will inflict on a collision
	 */
	@Override
	public void setCollisionDamage(float damage) {
		mDef.setCollisionDamage(damage);
	}

	/**
	 * @return collision damage with the enemy
	 */
	@Override
	public float getCollisionDamage() {
		return mDef.getCollisionDamage();
	}

	/**
	 * Sets whether this actor shall be destroyed on collision
	 * @param destroyOnCollision set to true to destroy the enemy on collision
	 */
	@Override
	public void setDestroyOnCollide(boolean destroyOnCollision) {
		mDef.setDestroyOnCollide(destroyOnCollision);
	}

	/**
	 * @return true if this enemy shall be destroyed on collision
	 */
	@Override
	public boolean shallDestroyOnCollide() {
		return mDef.shallDestroyOnCollide();
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
		mEnemyPathBackAndForth.resetPathMovement();
		for (int i = 0; i < nodes.length; ++i) {
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			mPathBackAndForth.addNodeToBack(nodes[i]);
		}

		// LOOP
		mPathLoop.setPathType(PathTypes.LOOP);
		mEnemyPathLoop.setPath(mPathLoop);
		mEnemyPathLoop.resetPathMovement();
		// Offset all y values so we don't get same path
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i].y += heightOffset;
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			mPathLoop.addNodeToBack(nodes[i]);
		}

		// ONCE
		mPathOnce.setPathType(PathTypes.ONCE);
		mEnemyPathOnce.setPath(mPathOnce);
		mEnemyPathOnce.resetPathMovement();
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

	/**
	 * Creates enemy bodies of the paths
	 */
	private void createPathBodies() {
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
	private void clearExamplePaths() {
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
	 * Resets the player position
	 */
	private void resetPlayerPosition() {
		Vector2 playerPosition = Pools.obtain(Vector2.class);
		Scene.screenToWorldCoord(mCamera, Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.5f, playerPosition, true);
		mPlayerActor.setPosition(playerPosition);
		mPlayerActor.getBody().setLinearVelocity(0, 0);
	}

	/**
	 * Scale label for paths
	 */
	private void scalePathLabels() {
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
	 * Creates the enemy actor and resets its position
	 */
	private void createEnemyActor() {
		mEnemyActor.setPosition(0, 0);
		mEnemyActor.createBody();
	}

	/**
	 * Resets all the bodies shapes to use the new updated shape
	 */
	private void resetBodyShapes() {
		mEnemyActor.reloadFixtures();
		mEnemyPathBackAndForth.reloadFixtures();
		mEnemyPathLoop.reloadFixtures();
		mEnemyPathOnce.reloadFixtures();
	}

	/**
	 * Sets the bullet actor definition. I.e. what bullets it shall shoot
	 * @param bulletActorDef the bullet definition, set to null to deactivate shooting
	 */
	private void setBulletActorDef(BulletActorDef bulletActorDef) {
		if (mDef.getWeaponDef().getBulletActorDef() != null) {
			mDef.removeDependency(mDef.getWeaponDef().getBulletActorDef().getId());
		}

		mDef.getWeaponDef().setBulletActorDef(bulletActorDef);

		if (bulletActorDef != null) {
			mDef.addDependency(bulletActorDef);
		}
		mActorSavedSinceLastEdit = false;
	}

	/**
	 * Which selection action we're currently using (when a SelectDefScene is active)
	 */
	enum SelectionActions {
		/** Bullet type for the weapon */
		BULLET_TYPE,
		/** Load an existing enemy */
		LOAD_ENEMY,
	}


	/** Current selection action, null if none */
	private SelectionActions mSelectionAction = null;
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
	private boolean mActorSavedSinceLastEdit = true;
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
	/** Invoker */
	private Invoker mInvoker = new Invoker();

	/** Table for path lables, these are added directly to the stage */
	private Table mPathLabels = new Table();

	@Override
	public void onActorAdded(Actor actor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActorRemoved(Actor actor) {
		// TODO Auto-generated method stub

	}
}
