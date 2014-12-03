package com.spiddekauga.voider.explore;

import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.resources.Def;

/**
 * Creates various explore scenes
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreFactory {
	/**
	 * Create an explore scene depending on the definition to explore
	 * @param defType what to explore
	 * @param action the action to do when the resource is selected
	 * @return correct explore scene for this type, null if no explore scene exists for
	 *         this type
	 */
	public static ExploreScene create(Class<? extends Def> defType, ExploreActions action) {
		if (LevelDef.class == defType) {
			return new ExploreLevelScene(action);
		}
		if (ActorDef.class.isAssignableFrom(defType)) {
			if (EnemyActorDef.class == defType) {
				return new ExploreEnemyScene(action);
			}
			if (BulletActorDef.class == defType) {
				return new ExploreBulletScene(action);
			}

			return new ExploreActorScene(action);
		}

		return null;
	}
}
