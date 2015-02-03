package com.spiddekauga.voider.explore;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonEnumListener;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.voider.explore.ExploreScene.ExploreViews;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.resource.BulletDamageSearchRanges;
import com.spiddekauga.voider.network.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.resource.CollisionDamageSearchRanges;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.resource.EnemySpeedSearchRanges;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.ButtonFactory.TabRadioWrapper;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.utils.User;

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

		resetContentMargins();
		mScene.repopulateContent();
	}

	@Override
	public void dispose() {
		super.dispose();

		mWidgets.dispose();
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
		mWidgets.view.search = addViewButton(SkinNames.General.EXPLORE_ONLINE_SEARCH, listener, getSearchFilterHider());

		if (!User.getGlobalUser().isOnline()) {
			mWidgets.view.search.setDisabled(true);
		}
	}

	@Override
	protected void onUserOnline() {
		if (mWidgets.view.search != null) {
			mWidgets.view.search.setDisabled(false);
		}
	}

	@Override
	protected void onUserOffline() {
		if (mWidgets.view.search != null) {
			mWidgets.view.search.setDisabled(true);
		}
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

		EnemyDefEntity enemy = mScene.getSelected();

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

	@Override
	protected void initSearchFilters(AlignTable table, GuiHider contentHider) {
		super.initSearchFilters(table, contentHider);

		// Movement Type
		mUiFactory.text.addPanelSection("Movement Type", table, null);
		table.row();
		mUiFactory.button.addEnumButton(MovementTypes.PATH, SkinNames.EditorIcons.MOVEMENT_PATH, null, table, mWidgets.search.movementTypes);
		mUiFactory.button.addEnumButton(MovementTypes.STATIONARY, SkinNames.EditorIcons.MOVEMENT_STATIONARY, null, table,
				mWidgets.search.movementTypes);
		mUiFactory.button.addEnumButton(MovementTypes.AI, SkinNames.EditorIcons.MOVEMENT_AI, null, table, mWidgets.search.movementTypes);
		new ButtonEnumListener<MovementTypes>(mWidgets.search.movementTypes, MovementTypes.values()) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.setMovementType(getChecked());

				// Hide movement speed if only stationary is active
				ArrayList<MovementTypes> movementTypes = mScene.getMovementTypes();
				if (movementTypes.size() == 1 && movementTypes.contains(MovementTypes.STATIONARY)) {
					mWidgets.search.movementHider.hide();
				} else {
					mWidgets.search.movementHider.show();
				}
			}
		};


		// Movement Speed
		mUiFactory.text.addPanelSection("Movement Speed", table, mWidgets.search.movementHider);
		mUiFactory.button.addEnumCheckboxes(EnemySpeedSearchRanges.values(), CheckBoxStyles.CHECK_BOX, mWidgets.search.movementHider, null, true,
				table, mWidgets.search.movementSpeeds);
		new ButtonEnumListener<EnemySpeedSearchRanges>(mWidgets.search.movementSpeeds, EnemySpeedSearchRanges.values()) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.setMovementSpeeds(getChecked());
			}
		};


		// Collision Damage
		mUiFactory.text.addPanelSection("Collision Damage", table, null);
		mUiFactory.button.addEnumCheckboxes(CollisionDamageSearchRanges.values(), CheckBoxStyles.CHECK_BOX, null, null, true, table,
				mWidgets.search.collisionDamages);
		new ButtonEnumListener<CollisionDamageSearchRanges>(mWidgets.search.collisionDamages, CollisionDamageSearchRanges.values()) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.setCollisionDamages(getChecked());
			}
		};


		// Destroy on Collide
		mUiFactory.text.addPanelSection("Destroy on Collide", table, null);
		TabRadioWrapper anyTab = mUiFactory.button.createTabRadioWrapper("Any");
		anyTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setDestroyOnCollide(null);
			}
		});

		TabRadioWrapper onTab = mUiFactory.button.createTabRadioWrapper("Destroyed on collision");
		onTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setDestroyOnCollide(true);
			}
		});

		TabRadioWrapper offTab = mUiFactory.button.createTabRadioWrapper("Never destroyed");
		offTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setDestroyOnCollide(false);
			}
		});

		mUiFactory.button.addTabs(table, contentHider, true, null, null, anyTab, onTab, offTab);
		mWidgets.search.destroyOnCollideAny = anyTab.getButton();
		mWidgets.search.destroyOnCollideTrue = onTab.getButton();
		mWidgets.search.destroyOnCollideFalse = offTab.getButton();


		// Has weapon
		mUiFactory.text.addPanelSection("Weapon", table, null);

		anyTab = mUiFactory.button.createTabRadioWrapper("Any");
		anyTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setHasWeapon(null);
			}
		});

		onTab = mUiFactory.button.createTabRadioWrapper("Has Weapon");
		onTab.setHider(mWidgets.search.weaponHider);
		onTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setHasWeapon(true);
			}
		});

		offTab = mUiFactory.button.createTabRadioWrapper("No Weapon");
		offTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setHasWeapon(false);
			}
		});

		mUiFactory.button.addTabs(table, contentHider, true, null, null, anyTab, onTab, offTab);
		mWidgets.search.weaponAny = anyTab.getButton();
		mWidgets.search.weaponOn = onTab.getButton();
		mWidgets.search.weaponOff = offTab.getButton();


		// Bullet Speed
		mUiFactory.text.addPanelSection("Bullet Speed", table, mWidgets.search.weaponHider);
		mUiFactory.button.addEnumCheckboxes(BulletSpeedSearchRanges.values(), CheckBoxStyles.CHECK_BOX, mWidgets.search.weaponHider, null, true,
				table, mWidgets.search.bulletSpeeds);
		new ButtonEnumListener<BulletSpeedSearchRanges>(mWidgets.search.bulletSpeeds, BulletSpeedSearchRanges.values()) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.setBulletSpeeds(getChecked());
			}
		};


		// Bullet Damage
		mUiFactory.text.addPanelSection("Bullet Damage", table, mWidgets.search.weaponHider);
		mUiFactory.button.addEnumCheckboxes(BulletDamageSearchRanges.values(), CheckBoxStyles.CHECK_BOX, mWidgets.search.weaponHider, null, true,
				table, mWidgets.search.bulletDamages);
		new ButtonEnumListener<BulletDamageSearchRanges>(mWidgets.search.bulletDamages, BulletDamageSearchRanges.values()) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.setBulletDamages(getChecked());
			}
		};


		// Aim Types
		mUiFactory.text.addPanelSection("Aim Type", table, mWidgets.search.weaponHider);
		table.row();
		mUiFactory.button.addEnumButton(AimTypes.ON_PLAYER, SkinNames.EditorIcons.AIM_ON_PLAYER, mWidgets.search.weaponHider, table,
				mWidgets.search.aimTypes);
		mUiFactory.button.addEnumButton(AimTypes.IN_FRONT_OF_PLAYER, SkinNames.EditorIcons.AIM_IN_FRONT_PLAYER, mWidgets.search.weaponHider, table,
				mWidgets.search.aimTypes);
		mUiFactory.button.addEnumButton(AimTypes.MOVE_DIRECTION, SkinNames.EditorIcons.AIM_MOVEMENT, mWidgets.search.weaponHider, table,
				mWidgets.search.aimTypes);
		mUiFactory.button.addEnumButton(AimTypes.DIRECTION, SkinNames.EditorIcons.AIM_DIRECTION, mWidgets.search.weaponHider, table,
				mWidgets.search.aimTypes);
		mUiFactory.button.addEnumButton(AimTypes.ROTATE, SkinNames.EditorIcons.AIM_ROTATE, mWidgets.search.weaponHider, table,
				mWidgets.search.aimTypes);
		new ButtonEnumListener<AimTypes>(mWidgets.search.aimTypes, AimTypes.values()) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.setAimTypes(getChecked());
			}
		};


		// Hiders
		contentHider.addChild(mWidgets.search.movementHider);
		contentHider.addChild(mWidgets.search.weaponHider);


		mLeftPanel.layout();
	}

	@Override
	protected void resetSearchFilters() {
		super.resetSearchFilters();

		resetEnumButtons(mWidgets.search.movementTypes, mScene.getMovementTypes());
		resetEnumButtons(mWidgets.search.movementSpeeds, mScene.getMovementSpeeds());
		resetEnumButtons(mWidgets.search.collisionDamages, mScene.getCollisionDamages());
		resetEnumButtons(mWidgets.search.bulletSpeeds, mScene.getBulletSpeeds());
		resetEnumButtons(mWidgets.search.bulletDamages, mScene.getBulletDamages());

		// Destroy on Collide
		if (mScene.getDestroyOnCollide() == null) {
			mWidgets.search.destroyOnCollideAny.setChecked(true);
		} else if (mScene.getDestroyOnCollide()) {
			mWidgets.search.destroyOnCollideTrue.setChecked(true);
		} else {
			mWidgets.search.destroyOnCollideFalse.setChecked(true);
		}

		// Weapon
		if (mScene.getHasWeapon() == null) {
			mWidgets.search.weaponAny.setChecked(true);
		} else if (mScene.getHasWeapon()) {
			mWidgets.search.weaponOn.setChecked(true);
		} else {
			mWidgets.search.weaponOff.setChecked(true);
		}
	}

	@Override
	protected void clearSearchFilters() {
		super.clearSearchFilters();

		clearButtons(mWidgets.search.movementTypes);
		clearButtons(mWidgets.search.movementSpeeds);
		clearButtons(mWidgets.search.collisionDamages);
		clearButtons(mWidgets.search.bulletSpeeds);
		clearButtons(mWidgets.search.bulletDamages);

		mWidgets.search.destroyOnCollideAny.setChecked(true);
		mWidgets.search.weaponAny.setChecked(true);
	}


	/**
	 * Sets the explore scene
	 * @param exploreScene
	 */
	void setExploreEnemyScene(ExploreEnemyScene exploreScene) {
		mScene = exploreScene;
	}

	private ExploreEnemyScene mScene = null;
	private Widgets mWidgets = new Widgets();

	private class Widgets implements Disposable {
		Search search = new Search();
		Info info = new Info();
		View view = new View();

		private class View {
			Button search = null;
		}

		private class Info implements Disposable {
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

		private class Search implements Disposable {
			AlignTable table = new AlignTable();
			HideManual movementHider = new HideManual();
			HideListener weaponHider = new HideListener(true);
			Button movementTypes[] = new Button[MovementTypes.values().length];
			Button movementSpeeds[] = new Button[EnemySpeedSearchRanges.values().length];
			Button weaponAny = null;
			Button weaponOn = null;
			Button weaponOff = null;
			Button bulletSpeeds[] = new Button[BulletSpeedSearchRanges.values().length];
			Button bulletDamages[] = new Button[BulletDamageSearchRanges.values().length];
			Button aimTypes[] = new Button[AimTypes.values().length];
			Button collisionDamages[] = new Button[CollisionDamageSearchRanges.values().length];
			Button destroyOnCollideAny = null;
			Button destroyOnCollideTrue = null;
			Button destroyOnCollideFalse = null;

			@Override
			public void dispose() {
				table.dispose();
				movementHider.dispose();
				weaponHider.dispose();
			}
		}

		@Override
		public void dispose() {
			search.dispose();
			info.dispose();
		}
	}
}
