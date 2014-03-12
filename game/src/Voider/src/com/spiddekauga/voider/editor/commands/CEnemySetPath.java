package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.actors.EnemyActor;

/**
 * Sets the enemy's path
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CEnemySetPath extends CResourceChange {
	/**
	 * Creates a command that sets the enemy path to the specified one
	 * @param enemy the enemy to set the path in
	 * @param path the path to set
	 * @param editor the resource editor that shall recieve the onResourceChanged event
	 */
	public CEnemySetPath(EnemyActor enemy, Path path, IResourceChangeEditor editor) {
		super(enemy, editor);
		mOldPath = enemy.getPath();
		mNewPath = path;
	}

	@Override
	public boolean execute() {
		// Skip if old path is the same...
		if (mNewPath == mOldPath) {
			return false;
		}

		((EnemyActor)mResource).setPath(mNewPath);
		return true;
	}

	@Override
	public boolean undo() {
		((EnemyActor)mResource).setPath(mOldPath);
		return true;
	}

	/** New path to set (on execute()) */
	Path mNewPath;
	/** Old path of the enemy (on undo()) */
	Path mOldPath;
}
