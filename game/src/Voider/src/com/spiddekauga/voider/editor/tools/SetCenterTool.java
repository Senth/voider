package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorCenterMove;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for setting the center of a specified actor type
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SetCenterTool extends ActorTool implements ISelectionListener {
	/**
	 * @param camera used for picking on screen
	 * @param world used for converting screen to world coordinates
	 * @param invoker used undo/redo
	 * @param selection all selected resources
	 * @param editor editor this tool is bound to
	 * @param actorType actor type
	 */
	public SetCenterTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor, Class<? extends Actor> actorType) {
		super(camera, world, invoker, selection, editor, actorType);
	}

	@Override
	protected boolean down() {
		if (!mSelection.isSelectionChangedDuringDown()) {
			ArrayList<? extends Actor> selectedActors = mSelection.getSelectedResourcesOfType(mActorType);

			Vector2 centerOffset = Pools.vector2.obtain();
			boolean chained = false;
			for (Actor actor : selectedActors) {
				centerOffset.set(actor.getPosition()).sub(mTouchCurrent);
				centerOffset.add(actor.getDef().getVisualVars().getCenterOffset());
				Vector2 originalPosition = actor.getDef().getVisualVars().getCenterOffset();
				mInvoker.execute(new CActorCenterMove(actor.getDef(), centerOffset, originalPosition, mEditor, actor), chained);
				chained = true;
			}

			Pools.vector2.free(centerOffset);
			Pools.arrayList.free(selectedActors);
		}
		return false;
	}

	@Override
	protected boolean dragged() {
		// Does nothing
		return false;
	}

	@Override
	protected boolean up() {
		// Does nothing
		return false;
	}

	@Override
	public void activate() {
		mSelection.addListener(this);

		for (IResource resource : mSelection.getSelectedResources()) {
			onResourceSelected(resource);
		}
	}

	@Override
	public void deactivate() {
		mSelection.removeListener(this);

		for (IResource resource : mSelection.getSelectedResources()) {
			onResourceDeselected(resource);
		}
	}

	@Override
	public void onResourceSelected(IResource resource) {
		if (mActorType.isAssignableFrom(resource.getClass())) {
			((Actor)resource).createBodyCenter();
		}
	}

	@Override
	public void onResourceDeselected(IResource resource) {
		if (mActorType.isAssignableFrom(resource.getClass())) {
			((Actor)resource).destroyBodyCenter();
		}
	}
}
