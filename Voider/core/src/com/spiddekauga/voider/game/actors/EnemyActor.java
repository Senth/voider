package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.triggers.TActorActivated;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.EditorImages;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceEditorRenderSprite;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

import java.util.List;

/**
 * An enemy actor, these can generally shoot on the player.
 */
public class EnemyActor extends Actor implements IResourceEditorRenderSprite {
private static final int CLASS_REVISION = 2;
@Tag(132)
private int mClassRevision = 0;
private Sprite mNotSpawnSprite = null;

@Tag(141)
private Weapon mWeapon = new Weapon();
@Tag(142)
private float mShootAngle = 0;
@Tag(75)
private EnemyGroup mGroup = null;
@Tag(140)
private boolean mGroupLeader = false;
@Tag(138)
private float mRandomMoveNext = 0;
@Tag(139)
private Vector2 mRandomMoveDirection = new Vector2();
@Tag(133)
private Path mPath = null;
@Tag(134)
private int mPathIndexNext = -1;
@Tag(135)
private boolean mPathForward = true;
@Tag(136)
private boolean mPathOnceReachedEnd = false;
@Tag(137)
private Vector2 mTargetDirection = new Vector2();


/**
 * Default constructor
 */
public EnemyActor() {
	deactivate();
}

/**
 * Slows down the enemy
 * @param deltaTime time elapsed since last frame
 */
private void slowDown(float deltaTime) {
	if (getBody() != null) {
		Vector2 velocity = Pools.vector2.obtain().set(getBody().getLinearVelocity());
		Vector2 dampVelocity = Pools.vector2.obtain().set(velocity);

		float dampening = Config.Actor.Enemy.LINEAR_DAMPENING * deltaTime;
		dampVelocity.scl(dampening);
		velocity.sub(dampVelocity);

		getBody().setLinearVelocity(velocity);

		Pools.vector2.freeAll(velocity, dampVelocity);
	}
}

@Override
public void renderEditorSprite(SpriteBatch spriteBatch) {
	// Highlight enemies that won't be spawned if will be spawned when test
	// running the level
	Scene scene = SceneSwitcher.getActiveScene(false);
	if (scene instanceof LevelEditor) {
		LevelEditor levelEditor = (LevelEditor) scene;
		if (mNotSpawnSprite == null) {
			TextureRegion region = SkinNames.getRegion(EditorImages.ENEMY_NOT_SPAWNED);
			mNotSpawnSprite = new Sprite(region);
			mNotSpawnSprite.setScale(Config.Graphics.WORLD_SCALE);
			float originX = mNotSpawnSprite.getWidth() * Config.Graphics.WORLD_SCALE / 2;
			float originY = mNotSpawnSprite.getHeight() * Config.Graphics.WORLD_SCALE / 2;
			mNotSpawnSprite.setOrigin(-originX, -originY);

			if (mNotSpawnSprite == null) {
				return;
			}
		}
		mNotSpawnSprite.setPosition(getPosition().x, getPosition().y);

		float levelStartCoord = levelEditor.getRunFromHereLeftPosition();
		float enemyActivationCoord = Float.MIN_VALUE;

		// Enemy has a dedicated trigger
		TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_ACTIVATE);
		if (triggerInfo != null) {
			if (triggerInfo.trigger instanceof TScreenAt) {
				enemyActivationCoord = ((TScreenAt) triggerInfo.trigger).getPosition().x;
			}
		}

		// Enemy will use default trigger
		if (enemyActivationCoord == Float.MIN_VALUE) {
			enemyActivationCoord = calculateDefaultActivateTriggerPosition(levelEditor.getLevel().getSpeed());
		}

		boolean enemyWillSpawn = false;
		if (levelStartCoord <= enemyActivationCoord) {
			enemyWillSpawn = true;
		}

		// Enemy will spawn
		if (!enemyWillSpawn) {
			mNotSpawnSprite.draw(spriteBatch);
		}
	}
}

/**
 * Calculates the activation coordinate depending on the current level speed
 * @param levelSpeed speed of the level
 * @return enemy activation coordinate
 */
