package com.spiddekauga.voider.game;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
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
		super.initGui();

		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setCellPaddingDefault(2, 2, 2, 2);
		initScoreMultiplier();
	}

	@Override
	public void resetValues() {
		mWidgets.score.setText(mGameScene.getPlayerScore());
		mWidgets.score.pack();
		mWidgets.multiplier.setText("X" + mGameScene.getPlayerMultiplier());
		mWidgets.multiplier.invalidateHierarchy();

		mMainTable.pack();
	}

	/**
	 * Initializes the score and multiplier
	 */
	public void initScoreMultiplier() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		LabelStyle labelStyle = skin.get("default", LabelStyle.class);


		// Score
		Label label = new Label("Score: ", labelStyle);
		mMainTable.add(label).setPadRight(Config.Gui.SEPARATE_PADDING);

		label = new Label("", labelStyle);
		mWidgets.score = label;
		mMainTable.add(label);


		// Multiplier
		mMainTable.row();
		label = new Label("X1", labelStyle);
		mWidgets.multiplier = label;
		mMainTable.add(label);
	}

	/** GameScene object that this GUI acts on */
	private GameScene mGameScene = null;
	/** All widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	/**
	 * All the widgets which state can be changed and thus reset
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		Label score = null;
		Label multiplier = null;
	}
}
