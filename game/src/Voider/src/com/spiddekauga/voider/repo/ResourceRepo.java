package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.IOutstreamProgressListener;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.network.entities.DefEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse.Statuses;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.utils.Pools;

/**
 * Common resource repository for both web and local. Handles requests that affects
 * both.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceRepo implements ICallerResponseListener {
	/**
	 * Protected constructor to enforce singleton usage
	 */
	protected ResourceRepo() {
		// Does nothing
	}

	/**
	 * @return singleton instance of ResourceRepo
	 */
	public static ResourceRepo getInstance() {
		if (mInstance == null) {
			mInstance = new ResourceRepo();
		}

		return mInstance;
	}

	/**
	 * Publish an actor (and its unpublished dependencies) to the server
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param actorDef the actor to publish
	 */
	public void publish(ICallerResponseListener responseListener, IOutstreamProgressListener progressListener, ActorDef actorDef) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> resources = Pools.arrayList.obtain();

		resources.addAll(getNonPublishedDependencies(actorDef));
		resources.add(actorDef);

		publish(responseListener, progressListener, resources);
	}

	/**
	 * Publish a level (and its unpublished dependencies) to the server
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param level the level to publish
	 */
	public void publish(ICallerResponseListener responseListener, IOutstreamProgressListener progressListener, Level level) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> resources = Pools.arrayList.obtain();

		resources.addAll(getNonPublishedDependencies(level.getDef()));
		resources.add(level);
		resources.add(level.getDef());

		publish(responseListener, progressListener, resources);
	}

	/**
	 * Publish a campaign (and its levels and unpublished dependencies) to the server
	 * @param campaignDef the campaign to publish
	 * @param responseListener listens to the web response
	 * @return true if publish was successful
	 */
	// TODO

	/**
	 * Publish all the resources. In addition frees the ArrayList
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param resources all resources to publish
	 */
	private void publish(ICallerResponseListener responseListener, IOutstreamProgressListener progressListener, ArrayList<IResource> resources) {
		// Publish to server
		mWebRepo.publish(resources, progressListener, this, responseListener);
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof PublishMethodResponse) {
			if (((PublishMethodResponse) response).status == Statuses.SUCCESS) {
				for (DefEntity defEntity : ((PublishMethod)method).defs) {
					ResourceLocalRepo.removeRevisions(defEntity.resourceId);
					ResourceLocalRepo.setPublished(defEntity.resourceId, true);

					// If level set level as published too
					if (defEntity instanceof LevelDefEntity) {
						ResourceLocalRepo.removeRevisions(((LevelDefEntity) defEntity).levelId);
						ResourceLocalRepo.setPublished(((LevelDefEntity) defEntity).levelId, true);
					}
				}
			}
		}

	}

	/**
	 * Get all non published dependencies from the specified definition
	 * @param def the definition to get all non published dependencies from
	 * @return all non published dependencies of def
	 */
	public static ArrayList<Def> getNonPublishedDependencies(Def def) {
		if (def != null) {
			@SuppressWarnings("unchecked")
			HashSet<UUID> uuidDeps = Pools.hashSet.obtain();
			@SuppressWarnings("unchecked")
			ArrayList<Def> dependencies = Pools.arrayList.obtain();

			getNonPublishedDependencies(def, uuidDeps, dependencies);

			Pools.hashSet.free(uuidDeps);
			return dependencies;
		}
		return null;
	}

	/**
	 * Gets all the non-published def dependencies of the the specified definition
	 * @param def the definition to get the external dependencies from
	 * @param foundUuids all the found dependencies' UUID
	 * @param dependencies all non-published dependencies
	 */
	private static final void getNonPublishedDependencies(Def def, Set<UUID> foundUuids, ArrayList<Def> dependencies) {
		for (Entry<UUID, AtomicInteger> entry : def.getExternalDependencies().entrySet()) {
			if (foundUuids.contains(entry.getKey()) && !ResourceLocalRepo.isPublished(def.getId())) {
				foundUuids.add(entry.getKey());

				Def dependency = ResourceCacheFacade.get(entry.getKey());
				if (dependency != null) {
					dependencies.add(dependency);

					getNonPublishedDependencies(dependency, foundUuids, dependencies);
				} else {
					Gdx.app.error("ResourceRepo", "Could not find dependency when publishing...");
				}
			}
		}
	}


	/** Instance of this class */
	private static ResourceRepo mInstance = null;
	/** ResourceWebRepo */
	protected ResourceWebRepo mWebRepo = ResourceWebRepo.getInstance();
}
