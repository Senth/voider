package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.stat.CommentEntity;

import java.util.ArrayList;

/**
 * Returns comments for the specified level
 */
public class CommentFetchResponse extends FetchResponse {
/** All level comments */
public ArrayList<CommentEntity> comments = new ArrayList<>();
/**
 * User level comment, will only be sent if called without cursor and the player has made a comment
 * on the level.
 */
public CommentEntity userComment = null;
}
