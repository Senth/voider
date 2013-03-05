package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.resources.IResource;
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
		mActors = new ArrayList<Actor>();
		mPaths = new ArrayList<Path>();
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
	 * Binds all the triggers
	 */
	public void bindTriggers() {
		//		ArrayList<ITriggerListener> triggerListeners = mResourceBinder.getResources(ITriggerListener.class);
		//
		//		for (ITriggerListener triggerListener : triggerListeners) {
		//			for (TriggerInfo triggerInfo : triggerListener.getTriggerInfos()) {
		//				Trigger trigger = mTriggers.get(triggerInfo.triggerId);
		//
		//				if (trigger != null) {
		//					trigger.addListener(triggerInfo);
		//				} else {
		//					Gdx.app.error("Level", "Could not find trigger to bind with!");
		//				}
		//			}
		//		}
	}

	/**
	 * Unbinds all the triggers
	 */
	public void unbindTriggers() {
		for (ObjectMap.Entry<UUID, Trigger> entry : mTriggers.entries()) {
			entry.value.clearListeners();
		}
	}

	/**
	 * Updates all the actors in the level.
	 * This shall always be run, even in the editor
	 * @param run set this to true if you want to play the level
	 * as this will make the screen move, the units move etc
	 */
	public void update(boolean run) {
		// Make the map move forward
		if (run) {
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
			for (Actor actor : mActors) {
				actor.editorUpdate();
			}
		}
	}

	/**
	 * @return a copy of this level.
	 * @pre world has to been changed to the new world before copy is called()
	 */
	@Override
	public <ResourceType> ResourceType copy() {
		Json json = new Json();
		String jsonString = json.toJson(this);
		Level level = json.fromJson(Level.class, jsonString);

		// Create a copy of the level definition too
		LevelDef levelDef = (LevelDef) mLevelDef.copy();
		level.mUniqueId = levelDef.getLevelId();
		level.mLevelDef = levelDef;

		return (ResourceType) level;
	}

	/**
	 * @return a copy of this level without changing the ID of it
	 */
	public Level copyKeepId() {
		Json json = new Json();
		String jsonString = json.toJson(this);
		Level level = json.fromJson(Level.class, jsonString);

		level.mLevelDef = mLevelDef;

		return level;
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
	 * @return current speed of the level
	 */
	public float getSpeed() {
		return mSpeed;
	}

	/**
	 * Renders the level
	 * @param spriteBatch the SpriteBatch to use for rendering
	 */
	public void render(SpriteBatch spriteBatch) {
		for (Actor actor : mActors) {
			if (actor.getBody() != null) {
				actor.render(spriteBatch);
			}
		}
		mPlayerActor.render(spriteBatch);
	}

	/**
	 * Renders extra information from actors when level editor is active
	 * @param spriteBatch the SpriteBatch to use for rendering
	 */
	public void renderEditor(SpriteBatch spriteBatch) {
		for (Actor actor : mActors) {
			if (actor.getBody() != null) {
				actor.renderEditor(spriteBatch);
			}
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
		} else if (resource instanceof Path) {
			mPaths.add((Path) resource);
		}
	}

	/**
	 * Removes a resource from the level
	 * @param resourceId id of the resource
	 */
	public void removeResource(UUID resourceId) {
		IResource removedResource = mResourceBinder.removeResource(resourceId);

		if (removedResource instanceof Actor) {
			removeActor(resourceId);
		} else if (removedResource instanceof Path) {
			mPaths.remove(removedResource);
		}
	}

	/**
	 * Adds an actor to the level
	 * @param actor the actor to add to the level
	 */
	private void addActor(Actor actor) {
		mActors.add(actor);

		// Add to dependency, if it doesn't load its own def
		if (!actor.savesDef()) {
			mLevelDef.addDependency(actor.getDef());
		}
	}

	/**
	 * Removes an actor from the level
	 * @param actorId the actor to remove
	 */
	private void removeActor(UUID actorId) {
		Actor actor = null;
		for (int i = 0; i < mActors.size(); ++i) {
			if (mActors.get(i).equals(actorId)) {
				actor = mActors.remove(i);
				break;
			}
		}

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

	/**
	 * Returns all paths, only applicable if an editor is active
	 * @return all paths, null or empty if no editor is active
	 */
	public ArrayList<Path> getPaths() {
		return mPaths;
	}

	/**
	 * Checks if the actor exists inside this level
	 * @param actor the actor check if it exist
	 * @return true if this level contains the specified actor
	 */
	public boolean containsActor(Actor actor) {
		return mActors.contains(actor);
	}

	/**
	 * @return all actors in the level
	 */
	public ArrayList<Actor> getActors() {
		return mActors;
	}

	@Override
	public void dispose() {
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
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		mResourceBinder = json.readValue("mResourceBinder", ResourceBinder.class, jsonData);
		mResourceBinder.addResource(this);
		mResourceBinder.bindResources();

		mXCoord = json.readValue("mXCoord", float.class, jsonData);
		mSpeed = json.readValue("mSpeed", float.class, jsonData);
		mCompletedLevel = json.readValue("mCompletedLevel", boolean.class, jsonData);

		mActors = mResourceBinder.getResources(Actor.class);
		ArrayList<Trigger> triggers = mResourceBinder.getResources(Trigger.class);
		for (Trigger trigger : triggers) {
			mTriggers.put(trigger.getId(), trigger);
		}

		if (Actor.isEditorActive()) {
			mPaths = mResourceBinder.getResources(Path.class);
		}

		// Get the actual LevelDef
		UUID levelDefId = json.readValue("mLevelDefId", UUID.class, jsonData);
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

	/** Contains all the resources used in this level */
	private ResourceBinder mResourceBinder = new ResourceBinder();
	/** All actors in the level */
	private ArrayList<Actor> mActors = null;
	/** All paths, only used when editor is active */
	private ArrayList<Path> mPaths = null;
	/** All resources that needs updating */
	private ArrayList<IResourceUpdate> mResourceUpdates = null;
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
	/** The player actor */
	private PlayerActor mPlayerActor = null;
}
