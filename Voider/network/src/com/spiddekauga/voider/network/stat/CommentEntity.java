package com.spiddekauga.voider.network.stat;

import java.util.Date;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Entity for level comments
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CommentEntity implements IEntity {
	/** The actual comment */
	public String comment;
	/** Date of comment */
	public Date date;
	/** Username of commenter */
	public String username;
}
