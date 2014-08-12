package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.UiFactory.BarLocations;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the GameScene
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

		getStage().addActor(mOptionBar);

		mMainTable.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		mLifeTable.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		mMainTable.setAlign(Horizontal.RIGHT, Vertical.TOP);
		mLifeTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		addActor(mLifeTable);

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
		mWidgets.multiplier.setText("(X " + mGameScene.getPlayerMultiplier() + ")");
		mWidgets.multiplier.invalidateHierarchy();
		if (mWidgets.health != null) {
			mWidgets.health.setValue(mGameScene.getPercentageHealth());
		}

		if (mWidgets.screenShot != null) {
			mWidgets.screenShot.setDisabled(mGameScene.isPublished());
		}

		mMainTable.pack();
	}

	/**
	 * Initializes the lives
	 */
	public void initLives() {
		// TODO
	}

	/**
	 * Initializes test run options
	 */
	private void initTestRunOptionBar() {
		// Create buttons
		mOptionBar.setAlign(Horizontal.CENTER, Vertical.TOP);
		Button button = new ImageButton((ImageButtonStyle) SkinNames.getResource(SkinNames.EditorIcons.SCREENSHOT));
		mWidgets.screenShot = button;
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mGameScene.takeScreenshot();
			}
		};
		mOptionBar.add(button);
		if (mGameScene.isPublished()) {
			button.setDisabled(true);
		}

		// Set bar background
		mUiFactory.addBar(BarLocations.TOP, getStage());
	}

	/**
	 * Initializes the health bar
	 */
	public void initHealthBar() {
		SliderStyle healthBarStyle = SkinNames.getResource(SkinNames.Game.HEALTH_BAR);

		Slider slider = new Slider(0, 1, 0.01f, false, healthBarStyle);
		mWidgets.health = slider;
		mMainTable.add(slider);
	}

	/**
	 * Initializes the score and multiplier
	 */
	public void initScoreMultiplier() {
		LabelStyle labelStyle = mUiFactory.getStyles().label.highlight;

		// Score
		mWidgets.score = new Label("", labelStyle);
		mMainTable.add(mWidgets.score).setPadRight(mUiFactory.getStyles().vars.paddingInner);

		// Multiplier
		mWidgets.multiplier = new Label("(X 1)", labelStyle);
		mMainTable.add(mWidgets.multiplier);
	}

	/** GameScene object that this GUI acts on */
	private GameScene mGameScene = null;
	/** Options bar when test running the level */
	private AlignTable mOptionBar = new AlignTable();
	/** All widgets */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** Life table */
	private AlignTable mLifeTable = new AlignTable();

	/**
	 * All the widgets which state can be changed and thus reset
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		Label score = null;
		Label multiplier = null;
		Slider health = null;
		Button screenShot = null;
		ArrayList<Image> lives = new ArrayList<>();
	}
}
