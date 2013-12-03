package com.spiddekauga.voider.app;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;


/**
 * GUI for main menu
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class MainMenuGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setTableAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setRowAlign(Horizontal.CENTER, Vertical.TOP);
		mMainTable.setCellPaddingDefault(Config.Gui.PADDING_DEFAULT);
		initMainMenu();
	}

	/**
	 * Sets the scene for this GUI
	 * @param mainMenuScene
	 */
	public void setMainMenu(MainMenu mainMenuScene) {
		mMainMenuScene = mainMenuScene;
	}

	@Override
	public void resetValues() {
		if (mMainMenuScene.hasResumeGame()) {
			mMenu.resume.setDisabled(false);
		} else {
			mMenu.resume.setDisabled(true);
		}
	}

	/**
	 * Initializes the main menu
	 */
	private void initMainMenu() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textPressStyle = skin.get(SkinNames.General.TEXT_BUTTON_PRESS.toString(), TextButtonStyle.class);

		@SuppressWarnings("unchecked")
		ArrayList<Button> buttons = Pools.arrayList.obtain();

		// Resume
		Button button = new TextButton("Resume", textPressStyle);
		buttons.add(button);
		mMenu.resume = button;
		mMainTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mMainMenuScene.resumeGame();
			}
		};

		// Campaign
		button = new TextButton("Campaign", textPressStyle);
		buttons.add(button);
		mMainTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mMainMenuScene.gotoCampaignMenu();
			}
		};

		// Downloaded content
		mMainTable.row();
		button = new TextButton("User content", textPressStyle);
		buttons.add(button);
		mMainTable.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, "User content", Messages.Tooltip.Menus.Main.DOWNLOADED);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mMainMenuScene.gotoDownloadedContentMenu();
			}
		};

		// Explore
		button = new TextButton("Explore", textPressStyle);
		buttons.add(button);
		mMainTable.add(button);
		tooltipListener = new TooltipListener(button, "Explore", Messages.Tooltip.Menus.Main.EXPLORE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mMainMenuScene.gotoExploreMenu();
			}
		};

		// Editor
		mMainTable.row();
		button = new TextButton("Editor", textPressStyle);
		buttons.add(button);
		mMainTable.add(button);
		tooltipListener = new TooltipListener(button, "Editor", Messages.Tooltip.Menus.Main.EDITOR);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mMainMenuScene.gotoEditor();
			}
		};

		// Options
		button = new TextButton("Options", textPressStyle);
		buttons.add(button);
		mMainTable.add(button);
		Skin tooltipSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_TOOLTIPS);
		Image image = new Image(tooltipSkin, SkinNames.EditorTooltips.ACTOR_COLLISION_DAMAGE.toString());
		tooltipListener = new TooltipListener(button, "Options", image, "Testing options tooltip", "http://youtube.com");
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mMainMenuScene.gotoOptions();
			}
		};


		// Set same size on all buttons
		float maxWidth = 0;
		for (Button currentButton : buttons) {
			if (currentButton.getPrefWidth() > maxWidth) {
				maxWidth = currentButton.getPrefWidth();
			}
		}

		for (Button currentButton : buttons) {
			currentButton.setWidth(maxWidth);
		}

		Pools.arrayList.free(buttons);
	}

	/** Main menu scene */
	private MainMenu mMainMenuScene = null;
	/** Menu buttons */
	private MenuButtons mMenu = new MenuButtons();

	/**
	 * Menu buttons
	 */
	@SuppressWarnings("javadoc")
	private static class MenuButtons {
		Button resume = null;
	}

}
