package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.explore.ExploreActions;
import com.spiddekauga.voider.explore.ExploreFactory;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.network.entities.resource.BulletDefEntity;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Messages;
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

		((BulletEditorGui) mGui).setBulletEditor(this);
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
				setDef(bulletDef);
				mGui.resetValues();
				setSaved();
				mInvoker.dispose();
			}
		} else if (outcome == Outcomes.NOT_APPLICAPLE) {
			mGui.hideMsgBoxes();
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
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		super.render();

		if (Config.Graphics.USE_RELEASE_RENDERER && !isSaving() && !isDone()) {
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);

			mBulletDestroyer.render(mShapeRenderer);

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
		setDef(newDef);
		mGui.resetValues();
		setSaved();
		mInvoker.dispose();
	}

	@Override
	public void saveDef() {
		setSaving(mDef, new BulletActor());
	}

	@Override
	public void saveDef(Command command) {
		setSaving(mDef, new BulletActor(), command);
	}

	@Override
	protected void saveToFile() {
		int oldRevision = mDef.getRevision();

		mResourceRepo.save(this, mDef);
		mNotification.show(NotificationTypes.SUCCESS, Messages.Info.SAVED);
		showSyncMessage();

		// Saved first time? Then load it and use the loaded version
		if (!ResourceCacheFacade.isLoaded(mDef.getId())) {
			ResourceCacheFacade.load(this, mDef.getId(), true);
			ResourceCacheFacade.finishLoading();

			setDef((BulletActorDef) ResourceCacheFacade.get(mDef.getId()));
		}

		// Update latest loaded resource
		if (oldRevision != mDef.getRevision() - 1) {
			ResourceCacheFacade.setLatestResource(mDef, oldRevision);
		}

		setSaved();
	}

	@Override
	public void loadDef() {
		SceneSwitcher.switchTo(ExploreFactory.create(BulletActorDef.class, ExploreActions.LOAD));
	}

	@Override
	public void duplicateDef() {
		mDef = mDef.copyNewResource();
		mWeapon.getDef().setBulletActorDef(mDef);
		mGui.resetValues();
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


	/**
	 * Sets a new definition for the bullet
	 * @param def the new definition to use
	 */
	private void setDef(BulletActorDef def) {
		setActorDef(def);

		mDef = def;
		mWeapon.getDef().setBulletActorDef(def);
		if (mGui.isInitialized()) {
			mGui.resetValues();
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
		setDef(null);
	}


	/** Current weapon that fires the bullets */
	private Weapon mWeapon = new Weapon();
	/** Current bullet definition */
	private BulletActorDef mDef = null;
	/** Shoot direction */
	private final static Vector2 SHOOT_DIRECTION = new Vector2(1, 0);
}
