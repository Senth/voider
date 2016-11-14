package com.spiddekauga.voider.network.stat;

import com.spiddekauga.voider.network.entities.IEntity;

import java.util.Date;

/**
 * Entity for level comments
 */
public class CommentEntity implements IEntity {
/** The actual comment */
public String comment;
/** Date of comment */
public Date date;
/** Username of commenter */
public String username;
}
