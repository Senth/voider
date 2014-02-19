package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.utils.Pools;

/**
 * Common resource repository for both web and local. Handles requests that affects
 * both.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceRepo {
	/**
	 * Publish an actor (and its unpublished dependencies) to the server
	 * @param actorDef the actor to publish
	 * @return true if publish was successful
	 */
	public static boolean publish(ActorDef actorDef) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> resources = Pools.arrayList.obtain();

		resources.addAll(getNonPublishedDependencies(actorDef));
		resources.add(actorDef);

		return publish(resources);
	}

	/**
	 * Publish a level (and its unpublished dependencies) to the server
	 * @param level the level to publish
	 * @return true if publish was successful
	 */
	public static boolean publish(Level level) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> resources = Pools.arrayList.obtain();

		resources.addAll(getNonPublishedDependencies(level.getDef()));
		resources.add(level);
		resources.add(level.getDef());

		return publish(resources);
	}

	/**
	 * Publish a campaign (and its levels and unpublished dependencies) to the server
	 * @param campaignDef the campaign to publish
	 * @return true if publish was successful
	 */
	// TODO

	/**
	 * Publish all the resources. In addition frees the ArrayList
	 * @param resources all resources to publish
	 * @return true if publish was successful
	 */
	private static boolean publish(ArrayList<IResource> resources) {
		// Publish to server
		boolean success = ResourceWebRepo.publish(resources);

		// TODO Remove file revisions if successful

		// TODO Remove SQL revisions

		Pools.arrayList.free(resources);

		return false;
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
			if (!def.isPublished() && !foundUuids.contains(entry.getKey())) {
				foundUuids.add(entry.getKey());

				Def dependency = ResourceCacheFacade.get(entry.getKey());
				if (dependency != null) {
					dependencies.add(dependency);

					getNonPublishedDependencies(dependency, foundUuids, dependencies);
				} else {
					Gdx.app.error("Editor", "Could not find dependency when publishing...");
				}
			}
		}
	}
}
