package com.spiddekauga.voider.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;

/**
 * Creates UI labels
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LabelFactory {
	/**
	 * Only UiFactory should be able to create this class
	 * @param uiStyles the uiStyles to use
	 */
	LabelFactory(UiStyles uiStyles) {
		mStyles = uiStyles;
	}

	/**
	 * Add a panel section label
	 * @param text section label text
	 * @param table the table to add the text to
	 * @param hider optional hider to hide the label
	 * @return label that was created
	 */
	public Label addPanelSection(String text, AlignTable table, GuiHider hider) {
		Label label = new Label(text, LabelStyles.PANEL_SECTION.getStyle());
		table.row().setHeight(mStyles.vars.rowHeightSection);
		table.add(label).setAlign(Vertical.MIDDLE);

		UiFactory.doExtraActionsOnActors(hider, null, label);

		return label;
	}

	/**
	 * Add a section label
	 * @param text section label text
	 * @param table the table to add the text to
	 * @param hider optional hider to hide the label
	 * @return label that was created
	 */
	public Label addSection(String text, AlignTable table, GuiHider hider) {
		return addSection(text, table, hider, null);
	}

	/**
	 * Add a section label
	 * @param text section label text, if null this method does nothing
	 * @param table the table to add the text to
	 * @param hider optional hider to hide the label
	 * @param createdActors optional adds the created label to this array
	 * @return label that was created
	 */
	public Label addSection(String text, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {
		if (text != null) {
			Label label = new Label(text, LabelStyles.DEFAULT.getStyle());
			table.row();
			table.add(label).setHeight(mStyles.vars.rowHeight);

			UiFactory.doExtraActionsOnActors(hider, createdActors, label);
			return label;
		} else {
			return null;
		}
	}

	/**
	 * Create an error label.
	 * @param labelText text to display before the error
	 * @param labelIsSection set to true if the label is a section (different styles are used for true/false).
	 * @param table the table to add the error text to
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return created error label. Can also be accessed via {@link #getLastCreatedErrorLabel()} directly after calling
	 *         this method.
	 */
	public Label addError(String labelText, boolean labelIsSection, AlignTable table, ArrayList<Actor> createdActors) {
		AlignTable wrapTable = new AlignTable();
		wrapTable.setName("error-table");
		table.row();
		table.add(wrapTable).setWidth(mStyles.vars.textFieldWidth);

		Label label = addSection(labelText, wrapTable, null);
		// Change style to error info
		if (!labelIsSection) {
			label.setStyle(mStyles.label.errorSectionInfo);
		}

		// Fill width
		wrapTable.getRows().get(0).setFillWidth(true);
		wrapTable.add().setFillWidth(true);

		// Add error label
		mCreatedErrorLabelLast = new Label("", mStyles.label.errorSection);
		wrapTable.add(mCreatedErrorLabelLast).setAlign(Horizontal.RIGHT, Vertical.MIDDLE);

		UiFactory.doExtraActionsOnActors(null, createdActors, label, mCreatedErrorLabelLast);

		return mCreatedErrorLabelLast;
	}

	/**
	 * Add a descriptive text inside a panel
	 * @param text the text to display
	 * @param table table to display in
	 * @param labelStyle the label style to use
	 * @return created label
	 */
	public Label addPanel(String text, AlignTable table, LabelStyles labelStyle) {
		table.row().setFillWidth(true).setAlign(Vertical.TOP);
		Label label = add(text, true, table, labelStyle);
		label.setName("panel-text");
		label.setAlignment(Align.center);
		table.getCell().setFillWidth(true).setPadTop(mStyles.vars.paddingSeparator).setPadBottom(mStyles.vars.paddingOuter);

		return label;
	}

	/**
	 * Add a label
	 * @param text the text of the label
	 * @param table the table to add the label to
	 * @return created label
	 */
	public Label add(String text, AlignTable table) {
		return add(text, false, table);
	}

	/**
	 * Add a label
	 * @param text the text of the label
	 * @param table the table to add the label to
	 * @param labelStyle style of the label
	 * @return created label
	 */
	public Label add(String text, AlignTable table, LabelStyles labelStyle) {
		return add(text, false, table, labelStyle);
	}


	/**
	 * Add a label
	 * @param text the text of the label
	 * @param wrap if the label should be wrapped
	 * @param table the table to add the label to
	 * @param labelStyle style of the label
	 * @return created label
	 */
	public Label add(String text, boolean wrap, AlignTable table, LabelStyles labelStyle) {
		Label label = create(text, wrap, labelStyle);
		table.add(label);
		return label;
	}

	/**
	 * Add a label
	 * @param text the text of the label
	 * @param wrap if the label should be wrapped
	 * @param table the table to add the label to
	 * @return created label
	 */
	public Label add(String text, boolean wrap, AlignTable table) {
		return add(text, wrap, table, LabelStyles.DEFAULT);
	}

	/**
	 * Create a label which uses the default style
	 * @param text the text of the label
	 * @return created label
	 */
	public Label create(String text) {
		return create(text, false, LabelStyles.DEFAULT);
	}

	/**
	 * Create a label which uses the default style
	 * @param text the text of the label
	 * @param wrap if we want to wrap the text
	 * @return created label
	 */
	public Label create(String text, boolean wrap) {
		return create(text, wrap, LabelStyles.DEFAULT);
	}

	/**
	 * Create a label
	 * @param text the text of the label
	 * @param labelStyle the style to use
	 * @return created label
	 */
	public Label create(String text, LabelStyles labelStyle) {
		return create(text, false, labelStyle);
	}

	/**
	 * Create a label
	 * @param text the text of the label
	 * @param wrap if we want to wrap the text
	 * @param labelStyle the style to use
	 * @return created label
	 */
	public Label create(String text, boolean wrap, LabelStyles labelStyle) {
		Label label = new Label(text, labelStyle.getStyle());
		label.setWrap(wrap);
		return label;
	}

	/**
	 * Add a header label
	 * @param text the text of the header
	 * @param table the table to add the header to
	 * @return created label
	 */
	public Label addHeader(String text, AlignTable table) {
		Label label = new Label(text, LabelStyles.HEADER.getStyle());
		table.row().setAlign(Horizontal.CENTER);
		table.add(label);

		return label;
	}

	/**
	 * @return last created error label
	 */
	public Label getLastCreatedErrorLabel() {
		return mCreatedErrorLabelLast;
	}

	/** Last created error label */
	private Label mCreatedErrorLabelLast = null;
	private UiStyles mStyles;
}
