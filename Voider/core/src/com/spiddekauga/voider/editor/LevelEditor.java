package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.utils.scene.ui.ProgressBar;
import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Level;
import com.spiddekauga.voider.editor.brushes.Brush;
import com.spiddekauga.voider.editor.commands.CLevelEnemyDefAdd;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.editor.tools.ActorAddTool;
import com.spiddekauga.voider.editor.tools.AddMoveCornerTool;
import com.spiddekauga.voider.editor.tools.DeleteTool;
import com.spiddekauga.voider.editor.tools.DrawAppendTool;
import com.spiddekauga.voider.editor.tools.DrawEraseTool;
import com.spiddekauga.voider.editor.tools.EnemyAddTool;
import com.spiddekauga.voider.editor.tools.ISelection;
import com.spiddekauga.voider.editor.tools.ISelectionListener;
import com.spiddekauga.voider.editor.tools.MoveTool;
import com.spiddekauga.voider.editor.tools.PanTool;
import com.spiddekauga.voider.editor.tools.PathAddTool;
import com.spiddekauga.voider.editor.tools.RemoveCornerTool;
import com.spiddekauga.voider.editor.tools.Selection;
import com.spiddekauga.voider.editor.tools.SelectionTool;
import com.spiddekauga.voider.editor.tools.TouchTool;
import com.spiddekauga.voider.editor.tools.TriggerSetTool;
import com.spiddekauga.voider.editor.tools.ZoomTool;
import com.spiddekauga.voider.explore.ExploreActions;
import com.spiddekauga.voider.explore.ExploreFactory;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelBackground;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.actors.PickupActor;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.resource.LevelDefEntity;
import com.spiddekauga.voider.network.resource.PublishResponse;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceNotFoundException;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.event.GameEvent;

import java.util.ArrayList;
import java.util.UUID;

/**
 * The level editor scene
 */
public class LevelEditor extends Editor implements IResourceChangeEditor, ISelectionListener {
private LevelBackground mBackground = null;
private byte[] mPngBytesBeforeTestRun = null;
private ArrayList<EnemyActorDef> mAddEnemies = new ArrayList<>();
private Level mLevel = null;
private LevelDef mLoadingLevel = null;
private ISelection mSelection = null;
private Tools mTool = Tools.SELECTION;
private ZoomTool mZoomTool = null;
private Color mDefaultTerrainColor = new Color();
private EditorObjects mObjects = new EditorObjects();

/**
 * Constructor for the level editor
 */
public LevelEditor() {
	super(new LevelEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_LEVEL_EDITOR, LevelDef.class);
	getGui().setLevelEditor(this);
}

;

@Override
public void handleEvent(GameEvent event) {
	switch (event.type) {
	case SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS:
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.LEVEL_DEF, false);
		ResourceCacheFacade.finishLoading();
		break;

	default:
		// Does nothing
		break;
	}

}

@Override
protected void onCreate() {
	mDefaultTerrainColor.set((Color) SkinNames.getResource(SkinNames.EditorVars.TERRAIN_COLOR_DEFAULT));
	mDefaultTerrainColor.a = SkinNames.getResource(SkinNames.EditorVars.TERRAIN_ALPHA_DEFAULT);

	mSelection = new Selection();
	mSelection.addListener(this);

	super.onCreate();

	IC_Level icLevel = ConfigIni.getInstance().editor.level;
	mZoomTool = new ZoomTool(this, icLevel.getZoomMin(), icLevel.getZoomMax());

	// Initialize tools
	Tools.ZOOM_IN.setTool(mZoomTool);
	Tools.ZOOM_OUT.setTool(mZoomTool);
	Tools.ADD_MOVE_CORNER.setTool(new AddMoveCornerTool(this, mSelection));
	Tools.DELETE.setTool(new DeleteTool(this, mSelection));
	Tools.ENEMY_ADD.setTool(new EnemyAddTool(this, mSelection, EnemyActor.class));
	Tools.ENEMY_SET_ACTIVATE_TRIGGER.setTool(new TriggerSetTool(this, mSelection, Actions.ACTOR_ACTIVATE));
	Tools.ENEMY_SET_DEACTIVATE_TRIGGER.setTool(new TriggerSetTool(this, mSelection, Actions.ACTOR_DEACTIVATE));
	Tools.MOVE.setTool(new MoveTool(this, mSelection));
	Tools.PAN.setTool(new PanTool(this));
	Tools.PATH_ADD.setTool(new PathAddTool(this, mSelection));
	Tools.PICKUP_ADD.setTool(new ActorAddTool(this, mSelection, PickupActor.class));
	Tools.REMOVE_CORNER.setTool(new RemoveCornerTool(this, mSelection));
	Tools.SELECTION.setTool(new SelectionTool(this, mSelection));
	Tools.TERRAIN_DRAW_APPEND.setTool(new DrawAppendTool(this, mSelection, StaticTerrainActor.class));
	Tools.TERRAIN_DRAW_ERASE.setTool(new DrawEraseTool(this, mSelection, StaticTerrainActor.class));

	mInputMultiplexer.addProcessor(Tools.PAN.getTool());
	mInputMultiplexer.addProcessor(Tools.SELECTION.getTool());
	mInputMultiplexer.addProcessor(Tools.DELETE.getTool());
	mInputMultiplexer.addProcessor(mZoomTool);

	updateCameraLimits();
}

