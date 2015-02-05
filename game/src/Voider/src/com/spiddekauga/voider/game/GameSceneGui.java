package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.commands.CMusicPlay;
import com.spiddekauga.utils.commands.CSceneEnd;
import com.spiddekauga.utils.commands.CSceneSwitch;
import com.spiddekauga.utils.commands.CSequence;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.menu.SettingsScene;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene.Outcomes;
import com.spiddekauga.voider.scene.ui.UiFactory.BarLocations;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;

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

		addActor(mLifeTable);
		addActor(mOptionBar);


		// Top left - lives
		mLifeTable.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		mLifeTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		mLifeTable.setPaddingCellDefault(0, mUiFactory.getStyles().vars.paddingInner, 0, 0);

		initLives();


		// Top right - score, health
		mMainTable.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		mMainTable.setAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setAlignRow(Horizontal.RIGHT, Vertical.MIDDLE);
		mMainTable.setPaddingCellDefault(0, 0, 0, mUiFactory.getStyles().vars.paddingInner);
		initScoreMultiplier();
		if (!mGameScene.isPlayerInvulnerable()) {
			initHealthBar();
		}


		// Top debug bar
		if (mGameScene.isTestRun()) {
			initTestRunOptionBar();
		}
	}

	@Override
	public void resetValues() {
		super.resetValues();

		mWidgets.score.setText(mGameScene.getPlayerScore());
		mWidgets.score.pack();
		mWidgets.multiplier.setText("(X" + mGameScene.getPlayerMultiplier() + ")");
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
	private void initLives() {
		mWidgets.lifeFull = SkinNames.getDrawable(SkinNames.GameImages.LIFE_FILLED);
		mWidgets.lifeEmpty = SkinNames.getDrawable(SkinNames.GameImages.LIFE_EMPTY);
	}

	/**
	 * Updates the number of extra lives
	 * @param current current number of extra lives
	 * @param max maximum number of lives
	 */
	void updateLives(int current, int max) {
		boolean updateTableSize = false;

		// Create images if not enough lives
		while (mWidgets.lives.size() < max) {
			Image image = new Image(mWidgets.lifeEmpty);
			mWidgets.lives.add(image);
			updateTableSize = true;
		}

		// Remove if too many
		while (mWidgets.lives.size() > max) {
			mWidgets.lives.remove(mWidgets.lives.size() - 1);
			updateTableSize = true;
		}

		if (updateTableSize) {
			mLifeTable.dispose();
			for (Image image : mWidgets.lives) {
				mLifeTable.add(image);
			}
		}

		// Set filled lives
		for (int filledIndex = 0; filledIndex < current; ++filledIndex) {
			mWidgets.lives.get(filledIndex).setDrawable(mWidgets.lifeFull);
			mWidgets.lives.get(filledIndex).setVisible(true);
		}

		// Set empty lives
		for (int emptyIndex = current; emptyIndex < max; emptyIndex++) {
			mWidgets.lives.get(emptyIndex).setDrawable(mWidgets.lifeEmpty);
		}
	}

	/**
	 * Initializes test run options
	 */
	private void initTestRunOptionBar() {
		// Create buttons
		mOptionBar.setAlign(Horizontal.CENTER, Vertical.TOP);
		mOptionBar.setMarginTop(mUiFactory.getStyles().vars.paddingOuter);
		Button button = mUiFactory.button.addImage(SkinNames.EditorIcons.SCREENSHOT, mOptionBar, null, null);
		mWidgets.screenShot = button;
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mGameScene.takeScreenshot();
			}
		};
		if (mGameScene.isPublished()) {
			button.setDisabled(true);
		}

		// Set bar background
		mUiFactory.addBar(BarLocations.TOP, getStage());


		// Fix so score is vertically centered
		mMainTable.setMarginTop(0);
		mMainTable.layout();
		float topPad = (mUiFactory.getStyles().vars.barUpperLowerHeight - mMainTable.getHeight()) / 2;
		mMainTable.setMargin(topPad);
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
		LabelStyles labelStyle = LabelStyles.HIGHLIGHT;

		AlignTable table = mMainTable;

		// Score
		mWidgets.score = mUiFactory.text.add("", table, labelStyle);

		// Multiplier
		mWidgets.multiplier = mUiFactory.text.add("(X1)", table, labelStyle);
	}

	/**
	 * Show menu for resuming game or going back
	 */
	void showMenu() {
		MsgBoxExecuter msgBox = getFreeMsgBox(false);
		msgBox.button("Resume Game");
		msgBox.buttonRow();
		msgBox.button("Restart Game", new CSceneEnd(Outcomes.LEVEL_RESTART));
		msgBox.buttonRow();
		msgBox.button("Options", new CSceneSwitch(SettingsScene.class));
		msgBox.buttonRow();
		msgBox.button("Main Menu", new CSequence(new CSceneEnd(Outcomes.LEVEL_QUIT), new CMusicPlay(Music.TITLE, MusicInterpolations.CROSSFADE)));
		msgBox.addCancelKeys();

		showMsgBox(msgBox);
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
	private static class InnerWidgets {
		Label score = null;
		Label multiplier = null;
		Slider health = null;
		Button screenShot = null;
		ArrayList<Image> lives = new ArrayList<>();
		Drawable lifeFull = null;
		Drawable lifeEmpty = null;
	}
}
