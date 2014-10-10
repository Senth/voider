package com.spiddekauga.voider.editor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Observable;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.UiFactory;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.editor.Editor.ImageSaveOnActor.Locations;
import com.spiddekauga.voider.editor.commands.CEnemyBulletDefSelect;
import com.spiddekauga.voider.game.CollisionResolver;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.menu.SelectDefScene;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.Synchronizer.SyncEvents;

/**
 * Editor for creating and editing enemies
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class EnemyEditor extends ActorEditor {
	/**
	 * Creates the enemy editor
	 */
	public EnemyEditor() {
		super(new EnemyEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR, EnemyActor.class);

		((EnemyEditorGui) mGui).setEnemyEditor(this);
	}

	@Override
	protected void onDeactivate() {
		((EnemyEditorGui) mGui).clearCollisionBoxes();
	}

	@Override
	protected void onInit() {
		super.onInit();


		mPlayerActor = new PlayerActor();
		mPlayerActor.createBody();
		resetPlayerPosition();
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
		BodyDef bodyDef = new BodyDef();
		mMouseBody = mWorld.createBody(bodyDef);
		mMouseJointDef.frequencyHz = Config.Game.MouseJoint.FREQUENCY;
		mMouseJointDef.bodyA = mMouseBody;
		mMouseJointDef.bodyB = mPlayerActor.getBody();
		mMouseJointDef.collideConnected = true;
		mMouseJointDef.maxForce = Config.Game.MouseJoint.FORCE_MAX;
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);
		Actor.setLevel(null);
		Actor.setPlayerActor(mPlayerActor);

		if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
			// Does nothing
		} else if (outcome == Outcomes.DEF_SELECTED) {
			if (message instanceof ResourceItem) {
				switch (mSelectionAction) {
				case BULLET_TYPE:
					mInvoker.execute(new CEnemyBulletDefSelect(((ResourceItem) message).id, this));
					break;

				case LOAD_ENEMY: {
					ResourceItem resourceItem = (ResourceItem) message;

					if (!ResourceCacheFacade.isLoaded(resourceItem.id, resourceItem.revision)) {
						ResourceCacheFacade.load(this, resourceItem.id, true, resourceItem.revision);
						ResourceCacheFacade.finishLoading();
					}

					setEnemyDef((EnemyActorDef) ResourceCacheFacade.get(resourceItem.id, resourceItem.revision));
					setMovementType(mDef.getMovementType());
					mGui.resetValues();
					setSaved();
					break;
				}
				}


			} else {
				Gdx.app.error("MainMenu", "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
			}

			mSelectionAction = null;
		} else if (outcome == Outcomes.NOT_APPLICAPLE) {
			mGui.hideMsgBoxes();
		}
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

			mGui.resetValues();
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
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		mGui.dispose();
		mGui.initGui();
		((EnemyEditorGui) mGui).updatePathLabelsPositions();
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

			// Reset Once enemy ever 4 seconds
			if (mfEnemyOnceReachEnd != null) {
				try {
					if ((Boolean) mfEnemyOnceReachEnd.get(mEnemyPathOnce)) {
						if (mEnemyPathOnceOutOfBoundsTime != 0.0f) {
							if (mEnemyPathOnceOutOfBoundsTime + Enemy.Movement.PATH_ONCE_RESET_TIME <= SceneSwitcher.getGameTime()
									.getTotalTimeElapsed()) {
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
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
				mEnemyActor.render(mShapeRenderer);
				break;

			case PATH:
				mPathBackAndForth.renderEditor(mShapeRenderer);
				mPathLoop.renderEditor(mShapeRenderer);
				mPathOnce.renderEditor(mShapeRenderer);
				mEnemyPathBackAndForth.render(mShapeRenderer);
				mEnemyPathLoop.render(mShapeRenderer);
				mEnemyPathOnce.render(mShapeRenderer);
				break;
			}

			mPlayerActor.render(mShapeRenderer);
			mBulletDestroyer.render(mShapeRenderer);

			mShapeRenderer.translate(0, 0, 1);
			mShapeRenderer.pop();
		}
	}

	@Override
	public void update(Observable observable, Object arg) {
		if (arg instanceof SyncEvents) {
			switch ((SyncEvents) arg) {
			case USER_RESOURCES_DOWNLOAD_SUCCESS:
				ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
				ResourceCacheFacade.loadAllOf(this, ExternalTypes.BULLET_DEF, true);
				ResourceCacheFacade.finishLoading();
				break;

			default:
				break;
			}
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.BULLET_DEF, true);
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
		if (mMovingPlayer) {
			return;
		}


		// Only test if player is still
		if (!mPlayerLastPosition.equals(mPlayerActor.getPosition())) {
			mPlayerLastPosition.set(mPlayerActor.getPosition());
			return;
		}


		// Test hit UI
		float playerRadius = mPlayerActor.getDef().getVisualVars().getBoundingRadius();
		mWorld.QueryAABB(mCallbackUiHit, mPlayerLastPosition.x - playerRadius, mPlayerLastPosition.y - playerRadius, mPlayerLastPosition.x
				+ playerRadius, mPlayerLastPosition.y + playerRadius);

		// Player can be stuck -> Reset player position
		if (mPlayerHitUi) {
			resetPlayerPosition();
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

	@Override
	public void saveDef() {
		setSaving(mDef, new EnemyActor(), getSaveImages());
	}

	@Override
	public void saveDef(Command command) {
		setSaving(mDef, new EnemyActor(), command, getSaveImages());
	}

	private ImageSaveOnActor[] getSaveImages() {
		ImageSaveOnActor[] images = null;

		// Different size depending on weapon
		if (mDef.hasWeapon()) {
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

	@Override
	protected void saveToFile() {
		int oldRevision = mDef.getRevision();

		mResourceRepo.save(mDef);
		mGui.showSuccessMessage(Messages.Info.SAVED);
		showSyncMessage();

		// Saved first time? Then load it and use the loaded resource
		if (!ResourceCacheFacade.isLoaded(mDef.getId())) {
			ResourceCacheFacade.load(this, mDef.getId(), true);
			ResourceCacheFacade.finishLoading();

			setEnemyDef((EnemyActorDef) ResourceCacheFacade.get(mDef.getId()));
		}


		// Update latest resource
		if (oldRevision != mDef.getRevision() - 1) {
			ResourceCacheFacade.setLatestResource(mDef, oldRevision);
		}

		setSaved();
	}

	/**
	 * Creates a new enemy
	 */
	@Override
	public void newDef() {
		setEnemyDef(new EnemyActorDef());
		mGui.resetValues();
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

	@Override
	public boolean hasUndo() {
		return mInvoker.canUndo();
	}

	@Override
	public void undo() {
		mInvoker.undo();
	}

	@Override
	public void redo() {
		mInvoker.redo();
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
		mSelectionAction = SelectionActions.BULLET_TYPE;

		Scene selectionScene = new SelectDefScene(ExternalTypes.BULLET_DEF, "Select", false, false, false);
		SceneSwitcher.switchTo(selectionScene);
	}


	/**
	 * Switches scene to load an enemy
	 */
	@Override
	public void loadDef() {
		mSelectionAction = SelectionActions.LOAD_ENEMY;

		Scene selectionScene = new SelectDefScene(ExternalTypes.ENEMY_DEF, "Load", true, true, true);
		SceneSwitcher.switchTo(selectionScene);
	}

	/**
	 * Duplicates the current enemy
	 */
	@Override
	public void duplicateDef() {
		setEnemyDef((EnemyActorDef) mDef.copy());
		mGui.resetValues();
		saveDef();
	}

	/**
	 * @return true if the enemy weapon has a bullet type
	 */
	boolean isWeaponBulletsSelected() {
		return mDef != null && mDef.getWeaponDef() != null && mDef.getWeaponDef().getBulletActorDef() != null;
	}

	/**
	 * Sets colliding damage of the enemy
	 * @param damage how much damage the enemy will inflict on a collision
	 */
	@Override
	public void setCollisionDamage(float damage) {
		if (mDef == null) {
			return;
		}
		mDef.setCollisionDamage(damage);
		setUnsaved();
	}

	/**
	 * @return collision damage with the enemy
	 */
	@Override
	public float getCollisionDamage() {
		if (mDef != null) {
			return mDef.getCollisionDamage();
		} else {
			return 0;
		}
	}

	/**
	 * Sets whether this actor shall be destroyed on collision
	 * @param destroyOnCollision set to true to destroy the enemy on collision
	 */
	@Override
	public void setDestroyOnCollide(boolean destroyOnCollision) {
		if (mDef == null) {
			return;
		}
		mDef.setDestroyOnCollide(destroyOnCollision);
		setUnsaved();
	}

	/**
	 * @return true if this enemy shall be destroyed on collision
	 */
	@Override
	public boolean isDestroyedOnCollide() {
		if (mDef != null) {
			return mDef.isDestroyedOnCollide();
		} else {
			return false;
		}
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
	/** Callback for "ray testing" if hit player */
	private QueryCallback mCallbackPlayerHit = new QueryCallback() {
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

	/**
	 * @return all three path positions
	 */
	Vector2[] getPathPositions() {
		Vector2[] positions = Pools.vector2.obtain(new Vector2[3]);

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

		@SuppressWarnings("unchecked")
		ArrayList<Path> paths = Pools.arrayList.obtain();


		// -- Create enemies and set correct path movements

		// BACK AND FORTH
		mPathBackAndForth.setPathType(PathTypes.BACK_AND_FORTH);
		paths.add(mPathBackAndForth);
		mEnemyPathBackAndForth.setPath(mPathBackAndForth);
		mEnemyPathBackAndForth.resetPathMovement();
		//
		// for (int i = 0; i < nodes.length; ++i) {
		// screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
		// try {
		// mPathBackAndForth.addCorner(nodes[i]);
		// } catch (Exception e) {
		// // Does nothing...
		// }
		// }

		// LOOP
		mPathLoop.setPathType(PathTypes.LOOP);
		paths.add(mPathLoop);
		mEnemyPathLoop.setPath(mPathLoop);
		mEnemyPathLoop.resetPathMovement();
		// // Offset all y values so we don't get same path
		// for (int i = 0; i < nodes.length; ++i) {
		// screenPos[i].y += heightOffset;
		// screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
		// try {
		// mPathLoop.addCorner(nodes[i]);
		// } catch (Exception e) {
		// // Does nothing
		// }
		// }

		// ONCE
		mPathOnce.setPathType(PathTypes.ONCE);
		paths.add(mPathOnce);
		mEnemyPathOnce.setPath(mPathOnce);
		mEnemyPathOnce.resetPathMovement();


		// Create paths corners
		Vector2[] nodes = Pools.vector2.obtain(new Vector2[4]);
		Vector2[] screenPos = Pools.vector2.obtain(new Vector2[4]);
		Vector2[] centerPositions = getPathPositions();
		Vector2 minPos = Pools.vector2.obtain();
		Vector2 maxPos = Pools.vector2.obtain();

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

		// // Offset all y values so we don't get same path
		// for (int i = 0; i < nodes.length; ++i) {
		// screenPos[i].y += heightOffset;
		// screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
		// try {
		// mPathOnce.addCorner(nodes[i]);
		// } catch (Exception e) {
		// // Does nothing
		// }
		// }

		// Free stuff
		Pools.vector2.freeAll(nodes, screenPos, centerPositions);
		Pools.vector2.freeAll(minPos, maxPos);
	}

	/**
	 * Sets the definition for the enemy actors.
	 * @param def the new definition to use for the enemies
	 */
	private void setEnemyDef(EnemyActorDef def) {
		setActorDef(def);

		mDef = def;
		mEnemyActor.setDef(mDef);
		mEnemyPathOnce.setDef(mDef);
		mEnemyPathLoop.setDef(mDef);
		mEnemyPathBackAndForth.setDef(mDef);

		if (mGui.isInitialized()) {
			mGui.resetValues();
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
		Vector2 playerPosition = Pools.vector2.obtain();
		Scene.screenToWorldCoord(mCamera, Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.5f, playerPosition, true);
		mPlayerActor.setPosition(playerPosition);
		mPlayerActor.getBody().setLinearVelocity(0, 0);
		Pools.vector2.free(playerPosition);
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
		setEnemyDef(null);
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

}
