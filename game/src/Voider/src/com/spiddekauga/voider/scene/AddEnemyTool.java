package com.spiddekauga.voider.scene;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IActorChangeEditor;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;

/**
 * Tool for adding enemies. This also has the ability to create a stack
 * of enemies that are grouped together. These enemies are essentially the
 * same, same path, same location, etc. The only difference is that
 * they can have a delay when they are activated.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AddEnemyTool extends AddActorTool {
	/**
	 * Creates an add enemy tool. You still need to set the enemy actor definition
	 * via {@link #setNewActorDef(com.spiddekauga.voider.game.actors.ActorDef)} to make
	 * it work.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param invoker used for undo/redo actions
	 * @param editor will be called when actors are added/removed
	 */
	public AddEnemyTool(Camera camera, World world, Invoker invoker, IActorChangeEditor editor) {
		super(camera, world, EnemyActor.class, invoker, true, editor);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sets the number of duplicates of the currently selected actor
	 * @param cActors number of duplicates of the currently selected actor
	 */
	public void setDuplicatesOfSelected(int cActors) {

	}

	/**
	 * @return number of duplicates of the currently selected actor
	 */
	public int getDuplicatesOfSelected() {
		return mDuplicates.size();
	}

	/**
	 * Sets the duplicate trigger delay of the selected actor.
	 * @param float seconds delay between each actor.
	 */
	public void setDuplicateTriggerDelay(int delay) {

	}

	/**
	 * @return seconds of duplicate trigger delay between each actors. -1
	 * if no enemy is selected, or only one duplicate exist.
	 */
	public int getDuplicateTriggerDelay() {
		if (mDuplicates.size() > 1) {
			// TODO get the delay of the group
			return -1;
		} else {
			return -1;
		}
	}

	@Override
	public void setSelectedActor(Actor selectedActor) {
		super.setSelectedActor(selectedActor);

		// TODO get the other actors in the group
	}

	@Override
	protected void down() {
		// TODO
	}

	@Override
	protected void dragged() {
		// TODO
	}

	@Override
	protected void up() {
		// TODO
	}

	/** TODO change to enemy group All the duplicates (including original actor) */
	protected ArrayList<EnemyActor> mDuplicates = new ArrayList<EnemyActor>();
}
