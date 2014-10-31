package com.spiddekauga.voider.editor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
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
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.PublishMethodResponse;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceNotFoundException;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.resource.SkinNames.GeneralImages;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Geometry.PolygonAreaTooSmallException;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;

/**
 * Common class for all actor editors
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class ActorEditor extends Editor implements IActorEditor, IResourceChangeEditor {
	/**
	 * @param gui all UI elements
	 * @param pickRadius picking radius
	 * @param actorType the actor type used in this editor
	 */
	public ActorEditor(Gui gui, float pickRadius, Class<? extends Actor> actorType) {
		super(gui, pickRadius);
		mActorType = actorType;


		// Set config variables
		IC_Visual icVisual = ((ActorGui) mGui).getVisualConfig();
		mShapeImageAngleMin = icVisual.getImageAngleDefault();
		mShapeImageDistMin = icVisual.getImageDistDefault();
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
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mActorDef == null) {
			return;
		}

		if (mDrawingActor != null && mActorDef.getVisual().getShapeType() == ActorShapeTypes.CUSTOM) {
			mDrawingActor.updateEditor();
		}

		((PanTool) mTools[Tools.PAN.ordinal()]).update(deltaTime);
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);

		updateCameraLimits();
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
			if (mDrawingActor != null && mActorDef.getVisual().getShapeType() == ActorShapeTypes.CUSTOM) {
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

	@Override
	public void setShapeType(ActorShapeTypes shapeType) {
		if (mActorDef == null || mActorDef.getVisual().getShapeType() == shapeType) {
			return;
		}

		if (shapeType == ActorShapeTypes.IMAGE) {
			mInvoker.execute(new CResourceCornerRemoveAll(mActorDef.getVisual(), this));

			if (mActorDef.getVisual().getImageName() == null) {
				mActorDef.getVisual().setImageName(SHAPE_IMAGE_DEFAULT);
			}
		}

		ActorShapeTypes previousShapeType = mActorDef.getVisual().getShapeType();

		mActorDef.getVisual().setShapeType(shapeType);

		// Add tool to input multiplexer
		if (shapeType == ActorShapeTypes.CUSTOM && previousShapeType != ActorShapeTypes.CUSTOM) {
			activateTools(Tools.DRAW_APPEND);
		}
		// Remove tool from input multiplexer
		else if (shapeType != ActorShapeTypes.CUSTOM && previousShapeType == ActorShapeTypes.CUSTOM) {
			deactivateTools();
		}

		setUnsaved();
	}

	@Override
	public ActorShapeTypes getShapeType() {
		if (mActorDef != null) {
			return mActorDef.getVisual().getShapeType();
		} else {
			return ActorShapeTypes.CIRCLE;
		}
	}

	@Override
	public void setShapeRadius(float radius) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.getVisual().setShapeRadius(radius);
		setUnsaved();
	}

	@Override
	public float getShapeRadius() {
		if (mActorDef != null) {
			return mActorDef.getVisual().getShapeRadius();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeWidth(float width) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.getVisual().setShapeWidth(width);
		setUnsaved();
	}

	@Override
	public float getShapeWidth() {
		if (mActorDef != null) {
			return mActorDef.getVisual().getShapeWidth();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeHeight(float height) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.getVisual().setShapeHeight(height);
		setUnsaved();
	}

	@Override
	public float getShapeHeight() {
		if (mActorDef != null) {
			return mActorDef.getVisual().getShapeHeight();
		} else {
			return 0;
		}
	}

	@Override
	public void resetCenterOffset() {
		if (mActorDef == null) {
			return;
		}
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mDrawingActor != null) {
			mDrawingActor.destroyBody();

			diffOffset = new Vector2(mActorDef.getVisual().getCenterOffset());
		}

		mActorDef.getVisual().resetCenterOffset();

		if (mDrawingActor != null) {
			diffOffset.sub(mActorDef.getVisual().getCenterOffset());
			diffOffset.add(mDrawingActor.getPosition());
			mDrawingActor.setPosition(diffOffset);
			mDrawingActor.createBody();
		}

		setUnsaved();
	}

	@Override
	public void setCenterOffset(Vector2 newCenter) {
		if (mActorDef == null) {
			return;
		}
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mDrawingActor != null) {
			mDrawingActor.destroyBody();

			diffOffset = new Vector2(mActorDef.getVisual().getCenterOffset());
		}

		mActorDef.getVisual().setCenterOffset(newCenter);

		if (mDrawingActor != null) {
			diffOffset.sub(mActorDef.getVisual().getCenterOffset());
			diffOffset.add(mDrawingActor.getPosition());
			mDrawingActor.setPosition(diffOffset);
			mDrawingActor.createBody();
		}

		setUnsaved();
	}

	@Override
	public Vector2 getCenterOffset() {
		if (mActorDef != null) {
			return mActorDef.getVisual().getCenterOffset();
		} else {
			return new Vector2();
		}
	}

	@Override
	public void setName(String name) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.setName(name);
		((EditorGui) mGui).resetName();

		setUnsaved();
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
	public void setDescription(String description) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.setDescription(description);

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
	public void switchTool(Tools tool) {
		if (mZoomTool != null) {
			if (mTools[tool.ordinal()] != null) {
				deactivateCurrentTool();
			}
			activateTool(tool);
		}
	}

	/**
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

	/**
	 * Reset the current zoom
	 */
	private void resetZoom() {
		if (mZoomTool != null) {
			fixCamera();
			mZoomTool.resetZoom();
		}
	}

	/**
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


		// Extra activation actions
		switch (tool) {
		case ZOOM_IN:
			mZoomTool.setZoomStateOnClick(true);
			break;

		case ZOOM_OUT:
			mZoomTool.setZoomStateOnClick(false);
			break;

		case ZOOM_RESET:
			resetZoom();
			break;

		case CENTER_RESET:
			mInvoker.execute(new CActorEditorCenterReset(this));
			break;

		default:
			// Does nothing
			break;
		}
	}

	@Override
	public Tools getActiveTool() {
		return mActiveTool;
	}

	/**
	 * Activate tools
	 * @param activeTool which tool to set as activated
	 */
	private void activateTools(Tools activeTool) {
		switchTool(activeTool);
		((ActorGui) mGui).resetTools();

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
	}

	/**
	 * Deactivate tools
	 */
	private void deactivateTools() {
		switchTool(Tools.NONE);
		mInputMultiplexer.removeProcessor(mTools[Tools.DELETE.ordinal()]);
		mInputMultiplexer.removeProcessor(mZoomTool);
		resetZoom();
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

		if (mActorDef.getVisual().getShapeType() == ActorShapeTypes.CUSTOM) {
			if (mDrawingActor == null) {
				mDrawingActor = newActor();
				mDrawingActor.createBody();
				mSelection.selectResource(mDrawingActor);
			}

			mDrawingActor.setDef(mActorDef);
			activateTools(Tools.MOVE);
		} else {
			deactivateTools();
		}
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
	public void onResourceAdded(IResource resource) {
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
			mInvoker.execute(new CResourceCornerRemoveAll(mActorDef.getVisual(), this), true);
			mDrawingActor = null;
			setUnsaved();
		} else if (resource instanceof VectorBrush) {
			mVectorBrush = null;
		}
	}

	@Override
	public void onResourceChanged(IResource resource) {
		// Does nothing
	}

	@Override
	public void setColor(Color color) {
		if (mActorDef != null) {
			mActorDef.getVisual().setColor(color);
			setUnsaved();
		}
	}

	@Override
	public Color getColor() {
		if (mActorDef != null) {
			return mActorDef.getVisual().getColor();
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
	 * Creates a new actor of the current actor type via the default constructor. If an
	 * actor definition has been set, this will also set that definition, else you need to
	 * set this manually if it hasn't been set through the actor's default constructor.
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
	public ArrayList<Def> getNonPublishedDependencies() {
		return ResourceRepo.getNonPublishedDependencies(mActorDef);
	}

	@Override
	public void publishDef() {
		if (mActorDef != null) {
			mGui.showProgressBar("Uploading...");
			mResourceRepo.publish(this, this, mActorDef);
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
	}

	@Override
	public void handleWebResponseSyncronously(IMethodEntity method, IEntity response) {
		super.handleWebResponseSyncronously(method, response);

		// Publish -> Remove tools
		if (response instanceof PublishMethodResponse) {
			if (((PublishMethodResponse) response).status == PublishMethodResponse.Statuses.SUCCESS) {
				switchTool(Tools.MOVE);
				mInputMultiplexer.removeProcessor(mTools[Tools.DELETE.ordinal()]);
			}
		}
	}

	@Override
	public Def getDef() {
		return mActorDef;
	}

	@Override
	public void setShapeImageScale(float scale) {
		if (mActorDef != null) {
			mActorDef.getVisual().setImageScale(scale);
			updateShapeImageActor();
		}
	}

	@Override
	public float getShapeImageScale() {
		if (mActorDef != null) {
			return mActorDef.getVisual().getImageScale();
		}
		return 0;
	}

	@Override
	public void setShapeImage(IImageNames image) {
		if (mActorDef != null) {
			mActorDef.getVisual().setImageName(image);
			updateShapeImageActor();
		}
	}

	@Override
	public IImageNames getShapeImage() {
		if (mActorDef != null) {
			return mActorDef.getVisual().getImageName();
		}

		return SHAPE_IMAGE_DEFAULT;
	}

	/**
	 * Updates and creates the shape image actor
	 */
	private void updateShapeImageActor() {
		if (mShapeImageUpdate && mActorDef != null) {
			try {
				mActorDef.getVisual().updateImageShape(mShapeImageDistMin, mShapeImageAngleMin, getScreenToWorldScale());
				setUnsaved();
			} catch (PolygonAreaTooSmallException | PolygonComplexException | PolygonCornersTooCloseException e) {
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

	/** If the shape image should be updated continuously */
	private boolean mShapeImageUpdate = false;
	/** Minimum angle to save between actors */
	private Float mShapeImageAngleMin;
	/** Minimum distance between points */
	private float mShapeImageDistMin;
	/** Default shape image */
	private static final IImageNames SHAPE_IMAGE_DEFAULT = GeneralImages.SHUTTLE_LARGE;
	/** The actor type */
	private Class<? extends Actor> mActorType;
	/** Vector brush to render when drawing custom shapes */
	private VectorBrush mVectorBrush = null;
	/** The actor definition */
	private ActorDef mActorDef = null;
	/** Drawing actor */
	private Actor mDrawingActor = null;
	/** Active tool */
	private TouchTool[] mTools = new TouchTool[Tools.values().length];
	/** Current tool state */
	private Tools mActiveTool = Tools.NONE;
	/** Current selection */
	protected Selection mSelection = new Selection();
	/** Resource Repository */
	protected ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** Zoom tool */
	private ZoomTool mZoomTool = null;
}
