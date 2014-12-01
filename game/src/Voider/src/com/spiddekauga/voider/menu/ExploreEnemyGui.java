package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.resource.BulletDamageSearchRanges;
import com.spiddekauga.voider.network.entities.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.entities.resource.CollisionDamageSearchRanges;
import com.spiddekauga.voider.network.entities.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.resource.EnemySpeedSearchRanges;
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
		initSearchFilters();

		mScene.fetchDefault();
	}

	@Override
	public void resetValues() {
		super.resetValues();


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
	protected void initInfo(AlignTable table, HideListener hider) {
		super.initInfo(table, hider);

		// Movement type
		mUiFactory.text.addPanelSection("Movement Type", table, null);
		table.row();
		mWidgets.info.movementType = mUiFactory.text.add("", table);

		// Movement speed
		mUiFactory.text.addPanelSection("Movement Speed", table, mWidgets.info.movementHider);
		table.row();
		mWidgets.info.movementSpeed = mUiFactory.text.add("", table);
		mWidgets.info.movementHider.addToggleActor(mWidgets.info.movementSpeed);

		// Collision damage
		mUiFactory.text.addPanelSection("Collision Damage", table, null);
		table.row();
		mWidgets.info.collisionDamage = mUiFactory.text.add("", table);

		// Destroy on collide
		mUiFactory.text.addPanelSection("Destroy on Collide?", table, null);
		table.row();
		mWidgets.info.destroyOnCollide = mUiFactory.text.add("", table);

		// Has weapon
		mUiFactory.text.addPanelSection("Has Weapon", table, null);
		table.row();
		mWidgets.info.hasWeapon = mUiFactory.text.add("", table);

		// Bullet speed
		mUiFactory.text.addPanelSection("Bullet Speed", table, mWidgets.info.weaponHider);
		table.row();
		mWidgets.info.bulletSpeed = mUiFactory.text.add("", table);
		mWidgets.info.weaponHider.addToggleActor(mWidgets.info.bulletSpeed);

		// Bullet damage
		mUiFactory.text.addPanelSection("Bullet Damage", table, mWidgets.info.weaponHider);
		table.row();
		mWidgets.info.bulletDamage = mUiFactory.text.add("", table);
		mWidgets.info.weaponHider.addToggleActor(mWidgets.info.bulletDamage);

		// Weapon aim type
		mUiFactory.text.addPanelSection("Aim Type", table, mWidgets.info.weaponHider);
		table.row();
		mWidgets.info.aimType = mUiFactory.text.add("", table);
		mWidgets.info.weaponHider.addToggleActor(mWidgets.info.aimType);


		// Hiders
		hider.addChild(mWidgets.info.movementHider);
		hider.addChild(mWidgets.info.weaponHider);
	}

	@Override
	protected void resetInfo() {
		super.resetInfo();

		EnemyDefEntity enemy = mScene.getSelectedActor();

		if (enemy != null) {
			// Has created UI elements
			if (mWidgets.info.aimType != null) {
				// Weapon
				mWidgets.info.hasWeapon.setText(enemy.hasWeapon ? "Yes" : "No");
				if (enemy.hasWeapon) {
					mWidgets.info.weaponHider.show();
					mWidgets.info.aimType.setText(enemy.aimType.toString());
					mWidgets.info.bulletDamage.setText(getEnumString(BulletDamageSearchRanges.getRange(enemy.bulletDamage)));
					mWidgets.info.bulletSpeed.setText(getEnumString(BulletSpeedSearchRanges.getRange(enemy.bulletSpeed)));
				} else {
					mWidgets.info.weaponHider.hide();
				}

				// Collision
				mWidgets.info.collisionDamage.setText(getEnumString(CollisionDamageSearchRanges.getRange(enemy.collisionDamage)));
				mWidgets.info.destroyOnCollide.setText(enemy.destroyOnCollide ? "Yes" : "No");

				// Movement
				mWidgets.info.movementType.setText(enemy.movementType.toString());
				if (enemy.movementType != MovementTypes.STATIONARY) {
					mWidgets.info.movementHider.show();
					mWidgets.info.movementSpeed.setText(getEnumString(EnemySpeedSearchRanges.getRange(enemy.movementSpeed)));
				} else {
					mWidgets.info.movementHider.hide();
				}

			}
		} else {
			// Has created UI elements
			if (mWidgets.info.aimType != null) {
				// Weapon
				mWidgets.info.hasWeapon.setText("");
				mWidgets.info.weaponHider.hide();

				// Collision
				mWidgets.info.collisionDamage.setText("");
				mWidgets.info.destroyOnCollide.setText("");

				// Movement
				mWidgets.info.movementType.setText("");
				mWidgets.info.movementHider.hide();
			}
		}
	}

	/**
	 * Initialize left panel
	 */
	private void initLeftPanel() {
		mUiFactory.addTabScroll(SkinNames.General.SEARCH_FILTER, mWidgets.search.table, mWidgets.search.hider, mLeftPanel);

		mLeftPanel.layout();
	}

	/**
	 * Initialize filter options
	 */
	private void initSearchFilters() {
		AlignTable table = mWidgets.search.table;

		mUiFactory.text.addPanelSection("Search Filter", table, null);

		// Movement Type
		mUiFactory.text.addPanelSection("Movement Type", table, null);


		// Movement Speed
		mUiFactory.text.addPanelSection("Movement Speed", table, mWidgets.search.movementHider);

		// Collision Damage
		mUiFactory.text.addPanelSection("Collision Damage", table, null);

		// Destroy on Collide
		mUiFactory.text.addPanelSection("Destroy on Collide", table, null);

		// Has weapon
		mUiFactory.text.addPanelSection("Weapon", table, null);

		// Bullet Speed
		mUiFactory.text.addPanelSection("Bullet Speed", table, mWidgets.search.weaponHider);

		// Bullet Damage
		mUiFactory.text.addPanelSection("Bullet Damage", table, mWidgets.search.weaponHider);

		// Aim Types
		mUiFactory.text.addPanelSection("Aim Type", table, mWidgets.search.weaponHider);
		// Clear button
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
			Label movementType = null;
			Label movementSpeed = null;
			Label destroyOnCollide = null;
			Label collisionDamage = null;
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
			HideListener hider = new HideListener(true);
			HideManual movementHider = new HideManual();
			HideManual weaponHider = new HideManual();
			Button movementTypes[] = new Button[MovementTypes.values().length];
			Button movementSpeeds[] = new Button[EnemySpeedSearchRanges.values().length];
			Button weaponSkip = null;
			Button weaponOn = null;
			Button weaponOff = null;
			Button bulletSpeeds[] = new Button[BulletSpeedSearchRanges.values().length];
			Button bulletDamages[] = new Button[BulletDamageSearchRanges.values().length];
			Button aimTypes[] = new Button[AimTypes.values().length];
			Button collisionDamages[] = new Button[CollisionDamageSearchRanges.values().length];
			Button destroyOnCollideSkip = null;
			Button destroyOnCollideTrue = null;
			Button destroyOnCollideFalse = null;

			private Search() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				hider.dispose();
				movementHider.dispose();
				weaponHider.dispose();
			}

			private void init() {
				hider.addChild(movementHider);
				hider.addChild(weaponHider);
			}
		}

		@Override
		public void dispose() {
			search.dispose();
			info.dispose();
		}
	}
}
