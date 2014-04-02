package com.spiddekauga.voider.repo;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
	/** True if we have fetched all */
	public boolean fetchedAll = false;

	@Override
	public void dispose() {
		if (levels != null) {
			// Dispose drawables
			for (LevelInfoEntity levelInfoEntity : levels) {
				if (levelInfoEntity.defEntity.drawable instanceof TextureRegionDrawable) {
					((TextureRegionDrawable) levelInfoEntity.defEntity.drawable).getRegion().getTexture().dispose();
				}
				levelInfoEntity.defEntity.drawable = null;
			}


			Pools.arrayList.free(levels);
		}
	}
}
