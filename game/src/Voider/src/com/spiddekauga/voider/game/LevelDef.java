package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.ResourceNames;

/**
 * Level definition of a level. I.e. this is the level's header information
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelDef extends Def {
	/**
	 * @return the music of the level
	 */
	public ResourceNames getMusic() {
		return mMusic;
	}

	/**
	 * Sets the music of the level
	 * @param music the music to set
	 */
	public void setMusic(ResourceNames music) {
		mMusic = music;
	}

	/**
	 * Returns the campaign id if the level belongs to a certain campaign
	 * @return campaign id if level belongs to a campaign, else null
	 */
	public UUID getCampaignId() {
		return mCampaignId;
	}

	/**
	 * Sets the campaign id the level belongs to
	 * @param campaignId the campaign id the level belongs to, set to null
	 * if the level don't belong to any campaign
	 */
	public void setCampaignId(UUID campaignId) {
		mCampaignId = campaignId;
	}

	/**
	 * @return story displayed before the level starts
	 */
	public String getStoryBefore() {
		return mStoryBefore;
	}

	/**
	 * Sets a new story that is displayed before the level starts
	 * @param storyBefore the new story to be displayed before the level starts
	 */
	public void setStoryBefore(String storyBefore) {
		mStoryBefore = storyBefore;
	}

	/**
	 * @return story displayed after the level starts
	 */
	public String getStoryAfter() {
		return mStoryAfter;
	}

	/**
	 * Sets a new story that is displayed after the level ends, i.e. the player
	 * clears the map.
	 * @param storyAfter the new story to be displayeb after the level ends.
	 */
	public void setStoryAfter(String storyAfter) {
		mStoryAfter = storyAfter;
	}

	/**
	 * Sets the version of the level
	 * @param first the first number (i.e. 1 in 1.0.13)
	 * @param second the second number (i.e. 0 in 1.0.13)
	 * @param third the third number (i.e. 13 in 1.0.13)
	 */
	public void setVersion(int first, int second, int third) {
		mVersionFirst = first;
		mVersionSecond = second;
		mVersionThird = third;
	}

	/**
	 * @return the first number in the version (i.e. 1 in 1.0.13)
	 */
	public int getVersionFirst() {
		return mVersionFirst;
	}

	/**
	 * @return the second number in the version (i.e. 0 in 1.0.13)
	 */
	public int getVersionSecond() {
		return mVersionSecond;
	}

	/**
	 * @return the third number in the version (i.e. 0 in 1.0.13)
	 */
	public int getVersionThird() {
		return mVersionThird;
	}

	/**
	 * Updates the first number in the version and resets the other counters
	 */
	public void increaseVersionFirst() {
		mVersionFirst++;
		mVersionSecond = 0;
		mVersionThird = 0;
	}

	/**
	 * Updates the second number in the version and resets the third counter.
	 * The first number is unchanged
	 */
	public void increaseVersionSecond() {
		mVersionSecond++;
		mVersionThird = 0;
	}

	/**
	 * Updates the third number in the version. The first and second number
	 * is unchanged
	 */
	public void increaseVersionThird() {
		mVersionThird++;
	}

	/**
	 * @return the version number as a string
	 */
	public String getVersionString() {
		return Integer.toString(mVersionFirst) + "." +
				Integer.toString(mVersionSecond) + "." +
				Integer.toString(mVersionThird);
	}

	/**
	 * @return the theme id of the level
	 */
	public UUID getThemeId() {
		return mThemeId;
	}

	/**
	 * @return the revision of the level
	 */
	public long getRevision() {
		return mRevision;
	}

	/**
	 * Increases the revision count by one
	 */
	public void increaseRevision() {
		++mRevision;
	}

	/**
	 * Sets the theme id, also adds it as a dependency
	 * @param themeId the theme of the level
	 */
	public void setThemeId(UUID themeId) {
		// Remove old theme from dependencies
		if (mThemeId != null) {
			removeDependency(mThemeId);
		}

		mThemeId = themeId;

		// Add new dependency
		if (mThemeId != null) {
			addDependency(mThemeId, ThemeDef.class);
		}
	}

	@Override
	public void write(Json json) {
		json.writeValue("VERSION", VERSION);

		json.writeObjectStart("Def");
		super.write(json);
		json.writeObjectEnd();

		json.writeValue("mMusic", mMusic);
		json.writeValue("mCampaignId", mCampaignId.toString());
		json.writeValue("mStoryBefore", mStoryBefore);
		json.writeValue("mStoryAfter", mStoryAfter);
		json.writeValue("mRevision", mRevision);
		json.writeValue("mVersion", getVersionString());
		json.writeValue("mThemeId", mThemeId.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		long version = json.readValue("VERSION", long.class, jsonData);

		/** @TODO do something when another version... */
		if (version != VERSION) {
			//...
		}


		// Superclass
		OrderedMap<String, Object> defMap = json.readValue("Def", OrderedMap.class, jsonData);
		if (defMap != null) {
			super.read(json, defMap);
		}


		// Variables
		mMusic = json.readValue("mMusic", ResourceNames.class, jsonData);
		mCampaignId = UUID.fromString(json.readValue("mCampaignId", String.class, jsonData));
		mStoryBefore = json.readValue("mStoryBefore", String.class, jsonData);
		mStoryAfter = json.readValue("mStoryAfter", String.class, jsonData);
		mRevision = json.readValue("mRevision", long.class, jsonData);
		mThemeId = UUID.fromString(json.readValue("mThemeId", String.class, jsonData));

		// Version
		String stringVersion = json.readValue("mVersion", String.class, jsonData);
		String[] stringVersions = stringVersion.split("\\.");
		mVersionFirst = Integer.parseInt(stringVersions[0]);
		mVersionSecond = Integer.parseInt(stringVersions[1]);
		mVersionThird = Integer.parseInt(stringVersions[2]);
	}

	/** Theme of the level */
	private UUID mThemeId = null;
	/** The level's music */
	private ResourceNames mMusic = null;
	/** Campaign id the level belongs to, null if it doesn't belong to any */
	private UUID mCampaignId = null;
	/** Story before the level, set to null to not show */
	private String mStoryBefore = null;
	/** Story after the level, set to null to not show */
	private String mStoryAfter = null;
	/** The revision of the map, this increases after each save */
	private long mRevision = 1;
	/** Main version (1 in 1.0.13) */
	private int mVersionFirst = 0;
	/** Minor version (0 in 1.0.13) */
	private int mVersionSecond = 0;
	/** Third small version (13 in 1.0.13) */
	private int mVersionThird = 0;

	/** The version of this level definition structure */
	private static final long VERSION = 100;

}
