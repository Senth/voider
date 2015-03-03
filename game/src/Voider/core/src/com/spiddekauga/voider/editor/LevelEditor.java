package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Game;
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
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * The level editor scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LevelEditor extends Editor implements IResourceChangeEditor, ISelectionListener {
	/**
	 * Constructor for the level editor
	 */
	public LevelEditor() {
		super(new LevelEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_LEVEL_EDITOR);
		((LevelEditorGui) mGui).setLevelEditor(this);
	}

	@Override
	protected void onInit() {
		mDefaultTerrainColor.set((Color) SkinNames.getResource(SkinNames.EditorVars.TERRAIN_COLOR_DEFAULT));
		mDefaultTerrainColor.a = SkinNames.getResource(SkinNames.EditorVars.TERRAIN_ALPHA_DEFAULT);

		mSelection = new Selection();
		mSelection.addListener(this);

		super.onInit();

		mZoomTool = new ZoomTool(this, Config.Editor.Level.ZOOM_MIN, Config.Editor.Level.ZOOM_MAX);

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
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		mGui.dispose();
		mGui.initGui();
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
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mLevel == null) {
			return;
		}

		mLevel.update(deltaTime);

		((PanTool) Tools.PAN.getTool()).update(deltaTime);
	}

	@Override
	protected void render() {
		if (mLevel == null) {
			return;
		}

		if (Config.Graphics.USE_RELEASE_RENDERER) {
			enableBlendingWithDefaults();
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
			mLevel.render(mShapeRenderer);
			mLevel.renderEditor(mShapeRenderer);
			mShapeRenderer.flush();
			renderAboveBelowBorders();
			mShapeRenderer.pop();


			mSpriteBatch.setProjectionMatrix(mCamera.combined);
			mSpriteBatch.begin();
			mLevel.renderSprite(mSpriteBatch);
			mSpriteBatch.end();
		}
	}

	/**
	 * Renders the background if it should
	 */
	private void renderBackground() {
		if (mShowBackground) {
			if (mBackgroundBottom == null) {
				createBackground();
			}

			mSpriteBatch.setProjectionMatrix(mCamera.combined);
			mSpriteBatch.begin();

			IC_Game game = ConfigIni.getInstance().game;
			renderBackground(mBackgroundBottom, game.getLayerBottomSpeed());
			renderBackground(mBackgroundTop, game.getLayerTopSpeed());

			mSpriteBatch.end();
		}
	}

	/**
	 * Render a specific background
	 * @param background the background to render
	 * @param layerSpeed relative speed of the background
	 */
	private void renderBackground(Texture background, float layerSpeed) {
		// Offset in screen coordinates
		float layerOffset = mCamera.position.x / getScreenToWorldScale() * layerSpeed;

		// Get shown height
		float renderHeightDefault = Gdx.graphics.getHeight() * Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE_INVERT;
		float renderHeight = renderHeightDefault * getScreenToWorldScale();

		// Texture scaling
		float textureScale = renderHeightDefault / background.getHeight();
		float width = Gdx.graphics.getWidth() / (background.getWidth() * textureScale) * mCamera.zoom;
		float startX = layerOffset / background.getWidth();

		// Position
		float offsetY = (Gdx.graphics.getHeight() - renderHeightDefault) / 2;
		float x = mCamera.position.x - mCamera.viewportWidth * mCamera.zoom / 2;
		float y = -mCamera.viewportHeight / 2 + offsetY * getScreenToWorldScale();

		// Draw
		mSpriteBatch.draw(background, x, y, mCamera.viewportWidth * mCamera.zoom, renderHeight, startX, 0, startX + width, 1);
	}

	/**
	 * Create level background
	 */
	private void createBackground() {
		Themes currentTheme = mLevel.getLevelDef().getTheme();
		mBackgroundBottom = ResourceCacheFacade.get(currentTheme.getBottomLayer());
		mBackgroundTop = ResourceCacheFacade.get(currentTheme.getTopLayer());
	}

	@Override
	public boolean isDrawing() {
		if (mTool.getTool() != null) {
			return mTool.getTool().isDrawing();
		}
		return false;
	}

	/**
	 * Sets the level that shall be played and resets tools, invoker, etc.
	 * @param level level to play
	 */
	private void setLevel(Level level) {
		boolean sameLevel = false;
		boolean sameRevision = false;

		boolean oldIsPublished = isPublished();

		mBackgroundBottom = null;
		mBackgroundTop = null;

		if (mLevel != null) {
			if (level != null && mLevel.equals(level.getId())) {
				sameLevel = true;
				if (mLevel.equals(level)) {
					sameRevision = true;
				}
			}

			// Unload the old level
			if (ResourceCacheFacade.isLoaded(mLevel.getId()) && !sameRevision) {
				ResourceCacheFacade.unload(mLevel.getId(), mLevel.getRevision());
			} else {
				mLevel.dispose();
			}
		}

		mLevel = level;

		if (mLevel != null) {
			// Reset camera position to the start
			if (!sameLevel) {
				mCamera.position.x = mLevel.getLevelDef().getStartXCoord() + mCamera.viewportWidth * 0.5f;
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
		mGui.resetValues();

		mInvoker.dispose();

		Actor.setLevel(mLevel);
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

	// --------------------------------
	// Resource loading etc.
	// --------------------------------
	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.PICKUP_DEF, true);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.LEVEL_DEF, false);

		ResourceCacheFacade.load(InternalDeps.MUSIC_LEVEL_THEMES);
		ResourceCacheFacade.load(InternalDeps.THEME_ALL);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();

		ResourceCacheFacade.unload(InternalDeps.MUSIC_LEVEL_THEMES);
		ResourceCacheFacade.unload(InternalDeps.THEME_ALL);
	}

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

	};

	@Override
	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
		super.reloadResourcesOnActivate(outcome, message);

		// Check if we have created any new enemies, load them in that case
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		// Check so that all resources have been loaded
		if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
			// Loading a level
			if (mLoadingLevel != null) {
				Level loadedLevel;
				loadedLevel = ResourceCacheFacade.get(mLoadingLevel.getLevelId());
				if (loadedLevel != null) {
					setLevel(loadedLevel);
					switchTool(Tools.SELECTION);
					((LevelEditorGui) mGui).resetTools();
					mGui.hideMsgBoxes();
					setSaved();
					mLoadingLevel = null;
				} else {
					Gdx.app.error("LevelEditor", "Could not find level (" + mLoadingLevel.getLevelId() + ")");
				}
			}
		} else if (outcome == Outcomes.EXPLORE_SELECT) {
			// mInvoker.execute(new CLevelPickupDefSelect(((ResourceItem) message).id,
			// this));

			if (message instanceof EnemyDefEntity) {
				mInvoker.execute(new CLevelEnemyDefAdd(((EnemyDefEntity) message).resourceId, this));
			}
		} else if (outcome == Outcomes.EXPLORE_LOAD) {
			mGui.hideMsgBoxes();

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
			mGui.hideMsgBoxes();
		}
		// Set as unsaved if took screenshot
		else if (outcome == Outcomes.LEVEL_PLAYER_DIED || outcome == Outcomes.LEVEL_COMPLETED || outcome == Outcomes.LEVEL_QUIT) {
			if (mPngBytesBeforeTest != mLevel.getDef().getPngImage()) {
				setUnsaved();
				((LevelEditorGui) mGui).resetImage();
			}
		}
		// Theme selected
		else if (outcome == Outcomes.THEME_SELECTED) {
			Themes theme = (Themes) message;
			setTheme(theme);
			mGui.hideMsgBoxActive();
		}

		if (mLevel != null) {
			Actor.setLevel(mLevel);
		}
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
					mAddEnemies.add(0, enemyActorDef);
					((LevelEditorGui) mGui).resetEnemyAddTable();
					return true;
				} else {
					mNotification.show("This enemy has already been added.");
				}
			}
		}

		return false;
	}

	/**
	 * Adds a new enemy to the add enemy table
	 * @param enemyDef the enemy definition to add to the add list
	 */
	private void addEnemyDef(EnemyActorDef enemyDef) {
		if (enemyDef != null) {
			if (!mAddEnemies.contains(enemyDef)) {
				mAddEnemies.add(0, enemyDef);
				((LevelEditorGui) mGui).resetEnemyAddTable();
			}
		}
	}

	/**
	 * Removes an enemy from teh add enemy table
	 * @param enemyId the enemy id to remove.
	 * @return true if the enemy was successfully removed, false if it was not found.
	 */
	public boolean removeEnemyDef(UUID enemyId) {
		if (enemyId != null) {
			EnemyActorDef enemyActorDef = ResourceCacheFacade.get(enemyId);
			if (enemyActorDef != null) {
				boolean removed = mAddEnemies.remove(enemyActorDef);

				if (removed) {
					((LevelEditorGui) mGui).resetEnemyAddTable();
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Clears all enemy definitions
	 */
	private void clearEnemyDef() {
		mAddEnemies.clear();
	}

	/**
	 * Selects the specified pickup definition. This pickup will be used when adding new
	 * pickups
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
			mGui.resetValues();
		} catch (Exception e) {
			Gdx.app.error("LevelEditor", e.toString());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	protected void onDispose() {
		super.onDispose();

		setLevel(null);
	}

	@Override
	public void onResourceAdded(IResource resource) {
		mGui.resetValues();
		mLevel.addResource(resource);

		// Set default color
		if (resource instanceof StaticTerrainActor) {
			((StaticTerrainActor) resource).getDef().getVisual().setColor(mDefaultTerrainColor);
		}

		setUnsaved();
	}

	@Override
	public void onResourceRemoved(IResource resource) {
		mGui.resetValues();
		mLevel.removeResource(resource.getId());
		setUnsaved();
	}

	@Override
	public void onResourceChanged(IResource resource) {
		setUnsaved();

		if (resource instanceof EnemyActor) {
			((LevelEditorGui) mGui).resetEnemyOptions();
		}
	}

	@Override
	public void onResourceSelected(IResource resource) {
		mGui.resetValues();
	}

	@Override
	public void onResourceDeselected(IResource resource) {
		mGui.resetValues();
	}

	/**
	 * @return true if an enemy is currently selected
	 */
	boolean isEnemySelected() {
		if (mSelection != null) {
			return mSelection.isSelected(EnemyActor.class);
		} else {
			return false;
		}
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

		// Testing
		if (Debug.isBuildOrBelow(Builds.DEV_LOCAL)) {
			// Update theme
			if (Keys.F12 == keycode) {
				((LevelEditorGui) mGui).resetTheme();
			}
		}

		return super.onKeyDown(keycode);
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

		((LevelEditorGui) mGui).resetColor();
	}

	/**
	 * Checks if the tool should be available in the background
	 * @param tool
	 * @return true if the tool should be available when inactive (but with limited
	 *         functionality)
	 */
	private boolean isAvailableWhenInactive(Tools tool) {
		switch (tool) {
		case SELECTION:
		case DELETE:
		case PAN:
		case ZOOM_IN:
		case ZOOM_OUT:
			return true;

		default:
			return false;
		}
	}

	/**
	 * @return selected tool
	 */
	Tools getSelectedTool() {
		return mTool;
	}

	/**
	 * Tests to run a game from the current location
	 * @param invulnerable makes the player invulnerable
	 */
	public void runFromHere(boolean invulnerable) {
		if (!isSaved()) {
			saveDef();
		}

		boolean testRun = !ResourceLocalRepo.isPublished(mLevel.getId());
		GameScene testGame = new GameScene(testRun, invulnerable);
		Level copyLevel = mLevel.copy();
		// Because of scaling decrease the x position
		float xPosition = getRunFromHerePosition();
		copyLevel.setStartPosition(xPosition);
		copyLevel.calculateEndPosition();

		testGame.setLevelToRun(copyLevel);

		mPngBytesBeforeTest = copyLevel.getDef().getPngImage();

		// Remove screen triggers before the specified coordinate
		ArrayList<TScreenAt> triggers = copyLevel.getResources(TScreenAt.class);
		for (TScreenAt trigger : triggers) {
			if (trigger.isTriggered()) {
				copyLevel.removeResource(trigger.getId());
			}
		}

		SceneSwitcher.switchTo(testGame);
	}

	/**
	 * Calculates the current starting position when test running
	 * @return start position of the level when test running from here
	 */
	public float getRunFromHerePosition() {
		return mCamera.position.x + mCamera.viewportWidth * 0.5f - mCamera.viewportWidth * Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE_INVERT;
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

	@Override
	public void saveDef() {
		saveToFile();
	}

	@Override
	public void saveDef(Command command) {
		saveToFile();
		if (command != null) {
			command.execute();
		}
	}

	@Override
	protected void saveToFile() {
		if (!isPublished() && !isSaved()) {
			mLevel.calculateStartEndPosition();

			int oldRevision = mLevel.getRevision();
			mResourceRepo.save(mLevel.getDef(), mLevel);
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
		}

		setSaved();
	}

	@Override
	public void loadDef() {
		SceneSwitcher.switchTo(ExploreFactory.create(LevelDef.class, ExploreActions.LOAD));
		setSaved();
	}

	@Override
	public void newDef() {
		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		setLevel(level);
		setSaved();
	}

	@Override
	public void duplicateDef() {
		Level level = mLevel.copyNewResource();

		setLevel(level);

		mGui.resetValues();
		mInvoker.dispose();
		saveDef();
	}

	@Override
	public void publishDef() {
		if (mLevel != null) {
			mGui.showProgressBar("Uploading...");
			mResourceRepo.publish(this, this, mLevel);
		}
	}

	@Override
	public void handleWrite(long mcWrittenBytes, long mcTotalBytes) {
		float percentage = 0;
		if (mcTotalBytes != 0) {
			percentage = (float) (((double) mcWrittenBytes) / mcTotalBytes) * 100;
		}

		mGui.updateProgressBar(percentage);
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
	public void handleWebResponseSyncronously(IMethodEntity method, IEntity response) {
		super.handleWebResponseSyncronously(method, response);

		// Publish -> Update available tools
		if (response instanceof PublishResponse) {
			if (((PublishResponse) response).status == PublishResponse.Statuses.SUCCESS) {
				updateAvailableTools(false, true);
			}
		}
	}

	/**
	 * Sets the starting speed of the current level
	 * @param speed starting speed of the current level
	 */
	void setLevelStartingSpeed(float speed) {
		if (mLevel != null) {
			mLevel.getLevelDef().setBaseSpeed(speed);
			setUnsaved();
		}
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
	 * @return current revision of the level, empty string if no level is available
	 */
	String getLevelRevision() {
		if (mLevel != null) {
			return String.valueOf(mLevel.getRevision());
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
			((EditorGui) mGui).resetName();
			setUnsaved();
		}
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
	 * @return story that will be displayed before the level, empty string if no level is
	 *         available
	 */
	String getPrologue() {
		if (mLevel != null) {
			return mLevel.getLevelDef().getPrologue();
		} else {
			return "";
		}
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
	 * @return story that will be displayed after completing the level, empty string if no
	 *         level is available
	 */
	String getEpilogue() {
		if (mLevel != null) {
			return mLevel.getLevelDef().getEpilogue();
		}
		return "";
	}

	/**
	 * @return true if screenshot has been taken for the level
	 */
	boolean hasScreenshot() {
		if (mLevel != null) {
			return mLevel.getDef().getPngImage() != null;
		}
		return false;
	}

	/**
	 * Sets the color of the selected terrain
	 * @param color new color of the terrain
	 */
	void setSelectedTerrainColor(Color color) {
		ArrayList<StaticTerrainActor> terrains = mSelection.getSelectedResourcesOfType(StaticTerrainActor.class);

		for (StaticTerrainActor terrain : terrains) {
			Color terrainColor = terrain.getDef().getVisual().getColor();
			terrainColor.r = color.r;
			terrainColor.g = color.g;
			terrainColor.b = color.b;
		}
	}

	/**
	 * @return color of the selected terrain. If multiple terrains are selected and have
	 *         different colors, null is returned. If no terrain is selected the default
	 *         color is returned.
	 */
	Color getSelectedTerrainColor() {
		ArrayList<StaticTerrainActor> terrains = mSelection.getSelectedResourcesOfType(StaticTerrainActor.class);


		if (!terrains.isEmpty()) {
			Color terrainColor = new Color(terrains.get(0).getDef().getVisual().getColor());
			terrainColor.a = 1;

			// Check for different colors -> Return default color
			Color testColor = new Color();
			for (StaticTerrainActor terrain : terrains) {
				testColor.set(terrain.getDef().getVisual().getColor());
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
	 * Sets the opacity of the selected terrain
	 * @param opacity new transparency level of the terrain, should be between 0-100
	 */
	void setSelectedTerrainOpacity(float opacity) {
		ArrayList<StaticTerrainActor> terrains = mSelection.getSelectedResourcesOfType(StaticTerrainActor.class);

		for (StaticTerrainActor terrain : terrains) {
			terrain.getDef().getVisual().getColor().a = opacity / 100f;
		}
	}

	/**
	 * @return opacity of the selected terrain. -1 if multiple terrains are selected and
	 *         have different opacity. The default opacity is return when no terrain is
	 *         selected.
	 */
	float getSelectedTerrainOpacity() {
		ArrayList<StaticTerrainActor> terrains = mSelection.getSelectedResourcesOfType(StaticTerrainActor.class);

		if (!terrains.isEmpty()) {
			float opacity = terrains.get(0).getDef().getVisual().getColor().a;

			// Check for different colors -> Return default color
			for (StaticTerrainActor terrain : terrains) {
				if (opacity != terrain.getDef().getVisual().getColor().a) {
					return -1;
				}
			}

			return opacity * 100;
		}

		return mDefaultTerrainColor.a * 100;
	}

	/**
	 * Sets the default color for new terrain. Doesn't use alpha value
	 * @param color default color for new terrains
	 */
	void setDefaultTerrainColor(Color color) {
		mDefaultTerrainColor.set(color.r, color.g, color.b, mDefaultTerrainColor.a);
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
	 * Sets the default opacity/alpha value for new terrain.
	 * @param opacity transparency of new terrains, should be from 0-100
	 */
	void setDefaultTerrainOpacity(float opacity) {
		mDefaultTerrainColor.a = opacity / 100f;
	}

	/**
	 * @return default opacity value for new terrain
	 */
	float getDefaultTerrainOpacity() {
		return mDefaultTerrainColor.a * 100;
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

		((LevelEditorGui) mGui).resetEnemyOptions();
		setUnsaved();
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
	 * Clears the selection
	 */
	void clearSelection() {
		mSelection.clearSelection();
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
	 * @return spawn delay between actors in the same group, negative value if no group
	 *         exist
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
	 * Sets the path type of the selected path
	 * @param pathType type of the path
	 */
	void setPathType(Path.PathTypes pathType) {
		ArrayList<Path> selectedPaths = mSelection.getSelectedResourcesOfType(Path.class);

		for (Path path : selectedPaths) {
			path.setPathType(pathType);
		}

		mGui.resetValues();
	}

	/**
	 * @return current path type. If several paths are selected and they have different
	 *         path types null is return. If no path is selected null is also returned.
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
	 * @return true if the selected enemy has an activation trigger, false if not or if no
	 *         enemy is selected
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
	 * @return delay of the activation trigger, negative if no activation trigger has been
	 *         set.
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
	 * @return true if the selected enemy has an deactivation trigger, false if not or if
	 *         no enemy is selected
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
	 * @return delay of the deactivation trigger, negative if no deactivation trigger has
	 *         been set.
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
	 * Sets if enemies that will be used when test running the level from here should be
	 * highlighted.
	 * @param highlight set to true to highlight the enemies
	 */
	void setEnemyHighlight(boolean highlight) {
		mEnemyHighlight = highlight;
	}

	/**
	 * @return true if the enemy should be highlighted if the will be used when test
	 *         running a level from the current position
	 */
	public boolean isEnemyHighlightOn() {
		return mEnemyHighlight;
	}

	/**
	 * Sets if the level background should be rendered
	 * @param show set to true to show the background
	 */
	void setShowBackground(boolean show) {
		mShowBackground = show;
	}

	/**
	 * @return true if the background is shown
	 */
	boolean isBackgroundShown() {
		return mShowBackground;
	}

	/**
	 * @return current level
	 */
	public Level getLevel() {
		return mLevel;
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
		ZOOM_OUT,

		;

		/**
		 * Sets the actual tool
		 * @param tool the actual tool that is used
		 */
		public void setTool(TouchTool tool) {
			mTool = tool;
		}

		/**
		 * @return the actual tool used in the editor
		 */
		public TouchTool getTool() {
			return mTool;
		}

		/** The actual tool */
		private TouchTool mTool = null;
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
	 * @return all enemies for the add table
	 */
	ArrayList<EnemyActorDef> getAddEnemies() {
		return mAddEnemies;
	}

	/**
	 * Called when selecting the enemy type
	 * @param enemyDef the enemy type to create
	 */
	void createNewEnemy(EnemyActorDef enemyDef) {
		((EnemyAddTool) Tools.ENEMY_ADD.mTool).setActorDef(enemyDef);
	}

	/**
	 * Set the theme for the level
	 * @param theme the theme for the level
	 */
	void setTheme(Themes theme) {
		if (mLevel != null) {
			mLevel.getLevelDef().setTheme(theme);
			setUnsaved();
			((LevelEditorGui) mGui).resetTheme();
			mBackgroundBottom = null;
			mBackgroundTop = null;
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
	 * Sets the music for the level
	 * @param music the music for the level
	 */
	void setMusic(Music music) {
		if (mLevel != null) {
			mLevel.getLevelDef().setMusic(music);
			setUnsaved();
			((LevelEditorGui) mGui).resetMusic();
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

	@Override
	public void undoJustCreated() {
		setLevel(null);
	}

	@Override
	public Def getDef() {
		if (mLevel != null) {
			return mLevel.getDef();
		}
		return null;
	}

	private Texture mBackgroundBottom = null;
	private Texture mBackgroundTop = null;
	private byte[] mPngBytesBeforeTest = null;
	private boolean mShowBackground = true;
	private ArrayList<EnemyActorDef> mAddEnemies = new ArrayList<>();
	private Level mLevel = null;
	private LevelDef mLoadingLevel = null;
	private ISelection mSelection = null;
	private Tools mTool = Tools.SELECTION;
	private ZoomTool mZoomTool = null;
	private boolean mEnemyHighlight = true;
	private Color mDefaultTerrainColor = new Color();
	/** Resource repository */
	protected ResourceRepo mResourceRepo = ResourceRepo.getInstance();
}