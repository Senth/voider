package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.LevelDefEntity;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.sound.Music;

/**
 * Level definition of a level. I.e. this is the level's header information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LevelDef extends Def {
	/**
	 * Constructor that create the level id for this definition
	 */
	public LevelDef() {
		mLevelId = UUID.randomUUID();

		setTheme(Themes.SPACE);
		setMusic(Music.SPACE);
	}

	@Override
	public void set(Resource resource) {
		super.set(resource);

		LevelDef def = (LevelDef) resource;
		mCampaignId = def.mCampaignId;
		mEndXCoord = def.mEndXCoord;
		mEpilogue = def.mEpilogue;
		mLevelId = def.mLevelId;
		mPrologue = def.mPrologue;
		mSpeed = def.mSpeed;
		mStartXCoord = def.mStartXCoord;
		mTheme = def.mTheme;
		mLengthInTime = def.mLengthInTime;
	}

	@Override
	protected void setNewDefEntity(DefEntity defEntity, boolean toOnline) {
		super.setNewDefEntity(defEntity, toOnline);

		LevelDefEntity levelDefEntity = (LevelDefEntity) defEntity;
		levelDefEntity.levelId = mLevelId;
		levelDefEntity.levelSpeed = mSpeed;

		if (mLengthInTime == 0) {
			calculateLength();
		}

		levelDefEntity.levelLength = mLengthInTime;
	}

	@Override
	protected DefEntity newDefEntity() {
		return new LevelDefEntity();
	}

	@Override
	public <ResourceType> ResourceType copyNewResource() {
		ResourceType copy = super.copyNewResource();

		LevelDef defCopy = (LevelDef) copy;
		defCopy.mLevelId = UUID.randomUUID();
		defCopy.mCampaignId = null;

		return copy;
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
	 * @param campaignId the campaign id the level belongs to, set to null if the level
	 *        don't belong to any campaign
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
	 * Sets a new story that is displayed after the level ends, i.e. the player clears the
	 * map.
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
		calculateLength();
	}

	/**
	 * @return the end x coordinate of the level
	 */
	public float getEndXCoord() {
		return mEndXCoord;
	}

	/**
	 * Sets the base speed of the level. This should only be changed when editing the map,
	 * and for the whole level. To change the level's current speed see
	 * #Level.setSpeed(float)
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
		calculateLength();
	}

	/**
	 * @return starting position of the level
	 */
	public float getStartXCoord() {
		return mStartXCoord;
	}

	/**
	 * Set the music for the level
	 * @param music the music the level should use
	 */
	public void setMusic(Music music) {
		if (mMusic != null) {
			removeDependency(mMusic.getDependency());
		}

		mMusic = music;

		if (mMusic != null) {
			addDependency(mMusic.getDependency());
		}
	}

	/**
	 * @return music of the level
	 */
	public Music getMusic() {
		return mMusic;
	}

	/**
	 * Set the theme for the level
	 * @param theme the theme for the level
	 */
	public void setTheme(Themes theme) {
		if (mTheme != null) {
			removeDependency(mTheme.getDependency());
		}

		mTheme = theme;

		if (mTheme != null) {
			addDependency(mTheme.getDependency());
		}
	}

	/**
	 * @return theme of the level
	 */
	public Themes getTheme() {
		return mTheme;
	}

	/**
	 * @return length of the level in seconds
	 */
	public float getLengthInTime() {
		return mLengthInTime;
	}

	/**
	 * Calculates the length of the level depending on the start, end, and speed of the
	 * level.
	 */
	private void calculateLength() {
		float length = mEndXCoord - mStartXCoord;
		mLengthInTime = length / mSpeed;
	}

	@Override
	public void postRead() {
		super.postRead();

		calculateLength();
	}

	/**
	 * Use placeholder image if no screenshot has been taken
	 * @return placeholder image of screenshot
	 */
	@Override
	public TextureRegionDrawable getTextureRegionDrawable() {
		TextureRegionDrawable drawable = super.getTextureRegionDrawable();

		if (drawable == null) {
			drawable = (TextureRegionDrawable) SkinNames.getDrawable(SkinNames.GeneralImages.SCREENSHOT_PLACEHOLDER);
		}

		return drawable;
	}

	/** Starting coordinate of the level (right screen edge) */
	@Tag(78) private float mStartXCoord = 0;
	/** The actual level id, i.e. not this definition's id */
	@Tag(79) private UUID mLevelId = null;
	/** Campaign id the level belongs to, null if it doesn't belong to any */
	@Tag(80) private UUID mCampaignId = null;
	/** Story before the level, set to null to not show */
	@Tag(81) private String mPrologue = "";
	/** Story after the level, set to null to not show */
	@Tag(82) private String mEpilogue = "";
	/**
	 * Base speed of the level, the actual level speed may vary as it can be changed by
	 * triggers
	 */
	@Tag(83) private float mSpeed = Config.Editor.Level.LEVEL_SPEED_DEFAULT;
	/** End of the map (right screen edge) */
	@Tag(84) private float mEndXCoord = 100;
	/** Theme of the level */
	@Tag(109) private Themes mTheme = null;
	/** Music of the level */
	@Tag(129) private Music mMusic = null;
	/** Length of the level in seconds */
	private float mLengthInTime = 0;

}
