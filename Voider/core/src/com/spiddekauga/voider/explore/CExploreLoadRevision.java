package com.spiddekauga.voider.explore;

import com.spiddekauga.utils.commands.Command;

/**
 * Sets the revision for the currently selected resource
 */
class CExploreLoadRevision extends Command {
private ExploreGui mExploreGui = null;
private ExploreScene mExploreScene;
private int mRevision = -1;

/**
 * Set the revision for the current selected definition
 * @param revision the revision to use
 * @param exploreScene
 */
CExploreLoadRevision(int revision, ExploreScene exploreScene) {
	mExploreScene = exploreScene;
	mRevision = revision;
}

/**
 * Set the revision for the current selected definition. Uses the ExploreScene's GUI to get the
 * currently selected revision
 * @param exploreScene
 * @param exploreGui
 */
CExploreLoadRevision(ExploreScene exploreScene, ExploreGui exploreGui) {
	mExploreScene = exploreScene;
	mExploreGui = exploreGui;
}

@Override
public boolean execute() {
	int revisionToLoad = -1;
	if (mRevision > 0) {
		revisionToLoad = mRevision;
	}
	// Get current revision
	else if (mExploreGui != null) {
		revisionToLoad = mExploreGui.getSelectedRevision();
	} else {
		return false;
	}
	mExploreScene.setRevision(revisionToLoad);
	mExploreScene.selectAction();

	return true;
}

@Override
public boolean undo() {
	return false;
}
}
