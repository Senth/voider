package com.spiddekauga.voider.scene;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.utils.Messages;

/**
 * GUI for the game over screen
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class GameOverSceneGui extends Gui {
	/**
	 * Sets the game over scene
	 * @param gameOverScene scene to get information from
	 */
	void setGameOverScene(GameOverScene gameOverScene) {
		mGameOverScene = gameOverScene;
	}

	@Override
	public void reset() {
		mMainTable.dispose(true);
	}

	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setPaddingCellDefault(2, 2, 2, 2);
		mMainTable.row().setFillHeight(true);


		Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		LabelStyle labelStyle = skin.get("default", LabelStyle.class);
		TextButtonStyle buttonStyle = skin.get("default", TextButtonStyle.class);

		// Header (Game over / congratulations)
		mMainTable.row();
		Label header = null;
		if (mGameOverScene.isLevelCompleted()) {
			/** @todo use larger font size for header */
			header = new Label(Messages.Level.COMPLETED_HEADER, skin);
		} else {
			/** @todo use larger font size for header */
			header = new Label(Messages.Level.GAME_OVER_HEADER, skin);
		}
		mMainTable.add(header);


		// Padding - Score table
		mMainTable.row().setFillHeight(true);
		mMainTable.row();
		Label label = new Label("Scoring", labelStyle);
		mMainTable.add(label);

		// Score
		mMainTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		mMainTable.row();
		label = new Label("Score:", labelStyle);
		mMainTable.add(label).setWidth(Config.Gui.SCORE_TABLE_FIRST_CELL_WIDTH);
		label = new Label(mGameOverScene.getPlayerScore(), labelStyle);
		mMainTable.add(label);


		// Buttons
		mMainTable.row().setFillHeight(true);
		mMainTable.row().setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		boolean tryAgain = true;
		/** @todo check if level was completed and was running a campaign */

		if (tryAgain) {
			TextButton button = new TextButton("Try again", buttonStyle);
			mMainTable.add(button);
			button.addListener(new ButtonListener(button) {
				@Override
				protected void onPressed() {
					mGameOverScene.tryAgain();
				}
			});

			button = new TextButton("Main Menu", buttonStyle);
			mMainTable.add(button);
			button.addListener(new ButtonListener(button) {
				@Override
				protected void onPressed() {
					mGameOverScene.gotoMainMenu();
				}
			});
		} else {
			TextButton button = new TextButton("Continue", buttonStyle);
			mMainTable.add(button);
			button.addListener(new ButtonListener(button) {
				@Override
				protected void onPressed() {
					mGameOverScene.gotoMainMenu();
				}
			});
		}
	}

	/** Used for getting information about the scene */
	GameOverScene mGameOverScene = null;
}
