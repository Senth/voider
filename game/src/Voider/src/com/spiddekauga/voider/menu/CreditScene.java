package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;

/**
 * Scene for credits
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class CreditScene extends Scene {
	/**
	 * Default constructor
	 */
	public CreditScene() {
		super(new CreditGui());

		CreditGui gui = getGui();
		gui.setScene(this);
	}

	/**
	 * @return all credits
	 */
	ArrayList<CreditSection> getCredits() {
		ArrayList<CreditSection> creditSections = new ArrayList<>();

		// TODO

		return creditSections;
	}

	@Override
	protected void loadResources() {
		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.load(InternalNames.CREDITS);

		super.loadResources();
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
		ResourceCacheFacade.unload(InternalNames.CREDITS);

		super.unloadResources();
	}


	/**
	 * Wrapper for credits sections
	 */
	class CreditSection {
		/** Name of the section */
		String sectionName = null;
		/** All names in the section */
		ArrayList<CreditName> names = new ArrayList<>();
	}

	/** Wrapper for a credit name */
	class CreditName {
		/** Name to display */
		String name = null;
		/** Optional link text */
		String link = null;
		/** Optional link URL */
		String url = null;
	}

}
