package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual;
import com.spiddekauga.voider.editor.brushes.VectorBrush;
import com.spiddekauga.voider.editor.commands.CActorEditorCenterReset;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveAll;
import com.spiddekauga.voider.editor.tools.AddMoveCornerTool;
import com.spiddekauga.voider.editor.tools.DeleteTool;
import com.spiddekauga.voider.editor.tools.DrawAppendTool;
import com.spiddekauga.voider.editor.tools.DrawEraseTool;
import com.spiddekauga.voider.editor.tools.MoveTool;
import com.spiddekauga.voider.editor.tools.PanTool;
import com.spiddekauga.voider.editor.tools.RemoveCornerTool;
import com.spiddekauga.voider.editor.tools.Selection;
import com.spiddekauga.voider.editor.tools.SetCenterTool;
import com.spiddekauga.voider.editor.tools.TouchTool;
import com.spiddekauga.voider.editor.tools.ZoomTool;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.DrawImages;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.PublishResponse;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceNotFoundException;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Geometry.PolygonAreaTooSmallException;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Messages;

import java.lang.reflect.Constructor;

/**
 * Common class for all actor editors
 */
public abstract class ActorEditor extends Editor implements IActorEditor, IResourceChangeEditor {
private static final DrawImages SHAPE_IMAGE_DEFAULT = DrawImages.SHUTTLE_LARGE;
/** Current selection */
protected Selection mSelection = new Selection();
private boolean mShapeImageUpdate = false;
private Float mShapeImageAngleMin;
private float mShapeImageDistMin;
private Class<? extends Actor> mActorType;
private VectorBrush mVectorBrush = null;
private ActorDef mActorDef = null;@Override
public void setShapeType(ActorShapeTypes shapeType) {
	if (mActorDef == null || mActorDef.getShape().getShapeType() == shapeType) {
		return;
	}

	if (shapeType == ActorShapeTypes.IMAGE) {
		mInvoker.execute(new CResourceCornerRemoveAll(mActorDef.getShape(), this));

		if (mActorDef.getShape().getDrawName() == null) {
			mActorDef.getShape().setDrawName(SHAPE_IMAGE_DEFAULT);
		}
	}

	mActorDef.getShape().setShapeType(shapeType);

	setUnsaved();
}
private Actor mDrawingActor = null;@Override
public ActorShapeTypes getShapeType() {
	if (mActorDef != null) {
		return mActorDef.getShape().getShapeType();
	} else {
		return ActorShapeTypes.CIRCLE;
	}
}
private TouchTool[] mTools = new TouchTool[Tools.values().length];@Override
public void setShapeRadius(float radius) {
	if (mActorDef == null) {
		return;
	}
	mActorDef.getShape().setShapeRadius(radius);
	setUnsaved();
}
/** Last active after a deactivation */
private Tools mLastTool = Tools.DRAW_APPEND;@Override
public float getShapeRadius() {
	if (mActorDef != null) {
		return mActorDef.getShape().getShapeRadius();
	} else {
		return 0;
	}
}
/** Current tool state */
private Tools mActiveTool = Tools.NONE;@Override
public void setShapeWidth(float width) {
	if (mActorDef == null) {
		return;
	}
	mActorDef.getShape().setShapeWidth(width);
	setUnsaved();
}
/** Zoom tool */
private ZoomTool mZoomTool = null;@Override
public float getShapeWidth() {
	if (mActorDef != null) {
		return mActorDef.getShape().getShapeWidth();
	} else {
		return 0;
	}
}

/**
 * @param gui all UI elements
 * @param pickRadius picking radius
 * @param defType what we editor in this editor
 * @param actorType the actor type used in this editor
 */
public ActorEditor(Gui gui, float pickRadius, Class<? extends Def> defType, Class<? extends Actor> actorType) {
	super(gui, pickRadius, defType);
	mActorType = actorType;


	// Set config variables
	IC_Visual icVisual = getGui().getVisualConfig();
	mShapeImageAngleMin = icVisual.getImageAngleDefault();
	mShapeImageDistMin = icVisual.getImageDistDefault();
	mSelection.setAsSelectedOnSelection(false);
}@Override
public void setShapeHeight(float height) {
	if (mActorDef == null) {
		return;
	}
	mActorDef.getShape().setShapeHeight(height);
	setUnsaved();
}

@Override
protected ActorGui getGui() {
	return (ActorGui) super.getGui();
}@Override
public float getShapeHeight() {
	if (mActorDef != null) {
		return mActorDef.getShape().getShapeHeight();
	} else {
		return 0;
	}
}

@Override
protected void onInit() {
	super.onInit();

	IC_Actor icActor = ConfigIni.getInstance().editor.actor;

	mZoomTool = new ZoomTool(this, icActor.getZoomMin(), icActor.getZoomMax());

	mTools[Tools.ZOOM_IN.ordinal()] = mZoomTool;
	mTools[Tools.ZOOM_OUT.ordinal()] = mZoomTool;
	mTools[Tools.PAN.ordinal()] = new PanTool(this);
	mTools[Tools.MOVE.ordinal()] = new MoveTool(this, mSelection);
	mTools[Tools.DELETE.ordinal()] = new DeleteTool(this, mSelection);
	mTools[Tools.DRAW_APPEND.ordinal()] = new DrawAppendTool(this, mSelection, mActorType);
	mTools[Tools.DRAW_ERASE.ordinal()] = new DrawEraseTool(this, mSelection, mActorType);
	mTools[Tools.ADD_MOVE_CORNER.ordinal()] = new AddMoveCornerTool(this, mSelection);
	mTools[Tools.REMOVE_CORNER.ordinal()] = new RemoveCornerTool(this, mSelection);
	mTools[Tools.CENTER_SET.ordinal()] = new SetCenterTool(this, mSelection, mActorType);


	updateCameraLimits();
}@Override
public void resetCenterOffset() {
	if (mActorDef == null) {
		return;
	}
	// Save diff offset and move the actor in the opposite direction...
	Vector2 diffOffset = null;
	if (mDrawingActor != null) {
		mDrawingActor.destroyBody();

		diffOffset = new Vector2(mActorDef.getShape().getCenterOffset());
	}

	mActorDef.getShape().resetCenterOffset();

	if (mDrawingActor != null) {
		diffOffset.sub(mActorDef.getShape().getCenterOffset());
		diffOffset.add(mDrawingActor.getPosition());
		mDrawingActor.setPosition(diffOffset);
		mDrawingActor.createBody();
	}

	setUnsaved();
}

@Override
protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onActivate(outcome, message, loadingOutcome);

