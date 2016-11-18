package com.spiddekauga.voider.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Ship;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Editor for creating ships
 */
public class ShipEditor extends ActorEditor {
private static final int INVALID_POINTER = -1;
private PlayerActor mActor = new PlayerActor();
private PlayerActorDef mDef = null;
private MouseJointDef mMouseJointDef = new MouseJointDef();
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

/**
 * Default constructor
 */
public ShipEditor() {
	super(new ShipEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR, PlayerActorDef.class, PlayerActor.class);

	getGui().setShipEditor(this);
}

@Override
protected void onCreate() {
	super.onCreate();

	createBorder();

	mMouseJointDef.bodyA = mWorld.createBody(new BodyDef());
}

@Override
protected void update(float deltaTime) {
	super.update(deltaTime);

	mActor.updateEditor();
	mActor.update(deltaTime);
}

@Override
protected void onResume(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onResume(outcome, message, loadingOutcome);

	if (outcome == Outcomes.EXPLORE_LOAD) {
		if (message instanceof DefEntity) {
			DefEntity defEntity = (DefEntity) message;

			if (!ResourceCacheFacade.isLoaded(defEntity.resourceId, defEntity.revision)) {
				ResourceCacheFacade.load(this, defEntity.resourceId, true, defEntity.revision);
				ResourceCacheFacade.finishLoading();
			}

			PlayerActorDef playerDef = ResourceCacheFacade.get(defEntity.resourceId, defEntity.revision);
			setActorDef(playerDef);
			getGui().resetValues();
			setSaved();
			mInvoker.dispose();
		}
	}
}

@Override
protected void render() {
	enableBlendingWithDefaults();
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

@Override
protected void setActorDef(ActorDef def) {
	super.setActorDef(def);

	mDef = (PlayerActorDef) def;

	if (def != null) {
		mActor.setDef(def);

		if (mActor.getBody() == null) {
			mActor.createBody();
		}

		updateMouseJointDef();
	} else {
		mActor.destroyBody();
	}

	if (getGui().isInitialized()) {
		getGui().resetValues();
	}
}

@Override
protected void saveToFile() {
	int oldRevision = mDef.getRevision();

	mResourceRepo.save(this, mDef);
	mNotification.show(NotificationTypes.SUCCESS, Messages.Info.SAVED);
	showSyncMessage();

	// Saved first time? Then load it and use the loaded gameVersion
	if (!ResourceCacheFacade.isLoaded(mDef.getId())) {
		ResourceCacheFacade.load(this, mDef.getId(), true);
		ResourceCacheFacade.finishLoading();

		setActorDef((PlayerActorDef) ResourceCacheFacade.get(mDef.getId()));
	}

	// Update latest loaded resource
	if (oldRevision != mDef.getRevision() - 1) {
		ResourceCacheFacade.setLatestResource(mDef, oldRevision);
	}

	setSaved();
}

@Override
protected ShipEditorGui getGui() {
	return (ShipEditorGui) super.getGui();
}

/**
 * Update mouse joint definition
 */
private void updateMouseJointDef() {
	if (mDef != null) {
		mMouseJointDef.maxForce = mDef.getMouseJointForceMax();
	}

	mMouseJointDef.bodyB = mActor.getBody();
}@Override
public void setDrawOnlyOutline(boolean drawOnlyOutline) {
	if (mActor != null) {
		mActor.setDrawOnlyOutline(drawOnlyOutline);
	}
}

@Override
protected void loadResources() {
	super.loadResources();
	ResourceCacheFacade.loadAllOf(this, ExternalTypes.PLAYER_DEF, true);
}@Override
public boolean isDrawOnlyOutline() {
	if (mActor != null) {
		return mActor.isDrawOnlyOutline();
	}

	return false;
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
 * @return frequency of the mouse joint
 */
float getFrequency() {
	return mMouseJointDef.frequencyHz;
}

/**
 * Sets the frequency of the mouse joint
 * @param frequency
 */
void setFrequency(float frequency) {
	mMouseJointDef.frequencyHz = frequency;
}

/**
 * @return dampening ratio of the mouse joint in the interval of [0,1]
 */
float getDampening() {
	return mMouseJointDef.dampingRatio;
}

/**
 * Sets the dampening of the mouse joint
 * @param dampening should be in the interval [0,1]
 */
void setDampening(float dampening) {
	mMouseJointDef.dampingRatio = dampening;
}

/**
 * @return density of the ship
 */
float getDensity() {
	if (mDef != null) {
		return mDef.getShape().getDensity();
	}

	return 0;
}

/**
 * Sets the density of the ship
 * @param density
 */
void setDensity(float density) {
	if (mDef != null) {
		mDef.getShape().setDensity(density);
	}
}

/**
 * @return friction of the ship
 */
float getFriction() {
	if (mDef != null) {
		return mDef.getShape().getFriction();
	}

	return 0;
}

/**
 * Set the friction of the ship
 * @param friction
 */
void setFriction(float friction) {
	if (mDef != null) {
		mDef.getShape().setFriction(friction);
	}
}

/**
 * @return elasticity of the ship
 */
float getElasticity() {
	if (mDef != null) {
		return mDef.getShape().getElasticity();
	}

	return 0;
}

/**
 * Set the elasticity of the ship
 * @param elasticity
 */
void setElasticity(float elasticity) {
	if (mDef != null) {
		mDef.getShape().setElasticity(elasticity);
	}
}

@Override
public void newDef() {
	PlayerActorDef newDef = new PlayerActorDef();
	newDef.getShape().setColor((Color) SkinNames.getResource(SkinNames.EditorVars.PLAYER_COLOR_DEFAULT));
	setActorDef(newDef);
	getGui().resetValues();
	setSaved();
	mInvoker.dispose();
}

@Override
public void undoJustCreated() {
	setActorDef(null);
}

@Override
public void handleEvent(GameEvent event) {
	// Does nothing
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
public boolean touchUp(int x, int y, int pointer, int button) {
	if (mPlayerPointer == pointer) {
		mPlayerPointer = INVALID_POINTER;

		mWorld.destroyJoint(mMouseJoint);
		mMouseJoint = null;

		return true;
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



}
