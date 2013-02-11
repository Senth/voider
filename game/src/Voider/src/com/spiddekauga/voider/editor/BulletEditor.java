package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.WorldScene;

/**
 * Creates bullets for the enemies and player to use.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletEditor extends WorldScene {
	/**
	 * Creates a bullet editor.
	 */
	public BulletEditor() {
		super(new BulletEditorGui());
	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void loadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.loadAllOf(BulletActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.unloadAllOf(BulletActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}
}
