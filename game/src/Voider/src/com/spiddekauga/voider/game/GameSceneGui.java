package com.spiddekauga.voider.game;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the GameScene
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

		mMainTable.setAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setCellPaddingDefault((Float)SkinNames.getResource(SkinNames.General.PADDING_DEFAULT));

		if (!mGameScene.isPlayerInvulnerable()) {
			initHealthBar();
		}

		initScoreMultiplier();

		if (mGameScene.isTestRun()) {
			initTestRunOptionBar();
		}
	}

	@Override
	public void resetValues() {
		mWidgets.score.setText(mGameScene.getPlayerScore());
		mWidgets.score.pack();
		mWidgets.multiplier.setText("X" + mGameScene.getPlayerMultiplier());
		mWidgets.multiplier.invalidateHierarchy();
		mWidgets.health.setValue(mGameScene.getPercentageHealth());

		mMainTable.pack();
	}

	/**
	 * Initializes test run options
	 */
	private void initTestRunOptionBar() {
		mMainTable.setAlign(Horizontal.CENTER, Vertical.TOP);
	}

	/**
	 * Initializes the health bar
	 */
	public void initHealthBar() {
		SliderStyle healthBarStyle = SkinNames.getResource(SkinNames.Game.HEALTH_BAR);

		Slider slider = new Slider(0, 1, 0.01f, false, healthBarStyle);
		mWidgets.health = slider;
		mMainTable.row();
		mMainTable.add(slider);
	}

	/**
	 * Initializes the score and multiplier
	 */
	public void initScoreMultiplier() {
		LabelStyle labelStyle = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);

		mMainTable.row();

		// Score
		Label label = new Label("Score: ", labelStyle);
		mMainTable.add(label).setPadRight((Float)SkinNames.getResource(SkinNames.General.PADDING_SEPARATOR));

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
	/** Options bar when test running the level */
	private AlignTable mOptionBar = new AlignTable();
	/** All widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	/**
	 * All the widgets which state can be changed and thus reset
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		Label score = null;
		Label multiplier = null;
		Slider health = null;
	}
}