public float calculateDefaultActivateTriggerPosition(float levelSpeed) {
	// Calculate position of trigger
	float xCoord = getBoundingBox().getLeft();

	// Decrease position if we are in an enemy group
	if (mGroup != null) {
		int cEnemies = mGroup.getEnemyCount();
		int spawnIndex = mGroup.getEnemySpawnIndex(this);
		float distancePerEnemy = levelSpeed * mGroup.getSpawnTriggerDelay();
		float offset = (cEnemies - spawnIndex) * distancePerEnemy;
		xCoord -= offset;
	}

	return xCoord;
}

/**
 * @return true if the enemy will be stationary. I.e. it's movement type is either stationary or
 * path but doesn't have a path set
 */
public boolean isStationary() {
	return getDef().getMovementType() == MovementTypes.STATIONARY || (getDef().getMovementType() == MovementTypes.PATH && mPath == null);
}

/**
 * @return current path we're following, if we're not following any it returns null
 */
public Path getPath() {
	return mPath;
}

/**
 * Set the path we're following
 * @param path the path we're following
 */
public void setPath(Path path) {
	// Remove enemy from old path
	if (mPath != null) {
		mPath.removeEnemy(this);
	}

	mPath = path;

	if (path != null) {
		mPath.addEnemy(this);
	}
}

/**
 * Sets the speed of the actor, although not the definition, so this is just a temporary speed
 * @param speed new temporary speed.
 */
public void setSpeed(float speed) {
	if (getBody() != null) {
		Vector2 velocity = getBody().getLinearVelocity();
		velocity.nor();
		velocity.scl(speed);
		getBody().setLinearVelocity(velocity);
	}
}

/**
 * Creates the default activate trigger.
 * @param level the current active level
 * @return trigger info that was created
 */
public TriggerInfo createDefaultActivateTrigger(Level level) {
	// Calculate position of trigger
	float xCoord = calculateDefaultActivateTriggerPosition(level.getSpeed());

	// Create the trigger
	TScreenAt trigger = new TScreenAt(level, xCoord);
	trigger.setHidden(true);


	// Create the trigger information
	TriggerInfo triggerInfo = new TriggerInfo();

	triggerInfo.action = Actions.ACTOR_ACTIVATE;
	triggerInfo.delay = 0;
	triggerInfo.listener = this;
	triggerInfo.setTrigger(trigger);

	return triggerInfo;
}

/**
 * Creates the default deactivate trigger. Only AI movement uses this atm
 * @return trigger info that was created
 */
public TriggerInfo createDefaultDeactivateTrigger() {
	TActorActivated trigger = new TActorActivated(this);

	TriggerInfo triggerInfo = new TriggerInfo();
	triggerInfo.action = Actions.ACTOR_DEACTIVATE;
	triggerInfo.delay = Config.Actor.Enemy.DEACTIVATE_TIME_DEFAULT;
	triggerInfo.listener = this;
	triggerInfo.setTrigger(trigger);

	return triggerInfo;
}

/**
 * Creates a new copy of this enemy for an enemy group. I.e. sort of deep copy
 * @return new enemy to be used for the same group
 */
EnemyActor copyForGroup() {
	EnemyActor enemyActor = new EnemyActor();
	enemyActor.setDef(getDef());
	enemyActor.setPosition(getPosition());
	enemyActor.mGroup = mGroup;
	enemyActor.mPath = mPath;
	return enemyActor;
}

@Override
public void setPosition(Vector2 position) {
	super.setPosition(position);

	// Set position of other actors in the group
	if (mGroupLeader && mGroup != null) {
		mGroup.setPosition(position);
	}
}

