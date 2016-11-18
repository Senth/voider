package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IdentityMap;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.PngExport;
import com.spiddekauga.utils.Screens;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Gui;
import com.spiddekauga.utils.scene.ui.LoadingProgressScene;
import com.spiddekauga.utils.scene.ui.LoadingScene;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.utils.scene.ui.ProgressBar;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;
import com.spiddekauga.utils.scene.ui.WorldScene;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_General;
import com.spiddekauga.voider.editor.commands.CEditorDuplicate;
import com.spiddekauga.voider.explore.ExploreActions;
import com.spiddekauga.voider.explore.ExploreFactory;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.PublishResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.utils.Graphics;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.IEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Common class for all editors
 */
public abstract class Editor extends WorldScene
		implements IEditor, IResourceChangeEditor, IResponseListener, IOutstreamProgressListener, IEventListener {

private static final int COLOR_TRANSPARENT = 0x00000000;
private static final int COLOR_BLACK = 0x000000FF;
protected static Synchronizer mSynchronizer = Synchronizer.getInstance();
protected ResourceRepo mResourceRepo = ResourceRepo.getInstance();
protected Invoker mInvoker = new Invoker();
protected SpriteBatch mSpriteBatch = new SpriteBatch();
private boolean mSaved = false;
private boolean mSaving = false;
private ActorDef mSavingActorDef = null;
private Actor mSavingActor = null;
private ImageSaveOnActor[] mSavingImages = null;
private float mUnsavedTime = 0;
private float mActivityTimeLast = 0;
private Command mExecutedAfterSaved = null;
private boolean mGridRender = true;
private BlockingQueue<WebWrapper> mWebResponses = new LinkedBlockingQueue<>();
private boolean mCollisionBoxesBeenCreated = false;
private Matrix4 mProjectionMatrixDefault = new Matrix4();
private Class<? extends Def> mDefType;


/**
 * @param gui GUI to be used with the editor
 * @param pickRadius picking radius of the editor
 * @param defType the definition we edit in this editor
 */
public Editor(Gui gui, float pickRadius, Class<? extends Def> defType) {
	super(gui, pickRadius);

	mDefType = defType;
	mSpriteBatch.setShader(SpriteBatch.createDefaultShader());
	mSpriteBatch.enableBlending();
	mSpriteBatch.setBlendFunction(Config.Graphics.BLEND_SRC_FACTOR, Config.Graphics.BLEND_DST_FACTOR);
	mProjectionMatrixDefault.set(mSpriteBatch.getProjectionMatrix());
}

@Override
protected void onCreate() {
	super.onCreate();
	EventDispatcher.getInstance().connect(EventTypes.SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS, this);
}

@Override
protected void onResume(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onResume(outcome, message, loadingOutcome);

	mMusicPlayer.stop(MusicInterpolations.FADE_OUT);

	Actor.setEditorActive(true);

	if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
		mInvoker.dispose();

		ShaderProgram defaultShader = ResourceCacheFacade.get(InternalNames.SHADER_DEFAULT);
		if (defaultShader != null) {
			mShapeRenderer.setShader(defaultShader);
		}
	}
}

@Override
protected void onPause() {
	super.onPause();

	Actor.setEditorActive(false);
}

@Override
protected void onResize(int width, int height) {
	super.onResize(width, height);

	if (isInitialized()) {
		EditorGui gui = getGui();
		gui.resetCollisionBoxes();
	}
}

/**
 * Checks if the resource needs saving and then saves it if that's the case
 * @param deltaTime time elapsed since last frame
 */
@Override
protected void update(float deltaTime) {
	super.update(deltaTime);

	if (getDef() == null) {
		getGui().showFirstTimeMenu();
		return;
	}

	// Show info dialog after the resource has been created
	if (getDef().getName().equals(Config.Actor.NAME_DEFAULT)) {
		getGui().showInfoDialog();
	}

	if (shallAutoSave()) {
		saveDef();
	}

	if (!mCollisionBoxesBeenCreated) {
		mCollisionBoxesBeenCreated = true;
		getGui().resetCollisionBoxes();
	}
}

/**
 * @return true if the editor shall try to auto-save the current file
 */
