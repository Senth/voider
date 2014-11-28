package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Can be used separately or be derived and used as a base class for tailoring this scene.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreActorScene extends ExploreScene {

	/**
	 * Hidden constructor. Create from ExploreFactory
	 */
	ExploreActorScene() {
		this(new ExploreActorGui());
	}

	/**
	 * This constructor should be called if it's a super class
	 * @param gui
	 */
	protected ExploreActorScene(ExploreActorGui gui) {
		super(gui);

		((ExploreActorGui) mGui).setActorScene(this);
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