@Override
public void update(float deltaTime) {
	super.update(deltaTime);

	EnemyActorDef def = getDef();
	if (def != null && def.getMovementType() != null && getBody() != null) {
		if (isActive()) {
			// Update movement
			switch (def.getMovementType()) {
			case PATH:
				if (mPath != null) {
					updatePathMovement(deltaTime);
				}

				if (!mEditorActive) {
					if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
						if (mPath != null) {
							checkPathDeactivate();
						} else {
							checkStationaryDeactivate();
						}
					}
				}
				break;

			case AI:
				updateAiMovement(deltaTime);
				break;

			case STATIONARY:
				if (!mEditorActive) {
					if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
						checkStationaryDeactivate();
					}
				}
				break;
			}

			// Update weapon
			if (def.hasWeapon()) {
				updateWeapon(deltaTime);
			}
		}
		// Not active -> Slow down for AI
		else {
			if (def.getMovementType() == MovementTypes.AI) {
				slowDown(deltaTime);
			}
		}
	}
}

@Override
public EnemyActorDef getDef() {
	return (EnemyActorDef) super.getDef();
}

/**
 *
 */
@Override
public void setDef(ActorDef def) {
	super.setDef(def);

	// Set weapon
	resetWeapon();
}

@Override
public void renderShape(ShapeRendererEx shapeRenderer) {
	// Don't render non-leaders when in editor
	if (mEditorActive) {
		if (mGroup == null || mGroupLeader) {
			super.renderShape(shapeRenderer);
		}
	} else {
		super.renderShape(shapeRenderer);
	}
}

@Override
public void renderEditor(ShapeRendererEx shapeRenderer) {
	if (mGroup == null || mGroupLeader) {
		super.renderEditor(shapeRenderer);

		RenderOrders.offsetZValueEditor(shapeRenderer, this);

		// Draw path to
		// Activate trigger
		shapeRenderer.push(ShapeType.Line);
		TriggerInfo activateTrigger = TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_ACTIVATE);
		if (activateTrigger != null && activateTrigger.trigger instanceof IResourcePosition) {
			shapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.ENEMY_ACTIVATE_TRIGGER_LINE_COLOR));
			shapeRenderer.line(getPosition(), ((IResourcePosition) activateTrigger.trigger).getPosition());
		}

		// Deactivate trigger
		TriggerInfo deactivateTrigger = TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE);
		if (deactivateTrigger != null && deactivateTrigger.trigger instanceof IResourcePosition) {
			shapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.ENEMY_DEACTIVATE_TRIGGER_LINE_COLOR));
			shapeRenderer.line(getPosition(), ((IResourcePosition) deactivateTrigger.trigger).getPosition());
		}

		shapeRenderer.pop();


		RenderOrders.resetZValueOffset(shapeRenderer);
		RenderOrders.resetZValueOffsetEditor(shapeRenderer, this);
	}
}

/**
 * @return enemy filter category
 */
@Override
protected short getFilterCategory() {
	return ActorFilterCategories.ENEMY;
}

/**
 * Can collide only with other players
 * @return colliding categories
 */
@Override
protected short getFilterCollidingCategories() {
	return ActorFilterCategories.PLAYER;
}

@Override
public void preWrite() {
	super.preWrite();

	mClassRevision = CLASS_REVISION;
}

@Override
public void read(Kryo kryo, Input input) {
	super.read(kryo, input);

	EnemyActorDef enemyDef = getDef();
	super.setDef(enemyDef);

	if (mClassRevision == 0) {
		// Weapon
		if (enemyDef.hasWeapon()) {
			mWeapon = kryo.readObject(input, Weapon.class);
			mWeapon.setWeaponDef(enemyDef.getWeaponDef());
			mShootAngle = input.readFloat();
		}

		// Group
		if (mGroup != null) {
			mGroupLeader = input.readBoolean();
		}

		// Movement
		if (enemyDef.getMovementType() == MovementTypes.AI) {
			if (enemyDef.isMovingRandomly()) {
				mRandomMoveNext = input.readFloat(100, true);
				mRandomMoveDirection = kryo.readObject(input, Vector2.class);
			}
		} else if (enemyDef.getMovementType() == MovementTypes.PATH) {
			mPath = kryo.readObjectOrNull(input, Path.class);

			if (mPath != null) {
				mPathIndexNext = input.readInt(false);

				switch (mPath.getPathType()) {
				case ONCE:
					mPathOnceReachedEnd = input.readBoolean();
					break;

				case BACK_AND_FORTH:
					mPathForward = input.readBoolean();
					break;

				case LOOP:
					// Does nothing
					break;
				}
			}
		}
	} else {
		if (mWeapon == null) {
			mWeapon = new Weapon();
		}

		if (mWeapon.getDef() == null || enemyDef.hasWeapon()) {
			mWeapon.setWeaponDef(enemyDef.getWeaponDef());
		}
	}
}

