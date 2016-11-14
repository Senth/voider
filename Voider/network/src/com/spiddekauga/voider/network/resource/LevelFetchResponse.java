package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.stat.LevelInfoEntity;

import java.util.ArrayList;

/**
 * All levels that matched the query
 */
public class LevelFetchResponse extends FetchResponse {
/** All levels */
public ArrayList<LevelInfoEntity> levels = new ArrayList<>();
}
