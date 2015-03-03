package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorCenterMove;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.resources.IResource;

/**
 * Tool for setting the center of a specified actor type
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SetCenterTool extends ActorTool implements ISelectionListener {
	/**
	 * @param editor editor this tool is bound to
	 * @param selection all selected resources
	 * @param actorType actor type
	 */
	public SetCenterTool(IResourceChangeEditor editor, ISelection selection, Class<? extends Actor> actorType) {
		super(editor, selection, actorType);
	}

	@Override
	protected boolean down(int button) {
		if (!mSelection.isSelectionChangedDuringDown()) {
			ArrayList<? extends Actor> selectedActors = mSelection.getSelectedResourcesOfType(mActorType);

			for (Actor actor : selectedActors) {
				mtCenterOffset.set(actor.getPosition()).sub(mTouchCurrent);
				mtCenterOffset.add(actor.getDef().getVisual().getCenterOffset());
				mOriginalCenter.set(actor.getDef().getVisual().getCenterOffset());
				actor.getDef().getVisual().setCenterOffset(mtCenterOffset);

				mtNewActorPos.set(mOriginalCenter).sub(mtCenterOffset);
				mtNewActorPos.add(actor.getPosition());
				actor.setPosition(mtNewActorPos);
			}

			return true;
		}
		return false;
	}

	@Override
	protected boolean dragged() {
		if (!mSelection.isSelectionChangedDuringDown()) {
			ArrayList<? extends Actor> selectedActors = mSelection.getSelectedResourcesOfType(mActorType);

			for (Actor actor : selectedActors) {
				mtCenterOffset.set(actor.getPosition()).sub(mTouchCurrent);
				mtCenterOffset.add(actor.getDef().getVisual().getCenterOffset());
				mtOldCenterOffset.set(actor.getDef().getVisual().getCenterOffset());
				actor.getDef().getVisual().setCenterOffset(mtCenterOffset);

				mtNewActorPos.set(mtOldCenterOffset).sub(mtCenterOffset);
				mtNewActorPos.add(actor.getPosition());
				actor.setPosition(mtNewActorPos);
			}

			return true;
		}
		return false;
	}

	@Override
	protected boolean up(int button) {
		if (!mSelection.isSelectionChangedDuringDown()) {
			ArrayList<? extends Actor> selectedActors = mSelection.getSelectedResourcesOfType(mActorType);

			boolean chained = false;
			for (Actor actor : selectedActors) {
				mtCenterOffset.set(actor.getPosition()).sub(mTouchCurrent);
				mtCenterOffset.add(actor.getDef().getVisual().getCenterOffset());
				mtOldCenterOffset.set(actor.getDef().getVisual().getCenterOffset());
				actor.getDef().getVisual().setCenterOffset(mOriginalCenter);

				// Reset player position
				mtOldActorPos.set(mtOldCenterOffset).sub(mOriginalCenter);
				mtOldActorPos.add(actor.getPosition());
				actor.setPosition(mtOldActorPos);

				mInvoker.execute(new CActorCenterMove(actor.getDef(), mtCenterOffset, mOriginalCenter, mEditor, actor), chained);
				chained = true;
			}

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
			((Actor) resource).createBodyCenter();
		}
	}

	@Override
	public void onResourceDeselected(IResource resource) {
		if (mActorType.isAssignableFrom(resource.getClass())) {
			((Actor) resource).destroyBodyCenter();
		}
	}

	/** Original drag position of the center */
	private Vector2 mOriginalCenter = new Vector2();

	// Temporary variables
	private Vector2 mtCenterOffset = new Vector2();
	private Vector2 mtOldCenterOffset = new Vector2();
	private Vector2 mtOldActorPos = new Vector2();
	private Vector2 mtNewActorPos = new Vector2();
}
