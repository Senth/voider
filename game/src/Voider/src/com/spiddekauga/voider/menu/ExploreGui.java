package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;
import com.spiddekauga.utils.scene.ui.RatingWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
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

		initRightPanel();
		initViewButtons();
		initSort();
		initSearchBar();
		initComments();
		initInfo();
		initLevel();
		initTags();
		initActions();
		initTopBar();
	}

	/**
	 * Initializes the top bar
	 */
	private void initTopBar() {
		mWidgets.topBar = new Background((Color) SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_COLOR));
		mWidgets.topBar.setHeight((Float) SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT));
		mWidgets.topBar.setWidth(Gdx.graphics.getWidth());
		getStage().addActor(mWidgets.topBar);
		mWidgets.topBar.setZIndex(0);
		mWidgets.topBar.setPosition(0, Gdx.graphics.getHeight() - mWidgets.topBar.getHeight());
	}

	/**
	 * Initializes different view buttons
	 */
	private void initViewButtons() {
		// Create button menu
		AlignTable table = mWidgets.view.table;
		table.dispose(true);
		table.setAlign(Horizontal.LEFT, Vertical.TOP);
		getStage().addActor(table);
		ButtonGroup buttonGroup = new ButtonGroup();

		// Sort
		// TODO replace stub with sort button
		Button button = new ImageButton((ImageButtonStyle) SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_STUB_TOGGLE));
		table.add(button);
		buttonGroup.add(button);
		mWidgets.view.sort = button;
		mWidgets.view.sortHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				SortOrders sortOrder = getSelectedSortOrder();
				if (sortOrder != null) {
					mExploreScene.fetchLevels(sortOrder, getSelectedTags());
				}
			}
		};


		// Search
		// TODO replace stub with search button
		button = new ImageButton((ImageButtonStyle) SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_STUB_TOGGLE));
		table.add(button);
		buttonGroup.add(button);
		mWidgets.view.search = button;
		mWidgets.view.searchHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mExploreScene.fetchLevels(mWidgets.search.field.getText());
			}
		};
	}

	/**
	 * @return selected sort order
	 */
	private SortOrders getSelectedSortOrder() {
		for (SortOrders sortOrder : SortOrders.values()) {
			if (mWidgets.sort.buttons[sortOrder.ordinal()] != null) {
				if (mWidgets.sort.buttons[sortOrder.ordinal()].isChecked()) {
					return sortOrder;
				}
			}
		}

		return null;
	}

	/**
	 * Initializes sort buttons
	 */
	private void initSort() {
		float paddingSeparator = SkinNames.getResource(SkinNames.GeneralVars.PADDING_SEPARATOR);

		AlignTable table = mWidgets.sort.table;
		table.dispose(true);
		table.setPaddingCellDefault(0, 0, 0, paddingSeparator);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		mWidgets.view.sortHider.addToggleActor(table);
		getStage().addActor(table);

		CheckBoxStyle radioStyle = SkinNames.getResource(SkinNames.General.CHECK_BOX_RADIO);
		ButtonGroup buttonGroup = new ButtonGroup();

		// Create buttons
		for (final SortOrders sortOrder : SortOrders.values()) {
			CheckBox checkBox = new CheckBox(sortOrder.toString(), radioStyle);
			mWidgets.sort.buttons[sortOrder.ordinal()] = checkBox;
			buttonGroup.add(checkBox);
			table.add(checkBox);
			new ButtonListener(checkBox) {
				@Override
				protected void onPressed() {
					mExploreScene.fetchLevels(sortOrder, getSelectedTags());
				}
			};
		}
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
		AlignTable table = mWidgets.search.table;
		table.dispose(true);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		getStage().addActor(table);
		mWidgets.view.searchHider.addToggleActor(table);

		TextField textField = new TextField("", (TextFieldStyle) SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT));
		textField.setMaxLength(Config.Editor.NAME_LENGTH_MAX);
		table.add(textField);
		mWidgets.search.field = textField;
		new TextFieldListener(textField, "Search", null) {
			@Override
			protected void onChange(String newText) {
				mExploreScene.fetchLevels(newText);
			}
		};
	}

	/**
	 * Initialize the right panel
	 */
	private void initRightPanel() {
		float topBottomMargin = SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT);
		topBottomMargin += (Float) SkinNames.getResource(SkinNames.GeneralVars.PADDING_BELOW_ABOVE_BAR);

		AlignTable table = mWidgets.rightPanel;
		table.setMargin(topBottomMargin, 0, topBottomMargin, 0);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		table.setName("right-panel");
		getStage().addActor(table);
	}

	/**
	 * Initializes info panel
	 */
	private void initInfo() {
		LabelStyle labelStyle = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);
		Skin generalSkin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		Color widgetBackgroundColor = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR);


		float infoWidth = SkinNames.getResource(SkinNames.GeneralVars.INFO_BAR_WIDTH);
		AlignTable table = mWidgets.info.table;
		table.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
		table.setAlignRow(Horizontal.LEFT, Vertical.TOP);
		table.setBackgroundImage(new Background(widgetBackgroundColor));
		table.setKeepWidth(true).setWidth(infoWidth);
		table.setName("info-table");
		mWidgets.rightPanel.row().setFillHeight(true);
		mWidgets.rightPanel.add(table).setFillHeight(true);


		// Name
		Label label = new Label("", labelStyle);
		mWidgets.info.name = label;
		table.row(Horizontal.CENTER, Vertical.TOP);
		table.add(label);


		// Rating
		// TODO replace rating star icons
		Drawable filledStar = generalSkin.getDrawable(SkinNames.General.STUB.toString());
		Drawable emptyStar = generalSkin.getDrawable(SkinNames.General.STUB.toString());
		RatingWidget rating = new RatingWidget(filledStar, emptyStar, 5, Touchable.disabled);
		mWidgets.info.rating = rating;
		rating.setName("rating");
		table.row(Horizontal.CENTER, Vertical.TOP);
		table.add(rating);


		// Description
		label = new Label("", labelStyle);
		mWidgets.info.description = label;
		label.setWrap(true);
		table.row(Horizontal.CENTER, Vertical.TOP);
		table.add(label);


		// Created by
		label = new Label("Created by", labelStyle);
		table.row();
		table.add(label);

		// TODO replace player icon
		Drawable playerIcon = generalSkin.getDrawable(SkinNames.General.STUB.toString());
		Image image = new Image(playerIcon);
		table.row();
		table.add(image);

		label = new Label("", labelStyle);
		mWidgets.info.createdBy = label;
		table.add(label);


		// Revised by
		label = new Label("Revised by", labelStyle);
		table.row();
		table.add(label);

		image = new Image(playerIcon);
		table.row();
		table.add(image);

		label = new Label("", labelStyle);
		mWidgets.info.revisedBy = label;
		table.add(label);


		// Date
		// TODO replace date icon
		Drawable dateIcon = generalSkin.getDrawable(SkinNames.General.STUB.toString());
		image = new Image(dateIcon);
		table.row();
		table.add(image);

		label = new Label("", labelStyle);
		mWidgets.info.date = label;
		table.add(label);


		// Plays
		// TODO replace number of plays icon
		Drawable playsIcon = generalSkin.getDrawable(SkinNames.General.STUB.toString());
		image = new Image(playsIcon);
		table.row();
		table.add(image);

		label = new Label("", labelStyle);
		mWidgets.info.plays = label;
		table.add(label);


		// Likes
		// TODO replace number of likes icon
		Drawable likesIcon = generalSkin.getDrawable(SkinNames.General.STUB.toString());
		image = new Image(likesIcon);
		table.row();
		table.add(image);

		label = new Label("", labelStyle);
		mWidgets.info.likes = label;
		table.add(label);


		// Tags
		// TODO replace tag icon
		Drawable tagIcon = generalSkin.getDrawable(SkinNames.General.STUB.toString());
		image = new Image(tagIcon);
		table.row();
		table.add(image);

		label = new Label("", labelStyle);
		mWidgets.info.tags = label;
		table.add(label);

		table.row().setFillHeight(true).setFillWidth(true);
		table.add().setFillHeight(true).setFillWidth(true);
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
		AlignTable rightPanel = new AlignTable();

		private static class View {
			Button sort = null;
			HideListener sortHider = null;
			Button search = null;
			HideListener searchHider = null;
			AlignTable table = new AlignTable();
		}

		private static class Level {
			AlignTable table = new AlignTable();
		}

		private static class Sort {
			Button[] buttons = new Button[SortOrders.values().length];
			AlignTable table = new AlignTable();
		}

		private static class Info {
			AlignTable table = new AlignTable();
			Label name = null;
			Label description = null;
			RatingWidget rating = null;
			Label revisedBy = null;
			Label createdBy = null;
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
			AlignTable table = new AlignTable();
		}

		private static class Action {
			Button menu = null;
			Button play = null;
		}
	}
}
