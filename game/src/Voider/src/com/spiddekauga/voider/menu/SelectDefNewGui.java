package com.spiddekauga.voider.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for Select Definition Scene. This creates a border at
 * the top for filtering search, and an optionally checkbox for
 * only showing the player's own actors.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SelectDefNewGui extends Gui {
	/**
	 * Creates the GUI (but does not init it) for the select actor
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox
	 * to only display one's own actors.
	 */
	public SelectDefNewGui(boolean showMineOnlyCheckbox) {
		mShowMineOnlyCheckbox = showMineOnlyCheckbox;
	}

	/**
	 * Sets the select def scene this GUI is bound to.
	 * @param selectDefScene scene this GUI is bound to
	 */
	public void setSelectDefScene(SelectDefScene selectDefScene) {
		mSelectDefScene = selectDefScene;
	}

	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		dispose();
		initGui();
	}

	@Override
	public void resetValues() {
		super.resetValues();
	}

	/**
	 * Initializes search bar
	 */
	private void initSearchBar() {
		float infoWidth = SkinNames.getResource(SkinNames.GeneralVars.INFO_BAR_WIDTH);

		AlignTable table = mWidgets.search.table;
		table.dispose(true);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		getStage().addActor(table);

		TextField textField = new TextField("", (TextFieldStyle) SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT));
		table.add(textField).setWidth(infoWidth);
		mWidgets.search.field = textField;
		new TextFieldListener(textField, "Search", null) {
			@Override
			protected void onChange(String newText) {
				// TODO
			}
		};
	}

	/**
	 * Initializes info panel
	 */
	private void initInfo() {
		LabelStyle labelStyle = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);
		Color widgetBackgroundColor = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR);


		AlignTable table = mWidgets.info.table;
		table.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
		table.setAlignRow(Horizontal.LEFT, Vertical.TOP);
		table.setBackgroundImage(new Background(widgetBackgroundColor));


		// Name
		Label label = new Label("", labelStyle);
		mWidgets.info.name = label;
		table.row(Horizontal.CENTER, Vertical.TOP);
		table.add(label);


		// Rating
		//		RatingWidgetStyle ratingStyle = SkinNames.getResource(SkinNames.General.RATING_DEFAULT);
		//		RatingWidget rating = new RatingWidget(ratingStyle, 5, Touchable.disabled);
		//		mWidgets.info.rating = rating;
		//		rating.setName("rating");
		//		table.row(Horizontal.CENTER, Vertical.TOP);
		//		table.add(rating);


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

		Drawable playerIcon = SkinNames.getDrawable(SkinNames.GeneralImages.PLAYER);
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
		Drawable dateIcon = SkinNames.getDrawable(SkinNames.GeneralImages.DATE);
		image = new Image(dateIcon);
		table.row();
		table.add(image);

		label = new Label("", labelStyle);
		mWidgets.info.date = label;
		table.add(label);


		// Plays
		//		Drawable playsIcon = SkinNames.getDrawable(SkinNames.GeneralImages.PLAYS);
		//		image = new Image(playsIcon);
		//		table.row();
		//		table.add(image);
		//
		//		label = new Label("", labelStyle);
		//		mWidgets.info.plays = label;
		//		table.add(label);


		// Likes
		//		Drawable likesIcon = SkinNames.getDrawable(SkinNames.GeneralImages.LIKE);
		//		image = new Image(likesIcon);
		//		table.row();
		//		table.add(image);
		//
		//		label = new Label("", labelStyle);
		//		mWidgets.info.likes = label;
		//		table.add(label);


		// Tags
		//		Drawable tagIcon = SkinNames.getDrawable(SkinNames.GeneralImages.TAG);
		//		image = new Image(tagIcon);
		//		table.row();
		//		table.add(image);
		//
		//		label = new Label("", labelStyle);
		//		mWidgets.info.tags = label;
		//		table.add(label);

		table.row().setFillHeight(true).setFillWidth(true);
		table.add().setFillHeight(true).setFillWidth(true);
	}

	/** If the checkbox that only shows one's own actors shall be shown */
	private boolean mShowMineOnlyCheckbox;
	/** SelectDefScene this GUI is bound to */
	private SelectDefScene mSelectDefScene = null;
	/** Inner widgets */
	private Widgets mWidgets = new Widgets();

	@SuppressWarnings("javadoc")
	private static class Widgets {
		Background topBar = null;
		Search search = new Search();
		Info info = new Info();

		private static class Search {
			TextField field = null;
			AlignTable table = new AlignTable();
		}

		private static class Info {
			AlignTable table = new AlignTable();
			Label name = null;
			Label description = null;
			//			RatingWidget rating = null;
			Label revisedBy = null;
			Label createdBy = null;
			Label date = null;
			//			Label plays = null;
			//			Label likes = null;
			//			Label tags = null;
		}
	}
}