@Override
public void copy(Object fromOriginal) {
	super.copy(fromOriginal);

	EnemyActor fromEnemy = (EnemyActor) fromOriginal;

	mGroupLeader = fromEnemy.mGroupLeader;
	mWeapon = fromEnemy.mWeapon.copy();
	mShootAngle = fromEnemy.mShootAngle;

	mRandomMoveNext = fromEnemy.mRandomMoveNext;
	mRandomMoveDirection.set(fromEnemy.mRandomMoveDirection);

	if (fromEnemy.mPath != null) {
		mPath = fromEnemy.mPath;
		mPathIndexNext = fromEnemy.mPathIndexNext;
		mPathOnceReachedEnd = fromEnemy.mPathOnceReachedEnd;
		mPathForward = fromEnemy.mPathForward;
	}
}

@Override
public void dispose() {
	super.dispose();
}

@Override
public void reloadFixtures() {
	super.reloadFixtures();

	// reloadActivateCircle();
}


@Override
public <ResourceType> ResourceType copyNewResource() {
	ResourceType copy = super.copyNewResource();

	EnemyActor enemyCopy = (EnemyActor) copy;

	// Never make a copy a group leader?
	enemyCopy.mGroupLeader = false;

	enemyCopy.mWeapon = mWeapon.copy();
	enemyCopy.mRandomMoveDirection.set(mRandomMoveDirection);
	enemyCopy.mTargetDirection.set(mTargetDirection);

	return copy;
}

@Override
public void removeBoundResource(IResource boundResource, List<Command> commands) {
	super.removeBoundResource(boundResource, commands);

	if (boundResource == mPath) {
		Command command = new Command() {
			private Path mOldPath = mPath;

			@Override
			public boolean execute() {
				setPath(null);
				return true;
			}

			@Override
			public boolean undo() {
				setPath(mOldPath);
				return true;
			}


		};
		commands.add(command);
	}

	// Group
	if (boundResource == mGroup) {
		Command command = new Command() {
			private boolean mOldGroupLeader = mGroupLeader;
			private EnemyGroup mOldEnemyGroup = mGroup;

			@Override
			public boolean undo() {
				mGroup = mOldEnemyGroup;
				mGroupLeader = mOldGroupLeader;
				return true;
			}

			@Override
			public boolean execute() {
				mGroup = null;
				mGroupLeader = false;
				return true;
			}


		};
		commands.add(command);
	}
}

/**
 * Resets the weapon
 */
public void resetWeapon() {
	mWeapon.setWeaponDef(getDef().getWeaponDef());
	mShootAngle = getDef().getAimStartAngle();
}

/**
 * Resets the movement. This resets the movement to start from the beginning of the path. Only
 * applicable for path movement
 */
public void resetPathMovement() {
	mPathIndexNext = -1;
	mPathForward = true;
	mPathOnceReachedEnd = false;
	if (getBody() != null) {
		getBody().setAngularVelocity(getDef().getRotationSpeedRad());
		getBody().setTransform(getPosition(), getDef().getBodyDef().angle);
	}
}

/**
 * @return the enemy group
 */
public EnemyGroup getEnemyGroup() {
	return mGroup;
}

/**
 * Sets the group of this enemy
 * @param enemyGroup the enemy group, set as null to "remove" it from any group
 */
void setEnemyGroup(EnemyGroup enemyGroup) {
	mGroup = enemyGroup;

	if (mGroup == null) {
		mGroupLeader = false;
	}
}

/**
 * @return true if the enemy is a group leader
 */
