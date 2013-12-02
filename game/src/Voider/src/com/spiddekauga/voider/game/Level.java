package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.KryoPostRead;
import com.spiddekauga.utils.KryoPostWrite;
import com.spiddekauga.utils.KryoPreWrite;
import com.spiddekauga.utils.KryoTaggedCopyable;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourceEditorUpdate;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.resources.IResourcePrepareWrite;
import com.spiddekauga.voider.resources.IResourceRender;
import com.spiddekauga.voider.resources.IResourceRenderOrder;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.resources.IResourceUpdate;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceBinder;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.utils.Pools;

/**
 * A game level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Level extends Resource implements KryoPreWrite, KryoPostWrite, KryoPostRead, KryoTaggedCopyable, KryoSerializable, Disposable, IResourceRevision {
	/**
	 * Constructor which creates an new empty level with the bound
	 * level definition
	 * @param levelDef the level definition of this level
	 */
	public Level(LevelDef levelDef) {
		mLevelDef = levelDef;
		mUniqueId = levelDef.getLevelId();
		mSpeed = mLevelDef.getBaseSpeed();
		mCompletedLevel = false;
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
			mXCoord += mSpeed * deltaTime;

			if (mPlayerActor != null && mPlayerActor.getBody() != null) {
				mPlayerActor.getBody().setLinearVelocity(mSpeed, 0.0f);
			}

			if (!mCompletedLevel) {
				if (mXCoord >= mLevelDef.getEndXCoord()) {
					mCompletedLevel = true;
				}
			}


			mPlayerActor.update(deltaTime);

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
			Pools.arrayList.free(resourceUpdates);
			resourceUpdates = null;
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
	 * To easily get all the resources of a specific type after they have
	 * been read.
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
	 * Sets the x-coordinate of the level. This makes the level jump to or start
	 * at the specific place.
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
	 * Makes the level run, this will optimize the update and render processes.
	 * Also makes the map move forward
	 */
	public void run() {
		mRunning = true;
	}

	/**
	 * Renders the level, or rather its actors
	 * @param shapeRenderer shape renderer used for rendering
	 */
	public void render(ShapeRendererEx shapeRenderer) {
		if (mRunning) {
			if (mResourceRenders == null) {
				mResourceRenders = mResourceBinder.getResources(IResourceRender.class);
				//				sortRenderList(mResourceRenders);
			}
			for (IResourceRender resourceRender : mResourceRenders) {
				offsetZValue(shapeRenderer, resourceRender);
				resourceRender.render(shapeRenderer);
				resetZValueOffset(shapeRenderer, resourceRender);
			}
		} else {
			ArrayList<IResourceRender> resourceRenders = mResourceBinder.getResources(IResourceRender.class);
			for (IResourceRender resourceRender : resourceRenders) {
				offsetZValue(shapeRenderer, resourceRender);
				resourceRender.render(shapeRenderer);
				resetZValueOffset(shapeRenderer, resourceRender);
			}
			Pools.arrayList.free(resourceRenders);
			resourceRenders = null;
		}

		if (mPlayerActor != null) {
			mPlayerActor.render(shapeRenderer);
		}
	}

	/**
	 * Renders the levels resources with editor special rendering
	 * @param shapeRenderer shape renderer used for rendering
	 */
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		ArrayList<IResourceEditorRender> resourceRenders = mResourceBinder.getResources(IResourceEditorRender.class);
		//		sortRenderList(resourceRenders);
		for (IResourceEditorRender resourceRender : resourceRenders) {
			offsetZValue(shapeRenderer, resourceRender);
			resourceRender.renderEditor(shapeRenderer);
			resetZValueOffset(shapeRenderer, resourceRender);
		}
		Pools.arrayList.free(resourceRenders);
		resourceRenders = null;
	}

	/**
	 * Offset/Translate the z-value
	 * @param shapeRenderer the shape renderer to translate
	 * @param object information about z-value translation
	 */
	private void offsetZValue(ShapeRendererEx shapeRenderer, IResourceRender object) {
		shapeRenderer.translate(0, 0, object.getRenderOrder().getZValue() - 0.5f);
	}

	/**
	 * Reset z-value offset
	 * @param shapeRenderer the shape renderer to reset the z-value translation
	 * @param object information about z-value translation
	 */
	private void resetZValueOffset(ShapeRendererEx shapeRenderer, IResourceRender object) {
		shapeRenderer.translate(0, 0, -(object.getRenderOrder().getZValue() - 0.5f));
	}

	/**
	 * Offset/Translate the z-value
	 * @param shapeRenderer the shape renderer to translate
	 * @param object information about z-value translation
	 */
	private void offsetZValue(ShapeRendererEx shapeRenderer, IResourceEditorRender object) {
		shapeRenderer.translate(0, 0, object.getRenderOrder().getZValue());
	}

	/**
	 * Reset z-value offset
	 * @param shapeRenderer the shape renderer to reset the z-value translation
	 * @param object information about z-value translation
	 */
	private void resetZValueOffset(ShapeRendererEx shapeRenderer, IResourceEditorRender object) {
		shapeRenderer.translate(0, 0, -(object.getRenderOrder().getZValue()));
	}

	/**
	 * Sort the rendering objects. I.e. which order the objects should be rendered
	 * so they are displayed correctly.
	 * @param renderObjects the list to sort rendering orders, back objects
	 * will be placed first in the list.
	 */
	@SuppressWarnings("unchecked")
	private void sortRenderList(ArrayList<? extends IResourceRenderOrder> renderObjects) {
		// Create arrays for storing all different render objects
		ArrayList<IResourceRenderOrder>[] sortedLists = new ArrayList[RenderOrders.values().length];
		for (int i = 0; i < sortedLists.length; ++i) {
			sortedLists[i] = Pools.arrayList.obtain();
		}


		// Sort objects into one of the lists
		for (IResourceRenderOrder object : renderObjects) {
			sortedLists[object.getRenderOrder().getOrder()].add(object);
		}


		// Readd all objects to the original render list, starting with lowest (one in back) priority
		renderObjects.clear();
		ArrayList<IResourceRenderOrder> castRenderObjects = (ArrayList<IResourceRenderOrder>) renderObjects;
		for (int i = sortedLists.length - 1; i >= 0; --i) {
			for (IResourceRenderOrder object : sortedLists[i]) {
				castRenderObjects.add(object);
			}
		}

		Pools.arrayList.freeAll(sortedLists);
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
	public void addResource(ArrayList<? extends IResource> resources) {
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
	public void removeResource(ArrayList<? extends IResource> resources) {
		for (IResource resource : resources) {
			removeResource(resource.getId());
		}
	}

	/**
	 * Create default triggers for the enemies that doesn't have
	 * an activate trigger yet—if they are in the group they
	 * need to be the leader.
	 */
	public void createDefaultTriggers() {
		ArrayList<EnemyActor> enemies = mResourceBinder.getResources(EnemyActor.class);
		for (EnemyActor enemy : enemies) {
			// Check enemy group
			EnemyGroup enemyGroup = enemy.getEnemyGroup();
			if (enemyGroup == null || enemy.isGroupLeader()) {

				// Check already have an activate trigger
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
		Pools.arrayList.free(enemies);
		enemies = null;
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
		mResourceBinder.removeResource(getId(), false);

		ArrayList<Disposable> disposables = mResourceBinder.getResources(Disposable.class);
		for (Disposable disposable : disposables) {
			disposable.dispose();
		}

		Pools.arrayList.free(disposables);
		disposables = null;
		if (mResourceRenders != null) {
			Pools.arrayList.free(mResourceRenders);
			mResourceRenders = null;
		}
		if (mResourceUpdates != null) {
			Pools.arrayList.free(mResourceUpdates);
			mResourceUpdates = null;
		}
	}

	/**
	 * @return the level definition
	 */
	public LevelDef getDef() {
		return mLevelDef;
	}

	/**
	 * Calculates the starting position of the level
	 */
	public void calculateStartPosition() {
		float startPosition = Float.MAX_VALUE;

		ArrayList<IResourcePosition> resources = mResourceBinder.getResources(IResourcePosition.class);

		for (IResourcePosition resource : resources) {
			float position = resource.getPosition().x - resource.getBoundingRadius();

			if (position < startPosition) {
				startPosition = position;
			}
		}

		startPosition -= Config.Level.START_COORD_OFFSET;
		mLevelDef.setStartXCoord(startPosition);

		Pools.arrayList.free(resources);
		resources = null;
	}

	/**
	 * Calculate the end position of the level
	 */
	public void calculateEndPosition() {
		float endPosition = Float.MIN_VALUE;

		ArrayList<IResourcePosition> resources = mResourceBinder.getResources(IResourcePosition.class);

		for (IResourcePosition resource : resources) {
			float position = resource.getPosition().x + resource.getBoundingRadius();

			if (position > endPosition) {
				endPosition = position;
			}
		}

		endPosition += Config.Level.END_COORD_OFFSET;
		mLevelDef.setEndXCoord(endPosition);

		Pools.arrayList.free(resources);
		resources = null;
	}

	@Override
	public void preWrite() {
		for (IResourcePrepareWrite resource : mResourceBinder.getResources(IResourcePrepareWrite.class)) {
			resource.prepareWrite();
		}


		// Remove multiple enemies from the group, i.e. only save the leader.
		if (Actor.isEditorActive()) {
			@SuppressWarnings("unchecked")
			ArrayList<EnemyActor> removeEnemies = Pools.arrayList.obtain();

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

			Pools.arrayList.free(removeEnemies);
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
			@SuppressWarnings("unchecked")
			ArrayList<EnemyActor> addEnemies = Pools.arrayList.obtain();

			for (Entry<EnemyGroup, Integer> entry : mGroupEnemiesSave.entrySet()) {
				EnemyGroup enemyGroup = entry.getKey();
				enemyGroup.setEnemyCount(entry.getValue(), addEnemies, null);
			}
			mGroupEnemiesSave.clear();

			// Actually add all the enemies
			for (EnemyActor addEnemy : addEnemies) {
				mResourceBinder.addResource(addEnemy);
			}

			Pools.arrayList.free(addEnemies);
			addEnemies = null;
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

	@Override
	public void read(Kryo kryo, Input input) {
		@SuppressWarnings("unused")
		int classRevision = input.readInt(true);

		// LevelDef
		UUID levelDefId = kryo.readObject(input, UUID.class);
		int revision = input.readInt(false);
		mLevelDef = ResourceCacheFacade.get(null, levelDefId, revision);
	}

	@Override
	public void copy(Object fromOriginal) {
		if (fromOriginal instanceof Level) {
			Level fromLevel = (Level)fromOriginal;
			mLevelDef = fromLevel.mLevelDef;
		}
	}

	@Override
	public <ResourceType> ResourceType copyNewResource() {
		ResourceType copy = copy();

		Level copyLevel = (Level)copy;

		// Create a copy of the level definition too
		LevelDef levelDef = (LevelDef) mLevelDef.copyNewResource();
		copyLevel.mUniqueId = levelDef.getLevelId();
		copyLevel.mLevelDef = levelDef;

		return copy;
	}

	/**
	 * Default constructor, used when loading levels.
	 */
	protected Level() {
		// Does nothing
	}


	/** Contains all the resources used in this level */
	@Tag(13) private ResourceBinder mResourceBinder = new ResourceBinder();
	/** All resources that needs updating */
	private ArrayList<IResourceUpdate> mResourceUpdates = null;
	/** All resources that shall be rendered */
	private ArrayList<IResourceRender> mResourceRenders = null;
	/** Current x coordinate (of the screen's left edge) */
	@Tag(14) private float mXCoord = 0.0f;
	/** Level definition for this level */
	private LevelDef mLevelDef = null;
	/** Current speed of the level */
	@Tag(15) private float mSpeed;
	/** If the level has been completed */
	@Tag(16) private boolean mCompletedLevel;
	/** True if the level is running */
	private boolean mRunning = false;
	/** The player actor */
	private PlayerActor mPlayerActor = null;
	/** Multiple enemies in a group, but just save the leader and number of enemies */
	@Tag(103) private Map<EnemyGroup, Integer> mGroupEnemiesSave = new HashMap<EnemyGroup, Integer>();

	/** Revision of the actor */
	protected final int CLASS_REVISION = 1;
}
