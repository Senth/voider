package com.spiddekauga.voider.network.entities;

import java.util.Date;

/**
 * Entity for level comments
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelCommentEntity implements IEntity {
	/** The actual comment */
	public String comment;
	/** Date of comment */
	public Date date;
	/** Username of commenter */
	public String username;
}