public boolean isGroupLeader() {
	return mGroupLeader;
}

/**
 * Sets if the enemy is a group leader or not
 * @param leader set to true to make this a leader
 */
public void setGroupLeader(boolean leader) {
	mGroupLeader = leader;
}

/**
 * Updates the weapon
 * @param deltaTime time elapsed since last frame
 */
private void updateWeapon(float deltaTime) {
	mWeapon.update(deltaTime);

	mWeapon.setPosition(getPosition());

	if (mWeapon.canShoot()) {
		Vector2 shootDirection = getShootDirection();

		mWeapon.shoot(shootDirection);

		// Calculate next shooting angle
		if (getDef(EnemyActorDef.class).getAimType() == AimTypes.ROTATE) {
			mShootAngle += mWeapon.getCooldownTime() * getDef(EnemyActorDef.class).getAimRotateSpeed();
		}
	}
}

/**
 * @return direction which we want to shoot in. Be sure to free this vector using
 * Pools.vector2.free(vector);.
 */
private Vector2 getShootDirection() {
	Vector2 shootDirection = new Vector2();

	switch (getDef(EnemyActorDef.class).getAimType()) {
	case ON_PLAYER:
		shootDirection.set(mPlayerActor.getPosition()).sub(getPosition());
		break;

	case DIRECTION:
		// Shoot in current direction
		shootDirection.set(1, 0);
		shootDirection.rotate(getDef(EnemyActorDef.class).getAimStartAngle());
		break;

	case MOVE_DIRECTION:
		// If we're moving shoot in moving direction
		if (getBody() != null) {
			shootDirection.set(getBody().getLinearVelocity());
		} else {
			shootDirection.set(0, 0);
		}

		// If we're still, shoot in actor's "look" direction
		if (shootDirection.len2() == 0) {
			shootDirection.set(1, 0);

			if (getBody() != null) {
				shootDirection.rotate(MathUtils.radiansToDegrees * getBody().getAngle());
			} else {
				shootDirection.rotate(MathUtils.radiansToDegrees * getDef().getBodyDef().angle);
			}
		}
		break;

	case IN_FRONT_OF_PLAYER: {
		Vector2 playerVelocity = mPlayerActor.getBody().getLinearVelocity();
		// float levelSpeed = 0;
		// if (!mEditorActive) {
		// levelSpeed = mLevel.getSpeed();
		// }
		// playerVelocity.x -= levelSpeed;

		boolean targetPlayerInstead = false;

		// If velocity is standing still, just shoot at the player...
		float playerSpeedSq = playerVelocity.len2();
		if (Maths.floatEquals(playerSpeedSq, 0)) {
			targetPlayerInstead = true;
		}
		// Calculate where the bullet would intersect with the player
		else {
			Vector2 bulletVelocity = Geometry.interceptTarget(getPosition(), mWeapon.getDef().getBulletSpeed(), mPlayerActor.getPosition(),
					playerVelocity);
			shootDirection.set(bulletVelocity);

			// Cannot intercept, target player
			if (shootDirection.x != shootDirection.x || shootDirection.y != shootDirection.y) {
				targetPlayerInstead = true;
			}
		}

		if (targetPlayerInstead) {
			shootDirection.set(mPlayerActor.getPosition()).sub(getPosition());
		}
		break;
	}

	case ROTATE:
		// Shoot in current direction
		shootDirection.set(1, 0);
		shootDirection.rotate(mShootAngle);
		break;
	}

	return shootDirection;
}

/**
 * Follow the path
 * @param deltaTime time elapsed since last frame
 */