	if (mDrawingActor != null && mDrawingActor.hasBody()) {
		mDrawingActor.destroyBody();
		mSelection.clearSelection();
		mDrawingActor = null;
	}
}@Override
public void setCenterOffset(Vector2 newCenter) {
	if (mActorDef == null) {
		return;
	}
	// Save diff offset and move the actor in the opposite direction...
	Vector2 diffOffset = null;
	if (mDrawingActor != null) {
		mDrawingActor.destroyBody();

		diffOffset = new Vector2(mActorDef.getShape().getCenterOffset());
	}

	mActorDef.getShape().setCenterOffset(newCenter);

	if (mDrawingActor != null) {
		diffOffset.sub(mActorDef.getShape().getCenterOffset());
		diffOffset.add(mDrawingActor.getPosition());
		mDrawingActor.setPosition(diffOffset);
		mDrawingActor.createBody();
	}

	setUnsaved();
}

@Override
protected void onResize(int width, int height) {
	super.onResize(width, height);

	updateCameraLimits();
}@Override
public Vector2 getCenterOffset() {
	if (mActorDef != null) {
		return mActorDef.getShape().getCenterOffset();
	} else {
		return new Vector2();
	}
}

@Override
protected void update(float deltaTime) {
	super.update(deltaTime);

	if (mActorDef == null) {
		return;
	}

	if (mDrawingActor != null) {
		mDrawingActor.updateEditor();
	}

	((PanTool) mTools[Tools.PAN.ordinal()]).update(deltaTime);
}

@Override
protected void render() {
	super.render();

	if (mActorDef == null) {
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

		// Drawing actor
		if (mDrawingActor != null && mActorDef.getShape().getShapeType() == ActorShapeTypes.CUSTOM) {
			mDrawingActor.renderShape(mShapeRenderer);
			mDrawingActor.renderEditor(mShapeRenderer);
		}

		if (mVectorBrush != null) {
			mVectorBrush.renderEditor(mShapeRenderer);
		}

		mShapeRenderer.translate(0, 0, 1);
		mShapeRenderer.pop();
	}
}

