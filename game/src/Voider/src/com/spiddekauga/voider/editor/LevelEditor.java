package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Scroller;
import com.spiddekauga.utils.Scroller.ScrollAxis;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.CCameraMove;
import com.spiddekauga.voider.editor.commands.CLevelEnemyDefSelect;
import com.spiddekauga.voider.editor.commands.CLevelPickupDefSelect;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
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
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.ActorTool;
import com.spiddekauga.voider.scene.AddActorTool;
import com.spiddekauga.voider.scene.AddEnemyTool;
import com.spiddekauga.voider.scene.DrawActorTool;
import com.spiddekauga.voider.scene.LoadingScene;
import com.spiddekauga.voider.scene.PathTool;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.scene.TouchTool;
import com.spiddekauga.voider.scene.TriggerTool;
import com.spiddekauga.voider.scene.WorldScene;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * The level editor scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelEditor extends WorldScene implements IResourceChangeEditor, IEditor {
	/**
	 * Constructor for the level editor
	 */
	public LevelEditor() {
		super(new LevelEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_LEVEL_EDITOR);
		((LevelEditorGui)mGui).setLevelEditor(this);

		Actor.setEditorActive(true);

		mScroller = new Scroller(50, 2000, 10, 200, ScrollAxis.X);

		// Initialize all tools
		DrawActorTool terrainTool = new DrawActorTool(mCamera, mWorld, StaticTerrainActor.class, mInvoker, this);
		mTouchTools[Tools.STATIC_TERRAIN.ordinal()] = terrainTool;
		AddActorTool pickupTool = new AddActorTool(mCamera, mWorld, PickupActor.class, mInvoker, true, this);
		mTouchTools[Tools.PICKUP.ordinal()] = pickupTool;
		AddEnemyTool enemyTool = new AddEnemyTool(mCamera, mWorld, mInvoker, this);
		enemyTool.addListener(this);
		mTouchTools[Tools.ENEMY.ordinal()] = enemyTool;
		PathTool pathTool = new PathTool(mCamera, mWorld, mInvoker, this);
		pathTool.addListener(this);
		mTouchTools[Tools.PATH.ordinal()] = pathTool;
		TriggerTool triggerTool = new TriggerTool(mCamera, mWorld, mInvoker, this);
		triggerTool.addListener(this);
		mTouchTools[Tools.TRIGGER.ordinal()] = triggerTool;

		switchTool(Tools.STATIC_TERRAIN);
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
	protected void fixCamera() {
		float width = Gdx.graphics.getWidth() * Config.Graphics.LEVEL_EDITOR_SCALE;
		// Decrease scale of width depending on height scaled
		float heightScale = Config.Graphics.HEIGHT / Gdx.graphics.getHeight();
		width *= heightScale;
		float height = Config.Graphics.HEIGHT * Config.Graphics.LEVEL_EDITOR_SCALE;

		if (mCamera != null) {
			mCamera.viewportHeight = height;
			mCamera.viewportWidth = width;
			mCamera.update();
		} else {
			mCamera = new OrthographicCamera(width , height);
		}
	}

	/**
	 * Sets the level that shall be played and resets tools, invoker, etc.
	 * @param level level to play
	 */
	public void setLevel(Level level) {
		boolean sameLevel = false;

		if (mLevel != null) {
			if (mLevel.equals(level)) {
				sameLevel = true;
			}

			// Dispose the level
			if (ResourceCacheFacade.isLoaded(mLevel.getId(), Level.class) && !sameLevel) {
				ResourceCacheFacade.unload(mLevel, mLevel.getDef());
			} else {
				mLevel.dispose();
			}
		}

		mLevel = level;

		if (mLevel != null) {
			mLevel.addResource(mLevel);
			mLevel.bindResources();


			clearTools();

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
				enemy.activate();
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
		return null;
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(ResourceNames.UI_EDITOR_BUTTONS);
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.load(ResourceNames.SHADER_DEFAULT);
		ResourceCacheFacade.loadAllOf(EnemyActorDef.class, true);
		ResourceCacheFacade.loadAllOf(PickupActorDef.class, true);
		ResourceCacheFacade.loadAllOf(LevelDef.class, false);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_EDITOR_BUTTONS);
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.unload(ResourceNames.SHADER_DEFAULT);
		ResourceCacheFacade.unloadAllOf(EnemyActorDef.class, true);
		ResourceCacheFacade.unloadAllOf(PickupActorDef.class, true);
		ResourceCacheFacade.unloadAllOf(LevelDef.class, false);
	}

	@Override
	protected void onActivate(Outcomes outcome, String message) {
		super.onActivate(outcome, message);

		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);

		// Check so that all resources have been loaded
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			mUnsaved = false;

			// Loading a level
			if (mLoadingLevel != null) {
				Level loadedLevel;
				try {
					loadedLevel = ResourceCacheFacade.get(mLoadingLevel.getLevelId(), Level.class);
					setLevel(loadedLevel);
					mGui.resetValues();
					mGui.hideMsgBoxes();
					mUnsaved = false;
				} catch (UndefinedResourceTypeException e) {
					Gdx.app.error("LevelEditor", e.toString());
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

			switch (mSelectionAction) {
			case LEVEL:
				try {
					mLoadingLevel = ResourceCacheFacade.get(UUID.fromString(message), LevelDef.class);

					// Only load level if it's not the current level we selected
					if (!mLoadingLevel.equals(mLevel.getDef())) {
						ResourceCacheFacade.load(mLoadingLevel.getLevelId(), Level.class, false);
						Scene scene = getLoadingScene();
						if (scene != null) {
							SceneSwitcher.switchTo(scene);
						} else {
							/** @todo remove after we have a loading scene */
							ResourceCacheFacade.finishLoading();
							Level loadedLevel = ResourceCacheFacade.get(mLoadingLevel.getLevelId(), Level.class);
							setLevel(loadedLevel);
							mUnsaved = false;
							mGui.resetValues();
							mGui.hideMsgBoxes();
						}
					} else {
						mLoadingLevel = null;
					}
				} catch (Exception e) {
					Gdx.app.error("LevelEditor", e.toString());
					e.printStackTrace();
				}
				break;

			case PICKUP:
				mInvoker.execute(new CLevelPickupDefSelect(UUID.fromString(message), this));
				break;

			case ENEMY:
				mInvoker.execute(new CLevelEnemyDefSelect(UUID.fromString(message), this));
				break;
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
	 * @param enemyId the enemy id to select
	 * @return true if enemy was selected successfully, false if unsuccessful
	 */
	public boolean selectEnemyDef(UUID enemyId) {
		try {
			if (enemyId != null) {
				EnemyActorDef enemyActorDef = ResourceCacheFacade.get(enemyId, EnemyActorDef.class);

				// Update def
				((ActorTool)mTouchTools[Tools.ENEMY.ordinal()]).setNewActorDef(enemyActorDef);
			} else {
				((ActorTool)mTouchTools[Tools.ENEMY.ordinal()]).setNewActorDef(null);
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
			PickupActorDef oldPickupActorDef = (PickupActorDef) ((ActorTool)mTouchTools[Tools.PICKUP.ordinal()]).getNewActorDef();

			if (pickupId != null) {
				PickupActorDef pickupActorDef = ResourceCacheFacade.get(pickupId, PickupActorDef.class);

				// Load dependencies
				ResourceCacheFacade.load(pickupActorDef, true);
				ResourceCacheFacade.finishLoading();

				// Update def
				((ActorTool)mTouchTools[Tools.PICKUP.ordinal()]).setNewActorDef(pickupActorDef);
			} else {
				((ActorTool)mTouchTools[Tools.PICKUP.ordinal()]).setNewActorDef(null);
			}
			mGui.resetValues();

			// Unload old dependencies
			if (oldPickupActorDef != null) {
				ResourceCacheFacade.unload(oldPickupActorDef, true);
			}
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
		// Scrolling, move the map
		if (mScroller.isScrollingByHand() && pointer == 0) {
			mScroller.touchDragged(x, y);
			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {

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
		mUnsaved = true;
	}

	@Override
	public void onResourceRemoved(IResource resource) {
		mLevel.removeResource(resource.getId());
		mUnsaved = true;
	}

	@Override
	public void onResourceChanged(IResource resource) {
		mUnsaved = true;

		if (resource instanceof EnemyActor) {
			((LevelEditorGui)mGui).resetEnemyOptions();
		}
	}

	@Override
	public void onResourceSelected(IResource deselectedResource, IResource selectedResource) {
		mGui.resetValues();
	}

	/**
	 * Checks for all bound resources that uses  the specified parameter resource.
	 * @param usesResource resource to check for in all other resources
	 * @param foundResources list with all resources that uses
	 */
	public void usesResource(IResource usesResource, ArrayList<IResource> foundResources) {
		if (mLevel != null) {
			mLevel.usesResource(usesResource, foundResources);
		}
	}

	/**
	 * @return true if an enemy is currently selected
	 */
	boolean isEnemySelected() {
		if (mToolType == Tools.ENEMY) {
			return ((AddActorTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource() != null;
		}

		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
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
		// Main menu
		else if (KeyHelper.isBackPressed(keycode)) {
			((EditorGui)mGui).showMainMenu();
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
	 * @param tool the new tool type
	 */
	void switchTool(Tools tool) {
		// Remove old tool
		if (mToolType != null) {
			mInputMultiplexer.removeProcessor(mTouchTools[mToolType.ordinal()]);
			mTouchTools[mToolType.ordinal()].deactivate();
		}

		// Set current tool
		mToolType = tool;

		// add new tool
		if (mToolType != null) {
			mInputMultiplexer.addProcessor(mTouchTools[mToolType.ordinal()]);
			mTouchTools[mToolType.ordinal()].activate();
		}

		if (mGui.isInitialized()) {
			mGui.resetValues();
		}
	}

	/**
	 * Tests to run a game from the current location
	 * @param invulnerable makes the player invulnerable
	 */
	public void runFromHere(boolean invulnerable) {
		GameScene testGame = new GameScene(true, invulnerable);
		Level copyLevel = mLevel.copyKeepId();
		// Because of scaling decrease the x position
		float levelScaling = (Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE - 1) / Config.Graphics.LEVEL_EDITOR_HEIGHT_SCALE;
		float xPosition = mCamera.position.x + mCamera.viewportWidth * 0.5f - mCamera.viewportWidth * levelScaling;
		copyLevel.setXCoord(xPosition);

		// Remove screen triggers before the specified coordinate
		testGame.setLevel(copyLevel);

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
		ResourceSaver.save(mLevel.getDef());
		ResourceSaver.save(mLevel);

		// Load the saved actor and use it instead
		if (!ResourceCacheFacade.isLoaded(mLevel.getDef().getId(), LevelDef.class)) {
			try {
				ResourceCacheFacade.load(mLevel.getDef().getId(), LevelDef.class, false);
				ResourceCacheFacade.load(mLevel.getId(), Level.class, mLevel.getDef());
				ResourceCacheFacade.finishLoading();

				setLevel(ResourceCacheFacade.get(mLevel.getId(), Level.class));
			} catch (Exception e) {
				Gdx.app.error("LevelEditor", "Loading of saved level failed! " + e.toString());
				e.printStackTrace();
			}
		}

		mSaveTimeLast = GameTime.getTotalGlobalTimeElapsed();
		mUnsaved = false;
	}

	@Override
	public void loadDef() {
		mSelectionAction = SelectionActions.LEVEL;

		Scene scene = new SelectDefScene(LevelDef.class, true, true);
		SceneSwitcher.switchTo(scene);
	}

	@Override
	public void newDef() {
		if (mLevel != null) {
			mLevel.dispose();
		}

		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		setLevel(level);

		clearTools();

		mUnsaved = false;
	}

	@Override
	public void duplicateDef() {
		Level level = mLevel.copy();

		setLevel(level);

		mGui.resetValues();
		mUnsaved = true;
		mInvoker.dispose();
	}

	@Override
	public boolean isUnsaved() {
		return mUnsaved;
	}

	/**
	 * Sets the starting speed of the current level
	 * @param speed starting speed of the current level
	 */
	void setLevelStartingSpeed(float speed) {
		if (mLevel != null) {
			mLevel.setSpeed(speed);
			mUnsaved = true;
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
	 * @return version of the level, empty string if no level is available
	 */
	String getLevelVersion() {
		if (mLevel != null) {
			return mLevel.getDef().getVersionString();
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
			mUnsaved = true;
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
			mUnsaved = true;
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
			mUnsaved = true;
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
			mUnsaved = true;
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
		ActorDef actorDef = ((AddActorTool)mTouchTools[Tools.ENEMY.ordinal()]).getNewActorDef();

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
		ActorDef actorDef = ((AddActorTool)mTouchTools[Tools.PICKUP.ordinal()]).getNewActorDef();

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
		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (selectedEnemy != null) {
			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();

			// We have an enemy group
			if (enemyGroup != null) {
				// Just change amount of enemies
				if (cEnemies > 1) {
					ArrayList<EnemyActor> addedEnemies = new ArrayList<EnemyActor>();
					ArrayList<EnemyActor> removedEnemies = new ArrayList<EnemyActor>();

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
				}
			}
			// No enemy group, do we create one?
			else if (cEnemies > 1) {
				enemyGroup = new EnemyGroup();
				mLevel.addResource(enemyGroup);

				enemyGroup.setLeaderEnemy(selectedEnemy);

				ArrayList<EnemyActor> addedEnemies = new ArrayList<EnemyActor>();
				enemyGroup.setEnemyCount(cEnemies, addedEnemies, null);

				for (EnemyActor addedEnemy : addedEnemies) {
					mLevel.addResource(addedEnemy);
				}

				// Set GUI delay value
				((LevelEditorGui)mGui).resetEnemyOptions();
			}
		}
	}

	/**
	 * @return number of enemies in a group
	 */
	int getEnemyCount() {
		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		int cEnemies = 0;

		if (selectedEnemy != null) {
			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();

			if (enemyGroup != null) {
				cEnemies = enemyGroup.getEnemyCount();
			}
			// No group, that means we only have one enemy
			else {
				cEnemies = 1;
			}
		}

		return cEnemies;
	}

	/**
	 * Sets the spawn delay between actors in the same group.
	 * @param delay seconds of delay between actors are activated.
	 */
	void setEnemySpawnDelay(float delay) {
		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (selectedEnemy != null) {
			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();

			// We have an enemy group
			if (enemyGroup != null) {
				enemyGroup.setSpawnTriggerDelay(delay);
			}
		}
	}

	/**
	 * @return spawn delay between actors in the same group, negative value if no group exist
	 */
	float getEnemySpawnDelay() {
		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (selectedEnemy != null) {
			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();

			// We have an enemy group
			if (enemyGroup != null) {
				return enemyGroup.getSpawnTriggerDelay();
			}
		}

		return -1;
	}

	/**
	 * Sets the path type of the selected path
	 * @param pathType type of the path
	 */
	void setPathType(Path.PathTypes pathType) {
		Path selectedPath = (Path) ((PathTool)mTouchTools[Tools.PATH.ordinal()]).getSelectedResource();

		if (selectedPath != null) {
			selectedPath.setPathType(pathType);
		}
	}

	/**
	 * @return current path type, null if no path has been selected
	 */
	PathTypes getPathType() {
		Path selectedPath = (Path) ((PathTool)mTouchTools[Tools.PATH.ordinal()]).getSelectedResource();

		if (selectedPath != null) {
			return selectedPath.getPathType();
		} else {
			return null;
		}
	}

	/**
	 * @return true if a path is selected
	 */
	boolean isPathSelected() {
		if (mToolType == Tools.PATH) {
			return ((Path) ((PathTool)mTouchTools[Tools.PATH.ordinal()]).getSelectedResource()) != null;
		} else {
			return false;
		}
	}

	/**
	 * Sets the current active pickup tool
	 * @param state current active pickup tool
	 */
	void setPickupState(AddActorTool.States state) {
		AddActorTool addActorTool = (AddActorTool) mTouchTools[Tools.PICKUP.ordinal()];
		addActorTool.setState(state);
	}

	/**
	 * @return current active pickup tool state
	 */
	AddActorTool.States getPickupState() {
		return ((AddActorTool)mTouchTools[Tools.PICKUP.ordinal()]).getState();
	}

	/**
	 * Sets the current active static terrain tool
	 * @param state current active static terrain tool
	 */
	void setStaticTerrainState(DrawActorTool.States state) {
		DrawActorTool drawActorTool = (DrawActorTool) mTouchTools[Tools.STATIC_TERRAIN.ordinal()];
		drawActorTool.setState(state);
	}

	/**
	 * @return current active static terrain tool state
	 */
	DrawActorTool.States getStaticTerrainState() {
		return ((DrawActorTool)mTouchTools[Tools.STATIC_TERRAIN.ordinal()]).getState();
	}

	/**
	 * Sets the active enemy tool state
	 * @param state current active enemy tool state
	 */
	void setEnemyState(AddEnemyTool.States state) {
		AddEnemyTool addEnemyTool = (AddEnemyTool) mTouchTools[Tools.ENEMY.ordinal()];
		addEnemyTool.setEnemyState(state);
	}

	/**
	 * Resets the enemy state to the active add actor state.
	 */
	void resetEnemyState() {
		AddEnemyTool addEnemyTool = (AddEnemyTool) mTouchTools[Tools.ENEMY.ordinal()];

		switch (addEnemyTool.getState()) {
		case ADD:
			addEnemyTool.setEnemyState(AddEnemyTool.States.ADD);
			break;

		case REMOVE:
			addEnemyTool.setEnemyState(AddEnemyTool.States.REMOVE);
			break;

		case MOVE:
			addEnemyTool.setEnemyState(AddEnemyTool.States.MOVE);
			break;

		case SELECT:
			addEnemyTool.setEnemyState(AddEnemyTool.States.SELECT);
			break;
		}
	}

	/**
	 * @return current active enemy tool state
	 */
	AddEnemyTool.States getEnemyState() {
		return ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getEnemyState();
	}

	/**
	 * Sets the active path tool state
	 * @param state new active path tool state
	 */
	void setPathState(PathTool.States state) {
		PathTool pathTool = (PathTool) mTouchTools[Tools.PATH.ordinal()];
		pathTool.setState(state);
	}

	/**
	 * @return current path tool state
	 */
	PathTool.States getPathState() {
		return ((PathTool)mTouchTools[Tools.PATH.ordinal()]).getState();
	}

	/**
	 * Sets the active trigger tool state
	 * @param state new active trigger tool state
	 */
	void setTriggerState(TriggerTool.States state) {
		TriggerTool triggerTool = (TriggerTool) mTouchTools[Tools.TRIGGER.ordinal()];
		triggerTool.setState(state);
	}

	/**
	 * @return current state of the trigger tool
	 */
	TriggerTool.States getTriggerState() {
		return ((TriggerTool)mTouchTools[Tools.TRIGGER.ordinal()]).getState();
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

		Scene scene = new SelectDefScene(PickupActorDef.class, false, false);
		SceneSwitcher.switchTo(scene);
	}

	@Override
	public boolean shallAutoSave() {
		return mUnsaved && GameTime.getTotalGlobalTimeElapsed() - mSaveTimeLast >= Config.Editor.AUTO_SAVE_TIME;
	}

	/**
	 * Select enemy
	 */
	void selectEnemy() {
		mSelectionAction = SelectionActions.ENEMY;

		Scene scene = new SelectDefScene(EnemyActorDef.class, false, true);
		SceneSwitcher.switchTo(scene);
	}

	/**
	 * @return true if the selected enemy has an activation trigger, false if not or
	 * if no enemy is selected
	 */
	boolean hasSelectedEnemyActivateTrigger() {
		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (enemy != null) {
			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
			for (TriggerInfo trigger : triggers) {
				if (trigger.action == Actions.ACTOR_ACTIVATE) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @return delay of the activation trigger, negative if no activation trigger has
	 * been set.
	 */
	float getSelectedEnemyActivateTriggerDelay() {
		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (enemy != null) {
			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
			for (TriggerInfo trigger : triggers) {
				if (trigger.action == Actions.ACTOR_ACTIVATE) {
					return trigger.delay;
				}
			}
		}

		return -1;
	}

	/**
	 * Sets the delay of the activation trigger
	 * @param delay seconds of delay
	 */
	void setSelectedEnemyActivateTriggerDelay(float delay) {
		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (enemy != null) {
			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
			for (TriggerInfo trigger : triggers) {
				if (trigger.action == Actions.ACTOR_ACTIVATE) {
					trigger.delay = delay;
				}
			}
		}
	}

	/**
	 * @return true if the selected enemy has an deactivation trigger, false if not or
	 * if no enemy is selected
	 */
	boolean hasSelectedEnemyDeactivateTrigger() {
		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (enemy != null) {
			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
			for (TriggerInfo trigger : triggers) {
				if (trigger.action == Actions.ACTOR_DEACTIVATE) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @return delay of the deactivation trigger, negative if no deactivation trigger has
	 * been set.
	 */
	float getSelectedEnemyDeactivateTriggerDelay() {
		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (enemy != null) {
			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
			for (TriggerInfo trigger : triggers) {
				if (trigger.action == Actions.ACTOR_DEACTIVATE) {
					return trigger.delay;
				}
			}
		}

		return -1;
	}

	/**
	 * Sets the delay of the deactivate trigger
	 * @param delay seconds of delay
	 */
	void setSelectedEnemyDeactivateTriggerDelay(float delay) {
		EnemyActor enemy =  (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedResource();

		if (enemy != null) {
			ArrayList<TriggerInfo> triggers = enemy.getTriggerInfos();
			for (TriggerInfo trigger : triggers) {
				if (trigger.action == Actions.ACTOR_DEACTIVATE) {
					trigger.delay = delay;
				}
			}
		}
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
	Tools getSelectedTool() {
		return mToolType;
	}

	/**
	 * @return currently selected pickup definition
	 */
	public ActorDef getSelectedPickupDef() {
		return ((ActorTool) mTouchTools[Tools.PICKUP.ordinal()]).getNewActorDef();
	}

	/**
	 * @return currently selected enemy definition
	 */
	public ActorDef getSelectedEnemyDef() {
		return ((ActorTool) mTouchTools[Tools.ENEMY.ordinal()]).getNewActorDef();
	}

	/**
	 * All the main tool buttons
	 */
	enum Tools {
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
	private void clearTools() {
		for (TouchTool touchTool : mTouchTools) {
			if (touchTool != null) {
				touchTool.clear();
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
	/** Current selected tool */
	private Tools mToolType = Tools.STATIC_TERRAIN;
	/** All the available tools */
	private TouchTool[] mTouchTools = new TouchTool[Tools.values().length];
	/** Is unsaved since last edit */
	private boolean mUnsaved = false;
	/** Last time we saved */
	private float mSaveTimeLast = GameTime.getTotalGlobalTimeElapsed();
}