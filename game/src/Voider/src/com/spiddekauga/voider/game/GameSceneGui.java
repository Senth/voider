package com.spiddekauga.voider.game;

import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the GameScene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class GameSceneGui extends Gui {
	/**
	 * Sets the game scene object
	 * @param gameScene the game scene that this object acts on
	 */
	public void setGameScene(GameScene gameScene) {
		mGameScene = gameScene;
	}

	@Override
	public void initGui() {
		/** @TODO add GUI to GameScene */
	}

	/** GameScene object that this GUI acts on */
	private GameScene mGameScene = null;
}
