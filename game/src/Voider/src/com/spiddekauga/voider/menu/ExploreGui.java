package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.AnimationWidget;
import com.spiddekauga.utils.scene.ui.AnimationWidget.AnimationWidgetStyle;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.RatingWidget;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.UiFactory.CheckBoxStyles;
import com.spiddekauga.utils.scene.ui.UiFactory.TextButtonStyles;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.User;

/**
 * GUI for explore scene
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
	public void resetValues() {
		super.resetValues();

		resetComments();
		resetContent();
		resetInfo();
	}

	/**
	 * Reset info panel
	 */
	void resetInfo() {
		LevelInfoEntity level = mExploreScene.getSelectedLevel();

		if (level != null) {
			mWidgets.info.createdBy.setText(level.defEntity.originalCreator);
			mWidgets.info.date.setText(User.getGlobalUser().dateToString(level.defEntity.date));
			mWidgets.info.description.setText(level.defEntity.description);
			mWidgets.info.name.setText(level.defEntity.name);
			mWidgets.info.revisedBy.setText(level.defEntity.creator);

			mWidgets.info.bookmarks.setText(String.valueOf(level.stats.cBookmarks));
			mWidgets.info.plays.setText(String.valueOf(level.stats.cPlayed));
			mWidgets.info.rating.setRating((level.stats.getIntRating()));

			// Set tags
			String tagList = Strings.toStringList(level.tags, ", ");
			mWidgets.info.tags.setText(tagList);
		} else {
			mWidgets.info.createdBy.setText("");
			mWidgets.info.date.setText("");
			mWidgets.info.description.setText("");
			mWidgets.info.name.setText("");
			mWidgets.info.revisedBy.setText("");
			mWidgets.info.bookmarks.setText("");
			mWidgets.info.plays.setText("");
			mWidgets.info.rating.setRating(0);
			mWidgets.info.tags.setText("");
		}
	}

	/**
	 * Resets the comments
	 */
	void resetComments() {
		// TODO
	}

	/**
	 * Adds a new comment to the comment table
	 * @param username the user that posted the comment
	 * @param comment the actual comment
	 * @param date date of the comment
	 */
	void addComment(String username, String comment, Date date) {
		// TODO
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
		initTags();
		initActions();
		initTopBar();
		initContent();

		mExploreScene.fetchInitialLevels(getSelectedSortOrder(), getSelectedTags());
		getStage().setScrollFocus(mWidgets.content.scrollPane);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		if (isInitialized()) {
			dispose();
			initGui();
		}
	}

	@Override
	public void dispose() {
		super.dispose();

		mWidgets.comment.table.dispose();
		mWidgets.content.table.dispose();
		mWidgets.info.table.dispose();
		mWidgets.search.table.dispose();
		mWidgets.sort.table.dispose();
		mWidgets.tag.wrapper.dispose();
		mWidgets.view.table.dispose();
	}

	/**
	 * Initializes the top bar
	 */
	private void initTopBar() {
		mWidgets.topBar = new Background((Color) SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR));
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
		table.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		table.setAlign(Horizontal.LEFT, Vertical.TOP);
		getStage().addActor(table);
		ButtonGroup buttonGroup = new ButtonGroup();

		// Sort
		Button button = new ImageButton((ImageButtonStyle) SkinNames.getResource(SkinNames.General.BROWSE));
		table.add(button);
		buttonGroup.add(button);
		mWidgets.sort.hider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mExploreScene.fetchInitialLevels(getSelectedSortOrder(), getSelectedTags());
			}
		};


		// Search
		button = new ImageButton((ImageButtonStyle) SkinNames.getResource(SkinNames.General.SEARCH));
		table.add(button);
		buttonGroup.add(button);
		mWidgets.search.hider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				mExploreScene.fetchInitialLevels(mWidgets.search.field.getText());
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
		table.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		mWidgets.sort.hider.addToggleActor(table);
		getStage().addActor(table);

		CheckBoxStyle radioStyle = SkinNames.getResource(SkinNames.General.CHECK_BOX_RADIO);
		ButtonGroup buttonGroup = new ButtonGroup();

		// Create buttons
		for (final SortOrders sortOrder : SortOrders.values()) {
			CheckBox checkBox = new CheckBox(sortOrder.toString(), radioStyle);
			mWidgets.sort.buttons[sortOrder.ordinal()] = checkBox;
			buttonGroup.add(checkBox);
			table.add(checkBox).setHeight(mUiFactory.getStyles().vars.rowHeight);
			new ButtonListener(checkBox) {
				@Override
				protected void onChecked(Button button, boolean checked) {
					if (checked) {
						mExploreScene.fetchInitialLevels(sortOrder, getSelectedTags());
					}
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
		float infoWidth = SkinNames.getResource(SkinNames.GeneralVars.RIGHT_PANEL_WIDTH);

		AlignTable table = mWidgets.search.table;
		table.dispose(true);
		table.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		getStage().addActor(table);
		mWidgets.search.hider.addToggleActor(table);

		TextField textField = new TextField("", (TextFieldStyle) SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT));
		table.add(textField).setWidth(infoWidth);
		mWidgets.search.field = textField;
		new TextFieldListener(textField, "Search", null) {
			@Override
			protected void onChange(String newText) {
				mExploreScene.fetchInitialLevels(newText);
			}
		};
	}

	/**
	 * Initialize the right panel
	 */
	private void initRightPanel() {
		TabWidget tabWidget = mUiFactory.createRightPanel();
		addActor(tabWidget);
		mWidgets.tabWidget = tabWidget;

		// Updated bottom margin as play/menu buttons will be available
		float bottomMargin = mUiFactory.getStyles().vars.textButtonHeight + mUiFactory.getStyles().vars.paddingOuter * 2;
		tabWidget.setMarginBottom(bottomMargin);

		// Info
		ImageButtonStyle buttonStyle = SkinNames.getResource(SkinNames.General.OVERVIEW);
		tabWidget.addTab(buttonStyle, mWidgets.info.table);

		// Comments
		buttonStyle = SkinNames.getResource(SkinNames.General.COMMENTS);
		tabWidget.addTab(buttonStyle, mWidgets.comment.table);

		tabWidget.layout();
	}

	/**
	 * Initializes info panel
	 */
	private void initInfo() {
		AlignTable table = mWidgets.info.table;
		table.setName("info");

		// Name
		mWidgets.info.name = mUiFactory.addPanelSection("", table, null);
		table.getRow().setAlign(Horizontal.CENTER, Vertical.TOP);

		// Rating
		table.row(Horizontal.CENTER, Vertical.TOP);
		mWidgets.info.rating = mUiFactory.addRatingWidget(Touchable.disabled, table, null);

		// Description
		table.row(Horizontal.CENTER, Vertical.TOP);
		mWidgets.info.description = mUiFactory.addLabel("", true, table);

		// Created by
		mUiFactory.addPanelSection("Created by", table, null);
		mWidgets.info.createdBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", table, null);

		// Revised by
		mUiFactory.addPanelSection("Revised by", table, null);
		mWidgets.info.revisedBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", table, null);

		// Date
		mWidgets.info.date = mUiFactory.addIconLabel(SkinNames.GeneralImages.DATE, "", table, null);

		// Plays
		mWidgets.info.plays = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYS, "", table, null);

		// Likes
		mWidgets.info.bookmarks = mUiFactory.addIconLabel(SkinNames.GeneralImages.BOOKMARK, "", table, null);

		// Tags
		mWidgets.info.tags = mUiFactory.addIconLabel(SkinNames.GeneralImages.TAG, "", table, null);


		// Fill down
		table.row().setFillHeight(true).setFillWidth(true);
		table.add().setFillHeight(true).setFillWidth(true);
	}

	/**
	 * Initializes comments
	 */
	private void initComments() {
		AlignTable table = mWidgets.comment.table;

		Label label = new Label("STUB", (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_DEFAULT));
		table.row().setFillHeight(true);
		table.add(label);
	}

	/**
	 * Initializes action buttons
	 */
	private void initActions() {
		AlignTable table = mWidgets.actionTable;
		table.setAlignTable(Horizontal.RIGHT, Vertical.BOTTOM);
		table.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		table.row().setFillWidth(true).setEqualCellSize(true);
		table.setWidth(mWidgets.tabWidget.getWidth());
		table.setKeepWidth(true);
		table.setName("action-table");
		addActor(table);

		// Menu
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mExploreScene.gotoMainMenu();
			}
		};
		mUiFactory.addTextButton("Menu", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().resetWidth().setFillWidth(true);
		mUiFactory.addButtonPadding(table);


		// Play
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mExploreScene.play();
			}
		};
		mUiFactory.addTextButton("Play", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().resetWidth().setFillWidth(true);
	}

	/**
	 * Initializes tags
	 */
	private void initTags() {
		float topMargin = SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT);
		topMargin += mUiFactory.getStyles().vars.paddingOuter;


		// This table wraps the tag table with the tab table
		AlignTable wrapper = mWidgets.tag.wrapper;
		wrapper.setMargin(topMargin, mUiFactory.getStyles().vars.paddingOuter, mUiFactory.getStyles().vars.paddingOuter,
				mUiFactory.getStyles().vars.paddingOuter);
		wrapper.setAlign(Horizontal.LEFT, Vertical.TOP);
		wrapper.setName("wrapper");
		getStage().addActor(wrapper);
		wrapper.row().setFillHeight(true);
		mWidgets.sort.hider.addToggleActor(wrapper);


		// Tags
		float tagTableWidth = SkinNames.getResource(SkinNames.GeneralVars.TAG_BAR_WIDTH);
		AlignTable tagTable = new AlignTable();
		tagTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		tagTable.setBackgroundImage(new Background(mUiFactory.getStyles().color.widgetBackground));
		tagTable.setPad(mUiFactory.getStyles().vars.paddingInner);
		tagTable.setName("tags");
		tagTable.setKeepWidth(true);
		tagTable.setWidth(tagTableWidth - mUiFactory.getStyles().vars.paddingInner * 2);
		wrapper.add(tagTable).setFillHeight(true);

		// Filter results
		mUiFactory.addPanelSection("Filter Results", tagTable, null);
		tagTable.getRow().setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		// Add tags
		for (Tags tag : Tags.values()) {
			ButtonListener listener = new ButtonListener() {
				@Override
				protected void onChecked(Button button, boolean checked) {
					if (!mClearingTags) {
						mExploreScene.fetchInitialLevels(getSelectedSortOrder(), getSelectedTags());
					}
				}
			};
			CheckBox checkBox = mUiFactory.addCheckBoxRow(tag.toString(), CheckBoxStyles.CHECK_BOX, listener, mWidgets.tag.buttonGroup, tagTable);
			mWidgets.tag.buttonTag.put(checkBox, tag);
		}

		// Fill out the space
		tagTable.row().setFillHeight(true);


		// Toggle image
		ImageButton imageButton = new ImageButton((ImageButtonStyle) SkinNames.getResource(SkinNames.General.TAGS));
		final float imageWidth = imageButton.getWidth();
		HideListener hideListener = new HideListener(imageButton, true) {
			@Override
			protected void onShow() {
				// mWidgets.tag.wrapper.setWidth(tableWidth);
				mWidgets.tag.wrapper.layout();
				resetContent();
			}

			@Override
			protected void onHide() {
				mWidgets.tag.wrapper.layout();
				resetContent();
			}
		};
		wrapper.add(imageButton);
		hideListener.addToggleActor(tagTable);
		mWidgets.sort.hider.addChild(hideListener);


		// Clear button
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				boolean tagsChanged = getSelectedTags().size() > 0;

				mClearingTags = true;
				mWidgets.tag.buttonGroup.uncheckAll();
				mClearingTags = false;

				if (tagsChanged) {
					mExploreScene.fetchInitialLevels(getSelectedSortOrder(), getSelectedTags());
				}
			}
		};
		wrapper.row();
		mUiFactory.addTextButton("Clear Tags", TextButtonStyles.FILLED_PRESS, wrapper, buttonListener, hideListener, null);
		wrapper.getCell().setWidth(tagTableWidth).setPadRight(imageWidth);

		wrapper.layout();
	}

	/**
	 * Initialize level
	 */
	private void initContent() {
		// TODO change scroll pane style to no visible scroll bars
		ScrollPaneStyle scrollPaneStyle = SkinNames.getResource(SkinNames.General.SCROLL_PANE_DEFAULT);
		AlignTable table = mWidgets.content.table;
		table.setName("content");
		table.setAlign(Horizontal.LEFT, Vertical.TOP);
		table.setHasPreferredHeight(false).setHasPreferredWidth(false);
		mWidgets.content.scrollPane = new ScrollPane(table, scrollPaneStyle);
		getStage().addActor(mWidgets.content.scrollPane);
		table.setAlign(Horizontal.LEFT, Vertical.TOP);

		ScrollPaneListener listener = new ScrollPaneListener() {
			@Override
			public void hitEdge(ScrollPane scrollPane, Edge edge) {
				if (edge == Edge.BOTTOM) {
					if (!mExploreScene.isFetchingLevels() && mExploreScene.hasMoreLevels()) {
						mExploreScene.fetchMoreLevels();

						// Add wait icon
						addWaitIconToTable(mWidgets.content.table);
						mWidgets.content.scrollPane.invalidate();
					}
				}
			}
		};
		mWidgets.content.scrollPane.addListener(listener);

		resetContentMargins();
	}

	/**
	 * Reset content margins
	 */
	private void resetContentMargins() {
		if (mWidgets.content.scrollPane != null) {
			float screenWidth = Gdx.graphics.getWidth();
			float screenHeight = Gdx.graphics.getHeight();
			float marginLeft = mWidgets.tag.wrapper.getWidthWithMargin();
			float marginRight = mWidgets.actionTable.getWidthWithMargin();
			float marginTop = mWidgets.tag.wrapper.getMarginTop();
			float marginBottom = mWidgets.tag.wrapper.getMarginBottom();


			ScrollPane scrollPane = mWidgets.content.scrollPane;
			float width = screenWidth - marginLeft - marginRight;
			scrollPane.setWidth(width);
			scrollPane.setHeight(screenHeight - marginTop - marginBottom);
			scrollPane.setPosition(marginLeft, marginBottom);
			mWidgets.content.table.setWidth(width);
			mWidgets.content.table.setKeepWidth(true);
		}
	}

	/**
	 * Reset the content
	 */
	void resetContent() {
		ArrayList<LevelInfoEntity> levels = mExploreScene.getLevels();
		resetContent(levels);
	}

	/**
	 * Reset content
	 * @param levels level to update
	 */
	synchronized void resetContent(ArrayList<LevelInfoEntity> levels) {
		// Populate table
		AlignTable table = mWidgets.content.table;
		table.dispose();

		resetContentMargins();

		if (levels.isEmpty()) {
			return;
		}

		mWidgets.content.buttonGroup = new ButtonGroup();


		addContent(levels);

		resetInfo();

		if (mWidgets.content.scrollPane != null) {
			mWidgets.content.scrollPane.setScrollPercentY(0);
		}

		if (mExploreScene.isFetchingLevels()) {
			addWaitIconToTable(mWidgets.content.table);
		}
	}

	/**
	 * Adds more levels to the existing content
	 * @param levels the levels to add
	 */
	void addContent(ArrayList<LevelInfoEntity> levels) {
		AlignTable table = mWidgets.content.table;

		// Calculate how many levels per row
		float floatLevelsPerRow = mWidgets.content.scrollPane.getWidth() / Config.Level.SAVE_TEXTURE_WIDTH;
		int levelsPerRow = (int) floatLevelsPerRow;
		if (floatLevelsPerRow != levelsPerRow) {
			levelsPerRow++;
		}

		int columnIndex = levelsPerRow;

		// Remove wait icon and empty cells
		if (mWidgets.content.waitIconRow != null) {
			table.removeRow(mWidgets.content.waitIconRow, true);
			mWidgets.content.waitIconRow = null;

			ArrayList<Row> rows = table.getRows();
			if (!rows.isEmpty()) {
				Row lastRow = rows.get(rows.size() - 1);
				lastRow.removeEmptyCells();
				columnIndex = lastRow.getCellCount();
			}
		}

		float paddingExplore = mUiFactory.getStyles().vars.paddingExplore;

		// for (int i = 0; i < 5; ++i) {
		for (LevelInfoEntity level : levels) {
			AlignTable levelTable = createLevelTable(level);

			if (columnIndex == levelsPerRow) {
				table.row().setFillWidth(true).setEqualCellSize(true).setPadTop(paddingExplore).setPadRight(paddingExplore);
				columnIndex = 0;
			}

			table.add(levelTable).setFillWidth(true).setPadLeft(paddingExplore);

			columnIndex++;
		}
		// }

		// Set pad bottom for last row
		Row lastRow = table.getRow();
		if (lastRow != null) {
			lastRow.setPadBottom(paddingExplore);
		}

		// Pad with empty cells
		if (columnIndex > 0 && columnIndex < levelsPerRow) {
			int columnsToPad = levelsPerRow - columnIndex;
			for (int i = 0; i < columnsToPad; ++i) {
				table.add().setFillWidth(true);
			}
		}

		table.invalidate();
		table.layout();
		mWidgets.content.scrollPane.invalidate();


		// If content view is not full, fetch more content...
		if (mExploreScene.hasMoreLevels() && table.getHeight() < mWidgets.content.scrollPane.getHeight()) {
			addWaitIconToTable(table);
			mExploreScene.fetchMoreLevels();
		}
	}

	/**
	 * Create level image table
	 * @param level the level to create an image table for
	 * @return table with level image, name and rating
	 */
	private AlignTable createLevelTable(final LevelInfoEntity level) {
		AlignTable table = new AlignTable();
		table.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		// Image button
		ImageButtonStyle defaultImageStyle = SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_TOGGLE);
		ImageButtonStyle imageButtonStyle = new ImageButtonStyle(defaultImageStyle);
		imageButtonStyle.imageUp = (Drawable) level.defEntity.drawable;

		Button button = new ImageButton(imageButtonStyle);
		table.row().setFillWidth(true);
		table.add(button).setFillWidth(true).setKeepAspectRatio(true);
		new ButtonListener(button) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mExploreScene.setSelectedLevel(level);
					resetInfo();
				}
			}

			@Override
			protected void onDown(Button button) {
				mWasCheckedOnDown = button.isChecked();
			}

			@Override
			protected void onUp(Button button) {
				if (mWasCheckedOnDown) {
					mExploreScene.play();
				}
			}

			/** If this level was selected before */
			private boolean mWasCheckedOnDown = false;
		};
		mWidgets.content.buttonGroup.add(button);

		// Level name
		table.row();
		mUiFactory.addLabel(level.defEntity.name, false, table);
		table.getCell().setHeight(mUiFactory.getStyles().vars.rowHeight);

		// Rating
		table.row();
		RatingWidget ratingWidget = mUiFactory.addRatingWidget(Touchable.disabled, table, null);
		ratingWidget.setRating(level.stats.getIntRating());

		return table;
	}

	/**
	 * Add a wait icon to a new row for the specified table
	 * @param table the table to add the animation wait widget to
	 */
	private void addWaitIconToTable(AlignTable table) {
		AnimationWidgetStyle waitIconStyle = SkinNames.getResource(SkinNames.General.ANIMATION_WAIT);
		AnimationWidget waitIcon = new AnimationWidget(waitIconStyle);

		mWidgets.content.waitIconRow = table.row(Horizontal.CENTER, Vertical.MIDDLE);
		table.add(waitIcon);
		table.invalidate();
	}

	/** If we're currently clearing the tags */
	private boolean mClearingTags = false;
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
		Background topBar = null;
		Content content = new Content();
		// AlignTable rightPanel = new AlignTable();
		TabWidget tabWidget = null;
		AlignTable actionTable = new AlignTable();

		private static class Content {
			AlignTable table = new AlignTable();
			ScrollPane scrollPane = null;
			ButtonGroup buttonGroup = new ButtonGroup();
			Row waitIconRow = null;
		}

		private static class View {
			AlignTable table = new AlignTable();
		}

		private static class Sort {
			Button[] buttons = new Button[SortOrders.values().length];
			AlignTable table = new AlignTable();
			HideListener hider = null;
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
			Label bookmarks = null;
			Label tags = null;
		}

		private static class Comments {
			AlignTable table = new AlignTable();
		}

		private static class Tag {
			AlignTable wrapper = new AlignTable();
			ButtonGroup buttonGroup = new ButtonGroup();
			HashMap<Button, Tags> buttonTag = new HashMap<>();

			{
				buttonGroup.setMinCheckCount(0);
				buttonGroup.setMaxCheckCount(5);
			}
		}

		private static class Search {
			TextField field = null;
			AlignTable table = new AlignTable();
			HideListener hider = null;
		}
	}
}