private void updatePathMovement(float deltaTime) {
	if (mPath == null || mPath.getCornerCount() < 2 || mPathOnceReachedEnd) {
		return;
	}

	// Special case, not initialized. Set position to first position
	if (mPathIndexNext == -1) {
		mPathIndexNext = 1;
		setPosition(mPath.getCornerPosition(0));

		mTargetDirection.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());

		// Set angle
		if (getDef(EnemyActorDef.class).isTurning()) {
			getBody().setTransform(getPosition(), (float) Math.toRadians(mTargetDirection.angle()));
			getBody().setLinearVelocity(0, 0);
		}

		moveToTarget(mTargetDirection, deltaTime);
	}


	if (hasPassedTarget()) {
		// Special case for ONCE and last index
		// Just continue straight forward, i.e do nothing
		if (mPath.getPathType() == PathTypes.ONCE && mPathIndexNext == mPath.getCornerCount() - 1) {
			mPathOnceReachedEnd = true;
			return;
		}

		calculateNextPathIndex();

		// No turning, change direction directly
		mTargetDirection.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());
		moveToTarget(mTargetDirection, deltaTime);
	} else if (getDef(EnemyActorDef.class).isTurning()) {
		mTargetDirection.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());
		moveToTarget(mTargetDirection, deltaTime);
	}
}

/**
 * Update the AI movement
 * @param deltaTime time elapsed since the last frame
 */
private void updateAiMovement(float deltaTime) {
	// Calculate distance to player
	Vector2 targetDirection = Pools.vector2.obtain();
	targetDirection.set(mPlayerActor.getPosition()).sub(getPosition());
	float targetDistanceSq = targetDirection.len2();

	// Enemy too far from the player?
	if (targetDistanceSq > getDef(EnemyActorDef.class).getPlayerDistanceMaxSq()) {
		moveToTarget(targetDirection, deltaTime);
		resetRandomMove();
	}
	// Enemy too close to player?
	else if (targetDistanceSq < getDef(EnemyActorDef.class).getPlayerDistanceMinSq()) {
		targetDirection.scl(-1);
		moveToTarget(targetDirection, deltaTime);
		resetRandomMove();
	}
	// Enemy in range
	else {
		if (getDef(EnemyActorDef.class).isMovingRandomly()) {
			calculateRandomMove(deltaTime);
		} else {
			if (mEditorActive) {
				getBody().setLinearVelocity(0, 0);
			} else {
				getBody().setLinearVelocity(mLevel.getSpeed(), 0);
			}
			getBody().setAngularVelocity(0);
			resetRandomMove();
		}
	}

	Pools.vector2.free(targetDirection);
}

/**
 * Resets the random movement
 */
private void resetRandomMove() {
	mRandomMoveNext = 0;
}

/**
 * Calculates the random movement of an enemy
 * @param deltaTime time elapsed since last fram
 */
private void calculateRandomMove(float deltaTime) {
	boolean newMove = false;
	if (mRandomMoveNext <= 0) {
		float range = getDef(EnemyActorDef.class).getRandomTimeMax() - getDef(EnemyActorDef.class).getRandomTimeMin();
		mRandomMoveNext = (float) Math.random() * range + getDef(EnemyActorDef.class).getRandomTimeMin();
		newMove = true;
	} else {
		mRandomMoveNext -= deltaTime;
	}

	if (newMove) {
		float angle = (float) Math.random() * 360;
		mRandomMoveDirection.set(1, 0);
		mRandomMoveDirection.setAngle(angle);
		moveToTarget(mRandomMoveDirection, deltaTime);
	} else {
		moveToTarget(mRandomMoveDirection, deltaTime);
	}
}

/**
 * Moves to the target area. Takes into account turning if enabled
 * @param targetDirection the direction we want to move in
 * @param deltaTime time elapsed since last frame
 */
private void moveToTarget(Vector2 targetDirection, float deltaTime) {

	if (getDef(EnemyActorDef.class).isTurning()) {
		moveToTargetTurning(targetDirection, deltaTime);
	} else {
		moveToTargetRegular(targetDirection, deltaTime);
	}
}

/**
 * Moves to target using no turning algorithm
 * @param targetDirection direction we want the enemy to move in
 * @param deltaTime time elapsed since last frame
 */
