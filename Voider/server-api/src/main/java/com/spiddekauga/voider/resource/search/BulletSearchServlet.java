package com.spiddekauga.voider.resource.search;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.resource.BulletDefEntity;
import com.spiddekauga.voider.network.resource.BulletFetchMethod;
import com.spiddekauga.voider.network.resource.BulletFetchResponse;
import com.spiddekauga.voider.network.resource.FetchStatuses;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.server.util.SearchTables;
import com.spiddekauga.voider.server.util.ServerConfig;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Servlet for fetching bullet information
 */
@SuppressWarnings("serial")
public class BulletSearchServlet extends ActorSearchServlet<BulletFetchMethod, BulletDefEntity> {

private BulletFetchMethod mParameters = null;
private BulletFetchResponse mResponse = null;

@Override
protected IEntity onRequest(BulletFetchMethod method) throws ServletException, IOException {
	if (mUser.isLoggedIn()) {
		mParameters = method;

		if (hasSearchOptions()) {
			mResponse.status = searchAndSetFoundDefs(SearchTables.BULLET, mParameters.nextCursor, mResponse.bullets);
		} else {
			getAndSetNewestBullets();
		}
	} else {
		mResponse.status = FetchStatuses.FAILED_USER_NOT_LOGGED_IN;
	}

	return mResponse;
}

/**
 * Checks if we should search for bullets or just get them
 * @return true if we should perform a custom search
 */
private boolean hasSearchOptions() {
	return mParameters.searchString != null && mParameters.searchString.length() >= ServerConfig.SEARCH_TEXT_LENGTH_MIN;

}

/**
 * Get and set the newest actors
 */
private void getAndSetNewestBullets() {
	getNewestActors(UploadTypes.BULLET_DEF, mParameters.nextCursor, mResponse.bullets);
	mResponse.cursor = getNextCursor();
	mResponse.status = getFetchStatus();
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

@Override
protected BulletDefEntity newNetworkDef() {
	return new BulletDefEntity();
}

@Override
protected void setAdditionalDefInformation(Entity publishedEntity, BulletDefEntity actorDef) {
	// Does nothing
}

@Override
protected void onInit() throws ServletException, IOException {
	super.onInit();

	mResponse = new BulletFetchResponse();
}
}