@Override
protected void onResume(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onResume(outcome, message, loadingOutcome);

	// Check so that all resources have been loaded
	if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
		// Loading a level
		if (mLoadingLevel != null) {
			Level loadedLevel;
			loadedLevel = ResourceCacheFacade.get(mLoadingLevel.getLevelId());
			if (loadedLevel != null) {
				setLevel(loadedLevel);
				switchTool(Tools.SELECTION);
				getGui().resetTools();
				getGui().removeAllMsgBoxes();
				setSaved();
				mLoadingLevel = null;
			} else {
				Gdx.app.error("LevelEditor", "Could not find level (" + mLoadingLevel.getLevelId() + ")");
			}
		}
	} else if (outcome == Outcomes.EXPLORE_SELECT) {
		if (message instanceof EnemyDefEntity) {
			mInvoker.execute(new CLevelEnemyDefAdd(((EnemyDefEntity) message).resourceId, this));
		}
	} else if (outcome == Outcomes.EXPLORE_LOAD) {
		getGui().removeAllMsgBoxes();

		if (message instanceof LevelDefEntity) {
			LevelDefEntity levelDefEntity = (LevelDefEntity) message;

			if (!ResourceCacheFacade.isLoaded(levelDefEntity.resourceId, levelDefEntity.revision)) {
				ResourceCacheFacade.load(this, levelDefEntity.resourceId, true, levelDefEntity.revision);
				ResourceCacheFacade.finishLoading();
			}

			mLoadingLevel = ResourceCacheFacade.get(levelDefEntity.resourceId, levelDefEntity.revision);

			// Only load level if it's not the current level we selected, or
			// another revision
			if (mLoadingLevel != null) {
				if (mLevel == null || !mLoadingLevel.equals(mLevel.getDef()) || mLoadingLevel.getRevision() != mLevel.getRevision()) {
					ResourceCacheFacade.load(this, mLoadingLevel.getLevelId(), mLoadingLevel.getId(), levelDefEntity.revision);
					Scene scene = getLoadingScene();
					if (scene != null) {
						SceneSwitcher.switchTo(scene);
					}
				} else {
					mLoadingLevel = null;
				}
			}
		}
	} else if (outcome == Outcomes.NOT_APPLICAPLE) {
		getGui().removeAllMsgBoxes();
	}
	// Set as unsaved if took screenshot
	else if (outcome == Outcomes.LEVEL_PLAYER_DIED || outcome == Outcomes.LEVEL_COMPLETED || outcome == Outcomes.LEVEL_QUIT) {
		if (mPngBytesBeforeTestRun != mLevel.getDef().getPngImage()) {
			setUnsaved();
			getGui().resetImage();
			saveDef();
		}
	}
	// Theme selected
	else if (outcome == Outcomes.THEME_SELECTED) {
		final Themes theme = (Themes) message;
		setTheme(theme);
		getGui().removeAllMsgBoxes();
	}

	if (mLevel != null) {
		Actor.setLevel(mLevel);
	}
}

@Override
protected void update(float deltaTime) {
	super.update(deltaTime);

	if (mLevel == null) {
		return;
	}

	mLevel.update(deltaTime);
	mObjects.update(deltaTime);

	((PanTool) Tools.PAN.getTool()).update(deltaTime);
}

@Override
protected void saveImpl(Command command) {
	saveToFile();
	if (command != null) {
		command.execute();
	}
}

@Override
protected void render() {
	if (mLevel == null) {
		return;
	}

	if (Config.Graphics.USE_RELEASE_RENDERER) {
		renderBackground();
	}

	super.render();

	if (Config.Graphics.USE_RELEASE_RENDERER) {

		ShaderProgram defaultShader = ResourceCacheFacade.get(InternalNames.SHADER_DEFAULT);
		if (defaultShader != null) {
			mShapeRenderer.setShader(defaultShader);
		}
		mShapeRenderer.setProjectionMatrix(mCamera.combined);
		mShapeRenderer.push(ShapeType.Filled);
		enableBlendingWithDefaults();
		mLevel.render(mShapeRenderer, getBoundingBoxWorld());
		mLevel.renderEditor(mShapeRenderer);
		mObjects.renderShapes(mShapeRenderer);
		mShapeRenderer.flush();
		renderAboveBelowBorders();
		mShapeRenderer.pop();


		mSpriteBatch.setProjectionMatrix(mCamera.combined);
		mSpriteBatch.begin();
		mLevel.render(mSpriteBatch, getBoundingBoxWorld());
		mLevel.renderEditor(mSpriteBatch);
		mSpriteBatch.end();
	}
}

@Override
protected void onDestroy() {
	super.onDestroy();
}

@Override
public void handleWebResponseSyncronously(IMethodEntity method, IEntity response) {
	super.handleWebResponseSyncronously(method, response);

	// Publish -> Update available tools
	if (response instanceof PublishResponse) {
		if (((PublishResponse) response).status == PublishResponse.Statuses.SUCCESS) {
			updateAvailableTools(false, true);
		}
	}
}

@Override
protected void saveToFile() {
	mLevel.calculateStartEndPosition();

	int oldRevision = mLevel.getRevision();
	mResourceRepo.save((IResource) mLevel.getDef(), mLevel);
	mNotification.show(NotificationTypes.SUCCESS, Messages.Info.SAVED);
	showSyncMessage();

	// Update latest resource if revision was changed by more than one
	if (oldRevision != mLevel.getDef().getRevision() - 1) {
		ResourceCacheFacade.setLatestResource(mLevel, oldRevision);
		ResourceCacheFacade.setLatestResource(mLevel.getDef(), oldRevision);
	}

	// Saved first time? Then load level and def and use loaded versions instead
	if (!ResourceCacheFacade.isLoaded(mLevel.getId())) {
		ResourceCacheFacade.load(this, mLevel.getDef().getId(), false);
		ResourceCacheFacade.load(this, mLevel.getId(), mLevel.getDef().getId());
		ResourceCacheFacade.finishLoading();

		// Reset the level to old revision
		mLevel.getDef().setRevision(oldRevision);

		setLevel((Level) ResourceCacheFacade.get(mLevel.getId()));
	}

	setSaved();
}

@Override
public boolean onKeyDown(int keycode) {
	// Back - Deselect or go back
	if (KeyHelper.isBackPressed(keycode)) {
		if (!mSelection.isEmpty()) {
			mInvoker.execute(new CSelectionSet(mSelection));
			return true;
		}
	}

	// Tools - General
	if (KeyHelper.isNoModifiersPressed()) {
		if (keycode == Input.Keys.S) {
			switchTool(Tools.SELECTION);
			return true;
		} else if (keycode == Input.Keys.P) {
			switchTool(Tools.PATH_ADD);
			return true;
		} else if (keycode == Input.Keys.M) {
			switchTool(Tools.MOVE);
			return true;
		} else if (keycode == Input.Keys.E) {
			switchTool(Tools.ENEMY_ADD);
			return true;
		}
	}

	// Tools - Terrain
	if (keycode == Input.Keys.D) {
		if (KeyHelper.isShiftPressed()) {
			switchTool(Tools.TERRAIN_DRAW_ERASE);
			return true;
		} else if (KeyHelper.isNoModifiersPressed()) {
			switchTool(Tools.TERRAIN_DRAW_APPEND);
			return true;
		}
	} else if (keycode == Input.Keys.C) {
		if (KeyHelper.isShiftPressed()) {
			switchTool(Tools.ADD_MOVE_CORNER);
			return true;
		} else if (KeyHelper.isNoModifiersPressed()) {
			switchTool(Tools.REMOVE_CORNER);
			return true;
		}
	}

	// Tools - Trigger
	else if (keycode == Input.Keys.T) {
		if (KeyHelper.isShiftPressed()) {
			switchTool(Tools.ENEMY_SET_DEACTIVATE_TRIGGER);
			return true;
		} else if (KeyHelper.isNoModifiersPressed()) {
			switchTool(Tools.ENEMY_SET_ACTIVATE_TRIGGER);
			return true;
		}
	}

	// Tools - zoom
	else if (keycode == Input.Keys.Z) {
		if (KeyHelper.isShiftPressed()) {
			switchTool(Tools.ENEMY_SET_ACTIVATE_TRIGGER);
			return true;
		} else if (KeyHelper.isNoModifiersPressed()) {
			switchTool(Tools.ENEMY_SET_DEACTIVATE_TRIGGER);
			return true;
		}
	}

	return super.onKeyDown(keycode);
}

