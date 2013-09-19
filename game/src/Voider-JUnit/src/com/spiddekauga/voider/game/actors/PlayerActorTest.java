package com.spiddekauga.voider.game.actors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceSaver;

/**
 * Tests the player actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActorTest extends ActorTest {
	/**
	 * Initialize tests
	 */
	@BeforeClass
	public static void beforeClass() {
		ActorTest.beforeClass();
	}

	/**
	 * Dispose
	 */
	@AfterClass
	public static void afterClass() {
		ActorTest.afterClass();
	}

	@Override
	@Test
	public void testActorDefWriteRead() {
		PlayerActorDef playerDef = new PlayerActorDef();
		PlayerActorDef copyPlayerDef = KryoPrototypeTest.copy(playerDef, PlayerActorDef.class, mKryo);

		// Nothing to test atm

		playerDef.dispose();
		copyPlayerDef.dispose();
	}

	@Override
	@Test
	public void testActorWriteRead() {
		PlayerActor player = new PlayerActor();
		PlayerActorDef playerDef = new PlayerActorDef();
		player.setDef(playerDef);

		ResourceSaver.save(playerDef);
		ResourceCacheFacade.load(mScene, playerDef.getId(), PlayerActorDef.class, playerDef.getRevision(), false);
		ResourceCacheFacade.finishLoading();

		PlayerActor copyPlayer = KryoPrototypeTest.copy(player, PlayerActor.class, mKryo);

		player.dispose();
		copyPlayer.dispose();
		playerDef.dispose();
	}
}
