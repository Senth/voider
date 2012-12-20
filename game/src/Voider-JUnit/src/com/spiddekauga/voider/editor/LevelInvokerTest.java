package com.spiddekauga.voider.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;

/**
 * Tests that the level invoker works correctly with execute/undo/redo
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelInvokerTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		World world = new World(new Vector2(), true);
		Actor.setWorld(world);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Resets the level invoker.
	 */
	@Before
	public void setUp() {
		mLevelDef= new LevelDef();
		mLevel = new Level(mLevelDef);
		mLevelInvoker = new LevelInvoker(mLevel);
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.editor.LevelInvoker#execute(com.spiddekauga.voider.editor.commands.LevelCommand)}.
	 */
	@Test
	public void execute() {
		mLevelInvoker.execute(new ClChangeSpeed(mLevel, 5.5f));
		assertEquals("first speed", 5.5f, mLevel.getSpeed(), 0.0f);
		mLevelInvoker.execute(new ClChangeSpeed(mLevel, 6.0f));
		assertEquals("second speed", 6.0f, mLevel.getSpeed(), 0.0f);
	}

	/**
	 * Tests execute, undo, and redo some times after some scenarios.
	 */
	@Test
	public void executeUndoRedo() {
		assertTrue("cannot undo", !mLevelInvoker.canUndo());
		assertTrue("cannot redo", !mLevelInvoker.canRedo());

		mLevelInvoker.execute(new ClChangeSpeed(mLevel, 5.5f));
		assertEquals("first speed", 5.5f, mLevel.getSpeed(), 0.0f);
		mLevelInvoker.undo();
		assertEquals("initial speed", mLevelDef.getBaseSpeed(), mLevel.getSpeed(), 0.0f);
		assertTrue("can redo", mLevelInvoker.canRedo());

		// New execute clears redo map
		mLevelInvoker.execute(new ClChangeSpeed(mLevel, 6.0f));
		assertEquals("first speed", 6.0f, mLevel.getSpeed(), 0.0f);

		mLevelInvoker.execute(new ClChangeSpeed(mLevel, 3.0f));
		assertEquals("second speed", 3.0f, mLevel.getSpeed(), 0.0f);
		mLevelInvoker.undo();
		assertEquals("undo second speed", 6.0f, mLevel.getSpeed(), 0.0f);
		mLevelInvoker.redo();
		assertEquals("redo second speed", 3.0f, mLevel.getSpeed(), 0.0f);
		mLevelInvoker.undo();
		assertEquals("undo second speed again", 6.0f, mLevel.getSpeed(), 0.0f);
		mLevelInvoker.undo();
		assertEquals("undo first speed", mLevelDef.getBaseSpeed(), mLevel.getSpeed(), 0.0f);
		mLevelInvoker.redo();
		assertEquals("redo first speed", 6.0f, mLevel.getSpeed(), 0.0f);

		assertTrue("can redo", mLevelInvoker.canRedo());
		assertTrue("can undo", mLevelInvoker.canUndo());
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.editor.LevelInvoker#undo()}.
	 * @note this test will fail if execute() fails!
	 */
	@Test
	public void undo() {
		assertTrue("cannot undo", !mLevelInvoker.canUndo());

		execute();

		assertTrue("can undo 1", mLevelInvoker.canUndo());
		mLevelInvoker.undo();
		assertEquals("first undo", 5.5f, mLevel.getSpeed(), 0.0f);
		assertTrue("can undo 2", mLevelInvoker.canUndo());
		mLevelInvoker.undo();
		assertEquals("second undo", mLevelDef.getBaseSpeed(), mLevel.getSpeed(), 0.0f);

		assertTrue("cannot undo", !mLevelInvoker.canUndo());
		mLevelInvoker.undo();
	}

	/**
	 * Test method for {@link com.spiddekauga.voider.editor.LevelInvoker#redo()}.
	 * @note this test will fail if either execute() or undo() fails!
	 */
	@Test
	public void redo() {
		assertTrue("cannot undo", !mLevelInvoker.canUndo());
		assertTrue("cannot redo", !mLevelInvoker.canRedo());

		mLevelInvoker.execute(new ClChangeSpeed(mLevel, 5.5f));
		assertEquals("first speed", 5.5f, mLevel.getSpeed(), 0.0f);
		mLevelInvoker.execute(new ClChangeSpeed(mLevel, 6.0f));
		assertEquals("second speed", 6.0f, mLevel.getSpeed(), 0.0f);

		assertTrue("can undo 1", mLevelInvoker.canUndo());
		assertTrue("cannot redo", !mLevelInvoker.canRedo());
		mLevelInvoker.undo();
		assertEquals("first undo", 5.5f, mLevel.getSpeed(), 0.0f);
		assertTrue("can redo 1", mLevelInvoker.canRedo());
		assertTrue("can undo 2", mLevelInvoker.canUndo());
		mLevelInvoker.undo();
		assertEquals("second undo", mLevelDef.getBaseSpeed(), mLevel.getSpeed(), 0.0f);

		assertTrue("cannot undo", !mLevelInvoker.canUndo());
		assertTrue("can redo 2", mLevelInvoker.canRedo());
		mLevelInvoker.redo();
		assertEquals("first redo", 5.5f, mLevel.getSpeed(), 0.0f);
		assertTrue("can redo 1", mLevelInvoker.canRedo());
		mLevelInvoker.redo();
		assertEquals("second redo", 6.0f, mLevel.getSpeed(), 0.0f);
		assertTrue("cannot redo", !mLevelInvoker.canRedo());
	}

	/** The level definition used for the level */
	LevelDef mLevelDef = null;
	/** Level to test actions on */
	Level mLevel = null;
	/** Tests invoker actions */
	LevelInvoker mLevelInvoker = null;
}