/**
 * Update camera/world limits
 */
private void updateCameraLimits() {
	float halfWidth = mCamera.viewportWidth / 2;
	float halfHeight = mCamera.viewportHeight / 2;

	mZoomTool.setWorldMin(-halfWidth, -halfHeight);
	mZoomTool.setWorldMax(halfWidth, halfHeight);

	PanTool panTool = (PanTool) mTools[Tools.PAN.ordinal()];
	panTool.setWorldMin(-halfWidth, -halfHeight);
	panTool.setWorldMax(halfWidth, halfHeight);
}

@Override
public void onResourceAdded(IResource resource, boolean isNew) {
	if (resource instanceof Actor) {
		mDrawingActor = (Actor) resource;
		setUnsaved();
	} else if (resource instanceof VectorBrush) {
		mVectorBrush = (VectorBrush) resource;
	}
}

@Override
public void onResourceRemoved(IResource resource) {
	if (resource instanceof Actor) {
		mInvoker.execute(new CResourceCornerRemoveAll(mActorDef.getShape(), this), true);
		mDrawingActor = null;
		setUnsaved();
	} else if (resource instanceof VectorBrush) {
		mVectorBrush = null;
	}
}@Override
public void switchTool(Tools tool) {
	if (mZoomTool != null) {
		deactivateCurrentTool();
		activateTool(tool);
	}
}

@Override
protected void saveImpl(Command command) {
	setSaving(mActorDef, newActor(), command);
}/**
 * Deactivate the current tool
 */
private void deactivateCurrentTool() {
	if (mTools[mActiveTool.ordinal()] != null) {
		mTools[mActiveTool.ordinal()].deactivate();

		// Never remove delete and zoom tool
		if (mActiveTool != Tools.DELETE && mActiveTool != Tools.ZOOM_IN && mActiveTool != Tools.ZOOM_OUT) {
			mInputMultiplexer.removeProcessor(mTools[mActiveTool.ordinal()]);
		}
	}
}

@Override
protected void saveToFile() {
	int oldRevision = mActorDef.getRevision();

	mResourceRepo.save(this, mActorDef);
	mNotification.showSuccess(Messages.Info.SAVED);
	showSyncMessage();

	// Saved first time? Then load it and use the loaded gameVersion
	if (!ResourceCacheFacade.isLoaded(mActorDef.getId())) {
		ResourceCacheFacade.load(this, mActorDef.getId(), true);
		ResourceCacheFacade.finishLoading();

		setActorDef((ActorDef) ResourceCacheFacade.get(mActorDef.getId()));
	}

	// Update latest loaded resource
	if (oldRevision != mActorDef.getRevision() - 1) {
		ResourceCacheFacade.setLatestResource(mActorDef, oldRevision);
	}

	setSaved();
}@Override
public void executeTool(Tools tool) {
	if (!isInitialized()) {
		return;
	}

	switch (tool) {
	case ZOOM_IN:
		mZoomTool.zoomIn();
		break;

	case ZOOM_OUT:
		mZoomTool.zoomOut();
		break;

	case ZOOM_RESET:
		mZoomTool.resetZoom();
		break;

	case CENTER_RESET:
		mInvoker.execute(new CActorEditorCenterReset(this));
		break;

	default:
		// Does nothing
		break;
	}
}

/**
 * Sets the actor definition
 * @param actorDef the actor definition
 */
protected void setActorDef(ActorDef actorDef) {
	mActorDef = actorDef;

	if (mActorDef == null) {
		return;
	}

	((DrawAppendTool) mTools[Tools.DRAW_APPEND.ordinal()]).setActorDef(mActorDef);
	mInvoker.dispose();
}/**
 * Activation the specific tool
 * @param tool the tool that will be activated
 */
private void activateTool(Tools tool) {
	// Only switch tool if the a specific tool actually exist
	if (mTools[tool.ordinal()] != null) {
		mActiveTool = tool;

		mTools[mActiveTool.ordinal()].activate();

		// Never add delete, zoom, or PAN tool
		if (mActiveTool != Tools.DELETE && mActiveTool != Tools.ZOOM_IN && mActiveTool != Tools.ZOOM_OUT && mActiveTool != Tools.PAN) {
			mInputMultiplexer.addProcessor(mTools[mActiveTool.ordinal()]);
		}
	}

	if (tool == Tools.NONE) {
		mActiveTool = tool;
	}
}

