package com.spiddekauga.voider.menu;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.repo.misc.SettingRepo;
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
		initInfo(mWidgets.info.table, mWidgets.info.hider);
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetInfo();
	}

	@Override
	public void dispose() {
		super.dispose();

		mWidgets.dispose();
	}

	@Override
	void resetContent() {
		super.resetContent();

		resetInfo();
	}

	/**
	 * Initialize right panel
	 */
	private void initRightPanel() {
		// Info
		mUiFactory.addTab(SkinNames.General.OVERVIEW, mWidgets.info.table, mWidgets.info.hider, mRightPanel);

		mRightPanel.layout();
	}

	/**
	 * Add actors to the content table
	 * @param actors all actors to append to the content table
	 */
	protected void addContent(List<? extends DefEntity> actors) {
		beginAddContent();
		DefEntity selectedActor = mScene.getSelectedActor();

		for (DefEntity actor : actors) {
			boolean selected = selectedActor == actor;
			addContent(createActorTable(actor, selected));
		}

		endAddContent();
	}

	/**
	 * Creates actor content table
	 * @param actor the actor to create
	 * @param selected true if the actor is selected
	 * @return table with actor image and name
	 */
	protected AlignTable createActorTable(final DefEntity actor, boolean selected) {
		AlignTable table = new AlignTable();
		table.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		ImageButtonStyle defaultImageStyle = SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_TOGGLE);
		ImageButtonStyle imageButtonStyle = new ImageButtonStyle(defaultImageStyle);
		imageButtonStyle.imageUp = (Drawable) actor.drawable;

		Button button = new ImageButton(imageButtonStyle);
		button.setChecked(selected);
		table.row().setFillWidth(true);
		table.add(button).setFillWidth(true).setKeepAspectRatio(true);

		new ButtonListener(button) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mScene.setSelectedActor(actor);
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
					mScene.onSelectAction();
				}
			}

			/** If this actor was selected before */
			private boolean mWasCheckedOnDown = false;
		};
		addContentButton(button);

		// Actor name
		table.row();
		mUiFactory.text.add(actor.name, table);
		table.getCell().setHeight(mUiFactory.getStyles().vars.rowHeight);

		return table;
	}

	/**
	 * Initialize the information table in the right panel. This populates the table with
	 * the default information. Override this table to add more information to it
	 * @param table information table
	 * @param hider info hider
	 */
	protected void initInfo(AlignTable table, HideListener hider) {
		// Name
		mWidgets.info.name = mUiFactory.text.addPanelSection("", table, null);
		table.getRow().setAlign(Horizontal.CENTER, Vertical.TOP);

		// Description
		table.row(Horizontal.CENTER, Vertical.TOP);
		mWidgets.info.description = mUiFactory.text.add("", true, table);

		// Created by
		mUiFactory.text.addPanelSection("Created By", table, null);
		mWidgets.info.createbBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", false, table, null);

		// Revised by
		mUiFactory.text.addPanelSection("Revised By", table, mWidgets.info.revisedHider);
		mWidgets.info.revisedBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", false, table, mWidgets.info.revisedHider);

		// Date
		mWidgets.info.date = mUiFactory.addIconLabel(SkinNames.GeneralImages.DATE, "", false, table, null);
	}

	/**
	 * Resets the values of info
	 */
	protected void resetInfo() {
		DefEntity actor = mScene.getSelectedActor();

		if (actor != null) {
			// Has created UI elements
			if (mWidgets.info.name != null) {
				mWidgets.info.createbBy.setText(actor.originalCreator);
				mWidgets.info.date.setText(mSettingRepo.date().getDate(actor.date));
				mWidgets.info.description.setText(actor.description);
				mWidgets.info.name.setText(actor.name);

				// Revised by another person
				if (!actor.originalCreatorKey.equals(actor.revisedByKey)) {
					mWidgets.info.revisedHider.show();
					mWidgets.info.revisedBy.setText(actor.revisedBy);
				} else {
					mWidgets.info.revisedHider.hide();
				}
			}
		} else {
			// Has created UI elements
			if (mWidgets.info.name != null) {
				mWidgets.info.createbBy.setText("");
				mWidgets.info.date.setText("");
				mWidgets.info.description.setText("");
				mWidgets.info.name.setText("");
				mWidgets.info.revisedHider.hide();
			}
		}
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
		// Does nothing
	}

	@Override
	protected float getMaxActorWidth() {
		return Config.Actor.SAVE_TEXTURE_SIZE;
	}

	private ExploreActorScene mScene = null;
	private Widgets mWidgets = new Widgets();
	private SettingRepo mSettingRepo = SettingRepo.getInstance();

	private class Widgets implements Disposable {
		Info info = new Info();

		class Info implements Disposable {
			AlignTable table = new AlignTable();
			HideListener hider = new HideListener(true);
			Label name = null;
			Label description = null;
			Label createbBy = null;
			Label revisedBy = null;
			Label date = null;
			HideManual revisedHider = new HideManual();

			private Info() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				revisedHider.dispose();
				hider.dispose();
				init();
			}

			private void init() {
				hider.addChild(revisedHider);
			}
		}

		@Override
		public void dispose() {
			info.dispose();
		}
	}
}
