package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.brushes.VectorBrush;
import com.spiddekauga.voider.editor.tools.AddMoveCornerTool;
import com.spiddekauga.voider.editor.tools.DeleteTool;
import com.spiddekauga.voider.editor.tools.DrawAppendTool;
import com.spiddekauga.voider.editor.tools.DrawEraseTool;
import com.spiddekauga.voider.editor.tools.MoveTool;
import com.spiddekauga.voider.editor.tools.RemoveCornerTool;
import com.spiddekauga.voider.editor.tools.Selection;
import com.spiddekauga.voider.editor.tools.SetCenterTool;
import com.spiddekauga.voider.editor.tools.TouchTool;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * Creates bullets for the enemies and player to use.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletEditor extends Editor implements IActorEditor, IResourceChangeEditor {
	/**
	 * Creates a bullet editor.
	 */
	public BulletEditor() {
		super(new BulletEditorGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR);

		((BulletEditorGui) mGui).setBulletEditor(this);

		mWeapon.setWeaponDef(new WeaponDef());
		Vector2 weaponPos = Pools.vector2.obtain();
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.7f, weaponPos, true);
		mWeapon.setPosition(weaponPos);
		Pools.vector2.free(weaponPos);

		// Initialize tools
		mTools[Tools.MOVE.ordinal()] = new MoveTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.DELETE.ordinal()] = new DeleteTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.DRAW_APPEND.ordinal()] = new DrawAppendTool(mCamera, mWorld, mInvoker, mSelection, this, BulletActor.class);
		mTools[Tools.DRAW_ERASE.ordinal()] = new DrawEraseTool(mCamera, mWorld, mInvoker, mSelection, this, BulletActor.class);
		mTools[Tools.ADD_MOVE_CORNER.ordinal()] = new AddMoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.REMOVE_CORNER.ordinal()] = new RemoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.SET_CENTER.ordinal()] = new SetCenterTool(mCamera, mWorld, mInvoker, mSelection, this, BulletActor.class);
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		mGui.dispose();
		mGui.initGui();
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);
		Actor.setLevel(null);

		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			mInvoker.dispose();

			ShaderProgram defaultShader = ResourceCacheFacade.get(ResourceNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
		}
		else if (outcome == Outcomes.DEF_SELECTED) {
			switch (mSelectionAction) {
			case LOAD_BULLET:
				if (message instanceof ResourceItem) {
					BulletActorDef bulletDef = ResourceCacheFacade.get(this, ((ResourceItem) message).id, ((ResourceItem) message).revision);
					setDef(bulletDef);
					mGui.resetValues();
					setSaved();
					mInvoker.dispose();
				}
				else {
					Gdx.app.error("MainMenu", "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
				}
			}
		}
		else if (outcome == Outcomes.NOT_APPLICAPLE) {
			mGui.hideMsgBoxes();
		}
	}

	@Override
	public boolean isDrawing() {
		if (mTools[mActiveTool.ordinal()] != null) {
			return mTools[mActiveTool.ordinal()].isDrawing();
		}
		else {
			return false;
		}
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mDef == null) {
			((EditorGui)mGui).showFirstTimeMenu();
			return;
		}

		// Force the player to set a name
		if (mDef.getName().equals(Config.Actor.NAME_DEFAULT)) {
			((ActorGui)mGui).showInfoDialog();
			mGui.showErrorMessage("Please enter a bullet name");
		}

		mWeapon.update(deltaTime);

		if (mWeapon.canShoot()) {
			mWeapon.shoot(SHOOT_DIRECTION);
		}

		if (mBulletActor != null && mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			mBulletActor.updateEditor();
		}

		if (shallAutoSave()) {
			saveDef();
			mGui.showErrorMessage(Messages.Info.SAVING);
		}
	}

	@Override
	protected void render() {
		super.render();

		if (Config.Graphics.USE_RELEASE_RENDERER && !isSaving()) {
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			mBulletDestroyer.render(mShapeRenderer);

			if (mBulletActor != null && mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
				mBulletActor.render(mShapeRenderer);
				mBulletActor.renderEditor(mShapeRenderer);
			}

			if (mVectorBrush != null) {
				mVectorBrush.renderEditor(mShapeRenderer);
			}

			mShapeRenderer.pop();
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.loadAllOf(this, BulletActorDef.class, true);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unloadAllOf(this, BulletActorDef.class, true);
	}

	@Override
	protected ActorDef getActorDef() {
		return mDef;
	}

	@Override
	protected Actor getNewActor() {
		return new BulletActor();
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
	protected void saveToFile() {
		ResourceSaver.save(mDef);

		// Saved first time? Then load it and use the loaded version
		if (!ResourceCacheFacade.isLoaded(this, mDef.getId())) {
			ResourceCacheFacade.load(this, mDef.getId(), true);
			ResourceCacheFacade.finishLoading();

			setDef((BulletActorDef) ResourceCacheFacade.get(this, mDef.getId()));
		}

		setSaved();
	}

	@Override
	public void loadDef() {
		mSelectionAction = SelectionActions.LOAD_BULLET;

		Scene selectionScene = new SelectDefScene(BulletActorDef.class, true, true, true);
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

	@Override
	public String getName() {
		if (mDef != null) {
			return mDef.getName();
		} else {
			return "";
		}
	}

	@Override
	public void setName(String name) {
		if (mDef == null) {
			return;
		}
		mDef.setName(name);
		setUnsaved();
	}

	@Override
	public void setDescription(String description) {
		if (mDef == null) {
			return;
		}
		mDef.setDescription(description);
		setUnsaved();
	}

	@Override
	public String getDescription() {
		if (mDef != null) {
			return mDef.getDescription();
		} else {
			return "";
		}
	}

	@Override
	public void setStartingAngle(float angle) {
		if (mDef == null) {
			return;
		}
		mDef.setStartAngleDeg(angle);
		setUnsaved();
	}

	@Override
	public float getStartingAngle() {
		if (mDef != null) {
			return mDef.getStartAngleDeg();
		} else {
			return 0;
		}
	}

	@Override
	public void setRotationSpeed(float rotationSpeed) {
		if (mDef == null) {
			return;
		}
		mDef.setRotationSpeedDeg(rotationSpeed);
		setUnsaved();
	}

	@Override
	public float getRotationSpeed() {
		if (mDef != null) {
			return mDef.getRotationSpeedDeg();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeType(ActorShapeTypes shapeType) {
		if (mDef == null) {
			return;
		}

		mDef.getVisualVars().setShapeType(shapeType);
		setUnsaved();

		if (shapeType == ActorShapeTypes.CUSTOM) {
			switchTool(Tools.DRAW_APPEND);

			// Add delete tool to input multiplexer
			if (!mInputMultiplexer.getProcessors().contains(mTools[Tools.DELETE.ordinal()], true)) {
				mInputMultiplexer.addProcessor(mTools[Tools.DELETE.ordinal()]);
			}
		}
		else {
			switchTool(Tools.NONE);
			mInputMultiplexer.removeProcessor(mTools[Tools.DELETE.ordinal()]);
		}
	}

	@Override
	public ActorShapeTypes getShapeType() {
		if (mDef != null) {
			return mDef.getVisualVars().getShapeType();
		} else {
			return null;
		}
	}

	@Override
	public void setShapeRadius(float radius) {
		if (mDef == null) {
			return;
		}
		mDef.getVisualVars().setShapeRadius(radius);
		setUnsaved();
	}

	@Override
	public float getShapeRadius() {
		if (mDef != null) {
			return mDef.getVisualVars().getShapeRadius();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeWidth(float width) {
		if (mDef == null) {
			return;
		}
		mDef.getVisualVars().setShapeWidth(width);
		setUnsaved();
	}

	@Override
	public float getShapeWidth() {
		if (mDef != null) {
			return mDef.getVisualVars().getShapeWidth();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeHeight(float height) {
		if (mDef == null) {
			return;
		}
		mDef.getVisualVars().setShapeHeight(height);
		setUnsaved();
	}

	@Override
	public float getShapeHeight() {
		if (mDef != null) {
			return mDef.getVisualVars().getShapeHeight();
		} else {
			return 0;
		}
	}

	@Override
	public void resetCenterOffset() {
		if (mDef == null) {
			return;
		}
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mBulletActor != null && mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			mBulletActor.destroyBody();

			diffOffset = Pools.vector2.obtain();
			diffOffset.set(mDef.getVisualVars().getCenterOffset());
		}

		mDef.getVisualVars().resetCenterOffset();

		if (mBulletActor != null && mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			diffOffset.sub(mDef.getVisualVars().getCenterOffset());
			diffOffset.add(mBulletActor.getPosition());
			mBulletActor.setPosition(diffOffset);
			mBulletActor.createBody();
			Pools.vector2.free(diffOffset);
		}

		setUnsaved();
	}

	@Override
	public void setCenterOffset(Vector2 newCenter) {
		if (mDef == null) {
			return;
		}

		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mBulletActor != null && mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			mBulletActor.destroyBody();

			diffOffset = Pools.vector2.obtain();
			diffOffset.set(mDef.getVisualVars().getCenterOffset());
		}

		mDef.getVisualVars().setCenterOffset(newCenter);

		if (mBulletActor != null && mDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			diffOffset.sub(mDef.getVisualVars().getCenterOffset());
			diffOffset.add(mBulletActor.getPosition());
			mBulletActor.setPosition(diffOffset);
			mBulletActor.createBody();
			Pools.vector2.free(diffOffset);
		}

		setUnsaved();
	}

	@Override
	public Vector2 getCenterOffset() {
		if (mDef != null) {
			return mDef.getVisualVars().getCenterOffset();
		} else {
			return new Vector2();
		}
	}

	/**
	 * Sets colliding damage of the enemy
	 * 
	 * @param damage
	 *            how much damage the enemy will inflict on a collision
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
	 * 
	 * @param destroyOnCollision
	 *            set to true to destroy the enemy on collision
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

	@Override
	public void onResourceAdded(IResource resource) {
		if (resource instanceof BulletActor) {
			mBulletActor = (BulletActor) resource;

			// Set position other than center
			Vector2 worldPosition = Pools.vector2.obtain();
			screenToWorldCoord(mCamera, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 3, worldPosition, false);
			mBulletActor.setPosition(worldPosition);
			Pools.vector2.free(worldPosition);

			setUnsaved();
		}
		else if (resource instanceof VectorBrush) {
			mVectorBrush = (VectorBrush) resource;
		}
	}

	@Override
	public void onResourceRemoved(IResource resource) {
		if (resource instanceof BulletActor) {
			mDef.getVisualVars().clearCorners();
			mBulletActor = null;
			setUnsaved();
		}
		else if (resource instanceof VectorBrush) {
			mVectorBrush = null;
		}
	}

	@Override
	public void onResourceChanged(IResource resource) {
		if (resource instanceof BulletActor) {
			setUnsaved();
		}
	}

	/**
	 * Sets the minimum cooldown of the weapon
	 * 
	 * @param time
	 *            new cooldown of the weapon
	 */
	void setCooldownMin(float time) {
		mWeapon.getDef().setCooldownMin(time);
	}

	/**
	 * Minimum cooldown of the weapon
	 * 
	 * @return time in seconds
	 */
	float getCooldownMin() {
		return mWeapon.getDef().getCooldownMin();
	}

	/**
	 * Sets the maximum cooldown of the weapon
	 * 
	 * @param time
	 *            new cooldown of the weapon
	 */
	void setCooldownMax(float time) {
		mWeapon.getDef().setCooldownMax(time);
	}

	/**
	 * Returns maximum cooldown of the weapon
	 * 
	 * @return time in seconds
	 */
	float getCooldownMax() {
		return mWeapon.getDef().getCooldownMax();
	}

	/**
	 * Sets the bullet speed of the weapon
	 * 
	 * @param bulletSpeed
	 *            new bullet speed of the weapon
	 */
	void setBulletSpeed(float bulletSpeed) {
		mWeapon.getDef().setBulletSpeed(bulletSpeed);
	}

	/**
	 * Returns bullet speed of the weapon
	 * 
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

	/**
	 * Sets a new definition for the bullet
	 * 
	 * @param def
	 *            the new definition to use
	 */
	private void setDef(BulletActorDef def) {
		mDef = def;
		((DrawAppendTool) mTools[Tools.DRAW_APPEND.ordinal()]).setActorDef(def);
		mWeapon.getDef().setBulletActorDef(def);
		setShapeType(mDef.getVisualVars().getShapeType());
		if (mGui.isInitialized()) {
			mGui.resetValues();
		}
	}

	/**
	 * Switch currently selected tool
	 * 
	 * @param tool
	 *            the new tool to use
	 */
	public void switchTool(Tools tool) {
		if (mTools[mActiveTool.ordinal()] != null) {
			mTools[mActiveTool.ordinal()].deactivate();

			// Never remove delete tool
			if (mActiveTool != Tools.DELETE) {
				mInputMultiplexer.removeProcessor(mTools[mActiveTool.ordinal()]);
			}
		}

		mActiveTool = tool;

		if (mTools[mActiveTool.ordinal()] != null) {
			mTools[mActiveTool.ordinal()].activate();

			// Never add delete tool
			if (mActiveTool != Tools.DELETE) {
				mInputMultiplexer.addProcessor(mTools[mActiveTool.ordinal()]);
			}
		}
	}

	public Tools getActiveTool() {
		return mActiveTool;
	}


	/** Active tool */
	private TouchTool[] mTools = new TouchTool[Tools.values().length];
	/** Current tool state */
	private Tools mActiveTool = Tools.NONE;
	/** Selection */
	private Selection mSelection = new Selection();
	/** Current weapon that fires the bullets */
	private Weapon mWeapon = new Weapon();
	/** Current bullet definition */
	private BulletActorDef mDef = null;
	/** Current selection scene */
	private SelectionActions mSelectionAction = null;
	/** Shoot direction */
	private final static Vector2 SHOOT_DIRECTION = new Vector2(1, 0);
	/** Current bullet actor (when drawing) */
	private BulletActor mBulletActor = null;
	/** Draw brush to render */
	private VectorBrush mVectorBrush = null;
}
