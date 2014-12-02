package com.spiddekauga.voider.explore;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.ButtonListener;
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

		initViewButtons();
	}

	/**
	 * Initialize view buttons (top left)
	 */
	private void initViewButtons() {
		// Search online
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setSearchOnline(true);
			}
		};
		addViewButton(SkinNames.General.SEARCH, listener);
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
