package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.kryo.KryoPostRead;
import com.spiddekauga.utils.kryo.KryoPostWrite;
import com.spiddekauga.utils.kryo.KryoPreWrite;
import com.spiddekauga.utils.kryo.KryoTaggedCopyable;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourceEditorRenderSprite;
import com.spiddekauga.voider.resources.IResourceEditorUpdate;
import com.spiddekauga.voider.resources.IResourceHasDef;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.resources.IResourcePrepareWrite;
import com.spiddekauga.voider.resources.IResourceRenderShape;
import com.spiddekauga.voider.resources.IResourceRenderSprite;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.resources.IResourceUpdate;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceBinder;
import com.spiddekauga.voider.utils.BoundingBox;

/**
 * A game level
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Level extends Resource implements KryoPreWrite, KryoPostWrite, KryoPostRead, KryoTaggedCopyable, KryoSerializable, Disposable,
		IResourceRevision, IResourceHasDef {
	/**
	 * Constructor which creates an new empty level with the bound level definition
	 * @param levelDef the level definition of this level
	 */
	public Level(LevelDef levelDef) {
		mLevelDef = levelDef;
		mUniqueId = levelDef.getLevelId();
		mSpeed = mLevelDef.getBaseSpeed();
		mCompletedLevel = false;
	}

	@Override
	public void set(Resource resource) {
		super.set(resource);

		Level level = (Level) resource;
		mCompletedLevel = level.mCompletedLevel;
		mGroupEnemiesSave = level.mGroupEnemiesSave;
		mLevelDef = level.mLevelDef;
		mResourceBinder = level.mResourceBinder;
		mRenderShapes = level.mRenderShapes;
		mResourceUpdates = level.mResourceUpdates;
		mRunning = level.mRunning;
		mSpeed = level.mSpeed;
		mXCoord = level.mXCoord;
		mBackground = level.mBackground;
	}

	/**
	 * Creates the background for this level
	 */
	private void createBackground() {
		if (mLevelDef.getTheme() != null) {
			mBackground = mLevelDef.getTheme().createBackground();
		}
	}

	/**
	 * @return true if the player has completed the level
	 */
	public boolean isCompletedLevel() {
		return mCompletedLevel;
	}

	/**
	 * Updates all the actors in the level. To optimize set is as running
	 * @param deltaTime elapsed seconds since last frame
	 */
	public void update(float deltaTime) {
		// Make the map move forward
		if (mRunning) {
			if (mBackground == null) {
				createBackground();
			}

			updateCoordinates();

			if (mPlayerActor != null && mPlayerActor.getBody() != null) {
				mPlayerActor.getBody().setLinearVelocity(mSpeed, 0.0f);
			}

			if (!mCompletedLevel) {
				if (mXCoord >= mLevelDef.getEndXCoord()) {
					mCompletedLevel = true;
				}
			}

			if (mPlayerActor != null) {
				mPlayerActor.update(deltaTime);
			}

			// Update resources
			if (mResourceUpdates == null) {
				mResourceUpdates = mResourceBinder.getResources(IResourceUpdate.class);
			}
			for (IResourceUpdate resourceUpdate : mResourceUpdates) {
				resourceUpdate.update(deltaTime);
			}

		} else {
			ArrayList<IResourceEditorUpdate> resourceUpdates = mResourceBinder.getResources(IResourceEditorUpdate.class);
			for (IResourceEditorUpdate resource : resourceUpdates) {
				resource.updateEditor();
			}
			resourceUpdates = null;
		}
	}

	/**
	 * Update the coordinate position
	 */
	private void updateCoordinates() {
		if (mXCoordBody == null) {
			World world = Actor.getWorld();
			if (world != null) {
				BodyDef bodyDef = new BodyDef();
				bodyDef.linearVelocity.x = mSpeed;
				bodyDef.type = BodyType.KinematicBody;
				bodyDef.position.x = mXCoord;
				mXCoordBody = world.createBody(bodyDef);
			}
		}

		if (mXCoordBody != null) {
			mXCoordBody.setLinearVelocity(mSpeed, 0);
			mXCoord = mXCoordBody.getPosition().x;
		}
	}

	@Override
	public int getRevision() {
		return mLevelDef.getRevision();
	}

	@Override
	public void setRevision(int revision) {
		// Does nothing. LevelDef sets the revision.
	}

	/**
	 * To easily get all the resources of a specific type after they have been read.
	 * @param <ResourceType> type of resources to return
	 * @param resourceType the resource type (including derived) to return
	 * @return a list of resources that are instances of the specified type.
	 */
	public <ResourceType> ArrayList<ResourceType> getResources(Class<ResourceType> resourceType) {
		return mResourceBinder.getResources(resourceType);
	}

	/**
	 * Sets the player ship, also determines its location
	 * @param playerActor
	 */
	public void setPlayer(PlayerActor playerActor) {
		mPlayerActor = playerActor;
	}

	/**
	 * Sets the x-coordinate of the level. This makes the level jump to or start at the
	 * specific place.
	 * @param x the current x-coordinate of the level
	 */
	public void setStartPosition(float x) {
		mXCoord = x;
		mLevelDef.setStartXCoord(x);
	}

	/**
	 * @return current x-coordinate of the map
	 */
	public float getXCoord() {
		return mXCoord;
	}

	/**
	 * Sets the speed of the level
	 * @param speed new speed of the level
	 */
	public void setSpeed(float speed) {
		mSpeed = speed;
	}

	/**
	 * @return current speed of the level
	 */
	public float getSpeed() {
		return mSpeed;
	}

	/**
	 * Makes the level run, this will optimize the update and render processes. Also makes
	 * the map move forward
	 */
	public void run() {
		mRunning = true;
	}

	/**
	 * Renders the background
	 * @param spriteBatch used for rendering sprites.
	 */
	public void renderBackground(SpriteBatch spriteBatch) {
		renderBackground(spriteBatch, 0, Gdx.graphics.getHeight());
	}

	/**
	 * Renders the background on a part of the screen
	 * @param spriteBatch used for rendering
	 * @param y where on the y-axis to start rendering
	 * @param height how high the background should be
	 */
	public void renderBackground(SpriteBatch spriteBatch, int y, int height) {
		if (mBackground != null) {
			float diffCoords = mXCoord - mLevelDef.getStartXCoord();
			mBackground.render(spriteBatch, diffCoords);
		}
	}

	/**
	 * Check if a resource is within the window box and we should render it
	 * @param resource
	 * @param windowBox
	 * @return true if the resource is within the window box (if it has a bounding box)
	 */
	public boolean shouldRender(IResource resource, BoundingBox windowBox) {
		if (resource instanceof IResourcePosition) {
			BoundingBox resourceBox = ((IResourcePosition) resource).getBoundingBox();
			return windowBox.overlaps(resourceBox);
		} else {
			return true;
		}
	}

	/**
	 * Renders the level, or rather its actors
	 * @param shapeRenderer shape renderer used for rendering
	 * @param windowBox bounding box for the window, i.e. we don't render anything outside
	 *        of this
	 */
	public void render(ShapeRendererEx shapeRenderer, BoundingBox windowBox) {
		// Only get once if running
		if (mRunning) {
			if (mRenderShapes == null) {
				mRenderShapes = mResourceBinder.getResources(IResourceRenderShape.class);
			}
		} else {
			mRenderShapes = mResourceBinder.getResources(IResourceRenderShape.class);
		}

		// Render
		for (IResourceRenderShape shape : mRenderShapes) {
			if (shouldRender(shape, windowBox)) {
				shape.renderShape(shapeRenderer);
			}
		}

		if (mPlayerActor != null) {
			mPlayerActor.renderShape(shapeRenderer);
		}
	}

	/**
	 * Renders the sprites for the level
	 * @param spriteBatch batch for rendering sprites
	 * @param windowBox bounding box for the window, i.e. we don't render anything outside
	 *        of this
	 */
	public void render(SpriteBatch spriteBatch, BoundingBox windowBox) {
		if (mRunning) {
			if (mRenderSprites == null) {
				mRenderSprites = mResourceBinder.getResources(IResourceRenderSprite.class);
			}
		} else {
			mRenderSprites = mResourceBinder.getResources(IResourceRenderSprite.class);
		}

		// Render
		for (IResourceRenderSprite sprite : mRenderSprites) {
			if (shouldRender(sprite, windowBox)) {
				sprite.renderSprite(spriteBatch);
			}
		}

		if (mPlayerActor != null) {
			mPlayerActor.renderSprite(spriteBatch);
		}
	}

	/**
	 * Renders the level's resources with editor special rendering
	 * @param shapeRenderer shape renderer used for rendering
	 */
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		ArrayList<IResourceEditorRender> resourceRenders = mResourceBinder.getResources(IResourceEditorRender.class);
		for (IResourceEditorRender resourceRender : resourceRenders) {
			resourceRender.renderEditor(shapeRenderer);
		}
	}

	/**
	 * Renders the level resources' editor sprites
	 * @param spriteBatch batch for rendering sprites
	 */
	public void renderEditor(SpriteBatch spriteBatch) {
		ArrayList<IResourceEditorRenderSprite> resourceSprites = mResourceBinder.getResources(IResourceEditorRenderSprite.class);
		for (IResourceEditorRenderSprite resourceSprite : resourceSprites) {
			resourceSprite.renderEditorSprite(spriteBatch);
		}
	}

	/**
	 * Adds a resource to the level
	 * @param resource the resource to add to the level
	 */
	public void addResource(IResource resource) {
		mResourceBinder.addResource(resource);

		if (resource instanceof Actor) {
			addActor((Actor) resource);
		}
	}

	/**
	 * Adds a number of resources to the level
	 * @param resources all resources
	 */
	public void addResource(IResource... resources) {
		for (IResource resource : resources) {
			addResource(resource);
		}
	}

	/**
	 * Adds a number of resources to the level
	 * @param resources an arraylist of resources
	 */
	public void addResource(Iterable<? extends IResource> resources) {
		for (IResource resource : resources) {
			addResource(resource);
		}
	}

	/**
	 * Removes a resource from the level
	 * @param resourceId id of the resource
	 */
	public void removeResource(UUID resourceId) {
		IResource removedResource = mResourceBinder.removeResource(resourceId, true);

		if (removedResource instanceof Actor) {
			removeActor((Actor) removedResource);
		}
	}

	/**
	 * Removes all the the specified resources from the level
	 * @param resources all resources to remove from the level
	 */
	public void removeResources(Iterable<? extends IResource> resources) {
		for (IResource resource : resources) {
			removeResource(resource.getId());
		}
	}

	/**
	 * Create default triggers for the enemies that doesn't have an activate trigger
	 * yet—if they are in the group they need to be the leader.
	 */
	public void createDefaultTriggers() {
		ArrayList<EnemyActor> enemies = mResourceBinder.getResources(EnemyActor.class);
		for (EnemyActor enemy : enemies) {
			// Create activate trigger
			if (TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_ACTIVATE) == null) {
				TriggerInfo defaultTrigger = enemy.createDefaultActivateTrigger(this);

				if (defaultTrigger != null) {
					addResource(defaultTrigger.trigger);
					enemy.addTrigger(defaultTrigger);
				}
			}

			// AI enemies, add an deactivate trigger
			if (enemy.getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
				if (TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_DEACTIVATE) == null) {
					TriggerInfo defaultTrigger = enemy.createDefaultDeactivateTrigger();

					if (defaultTrigger != null) {
						addResource(defaultTrigger.trigger);
						enemy.addTrigger(defaultTrigger);
					}
				}
			}
		}
	}

	/**
	 * Adds an actor to the level
	 * @param actor the actor to add to the level
	 */
	private void addActor(Actor actor) {
		// Add to dependency, if it doesn't load its own def
		if (!actor.savesDef()) {
			mLevelDef.addDependency(actor.getDef());
		}
	}

	/**
	 * Removes an actor from the level
	 * @param actor the actor to remove
	 */
	private void removeActor(Actor actor) {
		if (actor != null) {
			actor.destroyBody();

			// Remove dependency
			if (!actor.savesDef()) {
				mLevelDef.removeDependency(actor.getDef().getId());
			}
		} else {
			Gdx.app.error("Level", "Could not find the actor to remove");
		}
	}

	@Override
	public void dispose() {
		// Remove this level first...
		if (mResourceBinder != null) {
			mResourceBinder.removeResource(getId(), false);

			ArrayList<Disposable> disposables = mResourceBinder.getResources(Disposable.class);
			for (Disposable disposable : disposables) {
				disposable.dispose();
			}
			mResourceBinder = null;
		}

		if (mRenderShapes != null) {
			mRenderShapes = null;
		}
		if (mResourceUpdates != null) {
			mResourceUpdates = null;
		}
	}

	/**
	 * @return the level definition
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <DefType extends Def> DefType getDef() {
		return (DefType) mLevelDef;
	}

	/**
	 * @return level definition
	 */
	public LevelDef getLevelDef() {
		return mLevelDef;
	}

	/**
	 * Calculates both start and end positions of the level
	 */
	public void calculateStartEndPosition() {
		calculateStartPosition();
		calculateEndPosition();
	}

	/**
	 * Calculates the starting position of the level
	 */
	private void calculateStartPosition() {
		float startPosition = Float.MAX_VALUE;

		ArrayList<IResourcePosition> resources = mResourceBinder.getResources(IResourcePosition.class);

		for (IResourcePosition resource : resources) {
			float leftPos = Float.MAX_VALUE;

			// For enemies we use the default trigger
			if (resource instanceof EnemyActor) {
				EnemyActor enemy = (EnemyActor) resource;

				TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(enemy, Actions.ACTOR_ACTIVATE);
				// Check with the default trigger.
				// We don't use the existing trigger since we will iterate over that
				// resource anyhow.
				if (triggerInfo == null) {
					leftPos = enemy.calculateDefaultActivateTriggerPosition(mSpeed);
				}
			}
			// Use bounding box for everything else
			else {
				leftPos = resource.getBoundingBox().getLeft();
			}

			if (leftPos < startPosition) {
				startPosition = leftPos;
			}
		}

		// Only set start position if we had any resources
		if (startPosition != Float.MAX_VALUE) {
			// startPosition -= Config.Level.START_COORD_OFFSET;
			mLevelDef.setStartXCoord(startPosition);
		} else {
			mLevelDef.setStartXCoord(0);
		}
	}

	/**
	 * Calculate the end position of the level
	 */
	private void calculateEndPosition() {
		float endPosition = Float.MIN_VALUE;

		ArrayList<IResourcePosition> resources = mResourceBinder.getResources(IResourcePosition.class);

		for (IResourcePosition resource : resources) {
			float position = resource.getPosition().x + resource.getBoundingRadius();

			if (position > endPosition) {
				endPosition = position;
			}
		}

		// Only set end position if we had any resources
		if (endPosition > Float.MIN_VALUE) {
			endPosition += Config.Level.END_COORD_OFFSET;
			mLevelDef.setEndXCoord(endPosition);
		} else {
			mLevelDef.setEndXCoord(100);
		}
	}

	@Override
	public void preWrite() {
		for (IResourcePrepareWrite resource : mResourceBinder.getResources(IResourcePrepareWrite.class)) {
			resource.prepareWrite();
		}


		// Remove multiple enemies from the group, i.e. only save the leader.
		if (Actor.isEditorActive()) {
			ArrayList<EnemyActor> removeEnemies = new ArrayList<>();

			for (EnemyActor enemyActor : mResourceBinder.getResources(EnemyActor.class)) {
				int cEnemiesBefore = removeEnemies.size();

				EnemyGroup enemyGroup = enemyActor.getEnemyGroup();

				if (enemyActor.isGroupLeader() && enemyGroup != null) {
					enemyGroup.setEnemyCount(1, null, removeEnemies);

					// Save number of enemies in the group
					mGroupEnemiesSave.put(enemyGroup, removeEnemies.size() - cEnemiesBefore + 1);
				}
			}

			// Actually remove the enemies
			for (EnemyActor removeEnemy : removeEnemies) {
				mResourceBinder.removeResource(removeEnemy.getId(), false);
				removeEnemy.dispose();
			}
			removeEnemies = null;
		}
	}

	@Override
	public void postWrite() {
		postRead();
	}

	@Override
	public void postRead() {
		// Add all the removed enemies again
		if (!mGroupEnemiesSave.isEmpty()) {
			ArrayList<EnemyActor> addEnemies = new ArrayList<>();

			for (Entry<EnemyGroup, Integer> entry : mGroupEnemiesSave.entrySet()) {
				EnemyGroup enemyGroup = entry.getKey();
				enemyGroup.setEnemyCount(entry.getValue(), addEnemies, null);
			}
			mGroupEnemiesSave.clear();

			// Actually add all the enemies
			for (EnemyActor addEnemy : addEnemies) {
				mResourceBinder.addResource(addEnemy);
			}
			addEnemies = null;
		}

		// Set correct base speed if this isn't a save file
		if (mClassVersion >= 2) {
			if (mXCoord == mLevelDef.getStartXCoord()) {
				mSpeed = mLevelDef.getBaseSpeed();
			}
		}
	}

	@Override
	public void write(Kryo kryo, Output output) {
		// Class structure revision
		output.writeInt(CLASS_REVISION, true);

		// LevelDef
		kryo.writeObject(output, mLevelDef.getId());
		output.writeInt(mLevelDef.getRevision(), false);
	}

	@SuppressWarnings("unused")
	@Override
	public void read(Kryo kryo, Input input) {
		mClassVersion = input.readInt(true);

		// LevelDef
		UUID levelDefId = kryo.readObject(input, UUID.class);
		int revision = input.readInt(false);
		mLevelDef = ResourceCacheFacade.get(levelDefId);
	}

	@Override
	public void copy(Object fromOriginal) {
		if (fromOriginal instanceof Level) {
			Level fromLevel = (Level) fromOriginal;
			mLevelDef = fromLevel.mLevelDef;
			mSpeed = mLevelDef.getBaseSpeed();
		}
	}

	@Override
	public <ResourceType> ResourceType copyNewResource() {
		ResourceType copy = copy();

		Level copyLevel = (Level) copy;

		// Create a copy of the level definition too
		LevelDef levelDef = (LevelDef) mLevelDef.copyNewResource();
		copyLevel.mUniqueId = levelDef.getLevelId();
		copyLevel.mLevelDef = levelDef;
		copyLevel.mSpeed = levelDef.getBaseSpeed();

		return copy;
	}

	/**
	 * Default constructor, used when loading levels.
	 */
	protected Level() {
		// Does nothing
	}

	private LevelBackground mBackground = null;
	/** Contains all the resources used in this level */
	@Tag(13) private ResourceBinder mResourceBinder = new ResourceBinder();
	/** All resources that needs updating */
	private ArrayList<IResourceUpdate> mResourceUpdates = null;
	/** All shape resources that shall be rendered */
	private ArrayList<IResourceRenderShape> mRenderShapes = null;
	/** All sprite resources that shall be rendered */
	private ArrayList<IResourceRenderSprite> mRenderSprites = null;
	/** Current x coordinate (of the screen's left edge) */
	@Tag(14) private float mXCoord = 0.0f;
	/** Current x coordinate using a body to get the correct position */
	private Body mXCoordBody = null;
	/** Level definition for this level */
	private LevelDef mLevelDef = null;
	/** Current speed of the level */
	@Tag(15) private float mSpeed;
	/** If the level has been completed */
	@Tag(16) private boolean mCompletedLevel;
	/** True if the level is running */
	private boolean mRunning = false;
	private PlayerActor mPlayerActor = null;
	/** Read class version */
	private int mClassVersion = CLASS_REVISION;
	/** Multiple enemies in a group, but just save the leader and number of enemies */
	@Tag(103) private Map<EnemyGroup, Integer> mGroupEnemiesSave = new HashMap<EnemyGroup, Integer>();


	/** Revision this class structure */
	protected static final int CLASS_REVISION = 2;
}