// --------------------------------
// Resource loading etc.
// --------------------------------
@Override
protected void loadResources() {
	super.loadResources();
	ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
	ResourceCacheFacade.loadAllOf(this, ExternalTypes.PICKUP_DEF, true);
	ResourceCacheFacade.loadAllOf(this, ExternalTypes.LEVEL_DEF, false);

	ResourceCacheFacade.load(this, InternalDeps.MUSIC_LEVEL_THEMES);
	ResourceCacheFacade.load(this, InternalDeps.THEME_ALL);
}

@Override
protected LevelEditorGui getGui() {
	return (LevelEditorGui) super.getGui();
}

@Override
public void handleWrite(long mcWrittenBytes, long mcTotalBytes) {
	float percentage = 0;
	if (mcTotalBytes != 0) {
		percentage = (float) (((double) mcWrittenBytes) / mcTotalBytes) * 100;
	}

	ProgressBar.updateProgress(percentage);
}

@Override
public void onResourceChanged(IResource resource) {
	super.onResourceChanged(resource);

	if (resource instanceof EnemyActor) {
		getGui().resetEnemyOptions();
	}
}

/**
 * Render the background
 */
private void renderBackground() {
	float renderHeightDefault = Gdx.graphics.getHeight() * Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE_INVERT;
	int renderHeight = (int) (renderHeightDefault / mCamera.zoom);
	int offsetY = (int) (renderHeightDefault - (renderHeight * 0.5f));
	offsetY -= mCamera.position.y / getScreenToWorldScale();


	if (mBackground == null) {
		mBackground = mLevel.getLevelDef().getTheme().createBackground(renderHeight);
	} else {
		mBackground = mLevel.getLevelDef().getTheme().updateBackground(mBackground, (int) (renderHeight / getScreenToWorldScale()));
	}

	float x = mCamera.position.x / mCamera.zoom * ConfigIni.getInstance().game.getLayerTopSpeed();

	enableBlendingWithDefaults();
	mSpriteBatch.setProjectionMatrix(getProjectionMatrixDefault());
	mSpriteBatch.begin();
	mBackground.render(mSpriteBatch, x, offsetY, renderHeight);
	mSpriteBatch.end();
}

/**
 * Renders the above and below borders
 */
private void renderAboveBelowBorders() {
	// Get screen corners in world coordinates
	Vector2 minPos = new Vector2();
	Vector2 maxPos = new Vector2();
	screenToWorldCoord(mCamera, 0, Gdx.graphics.getHeight(), minPos, false);
	screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), 0, maxPos, false);

	float yCoord = Config.Graphics.HEIGHT_DEFAULT * Config.Graphics.WORLD_SCALE * 0.5f;

	mShapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.LEVEL_ABOVE_BELOW_COLOR));

	// Draw borders
	RenderOrders.offsetZValue(mShapeRenderer, RenderOrders.LEVEL_UPPER_LOWER_BORDERS);
	// Upper
	mShapeRenderer.rect(minPos.x, minPos.y, maxPos.x, -yCoord, true);
	// Lower
	mShapeRenderer.rect(minPos.x, maxPos.y, maxPos.x, yCoord, true);
	RenderOrders.resetZValueOffset(mShapeRenderer, RenderOrders.LEVEL_UPPER_LOWER_BORDERS);
}

/**
 * Creates all the resource bodies for the current level
 */
private void createResourceBodies() {
	if (mLevel != null) {
		ArrayList<IResourceBody> resources = mLevel.getResources(IResourceBody.class);

		// Create all bodies except enemies in a group, only create the leader then.
		for (IResourceBody resourceBody : resources) {
			if (resourceBody instanceof EnemyActor) {
				if (((EnemyActor) resourceBody).getEnemyGroup() == null || ((EnemyActor) resourceBody).isGroupLeader()) {
					resourceBody.createBody();
				}
			} else if (resourceBody instanceof Path) {
				((Path) resourceBody).setWorld(mWorld);
			} else {
				resourceBody.createBody();
			}
		}
	}
}

/**
 * Clears all enemy definitions
 */
private void clearEnemyDef() {
	mAddEnemies.clear();
}

/**
 * Adds a new enemy to the add enemy table
 * @param enemyDef the enemy definition to add to the add list
 */
private void addEnemyDef(EnemyActorDef enemyDef) {
	if (enemyDef != null) {
		if (!mAddEnemies.contains(enemyDef)) {
			mAddEnemies.add(0, enemyDef);
			getGui().resetEnemyAddTable();
		}
	}
}

/**
 * Updates the available tools depending on if the current level is published or not.
 * @param oldIsPublished true if the previous level was published
 * @param newIsPublished true if the newly loaded level is published
 */
private void updateAvailableTools(boolean oldIsPublished, boolean newIsPublished) {
	// Only do something with the tools if they changed
	if (oldIsPublished != newIsPublished) {
		// Readd delete tool again
		if (oldIsPublished) {
			mInputMultiplexer.addProcessor(Tools.DELETE.getTool());
		}
		// Remove delete tool and activate selection tool
		else {
			switchTool(Tools.SELECTION);
			mInputMultiplexer.removeProcessor(Tools.DELETE.getTool());
		}
	}
}

/**
 * Switch currently selected tool
 * @param tool the new tool to use
 */
