package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Scroller;
import com.spiddekauga.utils.Scroller.ScrollAxis;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.app.MainMenu;
import com.spiddekauga.voider.editor.commands.CCameraMove;
import com.spiddekauga.voider.editor.commands.CLevelEnemyDefSelect;
import com.spiddekauga.voider.editor.commands.CLevelPickupDefSelect;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.editor.tools.AddActorTool;
import com.spiddekauga.voider.editor.tools.AddEnemyTool;
import com.spiddekauga.voider.editor.tools.AddMoveCornerTool;
import com.spiddekauga.voider.editor.tools.DrawActorTool;
import com.spiddekauga.voider.editor.tools.DrawAppendTool;
import com.spiddekauga.voider.editor.tools.DrawEraseTool;
import com.spiddekauga.voider.editor.tools.ISelection;
import com.spiddekauga.voider.editor.tools.PathAddTool;
import com.spiddekauga.voider.editor.tools.PathTool;
import com.spiddekauga.voider.editor.tools.RemoveCornerTool;
import com.spiddekauga.voider.editor.tools.Selection;
import com.spiddekauga.voider.editor.tools.SelectionTool;
import com.spiddekauga.voider.editor.tools.TouchTool;
import com.spiddekauga.voider.editor.tools.TriggerAddTool;
import com.spiddekauga.voider.editor.tools.TriggerTool;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.LoadingTextScene;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
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
public class LevelEditor extends Editor implements IResourceChangeEditor {
	/**
	 * Constructor for the level editor
	 */
	public LevelEditor() {
		super(new LevelEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_LEVEL_EDITOR);
		((LevelEditorGui)mGui).setLevelEditor(this);

		Actor.setEditorActive(true);

		mScroller = new Scroller(50, 2000, 10, 200, ScrollAxis.X);
		mSelection = new Selection();

		// Initialize tools
		Tools.SELECTION.setTool(new SelectionTool(mCamera, mWorld, mInvoker, mSelection, this));
		mInputMultiplexer.addProcessor(Tools.SELECTION.getTool());

		Tools.PATH_ADD_CORNER.setTool(new PathAddTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.TERRAIN_DRAW_APPEND.setTool(new DrawAppendTool(mCamera, mWorld, mInvoker, mSelection, this, StaticTerrainActor.class));
		Tools.TERRAIN_DRAW_ERASE.setTool(new DrawEraseTool(mCamera, mWorld, mInvoker, mSelection, this, StaticTerrainActor.class));
		Tools.TERRAIN_ADJUST_ADD_MOVE_CORNER.setTool(new AddMoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.TERRAIN_ADJUST_REMOVE_CORNER.setTool(new RemoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this));
		Tools.TRIGGER_ADD.setTool(new TriggerAddTool(mCamera, mWorld, mInvoker, mSelection, this));
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);
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
		} else if (!mCreatedScrollCommand) {
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
		super.render();

		if (Config.Graphics.USE_RELEASE_RENDERER) {
			ShaderProgram defaultShader = ResourceCacheFacade.get(ResourceNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
			} else {
				mLevel.dispose();
			}
		}

		mLevel = level;

		if (mLevel != null) {
			clearToolGroups();

			// Reset camera position to the start
			if (!sameLevel) {
				mCamera.position.x = mLevel.getDef().getStartXCoord() + mCamera.viewportWidth * 0.5f;
				mCamera.update();
			}
			mScroller.stop();

			createResourceBodies();

			// Activate all enemies
			ArrayList<EnemyActor> enemies = mLevel.getResources(EnemyActor.class);
			for (EnemyActor enemy : enemies) {
				if (enemy.getEnemyGroup() == null || enemy.isGroupLeader()) {
					enemy.activate();
				}
			}
		}

		mInvoker.dispose();

		Actor.setLevel(mLevel);
	}

	@Override
	public Invoker getInvoker() {
		return mInvoker;
	}

