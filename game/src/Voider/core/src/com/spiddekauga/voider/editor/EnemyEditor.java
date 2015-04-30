package com.spiddekauga.voider.editor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Ship;
import com.spiddekauga.voider.editor.Editor.ImageSaveOnActor.Locations;
import com.spiddekauga.voider.editor.commands.CEnemyBulletDefSelect;
import com.spiddekauga.voider.explore.ExploreActions;
import com.spiddekauga.voider.explore.ExploreFactory;
import com.spiddekauga.voider.game.CollisionResolver;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.network.resource.BulletDefEntity;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Editor for creating and editing enemies
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class EnemyEditor extends ActorEditor {
	/**
	 * Creates the enemy editor
	 */
	public EnemyEditor() {
		super(new EnemyEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR, EnemyActorDef.class, EnemyActor.class);

		getGui().setEnemyEditor(this);
	}

	@Override
	protected void onInit() {
		super.onInit();


		mPlayerActor = new PlayerActor();
		PlayerActorDef defaultShip = ResourceLocalRepo.getPlayerShipDefault();
		if (defaultShip != null) {
			mPlayerActor.setDef(defaultShip);
		}

		createExamplePaths();

		mWorld.setContactListener(mCollisionResolver);

		try {
			mfEnemyOnceReachEnd = EnemyActor.class.getDeclaredField("mPathOnceReachedEnd");
			mfEnemyOnceReachEnd.setAccessible(true);
		} catch (Exception e) {
			Gdx.app.error("EnemyEditor", "Could not access mPathOnceReachEnd");
		}

		createBorder();

		// Create mouse joint
		IC_Ship.IC_Settings icSettings = ConfigIni.getInstance().editor.ship.settings;
		mMouseJointDef.frequencyHz = icSettings.getFrequencyDefault();
		mMouseJointDef.bodyA = mWorld.createBody(new BodyDef());
		mMouseJointDef.collideConnected = true;
		mMouseJointDef.maxForce = mPlayerActor.getDef(PlayerActorDef.class).getMouseJointForceMax();
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		if (!mPlayerActor.hasBody()) {
			mPlayerActor.createBody();
			resetPlayerPosition();
			mMouseJointDef.bodyB = mPlayerActor.getBody();
		}

		Actor.setPlayerActor(mPlayerActor);

		if (outcome == Outcomes.EXPLORE_SELECT) {
			if (message instanceof BulletDefEntity) {
				mInvoker.execute(new CEnemyBulletDefSelect(((BulletDefEntity) message).resourceId, this));
			}
		} else if (outcome == Outcomes.EXPLORE_LOAD) {
			if (message instanceof EnemyDefEntity) {
				EnemyDefEntity enemyDefEntity = (EnemyDefEntity) message;

				if (!ResourceCacheFacade.isLoaded(enemyDefEntity.resourceId, enemyDefEntity.revision)) {
					ResourceCacheFacade.load(this, enemyDefEntity.resourceId, true, enemyDefEntity.revision);
					ResourceCacheFacade.finishLoading();
				}

				setActorDef((EnemyActorDef) ResourceCacheFacade.get(enemyDefEntity.resourceId, enemyDefEntity.revision));
				setMovementType(mDef.getMovementType());
				getGui().resetValues();
				setSaved();
			}
		} else if (outcome == Outcomes.NOT_APPLICAPLE) {
			getGui().popMsgBoxes();
		}

		EventDispatcher.getInstance().connect(EventTypes.CAMERA_ZOOM_CHANGE, mBorderLlistener);
		EventDispatcher.getInstance().connect(EventTypes.CAMERA_MOVED, mBorderLlistener);
	}

	@Override
	protected void onDeactivate() {
		EventDispatcher.getInstance().disconnect(EventTypes.CAMERA_ZOOM_CHANGE, mBorderLlistener);
		EventDispatcher.getInstance().disconnect(EventTypes.CAMERA_MOVED, mBorderLlistener);
		getGui().clearCollisionBoxes();
		Actor.setPlayerActor(null);
		super.onDeactivate();
	}

	/**
	 * Sets the selected bullet definition. This will make the enemies use this bullet.
	 * @param bulletId id of the bullet definition to select
	 * @return true if bullet was selected successfully, false if unsuccessful
	 */
	public boolean selectBulletDef(UUID bulletId) {
		try {
			if (bulletId != null) {
				// Load it because it is added as a dependency we would try to unload it
				// we would have
				// one reference too low.
				if (ResourceCacheFacade.isLoaded(mDef.getId())) {
					ResourceCacheFacade.load(this, bulletId, true);
					ResourceCacheFacade.finishLoading();
				}

				BulletActorDef bulletActorDef = ResourceCacheFacade.get(bulletId);
				setBulletActorDef(bulletActorDef);
			} else {
				setBulletActorDef(null);
			}

			getGui().resetValues();
			setUnsaved();
		} catch (Exception e) {
			Gdx.app.error("EnemyEditor", e.toString());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * @return selected bullet definition, null if none are selected, or if no weapon is
	 *         available
	 */
	public BulletActorDef getSelectedBulletDef() {
		BulletActorDef selectedBulletDef = null;

		if (mDef != null && mDef.getWeaponDef() != null) {
			selectedBulletDef = mDef.getWeaponDef().getBulletActorDef();
		}

		return selectedBulletDef;
	}

	@Override
	protected void onDispose() {
		mPlayerActor.dispose();
		mEnemyActor.dispose();
		mEnemyPathBackAndForth.dispose();
		mEnemyPathLoop.dispose();
		mEnemyPathOnce.dispose();

		super.onDispose();
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mDef == null) {
			return;
		}

		mPlayerActor.update(deltaTime);
		checkForDeadActors();


		switch (mDef.getMovementType()) {
		case AI:
		case STATIONARY:
			mEnemyActor.update(deltaTime);
			mEnemyActor.updateEditor();
			break;

		case PATH:
			mEnemyPathLoop.update(deltaTime);
			mEnemyPathLoop.updateEditor();
			mEnemyPathOnce.update(deltaTime);
			mEnemyPathOnce.updateEditor();
			mEnemyPathBackAndForth.update(deltaTime);
			mEnemyPathBackAndForth.updateEditor();

			// Reset Once enemy ever X seconds
			if (mfEnemyOnceReachEnd != null) {
				try {
					if ((Boolean) mfEnemyOnceReachEnd.get(mEnemyPathOnce)) {
						if (mEnemyPathOnceOutOfBoundsTime != 0.0f) {
							if (mEnemyPathOnceOutOfBoundsTime + ConfigIni.getInstance().editor.enemy.movement.getPathOnceResetTime() <= SceneSwitcher
									.getGameTime().getTotalTimeElapsed()) {
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

		checkAndResetPlayerPosition();
	}

	@Override
	protected void render() {
		enableBlendingWithDefaults();
		super.render();

		if (mDef == null) {
			return;
		}

		if (Config.Graphics.USE_RELEASE_RENDERER && !isSaving() && !isDone()) {
			ShaderProgram defaultShader = ResourceCacheFacade.get(InternalNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			mShapeRenderer.translate(0, 0, -1);


			// Enemies
			switch (getMovementType()) {
			case AI:
			case STATIONARY:
				mEnemyActor.renderEditor(mShapeRenderer);
				mEnemyActor.renderShape(mShapeRenderer);
				break;

			case PATH:
				mPathBackAndForth.renderEditor(mShapeRenderer);
				mPathLoop.renderEditor(mShapeRenderer);
				mPathOnce.renderEditor(mShapeRenderer);
				mEnemyPathBackAndForth.renderShape(mShapeRenderer);
				mEnemyPathLoop.renderShape(mShapeRenderer);
				mEnemyPathOnce.renderShape(mShapeRenderer);
				break;
			}

			mPlayerActor.renderShape(mShapeRenderer);
			mBulletDestroyer.render(mShapeRenderer, getBoundingBoxWorld());

			mShapeRenderer.translate(0, 0, 1);
			mShapeRenderer.pop();

			// Sprites
			mSpriteBatch.setProjectionMatrix(mCamera.combined);
			mSpriteBatch.begin();
			mPlayerActor.renderSprite(mSpriteBatch);
			mSpriteBatch.end();
		}
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS:
			ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
			ResourceCacheFacade.loadAllOf(this, ExternalTypes.BULLET_DEF, true);
			ResourceCacheFacade.finishLoading();
			break;

		default:
			break;
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.BULLET_DEF, true);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.PLAYER_DEF, true);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
	}

	@Override
	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
		super.reloadResourcesOnActivate(outcome, message);

		// Load newly added bullets if we have created some in the bullet editor
		if (outcome == Outcomes.NOT_APPLICAPLE) {
			ResourceCacheFacade.loadAllOf(this, ExternalTypes.BULLET_DEF, true);
			ResourceCacheFacade.finishLoading();
		}
	}

	/**
	 * Resets the player if necessary. This happens if the player gets stuck behind
	 * something.
	 */
	private void checkAndResetPlayerPosition() {
		// Skip if moving player
		if (mHitPlayer) {
			return;
		}

		// Player is outside of the screen
		if (!Geometry.isPointWithinBox(mPlayerActor.getPosition(), getWorldMinCoordinates(), getWorldMaxCoordinates())) {
			resetPlayerPosition();
		}
		// Test hit UI
		else {
			float playerRadius = mPlayerActor.getDef().getVisual().getBoundingRadius();
			mWorld.QueryAABB(mCallbackUiHit, mPlayerLastPosition.x - playerRadius, mPlayerLastPosition.y - playerRadius, mPlayerLastPosition.x
					+ playerRadius, mPlayerLastPosition.y + playerRadius);

			// Player can be stuck -> Reset player position
			if (mPlayerHitUi) {
				resetPlayerPosition();
			}
		}
	}

	// --------------------------------
	// INPUT EVENTS (not gui)
	// --------------------------------
	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// Test if touching player
		if (mPlayerPointer == INVALID_POINTER) {
			screenToWorldCoord(mCamera, x, y, mCursorWorld, true);

			mWorld.QueryAABB(mCallbackPlayerHit, mCursorWorld.x - 0.0001f, mCursorWorld.y - 0.0001f, mCursorWorld.x + 0.0001f,
					mCursorWorld.y + 0.0001f);

			if (mHitPlayer) {
				mHitPlayer = false;
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
		if (mPlayerPointer == pointer) {
			screenToWorldCoord(mCamera, x, y, mCursorWorld, true);
			mMouseJoint.setTarget(mCursorWorld);
			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (mPlayerPointer == pointer) {
			mPlayerPointer = INVALID_POINTER;
			mHitPlayer = false;

			mWorld.destroyJoint(mMouseJoint);
			mMouseJoint = null;

			return true;
		}

		return false;
	}

	@Override
	protected void saveImpl(Command command) {
		setSaving(mDef, new EnemyActor(), command, getSaveImages());
	}

	/**
	 * @return all images to save on the enemy actor
	 */
	private ImageSaveOnActor[] getSaveImages() {
		ImageSaveOnActor[] images = null;

		// Different size depending on weapon
		if (mDef.hasWeapon() && mDef.getWeaponDef().getBulletActorDef() != null) {
			images = new ImageSaveOnActor[2];
		} else {
			images = new ImageSaveOnActor[1];
		}


		// Bottom right - Movement type
		TextureRegion movementTexture = null;
		switch (mDef.getMovementType()) {
		case AI:
			movementTexture = SkinNames.getRegion(SkinNames.EditorImages.MOVEMENT_AI_SAVE);
			break;
		case PATH:
			movementTexture = SkinNames.getRegion(SkinNames.EditorImages.MOVEMENT_PATH_SAVE);
			break;
		case STATIONARY:
			movementTexture = SkinNames.getRegion(SkinNames.EditorImages.MOVEMENT_STATIONARY_SAVE);
			break;
		}

		if (movementTexture != null) {
			images[0] = new ImageSaveOnActor(movementTexture, Locations.BOTTOM_RIGHT);
		}


		// Bottom left - Bullet image
		if (mDef.hasWeapon() && mDef.getWeaponDef().getBulletActorDef() != null) {
			BulletActorDef bulletActorDef = mDef.getWeaponDef().getBulletActorDef();
			TextureRegion bulletTexture = bulletActorDef.getTextureRegionDrawable().getRegion();

			images[1] = new ImageSaveOnActor(bulletTexture, Locations.BOTTOM_LEFT);
		}

		return images;
	}

	/**
	 * Creates a new enemy
	 */
	@Override
	public void newDef() {
		EnemyActorDef def = new EnemyActorDef();
		def.getVisual().setColor((Color) SkinNames.getResource(SkinNames.EditorVars.ENEMY_COLOR_DEFAULT));
		setActorDef(def);
		getGui().resetValues();
		setMovementType(MovementTypes.PATH);
		setSaved();
	}

	/**
	 * Sets the movement speed of the enemy
	 * @param speed new movement speed of the enemy
	 */
	void setSpeed(float speed) {
		if (mDef == null) {
			return;
		}
		mDef.setSpeed(speed);
		mEnemyActor.setSpeed(speed);
		mEnemyPathBackAndForth.setSpeed(speed);
		mEnemyPathLoop.setSpeed(speed);
		mEnemyPathOnce.setSpeed(speed);
		setUnsaved();
	}

	/**
	 * @return movement speed of the enemy
	 */
	float getSpeed() {
		if (mDef != null) {
			return mDef.getSpeed();
		} else {
			return 0;
		}
	}

	/**
	 * Sets the turning of the enemy
	 * @param enabled true if enemy turning is enabled
	 */
	void setTurning(boolean enabled) {
		if (mDef == null) {
			return;
		}
		mDef.setTurn(enabled);
		mEnemyActor.resetPathMovement();
		mEnemyPathBackAndForth.resetPathMovement();
		mEnemyPathLoop.resetPathMovement();
		mEnemyPathOnce.resetPathMovement();
		setUnsaved();
	}

	/**
	 * @return true if the enemy is turning
	 */
	boolean isTurning() {
		if (mDef != null) {
			return mDef.isTurning();
		} else {
			return false;
		}
	}

	/**
	 * Sets the minimum distance from the player the enemy want to be
	 * @param minDistance minimum distance from the player
	 */
	void setPlayerDistanceMin(float minDistance) {
		if (mDef == null) {
			return;
		}
		mDef.setPlayerDistanceMin(minDistance);
		setUnsaved();
	}

	/**
	 * @return minimum distance from the player the enemy wants to be
	 */
	float getPlayerDistanceMin() {
		if (mDef != null) {
			return mDef.getPlayerDistanceMin();
		} else {
			return 0;
		}
	}

	/**
	 * @return maximum distance from the player the enemy wants to be
	 */
	float getPlayerDistanceMax() {
		if (mDef != null) {
			return mDef.getPlayerDistanceMax();
		} else {
			return 0;
		}
	}

	/**
	 * Sets the maximum distance from the player the enemy want to be
	 * @param maxDistance maximum distance from the player
	 */
	void setPlayerDistanceMax(float maxDistance) {
		if (mDef == null) {
			return;
		}
		mDef.setPlayerDistanceMax(maxDistance);
		setUnsaved();
	}

	/**
	 * @return movement type of the enemy
	 */
	MovementTypes getMovementType() {
		if (mDef != null) {
			return mDef.getMovementType();
		} else {
			return MovementTypes.PATH;
		}
	}

	/**
	 * Sets the movement type for the enemy
	 * @param movementType new movement type
	 */
	void setMovementType(MovementTypes movementType) {
		if (mDef == null) {
			return;
		}
		mDef.setMovementType(movementType);

		switch (movementType) {
		case PATH:
			createPathBodies();
			resetPlayerPosition();
			mEnemyActor.destroyBody();
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

		setUnsaved();
	}

	/**
	 * Sets the turning speed of the enemy
	 * @param turnSpeed how fast the enemy shall turn
	 */
	void setTurnSpeed(float turnSpeed) {
		if (mDef == null) {
			return;
		}
		mDef.setTurnSpeed(turnSpeed);
		setUnsaved();
	}

	/**
	 * @return turning speed of the enemy
	 */
	float getTurnSpeed() {
		if (mDef != null) {
			return mDef.getTurnSpeed();
		} else {
			return 0;
		}
	}

	/**
	 * Sets if the enemy shall move randomly using the random spread set through
	 * #setRandomSpread(float).
	 * @param moveRandomly true if the enemy shall move randomly.
	 */
	void setMoveRandomly(boolean moveRandomly) {
		if (mDef == null) {
			return;
		}
		mDef.setMoveRandomly(moveRandomly);
		setUnsaved();
	}

	/**
	 * @return true if the enemy shall move randomly.
	 * @see #setRandomTimeMin(float) to set how random the enemy shall move
	 */
	boolean isMovingRandomly() {
		if (mDef != null) {
			return mDef.isMovingRandomly();
		} else {
			return false;
		}
	}

	/**
	 * Sets the minimum time that must have passed until the enemy will decide on another
	 * direction.
	 * @param minTime how many degrees it will can move
	 * @see #setMoveRandomly(boolean) to activate/deactivate the random movement
	 */
	void setRandomTimeMin(float minTime) {
		if (mDef == null) {
			return;
		}
		mDef.setRandomTimeMin(minTime);
		setUnsaved();
	}

	/**
	 * @return Minimum time until next random move
	 */
	float getRandomTimeMin() {
		if (mDef != null) {
			return mDef.getRandomTimeMin();
		} else {
			return 0;
		}
	}

	/**
	 * Sets the maximum time that must have passed until the enemy will decide on another
	 * direction.
	 * @param maxTime how many degrees it will can move
	 * @see #setMoveRandomly(boolean) to activate/deactivate the random movement
	 */
	void setRandomTimeMax(float maxTime) {
		if (mDef == null) {
			return;
		}
		mDef.setRandomTimeMax(maxTime);
		setUnsaved();
	}

	/**
	 * @return Maximum time until next random move
	 */
	float getRandomTimeMax() {
		if (mDef != null) {
			return mDef.getRandomTimeMax();
		} else {
			return 0;
		}
	}

	/**
	 * @return the bullet actor definition of this enemy, null if it doesn't shoot.
	 */
	BulletActorDef getBulletActorDef() {
		if (mDef != null) {
			return mDef.getWeaponDef().getBulletActorDef();
		} else {
			return null;
		}
	}

	/**
	 * Sets if the enemy shall use weapons or not
	 * @param useWeapon true if the enemy shall use weapons
	 */
	void setUseWeapon(boolean useWeapon) {
		if (mDef == null) {
			return;
		}
		mDef.setUseWeapon(useWeapon);

		// Remove weapon def
		BulletActorDef bulletDef = mDef.getWeaponDef().getBulletActorDef();
		if (!useWeapon && bulletDef != null) {
			mInvoker.execute(new CEnemyBulletDefSelect(bulletDef.getId(), this));
		}

		setUnsaved();
	}

	/**
	 * @return true if the enemy shall use weapons
	 */
	boolean hasWeapon() {
		if (mDef != null) {
			return mDef.hasWeapon();
		} else {
			return false;
		}
	}

	/**
	 * Sets the bullet speed
	 * @param speed new bullet speed
	 */
	void setBulletSpeed(float speed) {
		if (mDef == null) {
			return;
		}
		mDef.getWeaponDef().setBulletSpeed(speed);
		setUnsaved();
	}

	/**
	 * @return the bullet speed
	 */
	float getBulletSpeed() {
		if (mDef != null) {
			return mDef.getWeaponDef().getBulletSpeed();
		} else {
			return 0;
		}
	}

	/**
	 * Set the bullet speed to be relative to the level speed
	 * @param relativeToLevelSpeed
	 */
	void setBulletSpeedRelativeToLevelSpeed(boolean relativeToLevelSpeed) {
		if (mDef != null) {
			mDef.getWeaponDef().setSpeedRelativeToLevel(relativeToLevelSpeed);
			setUnsaved();
		}
	}

	/**
	 * @return true if the bullet speed is relative to the level speed
	 */
	boolean isBulletSpeedRelativeToLevelSpeed() {
		if (mDef != null) {
			return mDef.getWeaponDef().isSpeedRelativeToLevelSpeed();
		} else {
			return true;
		}
	}

	/**
	 * Sets the weapon damage
	 * @param damage how much damage the bullets will take when they hit something
	 */
	void setWeaponDamage(float damage) {
		if (mDef == null) {
			return;
		}
		mDef.getWeaponDef().setDamage(damage);
		setUnsaved();
	}

	/**
	 * @return weapon damage
	 */
	float getWeaponDamage() {
		if (mDef != null) {
			return mDef.getWeaponDef().getDamage();
		} else {
			return 0;
		}
	}

	/**
	 * Sets the minimum weapon cooldown. If this is equal to the max value set through
	 * #setCooldownMax(float) it will always have the same cooldown; if not it will get a
	 * random cooldown between min and max time.
	 * @param minCooldown minimum cooldown.
	 */
	void setCooldownMin(float minCooldown) {
		if (mDef == null) {
			return;
		}
		mDef.getWeaponDef().setCooldownMin(minCooldown);
		setUnsaved();
	}

	/**
	 * @return minimum cooldown time
	 */
	float getCooldownMin() {
		if (mDef != null) {
			return mDef.getWeaponDef().getCooldownMin();
		} else {
			return 0;
		}
	}

	/**
	 * Sets the maximum weapon cooldown. If this is equal to the min value set through
	 * #setCooldownMin(float) it will always have the same cooldown; if not it will get a
	 * random cooldown between min and max time.
	 * @param maxCooldown maximum cooldown.
	 */
	void setCooldownMax(float maxCooldown) {
		if (mDef == null) {
			return;
		}
		mDef.getWeaponDef().setCooldownMax(maxCooldown);
		setUnsaved();
	}

	/**
	 * @return minimum cooldown time
	 */
	float getCooldownMax() {
		if (mDef != null) {
			return mDef.getWeaponDef().getCooldownMax();
		} else {
			return 0;
		}
	}

	/**
	 * Sets the aim type of the enemy
	 * @param aimType new aim type
	 */
	void setAimType(AimTypes aimType) {
		if (mDef == null) {
			return;
		}
		mDef.setAimType(aimType);
		setUnsaved();
	}

	/**
	 * @return the aim type of the enemy
	 */
	AimTypes getAimType() {
		if (mDef != null) {
			return mDef.getAimType();
		} else {
			return AimTypes.MOVE_DIRECTION;
		}
	}

	/**
	 * Sets the starting aim angle, when rotating
	 * @param angle starting angle of aim.
	 */
	void setAimStartAngle(float angle) {
		if (mDef == null) {
			return;
		}
		mDef.setAimStartAngle(angle);
		mEnemyActor.resetWeapon();
		mEnemyPathBackAndForth.resetWeapon();
		mEnemyPathOnce.resetWeapon();
		mEnemyPathLoop.resetWeapon();
		setUnsaved();
	}

	/**
	 * @return starting aim angle.
	 */
	float getAimStartAngle() {
		if (mDef != null) {
			return mDef.getAimStartAngle();
		} else {
			return 0;
		}
	}

	/**
	 * Sets the aim's rotation speed. Only applicable when aim is set to rotating.
	 * @param rotateSpeed new rotation speed
	 */
	void setAimRotateSpeed(float rotateSpeed) {
		if (mDef == null) {
			return;
		}
		mDef.setAimRotateSpeed(rotateSpeed);
		setUnsaved();
	}

	/**
	 * @return aim's rotation speed.
	 */
	float getAimRotateSpeed() {
		if (mDef != null) {
			return mDef.getAimRotateSpeed();
		} else {
			return 0;
		}
	}

	/**
	 * Switches scene to select a bullet type for the weapon
	 */
	void selectBulletType() {
		if (mDef == null) {
			return;
		}

		SceneSwitcher.switchTo(ExploreFactory.create(BulletActorDef.class, ExploreActions.SELECT));
	}

	/**
	 * @return true if the enemy weapon has a bullet type
	 */
	boolean isWeaponBulletsSelected() {
		return mDef != null && mDef.getWeaponDef() != null && mDef.getWeaponDef().getBulletActorDef() != null;
	}


	@Override
	public void setDrawOnlyOutline(boolean drawOnlyOutline) {
		mEnemyActor.setDrawOnlyOutline(drawOnlyOutline);
		mEnemyPathBackAndForth.setDrawOnlyOutline(drawOnlyOutline);
		mEnemyPathLoop.setDrawOnlyOutline(drawOnlyOutline);
		mEnemyPathOnce.setDrawOnlyOutline(drawOnlyOutline);
	}

	@Override
	public boolean isDrawOnlyOutline() {
		return mEnemyActor.isDrawOnlyOutline();
	}

	/**
	 * @return all three path positions
	 */
	Vector2[] getPathPositions() {
		Vector2[] positions = Collections.fillNew(new Vector2[3], Vector2.class);

		// X position
		float availableWidth = Gdx.graphics.getWidth() - UiFactory.getInstance().getStyles().vars.rightPanelWidth;
		for (Vector2 position : positions) {
			position.x = (int) (availableWidth / 2);
		}

		float barUpperLowerHeight = UiFactory.getInstance().getStyles().vars.barUpperLowerHeight;
		float availableHeight = Gdx.graphics.getHeight() - barUpperLowerHeight * 2;
		int padding = (int) (availableHeight / 3.0f);
		int yStartOffset = (int) (barUpperLowerHeight + padding / 2);

		for (int i = 0; i < positions.length; ++i) {
			positions[i].y = yStartOffset + i * padding;
		}

		return positions;
	}

	/**
	 * Creates the example paths that are used
	 */
	private void createExamplePaths() {
		// All paths should be like each other, so the player clearly sees the
		// difference between how they work
		ArrayList<Path> paths = new ArrayList<>();


		// -- Create enemies and set correct path movements

		// BACK AND FORTH
		mPathBackAndForth.setPathType(PathTypes.BACK_AND_FORTH);
		paths.add(mPathBackAndForth);
		mEnemyPathBackAndForth.setPath(mPathBackAndForth);
		mEnemyPathBackAndForth.resetPathMovement();


		// LOOP
		mPathLoop.setPathType(PathTypes.LOOP);
		paths.add(mPathLoop);
		mEnemyPathLoop.setPath(mPathLoop);
		mEnemyPathLoop.resetPathMovement();


		// ONCE
		mPathOnce.setPathType(PathTypes.ONCE);
		paths.add(mPathOnce);
		mEnemyPathOnce.setPath(mPathOnce);
		mEnemyPathOnce.resetPathMovement();


		// Create paths corners
		Vector2[] nodes = Collections.fillNew(new Vector2[4], Vector2.class);
		Vector2[] screenPos = Collections.fillNew(new Vector2[4], Vector2.class);
		Vector2[] centerPositions = getPathPositions();
		Vector2 minPos = new Vector2();
		Vector2 maxPos = new Vector2();

		// X-area: From middle of screen to 1/6 of the screen width
		// Y-area: Height of each path should be 1/5. Offset it with 1/20 so it doesn't
		// touch the borders
		assert (centerPositions.length == paths.size());

		float xOffset = Gdx.graphics.getWidth() / 6f;
		float yOffset = Gdx.graphics.getHeight() * 0.1f;

		for (int i = 0; i < paths.size(); ++i) {
			Path path = paths.get(i);
			Vector2 centerPosition = centerPositions[i];

			minPos.set(centerPosition);
			minPos.add(-xOffset, yOffset);
			maxPos.set(centerPosition);
			maxPos.add(xOffset, -yOffset);

			// Create a shape like this:
			// @formatter:off
			// 2 ------- 1
			// 3 |     | 0
			// @formatter:on

			// 0 - Bottom right
			screenPos[0].set(maxPos.x, minPos.y);
			// 1 - Top right
			screenPos[1].set(maxPos.x, maxPos.y);
			// 2 - Top left
			screenPos[2].set(minPos.x, maxPos.y);
			// 3 - Bottom left
			screenPos[3].set(minPos.x, minPos.y);

			screenToWorldCoord(mCamera, screenPos, nodes, true);
			path.addCorners(nodes);
		}
	}

	@Override
	protected void setActorDef(ActorDef def) {
		super.setActorDef(def);

		mDef = (EnemyActorDef) def;
		mEnemyActor.setDef(mDef);
		mEnemyPathOnce.setDef(mDef);
		mEnemyPathLoop.setDef(mDef);
		mEnemyPathBackAndForth.setDef(mDef);

		if (getGui().isInitialized()) {
			getGui().resetValues();
		}
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
	}

	/**
	 * Resets the player position
	 */
	private void resetPlayerPosition() {
		// Vector2 playerPosition = new Vector2();
		// Scene.screenToWorldCoord(mCamera, Gdx.graphics.getWidth() * 0.1f,
		// Gdx.graphics.getHeight() * 0.5f, playerPosition, true);
		// mPlayerActor.setPosition(playerPosition);
		// mPlayerActor.getBody().setLinearVelocity(0, 0);
	}

	/**
	 * Creates the enemy actor and resets its position
	 */
	private void createEnemyActor() {
		mEnemyActor.setPosition(new Vector2());
		mEnemyActor.createBody();
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
		setUnsaved();
	}

	/**
	 * Checks for dead enemy actors and recreates them.
	 */
	private void checkForDeadActors() {
		switch (getMovementType()) {
		case STATIONARY:
		case AI:
			if (!mEnemyActor.isActive()) {
				mEnemyActor.createBody();
				mEnemyActor.activate();
				mEnemyActor.setPosition(Vector2.Zero);
			}
			break;


		case PATH:
			checkForDeadPathActor(mEnemyPathBackAndForth);
			checkForDeadPathActor(mEnemyPathLoop);
			checkForDeadPathActor(mEnemyPathOnce);
			break;
		}


	}

	/**
	 * Checks for dead enemy path actor and recreates it if it is dead
	 * @param enemyActor the enemy actor to check if it's dead
	 */
	private void checkForDeadPathActor(EnemyActor enemyActor) {
		if (!enemyActor.isActive()) {
			enemyActor.resetPathMovement();
			enemyActor.createBody();
			enemyActor.activate();
		}
	}

	@Override
	public void undoJustCreated() {
		setActorDef(null);
	}

	@Override
	protected EnemyEditorGui getGui() {
		return (EnemyEditorGui) super.getGui();
	}

	private IEventListener mBorderLlistener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			createBorder();
			getGui().resetCollisionBoxes();
		}
	};

	/** Current enemy actor */
	private EnemyActor mEnemyActor = new EnemyActor();
	/** Enemy actor for path once */
	private EnemyActor mEnemyPathOnce = new EnemyActor();
	/** Enemy actor for path loop */
	private EnemyActor mEnemyPathLoop = new EnemyActor();
	/** Enemy actor for path back and forth */
	private EnemyActor mEnemyPathBackAndForth = new EnemyActor();
	/** Current enemy actor definition */
	private EnemyActorDef mDef = null;
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
	/** Listens for collisions */
	private CollisionResolver mCollisionResolver = new CollisionResolver();

	/** Invalid pointer id */
	private static final int INVALID_POINTER = -1;
	/** Current pointer that moves the player */
	private int mPlayerPointer = INVALID_POINTER;
	/** If we're currently moving the player */
	private boolean mHitPlayer = false;
	/** Mouse joint definition */
	private MouseJointDef mMouseJointDef = new MouseJointDef();
	/** Mouse joint for player */
	private MouseJoint mMouseJoint = null;
	/** Callback for "ray testing" if hit player */
	private QueryCallback mCallbackPlayerHit = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.testPoint(mCursorWorld.x, mCursorWorld.y)) {
				if (fixture.getBody().getUserData() instanceof PlayerActor) {
					mHitPlayer = true;
					return false;
				}
			}
			return true;
		}
	};
	/** World coordinate for the cursor */
	private Vector2 mCursorWorld = new Vector2();
	/** Callback for "ray testing" hitting a UI element */
	private QueryCallback mCallbackUiHit = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			mPlayerHitUi = false;
			if (fixture.getFilterData().categoryBits == ActorFilterCategories.SCREEN_BORDER) {
				mPlayerHitUi = true;
				return false;
			}
			return true;
		}
	};
	/** Player hit UI element */
	private boolean mPlayerHitUi = false;
	/** Player last position */
	private Vector2 mPlayerLastPosition = new Vector2();

}
