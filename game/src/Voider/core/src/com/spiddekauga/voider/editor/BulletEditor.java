package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.explore.ExploreActions;
import com.spiddekauga.voider.explore.ExploreFactory;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.network.resource.BulletDefEntity;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Creates bullets for the enemies and player to use.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BulletEditor extends ActorEditor {
	/**
	 * Creates a bullet editor.
	 */
	public BulletEditor() {
		super(new BulletEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR, BulletActor.class);

		getGui().setBulletEditor(this);
	}

	@Override
	protected void onInit() {
		super.onInit();

		mWeapon.setWeaponDef(new WeaponDef());
		Vector2 weaponPos = new Vector2();
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.7f, weaponPos, true);
		mWeapon.setPosition(weaponPos);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		if (outcome == Outcomes.EXPLORE_LOAD) {
			if (message instanceof BulletDefEntity) {
				BulletDefEntity bulletDefEntity = (BulletDefEntity) message;

				if (!ResourceCacheFacade.isLoaded(bulletDefEntity.resourceId, bulletDefEntity.revision)) {
					ResourceCacheFacade.load(this, bulletDefEntity.resourceId, true, bulletDefEntity.revision);
					ResourceCacheFacade.finishLoading();
				}

				BulletActorDef bulletDef = ResourceCacheFacade.get(bulletDefEntity.resourceId, bulletDefEntity.revision);
				setActorDef(bulletDef);
				getGui().resetValues();
				setSaved();
				mInvoker.dispose();
			}
		} else if (outcome == Outcomes.NOT_APPLICAPLE) {
			getGui().popMsgBoxes();
		}
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mDef == null) {
			return;
		}

		mWeapon.update(deltaTime);

		if (mWeapon.canShoot()) {
			mWeapon.shoot(SHOOT_DIRECTION);
		}
	}

	@Override
	protected void render() {
		enableBlendingWithDefaults();
		super.render();

		if (Config.Graphics.USE_RELEASE_RENDERER && !isSaving() && !isDone()) {
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);

			mBulletDestroyer.render(mShapeRenderer, getBoundingBoxWorld());

			mShapeRenderer.pop();
		}
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS:
			ResourceCacheFacade.loadAllOf(this, ExternalTypes.BULLET_DEF, true);
			ResourceCacheFacade.finishLoading();
			break;

		default:
			break;
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.BULLET_DEF, true);
	}

	@Override
	public void newDef() {
		BulletActorDef newDef = new BulletActorDef();
		newDef.getVisual().setColor((Color) SkinNames.getResource(SkinNames.EditorVars.BULLET_COLOR_DEFAULT));
		setActorDef(newDef);
		getGui().resetValues();
		setSaved();
		mInvoker.dispose();
	}

	@Override
	public void loadDef() {
		SceneSwitcher.switchTo(ExploreFactory.create(BulletActorDef.class, ExploreActions.LOAD));
	}

	@Override
	public void duplicateDef() {
		mDef = mDef.copyNewResource();
		mWeapon.getDef().setBulletActorDef(mDef);
		getGui().resetValues();
		mInvoker.dispose();
		saveDef();
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
		if (mWeapon != null && mWeapon.getDef() != null) {
			return mWeapon.getDef().getCooldownMin();
		} else {
			return 0;
		}
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
		if (mWeapon != null && mWeapon.getDef() != null) {
			return mWeapon.getDef().getCooldownMax();
		} else {
			return 0;
		}
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
		if (mWeapon != null && mWeapon.getDef() != null) {
			return mWeapon.getDef().getBulletSpeed();
		} else {
			return 0;
		}
	}


	@Override
	protected void setActorDef(ActorDef def) {
		super.setActorDef(def);
		mDef = (BulletActorDef) def;
		mWeapon.getDef().setBulletActorDef(mDef);
		if (getGui().isInitialized()) {
			getGui().resetValues();
		}
	}

	@Override
	public void setDrawOnlyOutline(boolean drawOnlyOutline) {
		// Does nothing
	}

	@Override
	public boolean isDrawOnlyOutline() {
		return false;
	}

	@Override
	public void undoJustCreated() {
		setActorDef(null);
	}

	@Override
	protected BulletEditorGui getGui() {
		return (BulletEditorGui) super.getGui();
	}

	/** Current weapon that fires the bullets */
	private Weapon mWeapon = new Weapon();
	/** Current bullet definition */
	private BulletActorDef mDef = null;
	/** Shoot direction */
	private final static Vector2 SHOOT_DIRECTION = new Vector2(1, 0);
}
