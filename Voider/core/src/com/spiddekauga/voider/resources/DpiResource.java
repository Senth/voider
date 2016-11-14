package com.spiddekauga.voider.resources;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.resource.InternalNames;

/**
 * Wrapper for when these resources should be loaded for a specific screen DPI (or
 * setting)

 */
class DpiResource implements IInternalResource {
	/**
	 * @param densityBucket which density bucket this resource belongs to
	 * @param internalNames resources bound to this density bucket
	 */
	public DpiResource(DensityBuckets densityBucket, InternalNames... internalNames) {
		mDensityBucket = densityBucket;
		mInternalNames = internalNames;
	}

	@Override
	public boolean useResource() {
		// Android
		if (Gdx.app.getType() == ApplicationType.Android) {
			return mDensityBucket.isCurrent();
		}
		// Desktop
		else if (Gdx.app.getType() == ApplicationType.Desktop) {
			SettingRepo settingRepo = SettingRepo.getInstance();
			return settingRepo.display().getIconSize().toDensityBucket() == mDensityBucket;
		}

		return false;
	}

	@Override
	public InternalNames[] getDependencies() {
		return mInternalNames;
	}


	private DensityBuckets mDensityBucket;
	private InternalNames[] mInternalNames;
}
