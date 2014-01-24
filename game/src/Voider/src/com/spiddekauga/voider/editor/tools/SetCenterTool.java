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
	protected boolean down(int button) {
		if (!mSelection.isSelectionChangedDuringDown()) {
			ArrayList<? extends Actor> selectedActors = mSelection.getSelectedResourcesOfType(mActorType);

			Vector2 centerOffset = Pools.vector2.obtain();
			Vector2 newActorPos = Pools.vector2.obtain();
			for (Actor actor : selectedActors) {
				centerOffset.set(actor.getPosition()).sub(mTouchCurrent);
				centerOffset.add(actor.getDef().getVisualVars().getCenterOffset());
				mOriginalCenter.set(actor.getDef().getVisualVars().getCenterOffset());
				actor.getDef().getVisualVars().setCenterOffset(centerOffset);

				newActorPos.set(mOriginalCenter).sub(centerOffset);
				newActorPos.add(actor.getPosition());
				actor.setPosition(newActorPos);
			}

			Pools.vector2.freeAll(centerOffset, newActorPos);
			Pools.arrayList.free(selectedActors);

			return true;
		}
		return false;
	}

	@Override
	protected boolean dragged() {
		if (!mSelection.isSelectionChangedDuringDown()) {
			ArrayList<? extends Actor> selectedActors = mSelection.getSelectedResourcesOfType(mActorType);

			Vector2 centerOffset = Pools.vector2.obtain();
			Vector2 newActorPos = Pools.vector2.obtain();
			Vector2 oldCenterOffset = Pools.vector2.obtain();
			for (Actor actor : selectedActors) {
				centerOffset.set(actor.getPosition()).sub(mTouchCurrent);
				centerOffset.add(actor.getDef().getVisualVars().getCenterOffset());
				oldCenterOffset.set(actor.getDef().getVisualVars().getCenterOffset());
				actor.getDef().getVisualVars().setCenterOffset(centerOffset);

				newActorPos.set(oldCenterOffset).sub(centerOffset);
				newActorPos.add(actor.getPosition());
				actor.setPosition(newActorPos);
			}

			Pools.vector2.freeAll(centerOffset, newActorPos);
			Pools.arrayList.free(selectedActors);

			return true;
		}
		return false;
	}

	@Override
	protected boolean up(int button) {
		if (!mSelection.isSelectionChangedDuringDown()) {
			ArrayList<? extends Actor> selectedActors = mSelection.getSelectedResourcesOfType(mActorType);

			Vector2 centerOffset = Pools.vector2.obtain();
			Vector2 oldCenterOffset = Pools.vector2.obtain();
			Vector2 oldActorPos = Pools.vector2.obtain();
			boolean chained = false;
			for (Actor actor : selectedActors) {
				centerOffset.set(actor.getPosition()).sub(mTouchCurrent);
				centerOffset.add(actor.getDef().getVisualVars().getCenterOffset());
				oldCenterOffset.set(actor.getDef().getVisualVars().getCenterOffset());
				actor.getDef().getVisualVars().setCenterOffset(mOriginalCenter);

				// Reset player position
				oldActorPos.set(oldCenterOffset).sub(mOriginalCenter);
				oldActorPos.add(actor.getPosition());
				actor.setPosition(oldActorPos);

				mInvoker.execute(new CActorCenterMove(actor.getDef(), centerOffset, mOriginalCenter, mEditor, actor), chained);
				chained = true;
			}

			Pools.vector2.freeAll(centerOffset, oldCenterOffset, oldActorPos);
			Pools.arrayList.free(selectedActors);

			return true;
		}

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

	/** Original drag position of the center */
	private Vector2 mOriginalCenter = new Vector2();
}
