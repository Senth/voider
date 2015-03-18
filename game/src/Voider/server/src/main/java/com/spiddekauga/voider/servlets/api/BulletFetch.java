package com.spiddekauga.voider.servlets.api;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.BulletDefEntity;
import com.spiddekauga.voider.network.resource.BulletFetchMethod;
import com.spiddekauga.voider.network.resource.BulletFetchResponse;
import com.spiddekauga.voider.network.resource.FetchStatuses;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ActorFetch;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables;

/**
 * Servlet for fetching bullet information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BulletFetch extends ActorFetch<BulletDefEntity> {

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof BulletFetchMethod) {
				mParameters = (BulletFetchMethod) methodEntity;

				if (hasSearchOptions()) {
					mResponse.status = searchAndSetFoundDefs(SearchTables.BULLET, mParameters.nextCursor, mResponse.bullets);
				} else {
					getAndSetNewestBullets();
				}
			}
		} else {
			mResponse.status = FetchStatuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	@Override
	protected String buildSearchString() {
		SearchUtils.Builder builder = new SearchUtils.Builder();

		// Free text search
		if (mParameters.searchString != null && mParameters.searchString.length() >= ServerConfig.SEARCH_TEXT_LENGTH_MIN) {
			builder.text(mParameters.searchString);
		}

		return builder.build();
	}

	/**
	 * Get and set the newest actors
	 */
	private void getAndSetNewestBullets() {
		getNewestActors(UploadTypes.BULLET_DEF, mParameters.nextCursor, mResponse.bullets);
		mResponse.cursor = getNextCursor();
		mResponse.status = getFetchStatus();
	}

	/**
	 * Checks if we should search for bullets or just get them
	 * @return true if we should perform a custom search
	 */
	private boolean hasSearchOptions() {
		if (mParameters.searchString != null && mParameters.searchString.length() >= ServerConfig.SEARCH_TEXT_LENGTH_MIN) {
			return true;
		}

		return false;
	}

	@Override
	protected void onInit() {
		super.onInit();

		mResponse = new BulletFetchResponse();
	}

	@Override
	protected void setAdditionalDefInformation(Entity publishedEntity, BulletDefEntity actorDef) {
		// Does nothing
	}

	@Override
	protected BulletDefEntity newNetworkDef() {
		return new BulletDefEntity();
	}

	private BulletFetchMethod mParameters = null;
	private BulletFetchResponse mResponse = null;
}
