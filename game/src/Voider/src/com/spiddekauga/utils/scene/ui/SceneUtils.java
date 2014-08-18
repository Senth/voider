package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.spiddekauga.voider.utils.Pools;

/**
 * Helper methods for various scene tasks
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SceneUtils {
	/**
	 * Check if a (stage) point is within an actor or not
	 * @param actor the actor to test the point on
	 * @param x stage coordinate
	 * @param y stage coordinate
	 * @return true if point is within the actor
	 */
	public static boolean isStagePointInsideActor(Actor actor, float x, float y) {
		Vector2 min = Pools.vector2.obtain();
		Vector2 max = Pools.vector2.obtain();

		min.set(actor.getX(), actor.getY());
		actor.localToStageCoordinates(min);
		max.set(min.x + actor.getWidth(), min.y + actor.getHeight());

		boolean inside = true;
		if (x < min.x || x >= max.x || y < min.y || y >= max.y) {
			inside = false;
		}

		Pools.vector2.freeAll(min, max);

		return inside;
	}

	/**
	 * Check if a (stage) point is within an actor or not
	 * @param actor the actor to test the point on
	 * @param point stage coordinates
	 * @return true if point is within the actor
	 */
	public static boolean isStagePointInsideActor(Actor actor, Vector2 point) {
		return isStagePointInsideActor(actor, point.x, point.y);
	}
}
