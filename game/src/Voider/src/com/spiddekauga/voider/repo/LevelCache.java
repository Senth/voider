package com.spiddekauga.voider.repo;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.voider.network.entities.LevelInfoEntity;
import com.spiddekauga.voider.utils.Pools;

/**
 * Level cache when getting levels
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class LevelCache extends Cache implements Disposable {
	/** All the levels in the cache */
	@SuppressWarnings("unchecked")
	public ArrayList<LevelInfoEntity> levels = Pools.arrayList.obtain();
	/** Server cursor to continue the cache with */
	public String serverCursor = null;

	@Override
	public void dispose() {
		if (levels != null) {
			Pools.arrayList.free(levels);
		}
	}
}
