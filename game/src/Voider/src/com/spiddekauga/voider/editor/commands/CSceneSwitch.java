package com.spiddekauga.voider.editor.commands;

import java.lang.reflect.Constructor;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Switches to the a specified scene. If an instance of this scene already exists
 * it will switch to that one first.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CSceneSwitch extends Command {
	/**
	 * Creates a switch command that will switch to the specified scene type
	 * @param sceneType the scene type to switch to
	 */
	public CSceneSwitch(Class<? extends Scene> sceneType) {
		mSceneType = sceneType;
	}

	@Override
	public boolean execute() {
		boolean successSwitch = SceneSwitcher.switchTo(mSceneType);

		// No such scene has been created, create a new scene
		if (!successSwitch) {
			try {
				Constructor<?> constructor = mSceneType.getConstructor();
				Scene scene = (Scene) constructor.newInstance();
				SceneSwitcher.switchTo(scene);
			} catch (Exception e) {
				Gdx.app.error("CEditorSwitch", e.toString());
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

	/** The editor type to switch to */
	Class<? extends Scene> mSceneType;
}
