package com.spiddekauga.voider.network.stat;

import com.spiddekauga.voider.network.entities.IMethodEntity;

import java.util.UUID;

/**
 * Method for getting highscores
 */
public class HighscoreGetMethod implements IMethodEntity {
/** Id of the level to get highscores for */
public UUID levelId = null;
/** Fetch options */
public Fetch fetch = null;

@Override
public MethodNames getMethodName() {
	return MethodNames.HIGHSCORE_GET;
}

/**
 * What to fetch from the server
 */
public enum Fetch {
	/** Fetches user score, top score, and scores above/below the user */
	USER_SCORE,
	/** Fetches only the top score and user place (if available) */
	FIRST_PLACE,
	/** Fetches top scores */
	TOP_SCORES,
}
}
