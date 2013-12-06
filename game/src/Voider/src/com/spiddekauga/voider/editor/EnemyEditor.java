package com.spiddekauga.voider.editor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.editor.brushes.VectorBrush;
import com.spiddekauga.voider.editor.commands.CEnemyBulletDefSelect;
import com.spiddekauga.voider.editor.tools.AddMoveCornerTool;
import com.spiddekauga.voider.editor.tools.DeleteTool;
import com.spiddekauga.voider.editor.tools.DrawAppendTool;
import com.spiddekauga.voider.editor.tools.DrawEraseTool;
import com.spiddekauga.voider.editor.tools.MoveTool;
import com.spiddekauga.voider.editor.tools.RemoveCornerTool;
import com.spiddekauga.voider.editor.tools.Selection;
import com.spiddekauga.voider.editor.tools.SetCenterTool;
import com.spiddekauga.voider.editor.tools.TouchTool;
import com.spiddekauga.voider.game.CollisionResolver;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * Editor for creating and editing enemies
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyEditor extends Editor implements IActorEditor, IResourceChangeEditor {
	/**
	 * Creates the enemy editor
	 */
	public EnemyEditor() {
		super(new EnemyEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR);

		mTools[Tools.MOVE.ordinal()] = new MoveTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.DELETE.ordinal()] = new DeleteTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.DRAW_APPEND.ordinal()] = new DrawAppendTool(mCamera, mWorld, mInvoker, mSelection, this, EnemyActor.class);
		mTools[Tools.DRAW_ERASE.ordinal()] = new DrawEraseTool(mCamera, mWorld, mInvoker, mSelection, this, EnemyActor.class);
		mTools[Tools.ADD_MOVE_CORNER.ordinal()] = new AddMoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.REMOVE_CORNER.ordinal()] = new RemoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.SET_CENTER.ordinal()] = new SetCenterTool(mCamera, mWorld, mInvoker, mSelection, this, EnemyActor.class);

		mPlayerActor = new PlayerActor();
		mPlayerActor.createBody();
		resetPlayerPosition();
		createExamplePaths();

		mWorld.setContactListener(mCollisionResolver);

		((EnemyEditorGui)mGui).setEnemyEditor(this);

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
	protected void onDeactivate() {
		((EnemyEditorGui)mGui).clearCollisionBoxes();
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);
		Actor.setLevel(null);
		Actor.setPlayerActor(mPlayerActor);

		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			// Does nothing
		}
		else if (outcome == Outcomes.DEF_SELECTED) {
			if (message instanceof ResourceItem) {
				switch (mSelectionAction) {
				case BULLET_TYPE:
					mInvoker.execute(new CEnemyBulletDefSelect(((ResourceItem) message).id, this));
					break;

				case LOAD_ENEMY:
					setEnemyDef((EnemyActorDef) ResourceCacheFacade.get(this, ((ResourceItem) message).id, ((ResourceItem) message).revision));
					setMovementType(mDef.getMovementType());
					mGui.resetValues();
					setSaved();
					break;
				}


			} else {
				Gdx.app.error("MainMenu", "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
			}

			mSelectionAction = null;
		}
		else if (outcome == Outcomes.NOT_APPLICAPLE) {
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
			BulletActorDef oldBulletDef = mDef.getWeaponDef().getBulletActorDef();

			if (bulletId != null) {
				// Load it because it is added as a dependency we would try to unload it we would have
				// one reference too low.
				if (ResourceCacheFacade.isLoaded(this, mDef.getId())) {
					ResourceCacheFacade.load(this, bulletId, true);
					ResourceCacheFacade.finishLoading();
				}

				BulletActorDef bulletActorDef = ResourceCacheFacade.get(this, bulletId);
				setBulletActorDef(bulletActorDef);
			} else {
				setBulletActorDef(null);
			}

			// We need to unload it because it has been loaded as a dependency to this enemy, but now
			// we remove the dependency. I.e. one loaded reference would be left.
			if (oldBulletDef != null && ResourceCacheFacade.isLoaded(this, mDef.getId())) {
				ResourceCacheFacade.unload(this, oldBulletDef, true);
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
	 * @return selected bullet definition, null if none are selected, or if no weapon is available
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
		((EnemyEditorGui)mGui).scalePathLabels();
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
	public boolean isDrawing() {
		if (mTools[mActiveTool.ordinal()] != null) {
			return mTools[mActiveTool.ordinal()].isDrawing();
		} else {
			return false;
		}
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mDef == null) {
			((EditorGui)mGui).showFirstTimeMenu();
			return;
		}

		// Force the player to set a name
		if (mDef.getName().equals(Config.Actor.NAME_DEFAULT)) {
			((ActorGui)mGui).showInfoDialog();
			mGui.showErrorMessage("Please enter an enemy name");
		}

		mPlayerActor.update(deltaTime);
		checkForDeadActors();

		if (mDrawingEnemy != null && mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			mDrawingEnemy.updateEditor();
		}

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

		if (shallAutoSave()) {
			saveDef();
			mGui.showErrorMessage(Messages.Info.SAVING);
		}

		checkAndResetPlayerPosition();

		((EnemyEditorGui)mGui).resetCollisionBoxes();
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
			ShaderProgram defaultShader = ResourceCacheFacade.get(ResourceNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			mShapeRenderer.translate(0, 0, -1);


			// Drawing enemy
			if (mDrawingEnemy != null && mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
				mDrawingEnemy.render(mShapeRenderer);
				mDrawingEnemy.renderEditor(mShapeRenderer);
			}

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

			if (mVectorBrush != null) {
				mVectorBrush.renderEditor(mShapeRenderer);
			}

			mShapeRenderer.translate(0, 0, 1);
			mShapeRenderer.pop();
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.loadAllOf(this, EnemyActorDef.class, true);
		ResourceCacheFacade.loadAllOf(this, BulletActorDef.class, true);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unloadAllOf(this, EnemyActorDef.class, true);
		ResourceCacheFacade.unloadAllOf(this, BulletActorDef.class, true);
	}

	@Override
	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
		super.reloadResourcesOnActivate(outcome, message);

		// Unload bullets if there was a chance we have edited a bullet in
		// the bullet editor.
		if (outcome == Outcomes.NOT_APPLICAPLE) {
			ResourceCacheFacade.unloadAllOf(this, BulletActorDef.class, true);
			ResourceCacheFacade.loadAllOf(this, BulletActorDef.class, true, getBulletRevisions());
			ResourceCacheFacade.finishLoading();

			// Reset bullet definition
			if (mDef != null && hasWeapon()) {
				BulletActorDef unloadedBulletActorDef = getBulletActorDef();
				if (unloadedBulletActorDef != null) {
					BulletActorDef correctBulletActorDef = ResourceCacheFacade.get(this, unloadedBulletActorDef.getId(), unloadedBulletActorDef.getRevision());
					setBulletActorDef(correctBulletActorDef);
				}
			}
		}
	}

	/**
	 * @return the revision of the currently used bullet if any.
	 */
	private Map<UUID, Integer> getBulletRevisions() {
		Map<UUID, Integer> bulletRevisions = new HashMap<UUID, Integer>();

		if (mDef.hasWeapon()) {
			BulletActorDef bulletActorDef = mDef.getWeaponDef().getBulletActorDef();
			if (bulletActorDef != null) {
				bulletRevisions.put(bulletActorDef.getId(), bulletActorDef.getRevision());
			}
		}

		return bulletRevisions;
	}

	/**
	 * Resets the player if necessary. This happens if the player gets stuck
	 * behind something.
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
		mWorld.QueryAABB(mCallbackUiHit, mPlayerLastPosition.x - playerRadius, mPlayerLastPosition.y - playerRadius, mPlayerLastPosition.x + playerRadius, mPlayerLastPosition.y + playerRadius);

		// Player can be stuck -> Reset player position
		if (mPlayerHitUi) {
			resetPlayerPosition();
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

			mWorld.QueryAABB(mCallbackPlayerHit, mCursorWorld.x - 0.0001f, mCursorWorld.y - 0.0001f, mCursorWorld.x + 0.0001f, mCursorWorld.y + 0.0001f);

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
		setSaving(mDef, new EnemyActor());
	}

	@Override
	public void saveDef(Command command) {
		setSaving(mDef, new EnemyActor(), command);
	}

	@Override
	protected void saveToFile() {
		ResourceSaver.save(mDef);

		// Saved first time? Then load it and use the loaded resource
		if (!ResourceCacheFacade.isLoaded(this, mDef.getId())) {
			ResourceCacheFacade.load(this, mDef.getId(), true);
			ResourceCacheFacade.finishLoading();

			setEnemyDef((EnemyActorDef) ResourceCacheFacade.get(this, mDef.getId()));
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
		saveDef();
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

	@Override
	public void setStartingAngle(float angle) {
		if (mDef == null) {
			return;
		}
		mDef.setStartAngleDeg(angle);
		setUnsaved();
	}

	@Override
	public float getStartingAngle() {
		if (mDef != null) {
			return mDef.getStartAngleDeg();
		} else {
			return 0;
		}
	}

	@Override
	public void setRotationSpeed(float rotationSpeed) {
		if (mDef == null) {
			return;
		}
		mDef.setRotationSpeedDeg(rotationSpeed);
		setUnsaved();
	}

	@Override
	public float getRotationSpeed() {
		if (mDef != null) {
			return mDef.getRotationSpeedDeg();
		} else {
			return 0;
		}
	}

	@Override
	public void switchTool(Tools tool) {
		if (mTools[mActiveTool.ordinal()] != null) {
			mTools[mActiveTool.ordinal()].deactivate();

			// Never remove delete tool
			if (mActiveTool != Tools.DELETE) {
				mInputMultiplexer.removeProcessor(mTools[mActiveTool.ordinal()]);
			}
		}

		mActiveTool = tool;

		if (mTools[mActiveTool.ordinal()] != null) {
			mTools[mActiveTool.ordinal()].activate();

			// Never add delete tool
			if (mActiveTool != Tools.DELETE) {
				mInputMultiplexer.addProcessor(mTools[mActiveTool.ordinal()]);
			}
		}
	}

	@Override
	public Tools getActiveTool() {
		return mActiveTool;
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
	 * Sets the minimum time that must have passed until the enemy will decide
	 * on another direction.
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
	 * Sets the maximum time that must have passed until the enemy will decide
	 * on another direction.
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
	 * Sets the minimum weapon cooldown. If this is equal to the max value set
	 * through #setCooldownMax(float) it will always have the same cooldown; if not
	 * it will get a random cooldown between min and max time.
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
	 * Sets the maximum weapon cooldown. If this is equal to the min value set
	 * through #setCooldownMin(float) it will always have the same cooldown; if not
	 * it will get a random cooldown between min and max time.
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
	 * Sets the aim's rotation speed. Only applicable when aim is set
	 * to rotating.
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

		Scene selectionScene = new SelectDefScene(BulletActorDef.class, false, false, false);
		SceneSwitcher.switchTo(selectionScene);
	}

	@Override
	public void setShapeType(ActorShapeTypes shapeType) {
		if (mDef == null) {
			return;
		}
		mDef.getVisualVars().setShapeType(shapeType);
	}

	@Override
	public ActorShapeTypes getShapeType() {
		if (mDef != null) {
			return mDef.getVisualVars().getShapeType();
		} else {
			return ActorShapeTypes.CIRCLE;
		}
	}

	@Override
	public void setShapeRadius(float radius) {
		if (mDef == null) {
			return;
		}
		mDef.getVisualVars().setShapeRadius(radius);
		setUnsaved();
	}

	@Override
	public float getShapeRadius() {
		if (mDef != null) {
			return mDef.getVisualVars().getShapeRadius();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeWidth(float width) {
		if (mDef == null) {
			return;
		}
		mDef.getVisualVars().setShapeWidth(width);
		setUnsaved();
	}

	@Override
	public float getShapeWidth() {
		if (mDef != null) {
			return mDef.getVisualVars().getShapeWidth();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeHeight(float height) {
		if (mDef == null) {
			return;
		}
		mDef.getVisualVars().setShapeHeight(height);
		setUnsaved();
	}

	@Override
	public float getShapeHeight() {
		if (mDef != null) {
			return mDef.getVisualVars().getShapeHeight();
		} else {
			return 0;
		}
	}

	@Override
	public void resetCenterOffset() {
		if (mDef == null) {
			return;
		}
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mDrawingEnemy != null) {
			mDrawingEnemy.destroyBody();

			diffOffset = Pools.vector2.obtain();
			diffOffset.set(mDef.getVisualVars().getCenterOffset());
		}

		mDef.getVisualVars().resetCenterOffset();

		if (mDrawingEnemy != null) {
			diffOffset.sub(mDef.getVisualVars().getCenterOffset());
			diffOffset.add(mDrawingEnemy.getPosition());
			mDrawingEnemy.setPosition(diffOffset);
			mDrawingEnemy.createBody();
			Pools.vector2.free(diffOffset);
		}

		setUnsaved();
	}

	@Override
	public void setCenterOffset(Vector2 newCenter) {
		if (mDef == null) {
			return;
		}
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mDrawingEnemy != null) {
			mDrawingEnemy.destroyBody();

			diffOffset = Pools.vector2.obtain();
			diffOffset.set(mDef.getVisualVars().getCenterOffset());
		}

		mDef.getVisualVars().setCenterOffset(newCenter);

		if (mDrawingEnemy != null) {
			diffOffset.sub(mDef.getVisualVars().getCenterOffset());
			diffOffset.add(mDrawingEnemy.getPosition());
			mDrawingEnemy.setPosition(diffOffset);
			mDrawingEnemy.createBody();
			Pools.vector2.free(diffOffset);
		}

		setUnsaved();
	}

	@Override
	public Vector2 getCenterOffset() {
		if (mDef != null) {
			return mDef.getVisualVars().getCenterOffset();
		} else {
			return new Vector2();
		}
	}

	@Override
	public void setName(String name) {
		if (mDef == null) {
			return;
		}
		mDef.setName(name);

		setUnsaved();
	}

	@Override
	public String getName() {
		if (mDef != null) {
			return mDef.getName();
		} else {
			return "";
		}
	}

	@Override
	public void setDescription(String description) {
		if (mDef == null) {
			return;
		}
		mDef.setDescription(description);

		setUnsaved();
	}

	@Override
	public String getDescription() {
		if (mDef != null) {
			return mDef.getDescription();
		} else {
			return "";
		}
	}

	/**
	 * Switches scene to load an enemy
	 */
	@Override
	public void loadDef() {
		mSelectionAction = SelectionActions.LOAD_ENEMY;

		Scene selectionScene = new SelectDefScene(EnemyActorDef.class, true, true, true);
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

	@Override
	public void onResourceAdded(IResource resource) {
		if (resource instanceof EnemyActor) {
			mDrawingEnemy = (EnemyActor) resource;

			// Set position other than center
			Vector2 worldPosition = Pools.vector2.obtain();
			screenToWorldCoord(mCamera, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 3, worldPosition, false);
			mDrawingEnemy.setPosition(worldPosition);
			Pools.vector2.free(worldPosition);

			setUnsaved();
		}
		else if (resource instanceof VectorBrush) {
			mVectorBrush = (VectorBrush) resource;
		}
	}

	@Override
	public void onResourceRemoved(IResource resource) {
		if (resource instanceof EnemyActor) {
			mDrawingEnemy = null;
			setUnsaved();
		}
		else if (resource instanceof VectorBrush) {
			mVectorBrush = null;
		}
	}

	@Override
	public void onResourceChanged(IResource resource) {
		// Does nothing
	}

	/**
	 * @return name of the bullet actor definition the enemies use, "" if they aren't
	 * using weapons or has no bullet actor definition set.
	 */
	String getBulletName() {
		if (mDef != null && mDef.getWeaponDef() != null && mDef.getWeaponDef().getBulletActorDef() != null) {
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
			screenPos[i] = Pools.vector2.obtain();
			nodes[i] = Pools.vector2.obtain();
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
			try {
				mPathBackAndForth.addCorner(nodes[i]);
			} catch (Exception e) {
				// Does nothing...
			}
		}

		// LOOP
		mPathLoop.setPathType(PathTypes.LOOP);
		mEnemyPathLoop.setPath(mPathLoop);
		mEnemyPathLoop.resetPathMovement();
		// Offset all y values so we don't get same path
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i].y += heightOffset;
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			try {
				mPathLoop.addCorner(nodes[i]);
			} catch (Exception e) {
				// Does nothing
			}
		}

		// ONCE
		mPathOnce.setPathType(PathTypes.ONCE);
		mEnemyPathOnce.setPath(mPathOnce);
		mEnemyPathOnce.resetPathMovement();
		// Offset all y values so we don't get same path
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i].y += heightOffset;
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			try {
				mPathOnce.addCorner(nodes[i]);
			} catch (Exception e) {
				// Does nothing
			}
		}

		// Free stuff
		for (int i = 0; i < nodes.length; ++i) {
			Pools.vector2.free(nodes[i]);
			Pools.vector2.free(screenPos[i]);
		}
	}

	/**
	 * Sets the definition for the enemy actors.
	 * @param def the new definition to use for the enemies
	 */
	private void setEnemyDef(EnemyActorDef def) {
		mDef = def;
		mEnemyActor.setDef(mDef);
		mEnemyPathOnce.setDef(mDef);
		mEnemyPathLoop.setDef(mDef);
		mEnemyPathBackAndForth.setDef(mDef);
		((DrawAppendTool)mTools[Tools.DRAW_APPEND.ordinal()]).setActorDef(mDef);
		if (mGui.isInitialized()) {
			mGui.resetValues();
		}


		if (mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			if (mDrawingEnemy == null) {
				mDrawingEnemy = new EnemyActor();
				mDrawingEnemy.setDef(mDef);
				mSelection.selectResource(mDrawingEnemy);
			}
		} else {
			if (mDrawingEnemy != null) {
				mSelection.deselectResource(mDrawingEnemy);
				mDrawingEnemy.dispose();
				mDrawingEnemy = null;
			}
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

	/**
	 * Which selection action we're currently using (when a SelectDefScene is active)
	 */
	enum SelectionActions {
		/** Bullet type for the weapon */
		BULLET_TYPE,
		/** Load an existing enemy */
		LOAD_ENEMY,
	}



	/** Active tool */
	private TouchTool[] mTools = new TouchTool[Tools.values().length];
	/** Current tool state */
	private Tools mActiveTool = Tools.NONE;
	/** Current selection */
	private Selection mSelection = new Selection();
	/** Current selection action, null if none */
	private SelectionActions mSelectionAction = null;
	/** Drawing enemy actor */
	private EnemyActor mDrawingEnemy = null;
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
	/** Vector brush to render when drawing custom shapes */
	private VectorBrush mVectorBrush = null;
}
