package com.spiddekauga.voider.game;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.voider.sound.SoundPlayer;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Listens to game events to produce sounds
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SoundEffectListener implements IEventListener, Disposable {
	/**
	 * Default constructor
	 */
	public SoundEffectListener() {
		mDispatcher.connect(EventTypes.GAME_ACTOR_HEALTH_CHANGED, this);
		mDispatcher.connect(EventTypes.GAME_ENEMY_EXPLODED, this);
		mDispatcher.connect(EventTypes.GAME_PLAYER_COLLISION_BEGIN, this);
		mDispatcher.connect(EventTypes.GAME_PLAYER_COLLISION_END, this);
		mDispatcher.connect(EventTypes.GAME_PLAYER_HIT_BY_BULLET, this);
		mDispatcher.connect(EventTypes.GAME_PLAYER_SHIP_LOST, this);
	}

	@Override
	public void dispose() {
		mDispatcher.disconnect(EventTypes.GAME_ACTOR_HEALTH_CHANGED, this);
		mDispatcher.disconnect(EventTypes.GAME_ENEMY_EXPLODED, this);
		mDispatcher.disconnect(EventTypes.GAME_PLAYER_COLLISION_BEGIN, this);
		mDispatcher.disconnect(EventTypes.GAME_PLAYER_COLLISION_END, this);
		mDispatcher.disconnect(EventTypes.GAME_PLAYER_HIT_BY_BULLET, this);
		mDispatcher.disconnect(EventTypes.GAME_PLAYER_SHIP_LOST, this);
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	private static EventDispatcher mDispatcher = EventDispatcher.getInstance();
	private static SoundPlayer mSoundPlayer = SoundPlayer.getInstance();
}
