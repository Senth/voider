package com.spiddekauga.voider.explore;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.commands.CEventConnect;
import com.spiddekauga.utils.commands.CSequence;
import com.spiddekauga.utils.commands.CUserConnect;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.RatingWidget;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.explore.ExploreScene.ExploreViews;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethod.SortOrders;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * GUI for explore level scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreLevelGui extends ExploreGui {
	/**
	 * Hidden constructor
	 */
	ExploreLevelGui() {
		// Does nothing
	}

	/**
	 * Sets the explore scene
	 * @param exploreScene the explore scene
	 */
	void setExploreLevelScene(ExploreLevelScene exploreScene) {
		mExploreScene = exploreScene;
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetComments();
		resetContent();
	}

	/**
	 * Reset info panel
	 */
	void resetInfo() {
		LevelInfoEntity level = mExploreScene.getSelectedLevel();

		if (level != null) {
			mWidgets.info.createdBy.setText(level.defEntity.originalCreator);
			mWidgets.info.date.setText(mDateRepo.getDate(level.defEntity.date));
			mWidgets.info.description.setText(level.defEntity.description);
			mWidgets.info.name.setText(level.defEntity.name);

			// Revised by
			if (!level.defEntity.originalCreatorKey.equals(level.defEntity.revisedByKey)) {
				mWidgets.info.revisedHider.show();
				mWidgets.info.revisedBy.setText(level.defEntity.revisedBy);
			} else {
				mWidgets.info.revisedHider.hide();
			}

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
			mWidgets.info.bookmarks.setText("");
			mWidgets.info.plays.setText("");
			mWidgets.info.rating.setRating(0);
			mWidgets.info.tags.setText("");
			mWidgets.info.revisedHider.hide();
		}
	}

	/**
	 * Resets the comments
	 */
	void resetComments() {
		if (mWidgets.comment.userComment != null) {
			mWidgets.comment.userComment.setText("");
		}
		if (mWidgets.comment.userDate != null) {
			mWidgets.comment.userDate.setText("");
		}
		mWidgets.comment.userHider.hide();
		mWidgets.comment.comments.dispose();
	}

	/**
	 * Adds a new comment to the comment table
	 * @param username the user that posted the comment
	 * @param comment the actual comment
	 * @param date date of the comment
	 */
	void addComment(String username, String comment, String date) {
		// Use padding
		boolean usePadding = true;
		if (mWidgets.comment.comments.getRowCount() == 0) {
			usePadding = false;
		}

		AlignTable table = mUiFactory.createComment(username, comment, date, usePadding, null);
		mWidgets.comment.comments.row();
		mWidgets.comment.comments.add(table);
	}

	/**
	 * Set the user comment
	 * @param comment user comment
	 * @param date when the comment was made
	 */
	void setUserComment(String comment, String date) {
		mWidgets.comment.userComment.setText(comment);
		mWidgets.comment.userDate.setText(date);

		// Commented something
		if (!comment.isEmpty()) {
			mWidgets.comment.userHider.show();
		} else {
			mWidgets.comment.userHider.hide();
		}
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

		resetContentMargins();
		mExploreScene.fetchInitialLevels(getSelectedSortOrder(), getSelectedTags());
	}

	@Override
	public void dispose() {
		super.dispose();
		mWidgets.dispose();
	}

	/**
	 * Initializes different view buttons
	 */
	@Override
	protected void initViewButtons() {
		super.initViewButtons();

		// Sort (online)
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mExploreScene.setView(ExploreViews.ONLINE_BROWSE);
				// REMOVE
				mExploreScene.fetchInitialLevels(getSelectedSortOrder(), getSelectedTags());
			}
		};
		addViewButton(SkinNames.General.EXPLORE_ONLINE, listener, mWidgets.sort.viewHider);


		// Search (online)
		listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mExploreScene.setView(ExploreViews.ONLINE_SEARCH);
				// REMOVE
				if (!mWidgets.search.field.getText().equals("Search")) {
					mExploreScene.fetchInitialLevels(mWidgets.search.field.getText());
				} else {
					mExploreScene.fetchInitialLevels("");
				}
			}
		};
		addViewButton(SkinNames.General.EXPLORE_ONLINE_SEARCH, listener, mWidgets.search.viewHider);
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
		float paddingSeparator = mUiFactory.getStyles().vars.paddingSeparator;

		AlignTable table = mWidgets.sort.table;
		table.dispose(true);
		table.setPaddingCellDefault(0, 0, 0, paddingSeparator);
		table.row().setPadRight(mUiFactory.getStyles().vars.paddingOuter);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		table.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		getStage().addActor(table);

		ButtonGroup buttonGroup = new ButtonGroup();

		// Create buttons
		for (final SortOrders sortOrder : SortOrders.values()) {
			ButtonListener listener = new ButtonListener() {
				@Override
				protected void onChecked(Button button, boolean checked) {
					if (checked) {
						mExploreScene.fetchInitialLevels(sortOrder, getSelectedTags());
					}
				}
			};
			CheckBox checkBox = mUiFactory.button.addCheckBox(sortOrder.toString(), CheckBoxStyles.RADIO, listener, buttonGroup, table);
			mWidgets.sort.buttons[sortOrder.ordinal()] = checkBox;
			table.getCell().setHeight(mUiFactory.getStyles().vars.rowHeight);
		}
	}

	/**
	 * @return all selected tags
	 */
	private ArrayList<Tags> getSelectedTags() {
		ArrayList<Tags> tags = new ArrayList<>();

		for (Button selectedButton : mWidgets.tag.buttonGroup.getAllChecked()) {
			tags.add(mWidgets.tag.buttonTag.get(selectedButton));
		}

		return tags;
	}

	/**
	 * Initializes search bar
	 */
	private void initSearchBar() {
		float infoWidth = mUiFactory.getStyles().vars.rightPanelWidth + 2 * mUiFactory.getStyles().vars.paddingInner;
		float height = mUiFactory.getStyles().vars.rowHeight;

		AlignTable table = mWidgets.search.table;
		table.dispose(true);
		table.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		getStage().addActor(table);

		TextField textField = new TextField("", mUiFactory.getStyles().textField.standard);
		table.add(textField).setSize(infoWidth, height);
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
		// Info
		mUiFactory.addTab(SkinNames.General.OVERVIEW, mWidgets.info.table, mWidgets.info.hider, mRightPanel);

		// Comments
		// mUiFactory.addTab(SkinNames.General.COMMENTS, mWidgets.comment.table, null,
		// mRightPanel);

		mRightPanel.layout();
	}

	/**
	 * Initializes info panel
	 */
	private void initInfo() {
		AlignTable table = mWidgets.info.table;
		table.setName("info");

		// Name
		mWidgets.info.name = mUiFactory.text.addPanelSection("", table, null);
		table.getRow().setAlign(Horizontal.CENTER, Vertical.TOP);

		// Rating
		table.row(Horizontal.CENTER, Vertical.TOP);
		mWidgets.info.rating = mUiFactory.addRatingWidget(Touchable.disabled, table, null);

		// Description
		table.row(Horizontal.CENTER, Vertical.TOP);
		mWidgets.info.description = mUiFactory.text.add("", true, table);

		// Created by
		mUiFactory.text.addPanelSection("Created by", table, null);
		mWidgets.info.createdBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", false, table, null);

		// Revised by
		mUiFactory.text.addPanelSection("Revised by", table, mWidgets.info.revisedHider);
		mWidgets.info.revisedBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", false, table, mWidgets.info.revisedHider);

		// Date
		mWidgets.info.date = mUiFactory.addIconLabel(SkinNames.GeneralImages.DATE, "", false, table, null);

		// Plays
		mWidgets.info.plays = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYS, "", false, table, null);

		// Likes
		mWidgets.info.bookmarks = mUiFactory.addIconLabel(SkinNames.GeneralImages.BOOKMARK, "", false, table, null);

		// Tags
		mWidgets.info.tags = mUiFactory.addIconLabel(SkinNames.GeneralImages.TAG, "", true, table, null);
		mWidgets.info.tags.setWrap(true);
	}

	/**
	 * Initializes comments
	 */
	private void initComments() {
		AlignTable table = mWidgets.comment.table;

		// User comment
		GuiHider userHider = mWidgets.comment.userHider;

		mUiFactory.text.addPanelSection("Your comment", table, userHider);

		ArrayList<Actor> createdActors = new ArrayList<>();
		AlignTable userComment = mUiFactory.createComment(User.getGlobalUser().getUsername(), "", "", false, createdActors);
		table.row();
		table.add(userComment);
		userHider.addToggleActor(userComment);
		mWidgets.comment.userComment = (Label) createdActors.get(1);
		mWidgets.comment.userDate = (Label) createdActors.get(2);

		// Level comments
		mUiFactory.text.addPanelSection("Latest comments", table, null);
		mWidgets.comment.comments.setAlign(Horizontal.LEFT, Vertical.TOP);
		ScrollPane scrollPane = new ScrollPane(mWidgets.comment.comments, mUiFactory.getStyles().scrollPane.noBackground);
		table.row().setFillHeight(true).setFillWidth(true);
		table.add(scrollPane).setFillHeight(true).setFillWidth(true);

		ScrollPaneListener listener = new ScrollPaneListener() {
			@Override
			public void hitEdge(ScrollPane scrollPane, Edge edge) {
				if (edge == Edge.BOTTOM) {
					mExploreScene.fetchMoreComments();
				}
			}
		};
		scrollPane.addListener(listener);
	}

	/**
	 * Initializes tags
	 */
	private void initTags() {
		AlignTable tagTable = new AlignTable();

		// Filter results
		mUiFactory.text.addPanelSection("Filter Results", tagTable, null);
		tagTable.getRow().setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		// Add tags
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (!mClearingTags) {
					mExploreScene.fetchInitialLevels(getSelectedSortOrder(), getSelectedTags());
				}
			}
		};
		for (Tags tag : Tags.values()) {
			CheckBox checkBox = mUiFactory.button.addCheckBoxRow(tag.toString(), CheckBoxStyles.CHECK_BOX, listener, mWidgets.tag.buttonGroup,
					tagTable);
			mWidgets.tag.buttonTag.put(checkBox, tag);
		}

		// Toggle image
		ImageButtonStyle imageButtonStyle = (ImageButtonStyle) SkinNames.getResource(SkinNames.General.TAGS);
		HideListener hideListener = new HideListener(true);
		mWidgets.sort.viewHider.addChild(hideListener);
		Button tagButton = mLeftPanel.addTab(imageButtonStyle, tagTable, hideListener);
		mWidgets.sort.viewHider.addToggleActor(tagButton);


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
		TextButton textButton = mUiFactory.button.createText("Clear Tags", TextButtonStyles.FILLED_PRESS);
		textButton.addListener(buttonListener);
		mLeftPanel.addActionButton(textButton);

		mLeftPanel.invalidate();
		mLeftPanel.layout();

	}

	/**
	 * Reset the content
	 */
	@Override
	void resetContent() {
		super.resetContent();

		resetInfo();
	}

	/**
	 * Adds more levels to the existing content
	 * @param levels the levels to add
	 */
	void addContent(ArrayList<LevelInfoEntity> levels) {
		beginAddContent();
		LevelInfoEntity selectedLevel = mExploreScene.getSelectedLevel();

		// for (int i = 0; i < 5; ++i) {
		for (LevelInfoEntity level : levels) {
			boolean selected = selectedLevel == level;
			addContent(createLevelTable(level, selected));
		}
		// }

		endAddContent();
	}

	/**
	 * Create level image table
	 * @param level the level to create an image table for
	 * @param selected true if it should be selected from the start
	 * @return table with level image, name and rating
	 */
	private AlignTable createLevelTable(final LevelInfoEntity level, boolean selected) {
		AlignTable table = new AlignTable();
		table.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		// Image button
		ImageButtonStyle defaultImageStyle = SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_TOGGLE);
		ImageButtonStyle imageButtonStyle = new ImageButtonStyle(defaultImageStyle);
		imageButtonStyle.imageUp = (Drawable) level.defEntity.drawable;

		Button button = new ImageButton(imageButtonStyle);
		button.setChecked(selected);
		table.row().setFillWidth(true);
		table.add(button).setFillWidth(true).setKeepAspectRatio(true);
		new ButtonListener(button) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mExploreScene.setSelectedLevel(level);
				}
			}

			@Override
			protected void onDown(Button button) {
				mWasCheckedOnDown = button.isChecked();
			}

			@Override
			protected void onUp(Button button) {
				if (mWasCheckedOnDown) {
					mExploreScene.selectAction();
				}
			}

			/** If this level was selected before */
			private boolean mWasCheckedOnDown = false;
		};
		addContentButton(button);

		// Level name
		table.row();
		mUiFactory.text.add(level.defEntity.name, table);
		table.getCell().setHeight(mUiFactory.getStyles().vars.rowHeight);

		// Rating
		table.row();
		RatingWidget ratingWidget = mUiFactory.addRatingWidget(Touchable.disabled, table, null);
		ratingWidget.setRating(level.stats.getIntRating());

		return table;
	}

	/**
	 * Show go online dialog
	 */
	void showGoOnlineDialog() {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);

		msgBox.setTitle("Go Online?");

		Label label = mUiFactory.text.create("To use online features you need to connect to the server.");
		msgBox.content(label);

		IEventListener loginListener = new IEventListener() {
			@Override
			public void handleEvent(GameEvent event) {
				EventDispatcher.getInstance().disconnect(EventTypes.USER_CONNECTED, this);

				// Sort
				if (mWidgets.sort.viewHider.isVisible()) {
					mExploreScene.fetchInitialLevels(getSelectedSortOrder(), getSelectedTags());
				}
				// Search
				else if (mWidgets.search.viewHider.isVisible()) {
					mExploreScene.fetchInitialLevels(mWidgets.search.field.getText());
				}
			}
		};

		Command eventConnect = new CEventConnect(loginListener, EventTypes.USER_CONNECTED);
		Command goOnline = new CUserConnect();

		msgBox.addCancelButtonAndKeys();
		msgBox.button("Go Online", new CSequence(eventConnect, goOnline));

		showMsgBox(msgBox);
	}

	/**
	 * Remove wait icon from comment table
	 */
	void commentWaitIconRemove() {
		if (mWidgets.comment.waitIconRow != null) {
			mWidgets.comment.comments.removeRow(mWidgets.comment.waitIconRow, false);
			mWidgets.comment.waitIconRow = null;
		}
	}

	/**
	 * Add wait icon to comment table
	 */
	void commentWaitIconAdd() {
		mWidgets.comment.waitIconRow = addWaitIconToTable(mWidgets.comment.comments);
	}

	@Override
	protected float getMaxActorWidth() {
		return Config.Level.SAVE_TEXTURE_WIDTH;
	}

	/** If we're currently clearing the tags */
	private boolean mClearingTags = false;
	private ExploreLevelScene mExploreScene = null;
	private Widgets mWidgets = null;

	/**
	 * All widgets
	 */
	private class Widgets implements Disposable {
		Sort sort = new Sort();
		Info info = new Info();
		Comments comment = new Comments();
		Tag tag = new Tag();
		Search search = new Search();

		private class Sort implements Disposable {
			Button[] buttons = new Button[SortOrders.values().length];
			AlignTable table = new AlignTable();
			HideListener viewHider = new HideListener(true) {
				@Override
				protected void onShow() {
					resetContentMargins();
				}
			};

			private Sort() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				viewHider.dispose();

				init();
			}

			private void init() {
				viewHider.addToggleActor(table);
			}
		}

		private class Info implements Disposable {
			AlignTable table = new AlignTable();
			HideListener hider = new HideListener(true);
			Label name = null;
			Label description = null;
			RatingWidget rating = null;
			Label revisedBy = null;
			Label createdBy = null;
			Label date = null;
			Label plays = null;
			Label bookmarks = null;
			Label tags = null;
			HideManual revisedHider = new HideManual();

			private Info() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				revisedHider.dispose();
				hider.dispose();
			}

			private void init() {
				hider.addChild(revisedHider);
			}
		}

		private class Comments implements Disposable {
			AlignTable table = new AlignTable();
			HideListener hider = new HideListener(true);
			AlignTable comments = new AlignTable();
			Label userComment = null;
			Label userDate = null;
			HideManual userHider = new HideManual();
			Row waitIconRow = null;

			{
				hider.addChild(userHider);
			}

			@Override
			public void dispose() {
				table.dispose();
			}
		}

		private class Tag implements Disposable {
			AlignTable table = new AlignTable();
			ButtonGroup buttonGroup = new ButtonGroup();
			HashMap<Button, Tags> buttonTag = new HashMap<>();

			private Tag() {
				buttonGroup.setMinCheckCount(0);
				buttonGroup.setMaxCheckCount(5);
			}

			@Override
			public void dispose() {
				table.dispose();
			}
		}

		private class Search implements Disposable {
			TextField field = null;
			AlignTable table = new AlignTable();
			HideListener viewHider = new HideListener(true) {
				@Override
				protected void onShow() {
					resetContentMargins();
				}
			};

			private Search() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				viewHider.dispose();

				init();
			}

			private void init() {
				viewHider.addToggleActor(table);
			}
		}

		@Override
		public void dispose() {
			comment.dispose();
			info.dispose();
			search.dispose();
			tag.dispose();
			sort.dispose();
		}
	}
}