protected boolean shallAutoSave() {
	if (!isSaved() && !isDrawing() && !isPublished()) {
		IC_General general = ConfigIni.getInstance().editor.general;

		float totalTimeElapsed = getGameTime().getTotalTimeElapsed();
		// Save after X seconds of inactivity or always save after Y minutes
		// regardless.
		if (totalTimeElapsed - mActivityTimeLast >= general.getAutoSaveTimeOnInactivity()) {
			return true;
		} else if (totalTimeElapsed - mUnsavedTime >= general.getAutoSaveTime()) {
			return true;
		}
	}
	return false;
}

@Override
public final void saveDef() {
	saveDef(null);
}

@Override
public final void saveDef(Command command) {
	if (!isSaved() && !isPublished()) {
		saveImpl(command);
	} else if (isPublished()) {
		mNotification.showHighlight("Cannot save a published " + getGui().getResourceTypeName());
	} else if (command != null) {
		command.execute();
	} else if (isSaved()) {
		mNotification.showSuccess("Saved...");
	}
}

@Override
public void loadDef() {
	SceneSwitcher.switchTo(ExploreFactory.create(mDefType, ExploreActions.LOAD));
}

@Override
public void duplicateDef() {
	if (!isSaved() && !isPublished()) {
		saveDef(new CEditorDuplicate(this));
	} else {
		getGui().showDuplicateDialog();
	}
}

@Override
public boolean isSaved() {
	return mSaved || isPublished();
}

/**
 * Set the editor as unsaved
 */
@Override
public void setUnsaved() {
	mSaved = false;
	mUnsavedTime = getGameTime().getTotalTimeElapsed();
	mActivityTimeLast = getGameTime().getTotalTimeElapsed();
}

@Override
public final ArrayList<Def> getNonPublishedDependencies() {
	Def def = getDef();
	if (def != null) {
		ArrayList<Def> resources = ResourceRepo.getNonPublishedDependencies(def);

		return resources;
	} else {
		return new ArrayList<>();
	}
}

@Override
public boolean isJustCreated() {
	return getDef() == null || !ResourceLocalRepo.exists(getDef().getId());
}

@Override
public void setGrid(boolean on) {
	mGridRender = on;
}

@Override
public boolean isGridOn() {
	return mGridRender;
}

/**
 * Called if the resources actually should be saved
 * @param command the command to execute after the save, null if none
 */
protected abstract void saveImpl(Command command);

@Override
protected void render() {
	postWebResponses();

	super.render();

	// Render saving actor
	if (mSaving) {
		saveResourceScreenshot();
	} else {
		// Render grid
		if (mGridRender) {
			renderGrid();
		}
	}
}

@Override
protected void onDestroy() {
	super.onDestroy();
	EventDispatcher.getInstance().disconnect(EventTypes.SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS, this);
}

/**
 * Post web responses in main thread
 */