@Override
public void duplicateDef(String name, String description) {
	if (mActorDef != null) {
		setActorDef((ActorDef) mActorDef.copyNewResource());
		mActorDef.setName(name);
		mActorDef.setDescription(description);
		setUnsaved();
		saveDef();
	}
}@Override
public Tools getActiveTool() {
	return mActiveTool;
}

@Override
public boolean isDrawing() {
	if (mTools[mActiveTool.ordinal()] != null) {
		return mTools[mActiveTool.ordinal()].isDrawing();
	} else {
		return false;
	}
}@Override
public void activateTools(Tools activateTool) {
	switchTool(activateTool);
	getGui().resetTools();

	// Add delete tool
	if (!isPublished() && !mInputMultiplexer.getProcessors().contains(mTools[Tools.DELETE.ordinal()], true)) {
		mInputMultiplexer.addProcessor(mTools[Tools.DELETE.ordinal()]);
	}

	// Add zoom tool
	if (!mInputMultiplexer.getProcessors().contains(mZoomTool, true)) {
		mInputMultiplexer.addProcessor(mZoomTool);
	}

	// Add pan tool
	if (!mInputMultiplexer.getProcessors().contains(mTools[Tools.PAN.ordinal()], true)) {
		mInputMultiplexer.addProcessor(mTools[Tools.PAN.ordinal()]);
	}

	// Create draw actor
	if (mDrawingActor == null && mActorDef.getShape().getCornerCount() > 0) {
		mDrawingActor = newActor();
		mDrawingActor.setDef(mActorDef);
		mDrawingActor.createBody();
		mSelection.selectResource(mDrawingActor);
	}
}

@Override
public void publishDef() {
	if (mActorDef != null) {
		getGui().showProgressBar("Uploading...");
		mResourceRepo.publish(this, this, mActorDef);
	}
}@Override
public void activateTools() {
	if (mActiveTool == Tools.NONE) {
		activateTools(mLastTool);
	}
}

@Override
public boolean isPublished() {
	if (mActorDef != null) {
		try {
			return ResourceLocalRepo.isPublished(mActorDef.getId());
		} catch (ResourceNotFoundException e) {
			// Do nothing
		}
	}
	return false;
}/**
 * Deactivate tools
 */
@Override
public void deactivateTools() {
	if (mActiveTool != Tools.NONE) {
		mLastTool = mActiveTool;
		switchTool(Tools.NONE);
		mInputMultiplexer.removeProcessor(mTools[Tools.DELETE.ordinal()]);
		mInputMultiplexer.removeProcessor(mTools[Tools.PAN.ordinal()]);
		mInputMultiplexer.removeProcessor(mZoomTool);
		if (mZoomTool != null) {
			mZoomTool.resetZoom();
		}

		// Remove drawing actor
		if (mDrawingActor != null) {
			mDrawingActor.destroyBody();
			mSelection.clearSelection();
			mDrawingActor = null;
		}
	}
}

@Override
public String getName() {
	if (mActorDef != null) {
		return mActorDef.getName();
	} else {
		return "";
	}
}

@Override
public void setName(String name) {
	if (mActorDef == null) {
		return;
	}
	mActorDef.setName(name);
	((EditorGui) getGui()).resetName();

	setUnsaved();
}

@Override
public String getDescription() {
	if (mActorDef != null) {
		return mActorDef.getDescription();
	} else {
		return "";
	}
}

@Override
public void setDescription(String description) {
	if (mActorDef == null) {
		return;
	}
	mActorDef.setDescription(description);

	setUnsaved();
}

@Override
public Def getDef() {
	return mActorDef;
}@Override
public void setColor(Color color) {
	if (mActorDef != null) {
		mActorDef.getShape().setColor(color);
		setUnsaved();
	}
}

@Override
public void handleWebResponseSyncronously(IMethodEntity method, IEntity response) {
	super.handleWebResponseSyncronously(method, response);

	// Publish -> Remove tools
	if (response instanceof PublishResponse) {
		if (((PublishResponse) response).status == PublishResponse.Statuses.SUCCESS) {
			switchTool(Tools.MOVE);
			mInputMultiplexer.removeProcessor(mTools[Tools.DELETE.ordinal()]);
		}
	}
}@Override
public Color getColor() {
	if (mActorDef != null) {
		return mActorDef.getShape().getColor();
	} else {
		return new Color();
	}
}

