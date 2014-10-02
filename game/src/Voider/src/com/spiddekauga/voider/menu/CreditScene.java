package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.spiddekauga.utils.KeyHelper;
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

	@Override
	protected boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			setOutcome(Outcomes.NOT_APPLICAPLE);
			return true;
		}

		return super.onKeyDown(keycode);
	};

	/**
	 * @return all credits
	 */
	ArrayList<CreditSection> getCredits() {
		ArrayList<CreditSection> creditSections = new ArrayList<>();

		Ini ini = ResourceCacheFacade.get(InternalNames.CREDITS);

		if (ini != null) {
			Section section = ini.get("credits");

			// Add all sections
			for (String childName : section.childrenNames()) {
				Section child = section.getChild(childName);

				CreditSection creditSection = new CreditSection();
				creditSection.sectionName = childName;
				creditSections.add(creditSection);

				// Add all names
				int cNames = child.getAll("names").size();
				for (int i = 0; i < cNames; ++i) {
					String creditString = child.fetch("names", i);
					CreditName creditName = convertToCreditName(creditString);
					creditSection.names.add(creditName);
				}
			}
		}

		return creditSections;
	}

	/**
	 * Converts a credit string into a CreditName class
	 * @param creditString string to convert
	 * @return valid credit name with possible link information
	 */
	private CreditName convertToCreditName(String creditString) {
		CreditName creditName = new CreditName();

		String[] parts = creditString.split(", ");

		// Name
		if (parts.length >= 1) {
			creditName.name = parts[0].trim();
		}
		// LinkText
		if (parts.length >= 2) {
			creditName.linkText = parts[1].trim();
		}
		// URL
		if (parts.length >= 3) {
			creditName.url = parts[3].trim();
		}

		return creditName;
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
		/**
		 * Optional link text, if starts with @ and doesn't have any URL it is treated as
		 * a twitter link
		 */
		String linkText = null;
		/** Optional link URL */
		String url = null;

		/**
		 * @return true if it has a twitter link
		 */
		boolean hasTwitter() {
			return linkText != null && !linkText.isEmpty() && linkText.charAt(0) == '@' && url == null;
		}

		/**
		 * @return true if a link exists. This method can return false where
		 *         {@link #hasTwitter()} returns true
		 */
		boolean hasLink() {
			return linkText != null && !linkText.isEmpty() && url != null && !url.isEmpty();
		}

		/**
		 * @return correct twitter link
		 */
		String getTwitterLink() {
			return "http://twitter.com/" + linkText;
		}
	}

}
