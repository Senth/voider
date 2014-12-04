package com.spiddekauga.voider.explore;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.explore.ExploreScene.ExploreViews;
import com.spiddekauga.voider.repo.resource.SkinNames;

/**
 * GUI for finding or loading bullets
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreBulletGui extends ExploreActorGui {
	/**
	 * Hidden constructor
	 */
	protected ExploreBulletGui() {
		// Does nothing
	}

	@Override
	public void initGui() {
		super.initGui();

		resetContentMargins();
		mScene.repopulateContent();
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
		addViewButton(SkinNames.General.EXPLORE_ONLINE_SEARCH, listener, getSearchFilterHider());
	}

	/**
	 * Sets the explore scene
	 * @param exploreScene
	 */
	void setExploreBulletScene(ExploreBulletScene exploreScene) {
		mScene = exploreScene;
	}

	private ExploreBulletScene mScene = null;
}
