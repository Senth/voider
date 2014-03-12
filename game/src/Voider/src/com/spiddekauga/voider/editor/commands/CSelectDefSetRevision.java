package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.scene.SelectDefScene;

/**
 * Sets the revision for the currently selected resource
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CSelectDefSetRevision extends Command {
	/**
	 * Set the revision for the current selected definition
	 * @param revision the revision to use
	 * @param selectDefScene the scene
	 */
	public CSelectDefSetRevision(int revision, SelectDefScene selectDefScene) {
		mRevision = revision;
		mScene = selectDefScene;
	}

	/**
	 * Set the list to check which is the currently selected revision
	 * @param list the list that contains all the revisions
	 * @param selectDefScene the scene
	 */
	public CSelectDefSetRevision(List list, SelectDefScene selectDefScene) {
		mList = list;
		mScene = selectDefScene;
	}

	@Override
	public boolean execute() {
		if (mRevision != -1) {
			mScene.setRevision(mRevision);
			return true;
		}
		// Get the current selected revision
		else if (mList != null) {
			String revisionDateString = mList.getSelection();

			if (revisionDateString != null) {
				String revisionString[] = revisionDateString.split("  ");

				if (revisionString.length == 2) {
					int revision = Integer.parseInt(revisionString[0]);
					mScene.setRevision(revision);
				} else {
					Gdx.app.error("CSelectDefSetRevision", "Could not split revision string properly: " + revisionDateString);
				}
			}
		}
		return false;
	}

	@Override
	public boolean undo() {
		// Does nothing
		return false;
	}

	/** Revision to use */
	int mRevision = -1;
	/** The scene */
	SelectDefScene mScene;
	/** List containing all revision */
	List mList = null;
}
