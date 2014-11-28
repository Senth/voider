package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.voider.repo.resource.SkinNames;

/**
 * GUI for finding or loading enemies
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreEnemyGui extends ExploreActorGui {
	/**
	 * Hidden constructor
	 */
	protected ExploreEnemyGui() {
		// Does nothing
	}

	@Override
	public void initGui() {
		super.initGui();


		initLeftPanel();
		initViewButtons();
	}

	/**
	 * Initialize view buttons (top left)
	 */
	private void initViewButtons() {
		// Search online
		mWidgets.search.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				// TODO Fetch initial enemies
			}
		};
		addViewButton(SkinNames.General.SEARCH, mWidgets.search.hider);
	}

	@Override
	protected void initInfo(AlignTable table) {
		super.initInfo(table);

		// Movement type
		mUiFactory.text.addPanelSection("Movement Type", table, null);

		// Movement speed

		// Collision damage

		// Destroy on collide

		// Has weapon

		// Bullet speed

		// Bullet damage

		// Weapon aim type

		Image movementType = null;
		Label movementSpeed = null;
		Label destroyOnCollide = null;
		Label collideDamage = null;
		Label hasWeapon = null;
		Label bulletSpeed = null;
		Label bulletDamage = null;
		Label aimType = null;

		// TODO
	}

	/**
	 * Initialize left panel
	 */
	private void initLeftPanel() {
		// TODO

		mLeftPanel.layout();
	}

	/**
	 * Initialize filter options
	 */
	private void initSearchFilters() {
		// TODO
	}


	@Override
	protected void onFetchMoreContent() {
		// TODO Auto-generated method stub
	}

	/**
	 * Sets the explore scene
	 * @param exploreScene
	 */
	void setExploreEnemyScene(ExploreEnemyScene exploreScene) {
		setExploreScene(exploreScene);
		mScene = exploreScene;
	}

	private ExploreEnemyScene mScene = null;
	private Widgets mWidgets = new Widgets();

	private static class Widgets implements Disposable {
		Search search = new Search();
		Info info = new Info();

		class Info implements Disposable {
			Image movementType = null;
			Label movementSpeed = null;
			Label destroyOnCollide = null;
			Label collideDamage = null;
			Label hasWeapon = null;
			Label bulletSpeed = null;
			Label bulletDamage = null;
			Label aimType = null;
			HideManual weaponHider = new HideManual();
			HideManual movementHider = new HideManual();

			@Override
			public void dispose() {
				weaponHider.dispose();
				movementHider.dispose();
			}
		}

		class Search implements Disposable {
			AlignTable table = new AlignTable();
			HideListener hider = null;

			@Override
			public void dispose() {
				table.dispose();
			}
		}

		@Override
		public void dispose() {
			search.dispose();
			info.dispose();
		}
	}
}
