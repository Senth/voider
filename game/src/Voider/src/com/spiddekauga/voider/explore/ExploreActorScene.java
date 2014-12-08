package com.spiddekauga.voider.explore;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.network.entities.resource.DefEntity;

/**
 * Can be used separately or be derived and used as a base class for tailoring this scene.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreActorScene extends ExploreScene {

	/**
	 * Hidden constructor. Create from ExploreFactory
	 * @param action the action to do when the resource is selected
	 * @param actorDefType the actor definition type browse
	 */
	ExploreActorScene(ExploreActions action, Class<? extends ActorDef> actorDefType) {
		this(new ExploreActorGui(), action, actorDefType);
	}

	/**
	 * This constructor should be called if it's a super class
	 * @param gui
	 * @param action the action to do when the resource is selected
	 * @param actorDefType the actor definition type to browse
	 */
	protected ExploreActorScene(ExploreActorGui gui, ExploreActions action, Class<? extends ActorDef> actorDefType) {
		super(gui, action, actorDefType);

		((ExploreActorGui) mGui).setActorScene(this);
	}

	@Override
	protected boolean isFetchingContent() {
		return false;
	}

	@Override
	protected boolean hasMoreContent() {
		return false;
	}

	@Override
	protected void fetchMoreContent() {
		// Does nothings
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

	@Override
	protected void onSelectAction(ExploreActions action) {
		switch (action) {
		case PLAY:
			Gdx.app.error("ExploreActorScene", "ExploreAction.PLAY is not valid for ExploreActorScene");
			endScene();
			break;

		case LOAD:
		case SELECT:
			downloadResource(getSelected());
			break;

		}
	}

	@Override
	protected void onResourceDownloaded(ExploreActions action) {
		switch (action) {
		case LOAD:
			setOutcome(Outcomes.EXPLORE_LOAD, getSelected());
			break;

		case SELECT:
			setOutcome(Outcomes.EXPLORE_SELECT, getSelected());
			break;

		default:
			break;
		}
	}
}
