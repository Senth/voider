package com.spiddekauga.voider.explore;

/**
 * What action to do when if the resource is selected
 */
public enum ExploreActions {
	/** Play the selected level */
	PLAY,
	/** Select a resource, can't select specific revisions */
	SELECT,
	/** Load a resource, can select specific revision if exists */
	LOAD,;

@Override
public String toString() {
	return name().substring(0, 1) + name().substring(1).toLowerCase();
}
}