private void postWebResponses() {
	while (!mWebResponses.isEmpty()) {
		try {
			WebWrapper webResponse = mWebResponses.take();
			handleWebResponseSyncronously(webResponse.method, webResponse.response);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

/**
 * Save resource screenshot
 */
private void saveResourceScreenshot() {
	// Only render if it has valid shape
	if (mSavingActorDef.getShape().isPolygonShapeValid()) {
		mShapeRenderer.setProjectionMatrix(mCamera.combined);
		mShapeRenderer.push(ShapeType.Filled);
		mSavingActor.renderShape(mShapeRenderer);
		mShapeRenderer.pop();
	}
	mSavingActor.dispose();
	mSavingActor = null;

	// Render saving images
	if (mSavingImages != null && mSavingImages.length > 0) {
		int imageSize = Config.Actor.SAVE_IMAGE_ON_ACTOR_SIZE;

		Camera camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.x = Gdx.graphics.getWidth() / 2;
		camera.position.y = Gdx.graphics.getHeight() / 2;
		camera.update();

		mSpriteBatch.setProjectionMatrix(camera.combined);
		mSpriteBatch.begin();
		for (ImageSaveOnActor imageSaveOnActor : mSavingImages) {
			int offsetX = 0;
			int offsetY = 0;

			switch (imageSaveOnActor.mLocation) {
			case BOTTOM_LEFT:
				// Does nothing
				break;

			case BOTTOM_RIGHT:
				offsetX = Config.Actor.SAVE_TEXTURE_SIZE - imageSize;
				break;

			case TOP_LEFT:
				offsetY = Config.Actor.SAVE_TEXTURE_SIZE - imageSize;
				break;

			case TOP_RIGHT:
				offsetX = Config.Actor.SAVE_TEXTURE_SIZE - imageSize;
				offsetY = Config.Actor.SAVE_TEXTURE_SIZE - imageSize;
				break;
			}

			mSpriteBatch.draw(imageSaveOnActor.mTextureRegion, offsetX, offsetY, imageSize, imageSize);
		}
		mSpriteBatch.end();
	}


	mSavingImages = null;

	// Take a screen shot
	Pixmap pixmap = Screens.getScreenshot(0, 0, Config.Actor.SAVE_TEXTURE_SIZE, Config.Actor.SAVE_TEXTURE_SIZE, true);

	// Make black color to alpha
	Graphics.pixmapReplaceColor(pixmap, COLOR_BLACK, COLOR_TRANSPARENT);


	// Convert to PNG
	try {
		byte[] pngBytes = PngExport.toPNG(pixmap);

		// Save texture to original definition
		mSavingActorDef.setPngImage(pngBytes);
	} catch (IOException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	}
	pixmap.dispose();

	// Save the file to file
	getGui().setVisible(true);
	mSaving = false;
	saveToFile();

	if (mExecutedAfterSaved != null) {
		mExecutedAfterSaved.execute();
		mExecutedAfterSaved = null;
	}
}

/**
 * Render the grid
 */
private void renderGrid() {
	enableBlendingWithDefaults();

	// Offset
	RenderOrders.offsetZValue(mShapeRenderer, RenderOrders.GRID_BELOW);

	mShapeRenderer.push(ShapeType.Line);

	Color color = SkinNames.getResource(SkinNames.EditorVars.GRID_COLOR);
	Color milestoneColor = SkinNames.getResource(SkinNames.EditorVars.GRID_MILESTONE_COLOR);

	Vector2 minWorldCoord = getWorldMinCoordinates();
	Vector2 maxWorldCoord = getWorldMaxCoordinates();

	// Round up/down the min/max coordinates to whole integer
	minWorldCoord.x = MathUtils.floor(minWorldCoord.x);
	minWorldCoord.y = MathUtils.floor(minWorldCoord.y);
	maxWorldCoord.x = MathUtils.ceil(maxWorldCoord.x);
	maxWorldCoord.y = MathUtils.ceil(maxWorldCoord.y);


	// Which coordinates should we show? If too close only show 10ths coordinates
	int showStep = Config.Editor.GRID_STEP_SIZE;
	float pixelsPerWorld = Gdx.graphics.getWidth() / (maxWorldCoord.x - minWorldCoord.x);
	if (pixelsPerWorld <= Config.Editor.GRID_SHOW_ONLY_MILESTONE_PIXELS_PER_WORLD) {
		showStep = Config.Editor.GRID_MILESTONE_STEP;
	}


	// Change so that we only show step size coordinates
	while (minWorldCoord.x % showStep != 0) {
		minWorldCoord.x -= 1;
	}
	while (minWorldCoord.y % showStep != 0) {
		minWorldCoord.y -= 1;
	}
	while (maxWorldCoord.x % showStep != 0) {
		maxWorldCoord.x += 1;
	}
	while (maxWorldCoord.y % showStep != 0) {
		maxWorldCoord.y += 1;
	}

	// Draw x lines
	for (int x = (int) minWorldCoord.x; x <= maxWorldCoord.x; x += showStep) {
		// Set color, every 5th is the milestone color
		if (x % Config.Editor.GRID_MILESTONE_STEP == 0) {
			mShapeRenderer.setColor(milestoneColor);
		} else {
			mShapeRenderer.setColor(color);
		}

		mShapeRenderer.line(x, minWorldCoord.y, x, maxWorldCoord.y);
	}

	// Draw y lines
	for (int y = (int) minWorldCoord.y; y <= maxWorldCoord.y; y += showStep) {
		// Set color, every 5th is the milestone color
		if (y % Config.Editor.GRID_MILESTONE_STEP == 0) {
			mShapeRenderer.setColor(milestoneColor);
		} else {
			mShapeRenderer.setColor(color);
		}

		mShapeRenderer.line(minWorldCoord.x, y, maxWorldCoord.x, y);
	}

	mShapeRenderer.pop();

	// Reset offset
	RenderOrders.resetZValueOffset(mShapeRenderer, RenderOrders.GRID_BELOW);
}

/**
 * Handle a web response in main thread synchronously
 */
protected void handleWebResponseSyncronously(IMethodEntity method, IEntity response) {
	// Publish
	if (response instanceof PublishResponse) {
		ProgressBar.hide();
		if (((PublishResponse) response).status == PublishResponse.Statuses.SUCCESS) {
			mNotification.show(NotificationTypes.SUCCESS, "Publish successful!");
			getGui().resetValues();
			mInvoker.dispose();
		} else {
			mNotification.show(NotificationTypes.ERROR, "Publish failed!");
		}
	}
}

/**
 * Called after the image for the actor definition has been created.
 */
protected abstract void saveToFile();

@Override
protected boolean onKeyDown(int keycode) {
	// Redo
	if (KeyHelper.isRedoPressed(keycode)) {
		mInvoker.redo();
		return true;
	}
	// Undo
	else if (KeyHelper.isUndoPressed(keycode)) {
		mInvoker.undo();
		return true;
	}
	// Back - main menu
	else if (KeyHelper.isBackPressed(keycode)) {
		if (!getGui().isDialogActive()) {
			getGui().showExitConfirmDialog();
			return true;
		}
	}

	// Hotkeys with control
	if (KeyHelper.isCtrlPressed()) {
		// Save
		if (keycode == Input.Keys.S) {
			saveDef();
		}
		// Open
		if (keycode == Input.Keys.O) {
			getGui().open();
		}
	}

	return super.onKeyDown(keycode);
}

@Override
public LoadingScene getLoadingScene() {
	return new LoadingProgressScene();
}

@Override
protected void loadResources() {
	super.loadResources();
	ResourceCacheFacade.load(this, InternalDeps.UI_EDITOR);
	ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
	ResourceCacheFacade.load(this, InternalNames.SHADER_DEFAULT);
}

/**
 * @return invoker for the editor
 */
@Override
public Invoker getInvoker() {
	return mInvoker;
}

@Override
protected EditorGui getGui() {
	return (EditorGui) super.getGui();
}

/**
 * Set the editor as saved
 */
protected void setSaved() {
	mSaved = true;
}

@Override
public void handleWrite(long mcWrittenBytes, long mcTotalBytes) {
	float percentage = 0;
	if (mcTotalBytes != 0) {
		percentage = (float) (((double) mcWrittenBytes) / mcTotalBytes) * 100;
	}

	ProgressBar.updateProgress(percentage);
}

/**
 * Shows syncing message
 */
protected void showSyncMessage() {
	if (User.getGlobalUser().isOnline()) {
		mNotification.show("Syncing...");
	}
}

@Override
public final void handleWebResponse(IMethodEntity method, IEntity response) {
	try {
		mWebResponses.put(new WebWrapper(method, response));
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
}

/**
 * Set the actor as saving. This will create an image of the actor
 * @param actorDef the actor definition to save
 * @param actor a new empty actor to use for creating a screen shot
 * @param images images to render on top of the saving actor
 */
protected void setSaving(ActorDef actorDef, Actor actor, ImageSaveOnActor... images) {
	setSaving(actorDef, actor, null, images);
}

/**
 * Set the actor as saving. This will create an image of the actor. After the resource is saved the
 * command will be executed
 * @param actorDef the actor definition to save
 * @param actor a new empty actor to use for creating a screen shot
 * @param command the command to be executed after the resource has been saved
 * @param images images to render on top of the saving actor
 */
protected void setSaving(ActorDef actorDef, Actor actor, Command command, ImageSaveOnActor... images) {
	if (!isPublished() && !isSaved()) {
		mSavingActorDef = actorDef;
		mSavingActor = actor;
		mSaving = true;
		mExecutedAfterSaved = command;
		mSavingImages = images;

		getGui().setVisible(false);
		createActorDefTexture();
	} else if (command != null) {
		command.execute();
	}
}

/**
 * Creates a texture out of the specified actor definition and sets it for the actor definition.
 */
private void createActorDefTexture() {
	float width = mSavingActorDef.getShape().getWidth();
	float height = mSavingActorDef.getShape().getHeight();

	// Skip if actor doesn't have a valid shape
	if (width == 0 || height == 0) {
		return;
	}

	// Create duplicate
	ActorDef copy = mSavingActorDef.copy();

	// Calculate how many world coordinates SAVE_TEXTURE_SIZE pixels is
	float worldScreenRatio = SceneSwitcher.getWorldScreenRatio();

	// Calculate normalize
	float normalizeLength = Config.Actor.SAVE_TEXTURE_SIZE / worldScreenRatio;
	if (width > height) {
		normalizeLength /= width;
	} else {
		normalizeLength /= height;
	}

	// Normalize width and height vertices to use SAVE_TEXTURE_SIZE pixels
	copy.getShape().setCenterOffset(0, 0);
	List<Vector2> triangleVertices = copy.getShape().getTriangleVertices();
	IdentityMap<Vector2, Vector2> scaledVertices = new IdentityMap<>();
	for (Vector2 vertex : triangleVertices) {
		if (!scaledVertices.containsKey(vertex)) {
			vertex.scl(normalizeLength);
			scaledVertices.put(vertex, vertex);
		}
	}

	List<Vector2> polygonVertices = copy.getShape().getPolygonShape();
	for (Vector2 vertex : polygonVertices) {
		vertex.scl(normalizeLength);
	}

	// Center to the rectangle screenshot area.
	Vector2 offset = new Vector2();
	offset.set(0, 0);
	copy.getShape().calculateBounds();
	height = copy.getShape().getHeight();
	float maxSize = Config.Actor.SAVE_TEXTURE_SIZE / worldScreenRatio;
	if (height < maxSize) {
		float offsetHeight = (maxSize - height) * 0.5f;
		offset.sub(0, offsetHeight);
	}
	width = copy.getShape().getWidth();
	if (width < maxSize) {
		float offsetWidth = (maxSize - width) * 0.5f;
		offset.sub(offsetWidth, 0);
	}

	// Calculate where to offset it so it's inside the screenshot area
	Vector2 minPos = new Vector2();
	minPos.set(Float.MAX_VALUE, Float.MAX_VALUE);
	float rotation = mSavingActorDef.getBodyDef().angle * MathUtils.radiansToDegrees;
	for (Vector2 vertex : polygonVertices) {
		vertex.rotate(rotation);
		if (vertex.x < minPos.x) {
			minPos.x = vertex.x;
		}
		if (vertex.y < minPos.y) {
			minPos.y = vertex.y;
		}
	}
	offset.add(minPos);

	// Offset with world coordinates
	Vector2 minScreenPos = SceneSwitcher.getWorldMinCoordinates();
	offset.set(minScreenPos.sub(offset));


	// Set actor def
	mSavingActor.setDef(copy);

	// Set position
	mSavingActor.setPosition(offset);
}

/**
 * @return true if the editor is currently saving an actor. I.e. creating a texture file for it.
 */
public boolean isSaving() {
	return mSaving;
}

/**
 * @return default projection matrix
 */
protected Matrix4 getProjectionMatrixDefault() {
	return mProjectionMatrixDefault;
}

@Override
public void onResourceChanged(IResource resource) {
	setUnsaved();
}

/**
 * Wrapper class for an image to be rendered on top of the saving actor
 */
protected static class ImageSaveOnActor {
	/** Image to render */
	private TextureRegion mTextureRegion;
	/** Location to render */
	private Locations mLocation;

	/**
	 * Sets the location and image to render
	 */
	protected ImageSaveOnActor(TextureRegion textureRegion, Locations location) {
		mTextureRegion = textureRegion;
		mLocation = location;
	}

	/** Various location to render the image */
	@SuppressWarnings("javadoc")
	protected enum Locations {
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT,
	}
}
}
