package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelCommentEntity;

/**
 * Returns comments for the specified level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelGetCommentMethodResponse implements IEntity {
	/** All level comments */
	public ArrayList<LevelCommentEntity> levelComments = new ArrayList<>();
	/** User level comment, will only be sent if called with offset = 0 and
	 * the player has made a comment on the level. */
	public LevelCommentEntity userComment = null;
	/** Cursor to continue query */
	public String cursor = null;
	/** True if no more comments exists */
	public boolean fetchedAll = false;
}
