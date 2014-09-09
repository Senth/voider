package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.network.entities.stat.HighscoreEntity;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.User;

/**
 * GUI for the highscore scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HighscoreSceneGui extends Gui {
	/**
	 * Sets the highscore scene
	 * @param scene highscore scene
	 */
	void setHighscoreScene(HighscoreScene scene) {
		mScene = scene;
	}

	@Override
	public void initGui() {
		super.initGui();

		initVars();
		initScoreTable();
	};

	/**
	 * Set first score
	 * @param firstPlace score for the first place
	 */
	void setFirstPlace(HighscoreEntity firstPlace) {
		mWidgets.firstPlaceName.setText(firstPlace.playerName);
		String scoreText = PlayerStats.formatScore(firstPlace.score);
		mWidgets.firstPlaceScore.setText(scoreText);
	}

	/**
	 * Populate user scores
	 * @param userScore player score
	 * @param userPlace player placement
	 * @param beforeUser scores before the player, i.e. higher scores
	 * @param afterUser scores after the player, i.e. lower scores
	 */
	void populateUserScores(HighscoreEntity userScore, int userPlace, ArrayList<HighscoreEntity> beforeUser, ArrayList<HighscoreEntity> afterUser) {
		mWidgets.scoreTable.dispose(true);

		// Add scores before the user
		int placement = userPlace - beforeUser.size() - 1;
		for (HighscoreEntity highscore : beforeUser) {
			placement++;
			addScoreToTable(placement, highscore.playerName, highscore.score);
		}

		// Add player score
		addScoreToTable(userPlace, userScore.playerName, userScore.score);

		// Add scores after the user
		placement = userPlace;
		for (HighscoreEntity highscore : afterUser) {
			placement++;
			addScoreToTable(placement, highscore.playerName, highscore.score);
		}
	}

	/**
	 * Add a score to score table
	 * @param placement what place the score is
	 * @param name name of the player
	 * @param score score of the player
	 */
	private void addScoreToTable(int placement, String name, int score) {
		AlignTable table = mWidgets.scoreTable;

		LabelStyle labelStyle;
		if (name.equals(User.getGlobalUser().getUsername())) {
			labelStyle = mUiFactory.getStyles().label.highlight;
		} else {
			labelStyle = mUiFactory.getStyles().label.standard;
		}

		table.row().setFillWidth(true).setHeight(mRowHegiht);

		// Placement
		Label label = new Label("" + placement + ".", labelStyle);
		table.add(label).setWidth(mPlacementWidth);

		// Name
		label = new Label(name, labelStyle);
		table.add(label);
		table.add().setFillWidth(true);

		// Score
		String scoreString = PlayerStats.formatScore(score);
		label = new Label(scoreString, labelStyle);
		table.add(label);
	}

	/**
	 * Initialize variables
	 */
	private void initVars() {
		mRowHegiht = SkinNames.getResource(SkinNames.GeneralVars.SCORE_LABEL_HEIGHT);
		mPlacementWidth = SkinNames.getResource(SkinNames.GeneralVars.HIGHSCORE_PLACEMENT_WIDTH);
	}

	/**
	 * Initialize score table
	 */
	private void initScoreTable() {
		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setKeepWidth(true);
		mMainTable.setWidth((float) SkinNames.getResource(SkinNames.GeneralVars.HIGHSCORE_SCREEN_WIDTH));
		mMainTable.setPaddingRowDefault(mUiFactory.getStyles().vars.paddingInner, 0, 0, 0);

		// Level highscores label
		mUiFactory.addHeader("Level Scores", mMainTable);

		// First place
		mMainTable.row().setFillWidth(true).setHeight(mRowHegiht);

		mUiFactory.addLabel("1.", false, mMainTable);
		mMainTable.getCell().setWidth(mPlacementWidth);

		mWidgets.firstPlaceName = mUiFactory.addLabel("Loading...", false, mMainTable);
		mMainTable.add().setFillWidth(true);

		mWidgets.firstPlaceScore = mUiFactory.addLabel("...", false, mMainTable);


		// User tables
		mMainTable.row().setFillWidth(true);
		mMainTable.add(mWidgets.scoreTable).setFillWidth(true);

		// Continue button
		mMainTable.row().setPadBottom(0);
		Button button = mUiFactory.addImageButtonLabel(SkinNames.General.GAME_CONTINUE, "Continue", Positions.BOTTOM, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScene.continueToNextScene();
			}
		};
	}

	/** Highscore scene */
	private HighscoreScene mScene = null;
	/** Widgets */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** Placement width */
	private float mPlacementWidth = 0;
	/** Row height of scores */
	private float mRowHegiht = 0;

	@SuppressWarnings("javadoc")
	private class InnerWidgets {
		// First place
		Label firstPlaceScore = null;
		Label firstPlaceName = null;

		// User score and those above/below
		AlignTable scoreTable = new AlignTable();

		{
			scoreTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		}
	}
}
