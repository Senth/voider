package com.spiddekauga.voider.scene;

/**
 * Displays a progress bar for loading local resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoadingProgressScene extends LoadingScene {
	/**
	 * Default constructor
	 */
	public LoadingProgressScene() {
		super(new LoadingProgressGui());
	}
}
