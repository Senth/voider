package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.IRatingListener;
import com.spiddekauga.utils.scene.ui.RatingWidget;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.utils.scene.ui.Gui;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;

/**
 * GUI for the game over screen
 */
public class ScoreSceneGui extends Gui {
/** Used for getting information about the scene */
private ScoreScene mScoreScene = null;
/** Inner widgets */
private InnerWidgets mWidgets = new InnerWidgets();

;

/**
 * Sets the game over scene
 * @param gameOverScene scene to get information from
 */
void setScoreScene(ScoreScene gameOverScene) {
	mScoreScene = gameOverScene;
}

@Override
public void onCreate() {
	super.onCreate();

	initScoreTable();
}

@Override
public void resetValues() {
	super.resetValues();

	if (mScoreScene.isPublished()) {
		mWidgets.bookmark.setChecked(mScoreScene.isBookmarked());
		mWidgets.rating.setRating(mScoreScene.getRating());
		// mWidgets.comment.setText(mScoreScene.getComment());
	}
}

/**
 * Initialize score table
 */
private void initScoreTable() {
	mMainTable.setAlignTable(Horizontal.CENTER, Vertical.MIDDLE);
	mMainTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);

	float tableWidth = SkinNames.getResource(SkinNames.GeneralVars.SCORE_SCREEN_WIDTH);
	mMainTable.setKeepWidth(true).setWidth(tableWidth);


	// My Score
	addToScoreTable("My Score", mScoreScene.getPlayerScore());


	if (mScoreScene.isPublished()) {
		// My top score
		addToScoreTable("My Highscore", mScoreScene.getPlayerHighscore());


		// Rate
		mMainTable.row().setFillWidth(true).setPadTop(mUiFactory.getStyles().vars.paddingInner);
		mUiFactory.text.add("Rate", mMainTable);
		mMainTable.add().setFillWidth(true);
		RatingWidget ratingWidget = mUiFactory.addRatingWidget(Touchable.enabled, mMainTable, null);
		mWidgets.rating = ratingWidget;
		IRatingListener ratingListener = new IRatingListener() {
			@Override
			public void onRatingChange(int newRating) {
				mScoreScene.setRating(newRating);
			}
		};
		ratingWidget.addListener(ratingListener);

		// Bookmark
		mMainTable.row().setFillWidth(true);
		mUiFactory.text.add("Bookmark", mMainTable);
		mMainTable.add().setFillWidth(true);
		Button button = mUiFactory.button.addImage(SkinNames.General.BOOKMARK, mMainTable, null, null);
		mWidgets.bookmark = button;
		new ButtonListener(button) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScoreScene.setBookmark(checked);
			}
		};

		// // Comment
		// TextFieldListener textFieldListener = new TextFieldListener() {
		// @Override
		// protected void onDone(String newText) {
		// mScoreScene.setComment(newText);
		// }
		// };
		// mWidgets.comment = mUiFactory.addTextArea(null, false,
		// "Comment on the level you just played", tableWidth, textFieldListener,
		// mMainTable,
		// null);
	} else {
		String text = "Highscore, rate and bookmark are available for published levels :)";
		mMainTable.row().setPadTop(mUiFactory.getStyles().vars.rowHeight);
		mUiFactory.text.add(text, true, mMainTable);
		mMainTable.getCell().setWidth(tableWidth);
	}

	// -- Buttons --
	// Replay
	mMainTable.row().setAlign(Horizontal.CENTER).setPadTop(mUiFactory.getStyles().vars.rowHeight);
	Button button = mUiFactory.button.addImageWithLabel(SkinNames.General.REPLAY, "Replay", Positions.BOTTOM, null, mMainTable, null, null);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mScoreScene.tryAgain();
		}
	};

	// Continue
	mUiFactory.button.addPadding(mMainTable);
	button = mUiFactory.button.addImageWithLabel(SkinNames.General.CONTINUE, "Continue", Positions.BOTTOM, null, mMainTable, null, null);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mScoreScene.gotoMainMenu();
		}
	};
}

/**
 * Add a text (left aligned) and then another actor (right aligned) to the score table
 * @param name the name that scored
 * @param score the score as text
 */
private void addToScoreTable(String name, String score) {

	float rowHeight = SkinNames.getResource(SkinNames.GeneralVars.SCORE_LABEL_HEIGHT);
	LabelStyle labelStyle = LabelStyles.HEADER.getStyle();
	Label nameLabel = new Label(name, labelStyle);
	Label scoreLabel = new Label(score, labelStyle);

	mMainTable.row().setFillWidth(true).setHeight(rowHeight);
	mMainTable.add(nameLabel);
	mMainTable.add().setFillWidth(true);
	mMainTable.add(scoreLabel);
}

private class InnerWidgets {
	RatingWidget rating = null;
	Button bookmark = null;
	// TextArea comment = null;
}
}
