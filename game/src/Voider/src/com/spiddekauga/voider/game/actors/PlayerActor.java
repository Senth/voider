package com.spiddekauga.voider.game.actors;

import com.spiddekauga.voider.game.Collectibles;




/**
 * The ship the player controls
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActor extends com.spiddekauga.voider.game.Actor {
	/**
	 * Creates a default player
	 */
	public PlayerActor() {
		super(new PlayerActorDef());
	}

	/**
	 * Adds a collectible to the player
	 * @param collectible the collectible to add to the player
	 */
	public void addCollectible(Collectibles collectible) {
		switch (collectible) {
		case HEALTH_25:
			mLife += 25;
			break;

		case HEALTH_50:
			mLife += 50;
			break;
		}
	}

	//	@Override
	//	public void update(float deltaTime) {
	//		super.update(deltaTime);
	//
	//		// Append position
	//		TimePos timePos = mTimePosPool.obtain();
	//		timePos.position.set(getPosition());
	//		timePos.time = SceneSwitcher.getGameTime().getTotalTimeElapsed();
	//		mPositionRecent.addLast(timePos);
	//
	//
	//		// Remove old position
	//		while (!mPositionRecent.isEmpty() && mPositionRecent.getFirst().time + Config.Actor.Player.RECENT_POS_SAVE_TIME < timePos.time) {
	//			TimePos oldTimePos = mPositionRecent.removeFirst();
	//			mTimePosPool.free(oldTimePos);
	//		}
	//	}
	//
	//	/**
	//	 * Velocity of the player. This is determined by using a diff vector of the oldest
	//	 * and newest positions the player had, time between these are set in
	//	 * #Config.Actor.Player.RECENT_POS_SAVE_TIME
	//	 * @return velocity of the player. Please free this vector using Pools.free(velocity);
	//	 */
	//	public Vector2 getVelocity() {
	//		Vector2 velocity = Pools.obtain(Vector2.class);
	//	}

	/**
	 * @return player filter category
	 */
	@Override
	protected short getFilterCategory() {
		return ActorFilterCategories.PLAYER;
	}

	/**
	 * Can collide with everything except player and player bullets
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return (short) (ActorFilterCategories.ENEMY | ActorFilterCategories.PICKUP | ActorFilterCategories.STATIC_TERRAIN | ActorFilterCategories.SCREEN_BORDER);
	}

	//	/** Last known position, used to calculate direction */
	//	private LinkedList<TimePos> mPositionRecent = new LinkedList<TimePos>();
	//
	//	/** Poolable object */
	//	private static Pool<TimePos> mTimePosPool = Pools.get(TimePos.class);
}
