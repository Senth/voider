package com.spiddekauga.voider.game.actors;

/**
 * Stub actor, used for testing
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorStub extends Actor {

	@Override
	protected short getFilterCategory() {
		return 0;
	}

	@Override
	protected short getFilterCollidingCategories() {
		return 0;
	}

	@Override
	protected float getZValue() {
		return 0;
	}
}