private void moveToTargetRegular(Vector2 targetDirection, float deltaTime) {
	Vector2 velocity = Pools.vector2.obtain();
	velocity.set(targetDirection);
	velocity.nor().scl(getDef(EnemyActorDef.class).getSpeed());

	// Increase with level speed
	if (!mEditorActive && getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
		float ratio = Math.abs(velocity.x / getDef(EnemyActorDef.class).getSpeed());
		float addSpeed = mLevel.getSpeed() * ratio;
		velocity.x += addSpeed;
	}

	getBody().setLinearVelocity(velocity);
	Pools.vector2.free(velocity);
}

/**
 * Moves to target using turning algorithm
 * @param targetDirection direction we want the enemy to move in
 * @param deltaTime time elapsed since last frame
 */
private void moveToTargetTurning(Vector2 targetDirection, float deltaTime) {
	Vector2 velocity = Pools.vector2.obtain();
	velocity.set(getBody().getLinearVelocity());

	float enemySpeed = getDef().getSpeed();

	// Decrease with level speed
	if (!mEditorActive && getDef().getMovementType() == MovementTypes.AI && !velocity.equals(Vector2.Zero)) {
		// float ratio = Math.abs(velocity.x /
		// getDef(EnemyActorDef.class).getSpeed());
		// float addSpeed = mLevel.getSpeed() * ratio;
		// velocity.x -= addSpeed;
	}

	boolean noVelocity = Maths.approxCompare(velocity.len2(), 0.01f);

	// Calculate angle between the vectors
	boolean counterClockwise = false;
	float bodyAngle = (float) Math.toDegrees(getBody().getAngle()) % 360;
	if (bodyAngle < 0) {
		bodyAngle += 360;
	}
	float velocityAngleOriginal = 0;
	float velocityAngle = 0;
	// We have no speed, use body angle instead
	if (noVelocity) {
		velocityAngleOriginal = bodyAngle;
	} else {
		velocityAngleOriginal = velocity.angle();
	}
	velocityAngle = velocityAngleOriginal;
	if (velocityAngle > 180) {
		counterClockwise = !counterClockwise;
		velocityAngle -= 180;
	}

	float targetAngleOriginal = targetDirection.angle();
	float targetAngle = targetAngleOriginal;
	if (targetAngle > 180) {
		counterClockwise = !counterClockwise;
		targetAngle -= 180;
	}

	float diffAngle = velocityAngle - targetAngle;
	if (diffAngle < 0) {
		counterClockwise = !counterClockwise;
	}

	// Because we only use 0-180 it could be 0 degrees when we should actually
	// head the other direction, take this into account!
	boolean oppositeDirection = false;
	if (velocityAngleOriginal < targetAngleOriginal) {
		if (Maths.approxCompare(velocityAngleOriginal, targetAngleOriginal - 180, Config.Actor.Enemy.TURN_ANGLE_MIN)) {
			oppositeDirection = true;
		}
	} else {
		if (Maths.approxCompare(velocityAngleOriginal - 180, targetAngleOriginal, Config.Actor.Enemy.TURN_ANGLE_MIN)) {
			oppositeDirection = true;
		}
	}

	if (!Maths.approxCompare(diffAngle, Config.Actor.Enemy.TURN_ANGLE_MIN) || oppositeDirection || noVelocity) {
		float angleBefore = velocity.angle();
		float rotation = getDef().getTurnSpeed() * deltaTime * enemySpeed;
		if (!counterClockwise) {
			rotation = -rotation;
		}

		float angleAfter = velocityAngleOriginal + rotation;

		// Check if we turned too much?
		boolean turnedTooMuch = false;
		// If toTarget angle is between the before and after velocity angles
		// We have turned too much
		if (counterClockwise) {
			if (angleBefore < targetAngleOriginal && targetAngleOriginal < angleAfter) {
				turnedTooMuch = true;
			}
		} else {
			if (angleBefore > targetAngleOriginal && targetAngleOriginal > angleAfter) {
				turnedTooMuch = true;
			}
		}


		if (turnedTooMuch) {
			velocity.set(targetDirection);
			velocity.nor().scl(getDef(EnemyActorDef.class).getSpeed());
		} else {
			velocity.y = 0;
			velocity.x = 1;
			velocity.setAngle(angleAfter);
			velocity.scl(getDef(EnemyActorDef.class).getSpeed());
		}

		// Increase with level speed
		if (!mEditorActive && getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
			float ratio = velocity.x / getDef(EnemyActorDef.class).getSpeed();
			float addSpeed = mLevel.getSpeed() * ratio * 0.25f;
			velocity.nor().scl(enemySpeed + addSpeed);
		}

		getBody().setLinearVelocity(velocity);
	}


	getBody().setTransform(getPosition(), (float) Math.toRadians(getBody().getLinearVelocity().angle()));

	Pools.vector2.free(velocity);
}

