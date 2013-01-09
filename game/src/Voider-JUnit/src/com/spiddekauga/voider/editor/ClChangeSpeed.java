package com.spiddekauga.voider.editor;

import java.lang.reflect.Field;

import com.spiddekauga.voider.editor.commands.LevelCommand;
import com.spiddekauga.voider.game.Level;

/**
 * Just a JUnit test command for level invoker
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ClChangeSpeed extends LevelCommand {
	/**
	 * New speed of the level
	 * @param level to change the command on
	 * @param newSpeed the new speed of the level
	 */
	public ClChangeSpeed(Level level, float newSpeed) {
		this(level, newSpeed, false);
	}

	/**
	 * New speed of the level (and chained?)
	 * @param level to change the speed on
	 * @param newSpeed the new speed of the level
	 * @param chained true if chained
	 */
	public ClChangeSpeed(Level level, float newSpeed, boolean chained) {
		super(chained);

		mNewSpeed = newSpeed;
		mOldSpeed = level.getSpeed();

		try {
			mSpeedField = Level.class.getDeclaredField("mSpeed");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		mSpeedField.setAccessible(true);
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#execute(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean execute(Level level, LevelEditor levelEditor) {
		try {
			mSpeedField.set(level, mNewSpeed);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.editor.commands.LevelCommand#undo(com.spiddekauga.voider.game.Level)
	 */
	@Override
	public boolean undo(Level level, LevelEditor levelEditor) {
		try {
			mSpeedField.set(level, mOldSpeed);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** Old speed before changing */
	private float mOldSpeed;
	/** New speeed after changing */
	private float mNewSpeed;

	/** Reflection for setting the speed in the level */
	private Field mSpeedField;
}
