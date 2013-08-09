package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.spiddekauga.utils.JsonWrapper; import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourceEditorUpdate;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.resources.IResourceRender;
import com.spiddekauga.voider.resources.IResourceUpdate;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceBinder;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;

/**
 * A game level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("unchecked")
public class Level extends Resource implements Disposable {
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

		mResourceBinder.addResource(this);
	}

	/**
	 * @return true if the player has completed the level
	 */
	public boolean hasCompletedLevel() {
		return mCompletedLevel;
	}

	/**
	 * Updates all the actors in the level. To optimize set is as running
	 */
	public void update() {
		// Make the map move forward
		if (mRunning) {
			float deltaTime = Gdx.graphics.getDeltaTime();
			mXCoord += mSpeed * deltaTime;

			if (mPlayerActor != null && mPlayerActor.getBody() != null) {
				mPlayerActor.getBody().setLinearVelocity(mSpeed, 0.0f);
			}

			if (!mCompletedLevel) {
				if (mXCoord >= mLevelDef.getEndXCoord()) {
					mCompletedLevel = true;
				}
			}


			mPlayerActor.update(Gdx.graphics.getDeltaTime());

			// Update resources
			if (mResourceUpdates == null) {
				mResourceUpdates = mResourceBinder.getResources(IResourceUpdate.class);
			}
			for (IResourceUpdate resourceUpdate : mResourceUpdates) {
				resourceUpdate.update(deltaTime);
			}

		} else {
			for (IResourceEditorUpdate resource : mResourceBinder.getResources(IResourceEditorUpdate.class)) {
				resource.updateEditor();
			}
		}
	}

	/**
	 * @return a copy of this level.
	 * @pre world has to been changed to the new world before copy is called()
	 */
	@Override
	public <ResourceType> ResourceType copy() {
		Json json = new JsonWrapper();
		String jsonString = json.toJson(this);
		Level level = json.fromJson(Level.class, jsonString);

		// Create a copy of the level definition too
		LevelDef levelDef = (LevelDef) mLevelDef.copy();
		level.mUniqueId = levelDef.getLevelId();
		level.mLevelDef = levelDef;

		// Change references to the new level id
		level.addResource(this);
		level.updateLevelReferences(mUniqueId);

		return (ResourceType) level;
	}

	/**
	 * @return a copy of this level without changing the ID of it
	 */
	public Level copyKeepId() {
		Json json = new JsonWrapper();
		String jsonString = json.toJson(this);
		Level level = json.fromJson(Level.class, jsonString);

		level.mLevelDef = mLevelDef;

		return level;
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
	 * at the specific place. Also updates the player if one exists
	 * @param x the current x-coordinate of the level
	 */
	public void setXCoord(float x) {
		mXCoord = x;
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
	 * Binds all resources, call this after the level has been loaded
	 */
	public void bindResources() {
		mResourceBinder.bindResources();
	}

	/**
	 * Renders the level, or rather its actors
	 * @param shapeRenderer shape renderer used for rendering
	 */
	public void render(ShapeRendererEx shapeRenderer) {
		// TODO IResourceRender for rendering resources
		if (mRunning) {
			if (mResourceRenders == null) {
				mResourceRenders = mResourceBinder.getResources(IResourceRender.class);
			}
			for (IResourceRender resourceRender : mResourceRenders) {
				resourceRender.render(shapeRenderer);
			}
		} else {
			for (IResourceRender resourceRender : mResourceBinder.getResources(IResourceRender.class)) {
				resourceRender.render(shapeRenderer);
			}
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
		for (IResourceEditorRender resourceRender : mResourceBinder.getResources(IResourceEditorRender.class)) {
			resourceRender.renderEditor(shapeRenderer);
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
	 * Removes a resource from the level
	 * @param resourceId id of the resource
	 */
	public void removeResource(UUID resourceId) {
		IResource removedResource = mResourceBinder.removeResource(resourceId);

		if (removedResource instanceof Actor) {
			removeActor((Actor) removedResource);
		}
	}

	/**
	 * Create default triggers for the enemies that doesn't have
	 * an activate trigger yet—if they are in the group they
	 * need to be the leader.
	 */
	public void createDefaultTriggers() {
		for (EnemyActor enemy : mResourceBinder.getResources(EnemyActor.class)) {
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
		mResourceBinder.removeResource(getId());

		for (Disposable disposable : mResourceBinder.getResources(Disposable.class)) {
			disposable.dispose();
		}
	}

	/**
	 * @return the level definition
	 */
	public LevelDef getDef() {
		return mLevelDef;
	}

	/**
	 * Checks for all bound resources that uses  the specified parameter resource.
	 * @param usesResource resource to check for in all other resources
	 * @param foundResources list with all resources that uses
	 */
	public void usesResource(IResource usesResource, ArrayList<IResource> foundResources) {
		mResourceBinder.usesResource(usesResource, foundResources);
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

		mLevelDef.setStartXCoord(startPosition);
	}

	@Override
	public void write(Json json) {
		super.write(json);

		// Remove level from resource binder, so we don't save the level inifinitely
		mResourceBinder.removeResource(getId());

		json.writeValue("mResourceBinder", mResourceBinder);
		json.writeValue("mLevelDefId", mLevelDef.getId());
		json.writeValue("mXCoord", mXCoord);
		json.writeValue("mSpeed", mSpeed);
		json.writeValue("mCompletedLevel", mCompletedLevel);
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		super.read(json, jsonValue);

		mResourceBinder = json.readValue("mResourceBinder", ResourceBinder.class, jsonValue);

		mXCoord = json.readValue("mXCoord", float.class, jsonValue);
		mSpeed = json.readValue("mSpeed", float.class, jsonValue);
		mCompletedLevel = json.readValue("mCompletedLevel", boolean.class, jsonValue);

		ArrayList<Trigger> triggers = mResourceBinder.getResources(Trigger.class);
		for (Trigger trigger : triggers) {
			mTriggers.put(trigger.getId(), trigger);
		}

		// Get the actual LevelDef
		UUID levelDefId = json.readValue("mLevelDefId", UUID.class, jsonValue);
		try {
			mLevelDef = ResourceCacheFacade.get(levelDefId, LevelDef.class);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("Level", "Could not get level def when loading level");
		} catch (GdxRuntimeException e) {
			// The level was just copied without having been saved first...
			// The copy will set level def.
		}
	}

	/**
	 * Default constructor, used when loading levels.
	 */
	protected Level() {
		// Does nothing
	}

	/**
	 * Updates the level references of other resources to the new level id
	 * @param oldId old level id
	 */
	private void updateLevelReferences(UUID oldId) {
		mResourceBinder.replaceResource(oldId, this);
	}

	/** Contains all the resources used in this level */
	private ResourceBinder mResourceBinder = new ResourceBinder();
	/** All resources that needs updating */
	private ArrayList<IResourceUpdate> mResourceUpdates = null;
	/** All resources that shall be rendered */
	private ArrayList<IResourceRender> mResourceRenders = null;
	/** All triggers */
	private ObjectMap<UUID, Trigger> mTriggers = new ObjectMap<UUID, Trigger>();
	/** Current x coordinate (of the screen's left edge) */
	private float mXCoord = 0.0f;
	/** Level definition for this level */
	private LevelDef mLevelDef = null;
	/** Current speed of the level */
	private float mSpeed;
	/** If the level has been completed */
	private boolean mCompletedLevel;
	/** True if the level is running */
	private boolean mRunning = false;
	/** The player actor */
	private PlayerActor mPlayerActor = null;
}
