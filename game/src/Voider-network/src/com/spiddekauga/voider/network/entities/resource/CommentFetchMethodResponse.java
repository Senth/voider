package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.stat.CommentEntity;

/**
 * Returns comments for the specified level
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class CommentFetchMethodResponse extends FetchMethodResponse {
	/** All level comments */
	public ArrayList<CommentEntity> comments = new ArrayList<>();
	/**
	 * User level comment, will only be sent if called without cursor and the player has
	 * made a comment on the level.
	 */
	public CommentEntity userComment = null;
}
