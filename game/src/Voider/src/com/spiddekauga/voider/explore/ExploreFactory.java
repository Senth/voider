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
	 * @return correct explore scene for this type, null if no explore scene exists for
	 *         this type
	 */
	public static ExploreScene create(Class<? extends Def> defType) {
		if (LevelDef.class == defType) {
			return new ExploreLevelScene();
		}
		if (ActorDef.class.isAssignableFrom(defType)) {
			if (EnemyActorDef.class == defType) {
				return new ExploreEnemyScene();
			}
			if (BulletActorDef.class == defType) {
				return new ExploreBulletScene();
			}

			return new ExploreActorScene();
		}

		return null;
	}
}
