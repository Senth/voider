package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Ship;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.menu.SelectDefScene;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Editor for creating ships
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ShipEditor extends ActorEditor {
	/**
	 * Default constructor
	 */
	public ShipEditor() {
		super(new ShipEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR, PlayerActor.class);

		((ShipEditorGui) mGui).setShipEditor(this);
	}

	@Override
	protected void onInit() {
		super.onInit();

		createBorder();

		mMouseJointDef.bodyA = mWorld.createBody(new BodyDef());
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		if (outcome == Outcomes.DEF_SELECTED) {
			switch (mSelectionAction) {
			case LOAD_SHIP:
				if (message instanceof ResourceItem) {
					ResourceItem resourceItem = (ResourceItem) message;

					if (!ResourceCacheFacade.isLoaded(resourceItem.id, resourceItem.revision)) {
						ResourceCacheFacade.load(this, resourceItem.id, true, resourceItem.revision);
						ResourceCacheFacade.finishLoading();
					}

					PlayerActorDef playerDef = ResourceCacheFacade.get(resourceItem.id, resourceItem.revision);
					setDef(playerDef);
					mGui.resetValues();
					setSaved();
					mInvoker.dispose();
				} else {
					Gdx.app.error("BulletEditor", "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
				}
				break;
			}
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.PLAYER_DEF, true);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		mActor.updateEditor();
		mActor.update(deltaTime);
	}

	@Override
	protected void render() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		super.render();

		if (Config.Graphics.USE_RELEASE_RENDERER && !isSaving() && !isDone()) {
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mSpriteBatch.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			mActor.renderShape(mShapeRenderer);
			mActor.renderEditor(mShapeRenderer);
			mShapeRenderer.pop();

			mSpriteBatch.begin();
			mActor.renderSprite(mSpriteBatch);
			mSpriteBatch.end();
		}
	}

	/**
	 * Sets the maximum force of the mouse joint
	 * @param maxForce
	 */
	void setMaxForce(float maxForce) {
		float realForce = maxForce * 100;

		if (mDef != null) {
			mDef.setMouseJointForceMax(realForce);
		}
		mMouseJointDef.maxForce = realForce;
		setUnsaved();
	}

	/**
	 * @return maximum force of the mouse joint
	 */
	float getMaxForce() {
		if (mDef != null) {
			return mDef.getMouseJointForceMax() / 100;
		}
		return mMouseJointDef.maxForce / 100;
	}

	/**
	 * Sets the frequency of the mouse joint
	 * @param frequency
	 */
	void setFrequency(float frequency) {
		mMouseJointDef.frequencyHz = frequency;
	}

	/**
	 * @return frequency of the mouse joint
	 */
	float getFrequency() {
		return mMouseJointDef.frequencyHz;
	}

	/**
	 * Sets the dampening of the mouse joint
	 * @param dampening should be in the interval [0,1]
	 */
	void setDampening(float dampening) {
		mMouseJointDef.dampingRatio = dampening;
	}

	/**
	 * @return dampening ratio of the mouse joint in the interval of [0,1]
	 */
	float getDampening() {
		return mMouseJointDef.dampingRatio;
	}

	/**
	 * Sets the density of the ship
	 * @param density
	 */
	void setDensity(float density) {
		if (mDef != null) {
			mDef.getVisual().setDensity(density);
		}
	}

	/**
	 * @return density of the ship
	 */
	float getDensity() {
		if (mDef != null) {
			return mDef.getVisual().getDensity();
		}

		return 0;
	}

	/**
	 * Set the friction of the ship
	 * @param friction
	 */
	void setFriction(float friction) {
		if (mDef != null) {
			mDef.getVisual().setFriction(friction);
		}
	}

	/**
	 * @return friction of the ship
	 */
	float getFriction() {
		if (mDef != null) {
			return mDef.getVisual().getFriction();
		}

		return 0;
	}

	/**
	 * Set the elasticity of the ship
	 * @param elasticity
	 */
	void setElasticity(float elasticity) {
		if (mDef != null) {
			mDef.getVisual().setElasticity(elasticity);
		}
	}

	/**
	 * @return elasticity of the ship
	 */
	float getElasticity() {
		if (mDef != null) {
			return mDef.getVisual().getElasticity();
		}

		return 0;
	}

	@Override
	public void setDrawOnlyOutline(boolean drawOnlyOutline) {
		if (mActor != null) {
			mActor.setDrawOnlyOutline(drawOnlyOutline);
		}
	}

	@Override
	public boolean isDrawOnlyOutline() {
		if (mActor != null) {
			return mActor.isDrawOnlyOutline();
		}

		return false;
	}

	@Override
	public void newDef() {
		PlayerActorDef newDef = new PlayerActorDef();
		newDef.getVisual().setColor((Color) SkinNames.getResource(SkinNames.EditorVars.PLAYER_COLOR_DEFAULT));
		setDef(newDef);
		mGui.resetValues();
		setSaved();
		mInvoker.dispose();
	}

	/**
	 * Sets the current definition
	 * @param def set the current definition
	 */
	private void setDef(PlayerActorDef def) {
		setActorDef(def);
		mDef = def;

		if (def != null) {
			mActor.setDef(def);

			if (mActor.getBody() == null) {
				mActor.createBody();
			}

			updateMouseJointDef();
		} else {
			mActor.destroyBody();
		}

		if (mGui.isInitialized()) {
			mGui.resetValues();
		}
	}

	/**
	 * Update mouse joint definition
	 */
	private void updateMouseJointDef() {
		if (mDef != null) {
			mMouseJointDef.maxForce = mDef.getMouseJointForceMax();
		}

		mMouseJointDef.bodyB = mActor.getBody();
	}

	@Override
	public void saveDef() {
		setSaving(mDef, new PlayerActor());
	}

	@Override
	public void saveDef(Command command) {
		setSaving(mDef, new PlayerActor(), command);
	}

	@Override
	public void loadDef() {
		mSelectionAction = SelectionActions.LOAD_SHIP;

		Scene selectionScene = new SelectDefScene(ExternalTypes.PLAYER_DEF, "Load", false, true, true);
		SceneSwitcher.switchTo(selectionScene);
	}

	@Override
	public void duplicateDef() {
		mDef = mDef.copyNewResource();
		mGui.resetValues();
		mInvoker.dispose();
		saveDef();
	}

	@Override
	public void undoJustCreated() {
		setDef(null);
	}

	@Override
	public void handleEvent(GameEvent event) {
		// Does nothing
	}

	@Override
	protected void saveToFile() {
		int oldRevision = mDef.getRevision();

		mResourceRepo.save(this, mDef);
		mNotification.show(NotificationTypes.SUCCESS, Messages.Info.SAVED);
		showSyncMessage();

		// Saved first time? Then load it and use the loaded version
		if (!ResourceCacheFacade.isLoaded(mDef.getId())) {
			ResourceCacheFacade.load(this, mDef.getId(), true);
			ResourceCacheFacade.finishLoading();

			setDef((PlayerActorDef) ResourceCacheFacade.get(mDef.getId()));
		}

		// Update latest loaded resource
		if (oldRevision != mDef.getRevision() - 1) {
			ResourceCacheFacade.setLatestResource(mDef, oldRevision);
		}

		setSaved();
	}

	/**
	 * Enumeration for what we're currently selecting
	 */
	private enum SelectionActions {
		/** Load a ship */
		LOAD_SHIP,
	}

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

				mMouseJointDef.target.set(mActor.getBody().getPosition());
				mMouseJoint = (MouseJoint) mWorld.createJoint(mMouseJointDef);
				mMouseJoint.setTarget(mCursorWorld);
				mActor.getBody().setAwake(true);

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

			mWorld.destroyJoint(mMouseJoint);
			mMouseJoint = null;

			return true;
		}

		return false;
	}


	private PlayerActor mActor = new PlayerActor();
	private SelectionActions mSelectionAction = null;
	private PlayerActorDef mDef = null;
	private MouseJointDef mMouseJointDef = new MouseJointDef();
	private static final int INVALID_POINTER = -1;
	/** Current pointer that moves the player */
	private int mPlayerPointer = INVALID_POINTER;
	/** Only active when pressed on the player */
	private MouseJoint mMouseJoint = null;
	private Vector2 mCursorWorld = new Vector2();
	private boolean mHitPlayer = false;
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

	// Initialize default values
	{
		IC_Ship.IC_Settings icSettings = ConfigIni.getInstance().editor.ship.settings;
		mMouseJointDef.dampingRatio = icSettings.getDampeningDefault();
		mMouseJointDef.frequencyHz = icSettings.getFrequencyDefault();
		mMouseJointDef.maxForce = icSettings.getForceDefault();
	}
}
