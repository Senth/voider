package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.network.stat.HighscoreEntity;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.utils.scene.ui.Gui;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

import java.util.ArrayList;

/**
 * GUI for the highscore scene
 */
public class HighscoreGui extends Gui {
/** Highscore scene */
private HighscoreScene mScene = null;
/** Widgets */
private InnerWidgets mWidgets = new InnerWidgets();

;
/** Placement width */
private float mPlacementWidth = 0;
/** Row height of scores */
private float mRowHegiht = 0;

/**
 * Sets the highscore scene
 * @param scene highscore scene
 */
void setHighscoreScene(HighscoreScene scene) {
	mScene = scene;
}

@Override
public void onCreate() {
	super.onCreate();

	initVars();
	initScoreTable();
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
	mUiFactory.text.addHeader("Global Highscores", mMainTable);

	// First place
	mMainTable.row().setFillWidth(true).setHeight(mRowHegiht);

	mUiFactory.text.add("1.", mMainTable);
	mMainTable.getCell().setWidth(mPlacementWidth);

	mWidgets.firstPlaceName = mUiFactory.text.add("Loading...", mMainTable);
	mMainTable.add().setFillWidth(true);

	mWidgets.firstPlaceScore = mUiFactory.text.add("...", mMainTable);


	// User tables
	mMainTable.row().setFillWidth(true);
	mMainTable.add(mWidgets.scoreTable).setFillWidth(true);

	// Continue button
	mMainTable.row().setPadBottom(0);
	ButtonListener listener = new ButtonListener() {
		@Override
		protected void onPressed(Button button) {
			mScene.endScene();
		}
	};
	mUiFactory.button.addText("Continue", TextButtonStyles.FILLED_PRESS, mMainTable, listener, null, null);
}

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
 * @param userScore player score this time
 * @param userHighscore player highscore
 * @param userPlace player placement
 * @param beforeUser scores before the player, i.e. higher scores
 * @param afterUser scores after the player, i.e. lower scores
 */
void populateUserScores(int userScore, HighscoreEntity userHighscore, int userPlace, ArrayList<HighscoreEntity> beforeUser,
						ArrayList<HighscoreEntity> afterUser) {
	mWidgets.scoreTable.dispose(true);

	// Add scores before the user
	int placement = userPlace - beforeUser.size() - 1;
	for (HighscoreEntity highscore : beforeUser) {
		placement++;
		addScoreToTable(placement, highscore.playerName, highscore.score);
	}

	// Add player score this time
	if (!mScene.isHighScoreThisTime()) {
		addScoreToTable(0, userHighscore.playerName, userScore);
	}

	// Add player score
	addScoreToTable(userPlace, userHighscore.playerName, userHighscore.score);

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

	LabelStyles labelStyle = getLabelStyle(name, placement == 0);

	table.row().setFillWidth(true).setHeight(mRowHegiht);

	// Placement
	String placementString = "";
	if (placement == 0) {
		placementString = "Now.";
	} else {
		placementString = "" + placement + ".";
	}
	mUiFactory.text.add(placementString, table, labelStyle);
	table.getCell().setWidth(mPlacementWidth);

	// Name
	mUiFactory.text.add(name, table, labelStyle);
	table.add().setFillWidth(true);

	// Score
	String scoreString = PlayerStats.formatScore(score);
	mUiFactory.text.add(scoreString, table, labelStyle);
}

/**
 * Get the correct style for the labels
 * @param name player name for the score
 * @param localOnly true if this score is local only
 * @return the LabelStyle to use for the score
 */
private LabelStyles getLabelStyle(String name, boolean localOnly) {
	LabelStyles labelStyle = LabelStyles.DEFAULT;

	// It's this user
	if (name.equals(User.getGlobalUser().getUsername()) || localOnly) {

		if (mScene.isHighScoreThisTime()) {
			labelStyle = LabelStyles.HIGHLIGHT;
		} else {
			if (localOnly) {
				labelStyle = LabelStyles.HIGHLIGHT;
			} else {
				labelStyle = LabelStyles.WARNING;
			}
		}
	}

	return labelStyle;
}

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
