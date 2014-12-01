package com.spiddekauga.voider.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.ImageScrollButton;
import com.spiddekauga.utils.scene.ui.ImageScrollButton.ScrollWhen;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.ISkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.ButtonStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

/**
 * UI factory for buttons
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ButtonFactory extends BaseFactory {
	/**
	 * Package constructor
	 */
	ButtonFactory() {
		// Does nothing
	}

	/**
	 * Add an image button to the specified table
	 * @param icon name of the image icon
	 * @param table the table to add the image to
	 * @param hider optional hider for the image
	 * @param createdActors adds the image button to this list if not null
	 * @return created image button
	 */
	public ImageButton addImage(ISkinNames icon, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {
		ImageButton imageButton = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));
		table.add(imageButton);

		UiFactory.doExtraActionsOnActors(hider, createdActors, imageButton);

		return imageButton;
	}

	/**
	 * Add an image scroll button to a table
	 * @param scrollWhen when to scroll the images
	 * @param width
	 * @param height
	 * @param style which button style to use
	 * @param table the table to add the button to
	 * @param createdActors optional adds the button ot this list (if not null)
	 * @return created image scroll button
	 */
	public ImageScrollButton addImageScroll(ScrollWhen scrollWhen, float width, float height, ButtonStyles style, AlignTable table,
			ArrayList<Actor> createdActors) {
		ImageScrollButton imageScrollButton = new ImageScrollButton(style.getStyle(), scrollWhen);
		table.add(imageScrollButton).setSize(width, height);

		UiFactory.doExtraActionsOnActors(null, createdActors, imageScrollButton);

		return imageScrollButton;
	}

	/**
	 * Add an image button with a label after the button
	 * @param icon the icon to show
	 * @param text text to display somewhere
	 * @param textPosition location of the text relative to the button
	 * @param textStyle optional text style, set to null to use default
	 * @param table the table to add the icon to
	 * @param hider optional hider for icon and label
	 * @param createdActors all created actors
	 * @return created button
	 */
	public ImageButton addImageWithLabel(ISkinNames icon, String text, Positions textPosition, LabelStyles textStyle, AlignTable table,
			GuiHider hider, ArrayList<Actor> createdActors) {
		if (textPosition == Positions.LEFT || textPosition == Positions.RIGHT) {
			table.row().setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		}

		// Actors
		ImageButton imageButton = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));
		mUiFactory.addIconLabel(imageButton, text, textPosition, textStyle, table, hider, createdActors);

		return imageButton;
	}

	/**
	 * Create a text button. Not that this text button will not use all the extra styles
	 * available in
	 * {@link #addText(String, TextButtonStyles, AlignTable, ButtonListener, GuiHider, ArrayList)}
	 * @param text the text that should be shown in the text button
	 * @param style which button style to use
	 * @return created text button
	 */
	public TextButton createText(String text, TextButtonStyles style) {
		return new TextButton(text, style.getStyle());
	}

	/**
	 * Add a text button to a table and set it to the default size
	 * @param text the text that should be shown in the text button
	 * @param style which button style to use
	 * @param table the table to add the text button to
	 * @param listener optional button listener
	 * @param hider optional GUI hider
	 * @param createdActors optional adds the button to this list (if not null)
	 * @return created text button cell
	 */
	public Cell addText(String text, TextButtonStyles style, AlignTable table, ButtonListener listener, GuiHider hider, ArrayList<Actor> createdActors) {
		TextButton button = createText(text, style);

		Cell cell = table.add(button);

		// Special style properties
		switch (style) {
		// Default size
		case FILLED_PRESS:
		case FILLED_TOGGLE:
			cell.setSize(mStyles.vars.textButtonWidth, mStyles.vars.textButtonHeight);
			break;

			// Slim fit to text
		case LINK:
			button.pack();
			break;

			// Fit to text (but with padding)
		case TAG:
		case TRANSPARENT_PRESS:
		case TRANSPARENT_TOGGLE: {
			button.layout();

			// Set padding around text so it doesn't touch the border
			float cellHeightDefault = cell.getPrefHeight();
			float cellWidthDefault = cell.getPrefWidth();

			float padding = mStyles.vars.paddingTransparentTextButton * 2;
			cell.setSize(cellWidthDefault + padding, cellHeightDefault + padding);


			// Height padding
			if (style == TextButtonStyles.TRANSPARENT_PRESS || style == TextButtonStyles.TRANSPARENT_TOGGLE) {
				float padHeight = (mStyles.vars.rowHeight - cellHeightDefault - padding) * 0.5f;

				// Check for uneven height, then pad extra at the top
				boolean padExtra = false;
				if (padHeight != ((int) padHeight)) {
					padExtra = true;
					padHeight = (int) padHeight;
				}

				float padTop = padExtra ? padHeight + 1 : padHeight;
				float padBottom = padHeight;

				cell.setPadTop(padTop);
				cell.setPadBottom(padBottom);
			}
			break;
		}
		}


		if (listener != null) {
			button.addListener(listener);
		}

		UiFactory.doExtraActionsOnActors(hider, createdActors, button);

		return cell;
	}

	/**
	 * Add button padding to the table
	 * @param table table to add the button padding to
	 */
	public void addPadding(AlignTable table) {
		table.getCell().setPadRight(mStyles.vars.paddingButton);
	}

	/**
	 * Add button padding between all the cells in a row
	 * @param row the row to add padding to
	 */
	public void addPadding(Row row) {
		float padHalf = mStyles.vars.paddingButton * 0.5f;

		for (Cell cell : row.getCells()) {
			cell.setPadLeft(padHalf);
			cell.setPadRight(padHalf);
		}

		// Remove padding for before first and after last buttons
		if (row.getCellCount() > 0) {
			row.getCells().get(0).setPadLeft(0);
			row.getCell().setPadRight(0);
		}
	}

	/**
	 * Add button padding to the last cell
	 * @param position where to add the padding
	 * @param table the table to add the padding to
	 */
	public void addPadding(AlignTable table, Positions position) {
		Cell cell = table.getCell();
		float padding = mStyles.vars.paddingButton;

		switch (position) {
		case BOTTOM:
			cell.setPadBottom(padding);
			break;

		case LEFT:
			cell.setPadLeft(padding);
			break;

		case RIGHT:
			cell.setPadRight(padding);
			break;

		case TOP:
			cell.setPadTop(padding);
			break;
		}
	}

	/**
	 * Adds a tool icon to the specified table
	 * @param icon icon for the tool
	 * @param group the button group the tools belong to
	 * @param table the table to add the tool to
	 * @param createdActors optional adds the tool button to this list (if not null)
	 * @return created tool icon button
	 */
	public ImageButton addTool(ISkinNames icon, ButtonGroup group, AlignTable table, ArrayList<Actor> createdActors) {
		ImageButton button = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));

		if (group != null) {
			group.add(button);
		}
		table.add(button);

		UiFactory.doExtraActionsOnActors(null, createdActors, button);

		return button;
	}

	/**
	 * Adds a tool separator to the specified table
	 * @param table add tool separator
	 */
	public void addToolSeparator(AlignTable table) {
		table.row().setPadBottom(mStyles.vars.paddingOuter);
		// table.add().setPadBottom(mStyles.vars.paddingOuter);
		table.row();
	}

	/**
	 * Adds checkbox padding for the newly added cell
	 * @param table the table the checkbox was added to
	 */
	public void addCheckBoxPadding(AlignTable table) {
		table.getCell().setPadRight(mStyles.vars.paddingCheckBox);
	}

	/**
	 * Adds a single checkbox with text before the checkbox
	 * @param text the text to display before the checkbox
	 * @param listener button listener that listens when it's checked etc
	 * @param table the table to add the checkbox to
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return created checkbox
	 */
	public CheckBox addPanelCheckBox(String text, ButtonListener listener, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {

		table.row().setFillWidth(true);
		Label label = new Label(text, LabelStyles.DEFAULT.getStyle());
		table.add(label);

		table.add().setFillWidth(true);

		CheckBox checkBox = new CheckBox("", CheckBoxStyles.CHECK_BOX.getStyle());
		table.add(checkBox);

		checkBox.addListener(listener);

		UiFactory.doExtraActionsOnActors(hider, createdActors, label, checkBox);

		return checkBox;
	}

	/**
	 * Add a checkbox
	 * @param text the text to display on the checkbox
	 * @param style which checkbox style to use
	 * @param listener listens when the checkbox is checked
	 * @param group button group the checkbox belongs to
	 * @param table the table to add the checkbox to
	 * @return created checkbox
	 */
	public CheckBox addCheckBox(String text, CheckBoxStyles style, ButtonListener listener, ButtonGroup group, AlignTable table) {
		CheckBox checkBox = new CheckBox(text, style.getStyle());
		float imageWidth = checkBox.getImage().getWidth();
		checkBox.getImageCell().width(imageWidth);
		checkBox.getLabelCell().padLeft(mStyles.vars.paddingCheckBoxText);
		checkBox.layout();
		checkBox.left();

		table.add(checkBox);
		group.add(checkBox);

		if (listener != null) {
			checkBox.addListener(listener);
		}

		return checkBox;
	}

	/**
	 * Add a separate checkbox row
	 * @param text the text to display on the checkbox
	 * @param style which checkbox style to use
	 * @param listener listens when the checkbox is checked
	 * @param group button group the checkbox belongs to
	 * @param table the table to add the checkbox to
	 * @return created checkbox
	 */
	public CheckBox addCheckBoxRow(String text, CheckBoxStyles style, ButtonListener listener, ButtonGroup group, AlignTable table) {
		table.row();

		CheckBox checkBox = addCheckBox(text, style, listener, group, table);
		table.getCell().setHeight(mStyles.vars.rowHeight);

		return checkBox;
	}
}
