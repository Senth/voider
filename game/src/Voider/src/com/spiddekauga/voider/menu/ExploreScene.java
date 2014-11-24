package com.spiddekauga.voider.menu;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Graphics;

/**
 * Common class for all explore scenes
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class ExploreScene extends Scene implements IResponseListener {
	/**
	 * @param gui explore GUI
	 */
	protected ExploreScene(ExploreGui gui) {
		super(gui);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		handleWepResponses();
	}

	@Override
	public final void handleWebResponse(IMethodEntity method, IEntity response) {
		try {
			mWebResponses.put(new WebWrapper(method, response));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles existing web responses
	 */
	private void handleWepResponses() {
		while (!mWebResponses.isEmpty()) {
			try {
				WebWrapper webWrapper = mWebResponses.take();
				onWebResponse(webWrapper.method, webWrapper.response);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create drawable for the def entity if it doesn't exist
	 * @param defEntity the def to create a drawable for if it doesn't exist
	 */
	protected void createDrawable(DefEntity defEntity) {
		if (defEntity.drawable == null && defEntity.png != null) {
			defEntity.drawable = Graphics.pngToDrawable(defEntity.png);
		}
	}

	/**
	 * @return true if we're currently fetching content
	 */
	abstract boolean isFetchingContent();

	/**
	 * @return true if we have more content to fetch
	 */
	abstract boolean hasMoreContent();

	/**
	 * Fetch more content
	 */
	abstract void fetchMoreContent();

	/**
	 * Handle synchronized web response. This method should be used instead of in
	 * sub-classes.
	 * @param method parameters to the server
	 * @param response response from the server
	 */
	protected abstract void onWebResponse(IMethodEntity method, IEntity response);

	/** Synchronized web responses */
	private BlockingQueue<WebWrapper> mWebResponses = new LinkedBlockingQueue<WebWrapper>();
}
