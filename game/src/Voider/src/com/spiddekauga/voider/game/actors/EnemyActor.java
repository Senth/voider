package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;

/**
 * An enemy actor, these can generally shoot on the player.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyActor extends Actor {
	/**
	 * Default constructor
	 */
	public EnemyActor() {
		super(new EnemyActorDef());
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);

		// Update movement
		EnemyActorDef def = getDef(EnemyActorDef.class);
		if (def != null && def.getMovementType() != null && getBody() != null) {
			switch (def.getMovementType()) {
			case PATH:
				updatePathMovement(deltaTime);
				break;

			case AI:
				updateAiMovement();
				break;

			case STATIONARY:
				// Does nothing
				break;
			}
		}
	}

	/**
	 * Set the path we're following
	 * @param path the path we're following
	 */
	public void setPath(Path path) {
		mPath = path;
		resetPathMovement();
	}

	/**
	 * @return current path we're following, if we're not following any it returns null
	 */
	public Path getPath() {
		return mPath;
	}

	/**
	 * Sets the speed of the actor, although not the definition, so this is
	 * just a temporary speed
	 * @param speed new temporary speed.
	 */
	public void setSpeed(float speed) {
		if (getBody() != null) {
			Vector2 velocity = getBody().getLinearVelocity();
			velocity.nor();
			velocity.mul(speed);
			getBody().setLinearVelocity(velocity);
		}
	}

	/**
	 * Resets the movement. This resets the movement to start from the beginning of the path.
	 * Only applicable for path movement
	 */
	public void resetPathMovement() {
		mPathIndexNext = -1;
		mPathForward = true;
		mPathOnceReachedEnd = false;
	}

	/**
	 * Follow the path
	 * @param deltaTime time elapsed since last frame
	 */
	private void updatePathMovement(float deltaTime) {
		if (mPath == null || mPath.getNodeCount() < 2 || mPathOnceReachedEnd) {
			return;
		}

		// Special case, not initialized. Set position to first position
		if (mPathIndexNext == -1) {
			mPathIndexNext = 1;
			setPosition(mPath.getNodeAt(0));

			Vector2 velocity = Pools.obtain(Vector2.class);
			velocity.set(mPath.getNodeAt(mPathIndexNext)).sub(getPosition());
			velocity.nor().mul(getDef(EnemyActorDef.class).getSpeed());
			getBody().setLinearVelocity(velocity);
			Pools.free(velocity);
		}


		if (isCloseToNextIndex()) {
			// Special case for ONCE and last index
			// Just continue straight forward, i.e do nothing
			if (mPath.getPathType() == PathTypes.ONCE && mPathIndexNext == mPath.getNodeCount() - 1) {
				mPathOnceReachedEnd = true;
				return;
			}

			calculateNextPathIndex();

			// No turning, change direction directly
			if (!getDef(EnemyActorDef.class).isTurning()) {
				Vector2 velocity = Pools.obtain(Vector2.class);
				velocity.set(mPath.getNodeAt(mPathIndexNext)).sub(getPosition());
				velocity.nor().mul(getDef(EnemyActorDef.class).getSpeed());
				getBody().setLinearVelocity(velocity);
				Pools.free(velocity);
			}
		}


		if (getDef(EnemyActorDef.class).isTurning()) {
			Vector2 velocity = Pools.obtain(Vector2.class);
			velocity.set(getBody().getLinearVelocity());

			Vector2 toTarget = Pools.obtain(Vector2.class);
			toTarget.set(mPath.getNodeAt(mPathIndexNext)).sub(getPosition());

			// Calculate angle between the vectors
			boolean clockwise = false;
			float velocityAngle = velocity.angle();
			if (velocityAngle > 180) {
				clockwise = !clockwise;
				velocityAngle -= 180;
			}
			float toTargetAngle = toTarget.angle();
			if (toTargetAngle > 180) {
				clockwise = !clockwise;
				toTargetAngle -= 180;
			}

			float diffAngle = velocityAngle - toTargetAngle;
			if (diffAngle >= Config.Actor.Enemy.TURN_ANGLE_MIN || diffAngle <= -Config.Actor.Enemy.TURN_ANGLE_MIN) {
				if (diffAngle < 0) {
					clockwise = !clockwise;
				}

				float rotation = getDef(EnemyActorDef.class).getTurnSpeed() * deltaTime * getDef(EnemyActorDef.class).getSpeed();
				if (!clockwise) {
					rotation = -rotation;
				}
				boolean angleWrapped = velocity.angle() + rotation > 360 || velocity.angle() + rotation < 0;
				velocity.rotate(rotation);

				// Check if we turned too much?
				float newVelocityAngle = velocity.angle();
				if (newVelocityAngle > 180) {
					newVelocityAngle -= 180;
				}
				float newDiffAngle = newVelocityAngle - toTargetAngle;
				boolean turnedTooMuch = false;
				if (angleWrapped) {
					if ((diffAngle > 0 && newDiffAngle > 0) || (diffAngle < 0 && newDiffAngle < 0)) {
						turnedTooMuch = true;
					}
				} else {
					if ((diffAngle > 0 && newDiffAngle < 0) || (diffAngle < 0 && newDiffAngle > 0)) {
						turnedTooMuch = true;
					}
				}

				if (turnedTooMuch) {
					//					velocity.set(toTarget);
					//					velocity.nor().mul(getDef(EnemyActorDef.class).getSpeed());
				}

				getBody().setLinearVelocity(velocity);
			}

			Pools.free(velocity);
			Pools.free(toTarget);
		}
	}

	/**
	 * Update the AI movement
	 */
	private void updateAiMovement() {
		/** @TODO implement AI movement */
	}

	/**
	 * Sets the next index to move to, takes into account what type
	 * the path is.
	 */
	private void calculateNextPathIndex() {
		if (mPathForward) {
			// We were at last, do path specific actions
			if (mPath.getNodeCount() - 1 == mPathIndexNext) {
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
	 * @return true if the enemy is close to the next path index
	 */
	private boolean isCloseToNextIndex() {
		Vector2 diff = Pools.obtain(Vector2.class);
		diff.set(mPath.getNodeAt(mPathIndexNext)).sub(getPosition());
		float distanceSq = diff.len2();
		Pools.free(diff);
		return distanceSq <= Config.Actor.Enemy.PATH_NODE_CLOSE_SQ;
	}

	// PATH MOVEMENT
	/** Path we're currently following */
	private Path mPath = null;
	/** Index of path we're heading to */
	private int mPathIndexNext = -1;
	/** If the enemy is moving in the path direction */
	private boolean mPathForward = true;
	/** ONCE path reach end */
	private boolean mPathOnceReachedEnd = false;
}