void switchTool(Tools tool) {
	if (mTool.getTool() != null) {
		mTool.getTool().deactivate();

		// Never certain tools that should be kept even when inactive
		if (!isAvailableWhenInactive(mTool)) {
			mInputMultiplexer.removeProcessor(mTool.getTool());
		}
	}

	mTool = tool;

	if (mTool.getTool() != null) {
		mTool.getTool().activate();

		// Never add some tools as they are already added
		if (!isAvailableWhenInactive(mTool)) {
			mInputMultiplexer.addProcessor(mTool.getTool());
		}

		// Set selectable resource types
		if (mTool != Tools.SELECTION) {
			((SelectionTool) Tools.SELECTION.getTool()).setSelectableResourceTypes(mTool.getTool().getSelectableResourceTypes(), mTool.getTool()
					.isSelectionToolAllowedToChangeResourceType());
		}
	}

	getGui().resetColor();
	getGui().resetTools();
}

/**
 * Checks if the tool should be available in the background
 * @return true if the tool should be available when inactive (but with limited functionality)
 */
private boolean isAvailableWhenInactive(Tools tool) {
	switch (tool) {
	case SELECTION:
	case DELETE:
	case PAN:
		return true;

	default:
		return false;
	}
}

/**
 * Update camera/world limits
 */
private void updateCameraLimits() {
	float halfHeight = mCamera.viewportHeight / 2;

	mZoomTool.setWorldMinY(-halfHeight);
	mZoomTool.setWorldMaxY(halfHeight);

	PanTool panTool = (PanTool) Tools.PAN.getTool();
	panTool.setWorldMinY(-halfHeight);
	panTool.setWorldMaxY(halfHeight);
}

@Override
protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
	super.reloadResourcesOnActivate(outcome, message);

	// Check if we have created any new enemies, load them in that case
	ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
}

/**
 * Adds a new enemy to the add enemy table
 * @param enemyId the enemy id to add
 * @return true if enemy was selected successfully, false if unsuccessful
 */
public boolean addEnemyDef(UUID enemyId) {
	if (enemyId != null) {
		EnemyActorDef enemyActorDef = ResourceCacheFacade.get(enemyId);
		if (enemyActorDef != null) {
			if (!mAddEnemies.contains(enemyActorDef)) {
				if (Tools.ENEMY_ADD.mTool != null) {
					((EnemyAddTool) Tools.ENEMY_ADD.mTool).setActorDef(enemyActorDef);
				}
				mAddEnemies.add(0, enemyActorDef);
				getGui().resetEnemyAddTable();
				return true;
			} else {
				mNotification.show("This enemy has already been added.");
			}
		}
	}

	return false;
}

/**
 * Removes an enemy from the add enemy table
 * @param enemyId the enemy id to remove.
 * @return true if the enemy was successfully removed, false if it was not found.
 */
public boolean removeEnemyDef(UUID enemyId) {
	if (enemyId != null) {
		EnemyActorDef enemyActorDef = ResourceCacheFacade.get(enemyId);
		if (enemyActorDef != null) {
			boolean removed = mAddEnemies.remove(enemyActorDef);

			if (removed) {
				getGui().resetEnemyAddTable();
				return true;
			}
		}
	}

	return false;
}

/**
 * Selects the specified pickup definition. This pickup will be used when adding new pickups
 * @param pickupId the pickup id to select
 * @return true if the pickup was selected successfully, false if unsuccessful
 */
public boolean selectPickupDef(UUID pickupId) {
	try {
		PickupActorDef pickupActorDef = null;
		if (pickupId != null) {
			pickupActorDef = ResourceCacheFacade.get(pickupId);
		}
		((ActorAddTool) Tools.PICKUP_ADD.getTool()).setActorDef(pickupActorDef);
		getGui().resetValues();
	} catch (Exception e) {
		Gdx.app.error("LevelEditor", e.toString());
		e.printStackTrace();
		return false;
	}

	return true;
}

@Override
public void onResourceAdded(IResource resource, boolean isNew) {
	// Brush
	if (resource instanceof Brush) {
		mObjects.add(resource);
	} else {
		getGui().resetValues();
		mLevel.addResource(resource);

		// Set default color
		if (isNew) {
			if (resource instanceof StaticTerrainActor) {
				((StaticTerrainActor) resource).getDef().getShape().setColor(mDefaultTerrainColor);
			}
		}

		setUnsaved();
	}
}

@Override
public void onResourceRemoved(IResource resource) {
	// Brush
	if (resource instanceof Brush) {
		mObjects.remove(resource);
	} else {
		getGui().resetValues();
		mLevel.removeResource(resource);

		setUnsaved();
	}
}

@Override
public void onResourceSelected(IResource resource) {
	getGui().resetValues();
}

@Override
public void onResourceDeselected(IResource resource) {
	getGui().resetValues();
}

/**
 * @return true if an enemy is currently selected
 */
boolean isEnemySelected() {
	return mSelection != null && mSelection.isSelected(EnemyActor.class);
}

/**
 * @return selected tool
 */
Tools getSelectedTool() {
	return mTool;
}

/**
 * Tests to run the level from the current location
 * @param invulnerable makes the player invulnerable
 */
public void runFromHere(boolean invulnerable) {
	runFromPos(invulnerable, getRunFromHerePosition());
}

/**
 * Run from the specified position
 * @param invulnerable makes the player invulnerable
 * @param xPosition run from this position
 */
private void runFromPos(boolean invulnerable, float xPosition) {
	if (!isSaved()) {
		saveDef();
	}

	GameScene testGame = new GameScene(true, invulnerable);
	Level copyLevel = mLevel.copy();

	copyLevel.setStartPosition(xPosition);
	copyLevel.createDefaultTriggers();
	removeTriggersBeforeRun(copyLevel);
	testGame.setLevelToRun(copyLevel);

	mPngBytesBeforeTestRun = copyLevel.getDef().getPngImage();

	SceneSwitcher.switchTo(testGame);
}

/**
 * Calculates the current starting position when test running
 * @return start position of the level when test running from here
 */
private float getRunFromHerePosition() {
	return mCamera.position.x + mCamera.viewportWidth * 0.5f - mCamera.viewportWidth * Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE_INVERT;
}

/**
 * Remove triggers before the testing coordinates for enemies
 * @param level the level to remove triggers from
 */
