package com.spiddekauga.voider.game.triggers;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.triggers.TriggerAction.Reasons;
import com.spiddekauga.voider.resources.IResource;

/**
 * Triggers when the right side of the screen is at or beyond a specific position.
 * Equal to the level's current x-coordinate
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerScreenAt extends Trigger {
	/**
	 * @param level checks this level for the x coordinate
	 * @param xCoord the x-coordinate we want the level to be at or beyond.
	 */
	public TriggerScreenAt(Level level, float xCoord) {
		mLevel = level;
		mLevelId = level.getId();
		mXCoord = xCoord;
	}

	@Override
	protected Reasons getReason() {
		return Reasons.SCREEN_AT;
	}

	@Override
	protected Object getCauseObject() {
		return null;
	}

	@Override
	protected boolean isTriggered() {
		return mLevel.getXCoord() >= mXCoord;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mLevelId", mLevelId);
		json.writeValue("mXCoord", mXCoord);
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		mLevelId = json.readValue("mLevelId", UUID.class, jsonData);
		mXCoord = json.readValue("mXCoord", float.class, jsonData);
	}

	@Override
	public void getReferences(ArrayList<UUID> references) {
		references.add(mLevelId);
	}

	@Override
	public void bindReference(IResource resource) {
		if (resource instanceof Level) {
			mLevel = (Level) resource;
		}
	}

	/** Level to check for the x-coordinate */
	private Level mLevel = null;
	/** Level id, used for binding the level */
	private UUID mLevelId = null;
	/** X-coordinate to check for */
	private float mXCoord;
}
