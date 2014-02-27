package com.spiddekauga.voider.network.entities;

import java.util.Date;

/**
 * Entity for level comments
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelCommentEntity implements IEntity {
	/** The actual comment */
	String comment;
	/** Date of comment */
	Date date;
	/** Username of commenter */
	String username;
}
