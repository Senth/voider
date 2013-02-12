package com.spiddekauga.voider.editor;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.scene.WorldScene;

/**
 * Creates bullets for the enemies and player to use.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletEditor extends WorldScene implements IActorEditor {
	/**
	 * Creates a bullet editor.
	 */
	public BulletEditor() {
		super(new BulletEditorGui());

		((BulletEditorGui)mGui).setBulletEditor(this);

		mWeapon.setWeaponDef(new WeaponDef());
		mWeapon.getDef().setBulletActorDef(mDef);
		mWeapon.setPosition(new Vector2());
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);

		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			mGui.initGui();
		} else if (outcome == Outcomes.DEF_SELECTED) {
			switch (mSelectionAction) {
			case LOAD_BULLET:
				try {
					BulletActorDef bulletDef = ResourceCacheFacade.get(UUID.fromString(message), BulletActorDef.class);
					mDef = bulletDef;
					mWeapon.getDef().setBulletActorDef(mDef);
					mGui.resetValues();
					mUnsaved = false;
				} catch (UndefinedResourceTypeException e) {
					Gdx.app.error("BulletEditor", e.toString());
				}
			}
		}
	}

	@Override
	public void update() {
		super.update();
		mWeapon.update(Gdx.graphics.getDeltaTime());

		if (mWeapon.canShoot()) {
			mWeapon.shoot(mShootDirection);
		}
	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void loadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.loadAllOf(BulletActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.unloadAllOf(BulletActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}

	@Override
	public void newActor() {
		mDef = new BulletActorDef();
		mWeapon.getDef().setBulletActorDef(mDef);
		mGui.resetValues();
		mUnsaved = false;
	}

	@Override
	public void saveActor() {
		ResourceSaver.save(mDef);

		// Load the saved actor and use it instead
		if (!ResourceCacheFacade.isLoaded(mDef.getId(), mDef.getClass())) {
			try {
				ResourceCacheFacade.load(mDef.getId(), mDef.getClass(), true);
				ResourceCacheFacade.finishLoading();

				mDef = ResourceCacheFacade.get(mDef.getId(), mDef.getClass());
			} catch (Exception e) {
				Gdx.app.error("BulletEditor", "Loading of saved actor failed! " + e.toString());
			}
		}

		mUnsaved = false;
	}

	@Override
	public void loadActor() {
		mSelectionAction = SelectionActions.LOAD_BULLET;

		Scene selectionScene = new SelectDefScene(BulletActorDef.class, true, true);
		SceneSwitcher.switchTo(selectionScene);
	}

	@Override
	public void duplicateActor() {
		mDef = (BulletActorDef) mDef.copy();
		mWeapon.getDef().setBulletActorDef(mDef);
		mGui.resetValues();
		mUnsaved = true;
	}

	public boolean isUnsaved() {
		return mUnsaved;
	}

	@Override
	public String getName() {
		return mDef.getName();
	}

	@Override
	public void setName(String name) {
		mDef.setName(name);
		mUnsaved = true;
	}

	@Override
	public void setDescription(String description) {
		mDef.setDescription(description);
		mUnsaved = true;
	}

	@Override
	public String getDescription() {
		return mDef.getDescription();
	}

	@Override
	public void setStartingAngle(float angle) {
		mDef.setStartAngle(angle);
		mUnsaved = true;
	}

	@Override
	public float getStartingAngle() {
		return mDef.getStartAngle();
	}

	@Override
	public void setShapeType(ActorShapeTypes shapeType) {
		mDef.setShapeType(shapeType);
		mUnsaved = true;
	}

	@Override
	public ActorShapeTypes getShapeType() {
		return mDef.getShapeType();
	}

	@Override
	public void setShapeRadius(float radius) {
		mDef.setShapeRadius(radius);
		mUnsaved = true;
	}

	@Override
	public float getShapeRadius() {
		return mDef.getShapeRadius();
	}

	@Override
	public void setShapeWidth(float width) {
		mDef.setShapeWidth(width);
		mUnsaved = true;
	}

	@Override
	public float getShapeWidth() {
		return mDef.getShapeWidth();
	}

	@Override
	public void setShapeHeight(float height) {
		mDef.setShapeHeight(height);
		mUnsaved = true;
	}

	@Override
	public float getShapeHeight() {
		return mDef.getShapeHeight();
	}

	/**
	 * Sets the minimum cooldown of the weapon
	 * @param time new cooldown of the weapon
	 */
	void setCooldownMin(float time) {
		mWeapon.getDef().setCooldownMin(time);
	}

	/**
	 * Minimum cooldown of the weapon
	 * @return time in seconds
	 */
	float getCooldownMin() {
		return mWeapon.getDef().getCooldownMin();
	}

	/**
	 * Sets the maximum cooldown of the weapon
	 * @param time new cooldown of the weapon
	 */
	void setCooldownMax(float time) {
		mWeapon.getDef().setCooldownMax(time);
	}

	/**
	 * Returns maximum cooldown of the weapon
	 * @return time in seconds
	 */
	float getCooldownMax() {
		return mWeapon.getDef().getCooldownMax();
	}

	/**
	 * Sets the bullet speed of the weapon
	 * @param bulletSpeed new bullet speed of the weapon
	 */
	void setBulletSpeed(float bulletSpeed) {
		mWeapon.getDef().setBulletSpeed(bulletSpeed);
	}

	/**
	 * Returns bullet speed of the weapon
	 * @return how long it travels in a second
	 */
	float getBulletSpeed() {
		return mWeapon.getDef().getBulletSpeed();
	}

	/**
	 * Enumeration for what we're currently selecting from a selection scene
	 */
	enum SelectionActions {
		/** Loading another bullet */
		LOAD_BULLET
	}

	/** Current weapon that fires the bullets */
	private Weapon mWeapon = new Weapon();
	/** If the bullet is unsaved since it was edited */
	private boolean mUnsaved = false;
	/** Current bullet definition */
	private BulletActorDef mDef = new BulletActorDef();
	/** Current selection scene */
	private SelectionActions mSelectionAction = null;
	/** Shoot direction */
	private Vector2 mShootDirection = new Vector2(-1, 0);
}
