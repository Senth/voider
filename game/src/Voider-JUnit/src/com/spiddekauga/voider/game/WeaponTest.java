package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.utils.Pools;


/**
 * Test WeaponDef
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class WeaponTest {
	/**
	 * Before class
	 */
	@BeforeClass
	public static void beforeClass() {
		Debug.JUNIT_TEST = true;
	}

	/**
	 * Test WeaponDef
	 */
	@Test
	public void testDefWriteRead() {
		Kryo kryo = Pools.kryo.obtain();

		WeaponDef weaponDef = new WeaponDef();
		BulletActorDef bulletActorDef = new BulletActorDef();
		weaponDef.setBulletActorDef(bulletActorDef);
		weaponDef.setCooldownMax(12);
		weaponDef.setCooldownMin(5);
		weaponDef.setBulletSpeed(22);
		weaponDef.setDamage(1337);

		// Regular copy
		WeaponDef copy = kryo.copy(weaponDef);
		testWeaponDefEquals(weaponDef, copy);

		// Test write and read
		copy = KryoPrototypeTest.copy(weaponDef, WeaponDef.class, kryo);
		testWeaponDefEquals(weaponDef, copy);

		Pools.kryo.free(kryo);
	}

	/**
	 * Test Weapon
	 */
	@Test
	public void testInstanceWriteRead() {
		Kryo kryo = Pools.kryo.obtain();

		WeaponDef weaponDef = new WeaponDef();
		Weapon weapon = new Weapon();
		weapon.setWeaponDef(weaponDef);
		weapon.setPosition(new Vector2(1,2));

		// Regular copy
		Weapon copy = weapon.copy();
		testWeaponEquals(weapon, copy);

		Pools.kryo.free(kryo);
	}

	/**
	 * Equal tests for WeaponDef
	 * @param expected expected result
	 * @param actual copied or read WeaponDef
	 */
	public static void testWeaponDefEquals(WeaponDef expected, WeaponDef actual) {
		assertNotSame(expected, actual);
		assertNull(actual.getBulletActorDef());
		assertEquals(expected.getBulletSpeed(), actual.getBulletSpeed(), 0);
		assertEquals(expected.getDamage(), actual.getDamage(), 0);
		assertEquals(expected.getCooldownMin(), actual.getCooldownMin(), 0);
		assertEquals(expected.getCooldownMax(), actual.getCooldownMax(), 0);
	}

	/**
	 * Equal test for Weapon
	 * @param expected expected result
	 * @param actual  copied or read Weapon
	 */
	public static void testWeaponEquals(Weapon expected, Weapon actual) {
		assertNotSame(expected, actual);
		assertSame(expected.getDef(), actual.getDef());
		assertEquals(expected.getCooldownTime(), actual.getCooldownTime(), 0);
		assertEquals(expected.getPosition(), actual.getPosition());
	}
}
