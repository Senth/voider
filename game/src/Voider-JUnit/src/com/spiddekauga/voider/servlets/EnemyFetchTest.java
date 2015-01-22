package com.spiddekauga.voider.servlets;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethod;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables.SEnemy;

/**
 * Test class
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class EnemyFetchTest {
	@BeforeClass
	public static void BeforeClass() throws NoSuchFieldException, SecurityException, NoSuchMethodException {
		mfParameters = EnemyFetch.class.getDeclaredField("mParameters");
		mfParameters.setAccessible(true);
		mmBuildSearchString = EnemyFetch.class.getDeclaredMethod("buildSearchString");
		mmBuildSearchString.setAccessible(true);
	}

	@Before
	public void before() throws IllegalAccessException {
		mfParameters.set(mEnemyFetch, mParameters);
	}

	@Test
	public void testBuildSearchString() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// NULL
		String search = buildSearchString();
		Assert.assertEquals("", search);


		// Search
		// Too short search string
		mParameters.searchString = "12";
		search = buildSearchString();
		Assert.assertEquals("", search);

		// OK length
		mParameters.searchString = "123";
		search = buildSearchString();
		Assert.assertEquals(mParameters.searchString, search);

		mParameters.searchString = null;


		// Movement type
		String field = SEnemy.MOVEMENT_TYPE;
		mParameters.movementTypes.add(MovementTypes.AI);
		search = buildSearchString();
		Assert.assertEquals(field + ":" + MovementTypes.AI.toSearchId(), search);

		// Multiple
		mParameters.movementTypes.add(MovementTypes.STATIONARY);
		search = buildSearchString();
		Assert.assertEquals("(" + field + ":" + MovementTypes.AI.toSearchId() + " OR " + field + ":" + MovementTypes.STATIONARY.toSearchId() + ")",
				search);

		// All (nothing)
		mParameters.movementTypes.add(MovementTypes.PATH);
		search = buildSearchString();
		Assert.assertEquals("", search);

		mParameters.movementTypes.clear();


		// Has weapon
		// True
		field = SEnemy.HAS_WEAPON;
		mParameters.hasWeapon = true;
		search = buildSearchString();
		Assert.assertEquals(field + ":1", search);

		// False
		mParameters.hasWeapon = false;
		search = buildSearchString();
		Assert.assertEquals(field + ":0", search);


		// Multiple things
		mParameters.searchString = "123";
		mParameters.hasWeapon = true;
		mParameters.bulletSpeedRanges.add(BulletSpeedSearchRanges.SLOW);
		mParameters.bulletSpeedRanges.add(BulletSpeedSearchRanges.FASTEST);

		search = buildSearchString();
		String expected = mParameters.searchString + " " + SEnemy.HAS_WEAPON + ":1" + " (" + SEnemy.BULLET_SPEED_CAT + ":"
				+ BulletSpeedSearchRanges.SLOW.toSearchId() + " OR " + SEnemy.BULLET_SPEED_CAT + ":" + BulletSpeedSearchRanges.FASTEST.toSearchId()
				+ ")";
		Assert.assertEquals(expected, search);
	}

	private String buildSearchString() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (String) mmBuildSearchString.invoke(mEnemyFetch);
	}

	private EnemyFetch mEnemyFetch = new EnemyFetch();
	private EnemyFetchMethod mParameters = new EnemyFetchMethod();
	private static Field mfParameters = null;
	private static Method mmBuildSearchString = null;
}
