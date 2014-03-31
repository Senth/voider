package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Iterator;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethodResponse;
import com.spiddekauga.voider.repo.ICallerResponseListener;
import com.spiddekauga.voider.repo.ResourceWebRepo;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Graphics;

/**
 * Scene for exploring new content
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreScene extends Scene implements ICallerResponseListener {
	/**
	 * Default constructor
	 */
	public ExploreScene() {
		super(new ExploreGui());

		((ExploreGui)mGui).setExploreScene(this);
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();

		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);

		handleWepResponses();
	}

	@Override
	public boolean keyDown(int keycode) {
		super.keyDown(keycode);

		if (KeyHelper.isBackPressed(keycode)) {
			setOutcome(Outcomes.NOT_APPLICAPLE);
		}

		return false;
	}

	@Override
	public synchronized void handleWebResponse(IMethodEntity method, IEntity response) {
		mWebResponses.add(new WebWrapper(method, response));
	}

	/**
	 * Handles existing web responses
	 */
	private synchronized void handleWepResponses() {
		Iterator<WebWrapper> webIt = mWebResponses.iterator();

		while (webIt.hasNext()) {
			WebWrapper webWrapper = webIt.next();
			IEntity response = webWrapper.response;

			if (response instanceof LevelGetAllMethodResponse) {
				switch (((LevelGetAllMethodResponse) response).status) {
				case FAILED_SERVER_CONNECTION:
					mGui.showErrorMessage("Failed to connect to the server");
					break;

				case FAILED_SERVER_ERROR:
					mGui.showErrorMessage("Internal server error");
					break;

				case SUCCESS_FETCHED_ALL:
				case SUCCESS_MORE_EXISTS:
					createDrawables(((LevelGetAllMethodResponse) response).levels);
					((ExploreGui)mGui).resetContent(((LevelGetAllMethodResponse) response).levels);
					break;
				}
			}

			webIt.remove();
		}
	}

	/**
	 * Creates drawables for all levels that are missing the drawables
	 * @param levels all levels to create a drawable for
	 */
	private void createDrawables(ArrayList<LevelInfoEntity> levels) {
		for (LevelInfoEntity level : levels) {
			if (level.defEntity.drawable == null && level.defEntity.png != null) {
				level.defEntity.drawable = Graphics.pngToDrawable(level.defEntity.png);
			}
		}
	}

	/**
	 * Fetch levels from the server by the specified sort
	 * @param sort sorting order to get levels by
	 * @param tags selected tags
	 */
	void fetchLevels(SortOrders sort, ArrayList<Tags> tags) {
		mLastMethod = mResourceWebRepo.getLevels(this, sort, tags);

		((ExploreGui)mGui).resetContent();
	}

	/**
	 * Fetch levels from the server by the specified search string
	 * @param searchString the text to search for
	 */
	void fetchLevels(String searchString) {
		if (searchString.length() >= Config.Actor.NAME_LENGTH_MIN) {
			mLastMethod = mResourceWebRepo.getLevels(this, searchString);

			((ExploreGui)mGui).resetContent();
		}
	}

	/**
	 * Go back to main menu
	 */
	void gotoMainMenu() {
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/**
	 * Play the selected level
	 */
	void play() {
		// TODO
	}

	/**
	 * @return all fetched levels
	 */
	ArrayList<LevelInfoEntity> getLevels() {
		return mResourceWebRepo.getCachedLevels(mLastMethod);
	}

	/**
	 * @return the selected level, null if none are selected
	 */
	LevelInfoEntity getSelectedLevel() {
		return mSelectedLevel;
	}

	/**
	 * Sets the selected level
	 * @param level new selected level
	 */
	void setSelectedLevel(LevelInfoEntity level) {
		mSelectedLevel = level;
	}

	/** Selected level */
	LevelInfoEntity mSelectedLevel = null;
	/** Resource web repository */
	private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
	/** Synchronized web responses */
	private ArrayList<WebWrapper> mWebResponses = new ArrayList<>();
	/** Last method that was called */
	private LevelGetAllMethod mLastMethod = null;
}