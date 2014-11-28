package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Scene for finding or loading enemies
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreEnemyScene extends ExploreActorScene {
	/**
	 * Hidden constructor. Create from ExploreFactory
	 */
	ExploreEnemyScene() {
		super(new ExploreEnemyGui());

		((ExploreEnemyGui) mGui).setExploreEnemyScene(this);
	}


	@Override
	boolean isFetchingContent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	boolean hasMoreContent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void fetchMoreContent() {
		// TODO Auto-generated method stub

	}

	@Override
	void repopulateContent() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onWebResponse(IMethodEntity method, IEntity response) {
		// TODO Auto-generated method stub

	}

}
