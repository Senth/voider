package com.spiddekauga.voider.editor.commands;

import java.lang.reflect.Constructor;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Returns to a specific scene. If an instance of this scene already exists
 * it will return to that one and pop all scenes before it in the stack. If
 * the scene doesn't exist it will create a new one (only works if the scene
 * has a default constructor) and place it at the top
 * of the stack.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CSceneReturn extends Command {
	/**
	 * Creates a return command that will return to the specified scene type
	 * @param sceneType the scene type to return to
	 */
	public CSceneReturn(Class<? extends Scene> sceneType) {
		mSceneType = sceneType;
	}

	@Override
	public boolean execute() {
		boolean successReturn = SceneSwitcher.returnTo(mSceneType);

		// No such scene has been created, create a new scene
		if (!successReturn) {
			try {
				Constructor<?> constructor = mSceneType.getConstructor();
				Scene scene = (Scene) constructor.newInstance();
				SceneSwitcher.switchTo(scene);
			} catch (Exception e) {
				Gdx.app.error("CSceneReturn", e.toString());
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean undo() {
		return false;
	}

	/** The scene type to switch to */
	Class<? extends Scene> mSceneType;
}
