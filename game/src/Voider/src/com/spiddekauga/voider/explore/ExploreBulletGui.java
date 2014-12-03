package com.spiddekauga.voider.explore;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

/**
 * GUI for finding or loading bullets
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreBulletGui extends ExploreActorGui {
	/**
	 * Hidden constructor
	 */
	protected ExploreBulletGui() {
		// Does nothing
	}

	@Override
	public void initGui() {
		super.initGui();

		initSearchFilters();
		initViewButtons();

		resetContentMargins();
		mScene.repopulateContent();
	}

	@Override
	public void dispose() {
		super.dispose();

		mWidgets.dispose();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetSearchFilters();
	}

	/**
	 * Initialize view buttons (top left)
	 */
	private void initViewButtons() {
		// Search online
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setSearchOnline(true);
			}
		};
		addViewButton(SkinNames.General.SEARCH, listener, mWidgets.search.viewHider);
	}

	/**
	 * Initialize search filters
	 */
	private void initSearchFilters() {
		// Tab
		Button button = mUiFactory.addTab(SkinNames.General.SEARCH_FILTER, mWidgets.search.table, mWidgets.search.contentHider, mLeftPanel);
		mWidgets.search.viewHider.addToggleActor(button);

		AlignTable table = mWidgets.search.table;
		table.setName("search-filters");
		mUiFactory.text.addPanelSection("Search Filter", table, null);
		table.getRow().setAlign(Horizontal.CENTER, Vertical.TOP);
		table.getCell();


		// Search Field
		TextFieldListener textFieldListener = new TextFieldListener() {
			@Override
			protected void onChange(String newText) {
				mScene.fetch(newText);
			}
		};
		mUiFactory.text.addPanelSection("Free-text Search", table, null);
		mWidgets.search.searchText = mUiFactory.addTextField(null, false, "Name or Creator", textFieldListener, table, null);

		// Clear button
		button = mUiFactory.button.createText("Clear Filters", TextButtonStyles.FILLED_PRESS);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				resetSearchFilters();
			}
		};
		mLeftPanel.addActionButton(button);
		mLeftPanel.layout();
	}

	/**
	 * Reset search filters
	 */
	private void resetSearchFilters() {
		mWidgets.search.searchText.setText("");
	}

	/**
	 * Sets the explore scene
	 * @param exploreScene
	 */
	void setExploreBulletScene(ExploreBulletScene exploreScene) {
		mScene = exploreScene;
	}

	private ExploreBulletScene mScene = null;
	private Widgets mWidgets = new Widgets();

	private class Widgets implements Disposable {
		Search search = new Search();

		private class Search implements Disposable {
			AlignTable table = new AlignTable();
			HideListener viewHider = new HideListener(true) {
				@Override
				protected void onShow() {
					resetContentMargins();
				}
			};
			HideListener contentHider = new HideListener(true);
			TextField searchText = null;

			private Search() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				viewHider.dispose();
				contentHider.dispose();

				init();
			}

			private void init() {
				viewHider.addChild(contentHider);
			}
		}

		@Override
		public void dispose() {
			search.dispose();
		}
	}
}
