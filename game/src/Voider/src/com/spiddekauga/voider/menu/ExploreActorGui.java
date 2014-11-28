package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.resource.SkinNames;

/**
 * Common GUI class for exploring actors. Override this class to make the layout and
 * actions more specific
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreActorGui extends ExploreGui {
	/**
	 * Hidden constructor
	 */
	protected ExploreActorGui() {
		// Does nothing
	}

	@Override
	public void initGui() {
		super.initGui();

		initRightPanel();
		initInfo(mWidgets.info.table);

	}

	/**
	 * Initialize right panel
	 */
	private void initRightPanel() {
		// Info
		mUiFactory.addTab(SkinNames.General.OVERVIEW, mWidgets.info.table, null, mRightPanel);

		mRightPanel.layout();
	}

	/**
	 * Initialize the information table in the right panel. This populates the table with
	 * the default information. Override this table to add more information to it
	 * @param table information table
	 */
	protected void initInfo(AlignTable table) {
		// Name
		mWidgets.info.name = mUiFactory.text.addPanel("", table, null);
		table.getRow().setAlign(Horizontal.CENTER, Vertical.TOP);

		// Description
		table.row(Horizontal.CENTER, Vertical.TOP);
		mWidgets.info.description = mUiFactory.text.add("", true, table);

		// Created by
		mUiFactory.text.addPanelSection("Created By", table, null);
		mWidgets.info.createbBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", false, table, null);

		// Revised by
		mUiFactory.text.addPanelSection("Revised By", table, null);
		mWidgets.info.revisedBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", false, table, null);

		// Date
		mWidgets.info.date = mUiFactory.addIconLabel(SkinNames.GeneralImages.DATE, "", false, table, null);
	}


	/**
	 * Sets the actor scene
	 * @param scene
	 */
	protected void setActorScene(ExploreActorScene scene) {
		mScene = scene;
	}

	@Override
	protected void onFetchMoreContent() {
		// TODO Auto-generated method stub

	}

	@Override
	protected float getMaxActorWidth() {
		return Config.Actor.SAVE_TEXTURE_SIZE;
	}

	private ExploreActorScene mScene = null;
	private Widgets mWidgets = new Widgets();

	private class Widgets implements Disposable {
		Info info = new Info();

		class Info implements Disposable {
			AlignTable table = new AlignTable();
			Label name = null;
			Label description = null;
			Label createbBy = null;
			Label revisedBy = null;
			Label date = null;

			@Override
			public void dispose() {
				table.dispose();
			}
		}

		@Override
		public void dispose() {
			info.dispose();
		}
	}
}
