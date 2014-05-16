package com.spiddekauga.voider.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.RevisionEntity;

/**
 * Test for resource sqlite database
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceSqliteGatewayTest {
	/**
	 * Initializes the database
	 */
	@BeforeClass
	public static void beforeClass() {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Gdx.app = new ApplicationStub();
		Config.Debug.JUNIT_TEST = true;

		mGateway = new ResourceSqliteGateway();
	}

	/**
	 * New database before each test
	 */
	@Before
	public void before() {
		SqliteResetter.reset();
	}

	/**
	 * Remove the database
	 */
	@AfterClass
	public static void afterClass() {
		Gdx.app.exit();
	}

	/**
	 * Test to add a resource (and simple remove all)
	 */
	@Test
	public void testAdd() {
		UUID first = UUID.randomUUID();
		UUID second = UUID.randomUUID();

		mGateway.add(first, 0);
		mGateway.add(second, 0);

		assertTrue(mGateway.exists(first));
		assertTrue(mGateway.exists(second));
		assertFalse(mGateway.exists(UUID.randomUUID()));

		assertEquals(2, mGateway.getCount(0));

		mGateway.removeAll(0);

		assertEquals(0, mGateway.getCount(0));
	}

	/**
	 * Test to add revision
	 */
	@Test
	public void testAddRevision() {
		// First
		UUID first = UUID.randomUUID();
		int cFirstRevisions = 5;
		Date[] firstDates = new Date[cFirstRevisions];
		int firstInitial = 1;

		createRevisions(first, firstInitial, cFirstRevisions, firstDates);
		equalsRevisions(first, firstInitial, cFirstRevisions, firstDates);


		// Second
		UUID second = UUID.randomUUID();
		int cSecondRevisions = 7;
		Date[] secondDates = new Date[cSecondRevisions];
		int secondInitial = 0;

		createRevisions(second, secondInitial, cSecondRevisions, secondDates);
		equalsRevisions(second, secondInitial, cSecondRevisions, secondDates);
		equalsRevisions(first, firstInitial, cFirstRevisions, firstDates);


		// Non-existent
		assertNull(mGateway.getRevisionLatest(UUID.randomUUID()));
		assertEquals(0, mGateway.getRevisions(UUID.randomUUID()).size());


		// Test to remove these
		mGateway.removeRevisions(first);
		assertEquals(0, mGateway.getRevisions(first).size());
		assertEquals(cSecondRevisions, mGateway.getRevisions(second).size());

		mGateway.removeRevisions(second);
		assertEquals(0, mGateway.getRevisions(second).size());
	}

	/**
	 * Test add revisions without a date
	 */
	@Test
	public void testAddRevisionWithoutDate() {
		UUID uuid = UUID.randomUUID();

		int cRevisions = 5;
		for (int i = 0; i < cRevisions; ++i) {
			mGateway.addRevision(uuid, i, null);
		}

		ArrayList<RevisionEntity> revisions = mGateway.getRevisions(uuid);
		assertEquals(cRevisions, revisions.size());

		for (int i = 0; i < cRevisions; ++i) {
			assertEquals(i, revisions.get(i).revision);
		}
	}

	/**
	 * Create revisions
	 * @param uuid UUID of resource
	 * @param initial revision value
	 * @param cRevisions number of revisions
	 * @param dates all the dates for the revision, should be of same size as cRevisions
	 */
	private void createRevisions(UUID uuid, int initial, int cRevisions, Date[] dates) {
		Random random = new Random();

		for (int i = initial; i < cRevisions + initial; ++i) {
			Date date = new Date(random.nextLong());
			dates[i - initial] = date;

			mGateway.addRevision(uuid, i, date);
		}
	}

	/**
	 * Tests the revision
	 * @param uuid the UUID of the resource
	 * @param initial revision value
	 * @param cRevisions number of revisions
	 * @param dates all the dates for the revision
	 */
	private void equalsRevisions(UUID uuid, int initial, int cRevisions, Date[] dates) {
		assertEquals("Latest revision", initial + cRevisions - 1, mGateway.getRevisionLatest(uuid).revision);

		ArrayList<RevisionEntity> revisionInfos = mGateway.getRevisions(uuid);
		assertEquals("# Revisions", cRevisions, revisionInfos.size());

		for (int i = 0; i < cRevisions; ++i) {
			RevisionEntity currentInfo = revisionInfos.get(i);

			assertEquals(i + initial, currentInfo.revision);
			assertEquals(dates[i], currentInfo.date);
		}
	}

	/**
	 * Test to remove specific revisions
	 */
	@Test
	public void testRemove() {
		UUID first = UUID.randomUUID();
		UUID second = UUID.randomUUID();
		UUID third = UUID.randomUUID();

		mGateway.add(first, 1);
		mGateway.add(second, 2);
		mGateway.add(third, 3);

		assertEquals(0, mGateway.getCount(0));
		assertEquals(1, mGateway.getCount(1));
		assertEquals(1, mGateway.getCount(2));
		assertEquals(1, mGateway.getCount(3));
		assertEquals(0, mGateway.getCount(4));


		mGateway.remove(first);

		assertEquals(0, mGateway.getCount(0));
		assertEquals(0, mGateway.getCount(1));
		assertEquals(1, mGateway.getCount(2));
		assertEquals(1, mGateway.getCount(3));
		assertEquals(0, mGateway.getCount(4));

		mGateway.removeAll(3);

		assertEquals(0, mGateway.getCount(0));
		assertEquals(0, mGateway.getCount(1));
		assertEquals(1, mGateway.getCount(2));
		assertEquals(0, mGateway.getCount(3));
		assertEquals(0, mGateway.getCount(4));

		mGateway.remove(second);

		assertEquals(0, mGateway.getCount(0));
		assertEquals(0, mGateway.getCount(1));
		assertEquals(0, mGateway.getCount(2));
		assertEquals(0, mGateway.getCount(3));
		assertEquals(0, mGateway.getCount(4));

		mGateway.remove(first);

		assertEquals(0, mGateway.getCount(0));
		assertEquals(0, mGateway.getCount(1));
		assertEquals(0, mGateway.getCount(2));
		assertEquals(0, mGateway.getCount(3));
		assertEquals(0, mGateway.getCount(4));
	}

	/**
	 * Test get all
	 */
	@Test
	public void testGetAll() {
		int cResources = 5;
		UUID[] resources = new UUID[cResources];
		for (int i = 0; i < cResources; ++i) {
			resources[i] = UUID.randomUUID();
			mGateway.add(resources[i], 0);
		}

		mGateway.add(UUID.randomUUID(), 1);

		ArrayList<UUID> gottenResources = mGateway.getAll(0);
		assertEquals(cResources, gottenResources.size());

		for (int i = 0; i < cResources; ++i) {
			assertEquals(resources[i], gottenResources.get(i));
		}
	}

	/** The database */
	private static ResourceSqliteGateway mGateway;
}
