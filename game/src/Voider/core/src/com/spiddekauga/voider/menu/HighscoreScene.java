package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Iterator;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.stat.HighscoreEntity;
import com.spiddekauga.voider.network.stat.HighscoreGetResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.ui.UiFactory;

/**
 * Scene for highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HighscoreScene extends Scene implements IResponseListener {
	/**
	 * Creates highscore scene
	 * @param score how much the player scored this time
	 */
	public HighscoreScene(int score) {
		super(new HighscoreSceneGui());
		getGui().setHighscoreScene(this);
		setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);
		mScore = score;
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		handleWebResponses();
	}

	@Override
	public synchronized void handleWebResponse(IMethodEntity method, IEntity response) {
		mWebResponses.add(new WebWrapper(method, response));
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
			setIsNewHighscore(response.userScore);
			getGui().setFirstPlace(response.firstPlace);
			getGui().populateUserScores(mScore, response.userScore, response.userPlace, response.beforeUser, response.afterUser);
		} else {
			endScene();
		}
	}

	/**
	 * Check if it's a new highscore and sets this to true or false
	 * @param userHighscore current player highscore
	 */
	private void setIsNewHighscore(HighscoreEntity userHighscore) {
		mIsHighscoreThisTime = userHighscore.score == mScore;
	}

	/**
	 * @return true if it's a highscore this time
	 */
	boolean isHighScoreThisTime() {
		return mIsHighscoreThisTime;
	}

	@Override
	protected HighscoreSceneGui getGui() {
		return (HighscoreSceneGui) super.getGui();
	}

	/** Score this time */
	private int mScore;
	private boolean mIsHighscoreThisTime = false;
	private ArrayList<WebWrapper> mWebResponses = new ArrayList<>();
}
