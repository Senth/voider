package com.spiddekauga.voider.explore;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.network.entities.resource.DefEntity;

/**
 * Can be used separately or be derived and used as a base class for tailoring this scene.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreActorScene extends ExploreScene {

	/**
	 * Hidden constructor. Create from ExploreFactory
	 * @param action the action to do when the resource is selected
	 */
	ExploreActorScene(ExploreActions action) {
		this(new ExploreActorGui(), action);
	}

	/**
	 * This constructor should be called if it's a super class
	 * @param gui
	 * @param action the action to do when the resource is selected
	 */
	protected ExploreActorScene(ExploreActorGui gui, ExploreActions action) {
		super(gui, action);

		((ExploreActorGui) mGui).setActorScene(this);
	}

	@Override
	boolean isFetchingContent() {
		return false;
	}

	@Override
	boolean hasMoreContent() {
		return false;
	}

	@Override
	void fetchMoreContent() {
		// Does nothings
	}

	@Override
	void repopulateContent() {
		// Does nothing
	}

	/**
	 * Create drawables for all actors
	 * @param actors all actors to create drawables for
	 */
	protected void createDrawables(List<? extends DefEntity> actors) {
		for (DefEntity defEntity : actors) {
			createDrawable(defEntity);
		}
	}

	/**
	 * @param <ActorType> type of actor that is selected
	 * @return the selected actor
	 */
	@SuppressWarnings("unchecked")
	protected <ActorType extends DefEntity> ActorType getSelectedActor() {
		return (ActorType) mSelected;
	}

	/**
	 * Sets the selected actor
	 * @param actor the selected actor
	 */
	protected void setSelectedActor(DefEntity actor) {
		mSelected = actor;
	}

	@Override
	protected void onSelectAction(ExploreActions action) {
		switch (action) {
		case PLAY:
			Gdx.app.error("ExploreActorScene", "ExploreAction.PLAY is not valid for ExploreActorScene");
			endScene();
			break;

		case LOAD:
		case SELECT:
			downloadResource(mSelected);
			break;

		}
	}

	@Override
	protected void onResourceDownloaded(ExploreActions action) {
		switch (action) {
		case LOAD:
		case SELECT:
			setOutcome(Outcomes.DEF_SELECTED, mSelected);
			break;

		default:
			break;
		}
	}

	private DefEntity mSelected = null;
}
