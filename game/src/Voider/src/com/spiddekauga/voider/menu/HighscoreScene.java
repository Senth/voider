package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Iterator;

import com.spiddekauga.utils.scene.ui.UiFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.stat.HighscoreGetMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.scene.Scene;

/**
 * Scene for highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HighscoreScene extends Scene implements IResponseListener {
	/**
	 * Creates highscore scene
	 */
	public HighscoreScene() {
		super(new HighscoreSceneGui());
		((HighscoreSceneGui) mGui).setHighscoreScene(this);
		setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);
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

			if (response instanceof HighscoreGetMethodResponse) {
				handleGetUserScores((HighscoreGetMethodResponse) response);
			}

			webIt.remove();
		}
	}

	/**
	 * Continue to the next scene
	 */
	void continueToNextScene() {
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/**
	 * Handle get user scores
	 * @param response server response
	 */
	private void handleGetUserScores(HighscoreGetMethodResponse response) {
		if (response.isSuccessful()) {
			((HighscoreSceneGui) mGui).setFirstPlace(response.firstPlace);
			((HighscoreSceneGui) mGui).populateUserScores(response.userScore, response.userPlace, response.beforeUser, response.afterUser);
		} else {
			continueToNextScene();
		}

		// switch (response.status) {
		// case FAILED_CONNECTION:
		// mGui.showErrorMessage("Failed to connect to the server");
		// break;
		//
		// case FAILED_INTERNAL:
		// mGui.showErrorMessage("Internal server error");
		// break;
		//
		// case FAILED_USER_NOT_LOGGED_IN:
		// mGui.showErrorMessage("You are not logged in to the server");
		// break;
		//
		// case FAILED_LEVEL_NOT_FOUND:
		// mGui.showErrorMessage("Could not find any highscores for this level");
		// break;
		//
		// case FAILED_HIGHSCORES_NOT_FOUND:
		// mGui.showErrorMessage("Didn't find any highscores for this level");
		// break;
		//
		// case SUCCESS:
		//
		// break;
		// }
	}

	/** Web responses to be processed */
	private ArrayList<WebWrapper> mWebResponses = new ArrayList<>();
}