private void removeTriggersBeforeRun(Level level) {
	float width = calculateDefaultWorldWidth();
	float leftXCoord = level.getXCoord() - width;

	ArrayList<IResource> toBeRemoved = new ArrayList<>();
	ArrayList<TScreenAt> triggers = level.getResources(TScreenAt.class);
	for (TScreenAt trigger : triggers) {
		if (trigger.isTriggered()) {
			// Remove triggers left of the screen
			if (trigger.getPosition().x <= leftXCoord) {
				toBeRemoved.add(trigger);
			}
		}
	}

	level.removeResources(toBeRemoved);
}

/**
 * Test run the level from the start
 * @param invulnerable makes the player invulnerable
 */
public void runFromStart(boolean invulnerable) {
	mLevel.calculateStartEndPosition();
	runFromPos(invulnerable, mLevel.getLevelDef().getStartXCoord());
}

/**
 * Calculates the left window position when test running
 * @return start position (left side of the window) when test running from here
 */
public float getRunFromHereLeftPosition() {
	return getRunFromHerePosition() - calculateDefaultWorldWidth();
}

@Override
public void newDef() {
	LevelDef levelDef = new LevelDef();
	Level level = new Level(levelDef);
	setLevel(level);
	setSaved();
}

@Override
public void duplicateDef(String name, String description) {
	// Remove this level from the level
	mLevel.removeResource(mLevel);

	Level level = mLevel.copyNewResource();
	LevelDef levelDef = level.getDef();
	levelDef.setName(name);
	levelDef.setDescription(description);

	// Save camera variables
	Vector3 cameraPos = new Vector3(mCamera.position);
	float cameraZoom = mCamera.zoom;

	setLevel(level);
	setUnsaved();
	saveDef();

	// Reset camera position
	mCamera.position.set(cameraPos);
	mCamera.zoom = cameraZoom;
	mCamera.update();
}

@Override
public boolean isDrawing() {
	return mTool.getTool() != null && mTool.getTool().isDrawing();
}

@Override
public void publishDef() {
	if (mLevel != null) {
		ProgressBar.showProgress("Uploading...");
		mResourceRepo.publish(this, this, mLevel);
	}
}

@Override
public boolean isPublished() {
	if (mLevel != null) {
		try {
			return ResourceLocalRepo.isPublished(mLevel.getDef().getId());
		} catch (ResourceNotFoundException e) {
			// Do nothing
		}
	}

	return false;
}

@Override
public void undoJustCreated() {
	setLevel(null);
}

/**
 * @return name of the level, empty string if no level is available
 */
@Override
public String getName() {
	if (mLevel != null) {
		return mLevel.getDef().getName();
	} else {
		return "";
	}
}

/**
 * Sets the name of the level
 * @param name name of the level
 */
@Override
public void setName(String name) {
	if (mLevel != null) {
		mLevel.getDef().setName(name);
		getGui().resetName();
		setUnsaved();
	}
}

/**
 * @return description of the level, empty string if no level is available
 */
@Override
public String getDescription() {
	if (mLevel != null) {
		return mLevel.getDef().getDescription();
	} else {
		return "";
	}
}

/**
 * Sets the description of the level
 * @param description text description of the level
 */
@Override
public void setDescription(String description) {
	if (mLevel != null) {
		mLevel.getDef().setDescription(description);
		setUnsaved();
	}
}

@Override
public Def getDef() {
	if (mLevel != null) {
		return mLevel.getDef();
	}
	return null;
}

/**
 * @return starting speed of the level, negative if no level is available
 */
float getLevelStartingSpeed() {
	if (mLevel != null) {
		return mLevel.getLevelDef().getBaseSpeed();
	} else {
		return -1;
	}
}

/**
 * Sets the starting speed of the current level
 * @param speed starting speed of the current level
 */
void setLevelStartingSpeed(float speed) {
	if (mLevel != null) {
		mLevel.setSpeed(speed);
		mLevel.getLevelDef().setBaseSpeed(speed);
		setUnsaved();
	}
}

/**
 * @return story that will be displayed before the level, empty string if no level is available
 */
String getPrologue() {
	if (mLevel != null) {
		return mLevel.getLevelDef().getPrologue();
	} else {
		return "";
	}
}

/**
 * Sets the story before the level
 * @param storyText the story that will be displayed before the level
 */
void setPrologue(String storyText) {
	if (mLevel != null) {
		mLevel.getLevelDef().setPrologue(storyText);
		setUnsaved();
	}
}

/**
 * @return story that will be displayed after completing the level, empty string if no level is
 * available
 */
String getEpilogue() {
	if (mLevel != null) {
		return mLevel.getLevelDef().getEpilogue();
	}
	return "";


}

/**
 * Sets the story after completing the level
 * @param storyText the story that will be displayed after the level
 */
void setEpilogue(String storyText) {
	if (mLevel != null) {
		mLevel.getLevelDef().setStoryAfter(storyText);
		setUnsaved();
	}


}

/**
 * @return true if screenshot has been taken for the level
 */
boolean hasScreenshot() {
	return mLevel != null && mLevel.getDef().getPngImage() != null;
}

/**
 * @return color of the selected terrain. If multiple terrains are selected and have different
 * colors, null is returned. If no terrain is selected the default color is returned.
 */
Color getSelectedTerrainColor() {
	ArrayList<StaticTerrainActor> terrains = mSelection.getSelectedResourcesOfType(StaticTerrainActor.class);

	if (!terrains.isEmpty()) {
		Color terrainColor = new Color(terrains.get(0).getDef().getShape().getColor());
		terrainColor.a = 1;

		// Check for different colors -> Return default color
		Color testColor = new Color();
		for (StaticTerrainActor terrain : terrains) {
			testColor.set(terrain.getDef().getShape().getColor());
			testColor.a = 1;

			if (!terrainColor.equals(testColor)) {
				return null;
			}
		}

		return terrainColor;
	}

	return null;
}

/**
 * Sets the color of the selected terrain
 * @param color new color of the terrain
 */
void setSelectedTerrainColor(Color color) {
	ArrayList<StaticTerrainActor> terrains = mSelection.getSelectedResourcesOfType(StaticTerrainActor.class);

	for (StaticTerrainActor terrain : terrains) {
		Color terrainColor = terrain.getDef().getShape().getColor();
		terrainColor.r = color.r;
		terrainColor.g = color.g;
		terrainColor.b = color.b;
	}

	setUnsaved();
}

