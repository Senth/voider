package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.repo.ICallerResponseListener;
import com.spiddekauga.voider.repo.ResourceWebRepo;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;

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
	public boolean keyDown(int keycode) {
		super.keyDown(keycode);

		if (KeyHelper.isBackPressed(keycode)) {
			setOutcome(Outcomes.NOT_APPLICAPLE);
		}

		return false;
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// TODO Auto-generated method stub
	}

	/**
	 * Fetch levels from the server by the specified sort
	 * @param sort sorting order to get levels by
	 * @param tags selected tags
	 */
	void fetchLevels(SortOrders sort, ArrayList<Tags> tags) {
		//		mResourceWebRepo.getLevels(this, sort, tags);
	}

	/**
	 * Go back to main menu
	 */
	void gotoMainMenu() {
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/** Resource web repository */
	private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
}