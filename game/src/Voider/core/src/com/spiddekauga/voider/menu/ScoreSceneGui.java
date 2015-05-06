package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;

/**
 * GUI for the game over screen
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ScoreSceneGui extends Gui {
	/**
	 * Sets the game over scene
	 * @param gameOverScene scene to get information from
	 */
	void setScoreScene(ScoreScene gameOverScene) {
		mScoreScene = gameOverScene;
	}

	@Override
	public void resetValues() {
		super.resetValues();

		if (mScoreScene.isPublished()) {
			mWidgets.bookmark.setChecked(mScoreScene.isBookmarked());
			mWidgets.rating.setRating(mScoreScene.getRating());
			// mWidgets.comment.setText(mScoreScene.getComment());
		}
	};

	@Override
	public void initGui() {
		super.initGui();

		initScoreTable();
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
		LabelStyle myScoreStyle = SkinNames.getResource(SkinNames.General.LABEL_MY_SCORE);
		addToScoreTable("My Score", myScoreStyle, mScoreScene.getPlayerScore());


		if (mScoreScene.isPublished()) {
			// My top score
			LabelStyle myHighscore = SkinNames.getResource(SkinNames.General.LABEL_TOP_SCORE);
			addToScoreTable("My Highscore", myHighscore, mScoreScene.getPlayerHighscore());


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
		button = mUiFactory.button.addImageWithLabel(SkinNames.General.GAME_CONTINUE, "Continue", Positions.BOTTOM, null, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScoreScene.gotoMainMenu();
			}
		};
	}

	/**
	 * Add a text (left aligned) and then another actor (right aligned) to the score table
	 * @param text label text
	 * @param labelStyle (optional) style for the text, if null it will use the default
	 *        label style
	 * @param actor the actor to add as right aligned
	 */
	private void addToScoreTable(String text, LabelStyle labelStyle, Actor actor) {
		LabelStyle usesLabelStyle = labelStyle;

		if (usesLabelStyle == null) {
			usesLabelStyle = LabelStyles.DEFAULT.getStyle();
		}

		float rowHeight = SkinNames.getResource(SkinNames.GeneralVars.SCORE_LABEL_HEIGHT);

		Label label = new Label(text, usesLabelStyle);
		mMainTable.row().setFillWidth(true).setHeight(rowHeight);
		mMainTable.add(label);
		mMainTable.add().setFillWidth(true);
		mMainTable.add(actor);
	}

	/**
	 * Add a text (left aligned) and then another actor (right aligned) to the score table
	 * @param leftText label text
	 * @param labelStyle (optional) style for both the left and right text, if null it
	 *        will use the default label style
	 * @param rightText text for the right side
	 */
	private void addToScoreTable(String leftText, LabelStyle labelStyle, String rightText) {
		LabelStyle usesLabelStyle = labelStyle;

		if (usesLabelStyle == null) {
			usesLabelStyle = LabelStyles.DEFAULT.getStyle();
		}

		Label label = new Label(rightText, usesLabelStyle);
		addToScoreTable(leftText, usesLabelStyle, label);
	}

	/** Used for getting information about the scene */
	private ScoreScene mScoreScene = null;
	/** Inner widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	private class InnerWidgets {
		RatingWidget rating = null;
		Button bookmark = null;
		// TextArea comment = null;
	}
}
