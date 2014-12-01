package com.spiddekauga.voider.menu;

import java.util.List;

import com.spiddekauga.voider.network.entities.resource.DefEntity;

/**
 * Can be used separately or be derived and used as a base class for tailoring this scene.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreActorScene extends ExploreScene {

	/**
	 * Hidden constructor. Create from ExploreFactory
	 */
	ExploreActorScene() {
		this(new ExploreActorGui());
	}

	/**
	 * This constructor should be called if it's a super class
	 * @param gui
	 */
	protected ExploreActorScene(ExploreActorGui gui) {
		super(gui);

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

	/**
	 * Called when an actor has been selected and pressed again. I.e. default action
	 */
	protected void onSelectAction() {
		// TODO
	}

	private DefEntity mSelected = null;
}
