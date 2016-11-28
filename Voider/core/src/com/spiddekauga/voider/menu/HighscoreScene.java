package com.spiddekauga.voider.menu;

import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.stat.HighscoreGetResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.stat.HighscoreRepo;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/**
 * Scene for highscores
 */
public class HighscoreScene extends Scene implements IResponseListener {
/** Score this time */
private int mScore;
private UUID mLevelId;
private IEventListener mHighscoreSyncListener = new IEventListener() {
	@Override
	public void handleEvent(GameEvent event) {
		if (event.type == EventTypes.SYNC_HIGHSCORE_SUCCESS) {
			fetchHighscoreFromServer();
		} else {
			endScene();
		}
	}
};
private boolean mIsHighscoreThisTime = false;
private ArrayList<WebWrapper> mWebResponses = new ArrayList<>();

/**
 * Creates highscore scene
 * @param levelId id of the level
 * @param score how much the player scored this time
 * @param newHighscore true if the player score a new highscore. This causes the highscore scene to
 * wait for the score to be published before getting new score from the server
 */
public HighscoreScene(UUID levelId, int score, boolean newHighscore) {
	super(new HighscoreGui());
	mIsHighscoreThisTime = newHighscore;
	mLevelId = levelId;

	getGui().setHighscoreScene(this);
	setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);

	if (mIsHighscoreThisTime) {
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.connect(EventTypes.SYNC_HIGHSCORE_SUCCESS, mHighscoreSyncListener);
		eventDispatcher.connect(EventTypes.SYNC_HIGHSCORE_FAILED, mHighscoreSyncListener);
	} else {
		fetchHighscoreFromServer();
	}

	mScore = score;
}

/**
 * Fetch highscore from the server
 */
private void fetchHighscoreFromServer() {
	HighscoreRepo.getInstance().getPlayerServerScore(mLevelId, this);
}

@Override
protected void update(float deltaTime) {
	super.update(deltaTime);

	handleWebResponses();
}

@Override
protected void loadResources() {
	super.loadResources();
	ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
}

@Override
protected void onPause() {
	super.onPause();

	if (mIsHighscoreThisTime) {
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.disconnect(EventTypes.SYNC_HIGHSCORE_SUCCESS, mHighscoreSyncListener);
		eventDispatcher.disconnect(EventTypes.SYNC_HIGHSCORE_FAILED, mHighscoreSyncListener);
	}
}

@Override
protected HighscoreGui getGui() {
	return (HighscoreGui) super.getGui();
}

/**
 * Handle existing web responses
 */
private synchronized void handleWebResponses() {
	Iterator<WebWrapper> webIt = mWebResponses.iterator();

	while (webIt.hasNext()) {
		WebWrapper webWrapper = webIt.next();
		IEntity response = webWrapper.response;

		if (response instanceof HighscoreGetResponse) {
			handleGetUserScores((HighscoreGetResponse) response);
		}

		webIt.remove();
	}
}

/**
 * Handle get user scores
 * @param response server response
 */
private void handleGetUserScores(HighscoreGetResponse response) {
	if (response.isSuccessful()) {
		getGui().setFirstPlace(response.firstPlace);
		getGui().populateUserScores(mScore, response.userScore, response.userPlace, response.beforeUser, response.afterUser);
	} else {
		endScene();
	}
}

@Override
public synchronized void handleWebResponse(IMethodEntity method, IEntity response) {
	mWebResponses.add(new WebWrapper(method, response));
}

/**
 * @return true if it's a highscore this time
 */
boolean isHighScoreThisTime() {
	return mIsHighscoreThisTime;
}
}