	// --------------------------------
	//		Resource loading etc.
	// --------------------------------
	@Override
	public LoadingScene getLoadingScene() {
		/** @TODO create default loading scene */
		return new LoadingTextScene("Loading...");
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(ResourceNames.UI_EDITOR_BUTTONS);
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.load(ResourceNames.SHADER_DEFAULT);
		ResourceCacheFacade.loadAllOf(this, EnemyActorDef.class, true);
		ResourceCacheFacade.loadAllOf(this, PickupActorDef.class, true);
		ResourceCacheFacade.loadAllOf(this, LevelDef.class, false);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_EDITOR_BUTTONS);
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.unload(ResourceNames.SHADER_DEFAULT);
		ResourceCacheFacade.unloadAllOf(this, EnemyActorDef.class, true);
		ResourceCacheFacade.unloadAllOf(this, PickupActorDef.class, true);
		ResourceCacheFacade.unloadAllOf(this, LevelDef.class, false);
	}

	@Override
	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
		super.reloadResourcesOnActivate(outcome, message);
		if (outcome == Outcomes.NOT_APPLICAPLE) {
			ResourceCacheFacade.loadAllNotYetLoadedOf(this, EnemyActorDef.class, true);
			ResourceCacheFacade.finishLoading();
		}
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);

		// Check so that all resources have been loaded
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			setSaved();

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
				} else {
					Gdx.app.error("LevelEditor", "Could not find level (" + mLoadingLevel.getLevelId() + ")");
				}
			} else if (mLevel == null) {
				newDef();
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

					mLoadingLevel = ResourceCacheFacade.get(this, ((ResourceItem) message).id, ((ResourceItem) message).revision);

					// Only load level if it's not the current level we selected, or another revision
					if (!mLoadingLevel.equals(mLevel.getDef()) || mLoadingLevel.getRevision() != mLevel.getRevision()) {
						ResourceCacheFacade.load(this, mLoadingLevel.getLevelId(), mLoadingLevel.getId(), mLoadingLevel.getRevision());
						Scene scene = getLoadingScene();
						if (scene != null) {
							SceneSwitcher.switchTo(scene);
						}
					} else {
						mLoadingLevel = null;
					}

					break;

				case PICKUP:
					mInvoker.execute(new CLevelPickupDefSelect(((ResourceItem) message).id, this));
					break;

				case ENEMY:
					mInvoker.execute(new CLevelEnemyDefSelect(((ResourceItem) message).id, ((ResourceItem) message).revision, this));
					break;
				}
			} else {
				Gdx.app.error(getClass().getSimpleName(), "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
			}
		} else if (outcome == Outcomes.NOT_APPLICAPLE) {
			mGui.hideMsgBoxes();

			if (mLevel == null) {
				newDef();
			}
		}

		if (mLevel != null) {
			Actor.setLevel(mLevel);
		}
	}

	/**
	 * Selects the specified enemy definition. This enemy will be used when adding new enemies.
	 * @param enemyId the enemy id to select, can be null.
	 * @param revision the revision of the enemy to select
	 * @return true if enemy was selected successfully, false if unsuccessful
	 */
	public boolean selectEnemyDef(UUID enemyId, int revision) {
		try {
			if (enemyId != null) {
				EnemyActorDef enemyActorDef = ResourceCacheFacade.get(this, enemyId, revision);

				// Update def
				// TODO
				//				((ActorToolOld)mTouchTools[ToolGroups.ENEMY.ordinal()]).setNewActorDef(enemyActorDef);
			} else {
				// TODO
				//				((ActorToolOld)mTouchTools[ToolGroups.ENEMY.ordinal()]).setNewActorDef(null);
			}

			mGui.resetValues();
		} catch (Exception e) {
			Gdx.app.error("LevelEditor", e.toString());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Selects the specified pickup definition. This pickup will be used when adding new pickups
	 * @param pickupId the pickup id to select
	 * @return true if the pickup was selected successfully, false if unsuccessful
	 */
	public boolean selectPickupDef(UUID pickupId) {
		try {
			if (pickupId != null) {
				PickupActorDef pickupActorDef = ResourceCacheFacade.get(this, pickupId, -1);

				// Update def
				// TODO
				//				((ActorToolOld)mTouchTools[ToolGroups.PICKUP.ordinal()]).setNewActorDef(pickupActorDef);
			} else {
				// TODO
				//				((ActorToolOld)mTouchTools[ToolGroups.PICKUP.ordinal()]).setNewActorDef(null);
			}
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
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		super.touchDown(x, y, pointer, button);

		// Scroll -> When press middle mouse or two fingers
		if (button == 2 || (Gdx.app.getInput().isTouched(0) && Gdx.app.getInput().isTouched(1))) {
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
		} else if (mScroller.isScrolling()) {
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
		mLevel.addResource(resource);
		setUnsaved();
	}

	@Override
	public void onResourceRemoved(IResource resource) {
		mLevel.removeResource(resource.getId());
		setUnsaved();
	}

	@Override
	public void onResourceChanged(IResource resource) {
		setUnsaved();

		if (resource instanceof EnemyActor) {
			((LevelEditorGui)mGui).resetEnemyOptions();
		}
	}

	@Override
	public void onResourceSelected(IResource deselectedResource, IResource selectedResource) {
		mGui.resetValues();
	}

	/**
	 * @return true if an enemy is currently selected
	 */
	boolean isEnemySelected() {
		return mSelection.getMostCommonSelectedResourceType() == EnemyActor.class;
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
			if (!mSelection.isEmpty()) {
				mInvoker.execute(new CSelectionSet(mSelection));
			} else {
				saveDef();
				SceneSwitcher.returnTo(MainMenu.class);
			}
		}
		/** @todo remove test buttons */
		// Toggle GUI/text buttons
		else if (keycode == Input.Keys.F5) {
			Config.Gui.setUseTextButtons(!Config.Gui.usesTextButtons());
			mGui.dispose();
			mGui.initGui();
		} else if (keycode== Input.Keys.F6) {
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
	 * Switches the tool to the selected tool
	 * @param toolGroup the new tool type
	 */
	@Deprecated
	void switchToolGroup(ToolGroups toolGroup) {
		//		// Remove old tool
		//		if (mToolGroup != null) {
		//			mInputMultiplexer.removeProcessor(mTouchTools[mToolGroup.ordinal()]);
		//			mTouchTools[mToolGroup.ordinal()].deactivate();
		//		}
		//
		//		// Set current tool
		//		mToolGroup = toolGroup;
		//
		//		// add new tool
		//		if (mToolGroup != null) {
		//			mInputMultiplexer.addProcessor(mTouchTools[mToolGroup.ordinal()]);
		//			mTouchTools[mToolGroup.ordinal()].activate();
		//		}
		//
		//		if (mGui.isInitialized()) {
		//			mGui.resetValues();
		//		}
	}

	/**
	 * Switch currently selected tool
	 * @param tool the new tool to use
	 */
	void switchTool(Tools tool) {
		if (mTool.getTool() != null) {
			mTool.getTool().deactivate();

			// Never remove selection tool
			if (mTool != Tools.SELECTION) {
				mInputMultiplexer.removeProcessor(mTool.getTool());
			}
		}

		mTool = tool;

		if (mTool.getTool() != null) {
			mTool.getTool().activate();

			// Never add selection tool
			if (mTool != Tools.SELECTION) {
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
	public void saveDef() {
		mLevel.calculateStartPosition();
		mLevel.calculateEndPosition();

		int oldRevision = mLevel.getRevision();
		ResourceSaver.save(mLevel.getDef());
		ResourceSaver.save(mLevel);

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
	}

	@Override
	public void newDef() {
		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		setLevel(level);
		saveDef();

		clearToolGroups();

		setSaved();
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
		} else {
			return -1;
		}
	}

	/**
	 * @return current revision of the level, empty string if no level is available
	 */
	String getLevelRevision() {
		if (mLevel != null) {
			return String.valueOf(mLevel.getDef().getRevision());
		} else {
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
	String getLevelName() {
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
			mLevel.getDef().setPrologue(storyText);
			setUnsaved();
		}
	}

	/**
	 * @return story that will be displayed before the level, empty string if no
	 * level is available
	 */
	String getPrologue() {
		if (mLevel != null) {
			return mLevel.getDef().getPrologue();
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
			mLevel.getDef().setStoryAfter(storyText);
			setUnsaved();
		}
	}

	/**
	 * @return story that will be displayed after completing the level, empty string if no
	 * level is available
	 */
	String getEpilogue() {
		if (mLevel != null) {
			return mLevel.getDef().getEpilogue();
		} else {
			return "";
		}
	}

	/**
	 * @return selected enemy name, null if none is selected
	 */
	String getSelectedEnemyName() {
		// TODO
		//		ActorDef actorDef = ((AddActorTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getNewActorDef();
		//
		//		if (actorDef != null) {
		//			return actorDef.getName();
		//		} else {
		//			return null;
		//		}
		return null;
	}

	/**
	 * @return selected pickup name, null if none is selected
	 */
	String getSelectedPickupName() {
		// TODO
		//		ActorDef actorDef = ((AddActorTool)mTouchTools[ToolGroups.PICKUP.ordinal()]).getNewActorDef();
		//
		//		if (actorDef != null) {
		//			return actorDef.getName();
		//		} else {
		//			return null;
		//		}
		return null;
	}

	/**
	 * Sets the number of enemies in one group
	 * @param cEnemies number of enemies in the group
	 */
	void setEnemyCount(int cEnemies) {
		// TODO
		//		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (selectedEnemy != null) {
		//			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();
		//
		//			// We have an enemy group
		//			if (enemyGroup != null) {
		//				// Just change amount of enemies
		//				if (cEnemies > 1) {
		//					@SuppressWarnings("unchecked")
		//					ArrayList<EnemyActor> addedEnemies = Pools.arrayList.obtain();
		//					@SuppressWarnings("unchecked")
		//					ArrayList<EnemyActor> removedEnemies = Pools.arrayList.obtain();
		//
		//					enemyGroup.setEnemyCount(cEnemies, addedEnemies, removedEnemies);
		//
		//					for (EnemyActor addedEnemy : addedEnemies) {
		//						mLevel.addResource(addedEnemy);
		//					}
		//
		//					for (EnemyActor removedEnemy : removedEnemies) {
		//						mLevel.removeResource(removedEnemy.getId());
		//					}
		//
		//					Pools.arrayList.freeAll(addedEnemies, removedEnemies);
		//					addedEnemies = null;
		//					removedEnemies = null;
		//				}
		//				// Delete enemy group
		//				else {
		//					// Remove all excess enemies
		//					ArrayList<EnemyActor> removedEnemies = enemyGroup.clear();
		//
		//					for (EnemyActor enemyActor : removedEnemies) {
		//						mLevel.removeResource(enemyActor.getId());
		//					}
		//
		//					mLevel.removeResource(enemyGroup.getId());
		//
		//					Pools.arrayList.free(removedEnemies);
		//					removedEnemies = null;
		//				}
		//			}
		//			// No enemy group, do we create one?
		//			else if (cEnemies > 1) {
		//				enemyGroup = new EnemyGroup();
		//				mLevel.addResource(enemyGroup);
		//
		//				enemyGroup.setLeaderEnemy(selectedEnemy);
		//
		//				@SuppressWarnings("unchecked")
		//				ArrayList<EnemyActor> addedEnemies = Pools.arrayList.obtain();
		//				enemyGroup.setEnemyCount(cEnemies, addedEnemies, null);
		//
		//				for (EnemyActor addedEnemy : addedEnemies) {
		//					mLevel.addResource(addedEnemy);
		//					addedEnemy.destroyBody();
		//				}
		//
		//				Pools.arrayList.free(addedEnemies);
		//				addedEnemies = null;
		//
		//				// Set GUI delay value
		//				((LevelEditorGui)mGui).resetEnemyOptions();
		//			}
		//		}
	}

	/**
	 * @return number of enemies in a group
	 */
	int getEnemyCount() {
		// TODO
		//		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		int cEnemies = 0;
		//
		//		if (selectedEnemy != null) {
		//			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();
		//
		//			if (enemyGroup != null) {
		//				cEnemies = enemyGroup.getEnemyCount();
		//			}
		//			// No group, that means we only have one enemy
		//			else {
		//				cEnemies = 1;
		//			}
		//		}
		//
		//		return cEnemies;
		return 0;
	}

	/**
	 * Sets the spawn delay between actors in the same group.
	 * @param delay seconds of delay between actors are activated.
	 */
	void setEnemySpawnDelay(float delay) {
		// TODO
		//		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (selectedEnemy != null) {
		//			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();
		//
		//			// We have an enemy group
		//			if (enemyGroup != null) {
		//				enemyGroup.setSpawnTriggerDelay(delay);
		//			}
		//		}
	}

	/**
	 * @return spawn delay between actors in the same group, negative value if no group exist
	 */
	float getEnemySpawnDelay() {
		// TODO
		//		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (selectedEnemy != null) {
		//			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();
		//
		//			// We have an enemy group
		//			if (enemyGroup != null) {
		//				return enemyGroup.getSpawnTriggerDelay();
		//			}
		//		}

		return -1;
	}

	/**
	 * Sets the path type of the selected path
	 * @param pathType type of the path
	 */
	void setPathType(Path.PathTypes pathType) {
		// TODO
		//		Path selectedPath = (Path) ((PathTool)mTouchTools[ToolGroups.PATH.ordinal()]).getSelectedResource();
		//
		//		if (selectedPath != null) {
		//			selectedPath.setPathType(pathType);
		//		}
	}

	/**
	 * @return current path type, null if no path has been selected
	 */
	PathTypes getPathType() {
		// TODO
		//		Path selectedPath = (Path) ((PathTool)mTouchTools[ToolGroups.PATH.ordinal()]).getSelectedResource();
		//
		//		if (selectedPath != null) {
		//			return selectedPath.getPathType();
		//		} else {
		//			return null;
		//		}
		return null;
	}

	/**
	 * @return true if a path is selected
	 */
	boolean isPathSelected() {
		return mSelection.getMostCommonSelectedResourceType() == Path.class;
	}

	/**
	 * Sets the current active pickup tool
	 * @param state current active pickup tool
	 */
	@Deprecated
	void setPickupState(AddActorTool.States state) {
		//		AddActorTool addActorTool = (AddActorTool) mTouchTools[ToolGroups.PICKUP.ordinal()];
		//		addActorTool.setState(state);
	}

	/**
	 * @return current active pickup tool state
	 */
	@Deprecated
	AddActorTool.States getPickupState() {
		return null;
		//		return ((AddActorTool)mTouchTools[ToolGroups.PICKUP.ordinal()]).getState();
	}

	/**
	 * Sets the current active static terrain tool
	 * @param state current active static terrain tool
	 */
	@Deprecated
	void setStaticTerrainState(DrawActorTool.States state) {
		//		DrawActorTool drawActorTool = (DrawActorTool) mTouchTools[ToolGroups.STATIC_TERRAIN.ordinal()];
		//		drawActorTool.setState(state);
	}

	/**
	 * @return current active static terrain tool state
	 */
	@Deprecated
	DrawActorTool.States getStaticTerrainState() {
		//		return ((DrawActorTool)mTouchTools[ToolGroups.STATIC_TERRAIN.ordinal()]).getState();
		return null;
	}

	/**
	 * Sets the active enemy tool state
	 * @param state current active enemy tool state
	 */
	@Deprecated
	void setEnemyState(AddEnemyTool.States state) {
		//		AddEnemyTool addEnemyTool = (AddEnemyTool) mTouchTools[ToolGroups.ENEMY.ordinal()];
		//		addEnemyTool.setEnemyState(state);
	}

	/**
	 * Resets the enemy state to the active add actor state.
	 */
	@Deprecated
	void resetEnemyState() {
		//		AddEnemyTool addEnemyTool = (AddEnemyTool) mTouchTools[ToolGroups.ENEMY.ordinal()];
		//
		//		switch (addEnemyTool.getState()) {
		//		case ADD:
		//			addEnemyTool.setEnemyState(AddEnemyTool.States.ADD);
		//			break;
		//
		//		case REMOVE:
		//			addEnemyTool.setEnemyState(AddEnemyTool.States.REMOVE);
		//			break;
		//
		//		case MOVE:
		//			addEnemyTool.setEnemyState(AddEnemyTool.States.MOVE);
		//			break;
		//
		//		case SELECT:
		//			addEnemyTool.setEnemyState(AddEnemyTool.States.SELECT);
		//			break;
		//		}
	}

	/**
	 * @return current active enemy tool state
	 */
	@Deprecated
	AddEnemyTool.States getEnemyState() {
		//		return ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getEnemyState();
		return null;
	}

	/**
	 * Sets the active path tool state
	 * @param state new active path tool state
	 */
	@Deprecated
	void setPathState(PathTool.States state) {
		//		PathTool pathTool = (PathTool) mTouchTools[ToolGroups.PATH.ordinal()];
		//		pathTool.setState(state);
	}

	/**
	 * @return current path tool state
	 */
	@Deprecated
	PathTool.States getPathState() {
		//		return ((PathTool)mTouchTools[ToolGroups.PATH.ordinal()]).getState();
		return null;
	}

	/**
	 * Sets the active trigger tool state
	 * @param state new active trigger tool state
	 */
	@Deprecated
	void setTriggerState(TriggerTool.States state) {
		//		TriggerTool triggerTool = (TriggerTool) mTouchTools[ToolGroups.TRIGGER.ordinal()];
		//		triggerTool.setState(state);
	}

	/**
	 * @return current state of the trigger tool
	 */
	@Deprecated
	TriggerTool.States getTriggerState() {
		//		return ((TriggerTool)mTouchTools[ToolGroups.TRIGGER.ordinal()]).getState();
		return null;
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
	 * @return true if the selected enemy has an activation trigger, false if not or
	 * if no enemy is selected
	 */
	boolean hasSelectedEnemyActivateTrigger() {
		// TODO
		//		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (enemy != null) {
		//			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
		//			for (TriggerInfo trigger : triggers) {
		//				if (trigger.action == Actions.ACTOR_ACTIVATE) {
		//					return true;
		//				}
		//			}
		//		}
		//
		return false;
	}

	/**
	 * @return delay of the activation trigger, negative if no activation trigger has
	 * been set.
	 */
	float getSelectedEnemyActivateTriggerDelay() {
		// TODO
		//		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (enemy != null) {
		//			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
		//			for (TriggerInfo trigger : triggers) {
		//				if (trigger.action == Actions.ACTOR_ACTIVATE) {
		//					return trigger.delay;
		//				}
		//			}
		//		}

		return -1;
	}

	/**
	 * Sets the delay of the activation trigger
	 * @param delay seconds of delay
	 */
	void setSelectedEnemyActivateTriggerDelay(float delay) {
		// TODO
		//		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (enemy != null) {
		//			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
		//			for (TriggerInfo trigger : triggers) {
		//				if (trigger.action == Actions.ACTOR_ACTIVATE) {
		//					trigger.delay = delay;
		//				}
		//			}
		//		}
	}

	/**
	 * @return true if the selected enemy has an deactivation trigger, false if not or
	 * if no enemy is selected
	 */
	boolean hasSelectedEnemyDeactivateTrigger() {
		// TODO
		//		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (enemy != null) {
		//			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
		//			for (TriggerInfo trigger : triggers) {
		//				if (trigger.action == Actions.ACTOR_DEACTIVATE) {
		//					return true;
		//				}
		//			}
		//		}

		return false;
	}

	/**
	 * @return delay of the deactivation trigger, negative if no deactivation trigger has
	 * been set.
	 */
	float getSelectedEnemyDeactivateTriggerDelay() {
		// TODO
		//		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (enemy != null) {
		//			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
		//			for (TriggerInfo trigger : triggers) {
		//				if (trigger.action == Actions.ACTOR_DEACTIVATE) {
		//					return trigger.delay;
		//				}
		//			}
		//		}

		return -1;
	}

	/**
	 * Sets the delay of the deactivate trigger
	 * @param delay seconds of delay
	 */
	void setSelectedEnemyDeactivateTriggerDelay(float delay) {
		// TODO
		//		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[ToolGroups.ENEMY.ordinal()]).getSelectedResource();
		//
		//		if (enemy != null) {
		//			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
		//			for (TriggerInfo trigger : triggers) {
		//				if (trigger.action == Actions.ACTOR_DEACTIVATE) {
		//					trigger.delay = delay;
		//				}
		//			}
		//		}
	}

	/**
	 * @return current level
	 */
	public Level getLevel() {
		return mLevel;
	}

	/**
	 * @return the current active tool of the level editor
	 */
	ToolGroups getSelectedTool() {
		return mToolGroup;
	}

	/**
	 * @return currently selected pickup definition
	 */
	public ActorDef getSelectedPickupDef() {
		// TODO
		//		return ((ActorToolOld) mTouchTools[ToolGroups.PICKUP.ordinal()]).getNewActorDef();
		return null;
	}

	/**
	 * @return currently selected enemy definition
	 */
	public ActorDef getSelectedEnemyDef() {
		// TODO
		//		return ((ActorToolOld) mTouchTools[ToolGroups.ENEMY.ordinal()]).getNewActorDef();
		return null;
	}

	/**
	 * All the main tool buttons
	 */
	enum ToolGroups {
		/** Add enemies */
		ENEMY,
		/** Add/Move/Remove pickups */
		PICKUP,
		/** Draws the terrain */
		STATIC_TERRAIN,
		/** Draw paths */
		PATH,
		/** Add/remove triggers */
		TRIGGER,
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
		/** Append either add a corner or move a corner in a terrain */
		TERRAIN_ADJUST_ADD_MOVE_CORNER,
		/** Remove a corner from the terrain */
		TERRAIN_ADJUST_REMOVE_CORNER,
		/** Append to the terrain */
		TERRAIN_DRAW_APPEND,
		/** Draw erase for terrain */
		TERRAIN_DRAW_ERASE,
		/** Add pickup */
		PICKUP_ADD,
		/** Add enemy */
		ENEMY_ADD,
		/** Add a trigger */
		TRIGGER_ADD,
		/** Add a corner to a path */
		PATH_ADD_CORNER,

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
					if (((EnemyActor) resourceBody).getEnemyGroup() == null || ((EnemyActor)resourceBody).isGroupLeader()) {
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
	 * Clears all the tools
	 */
	@Deprecated
	private void clearToolGroups() {
		//		for (TouchTool touchTool : mTouchTools) {
		//			if (touchTool != null) {
		//				touchTool.clear();
		//			}
		//		}
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

		mShapeRenderer.setColor(Config.Editor.Level.ABOVE_BELOW_COLOR);


		// Draw borders
		// Upper
		mShapeRenderer.rect(minPos.x, minPos.y, width, heightAvailable);
		// Lower
		mShapeRenderer.rect(minPos.x, maxPos.y - heightAvailable, width, heightAvailable);
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

	/** Created scroll command for the last scroll */
	private boolean mCreatedScrollCommand = true;
	/** Level we're currently editing */
	private Level mLevel = null;
	/** Invoker for the level editor */
	private Invoker mInvoker = new Invoker();
	/** Scrolling for nice scrolling */
	private Scroller mScroller;
	/** Starting position for the camera scroll */
	private Vector2 mScrollCameraOrigin = new Vector2();
	/** Which definition we're currently selecting */
	private SelectionActions mSelectionAction = null;
	/** Currently loading level */
	private LevelDef mLoadingLevel = null;
	/** Current selected tool group */
	private ToolGroups mToolGroup = ToolGroups.STATIC_TERRAIN;
	/** The selection */
	private ISelection mSelection = null;
	//	/** All the available tools */
	//	private TouchTool[] mTouchTools = new TouchTool[ToolGroups.values().length];
	/** Current selected tool */
	private Tools mTool = Tools.SELECTION;

}