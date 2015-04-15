package com.spiddekauga.voider.game.actors;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * The ship the player controls
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PlayerActor extends com.spiddekauga.voider.game.actors.Actor {
	/**
	 * Creates a default player
	 */
	public PlayerActor() {
		this(new PlayerActorDef());
	}

	/**
	 * Creates a player from the specified actor definition
	 * @param playerActorDef player actor definition to create the player from
	 */
	public PlayerActor(PlayerActorDef playerActorDef) {
		super(playerActorDef);
		setDef(playerActorDef);
	}

	/**
	 * Adds a collectible to the player
	 * @param collectible the collectible to add to the player
	 */
	public void addCollectible(Collectibles collectible) {
		switch (collectible) {
		case HEALTH_25:
			increaseHealth(25);
			break;

		case HEALTH_50:
			increaseHealth(50);
			break;
		}
	}

	@Override
	public void resetHealth() {
		super.resetHealth();
		makeInvulnerable();
	}

	/**
	 * Make the player invulnerable
	 */
	private void makeInvulnerable() {
		mInvulnerableTimeLeft = ConfigIni.getInstance().game.getInvulnerableTimeOnShipLost();

		reloadFixtures();
	}

	/**
	 * Make the player ship vulnerable
	 */
	private void makeVulnerable() {
		mInvulnerableTimeLeft = 0;

		reloadFixtures();
	}

	/**
	 * @return true if the player is invulnerable
	 */
	public boolean isInvulnerable() {
		return mInvulnerableTimeLeft > 0;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);

		if (mInvulnerableTimeLeft > 0) {
			mInvulnerableTimeLeft -= deltaTime;

			if (mInvulnerableTimeLeft <= 0) {
				makeVulnerable();
			}
		}
	}

	/**
	 * @return player filter category
	 */
	@Override
	protected short getFilterCategory() {
		if (isInvulnerable()) {
			return ActorFilterCategories.PLAYER;
		} else {
			return ActorFilterCategories.PLAYER;
		}
	}

	/**
	 * Can collide with everything except player and player bullets
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		if (isInvulnerable()) {
			return ActorFilterCategories.SCREEN_BORDER;
		} else {
			return (short) (ActorFilterCategories.ENEMY | ActorFilterCategories.PICKUP | ActorFilterCategories.STATIC_TERRAIN | ActorFilterCategories.SCREEN_BORDER);
		}
	}

	@Override
	public RenderOrders getRenderOrder() {
		return RenderOrders.PLAYER;
	}

	@Override
	public void addCollidingActor(Actor actor) {
		if (getCollidingActors().isEmpty()) {
			mEventDispatcher.fire(new GameEvent(EventTypes.GAME_PLAYER_COLLISION_BEGIN));
		}

		super.addCollidingActor(actor);
	}

	@Override
	public void removeCollidingActor(Actor actor) {
		super.removeCollidingActor(actor);

		if (getCollidingActors().isEmpty()) {
			mEventDispatcher.fire(new GameEvent(EventTypes.GAME_PLAYER_COLLISION_END));
		}
	}

	@Tag(156) private float mInvulnerableTimeLeft = 0;
}
