package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

/**
 * Scene for credits
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class CreditScene extends MenuScene {
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

		Ini ini = ResourceCacheFacade.get(InternalNames.INI_CREDITS);

		if (ini != null) {
			Section section = ini.get("credits");

			// Add all sections
			for (String childName : section.childrenNames()) {
				Section child = section.getChild(childName);

				CreditSection creditSection = new CreditSection();
				creditSection.sectionName = childName;
				creditSections.add(creditSection);

				parseTextLines(creditSection, child);
				parseImageLine(creditSection, child);
				parseUrlLine(creditSection, child);
				parseTwitterLine(creditSection, child);
			}
		}

		return creditSections;
	}

	/**
	 * Add all text lines to the section
	 * @param creditSection current credit section
	 * @param iniSection
	 */
	private void parseTextLines(CreditSection creditSection, Section iniSection) {
		int cNames = iniSection.getAll(TEXT_LINE).size();
		for (int i = 0; i < cNames; ++i) {
			String creditString = iniSection.fetch(TEXT_LINE, i);
			CreditLine creditName = convertToCreditLine(creditString);
			creditSection.texts.add(creditName);
		}
	}

	/**
	 * Parse twitter line
	 * @param creditSection current credit section
	 * @param iniSection
	 */
	private void parseTwitterLine(CreditSection creditSection, Section iniSection) {
		String twitterString = iniSection.fetch(TWITTER_LINE);
		if (twitterString != null) {
			creditSection.twitter = new CreditLine();
			creditSection.twitter.text = twitterString;
			creditSection.twitter.url = TWITTER_URL_PREFIX + twitterString;
		}
	}

	/**
	 * Parse the URL line
	 * @param creditSection current credit section
	 * @param iniSection
	 */
	private void parseUrlLine(CreditSection creditSection, Section iniSection) {
		String urlLine = iniSection.fetch(URL_LINE);
		if (urlLine != null) {
			creditSection.url = convertToCreditLine(urlLine);
		}
	}

	/**
	 * Parse the image line
	 * @param creditSection current credit section
	 * @param iniSection
	 */
	private void parseImageLine(CreditSection creditSection, Section iniSection) {
		creditSection.imageName = iniSection.fetch(IMAGE_LINE);
	}

	/**
	 * Converts a credit string into a CreditName class
	 * @param creditString string to convert
	 * @return valid credit name with possible link information
	 */
	private CreditLine convertToCreditLine(String creditString) {
		CreditLine creditName = new CreditLine();

		// Does it contain an URL
		int httpIndex = creditString.indexOf("http");
		if (httpIndex != -1) {
			// If we only have an URL set is as text too
			if (httpIndex == 0) {
				creditName.text = creditString.trim();
				creditName.url = creditString.trim();
			} else {
				creditName.text = creditString.substring(0, httpIndex).trim();
				creditName.url = creditString.substring(httpIndex).trim();
			}
		} else {
			creditName.text = creditString;
		}

		return creditName;
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(this, InternalNames.UI_CREDITS);
		ResourceCacheFacade.load(this, InternalNames.INI_CREDITS);
	}

	@Override
	protected CreditGui getGui() {
		return (CreditGui) super.getGui();
	}

	/**
	 * Wrapper for credits sections
	 */
	class CreditSection {
		/** Name of the section */
		String sectionName = null;
		/** Optional twitter account */
		CreditLine twitter = null;
		/** Optional URL */
		CreditLine url = null;
		/** Optional image name to be displayed above section name */
		String imageName = null;
		/** All text lines */
		ArrayList<CreditLine> texts = new ArrayList<>();

		/**
		 * @return true if the section has an image
		 */
		boolean hasImage() {
			return imageName != null && !imageName.isEmpty();
		}

		/**
		 * @return true if the section has an URL
		 */
		boolean hasUrl() {
			return url != null && url.hasLink();
		}

		/**
		 * @return true if the section has a twitter account
		 */
		boolean hasTwitter() {
			return twitter != null && twitter.hasLink();
		}
	}

	/** Wrapper for a line in each section */
	class CreditLine {
		private String text = null;
		/**
		 * URL is set for twitter and url keywords. Independent lines can also have an
		 * URL. See credits.ini.
		 */
		private String url = null;

		/**
		 * @return text of the credit line
		 */
		String getText() {
			if (text != null) {
				return text;
			} else {
				return url;
			}
		}

		/**
		 * @return URL of the credit line
		 */
		String getUrl() {
			return url;
		}

		/**
		 * @return true if a link exists.
		 */
		boolean hasLink() {
			return url != null && !url.isEmpty();
		}
	}

	private static final String TEXT_LINE = "text";
	private static final String TWITTER_LINE = "twitter";
	private static final String URL_LINE = "url";
	private static final String IMAGE_LINE = "image";
	private static final String TWITTER_URL_PREFIX = "http://twitter.com/";
}
