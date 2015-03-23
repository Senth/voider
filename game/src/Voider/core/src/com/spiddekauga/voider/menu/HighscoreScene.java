package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Iterator;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
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
	 */
	public HighscoreScene() {
		super(new HighscoreSceneGui());
		getGui().setHighscoreScene(this);
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

			if (response instanceof HighscoreGetResponse) {
				handleGetUserScores((HighscoreGetResponse) response);
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
	private void handleGetUserScores(HighscoreGetResponse response) {
		if (response.isSuccessful()) {
			getGui().setFirstPlace(response.firstPlace);
			getGui().populateUserScores(response.userScore, response.userPlace, response.beforeUser, response.afterUser);
		} else {
			continueToNextScene();
		}
	}

	@Override
	protected HighscoreSceneGui getGui() {
		return (HighscoreSceneGui) super.getGui();
	}

	/** Web responses to be processed */
	private ArrayList<WebWrapper> mWebResponses = new ArrayList<>();
}
