package com.spiddekauga.voider.scene;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.utils.Pools;

/**
 * Container class for all the selected actors in the level editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SelectionTool extends TouchTool {
	/**
	 * @param camera camera used for determining where in the world the pointer i
	 * @param world used for picking
	 * @param invoker used for undo/redo
	 */
	public SelectionTool(Camera camera, World world, Invoker invoker) {
		super(camera, world, invoker);
	}

	@Override
	protected boolean down() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean dragged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean up() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected QueryCallback getCallback() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void activate() {
		mActive = true;
	}

	@Override
	public void deactivate() {
		mActive = false;
	}

	/**
	 * @return all selected actors
	 */
	public ArrayList<Actor> getSelectedActors() {
		return mSelectedActors;
	}

	/**
	 * @return the most common type of selected actors
	 */
	public Class<? extends Actor> getMostCommonSelectedActorType() {
		// TODO
		return null;
	}

	/**
	 * @param <ActorType> type of actor to get
	 * @param type what type of selected actors to get
	 * @return all selected actors of the specified type. Don't forget to free the arraylist
	 */
	@SuppressWarnings("unchecked")
	public <ActorType extends Actor> ArrayList<ActorType> getSelectedActorsOfType(Class<ActorType> type) {
		ArrayList<ActorType> selectedActors = Pools.arrayList.obtain();

		for (Actor selectedActor : mSelectedActors) {
			if (selectedActor.getClass() == type) {
				selectedActors.add((ActorType) selectedActor);
			}
		}

		return selectedActors;
	}

	/**
	 * Delete selected actors
	 */
	public void deleteSelectedActors() {
		// TODO
	}

	/**
	 * Clears the selection, i.e. no actors will be selected
	 */
	public void clearSelection() {
		for (Actor actor : mSelectedActors) {
			actor.setSelected(false);
		}

		mSelectedActors.clear();
	}

	/** If the selection tool is active */
	boolean mActive = false;
	/** Current actor selection */
	ArrayList<Actor> mSelectedActors = new ArrayList<Actor>();
}