/**
 * @return opacity of the selected terrain. -1 if multiple terrains are selected and have different
 * opacity. The default opacity is return when no terrain is selected.
 */
float getSelectedTerrainOpacity() {
	ArrayList<StaticTerrainActor> terrains = mSelection.getSelectedResourcesOfType(StaticTerrainActor.class);

	if (!terrains.isEmpty()) {
		float opacity = terrains.get(0).getDef().getShape().getColor().a;

		// Check for different colors -> Return default color
		for (StaticTerrainActor terrain : terrains) {
			if (opacity != terrain.getDef().getShape().getColor().a) {
				return -1;
			}
		}

		return opacity * 100;
	}

	return mDefaultTerrainColor.a * 100;
}

/**
 * Sets the opacity of the selected terrain
 * @param opacity new transparency level of the terrain, should be between 0-100
 */
void setSelectedTerrainOpacity(float opacity) {
	ArrayList<StaticTerrainActor> terrains = mSelection.getSelectedResourcesOfType(StaticTerrainActor.class);

	for (StaticTerrainActor terrain : terrains) {
		terrain.getDef().getShape().getColor().a = opacity / 100f;
	}
}

/**
 * @return default color for new terrains
 */
Color getDefaultTerrainColor() {
	Color color = new Color(mDefaultTerrainColor);
	color.a = 1;
	return color;
}

/**
 * Sets the default color for new terrain. Doesn't use alpha value
 * @param color default color for new terrains
 */
void setDefaultTerrainColor(Color color) {
	mDefaultTerrainColor.set(color.r, color.g, color.b, mDefaultTerrainColor.a);
}

/**
 * @return default opacity value for new terrain
 */
float getDefaultTerrainOpacity() {
	return mDefaultTerrainColor.a * 100;
}

/**
 * Sets the default opacity/alpha value for new terrain.
 * @param opacity transparency of new terrains, should be from 0-100
 */
void setDefaultTerrainOpacity(float opacity) {
	mDefaultTerrainColor.a = opacity / 100f;
}

/**
 * @return true if a terrain tool is currently selected
 */
boolean isTerrainToolSelected() {
	switch (mTool) {
	case TERRAIN_DRAW_APPEND:
	case TERRAIN_DRAW_ERASE:
	case ADD_MOVE_CORNER:
	case REMOVE_CORNER:
		return true;

	default:
		return false;
	}
}

/**
 * @return true if any terrain is selected
 */
boolean isTerrainSelected() {
	return mSelection.isSelected(StaticTerrainActor.class);
}

/**
 * @return get the enemy actor to create, null if none is selected
 */
EnemyActorDef getSelectedEnemyToCreate() {
	ActorAddTool actorAddTool = ((ActorAddTool) Tools.ENEMY_ADD.getTool());
	if (actorAddTool != null) {
		ActorDef actorDef = actorAddTool.getActorDef();
		if (actorDef instanceof EnemyActorDef) {
			return (EnemyActorDef) actorDef;
		}
	}

	return null;
}

/**
 * @return selected enemy name, null if none is selected
 */
String getSelectedEnemyName() {
	ActorDef actorDef = ((ActorAddTool) Tools.ENEMY_ADD.getTool()).getActorDef();

	if (actorDef != null) {
		return actorDef.getName();
	} else {
		return null;
	}
}

/**
 * @return selected pickup name, null if none is selected
 */
String getSelectedPickupName() {
	ActorDef actorDef = ((ActorAddTool) Tools.PICKUP_ADD.getTool()).getActorDef();

	if (actorDef != null) {
		return actorDef.getName();
	} else {
		return null;
	}
}

/**
 * Clears the selection
 */
void clearSelection() {
	mSelection.clearSelection();
}

/**
 * @return spawn delay between actors in the same group, negative value if no group exist
 */
float getEnemySpawnDelay() {
	if (getEnemyCount() > 1) {
		ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

		if (!selectedEnemies.isEmpty()) {
			return selectedEnemies.get(0).getEnemyGroup().getSpawnTriggerDelay();
		}
	}

	return -1;
}

/**
 * @return number of enemies in a group
 */
int getEnemyCount() {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	// Only return enemy count if all selected enemies have the same enemy count
	if (selectedEnemies.size() > 0) {
		int cEnemies = 1;
		EnemyActor firstEnemy = selectedEnemies.get(0);
		EnemyGroup enemyGroup = firstEnemy.getEnemyGroup();
		if (enemyGroup != null) {
			cEnemies = enemyGroup.getEnemyCount();
		}

		for (int i = 1; i < selectedEnemies.size(); ++i) {
			enemyGroup = selectedEnemies.get(i).getEnemyGroup();

			if (enemyGroup != null) {
				if (cEnemies != enemyGroup.getEnemyCount()) {
					return -1;
				}
			} else if (cEnemies != 1) {
				return -1;
			}
		}

		return cEnemies;
	}

	return -1;
}

/**
 * Sets the number of enemies in one group
 * @param cEnemies number of enemies in the group
 */
void setEnemyCount(int cEnemies) {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	for (EnemyActor selectedEnemy : selectedEnemies) {

		if (selectedEnemy != null) {
			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();

			// We have an enemy group
			if (enemyGroup != null) {
				// Just change amount of enemies
				if (cEnemies > 1) {
					ArrayList<EnemyActor> addedEnemies = new ArrayList<>();
					ArrayList<EnemyActor> removedEnemies = new ArrayList<>();

					enemyGroup.setEnemyCount(cEnemies, addedEnemies, removedEnemies);

					for (EnemyActor addedEnemy : addedEnemies) {
						mLevel.addResource(addedEnemy);
					}

					for (EnemyActor removedEnemy : removedEnemies) {
						mLevel.removeResource(removedEnemy.getId());
					}
				}
				// Delete enemy group
				else {
					// Remove all excess enemies
					ArrayList<EnemyActor> removedEnemies = enemyGroup.clear();

					for (EnemyActor enemyActor : removedEnemies) {
						mLevel.removeResource(enemyActor.getId());
					}

					mLevel.removeResource(enemyGroup.getId());
					removedEnemies = null;
				}
			}
			// No enemy group, do we create one?
			else if (cEnemies > 1) {
				enemyGroup = new EnemyGroup();
				mLevel.addResource(enemyGroup);

				enemyGroup.setLeaderEnemy(selectedEnemy);

				ArrayList<EnemyActor> addedEnemies = new ArrayList<>();
				enemyGroup.setEnemyCount(cEnemies, addedEnemies, null);

				for (EnemyActor addedEnemy : addedEnemies) {
					mLevel.addResource(addedEnemy);
					addedEnemy.destroyBody();
				}
				addedEnemies = null;
			}
		}
	}

	getGui().resetEnemyOptions();
	setUnsaved();
}

