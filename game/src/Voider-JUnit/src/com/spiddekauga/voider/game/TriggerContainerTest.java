package com.spiddekauga.voider.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.spiddekauga.utils.Json;

/**
 * Test for TriggerContainer class
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerContainerTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mfListenerTriggers = TriggerContainer.class.getDeclaredField("mListenerTriggers");
		mfListenerTriggers.setAccessible(true);
		mfTriggerListeners = TriggerContainer.class.getDeclaredField("mTriggerListeners");
		mfTriggerListeners.setAccessible(true);
		mfTriggers = TriggerContainer.class.getDeclaredField("mTriggers");
		mfTriggers.setAccessible(true);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Create a new TriggerContainer to test on
	 */
	@Before
	public void setUp() {
		mTriggerContainer = new TriggerContainer();
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.TriggerContainer#removeUnusedTriggers()}.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void removeUnusedTriggers() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);

		ITriggerListener listener1 = new TestListener();
		ITriggerListener listener2 = new TestListener();
		ITriggerListener listener3 = new TestListener();

		// Create listener info
		TriggerListenerInfo listenerInfo10 = new TriggerListenerInfo();
		listenerInfo10.action = "listener1 first action";
		listenerInfo10.listener = listener1;
		listenerInfo10.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo2 = new TriggerListenerInfo();
		listenerInfo2.listener = listener2;
		listenerInfo2.listenerId = listener2.getId();

		TriggerListenerInfo listenerInfo3 = new TriggerListenerInfo();
		listenerInfo3.listener = listener3;
		listenerInfo3.listenerId = listener3.getId();


		// Trigger 1
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo10);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo2);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo3);
		mTriggerContainer.removeListener(trigger1.getId(), listener1.getId());


		mTriggerContainer.removeUnusedTriggers();

		assertEquals("Number of triggers in trigger list", 1, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 1, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);
	}

	/**
	 * Tests to remove triggers after having bound it to a listener
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void removeTriggerAfterListener() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);

		ITriggerListener listener1 = new TestListener();
		ITriggerListener listener2 = new TestListener();
		ITriggerListener listener3 = new TestListener();

		// Create listener info
		TriggerListenerInfo listenerInfo10 = new TriggerListenerInfo();
		listenerInfo10.action = "listener1 first action";
		listenerInfo10.listener = listener1;
		listenerInfo10.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo2 = new TriggerListenerInfo();
		listenerInfo2.listener = listener2;
		listenerInfo2.listenerId = listener2.getId();

		TriggerListenerInfo listenerInfo3 = new TriggerListenerInfo();
		listenerInfo3.listener = listener3;
		listenerInfo3.listenerId = listener3.getId();

		mTriggerContainer.addListener(trigger1.getId(), listenerInfo10);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo2);
		mTriggerContainer.addListener(trigger2.getId(), listenerInfo3);

		ObjectMap<UUID, Array<UUID>> listenerTriggers = ((ObjectMap<UUID, Array<UUID>>) mfListenerTriggers.get(mTriggerContainer));
		assertEquals("Triggers in listener", 1, listenerTriggers.get(listener1.getId()).size);
		assertEquals("Triggers in listener", 1, listenerTriggers.get(listener2.getId()).size);
		assertEquals("Triggers in listener", 1, listenerTriggers.get(listener3.getId()).size);

		mTriggerContainer.removeTrigger(trigger1);

		listenerTriggers = ((ObjectMap<UUID, Array<UUID>>) mfListenerTriggers.get(mTriggerContainer));
		assertNull("Triggers in listener", listenerTriggers.get(listener1.getId()));
		assertNull("Triggers in listener", listenerTriggers.get(listener2.getId()));
		assertEquals("Triggers in listener", 1, listenerTriggers.get(listener3.getId()).size);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.TriggerContainer#addTrigger(com.spiddekauga.voider.game.Trigger)}.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void addTrigger() throws IllegalArgumentException, IllegalAccessException {
		// Add some triggers
		mTriggerContainer.addTrigger(new TestTrigger());
		mTriggerContainer.addTrigger(new TestTrigger());
		mTriggerContainer.addTrigger(new TestTrigger());

		assertEquals("Number of triggers in trigger list", 3, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 3, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);

		// Test add same trigger twice should generate an error message
		try {
			Trigger trigger = new TestTrigger();
			mTriggerContainer.addTrigger(trigger);
			mTriggerContainer.addTrigger(trigger);
			fail("Added trigger twice did not generate error message");
		} catch (NullPointerException e) {
			assertTrue("Added trigger twice generated an error message", true);
		}

		assertEquals("Number of triggers in trigger list", 4, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 4, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.TriggerContainer#removeTrigger(com.spiddekauga.voider.game.Trigger)}.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @note depends on addTrigger() to succeed
	 */
	@Test
	public void removeTrigger() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);
		assertEquals("Number of triggers in trigger list", 3, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 3, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);

		mTriggerContainer.removeTrigger(trigger1);
		assertEquals("Number of triggers in trigger list", 2, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 2, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);

		mTriggerContainer.removeTrigger(trigger2);
		assertEquals("Number of triggers in trigger list", 1, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 1, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);

		// Test to remove same twice
		try {
			mTriggerContainer.removeTrigger(trigger2);
			fail("Removing twice didn't generate an error");
		} catch (NullPointerException e) {
			assertTrue("Removing twice generated an error", true);
		}

		mTriggerContainer.removeTrigger(trigger3);
		assertEquals("Number of triggers in trigger list", 0, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 0, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);
	}

	/**
	 * Test to both add and remove triggers multiple times
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @note depends on both addTrigger() and removeTrigger() to succeed
	 */
	@Test
	public void addRemoveTrigger() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);
		assertEquals("Number of triggers in trigger list", 3, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 3, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);

		mTriggerContainer.removeTrigger(trigger1);
		assertEquals("Number of triggers in trigger list", 2, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 2, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);

		mTriggerContainer.addTrigger(trigger1);
		assertEquals("Number of triggers in trigger list", 3, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 3, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);

		mTriggerContainer.removeTrigger(trigger1);
		assertEquals("Number of triggers in trigger list", 2, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 2, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);

		mTriggerContainer.removeTrigger(trigger2);
		assertEquals("Number of triggers in trigger list", 1, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		assertEquals("Number of triggers in trigger listeners", 1, ((ObjectMap<?, ?>) mfTriggerListeners.get(mTriggerContainer)).size);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.TriggerContainer#addListener(java.util.UUID, com.spiddekauga.voider.game.TriggerListenerInfo)}.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void addListener() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);

		ITriggerListener listener1 = new TestListener();
		ITriggerListener listener2 = new TestListener();
		ITriggerListener listener3 = new TestListener();

		// Create listener info
		TriggerListenerInfo listenerInfo10 = new TriggerListenerInfo();
		listenerInfo10.action = "listener1 first action";
		listenerInfo10.listener = listener1;
		listenerInfo10.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo11 = new TriggerListenerInfo();
		listenerInfo11.action = "second action";
		listenerInfo11.listener = listener1;
		listenerInfo11.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo2 = new TriggerListenerInfo();
		listenerInfo2.listener = listener2;
		listenerInfo2.listenerId = listener2.getId();

		TriggerListenerInfo listenerInfo3 = new TriggerListenerInfo();
		listenerInfo3.listener = listener3;
		listenerInfo3.listenerId = listener3.getId();

		TriggerListenerInfo listenerInfoInvalid = new TriggerListenerInfo();
		listenerInfoInvalid.listenerId = UUID.randomUUID();


		// -- TESTS START HERE --
		// Trigger 1
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo10);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo2);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo3);

		assertEquals("Added three listeners", 3, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		Array<TriggerListenerInfo> listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should conhain some listener", 3, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should be empty", 0, listeners.size);

		// Trigger 2
		mTriggerContainer.addListener(trigger2.getId(), listenerInfo11);
		mTriggerContainer.addListener(trigger2.getId(), listenerInfo3);
		assertEquals("Added another listener info, but still same listenerId", 3, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should conhain some listeners", 3, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should contain some listeners", 2, listeners.size);
		Array<UUID> triggers = ((ObjectMap<UUID, Array<UUID>>)mfListenerTriggers.get(mTriggerContainer)).get(listener1.getId());
		assertEquals("Listener 1 should contain some triggers", 2, triggers.size);


		// Add an invalid listener
		try {
			mTriggerContainer.addListener(trigger3.getId(), listenerInfoInvalid);
			fail("Added invalid listener, error message wasn't sent");
		} catch (NullPointerException e) {
			assertTrue("Added invalid listener, error message was sent", true);
		}

		assertEquals("Added invalid listener info, still same number of listeners", 3, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should contain some listeners", 3, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should contain some listeners", 2, listeners.size);


		// Add a listener to a non-existing trigger
		try {
			mTriggerContainer.addListener(UUID.randomUUID(), listenerInfo10);
			fail("Added listener to invalid trigger, error message wasn't sent");
		} catch (NullPointerException e) {
			assertTrue("Added listener to invalid trigger, error message was sent", true);
		}

		assertEquals("Added listener info to invalid trigger, still same number of listeners", 3, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should contain some listeners", 3, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should contain some listeners", 2, listeners.size);
	}

	/**
	 * Tests to remove listener for all triggers
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void removeListenerAll() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);

		ITriggerListener listener1 = new TestListener();
		ITriggerListener listener2 = new TestListener();
		ITriggerListener listener3 = new TestListener();

		// Create listener info
		TriggerListenerInfo listenerInfo10 = new TriggerListenerInfo();
		listenerInfo10.action = "listener1 first action";
		listenerInfo10.listener = listener1;
		listenerInfo10.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo11 = new TriggerListenerInfo();
		listenerInfo11.action = "second action";
		listenerInfo11.listener = listener1;
		listenerInfo11.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo2 = new TriggerListenerInfo();
		listenerInfo2.listener = listener2;
		listenerInfo2.listenerId = listener2.getId();

		TriggerListenerInfo listenerInfo3 = new TriggerListenerInfo();
		listenerInfo3.listener = listener3;
		listenerInfo3.listenerId = listener3.getId();


		// Trigger 1
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo10);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo2);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo3);

		// Trigger 2
		mTriggerContainer.addListener(trigger2.getId(), listenerInfo11);
		mTriggerContainer.addListener(trigger2.getId(), listenerInfo3);

		mTriggerContainer.removeListener(listener1.getId());
		assertEquals("Removed listener 1", 2, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		Array<TriggerListenerInfo> listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should contain some listeners", 2, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should contain some listeners", 1, listeners.size);
		Array<UUID> triggers = ((ObjectMap<UUID, Array<UUID>>)mfListenerTriggers.get(mTriggerContainer)).get(listener1.getId());
		assertNull("Listener 1 should be null", triggers);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.TriggerContainer#removeListener(java.util.UUID, java.util.UUID)}.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void removeListener() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);

		ITriggerListener listener1 = new TestListener();
		ITriggerListener listener2 = new TestListener();
		ITriggerListener listener3 = new TestListener();

		// Create listener info
		TriggerListenerInfo listenerInfo10 = new TriggerListenerInfo();
		listenerInfo10.action = "listener1 first action";
		listenerInfo10.listener = listener1;
		listenerInfo10.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo11 = new TriggerListenerInfo();
		listenerInfo11.action = "second action";
		listenerInfo11.listener = listener1;
		listenerInfo11.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo2 = new TriggerListenerInfo();
		listenerInfo2.listener = listener2;
		listenerInfo2.listenerId = listener2.getId();

		TriggerListenerInfo listenerInfo3 = new TriggerListenerInfo();
		listenerInfo3.listener = listener3;
		listenerInfo3.listenerId = listener3.getId();


		// -- TESTS START HERE --
		// Trigger 1
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo10);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo2);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo3);
		mTriggerContainer.removeListener(trigger1.getId(), listener1.getId());

		assertEquals("Added 3, removed 2 listeners", 2, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		Array<TriggerListenerInfo> listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should contain some listener", 2, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should be empty", 0, listeners.size);

		// Trigger 2
		mTriggerContainer.addListener(trigger2.getId(), listenerInfo11);
		mTriggerContainer.addListener(trigger2.getId(), listenerInfo3);
		assertEquals("Added another listener info, but readded same listener", 3, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should contain some listeners", 2, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should contain some listeners", 2, listeners.size);
		Array<UUID> triggers = ((ObjectMap<UUID, Array<UUID>>)mfListenerTriggers.get(mTriggerContainer)).get(listener1.getId());
		assertEquals("Listener 1 should contain a trigger", 1, triggers.size);


		// Remove a listener that doesn't exist (but trigger does)
		try {
			mTriggerContainer.removeListener(trigger1.getId(), UUID.randomUUID());
		} catch (NullPointerException e) {
			assertTrue("removed listener with invalid listener id, but valid trigger id", true);
		}
		assertEquals("Added another listener info, but readded same listener", 3, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should contain some listeners", 2, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should contain some listeners", 2, listeners.size);
		triggers = ((ObjectMap<UUID, Array<UUID>>)mfListenerTriggers.get(mTriggerContainer)).get(listener1.getId());
		assertEquals("Listener 1 should contain a trigger", 1, triggers.size);


		// Remove a trigger that doesn't exist (but listener does)
		try {
			mTriggerContainer.removeListener(UUID.randomUUID(), listener1.getId());
		} catch (NullPointerException e) {
			assertTrue("removed listener with invalid trigger id, but valid listener id", true);
		}
		assertEquals("Added another listener info, but readded same listener", 3, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should contain some listeners", 2, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should contain some listeners", 2, listeners.size);
		triggers = ((ObjectMap<UUID, Array<UUID>>)mfListenerTriggers.get(mTriggerContainer)).get(listener1.getId());
		assertEquals("Listener 1 should contain a trigger", 1, triggers.size);


		// Remove all
		mTriggerContainer.removeListener(trigger1.getId(), listener2.getId());
		mTriggerContainer.removeListener(trigger1.getId(), listener3.getId());
		mTriggerContainer.removeListener(trigger2.getId(), listener1.getId());
		mTriggerContainer.removeListener(trigger2.getId(), listener3.getId());
		assertEquals("Added another listener info, but readded same listener", 0, ((ObjectMap<?, ?>)mfListenerTriggers.get(mTriggerContainer)).size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger1.getId());
		assertEquals("Trigger 1 should contain some listeners", 0, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(mTriggerContainer)).get(trigger2.getId());
		assertEquals("Trigger 2 should contain some listeners", 0, listeners.size);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.TriggerContainer#bindTriggers(com.badlogic.gdx.utils.ObjectMap)}.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void bindTriggers() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);

		ITriggerListener listener1 = new TestListener();
		ITriggerListener listener2 = new TestListener();
		ITriggerListener listener3 = new TestListener();
		ObjectMap<UUID, ITriggerListener> triggerListeners = new ObjectMap<UUID, ITriggerListener>();
		triggerListeners.put(listener1.getId(), listener1);
		triggerListeners.put(listener2.getId(), listener2);
		triggerListeners.put(listener3.getId(), listener3);

		// Create listener info
		TriggerListenerInfo listenerInfo10 = new TriggerListenerInfo();
		listenerInfo10.action = "listener1 first action";
		listenerInfo10.listener = listener1;
		listenerInfo10.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo11 = new TriggerListenerInfo();
		listenerInfo11.action = "second action";
		listenerInfo11.listener = listener1;
		listenerInfo11.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo2 = new TriggerListenerInfo();
		listenerInfo2.listener = listener2;
		listenerInfo2.listenerId = listener2.getId();

		TriggerListenerInfo listenerInfo3 = new TriggerListenerInfo();
		listenerInfo3.listener = listener3;
		listenerInfo3.listenerId = listener3.getId();

		// -- TESTS START HERE --
		// Trigger 1
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo10);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo2);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo3);
		mTriggerContainer.removeListener(trigger1.getId(), listener1.getId());
		// Only listener 2 and 3 should now.

		Json json = new Json();
		String jsonString = json.toJson(mTriggerContainer);
		json.prettyPrint(jsonString);
		jsonString = json.toJson(mTriggerContainer);
		TriggerContainer jsonTriggerContainer = json.fromJson(TriggerContainer.class, jsonString);

		// Save and load triggers
		jsonString = json.toJson(triggerListeners);
		ObjectMap<UUID, ITriggerListener> jsonTriggerListeners = json.fromJson(ObjectMap.class, jsonString);
		jsonTriggerContainer.bindTriggers(jsonTriggerListeners);

		Array<TriggerListenerInfo> listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(jsonTriggerContainer)).get(trigger1.getId());
		assertNotNull("Trigger 1 listeners should not be null", listeners);
		assertEquals("Trigger 1 should contain some listener", 2, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(jsonTriggerContainer)).get(trigger2.getId());
		assertNotNull("Trigger 1 listeners should not be null", listeners);
		assertEquals("Trigger 2 should be empty", 0, listeners.size);
		assertEquals("Number of triggers in trigger list", 3, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		ObjectMap<UUID, Trigger> triggers = ((ObjectMap<UUID, Trigger>) mfTriggers.get(jsonTriggerContainer));
		assertEquals("Trigger 1", trigger1, triggers.get(trigger1.getId()));
		assertEquals("Trigger 2", trigger2, triggers.get(trigger2.getId()));
		assertEquals("Trigger 3", trigger3, triggers.get(trigger3.getId()));
		// Check so that the trigger points to the correct object
		assertTrue("Trigger 1 same reference", checkListenerBound(jsonTriggerListeners.get(listener2.getId()), jsonTriggerContainer, trigger1.getId()));
		assertTrue("Trigger 1 same reference", checkListenerBound(jsonTriggerListeners.get(listener3.getId()), jsonTriggerContainer, trigger1.getId()));
	}

	/**
	 * Checks if this listener is bound to a trigger correctly
	 * @param listener the listener to test
	 * @param container the trigger container to search in
	 * @param triggerId the trigger to test with
	 * @return true if this listener is bound to a trigger correctly
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	private boolean checkListenerBound(ITriggerListener listener, TriggerContainer container, UUID triggerId) throws IllegalArgumentException, IllegalAccessException {
		Array<TriggerListenerInfo> listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(container)).get(triggerId);
		for (TriggerListenerInfo listenerInfo : listeners) {
			if (listener == listenerInfo.listener && listener.getId().equals(listenerInfo.listenerId)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.game.TriggerContainer#write(com.spiddekauga.utils.Json)}.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void writeRead() throws IllegalArgumentException, IllegalAccessException {
		Trigger trigger1 = new TestTrigger();
		Trigger trigger2 = new TestTrigger();
		Trigger trigger3 = new TestTrigger();

		mTriggerContainer.addTrigger(trigger1);
		mTriggerContainer.addTrigger(trigger2);
		mTriggerContainer.addTrigger(trigger3);

		ITriggerListener listener1 = new TestListener();
		ITriggerListener listener2 = new TestListener();
		ITriggerListener listener3 = new TestListener();

		// Create listener info
		TriggerListenerInfo listenerInfo10 = new TriggerListenerInfo();
		listenerInfo10.action = "listener1 first action";
		listenerInfo10.listener = listener1;
		listenerInfo10.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo11 = new TriggerListenerInfo();
		listenerInfo11.action = "second action";
		listenerInfo11.listener = listener1;
		listenerInfo11.listenerId = listener1.getId();

		TriggerListenerInfo listenerInfo2 = new TriggerListenerInfo();
		listenerInfo2.listener = listener2;
		listenerInfo2.listenerId = listener2.getId();

		TriggerListenerInfo listenerInfo3 = new TriggerListenerInfo();
		listenerInfo3.listener = listener3;
		listenerInfo3.listenerId = listener3.getId();

		// -- TESTS START HERE --
		// Trigger 1
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo10);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo2);
		mTriggerContainer.addListener(trigger1.getId(), listenerInfo3);
		mTriggerContainer.removeListener(trigger1.getId(), listener1.getId());

		Json json = new Json();
		String jsonString = json.toJson(mTriggerContainer);
		json.prettyPrint(jsonString);
		jsonString = json.toJson(mTriggerContainer);
		TriggerContainer jsonTriggerContainer = json.fromJson(TriggerContainer.class, jsonString);

		Array<TriggerListenerInfo> listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(jsonTriggerContainer)).get(trigger1.getId());
		assertNotNull("Trigger 1 listeners should not be null", listeners);
		assertEquals("Trigger 1 should contain some listener", 2, listeners.size);
		listeners = ((ObjectMap<UUID, Array<TriggerListenerInfo>>) mfTriggerListeners.get(jsonTriggerContainer)).get(trigger2.getId());
		assertNotNull("Trigger 1 listeners should not be null", listeners);
		assertEquals("Trigger 2 should be empty", 0, listeners.size);
		assertEquals("Number of triggers in trigger list", 3, ((ObjectMap<?, ?>) mfTriggers.get(mTriggerContainer)).size);
		ObjectMap<UUID, Trigger> triggers = ((ObjectMap<UUID, Trigger>) mfTriggers.get(jsonTriggerContainer));
		assertEquals("Trigger 1", trigger1, triggers.get(trigger1.getId()));
		assertEquals("Trigger 2", trigger2, triggers.get(trigger2.getId()));
		assertEquals("Trigger 3", trigger3, triggers.get(trigger3.getId()));
	}

	/** Trigger container to test on */
	private TriggerContainer mTriggerContainer = null;

	// Private variables to test in trigger container
	/** Helper for finding all the triggers of a listener */
	private static Field mfListenerTriggers = null;
	/** All triggers */
	private static Field mfTriggers = null;
	/** Collection where each element is an array of the trigger's listeners */
	private static Field mfTriggerListeners = null;
}
