package com.spiddekauga.voider.explore;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.explore.ExploreScene.ExploreViews;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.user.User;

/**
 * GUI for finding or loading bullets
 */
public class ExploreBulletGui extends ExploreActorGui {
private ExploreBulletScene mScene = null;
private Button mViewButton = null;

/**
 * Hidden constructor
 */
protected ExploreBulletGui() {
	// Does nothing
}

@Override
public void onCreate() {
	super.onCreate();

	resetContentMargins();
	mScene.repopulateContent();
}

@Override
protected void onUserOnline() {
	if (mViewButton != null) {
		mViewButton.setDisabled(false);
	}
}

@Override
protected void onUserOffline() {
	if (mViewButton != null) {
		mViewButton.setDisabled(true);
	}
}

@Override
protected void initViewButtons() {
	super.initViewButtons();

	// Search online
	ButtonListener listener = new ButtonListener() {
		@Override
		protected void onPressed(Button button) {
			mScene.setView(ExploreViews.ONLINE_SEARCH);
		}
	};
	mViewButton = addViewButton(SkinNames.General.EXPLORE_ONLINE_SEARCH, listener, getSearchFilterHider());

	if (!User.getGlobalUser().isOnline()) {
		mViewButton.setDisabled(true);
	}
}

/**
 * Sets the explore scene
 */
void setExploreBulletScene(ExploreBulletScene exploreScene) {
	mScene = exploreScene;
}
}