/**
 * Sets the spawn delay between actors in the same group.
 * @param delay seconds of delay between actors are activated.
 */
void setEnemySpawnDelay(float delay) {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	for (EnemyActor enemy : selectedEnemies) {
		EnemyGroup enemyGroup = enemy.getEnemyGroup();

		if (enemyGroup != null) {
			enemyGroup.setSpawnTriggerDelay(delay);
		}
	}
	setUnsaved();
}

/**
 * @return current path type. If several paths are selected and they have different path types null
 * is return. If no path is selected null is also returned.
 */
PathTypes getPathType() {
	int cOnce = 0;
	int cLoop = 0;
	int cBackAndForth = 0;

	ArrayList<Path> selectedPaths = mSelection.getSelectedResourcesOfType(Path.class);

	for (Path path : selectedPaths) {
		switch (path.getPathType()) {
		case ONCE:
			cOnce++;
			break;

		case LOOP:
			cLoop++;
			break;

		case BACK_AND_FORTH:
			cBackAndForth++;
			break;
		}
	}

	// Only allow one to be above 0
	if (cOnce > 0 && cLoop == 0 && cBackAndForth == 0) {
		return PathTypes.ONCE;
	} else if (cLoop > 0 && cOnce == 0 && cBackAndForth == 0) {
		return PathTypes.LOOP;
	} else if (cBackAndForth > 0 && cOnce == 0 && cLoop == 0) {
		return PathTypes.BACK_AND_FORTH;
	}

	return null;
}

/**
 * Sets the path type of the selected path
 * @param pathType type of the path
 */
void setPathType(Path.PathTypes pathType) {
	ArrayList<Path> selectedPaths = mSelection.getSelectedResourcesOfType(Path.class);

	for (Path path : selectedPaths) {
		path.setPathType(pathType);
	}

	getGui().resetValues();
}

/**
 * @return true if a path is selected
 */
boolean isPathSelected() {
	if (mSelection != null) {
		return mSelection.isSelected(Path.class);
	} else {
		return false;
	}
}

/**
 * @return all paths in the current level
 */
public ArrayList<Path> getPaths() {
	return mLevel.getResources(Path.class);
}

/**
 * Select enemy
 */
void addEnemyToList() {
	SceneSwitcher.switchTo(ExploreFactory.create(EnemyActorDef.class, ExploreActions.SELECT));
}

/**
 * @return true if the selected enemy has an activation trigger, false if not or if no enemy is
 * selected
 */
boolean hasSelectedEnemyActivateTrigger() {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	boolean allHasActivateTrigger = true;
	for (EnemyActor enemy : selectedEnemies) {
		if (TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_ACTIVATE) == null) {
			allHasActivateTrigger = false;
			break;
		}
	}
	return allHasActivateTrigger;
}

/**
 * @return delay of the activation trigger, negative if no activation trigger has been set.
 */
float getSelectedEnemyActivateTriggerDelay() {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	float triggerDelay = 0;
	if (!selectedEnemies.isEmpty()) {
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(selectedEnemies.get(0), Actions.ACTOR_ACTIVATE);
		if (triggerInfo != null) {
			triggerDelay = triggerInfo.delay;
		}
	}

	for (EnemyActor enemy : selectedEnemies) {
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_ACTIVATE);

		if (triggerInfo == null || triggerInfo.delay != triggerDelay) {
			triggerDelay = 0;
			break;
		}
	}
	return triggerDelay;
}

/**
 * Sets the delay of the activation trigger
 * @param delay seconds of delay
 */
void setSelectedEnemyActivateTriggerDelay(float delay) {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	for (EnemyActor enemy : selectedEnemies) {
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_ACTIVATE);

		if (triggerInfo != null) {
			triggerInfo.delay = delay;
			break;
		}
	}
	setUnsaved();
}

/**
 * @return true if the selected enemy has an deactivation trigger, false if not or if no enemy is
 * selected
 */
boolean hasSelectedEnemyDeactivateTrigger() {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	boolean allHasDeactivateTrigger = true;
	for (EnemyActor enemy : selectedEnemies) {
		if (TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_DEACTIVATE) == null) {
			allHasDeactivateTrigger = false;
			break;
		}
	}
	return allHasDeactivateTrigger;
}

/**
 * @return delay of the deactivation trigger, negative if no deactivation trigger has been set.
 */
float getSelectedEnemyDeactivateTriggerDelay() {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	float triggerDelay = 0;
	if (!selectedEnemies.isEmpty()) {
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(selectedEnemies.get(0), Actions.ACTOR_DEACTIVATE);
		if (triggerInfo != null) {
			triggerDelay = triggerInfo.delay;
		}
	}

	for (EnemyActor enemy : selectedEnemies) {
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_DEACTIVATE);

		if (triggerInfo == null || triggerInfo.delay != triggerDelay) {
			triggerDelay = 0;
			break;
		}
	}
	return triggerDelay;
}

/**
 * Sets the delay of the deactivate trigger
 * @param delay seconds of delay
 */
void setSelectedEnemyDeactivateTriggerDelay(float delay) {
	ArrayList<EnemyActor> selectedEnemies = mSelection.getSelectedResourcesOfType(EnemyActor.class);

	for (EnemyActor enemy : selectedEnemies) {
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_DEACTIVATE);

		if (triggerInfo != null) {
			triggerInfo.delay = delay;
			break;
		}
	}
	setUnsaved();
}

/**
 * @return current level
 */
public Level getLevel() {
	return mLevel;
}

/**
 * Sets the level that shall be played and resets tools, invoker, etc.
 * @param level level to play
 */
