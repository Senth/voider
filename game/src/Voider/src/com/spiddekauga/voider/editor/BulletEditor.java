package com.spiddekauga.voider.editor;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.DrawActorTool;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.scene.TouchTool;
import com.spiddekauga.voider.scene.WorldScene;
import com.spiddekauga.voider.utils.Pools;

/**
 * Creates bullets for the enemies and player to use.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletEditor extends WorldScene implements IActorEditor, IResourceChangeEditor {
	/**
	 * Creates a bullet editor.
	 */
	public BulletEditor() {
		super(new BulletEditorGui());

		((BulletEditorGui)mGui).setBulletEditor(this);

		mWeapon.setWeaponDef(new WeaponDef());
		mWeapon.getDef().setBulletActorDef(mDef);
		Vector2 weaponPos = Pools.vector2.obtain();
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.5f, weaponPos, true);
		mWeapon.setPosition(weaponPos);
		Pools.vector2.free(weaponPos);

		mDrawActorTool = new DrawActorTool(mCamera, mWorld, BulletActor.class, mInvoker, this, mDef);
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);

		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			mGui.initGui();
			mGui.resetValues();
			mInvoker.dispose();
			mUnsaved = false;
		} else if (outcome == Outcomes.DEF_SELECTED) {
			switch (mSelectionAction) {
			case LOAD_BULLET:
				try {
					BulletActorDef bulletDef = ResourceCacheFacade.get(UUID.fromString(message), BulletActorDef.class);
					setDef(bulletDef);
					mGui.resetValues();
					mUnsaved = false;
					mInvoker.dispose();
				} catch (UndefinedResourceTypeException e) {
					Gdx.app.error("BulletEditor", e.toString());
				}
			}
		} else if (outcome == Outcomes.NOT_APPLICAPLE) {
			mGui.hideMsgBoxes();
		}
	}

	@Override
	protected void update() {
		super.update();
		mWeapon.update(Gdx.graphics.getDeltaTime());

		if (mWeapon.canShoot()) {
			mWeapon.shoot(mShootDirection);
		}

		if (mBulletActor != null && mDef.getShapeType() == ActorShapeTypes.CUSTOM) {
			mBulletActor.update(Gdx.graphics.getDeltaTime());
		}
	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		// Redo
		if (KeyHelper.isRedoPressed(keycode)) {
			mInvoker.redo();
			return true;
		}
		// Undo
		else if (KeyHelper.isUndoPressed(keycode)) {
			mInvoker.undo();
			return true;
		}
		// Main menu
		else if (KeyHelper.isBackPressed(keycode)) {
			((EditorGui)mGui).showMainMenu();
		}

		return false;
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
	public void newDef() {
		BulletActorDef newDef = new BulletActorDef();
		setDef(newDef);
		mGui.resetValues();
		mUnsaved = false;
		mInvoker.dispose();
	}

	@Override
	public void saveDef() {
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
	public void loadDef() {
		mSelectionAction = SelectionActions.LOAD_BULLET;

		Scene selectionScene = new SelectDefScene(BulletActorDef.class, true, true);
		SceneSwitcher.switchTo(selectionScene);
	}

	@Override
	public void duplicateDef() {
		mDef = (BulletActorDef) mDef.copy();
		mWeapon.getDef().setBulletActorDef(mDef);
		mGui.resetValues();
		mUnsaved = true;
		mInvoker.dispose();
	}

	@Override
	public boolean isUnsaved() {
		return mUnsaved;
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
		mDef.setStartAngle((float)Math.toRadians(angle));
		mUnsaved = true;
	}

	@Override
	public float getStartingAngle() {
		return (float)Math.toDegrees(mDef.getStartAngle());
	}

	@Override
	public void setRotationSpeed(float rotationSpeed) {
		mDef.setRotationSpeed((float) Math.toRadians(rotationSpeed));
		mUnsaved = true;
	}

	@Override
	public float getRotationSpeed() {
		return (float) Math.toDegrees(mDef.getRotationSpeed());
	}

	@Override
	public void setShapeType(ActorShapeTypes shapeType) {
		mDef.setShapeType(shapeType);
		mUnsaved = true;

		if (shapeType == ActorShapeTypes.CUSTOM) {
			mActiveTouchTool = mDrawActorTool;
			mInputMultiplexer.addProcessor(mActiveTouchTool);
			mDrawActorTool.activate();
		} else if (mActiveTouchTool == mDrawActorTool) {
			mDrawActorTool.deactivate();
			mActiveTouchTool = null;
			mInputMultiplexer.removeProcessor(mDrawActorTool);
		}
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

	@Override
	public void resetCenterOffset() {
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mBulletActor != null) {
			mBulletActor.destroyBody();

			diffOffset = Pools.vector2.obtain();
			diffOffset.set(mDef.getCenterOffset());
		}

		mDef.resetCenterOffset();

		if (mBulletActor != null) {
			diffOffset.sub(mDef.getCenterOffset());
			diffOffset.add(mBulletActor.getPosition());
			mBulletActor.setPosition(diffOffset);
			mBulletActor.createBody();
			Pools.vector2.free(diffOffset);
		}
	}

	@Override
	public void setCenterOffset(Vector2 newCenter) {
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mBulletActor != null) {
			mBulletActor.destroyBody();

			diffOffset = Pools.vector2.obtain();
			diffOffset.set(mDef.getCenterOffset());
		}

		mDef.setCenterOffset(newCenter);

		if (mBulletActor != null) {
			diffOffset.sub(mDef.getCenterOffset());
			diffOffset.add(mBulletActor.getPosition());
			mBulletActor.setPosition(diffOffset);
			mBulletActor.createBody();
			Pools.vector2.free(diffOffset);
		}
	}

	@Override
	public Vector2 getCenterOffset() {
		return mDef.getCenterOffset();
	}

	@Override
	public void setDrawActorToolState(DrawActorTool.States state) {
		mDrawActorTool.setState(state);
	}

	@Override
	public DrawActorTool.States getDrawActorToolState() {
		return mDrawActorTool.getState();
	}

	/**
	 * Sets colliding damage of the enemy
	 * @param damage how much damage the enemy will inflict on a collision
	 */
	@Override
	public void setCollisionDamage(float damage) {
		mDef.setCollisionDamage(damage);
	}

	/**
	 * @return collision damage with the enemy
	 */
	@Override
	public float getCollisionDamage() {
		return mDef.getCollisionDamage();
	}

	/**
	 * Sets whether this actor shall be destroyed on collision
	 * @param destroyOnCollision set to true to destroy the enemy on collision
	 */
	@Override
	public void setDestroyOnCollide(boolean destroyOnCollision) {
		mDef.setDestroyOnCollide(destroyOnCollision);
	}

	/**
	 * @return true if this enemy shall be destroyed on collision
	 */
	@Override
	public boolean shallDestroyOnCollide() {
		return mDef.shallDestroyOnCollide();
	}

	@Override
	public void onResourceAdded(IResource resource) {
		mBulletActor = (BulletActor) resource;
		mUnsaved = true;
	}

	@Override
	public void onResourceRemoved(IResource resource) {
		mBulletActor = null;
		mUnsaved = true;
	}

	@Override
	public void onResourceChanged(IResource resource) {
		mUnsaved = true;
	}

	@Override
	public void onResourceSelected(IResource deselectedResource, IResource selectedResource) {
		// Does nothing
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

	@Override
	public Invoker getInvoker() {
		return mInvoker;
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
		mDef = def;
		mDrawActorTool.setActorDef(def);
		mWeapon.getDef().setBulletActorDef(def);
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
	private Vector2 mShootDirection = new Vector2(1, 0);
	/** Invoker for the bullet editor */
	private Invoker mInvoker = new Invoker();
	/** Active touch tool */
	private TouchTool mActiveTouchTool = null;
	/** Current draw actor tool */
	private DrawActorTool mDrawActorTool;
	/** Current bullet actor (when drawing) */
	private BulletActor mBulletActor = null;
}