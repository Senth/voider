package com.spiddekauga.voider.game;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.sound.SoundPlayer;
import com.spiddekauga.voider.sound.Sounds;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Listens to game events to produce sounds
 */
public class SoundEffectListener implements IEventListener, Disposable {
private static EventDispatcher mDispatcher = EventDispatcher.getInstance();
private static SoundPlayer mSoundPlayer = SoundPlayer.getInstance();

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
	switch (event.type) {
	case GAME_ACTOR_HEALTH_CHANGED:
		healthChanged(event);
		break;

	case GAME_ENEMY_EXPLODED:
		mSoundPlayer.play(Sounds.ENEMY_EXPLODES);
		break;

	case GAME_PLAYER_COLLISION_BEGIN:
		mSoundPlayer.play(Sounds.SHIP_COLLIDE);
		break;

	case GAME_PLAYER_COLLISION_END:
		mSoundPlayer.stop(Sounds.SHIP_COLLIDE);
		break;

	case GAME_PLAYER_HIT_BY_BULLET:
		mSoundPlayer.play(Sounds.BULLET_HIT_PLAYER);
		break;

	case GAME_PLAYER_SHIP_LOST:
		mSoundPlayer.play(Sounds.SHIP_LOST);
		break;

	default:
		break;
	}
}

/**
 * Handle when actor health was changed
 * @param event
 */
private void healthChanged(GameEvent event) {
	if (event instanceof HealthChangeEvent) {
		HealthChangeEvent healthChangeEvent = (HealthChangeEvent) event;
		if (healthChangeEvent.actor instanceof PlayerActor) {
			healthChanged((PlayerActor) healthChangeEvent.actor, healthChangeEvent.actor.getHealth());
		}
	}
}

/**
 * Handle when the player Health has changed
 * @param player
 * @param healthOld
 */
private void healthChanged(PlayerActor player, float healthOld) {
	float healthLow = ConfigIni.getInstance().sound.effect.getLowHealthPercent();
	float healthCurrent = player.getHealth() / player.getDef().getHealthMax();

	if (healthCurrent <= healthLow) {
		mSoundPlayer.play(Sounds.SHIP_LOW_HEALTH);
	} else {
		mSoundPlayer.stop(Sounds.SHIP_LOW_HEALTH);
	}
}
}
