package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.menu.SelectDefScene;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;
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
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		mGui.dispose();
		mGui.initGui();
	}

	@Override
	protected void onInit() {
		super.onInit();

		mWeapon.setWeaponDef(new WeaponDef());
		Vector2 weaponPos = Pools.vector2.obtain();
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.7f, weaponPos, true);
		mWeapon.setPosition(weaponPos);
		Pools.vector2.free(weaponPos);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);
		Actor.setLevel(null);

		if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
			mInvoker.dispose();

			ShaderProgram defaultShader = ResourceCacheFacade.get(InternalNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
		} else if (outcome == Outcomes.DEF_SELECTED) {
			switch (mSelectionAction) {
			case LOAD_BULLET:
				if (message instanceof ResourceItem) {
					ResourceItem resourceItem = (ResourceItem) message;

					if (!ResourceCacheFacade.isLoaded(resourceItem.id, resourceItem.revision)) {
						ResourceCacheFacade.load(this, resourceItem.id, true, resourceItem.revision);
						ResourceCacheFacade.finishLoading();
					}

					BulletActorDef bulletDef = ResourceCacheFacade.get(resourceItem.id, resourceItem.revision);
					setDef(bulletDef);
					mGui.resetValues();
					setSaved();
					mInvoker.dispose();
				} else {
					Gdx.app.error("MainMenu", "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
				}
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
	protected void unloadResources() {
		super.unloadResources();
	}

	@Override
	public void newDef() {
		BulletActorDef newDef = new BulletActorDef();
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
		mGui.showSuccessMessage(Messages.Info.SAVED);
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
		mSelectionAction = SelectionActions.LOAD_BULLET;

		Scene selectionScene = new SelectDefScene(ExternalTypes.BULLET_DEF, "Load", true, true, true);
		SceneSwitcher.switchTo(selectionScene);
	}

	@Override
	public void duplicateDef() {
		mDef = (BulletActorDef) mDef.copy();
		mWeapon.getDef().setBulletActorDef(mDef);
		mGui.resetValues();
		mInvoker.dispose();
		saveDef();
	}

	@Override
	public boolean hasUndo() {
		return true;
	}

	@Override
	public void undo() {
		mInvoker.undo();
	}

	@Override
	public void redo() {
		mInvoker.redo();
	}

	/**
	 * Sets colliding damage of the enemy
	 * @param damage how much damage the enemy will inflict on a collision
	 */
	@Override
	public void setCollisionDamage(float damage) {
		if (mDef != null) {
			mDef.setCollisionDamage(damage);

			setUnsaved();
		}
	}

	/**
	 * @return collision damage with the enemy
	 */
	@Override
	public float getCollisionDamage() {
		if (mDef != null) {
			return mDef.getCollisionDamage();
		} else {
			return 0;
		}
	}

	/**
	 * Sets whether this actor shall be destroyed on collision
	 * @param destroyOnCollision set to true to destroy the enemy on collision
	 */
	@Override
	public void setDestroyOnCollide(boolean destroyOnCollision) {
		if (mDef != null) {
			mDef.setDestroyOnCollide(destroyOnCollision);

			setUnsaved();
		}
	}

	/**
	 * @return true if this enemy shall be destroyed on collision
	 */
	@Override
	public boolean isDestroyedOnCollide() {
		if (mDef != null) {
			return mDef.isDestroyedOnCollide();
		} else {
			return false;
		}
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
	 * Enumeration for what we're currently selecting from a selection scene
	 */
	enum SelectionActions {
		/** Loading another bullet */
		LOAD_BULLET
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
	public void undoJustCreated() {
		setDef(null);
	}


	/** Current weapon that fires the bullets */
	private Weapon mWeapon = new Weapon();
	/** Current bullet definition */
	private BulletActorDef mDef = null;
	/** Current selection scene */
	private SelectionActions mSelectionAction = null;
	/** Shoot direction */
	private final static Vector2 SHOOT_DIRECTION = new Vector2(1, 0);
}
