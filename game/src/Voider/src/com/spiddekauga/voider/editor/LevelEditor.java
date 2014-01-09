package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Scroller;
import com.spiddekauga.utils.Scroller.ScrollAxis;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.editor.commands.CCameraMove;
import com.spiddekauga.voider.editor.commands.CLevelEnemyDefAdd;
import com.spiddekauga.voider.editor.commands.CLevelPickupDefSelect;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.editor.tools.ActorAddTool;
import com.spiddekauga.voider.editor.tools.AddMoveCornerTool;
import com.spiddekauga.voider.editor.tools.DeleteTool;
import com.spiddekauga.voider.editor.tools.DrawAppendTool;
import com.spiddekauga.voider.editor.tools.DrawEraseTool;
import com.spiddekauga.voider.editor.tools.EnemyAddTool;
import com.spiddekauga.voider.editor.tools.EnemySetTriggerTool;
import com.spiddekauga.voider.editor.tools.ISelection;
import com.spiddekauga.voider.editor.tools.ISelectionListener;
import com.spiddekauga.voider.editor.tools.MoveTool;
import com.spiddekauga.voider.editor.tools.PathAddTool;
import com.spiddekauga.voider.editor.tools.RemoveCornerTool;
import com.spiddekauga.voider.editor.tools.Selection;
import com.spiddekauga.voider.editor.tools.SelectionTool;
import com.spiddekauga.voider.editor.tools.TouchTool;
import com.spiddekauga.voider.editor.tools.TriggerAddTool;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.LoadingTextScene;
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
import com.spiddekauga.voider.menu.MainMenu;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.LoadingScene;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * The level editor scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelEditor extends Editor implements IResourceChangeEditor, ISelectionListener {
	/**
	 * Constructor for the level editor
	 */
	public LevelEditor() {
		super(new LevelEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_LEVEL_EDITOR);
		((LevelEditorGui) mGui).setLevelEditor(this);

		Actor.setEditorActive(true);

		mScroller = new Scroller(50, 2000, 10, 200, ScrollAxis.X);
		mSelection = new Selection();
		mSelection.addListener(this);

		// Initialize tools
		Tools.ADD_MOVE_CORNER.setTool(new AddMoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.DELETE.setTool(new DeleteTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.ENEMY_ADD.setTool(new EnemyAddTool(mCamera, mWorld, mInvoker, mSelection, this, EnemyActor.class));
		Tools.ENEMY_SET_ACTIVATE_TRIGGER.setTool(new EnemySetTriggerTool(mCamera, mWorld, mInvoker, mSelection, this, Actions.ACTOR_ACTIVATE));
		Tools.ENEMY_SET_DEACTIVATE_TRIGGER.setTool(new EnemySetTriggerTool(mCamera, mWorld, mInvoker, mSelection, this, Actions.ACTOR_DEACTIVATE));
		Tools.MOVE.setTool(new MoveTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.PATH_ADD_CORNER.setTool(new PathAddTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.PICKUP_ADD.setTool(new ActorAddTool(mCamera, mWorld, mInvoker, mSelection, this, PickupActor.class));
		Tools.REMOVE_CORNER.setTool(new RemoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.SELECTION.setTool(new SelectionTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.TERRAIN_DRAW_APPEND.setTool(new DrawAppendTool(mCamera, mWorld, mInvoker, mSelection, this, StaticTerrainActor.class));
		Tools.TERRAIN_DRAW_ERASE.setTool(new DrawEraseTool(mCamera, mWorld, mInvoker, mSelection, this, StaticTerrainActor.class));
		Tools.TRIGGER_ADD.setTool(new TriggerAddTool(mCamera, mWorld, mInvoker, mSelection, this));

		mInputMultiplexer.addProcessor(Tools.SELECTION.getTool());
		mInputMultiplexer.addProcessor(Tools.DELETE.getTool());
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		mGui.dispose();
		mGui.initGui();
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mLevel == null) {
			((EditorGui)mGui).showFirstTimeMenu();
			return;
		}

		// Force the player to set a name
		if (mLevel.getDef().getName().equals(Config.Actor.NAME_DEFAULT)) {
			((LevelEditorGui)mGui).showInfoDialog();
			mGui.showErrorMessage("Please enter a level name");
		}

		mLevel.update(deltaTime);

		// Scrolling
		if (mScroller.isScrolling()) {
			mScroller.update(deltaTime);

			Vector2 diffScroll = Pools.vector2.obtain();
			diffScroll.set(mScroller.getOriginScroll()).sub(mScroller.getCurrentScroll());
			float scale = diffScroll.x / Gdx.graphics.getWidth() * getWorldWidth();

			mCamera.position.x = scale + mScrollCameraOrigin.x;
			mCamera.update();

			Pools.vector2.free(diffScroll);
		}
		else if (!mCreatedScrollCommand) {
			Vector2 scrollCameraCurrent = Pools.vector2.obtain();
			scrollCameraCurrent.set(mCamera.position.x, mCamera.position.y);

			mInvoker.execute(new CCameraMove(mCamera, scrollCameraCurrent, mScrollCameraOrigin));

			mCreatedScrollCommand = true;

			Pools.vector2.free(scrollCameraCurrent);
		}

		if (shallAutoSave()) {
			saveDef();
			mGui.showErrorMessage(Messages.Info.SAVING);
		}
	}

	@Override
	protected void render() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		super.render();

		if (mLevel == null) {
			return;
		}

		if (Config.Graphics.USE_RELEASE_RENDERER) {
			ShaderProgram defaultShader = ResourceCacheFacade.get(ResourceNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			mLevel.render(mShapeRenderer);
			mLevel.renderEditor(mShapeRenderer);

			renderAboveBelowBorders();

			mShapeRenderer.pop();
		}
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

		if (mLevel != null) {
			if (level != null && mLevel.equals(level.getId())) {
				sameLevel = true;
				if (mLevel.equals(level)) {
					sameRevision = true;
				}
			}

			// Unload the old level
			if (ResourceCacheFacade.isLoaded(this, mLevel.getId(), mLevel.getRevision()) && !sameRevision) {
				ResourceCacheFacade.unload(this, mLevel.getDef(), false);
				ResourceCacheFacade.unload(this, mLevel, mLevel.getDef());
			}
			else {
				mLevel.dispose();
			}
		}

		mLevel = level;

		if (mLevel != null) {
			// Reset camera position to the start
			if (!sameLevel) {
				mCamera.position.x = mLevel.getDef().getStartXCoord() + mCamera.viewportWidth * 0.5f;
				mCamera.update();
			}
			mScroller.stop();

			createResourceBodies();

			// Activate all enemies and add them ot the add enemy list
			clearEnemyDef();
			ArrayList<EnemyActor> enemies = mLevel.getResources(EnemyActor.class);
			for (EnemyActor enemy : enemies) {
				if (enemy.getEnemyGroup() == null || enemy.isGroupLeader()) {
					enemy.activate();
					addEnemyDef(enemy.getDef(EnemyActorDef.class));
				}
			}
		}

		mInvoker.dispose();

		Actor.setLevel(mLevel);
	}

	// --------------------------------
	// Resource loading etc.
	// --------------------------------
	@Override
	public LoadingScene getLoadingScene() {
		/** @TODO create default loading scene */
		return new LoadingTextScene("Loading...");
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.loadAllOf(this, EnemyActorDef.class, true);
		ResourceCacheFacade.loadAllOf(this, PickupActorDef.class, true);
		ResourceCacheFacade.loadAllOf(this, LevelDef.class, false);

		// Load all themes
		for (Themes theme : Themes.values()) {
			ResourceCacheFacade.load(theme.getSkin());
		}
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unloadAllOf(this, EnemyActorDef.class, true);
		ResourceCacheFacade.unloadAllOf(this, PickupActorDef.class, true);
		ResourceCacheFacade.unloadAllOf(this, LevelDef.class, false);

		// Unload all themes
		for (Themes theme : Themes.values()) {
			ResourceCacheFacade.unload(theme.getSkin());
		}
	}

	//	@Override
	//	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
	//		super.reloadResourcesOnActivate(outcome, message);
	//		if (outcome == Outcomes.NOT_APPLICAPLE) {
	//			ResourceCacheFacade.loadAllNotYetLoadedOf(this, EnemyActorDef.class, true);
	//			ResourceCacheFacade.finishLoading();
	//		}
	//	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);

		// Check so that all resources have been loaded
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			// Loading a level
			if (mLoadingLevel != null) {
				Level loadedLevel;
				loadedLevel = ResourceCacheFacade.get(this, mLoadingLevel.getLevelId(), mLoadingLevel.getRevision());
				if (loadedLevel != null) {
					setLevel(loadedLevel);
					mGui.resetValues();
					mGui.hideMsgBoxes();
					setSaved();
					mLoadingLevel = null;
				}
				else {
					Gdx.app.error("LevelEditor", "Could not find level (" + mLoadingLevel.getLevelId() + ")");
				}
			}
		}
		else if (outcome == Outcomes.LOADING_FAILED_CORRUPT_FILE) {
			/** @todo loading failed, load backup? */
			mLoadingLevel = null;
		}
		else if (outcome == Outcomes.LOADING_FAILED_MISSING_FILE) {
			/** @todo loading failed, missing file */
			mLoadingLevel = null;
		}
		else if (outcome == Outcomes.DEF_SELECTED) {
			mGui.hideMsgBoxes();

			if (message instanceof ResourceItem) {
				switch (mSelectionAction) {
				case LEVEL:

					ResourceItem resourceItem = (ResourceItem) message;

					if (!ResourceCacheFacade.isLoaded(this, resourceItem.id, resourceItem.revision)) {
						ResourceCacheFacade.load(this, resourceItem.id, true, resourceItem.revision);
						ResourceCacheFacade.finishLoading();
					}

					mLoadingLevel = ResourceCacheFacade.get(this, resourceItem.id, resourceItem.revision);

					// Only load level if it's not the current level we selected, or another revision
					if (mLoadingLevel != null) {
						if (mLevel == null || !mLoadingLevel.equals(mLevel.getDef()) || mLoadingLevel.getRevision() != mLevel.getRevision()) {
							ResourceCacheFacade.load(this, mLoadingLevel.getLevelId(), mLoadingLevel.getId(), mLoadingLevel.getRevision());
							Scene scene = getLoadingScene();
							if (scene != null) {
								SceneSwitcher.switchTo(scene);
							}
						}
						else {
							mLoadingLevel = null;
						}
					}

					break;

				case PICKUP:
					mInvoker.execute(new CLevelPickupDefSelect(((ResourceItem) message).id, this));
					break;

				case ENEMY:
					mInvoker.execute(new CLevelEnemyDefAdd(((ResourceItem) message).id, this));
					break;
				}
			}
			else {
				Gdx.app.error(getClass().getSimpleName(), "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
			}
		}
		else if (outcome == Outcomes.NOT_APPLICAPLE) {
			mGui.hideMsgBoxes();
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
			EnemyActorDef enemyActorDef = ResourceCacheFacade.get(this, enemyId);
			if (enemyActorDef != null) {
				if (!mAddEnemies.contains(enemyActorDef)) {
					mAddEnemies.add(0, enemyActorDef);
					((LevelEditorGui) mGui).resetEnemyAddTable();
					return true;
				} else {
					mGui.showErrorMessage("This enemy has already been added.");
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
			EnemyActorDef enemyActorDef = ResourceCacheFacade.get(this, enemyId);
			if (enemyActorDef != null) {
				boolean removed = mAddEnemies.remove(enemyActorDef);

				if (removed) {
					((LevelEditorGui)mGui).resetEnemyAddTable();
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
	 * Selects the specified pickup definition. This pickup will be used when adding new pickups
	 * @param pickupId the pickup id to select
	 * @return true if the pickup was selected successfully, false if unsuccessful
	 */
	public boolean selectPickupDef(UUID pickupId) {
		try {
			PickupActorDef pickupActorDef = null;
			if (pickupId != null) {
				pickupActorDef = ResourceCacheFacade.get(this, pickupId);
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
		setLevel(null);
		Pools.arrayList.free(mAddEnemies);
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		super.touchDown(x, y, pointer, button);

		// Scroll the screen
		if (KeyHelper.isScrolling(button)) {
			// If we're already scrolling create scroll command
			if (mScroller.isScrolling()) {
				Vector2 scrollCameraCurrent = Pools.vector2.obtain();
				scrollCameraCurrent.set(mCamera.position.x, mCamera.position.y);
				mInvoker.execute(new CCameraMove(mCamera, scrollCameraCurrent, mScrollCameraOrigin));
				Pools.vector2.free(scrollCameraCurrent);
			}

			mScroller.touchDown(x, y);
			mScrollCameraOrigin.set(mCamera.position.x, mCamera.position.y);
			mCreatedScrollCommand = false;
			return true;
		}
		else if (mScroller.isScrolling()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		super.touchDragged(x, y, pointer);

		// Scrolling, move the map
		if (mScroller.isScrollingByHand() && pointer == 0) {
			mScroller.touchDragged(x, y);
			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		super.touchUp(x, y, pointer, button);

		// Not scrolling any more
		if (mScroller.isScrollingByHand() && (button == 2 || !Gdx.app.getInput().isTouched(0) || !Gdx.app.getInput().isTouched(1))) {
			mScroller.touchUp(x, y);
			return true;
		}

		return false;
	}

	@Override
	public void onResourceAdded(IResource resource) {
		mGui.resetValues();
		mLevel.addResource(resource);
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
		return mSelection.isSelected(EnemyActor.class);
	}

	@Override
	public boolean keyDown(int keycode) {
		// Redo
		if (KeyHelper.isRedoPressed(keycode)) {
			redo();
			return true;
		}
		// Undo
		else if (KeyHelper.isUndoPressed(keycode)) {
			undo();
			return true;
		}
		// Back - Deselect or go back
		else if (KeyHelper.isBackPressed(keycode)) {
			if (!mGui.isMsgBoxActive()) {
				if (!mSelection.isEmpty()) {
					mInvoker.execute(new CSelectionSet(mSelection));
				}
				else {
					saveDef();
					SceneSwitcher.returnTo(MainMenu.class);
				}
			} else {
				/** @todo close message box */
			}
		}
		/** @todo remove test buttons */
		// Toggle GUI/text buttons
		else if (keycode == Input.Keys.F5) {
			Config.Gui.setUseTextButtons(!Config.Gui.usesTextButtons());
			mGui.dispose();
			mGui.initGui();
			mGui.resetValues();
			return true;
		}
		else if (keycode == Input.Keys.F6) {
			String message = "This is a longer error message with more text, a lot more text, see if it will wrap correctly later...";
			mGui.showErrorMessage(message);
		}

		return false;
	}

	/**
	 * Undoes the previous action
	 */
	void undo() {
		mInvoker.undo();
	}

	/**
	 * Redo the action
	 */
	void redo() {
		mInvoker.redo();
	}

	/**
	 * Switch currently selected tool
	 * @param tool the new tool to use
	 */
	void switchTool(Tools tool) {
		if (mTool.getTool() != null) {
			mTool.getTool().deactivate();

			// Never remove selection tool
			if (mTool != Tools.SELECTION && mTool != Tools.DELETE) {
				mInputMultiplexer.removeProcessor(mTool.getTool());
			}
		}

		mTool = tool;

		if (mTool.getTool() != null) {
			mTool.getTool().activate();

			// Never add selection tool
			if (mTool != Tools.SELECTION && mTool != Tools.DELETE) {
				mInputMultiplexer.addProcessor(mTool.getTool());
			}
		}
	}


	/**
	 * Tests to run a game from the current location
	 * @param invulnerable makes the player invulnerable
	 */
	public void runFromHere(boolean invulnerable) {
		GameScene testGame = new GameScene(true, invulnerable);
		Level copyLevel = mLevel.copy();
		// Because of scaling decrease the x position
		float levelScaling = (Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE - 1) / Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE;
		float xPosition = mCamera.position.x + mCamera.viewportWidth * 0.5f - mCamera.viewportWidth * levelScaling;
		copyLevel.setStartPosition(xPosition);
		copyLevel.calculateEndPosition();

		testGame.setLevel(copyLevel);

		// Remove screen triggers before the specified coordinate
		ArrayList<TScreenAt> triggers = copyLevel.getResources(TScreenAt.class);
		for (TScreenAt trigger : triggers) {
			if (trigger.isTriggered()) {
				copyLevel.removeResource(trigger.getId());
			}
		}

		SceneSwitcher.switchTo(testGame);
	}

	@Override
	protected void fixCamera() {
		float width = Gdx.graphics.getWidth() * Config.Graphics.LEVEL_EDITOR_SCALE;
		// Decrease scale of width depending on height scaled
		float heightScale = Config.Graphics.HEIGHT_DEFAULT / Gdx.graphics.getHeight();
		width *= heightScale;
		float height = Config.Graphics.HEIGHT_DEFAULT * Config.Graphics.LEVEL_EDITOR_SCALE;

		if (mCamera != null) {
			mCamera.viewportHeight = height;
			mCamera.viewportWidth = width;
			mCamera.update();
		} else {
			mCamera = new OrthographicCamera(width , height);
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
		mLevel.calculateStartPosition();
		mLevel.calculateEndPosition();

		int oldRevision = mLevel.getRevision();
		ResourceSaver.save(mLevel.getDef());
		ResourceSaver.save(mLevel);

		// Update latest resource if revision was changed by more than one
		if (oldRevision != mLevel.getDef().getRevision() - 1) {
			ResourceCacheFacade.setLatestResource(mLevel, oldRevision);
			ResourceCacheFacade.setLatestResource(mLevel.getDef(), oldRevision);
		}

		// Saved first time? Then load level and def and use loaded versions instead
		if (!ResourceCacheFacade.isLoaded(this, mLevel.getId())) {
			ResourceCacheFacade.load(this, mLevel.getDef().getId(), false);
			ResourceCacheFacade.load(this, mLevel.getId(), mLevel.getDef().getId());
			ResourceCacheFacade.finishLoading();

			// Reset the level to old revision
			mLevel.getDef().setRevision(oldRevision);

			setLevel((Level) ResourceCacheFacade.get(this, mLevel.getId()));
		}

		setSaved();
	}

	@Override
	public void loadDef() {
		mSelectionAction = SelectionActions.LEVEL;

		Scene scene = new SelectDefScene(LevelDef.class, true, true, true);
		SceneSwitcher.switchTo(scene);

		setSaved();
	}

	@Override
	public void newDef() {
		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		setLevel(level);
		saveDef();
	}

	@Override
	public void duplicateDef() {
		Level level = mLevel.copy();

		setLevel(level);

		mGui.resetValues();
		mInvoker.dispose();
		saveDef();
	}

	/**
	 * Sets the starting speed of the current level
	 * @param speed starting speed of the current level
	 */
	void setLevelStartingSpeed(float speed) {
		if (mLevel != null) {
			mLevel.setSpeed(speed);
			setUnsaved();
		}
	}

	/**
	 * @return starting speed of the level, negative if no level is available
	 */
	float getLevelStartingSpeed() {
		if (mLevel != null) {
			return mLevel.getSpeed();
		}
		else {
			return -1;
		}
	}

	/**
	 * @return current revision of the level, empty string if no level is available
	 */
	String getLevelRevision() {
		if (mLevel != null) {
			return String.valueOf(mLevel.getRevision());
		}
		else {
			return "";
		}
	}

	/**
	 * Sets the name of the level
	 * @param name name of the level
	 */
	void setLevelName(String name) {
		if (mLevel != null) {
			mLevel.getDef().setName(name);
			setUnsaved();
		}
	}

	/**
	 * @return name of the level, empty string if no level is available
	 */
	public String getLevelName() {
		if (mLevel != null) {
			return mLevel.getDef().getName();
		}
		else {
			return "";
		}
	}

	/**
	 * Sets the description of the level
	 * 
	 * @param description
	 *            text description of the level
	 */
	void setLevelDescription(String description) {
		if (mLevel != null) {
			mLevel.getDef().setDescription(description);
			setUnsaved();
		}
	}

	/**
	 * @return description of the level, empty string if no level is available
	 */
	String getLevelDescription() {
		if (mLevel != null) {
			return mLevel.getDef().getDescription();
		}
		else {
			return "";
		}
	}

	/**
	 * Sets the story before the level
	 * @param storyText the story that will be displayed before the level
	 */
	void setPrologue(String storyText) {
		if (mLevel != null) {
			mLevel.getDef().setPrologue(storyText);
			setUnsaved();
		}
	}

	/**
	 * @return story that will be displayed before the level, empty string if no level is available
	 */
	String getPrologue() {
		if (mLevel != null) {
			return mLevel.getDef().getPrologue();
		}
		else {
			return "";
		}
	}

	/**
	 * Sets the story after completing the level
	 * @param storyText the story that will be displayed after the level
	 */
	void setEpilogue(String storyText) {
		if (mLevel != null) {
			mLevel.getDef().setStoryAfter(storyText);
			setUnsaved();
		}
	}

	/**
	 * @return story that will be displayed after completing the level, empty string if no level is available
	 */
	String getEpilogue() {
		if (mLevel != null) {
			return mLevel.getDef().getEpilogue();
		}
		else {
			return "";
		}
	}

	/**
	 * @return selected enemy name, null if none is selected
	 */
	String getSelectedEnemyName() {
		ActorDef actorDef = ((ActorAddTool) Tools.ENEMY_ADD.getTool()).getActorDef();

		if (actorDef != null) {
			return actorDef.getName();
		}
		else {
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
		}
		else {
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
						@SuppressWarnings("unchecked")
						ArrayList<EnemyActor> addedEnemies = Pools.arrayList.obtain();
						@SuppressWarnings("unchecked")
						ArrayList<EnemyActor> removedEnemies = Pools.arrayList.obtain();

						enemyGroup.setEnemyCount(cEnemies, addedEnemies, removedEnemies);

						for (EnemyActor addedEnemy : addedEnemies) {
							mLevel.addResource(addedEnemy);
						}

						for (EnemyActor removedEnemy : removedEnemies) {
							mLevel.removeResource(removedEnemy.getId());
						}

						Pools.arrayList.freeAll(addedEnemies, removedEnemies);
						addedEnemies = null;
						removedEnemies = null;
					}
					// Delete enemy group
					else {
						// Remove all excess enemies
						ArrayList<EnemyActor> removedEnemies = enemyGroup.clear();

						for (EnemyActor enemyActor : removedEnemies) {
							mLevel.removeResource(enemyActor.getId());
						}

						mLevel.removeResource(enemyGroup.getId());

						Pools.arrayList.free(removedEnemies);
						removedEnemies = null;
					}
				}
				// No enemy group, do we create one?
				else if (cEnemies > 1) {
					enemyGroup = new EnemyGroup();
					mLevel.addResource(enemyGroup);

					enemyGroup.setLeaderEnemy(selectedEnemy);

					@SuppressWarnings("unchecked")
					ArrayList<EnemyActor> addedEnemies = Pools.arrayList.obtain();
					enemyGroup.setEnemyCount(cEnemies, addedEnemies, null);

					for (EnemyActor addedEnemy : addedEnemies) {
						mLevel.addResource(addedEnemy);
						addedEnemy.destroyBody();
					}

					Pools.arrayList.free(addedEnemies);
					addedEnemies = null;
				}
			}
		}

		((LevelEditorGui) mGui).resetEnemyOptions();

		Pools.arrayList.free(selectedEnemies);

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
				}
				else if (cEnemies != 1) {
					return -1;
				}
			}

			return cEnemies;
		}

		Pools.arrayList.free(selectedEnemies);

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

		Pools.arrayList.free(selectedEnemies);

		setUnsaved();
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

			Pools.arrayList.free(selectedEnemies);
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

		Pools.arrayList.free(selectedPaths);

		mGui.resetValues();
	}

	/**
	 * @return current path type. If several paths are selected and they have different path types null is return. If no
	 * path is selected null is also returned.
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

		Pools.arrayList.free(selectedPaths);


		// Only allow one to be above 0
		if (cOnce > 0 && cLoop == 0 && cBackAndForth == 0) {
			return PathTypes.ONCE;
		}
		else if (cLoop > 0 && cOnce == 0 && cBackAndForth == 0) {
			return PathTypes.LOOP;
		}
		else if (cBackAndForth > 0 && cOnce == 0 && cLoop == 0) {
			return PathTypes.BACK_AND_FORTH;
		}

		return null;
	}

	/**
	 * @return true if a path is selected
	 */
	boolean isPathSelected() {
		return mSelection.isSelected(Path.class);
	}

	/**
	 * @return all paths in the current level
	 */
	public ArrayList<Path> getPaths() {
		return mLevel.getResources(Path.class);
	}

	/**
	 * Select pickup
	 */
	void selectPickup() {
		mSelectionAction = SelectionActions.PICKUP;

		Scene scene = new SelectDefScene(PickupActorDef.class, false, false, false);
		SceneSwitcher.switchTo(scene);
	}

	/**
	 * Select enemy
	 */
	void selectEnemy() {
		mSelectionAction = SelectionActions.ENEMY;

		Scene scene = new SelectDefScene(EnemyActorDef.class, false, true, false);
		SceneSwitcher.switchTo(scene);
	}

	/**
	 * @return true if the selected enemy has an activation trigger, false if not or if no enemy is selected
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


		Pools.arrayList.free(selectedEnemies);

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

		Pools.arrayList.free(selectedEnemies);

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

		Pools.arrayList.free(selectedEnemies);

		setUnsaved();
	}

	/**
	 * @return true if the selected enemy has an deactivation trigger, false if not or if no enemy is selected
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


		Pools.arrayList.free(selectedEnemies);

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

		Pools.arrayList.free(selectedEnemies);

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

		Pools.arrayList.free(selectedEnemies);

		setUnsaved();
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
		return ((ActorAddTool)Tools.ENEMY_ADD.getTool()).getActorDef();
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
		/** Add a trigger */
		TRIGGER_ADD,
		/** Add a corner to a path */
		PATH_ADD_CORNER,
		/** Path remove corner */
		PATH_REMOVE_CORNER,

		;

		/**
		 * Sets the actual tool
		 * 
		 * @param tool
		 *            the actual tool that is used
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
				}
				else if (resourceBody instanceof Path) {
					((Path) resourceBody).setWorld(mWorld);
				}
				else {
					resourceBody.createBody();
				}
			}
		}
	}

	/**
	 * Renders the above and below borders
	 */
	private void renderAboveBelowBorders() {
		// Calculate how much space is left for the borders
		float heightAvailable = (Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE - 1) / Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE;

		Vector2 minPos = Pools.vector2.obtain();
		Vector2 maxPos = Pools.vector2.obtain();

		screenToWorldCoord(mCamera, 0, Gdx.graphics.getHeight(), minPos, false);
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), 0, maxPos, false);

		heightAvailable *= maxPos.y - minPos.y;
		heightAvailable *= 0.5f;
		float width = maxPos.x - minPos.x;

		mShapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.LEVEL_ABOVE_BELOW_COLOR));

		// Draw borders
		mShapeRenderer.translate(0, 0, RenderOrders.LEVEL_UPPER_LOWER_BORDERS.getZValue());
		// Upper
		mShapeRenderer.rect(minPos.x, minPos.y, width, heightAvailable);
		// Lower
		mShapeRenderer.rect(minPos.x, maxPos.y - heightAvailable, width, heightAvailable);
		mShapeRenderer.translate(0, 0, -RenderOrders.LEVEL_UPPER_LOWER_BORDERS.getZValue());
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
		((EnemyAddTool)Tools.ENEMY_ADD.mTool).setActorDef(enemyDef);
	}

	/**
	 * Set the theme for the level
	 * @param theme the theme for the level
	 */
	void setTheme(Themes theme) {
		if (mLevel != null) {
			ResourceCacheFacade.unload(mLevel.getDef().getTheme().getSkin());
			mLevel.getDef().setTheme(theme);
			ResourceCacheFacade.load(mLevel.getDef().getTheme().getSkin());
			setUnsaved();
		}
	}

	/**
	 * @return current theme of the level
	 */
	Themes getTheme() {
		if (mLevel != null) {
			return mLevel.getDef().getTheme();
		} else {
			return Themes.SPACE;
		}
	}

	/**
	 * All definition selection actions
	 */
	private enum SelectionActions {
		/** Select an enemy */
		ENEMY,
		/** Loads a level */
		LEVEL,
		/** Selects a pickup */
		PICKUP,
	}

	/** Enemies in the add enemy table */
	@SuppressWarnings("unchecked")
	private ArrayList<EnemyActorDef> mAddEnemies = Pools.arrayList.obtain();
	/** Created scroll command for the last scroll */
	private boolean mCreatedScrollCommand = true;
	/** Level we're currently editing */
	private Level mLevel = null;
	/** Scrolling for nice scrolling */
	private Scroller mScroller;
	/** Starting position for the camera scroll */
	private Vector2 mScrollCameraOrigin = new Vector2();
	/** Which definition we're currently selecting */
	private SelectionActions mSelectionAction = null;
	/** Currently loading level */
	private LevelDef mLoadingLevel = null;
	/** The selection */
	private ISelection mSelection = null;
	/** Current selected tool */
	private Tools mTool = Tools.SELECTION;
}