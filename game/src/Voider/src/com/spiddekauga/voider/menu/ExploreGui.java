package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Pools;

/**
 * GUI for explore scene
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreGui extends Gui {

	/**
	 * Sets the explore scene
	 * @param exploreScene the explore scene
	 */
	void setExploreScene(ExploreScene exploreScene) {
		mExploreScene = exploreScene;
	}

	@Override
	public void initGui() {
		super.initGui();

		mWidgets = new Widgets();

		initViewButtons();
		initSort();
		initTopBar();
	}

	/**
	 * Initializes the top bar
	 */
	private void initTopBar() {
		mWidgets.topBar = new Background((Color) SkinNames.getResource(SkinNames.General.BAR_UPPER_LOWER_COLOR));
		mWidgets.topBar.setHeight((Float) SkinNames.getResource(SkinNames.General.BAR_UPPER_LOWER_HEIGHT));
		mWidgets.topBar.setWidth(Gdx.graphics.getWidth());
		getStage().addActor(mWidgets.topBar);
		mWidgets.topBar.setZIndex(0);
		mWidgets.topBar.setPosition(0, Gdx.graphics.getHeight() - mWidgets.topBar.getHeight());
	}

	/**
	 * Initializes different view buttons
	 */
	private void initViewButtons() {
		mWidgets.view.sortHider = new HideListener(true);
		mWidgets.view.searchHider = new HideListener(true);
	}

	/**
	 * Initializes sort buttons
	 */
	private void initSort() {
		float paddingDefault = SkinNames.getResource(SkinNames.General.PADDING_DEFAULT);
		float paddingSeparator = SkinNames.getResource(SkinNames.General.PADDING_SEPARATOR);

		AlignTable table = mWidgets.sort.table;
		table.setCellPaddingDefault(paddingDefault, paddingDefault, paddingDefault, paddingSeparator);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		//		mWidgets.view.sortHider.addToggleActor(table);
		getStage().addActor(table);

		CheckBoxStyle radioStyle = SkinNames.getResource(SkinNames.General.CHECK_BOX_RADIO);
		ButtonGroup buttonGroup = new ButtonGroup();

		// New
		CheckBox checkBox = new CheckBox("New", radioStyle);
		mWidgets.sort.newest = checkBox;
		buttonGroup.add(checkBox);
		table.add(checkBox);
		new ButtonListener(checkBox) {
			@Override
			protected void onPressed() {
				mExploreScene.fetchLevels(SortOrders.NEWEST, getSelectedTags());
			}
		};

		// Rating
		checkBox = new CheckBox("Rating", radioStyle);
		mWidgets.sort.rating = checkBox;
		buttonGroup.add(checkBox);
		table.add(checkBox);
		new ButtonListener(checkBox) {
			@Override
			protected void onPressed() {
				mExploreScene.fetchLevels(SortOrders.RATING, getSelectedTags());
			}
		};

		// Likes
		checkBox = new CheckBox("Likes", radioStyle);
		mWidgets.sort.likes = checkBox;
		buttonGroup.add(checkBox);
		table.add(checkBox);
		new ButtonListener(checkBox) {
			@Override
			protected void onPressed() {
				mExploreScene.fetchLevels(SortOrders.LIKES, getSelectedTags());
			}
		};

		// Plays
		checkBox = new CheckBox("Plays", radioStyle);
		mWidgets.sort.plays = checkBox;
		buttonGroup.add(checkBox);
		table.add(checkBox);
		new ButtonListener(checkBox) {
			@Override
			protected void onPressed() {
				mExploreScene.fetchLevels(SortOrders.PLAYS, getSelectedTags());
			}
		};
	}

	/**
	 * @return all selected tags
	 */
	private ArrayList<Tags> getSelectedTags() {
		@SuppressWarnings("unchecked")
		ArrayList<Tags> tags = Pools.arrayList.obtain();

		for (Button selectedButton : mWidgets.tag.buttonGroup.getAllChecked()) {
			tags.add(mWidgets.tag.buttonTag.get(selectedButton));
		}

		return tags;
	}

	/**
	 * Initializes search bar
	 */
	private void initSearchBar() {

	}

	/**
	 * Initializes info panel
	 */
	private void initInfo() {

	}

	/**
	 * Initializes comments
	 */
	private void initComments() {

	}

	/**
	 * Initializes action buttons
	 */
	private void initActions() {

	}

	/**
	 * Initializes tags
	 */
	private void initTags() {

	}

	/**
	 * Initialize level
	 */
	private void initLevel() {

	}

	/** The explore scene */
	private ExploreScene mExploreScene = null;
	/** Widgets */
	private Widgets mWidgets = null;

	/**
	 * All widgets
	 */
	@SuppressWarnings("javadoc")
	private static class Widgets {
		View view = new View();
		Sort sort = new Sort();
		Info info = new Info();
		Comments comment = new Comments();
		Tag tag = new Tag();
		Search search = new Search();
		Action action = new Action();
		Level level = new Level();
		Background topBar = null;

		private static class View {
			Button sort = null;
			GuiHider sortHider = null;
			Button search = null;
			GuiHider searchHider = null;
			AlignTable table = new AlignTable();
		}

		private static class Level {
			AlignTable table = new AlignTable();
		}

		private static class Sort {
			Button newest = null;
			Button plays = null;
			Button rating = null;
			Button likes = null;
			AlignTable table = new AlignTable();
		}

		private static class Info {
			AlignTable table = new AlignTable();
			Label name = null;
			Label description = null;
			Image ratingFilled = null;
			Image ratingEmpty = null;
			Label creator = null;
			Label originalCreator = null;
			Label date = null;
			Label plays = null;
			Label likes = null;
			Label tags = null;
		}

		private static class Comments {
			AlignTable table = new AlignTable();
		}

		private static class Tag {
			Button toggle = null;
			Button clear = null;
			ButtonGroup buttonGroup = new ButtonGroup();
			ArrayList<Button> all = new ArrayList<>();
			HashMap<Button, Tags> buttonTag = new HashMap<>();

			{
				buttonGroup.setMinCheckCount(0);
				buttonGroup.setMaxCheckCount(5);
			}
		}

		private static class Search {
			TextField field = null;
		}

		private static class Action {
			Button menu = null;
			Button play = null;
		}
	}
}
