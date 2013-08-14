package com.spiddekauga.voider.game.triggers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourceUpdate;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Base class for all triggers
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Trigger extends Resource implements IResourceUpdate, IResourceEditorRender {
	/**
	 * Default constructor for the trigger. Creates a new unique id
	 */
	public Trigger() {
		mUniqueId = UUID.randomUUID();
	}

	@Override
	public void update(float deltaTime) {
		float totalTimeElapsed = SceneSwitcher.getGameTime().getTotalTimeElapsed();

		if (!mTriggered) {
			if (isTriggered()) {
				mTriggered = true;
				mTriggeredTime = totalTimeElapsed;
			}
		}

		if (mTriggered) {
			float timeSinceTriggered = totalTimeElapsed - mTriggeredTime;

			// Trigger and remove all triggers which delays have run out
			Iterator<TriggerInfo> iterator = mListeners.iterator();
			while (iterator.hasNext()) {
				TriggerInfo triggerListenerInfo = iterator.next();

				if (timeSinceTriggered >= triggerListenerInfo.delay) {
					TriggerAction triggerAction = new TriggerAction();
					triggerAction.action = triggerListenerInfo.action;
					triggerAction.reason = getReason();
					triggerAction.causeObject = getCauseObject();
					triggerListenerInfo.listener.onTriggered(triggerAction);

					iterator.remove();
				}
			}
		}
	}

	/**
	 * Checks if the trigger has triggered all listeners. You can safetly remove
	 * the trigger now
	 * @return true if all listeners have been triggered
	 */
	public boolean hasAllTriggered() {
		return mListeners.isEmpty();
	}

	/**
	 * @return the reason for the trigger
	 */
	protected abstract TriggerAction.Reasons getReason();

	/**
	 * @return object that caused the trigger to trigger
	 */
	protected abstract Object getCauseObject();

	/**
	 * Checks if the trigger is triggered
	 * @return true if the trigger has triggered
	 */
	public abstract boolean isTriggered();

	/**
	 * Adds a listener to the trigger
	 * @param triggerInfo all the necessary trigger information
	 */
	public void addListener(TriggerInfo triggerInfo) {
		mListeners.add(triggerInfo);
	}

	/**
	 * Removes a listener from the trigger
	 * @param listenerId the listener id to remove
	 */
	public void removeListener(UUID listenerId) {
		Iterator<TriggerInfo> iterator = mListeners.iterator();

		while (iterator.hasNext()) {
			TriggerInfo triggerListenerInfo = iterator.next();

			if (triggerListenerInfo.listener.getId().equals(listenerId)) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * @return all trigger listeners
	 */
	public ArrayList<TriggerInfo> getListeners() {
		return mListeners;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mTriggered", mTriggered);
		json.writeValue("mTriggeredTime", mTriggeredTime);
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		super.read(json, jsonValue);

		mTriggered = json.readValue("mTriggered", boolean.class, jsonValue);
		mTriggeredTime = json.readValue("mTriggeredTime", float.class, jsonValue);
	}

	/**
	 * Removes/Clears all the listeners. This will also remove the trigger from
	 * the listener
	 */
	public void clearListeners() {
		mListeners.clear();
	}

	/**
	 * Sets the trigger as selected
	 * @param selected true if the trigger is selected, false if not selected
	 */
	public void setSelected(boolean selected) {
		mSelected = selected;
	}

	/**
	 * @return true if the trigger is selected
	 */
	public boolean isSelected() {
		return mSelected;
	}

	/**
	 * Set as hidden. When set as hidden the trigger will neither be drawn nor will the
	 * player be able to select it.
	 * @param hidden set to true to hide
	 */
	public void setHidden(boolean hidden)  {
		mHidden = hidden;
	}

	/**
	 * @return true if the trigger shall be hidden, i.e. neither be drawn nor selectable.
	 */
	public boolean isHidden() {
		return mHidden;
	}

	/** Set as a hidden trigger (will not create a body and not be drawn) */
	private boolean mHidden = false;
	/** If the trigger is currently selected */
	private boolean mSelected = false;
	/** If the trigger has been triggered, this is used to avoid heavy calculations */
	private boolean mTriggered = false;
	/** Triggered time */
	private float mTriggeredTime = -1;
	/** Listener information about the trigger */
	private ArrayList<TriggerInfo> mListeners = new ArrayList<TriggerInfo>();
}