@Override
public void setStartingAngle(float angle) {
	if (mActorDef == null) {
		return;
	}
	mActorDef.setStartAngleDeg(angle);
	setUnsaved();
}

@Override
public float getStartingAngle() {
	if (mActorDef != null) {
		return mActorDef.getStartAngleDeg();
	} else {
		return 0;
	}
}

@Override
public void setRotationSpeed(float rotationSpeed) {
	if (mActorDef == null) {
		return;
	}
	mActorDef.setRotationSpeedDeg(rotationSpeed);
	setUnsaved();
}

@Override
public float getRotationSpeed() {
	if (mActorDef != null) {
		return mActorDef.getRotationSpeedDeg();
	} else {
		return 0;
	}
}







/**
 * Creates a new actor of the current actor type via the default constructor. If an actor definition
 * has been set, this will also set that definition, else you need to set this manually if it hasn't
 * been set through the actor's default constructor.
 * @return new actor of the current actor type.
 */
protected Actor newActor() {
	try {
		Constructor<?> constructor = mActorType.getConstructor();
		Actor actor = (Actor) constructor.newInstance();
		actor.setSkipRotating(true);

		if (mActorDef != null) {
			actor.setDef(mActorDef);
		}

		return actor;

	} catch (Exception e) {
		Gdx.app.error("ActorTool", e.toString());
		e.printStackTrace();
	}

	return null;
}









@Override
public void setShapeImageScale(float scale) {
	if (mActorDef != null) {
		mActorDef.getShape().setImageScale(scale);
		updateShapeImageActor();
	}
}

@Override
public float getShapeImageScale() {
	if (mActorDef != null) {
		return mActorDef.getShape().getImageScale();
	}
	return 0;
}

@Override
public void setDrawImage(DrawImages image) {
	if (mActorDef != null) {
		mActorDef.getShape().setDrawName(image);
		updateShapeImageActor();
	}
}

@Override
public DrawImages getDrawImage() {
	if (mActorDef != null) {
		return mActorDef.getShape().getDrawName();
	}

	return SHAPE_IMAGE_DEFAULT;
}

/**
 * Updates and creates the shape image actor
 */
private void updateShapeImageActor() {
	if (mShapeImageUpdate && mActorDef != null) {
		try {
			mActorDef.getShape().updateImageShape(mShapeImageDistMin, mShapeImageAngleMin, getScreenToWorldScale());
			setUnsaved();
		} catch (PolygonAreaTooSmallException | PolygonComplexException e) {
			// Do nothing
		}
	}
}

@Override
public void setShapeImageUpdateContinuously(boolean update) {
	mShapeImageUpdate = update;
	updateShapeImageActor();
}

@Override
public boolean isShapeImageUpdatedContinuously() {
	return mShapeImageUpdate;
}

@Override
public void setShapeImageDistMin(float distMin) {
	mShapeImageDistMin = distMin;
	updateShapeImageActor();
}

@Override
public float getShapeImageDistMin() {
	return mShapeImageDistMin;
}

@Override
public void setShapeImageAngleMin(float angleMin) {
	mShapeImageAngleMin = angleMin;
	updateShapeImageActor();
}

@Override
public float getShapeImageAngleMin() {
	return mShapeImageAngleMin;
}

/**
 * Sets colliding damage of the enemy
 * @param damage how much damage the enemy will inflict on a collision
 */
@Override
public void setCollisionDamage(float damage) {
	if (mActorDef != null) {
		mActorDef.setCollisionDamage(damage);
		setUnsaved();
	}
}

/**
 * @return collision damage with the enemy
 */
@Override
public float getCollisionDamage() {
	if (mActorDef != null) {
		return mActorDef.getCollisionDamage();
	}
	return 0;
}

/**
 * Sets whether this actor shall be destroyed on collision
 * @param destroyOnCollision set to true to destroy the enemy on collision
 */
@Override
public void setDestroyOnCollide(boolean destroyOnCollision) {
	if (mActorDef != null) {
		mActorDef.setDestroyOnCollide(destroyOnCollision);
		setUnsaved();
	}
}

/**
 * @return true if this enemy shall be destroyed on collision
 */
@Override
public boolean isDestroyedOnCollide() {
	if (mActorDef != null) {
		return mActorDef.isDestroyedOnCollide();
	}
	return false;
}
















}
