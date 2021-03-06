package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Helper methods for various scene tasks
 */
public class SceneUtils {
/**
 * Check if a (stage) point is within an actor or not
 * @param actor the actor to test the point on
 * @param point stage coordinates
 * @return true if point is within the actor
 */
public static boolean isStagePointInsideActor(Actor actor, Vector2 point) {
	return isStagePointInsideActor(actor, point.x, point.y);
}

/**
 * Check if a (stage) point is within an actor or not
 * @param actor the actor to test the point on
 * @param x stage coordinate
 * @param y stage coordinate
 * @return true if point is within the actor
 */
public static boolean isStagePointInsideActor(Actor actor, float x, float y) {
	Vector2 min = new Vector2();
	Vector2 max = new Vector2();

	min.set(actor.getX(), actor.getY());
	actor.localToStageCoordinates(min);
	max.set(min.x + actor.getWidth(), min.y + actor.getHeight());

	boolean inside = true;
	if (x < min.x || x >= max.x || y < min.y || y >= max.y) {
		inside = false;
	}

	return inside;
}
}
