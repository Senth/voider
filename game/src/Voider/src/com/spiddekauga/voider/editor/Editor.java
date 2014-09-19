package com.spiddekauga.voider.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IdentityMap;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.PngExport;
import com.spiddekauga.utils.Screens;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.PublishMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.WorldScene;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.User;

/**
 * Common class for all editors
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class Editor extends WorldScene implements IEditor, IResponseListener, IOutstreamProgressListener, Observer {

	/**
	 * @param gui GUI to be used with the editor
	 * @param pickRadius picking radius of the editor
	 */
	public Editor(Gui gui, float pickRadius) {
		super(gui, pickRadius);
	}

	/**
	 * @return true if the editor shall try to auto-save the current file
	 */
	protected boolean shallAutoSave() {
		if (!mSaved && !isDrawing() && !isPublished()) {

			float totalTimeElapsed = getGameTime().getTotalTimeElapsed();
			// Save after X seconds of inactivity or always save after Y minutes
			// regardless.
			if (totalTimeElapsed - mActivityTimeLast >= Config.Editor.AUTO_SAVE_TIME_ON_INACTIVITY) {
				return true;
			} else if (totalTimeElapsed - mUnsavedTime >= Config.Editor.AUTO_SAVE_TIME_FORCED) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(InternalNames.UI_EDITOR);
		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.load(InternalNames.SHADER_DEFAULT);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.UI_EDITOR);
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
		ResourceCacheFacade.unload(InternalNames.SHADER_DEFAULT);
	}

	@Override
	protected void onInit() {
		super.onInit();
		mSynchronizer.addObserver(this);
	}

	@Override
	protected void onDispose() {
		super.onDispose();
		mSynchronizer.deleteObserver(this);
	}

	/**
	 * Checks if the resource needs saving and then saves it if that's the case
	 * @param deltaTime time elapsed since last frame
	 */
	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (getDef() == null) {
			((EditorGui) mGui).showFirstTimeMenu();
			return;
		}

		// Show info dialog after the resource has been created
		if (getDef().getName().equals(Config.Actor.NAME_DEFAULT)) {
			((EditorGui) mGui).showInfoDialog();
		}

		if (shallAutoSave()) {
			saveDef();
		}
	}

	@Override
	public boolean onKeyDown(int keycode) {
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
			if (!mGui.isMsgBoxActive()) {
				((EditorGui) mGui).showExitConfirmDialog();
				return true;
			} else {
				/** @todo close message box */
				return true;
			}
		}

		return false;
	}

	@Override
	protected void render() {
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

	/**
	 * Render the grid
	 */
	private void renderGrid() {
		// Offset
		if (mGridRenderAboveResources) {
			RenderOrders.offsetZValue(mShapeRenderer, RenderOrders.GRID_ABOVE);
		} else {
			RenderOrders.offsetZValue(mShapeRenderer, RenderOrders.GRID_BELOW);
		}

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

		// Change so that we only show even coordinates
		if (minWorldCoord.x % 2 != 0) {
			minWorldCoord.x -= 1;
		}
		if (minWorldCoord.y % 2 != 0) {
			minWorldCoord.y -= 1;
		}
		if (maxWorldCoord.x % 2 != 0) {
			maxWorldCoord.x += 1;
		}
		if (maxWorldCoord.y % 2 != 0) {
			maxWorldCoord.y += 1;
		}

		// Draw x lines
		for (int x = (int) minWorldCoord.x; x <= maxWorldCoord.x; x += 2) {
			// Set color, every 5th is the milestone color
			if (x % 10 == 0) {
				mShapeRenderer.setColor(milestoneColor);
			} else {
				mShapeRenderer.setColor(color);
			}

			mShapeRenderer.line(x, minWorldCoord.y, x, maxWorldCoord.y);
		}

		// Draw y lines
		for (int y = (int) minWorldCoord.y; y <= maxWorldCoord.y; y += 2) {
			// Set color, every 5th is the milestone color
			if (y % 10 == 0) {
				mShapeRenderer.setColor(milestoneColor);
			} else {
				mShapeRenderer.setColor(color);
			}

			mShapeRenderer.line(minWorldCoord.x, y, maxWorldCoord.x, y);
		}

		mShapeRenderer.pop();

		// Reset offset
		if (mGridRenderAboveResources) {
			RenderOrders.resetZValueOffset(mShapeRenderer, RenderOrders.GRID_ABOVE);
		} else {
			RenderOrders.resetZValueOffset(mShapeRenderer, RenderOrders.GRID_BELOW);
		}
	}

	/**
	 * Save resource screenshot
	 */
	private void saveResourceScreenshot() {
		// Only render if it has valid shape
		if (mSavingActorDef.getVisualVars().isPolygonShapeValid()) {
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			mSavingActor.render(mShapeRenderer);
			mShapeRenderer.pop();
		}
		mSavingActor.dispose();
		mSavingActor = null;

		// Take a 200x200 screen shot
		Pixmap pixmap = Screens.getScreenshot(0, 0, Config.Actor.SAVE_TEXTURE_SIZE, Config.Actor.SAVE_TEXTURE_SIZE, true);

		// Make black color to alpha
		pixmap.getPixel(0, 0);
		for (int x = 0; x < pixmap.getWidth(); ++x) {
			for (int y = 0; y < pixmap.getHeight(); ++y) {
				if (isColorBlack(pixmap.getPixel(x, y))) {
					pixmap.drawPixel(x, y, COLOR_TRANSPARENT);
				}
			}
		}

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
		mGui.setVisible(true);
		mSaving = false;
		saveToFile();

		if (mExecutedAfterSaved != null) {
			mExecutedAfterSaved.execute();
			mExecutedAfterSaved = null;
		}
	}

	/**
	 * @param color the color to check if it's black (uses RGBA8888)
	 * @return true if the color is black
	 */
	private boolean isColorBlack(int color) {
		return (color & COLOR_BLACK) == 0;
	}

	@Override
	public boolean isSaved() {
		return mSaved;
	}

	/**
	 * Set the editor as saved
	 */
	protected void setSaved() {
		mSaved = true;
	}

	/**
	 * Set the editor as unsaved
	 */
	protected void setUnsaved() {
		mSaved = false;
		mUnsavedTime = getGameTime().getTotalTimeElapsed();
		mActivityTimeLast = getGameTime().getTotalTimeElapsed();
	}

	/**
	 * @return invoker for the editor
	 */
	@Override
	public Invoker getInvoker() {
		return mInvoker;
	}

	@Override
	public void handleWrite(long mcWrittenBytes, long mcTotalBytes) {
		float percentage = 0;
		if (mcTotalBytes != 0) {
			percentage = (float) (((double) mcWrittenBytes) / mcTotalBytes) * 100;
		}

		mGui.updateProgressBar(percentage);
	}

	/**
	 * Shows syncing message
	 */
	protected void showSyncMessage() {
		if (User.getGlobalUser().isOnline()) {
			mGui.showMessage("Syncing...");
		}
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// Publish
		if (response instanceof PublishMethodResponse) {
			mGui.hideProgressBar();
			if (((PublishMethodResponse) response).status == PublishMethodResponse.Statuses.SUCCESS) {
				mGui.showSuccessMessage("Publish successful!");
				mGui.resetValues();
				mInvoker.dispose();
			} else {
				mGui.showErrorMessage("Publish failed!");
			}
		}
	}

	/**
	 * Creates a texture out of the specified actor definition and sets it for the actor
	 * definition.
	 */
	private void createActorDefTexture() {
		float width = mSavingActorDef.getWidth();
		float height = mSavingActorDef.getHeight();

		// Skip if actor doesn't have a valid shape
		if (width == 0 || height == 0) {
			return;
		}

		// Create duplicate
		ActorDef copy = mSavingActorDef.copy();

		// Calculate how many world coordinates 200px is
		float worldScreenRatio = SceneSwitcher.getWorldScreenRatio();

		// Calculate normalize
		float normalizeLength = Config.Actor.SAVE_TEXTURE_SIZE / worldScreenRatio;
		if (width > height) {
			normalizeLength /= width;
		} else {
			normalizeLength /= height;
		}

		// Normalize width and height vertices to use 200px
		copy.getVisualVars().setCenterOffset(0, 0);
		ArrayList<Vector2> triangleVertices = copy.getVisualVars().getTriangleVertices();
		@SuppressWarnings("unchecked")
		IdentityMap<Vector2, Vector2> scaledVertices = Pools.identityMap.obtain();
		for (Vector2 vertex : triangleVertices) {
			if (!scaledVertices.containsKey(vertex)) {
				vertex.scl(normalizeLength);
				scaledVertices.put(vertex, vertex);
			}
		}
		Pools.identityMap.free(scaledVertices);

		ArrayList<Vector2> polygonVertices = copy.getVisualVars().getPolygonShape();
		for (Vector2 vertex : polygonVertices) {
			vertex.scl(normalizeLength);
		}

		// Center to the rectangle screenshot area.
		Vector2 offset = Pools.vector2.obtain();
		offset.set(0, 0);
		height = copy.getHeight();
		float maxSize = Config.Actor.SAVE_TEXTURE_SIZE / worldScreenRatio;
		if (height < maxSize) {
			float offsetHeight = (maxSize - height) * 0.5f;
			offset.sub(0, offsetHeight);
		}
		width = copy.getWidth();
		if (width < maxSize) {
			float offsetWidth = (maxSize - width) * 0.5f;
			offset.sub(offsetWidth, 0);
		}

		// Calculate where to offset it so it's inside the screenshot area
		Vector2 minPos = Pools.vector2.obtain();
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

		Pools.vector2.freeAll(offset, minScreenPos);
	}

	/**
	 * Set the actor as saving. This will create an image of the actor
	 * @param actorDef the actor definition to save
	 * @param actor a new empty actor to use for creating a screen shot
	 */
	protected void setSaving(ActorDef actorDef, Actor actor) {
		setSaving(actorDef, actor, null);
	}

	/**
	 * Set the actor as saving. This will create an image of the actor. After the resource
	 * is saved the command will be executed
	 * @param actorDef the actor definition to save
	 * @param actor a new empty actor to use for creating a screen shot
	 * @param command the command to be executed after the resource has been saved
	 */
	protected void setSaving(ActorDef actorDef, Actor actor, Command command) {
		if (!isPublished() && !isSaved()) {
			mSavingActorDef = actorDef;
			mSavingActor = actor;
			mSaving = true;
			mExecutedAfterSaved = command;

			mGui.setVisible(false);
			createActorDefTexture();
		}
	}

	/**
	 * @return true if the editor is currently saving an actor. I.e. creating a texture
	 *         file for it.
	 */
	public boolean isSaving() {
		return mSaving;
	}

	@Override
	public void setGrid(boolean on) {
		mGridRender = on;
	}

	@Override
	public boolean isGridOn() {
		return mGridRender;
	}

	@Override
	public void setGridRenderAboveResources(boolean above) {
		mGridRenderAboveResources = above;
	}

	@Override
	public boolean isGridRenderAboveResources() {
		return mGridRenderAboveResources;
	}

	@Override
	public boolean isJustCreated() {
		return getDef() == null || !ResourceLocalRepo.exists(getDef().getId());
	}

	/**
	 * Called when after the image for the actor definition has been created.
	 */
	protected abstract void saveToFile();

	/** Resource repo */
	protected ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** Invoker */
	protected Invoker mInvoker = new Invoker();
	/** Is the resource currently saved? */
	private boolean mSaved = false;
	/** Is the resource currently saving, this means it takes a screenshot of the image */
	private boolean mSaving = false;
	/** Saving actor definition */
	private ActorDef mSavingActorDef = null;
	/** Saving actor */
	private Actor mSavingActor = null;
	/** When the resource became unsaved */
	private float mUnsavedTime = 0;
	/** Last time the player did some activity */
	private float mActivityTimeLast = 0;
	/** Command to be executed after the resource has been saved */
	private Command mExecutedAfterSaved = null;
	/** If grid shall be rendered */
	private boolean mGridRender = true;
	/** If grid shall be rendered in front of the resources */
	private boolean mGridRenderAboveResources = false;

	/** Synchronizer */
	protected static Synchronizer mSynchronizer = Synchronizer.getInstance();
	/** Transparent color */
	private static final int COLOR_TRANSPARENT = 0x00000000;
	/** Black color */
	private static final int COLOR_BLACK = 0xFFFFFFFF;
}