private void setLevel(Level level) {
	boolean sameLevel = false;
	boolean sameRevision = false;

	boolean oldIsPublished = isPublished();

	if (mLevel != null) {
		if (level != null && mLevel.equals(level)) {
			sameLevel = true;
			if (mLevel.equals(level)) {
				sameRevision = true;
			}
		}

		// Unload the old level
		mLevel.dispose();
		if (ResourceCacheFacade.isLoaded(mLevel.getId()) && !sameRevision) {
			ResourceCacheFacade.unload(mLevel.getId(), mLevel.getRevision());
		}
	}

	mLevel = level;

	if (mLevel != null) {
		// Reset camera position to the start
		if (!sameLevel) {
			mCamera.position.x = mLevel.getLevelDef().getStartXCoord() + mCamera.viewportWidth * 0.5f;
			mCamera.position.y = 0;
			mCamera.zoom = 1;
			mCamera.update();
		}
		((PanTool) Tools.PAN.getTool()).stop();

		createResourceBodies();

		// Activate all enemies and add them to the add enemy list
		clearEnemyDef();
		ArrayList<EnemyActor> enemies = mLevel.getResources(EnemyActor.class);
		for (EnemyActor enemy : enemies) {
			if (enemy.getEnemyGroup() == null || enemy.isGroupLeader()) {
				enemy.activate();
				addEnemyDef(enemy.getDef(EnemyActorDef.class));
			}
		}
	}

	updateAvailableTools(oldIsPublished, isPublished());
	getGui().resetValues();

	mInvoker.dispose();

	Actor.setLevel(mLevel);
}

/**
 * @return currently selected pickup definition
 */
public ActorDef getSelectedPickupDef() {
	return ((ActorAddTool) Tools.PICKUP_ADD.getTool()).getActorDef();
}

/**
 * @return currently selected enemy definition
 */
public ActorDef getSelectedEnemyDef() {
	return ((ActorAddTool) Tools.ENEMY_ADD.getTool()).getActorDef();
}

/**
 * Reset the current zoom
 */
void resetZoom() {
	if (mZoomTool != null) {
		fixCamera();
		mZoomTool.resetZoom();
	}
}

@Override
protected void fixCamera() {
	float width = Gdx.graphics.getWidth() * Config.Graphics.LEVEL_EDITOR_SCALE;
	// Decrease scale of width depending on height scaled
	float heightScale = ((float) Config.Graphics.HEIGHT_DEFAULT) / Gdx.graphics.getHeight();
	width *= heightScale;
	float height = Config.Graphics.HEIGHT_DEFAULT * Config.Graphics.LEVEL_EDITOR_SCALE;

	if (mCamera != null) {
		mCamera.viewportHeight = height;
		mCamera.viewportWidth = width;
		mCamera.update();
	} else {
		mCamera = new OrthographicCamera(width, height);
	}
}

/**
 * Zoom in
 */
void zoomIn() {
	if (mZoomTool != null) {
		fixCamera();
		mZoomTool.zoomIn();
	}
}

/**
 * Zoom out
 */
void zoomOut() {
	if (mZoomTool != null) {
		fixCamera();
		mZoomTool.zoomOut();
	}
}

/**
 * @return all enemies for the add table
 */
ArrayList<EnemyActorDef> getAddEnemies() {
	return mAddEnemies;
}

/**
 * Called when selecting the enemy type to create
 * @param enemyDef the enemy type to create
 */
void createNewEnemy(EnemyActorDef enemyDef) {
	((EnemyAddTool) Tools.ENEMY_ADD.mTool).setActorDef(enemyDef);
}

/**
 * Set the theme internally
 */
private void _setTheme(Themes theme) {
	if (mLevel != null) {
		mLevel.getLevelDef().setTheme(theme);
		setUnsaved();
		getGui().resetTheme();
	}
}

/**
 * @return current theme of the level
 */
Themes getTheme() {
	if (mLevel != null) {
		return mLevel.getLevelDef().getTheme();
	} else {
		return Themes.SPACE;
	}
}

/**
 * Set the theme for the level
 * @param theme the theme for the level
 */
void setTheme(final Themes theme) {
	mInvoker.execute(new Command() {
		Themes mOldTheme = null;

		@Override
		public boolean execute() {
			mOldTheme = getTheme();
			_setTheme(theme);
			return true;
		}

		@Override
		public boolean undo() {
			_setTheme(mOldTheme);
			return true;
		}
	});
}

/**
 * @return drawable image of the level
 */
Drawable getImage() {
	if (mLevel != null) {
		return mLevel.getDef().getTextureRegionDrawable();
	} else {
		return null;
	}
}


/**
 * All tools, such as selection, add terrain, etc
 */
enum Tools {
	/** Selection tool */
	SELECTION,
	/** Move */
	MOVE,
	/** Delete */
	DELETE,
	/** Pan */
	PAN,
	/** add a corner or move a corner in a terrain */
	ADD_MOVE_CORNER,
	/** Remove a corner from the terrain */
	REMOVE_CORNER,
	/** Append to the terrain */
	TERRAIN_DRAW_APPEND,
	/** Draw erase for terrain */
	TERRAIN_DRAW_ERASE,
	/** Add pickup */
	PICKUP_ADD,
	/** Add enemy */
	ENEMY_ADD,
	/** Set activate trigger for enemies */
	ENEMY_SET_ACTIVATE_TRIGGER,
	/** Set deactivate trigger for enemies */
	ENEMY_SET_DEACTIVATE_TRIGGER,
	/** Add a corner to a path */
	PATH_ADD,
	/** Zoom in */
	ZOOM_IN,
	/** Zoom out */
	ZOOM_OUT,;

	/** The actual tool */
	private TouchTool mTool = null;

	/**
	 * @return the actual tool used in the editor
	 */
	public TouchTool getTool() {
		return mTool;
	}

	/**
	 * Sets the actual tool
	 * @param tool the actual tool that is used
	 */
	public void setTool(TouchTool tool) {
		mTool = tool;
	}
}

/**
 * Sets the music for the level
 * @param music the music for the level
 */
void setMusic(final Music music) {
	mInvoker.execute(new Command() {
		Music mOldMusic = null;

		@Override
		public boolean execute() {
			mOldMusic = getMusic();
			_setMusic(music);
			return true;
		}

		@Override
		public boolean undo() {
			_setMusic(mOldMusic);
			return true;
		}


	});
}

/**
 * Set the music internally
 */
private void _setMusic(Music music) {
	if (mLevel != null) {
		mLevel.getLevelDef().setMusic(music);
		setUnsaved();
		getGui().resetMusic();
	}
}

/**
 * @return current music for the level
 */
Music getMusic() {
	if (mLevel != null) {
		return mLevel.getLevelDef().getMusic();
	} else {
		return Music.SPACE;
	}
}


}