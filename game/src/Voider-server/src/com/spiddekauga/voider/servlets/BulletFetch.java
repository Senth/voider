package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.BulletDefEntity;
import com.spiddekauga.voider.network.entities.resource.BulletFetchMethod;
import com.spiddekauga.voider.network.entities.resource.BulletFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.FetchStatuses;
import com.spiddekauga.voider.network.entities.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ActorFetch;

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

				getNewestActors(UploadTypes.BULLET_DEF, mParameters.nextCursor, mResponse.bullets);
				mResponse.cursor = getNextCursor();
				mResponse.status = getFetchStatus();
			}
		} else {
			mResponse.status = FetchStatuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	@Override
	protected void onInit() {
		super.onInit();

		mResponse = new BulletFetchMethodResponse();
	}

	@Override
	protected void setAdditionalActorInformation(Entity publishedEntity, BulletDefEntity actorDef) {
		// Does nothing
	}

	@Override
	protected BulletDefEntity newActorDef() {
		return new BulletDefEntity();
	}

	private BulletFetchMethod mParameters = null;
	private BulletFetchMethodResponse mResponse = null;
}
