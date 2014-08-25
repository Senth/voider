package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.IRatingListener;
import com.spiddekauga.utils.scene.ui.RatingWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;

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
	public void reset() {
		mMainTable.dispose(true);
	}

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

		// My top score
		LabelStyle myHighscore = SkinNames.getResource(SkinNames.General.LABEL_TOP_SCORE);
		addToScoreTable("My Highscore", myHighscore, mScoreScene.getPlayerHighscore());


		// Rate
		mMainTable.row().setFillWidth(true).setPadTop(mUiFactory.getStyles().vars.paddingInner);
		mUiFactory.addLabel("Rate", false, mMainTable);
		mMainTable.add().setFillWidth(true);
		RatingWidget ratingWidget = mUiFactory.addRatingWidget(Touchable.enabled, mMainTable, null);
		mWidgets.main.rating = ratingWidget;
		IRatingListener ratingListener = new IRatingListener() {
			@Override
			public void onRatingChange(int newRating) {
				mScoreScene.setRating(newRating);
			}
		};
		ratingWidget.addListener(ratingListener);

		// Tag
		mMainTable.row().setFillWidth(true);
		mUiFactory.addLabel("Tag", false, mMainTable);
		mMainTable.add().setFillWidth(true);
		Button button = mUiFactory.addImageButton(SkinNames.General.TAG, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				// TODO show tags
			}
		};

		// Bookmark
		mMainTable.row().setFillWidth(true);
		mUiFactory.addLabel("Bookmark", false, mMainTable);
		mMainTable.add().setFillWidth(true);
		button = mUiFactory.addImageButton(SkinNames.General.BOOKMARK, mMainTable, null, null);
		mWidgets.main.bookmark = button;
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				mScoreScene.setBookmark(checked);
			}
		};

		// Comment
		TextFieldListener textFieldListener = new TextFieldListener() {
			@Override
			protected void onDone(String newText) {
				mScoreScene.setComment(newText);
			}
		};
		mWidgets.main.comment = mUiFactory.addTextArea(null, "Comment on the level you just played", tableWidth, textFieldListener, mMainTable, null);


		// -- Buttons --
		// Replay
		mMainTable.row().setAlign(Horizontal.CENTER).setPadTop(mUiFactory.getStyles().vars.rowHeight);
		button = mUiFactory.addImageButtonLabel(SkinNames.General.REPLAY, "Replay", Positions.BOTTOM, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mScoreScene.tryAgain();
			}
		};

		// Continue
		mUiFactory.addButtonPadding(mMainTable);
		button = mUiFactory.addImageButtonLabel(SkinNames.General.GAME_NEW, "Continue", Positions.BOTTOM, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
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
			usesLabelStyle = mUiFactory.getStyles().label.standard;
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
			usesLabelStyle = mUiFactory.getStyles().label.standard;
		}

		Label label = new Label(rightText, usesLabelStyle);
		addToScoreTable(leftText, usesLabelStyle, label);
	}

	/** Used for getting information about the scene */
	private ScoreScene mScoreScene = null;
	/** Inner widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	@SuppressWarnings("javadoc")
	private class InnerWidgets {
		Main main = new Main();

		class Main {
			RatingWidget rating = null;
			Button bookmark = null;
			TextArea comment = null;
		}
	}
}
