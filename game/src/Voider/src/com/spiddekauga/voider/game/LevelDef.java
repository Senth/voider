package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.voider.Config;
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

	@SuppressWarnings("unchecked")
	@Override
	public <ResourceType> ResourceType copy() {
		LevelDef def = (LevelDef) super.copy();
		def.mLevelId = UUID.randomUUID();
		def.mCampaignId = null;
		return (ResourceType) def;
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
	public String getPrologue() {
		return mPrologue;
	}

	/**
	 * Sets a new story that is displayed before the level starts
	 * @param prologue the new story to be displayed before the level starts
	 */
	public void setPrologue(String prologue) {
		mPrologue = prologue;
	}

	/**
	 * @return story displayed after the level starts
	 */
	public String getEpilogue() {
		return mEpilogue;
	}

	/**
	 * Sets a new story that is displayed after the level ends, i.e. the player
	 * clears the map.
	 * @param epilogue the new story to be displayed after the level ends.
	 */
	public void setStoryAfter(String epilogue) {
		mEpilogue = epilogue;
	}

	/**
	 * Sets the end x coordinate of the level
	 * @param endXCoord end x coordinate of the level
	 */
	public void setEndXCoord(float endXCoord) {
		mEndXCoord = endXCoord;
	}

	/**
	 * @return the end x coordinate of the level
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
	 * Sets the starting position of the level
	 * @param startXCoord starting x coordinate of the level
	 */
	void setStartXCoord(float startXCoord) {
		mStartXCoord = startXCoord;
	}

	/**
	 * @return starting position of the level
	 */
	public float getStartXCoord() {
		return mStartXCoord;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mMusic", mMusic);
		json.writeValue("mPrologue", mPrologue);
		json.writeValue("mEpilogue", mEpilogue);
		json.writeValue("mStartXCoord", mStartXCoord);
		json.writeValue("mEndXCoord", mEndXCoord);
		json.writeValue("mSpeed", mSpeed);
		json.writeValue("mLevelId", mLevelId);
		json.writeValue("mCampaignId", mCampaignId);
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		super.read(json, jsonValue);


		// Variables
		mMusic = json.readValue("mMusic", ResourceNames.class, jsonValue);
		mPrologue = json.readValue("mPrologue", String.class, jsonValue);
		mEpilogue = json.readValue("mEpilogue", String.class, jsonValue);
		mStartXCoord = json.readValue("mStartXCoord", float.class, jsonValue);
		mEndXCoord = json.readValue("mEndXCoord", float.class, jsonValue);
		mSpeed = json.readValue("mSpeed", float.class, jsonValue);


		// UUIDs
		mCampaignId = json.readValue("mCampaignId", UUID.class, jsonValue);
		mLevelId = json.readValue("mLevelId", UUID.class, jsonValue);
	}


	/** Starting coordinate of the level (right screen edge) */
	private float mStartXCoord = 0;
	/** The actual level id, i.e. not this definition's id */
	private UUID mLevelId = null;
	/** The level's music */
	private ResourceNames mMusic = null;
	/** Campaign id the level belongs to, null if it doesn't belong to any */
	private UUID mCampaignId = null;
	/** Story before the level, set to null to not show */
	private String mPrologue = "";
	/** Story after the level, set to null to not show */
	private String mEpilogue = "";
	/** Base speed of the level, the actual level speed may vary as it can
	 * be changed by triggers */
	private float mSpeed = Config.Editor.Level.LEVEL_SPEED_DEFAULT;
	/** End of the map (right screen edge) */
	private float mEndXCoord = 100f;

}