/**
 * Sets the next index to move to, takes into account what type the path is.
 */
private void calculateNextPathIndex() {
	if (mPathForward) {
		// We were at last, do path specific actions
		if (mPath.getCornerCount() - 1 == mPathIndexNext) {
			switch (mPath.getPathType()) {
			case ONCE:
				// Do nothing...
				break;

			case LOOP:
				mPathIndexNext = 0;
				break;

			case BACK_AND_FORTH:
				mPathIndexNext--;
				mPathForward = false;
			}
		} else {
			mPathIndexNext++;
		}
	}
	// We're going backwards, which must mean BACK_AND_FORTH is set
	else {
		if (mPathIndexNext == 0) {
			mPathIndexNext++;
			mPathForward = true;
		} else {
			mPathIndexNext--;
		}
	}
}

/**
 * @return true if the enemy has passed the target.
 */
private boolean hasPassedTarget() {
	Vector2 diff = Pools.vector2.obtain();
	diff.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());

	boolean hasPassedTarget = false;

	if (getDef(EnemyActorDef.class).isTurning()) {
		float distanceSq = diff.len2();
		hasPassedTarget = distanceSq <= Config.Actor.Enemy.PATH_NODE_CLOSE_SQ;
	} else {
		float diffAngle = diff.angle() - mTargetDirection.angle();
		if (diffAngle > 100 || diffAngle < -100) {
			hasPassedTarget = true;
		}
	}

	Pools.vector2.free(diff);
	return hasPassedTarget;
}

/**
 * Checks if the enemy shall be deactivated and destroyed when following an path. It will destroy
 * the enemy when it never can reach go onto the screen again.
 * @note Will do nothing if the enemy has a deactivate trigger.
 */
private void checkPathDeactivate() {
	if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
		if (getPath().getBoundingBox().getRight() < mLevel.getXCoord() - SceneSwitcher.getWorldWidth()) {

			// For 'once', check that the ship cannot be seen too
			boolean deactivate = false;
			if (getPath().getPathType() == PathTypes.ONCE) {
				Vector2 minPos = SceneSwitcher.getWorldMinCoordinates();
				Vector2 maxPos = SceneSwitcher.getWorldMaxCoordinates();

				// Left
				if (getPosition().x + getDef().getShape().getBoundingRadius() < minPos.x) {
					deactivate = true;
				}
				// Right
				else if (getPosition().x - getDef().getShape().getBoundingRadius() > maxPos.x) {
					deactivate = true;
				}
				// Bottom
				else if (getPosition().y + getDef().getShape().getBoundingRadius() < minPos.y) {
					deactivate = true;
				}
				// Top
				else if (getPosition().y - getDef().getShape().getBoundingRadius() > maxPos.y) {
					deactivate = true;
				}
			} else {
				deactivate = true;
			}

			if (deactivate) {
				deactivate();
				destroyBody();
			}
		}
	}
}

/**
 * Checks if the enemy shall be deactivated and destroyed when it is stationary. It will destroy the
 * enemy once it's outside of the screen.
 * @note Will do nothing if the enemy has a deactivate trigger.
 */
private void checkStationaryDeactivate() {
	if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
		if (getPosition().x + getDef().getShape().getBoundingRadius() < mLevel.getXCoord() - SceneSwitcher.getWorldWidth()) {
			deactivate();
			destroyBody();
		}
	}
}

@Override
public RenderOrders getRenderOrder() {
	return RenderOrders.ENEMY;
}
}