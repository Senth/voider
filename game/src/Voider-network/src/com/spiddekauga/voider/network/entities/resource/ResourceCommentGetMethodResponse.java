package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.entities.stat.ResourceCommentEntity;

/**
 * Returns comments for the specified level
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceCommentGetMethodResponse implements IEntity, ISuccessStatuses {
	/** All level comments */
	public ArrayList<ResourceCommentEntity> comments = new ArrayList<>();
	/**
	 * User level comment, will only be sent if called without cursor and the player has
	 * made a comment on the level.
	 */
	public ResourceCommentEntity userComment = null;
	/** Cursor to continue query */
	public String cursor = null;
	/** True if no more comments exists */
	public boolean fetchedAll = false;
	/** Response status */
	public Statuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/** Response statuses */
	public enum Statuses implements ISuccessStatuses {
		/** Have fetched all comments */
		SUCCESS_FETCHED_ALL,
		/** More comments exists to be fetched */
		SUCCESS_MORE_EXISTS,
		/** Failed internal error */
		FAILED_INTERNAL,
		/** Failed to connect to server */
		FAILED_CONNECTION,
		/** Failed user is not logged in */
		FAILED_USER_NOT_LOGGED_IN,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
