package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.ResourceNames;

/**
 * Level definition of a level. I.e. this is the level's header information
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelDef extends Def {
	/**
	 * Constructor that create the level id for this definition
	 */
	public LevelDef() {
		mLevelId = UUID.randomUUID();
	}

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
	 * Sets the end x coordinate of the level
	 * @param endXCoord end x coordinate of the level (left screen edge)
	 */
	public void setEndXCoord(float endXCoord) {
		mEndXCoord = endXCoord;
	}

	/**
	 * @return the end x coordinate of the level (left screen edge)
	 */
	public float getEndXCoord() {
		return mEndXCoord;
	}

	/**
	 * Sets the base speed of the level. This should only be changed when
	 * editing the map, and for the whole level. To change the level's current
	 * speed see #Level.setSpeed(float)
	 * @param speed the new base speed
	 */
	public void setBaseSpeed(float speed) {
		mSpeed = speed;
	}

	/**
	 * @return base speed of the level
	 */
	public float getBaseSpeed() {
		return mSpeed;
	}

	/**
	 * @return the level's id, i.e. not this definition's id
	 */
	public UUID getLevelId() {
		return mLevelId;
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
		json.writeValue("mStoryBefore", mStoryBefore);
		json.writeValue("mStoryAfter", mStoryAfter);
		json.writeValue("mRevision", mRevision);
		json.writeValue("mVersion", getVersionString());
		json.writeValue("mEndXCoord", mEndXCoord);
		json.writeValue("mSpeed", mSpeed);
		json.writeValue("mLevelId", mLevelId.toString());

		if (mCampaignId != null) {
			json.writeValue("mCampaignId", mCampaignId.toString());
		} else {
			json.writeValue("mCampaignId", (String)null);
		}

		if (mThemeId != null) {
			json.writeValue("mThemeId", mThemeId.toString());
		} else {
			json.writeValue("mThemeId", (String)null);
		}
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
		mStoryBefore = json.readValue("mStoryBefore", String.class, jsonData);
		mStoryAfter = json.readValue("mStoryAfter", String.class, jsonData);
		mRevision = json.readValue("mRevision", long.class, jsonData);
		mEndXCoord = json.readValue("mEndXCoord", float.class, jsonData);
		mSpeed = json.readValue("mSpeed", float.class, jsonData);


		// UUIDs
		String stringUuid = json.readValue("mCampaignId", String.class, jsonData);
		if (stringUuid != null) {
			mCampaignId = UUID.fromString(stringUuid);
		}
		stringUuid = json.readValue("mThemeId", String.class, jsonData);
		if (stringUuid != null) {
			mThemeId = UUID.fromString(stringUuid);
		}
		stringUuid = json.readValue("mLevelId", String.class, jsonData);
		if (stringUuid != null) {
			mLevelId = UUID.fromString(stringUuid);
		}

		// Version
		String stringVersion = json.readValue("mVersion", String.class, jsonData);
		String[] stringVersions = stringVersion.split("\\.");
		mVersionFirst = Integer.parseInt(stringVersions[0]);
		mVersionSecond = Integer.parseInt(stringVersions[1]);
		mVersionThird = Integer.parseInt(stringVersions[2]);
	}

	/** Theme of the level */
	private UUID mThemeId = null;
	/** The actual level id, i.e. not this definition's id */
	private UUID mLevelId = null;
	/** The level's music */
	private ResourceNames mMusic = null;
	/** Campaign id the level belongs to, null if it doesn't belong to any */
	private UUID mCampaignId = null;
	/** Story before the level, set to null to not show */
	private String mStoryBefore = null;
	/** Story after the level, set to null to not show */
	private String mStoryAfter = null;
	/** Base speed of the level, the actual level speed may vary as it can
	 * be changed by triggers */
	private float mSpeed = 1.0f;
	/** End of the map (left screen edge) */
	private float mEndXCoord = 100.0f;
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
