package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ImageScrollButton.ScrollWhen;
import com.spiddekauga.utils.scene.ui.RatingWidget.RatingWidgetStyle;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.IImageNames;
import com.spiddekauga.voider.resources.SkinNames.ISkinNames;

/**
 * Factory for creating UI objects, more specifically combined UI objects. This factory
 * class gets its default settings from general.json
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UiFactory {
	/**
	 * Creates an empty UI Factory. Call {@link #init()} to initialize all styles
	 */
	private UiFactory() {
		// does nothing
	}

	/**
	 * @return instance of the UiFactory
	 */
	public static UiFactory getInstance() {
		if (mInstance == null) {
			mInstance = new UiFactory();
		}
		return mInstance;
	}

	/**
	 * Create a tooltip widget
	 * @return new tooltip widget
	 */
	public TooltipWidget createTooltipWidget() {
		Drawable tooltipImage = SkinNames.getDrawable(SkinNames.EditorImages.TOOLTIP);
		Drawable youtubeImage = SkinNames.getDrawable(SkinNames.EditorImages.YOUTUBE);

		TooltipWidget tooltipWidget = new TooltipWidget(tooltipImage, youtubeImage, mStyles.label.tooltip, mStyles.vars.paddingInner);
		tooltipWidget.setMargin(mStyles.vars.paddingOuter);

		float width = Gdx.graphics.getWidth() - mStyles.vars.paddingOuter * 2;
		tooltipWidget.setWidth(width);

		return tooltipWidget;
	}

	/**
	 * Add an image button to the specified table
	 * @param icon name of the image icon
	 * @param table the table to add the image to
	 * @param hider optional hider for the image
	 * @param createdActors adds the image button to this list if not null
	 * @return created image button
	 */
	public ImageButton addImageButton(ISkinNames icon, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {
		ImageButton imageButton = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));
		table.add(imageButton);

		doExtraActionsOnActors(hider, createdActors, imageButton);

		return imageButton;
	}

	/**
	 * Add top, bottom, or both bars to the scene
	 * @param barLocation where the bar should be located
	 * @param stage the stage to add the bar to
	 */
	public void addBar(BarLocations barLocation, Stage stage) {
		// Top
		if (barLocation.contains(BarLocations.TOP)) {
			addBar(Gdx.graphics.getHeight() - mStyles.vars.barUpperLowerHeight, stage);
		}

		// Bottom
		if (barLocation.contains(BarLocations.BOTTOM)) {
			addBar(0, stage);
		}
	}

	/**
	 * Adds a bar at the specified coordinate
	 * @param y
	 * @param stage the stage to add the bar to
	 */
	private void addBar(float y, Stage stage) {
		Background background = new Background(mStyles.color.widgetBackground);
		float height = mStyles.vars.barUpperLowerHeight;
		background.setSize(Gdx.graphics.getWidth(), height);
		background.setPosition(0, y);
		stage.addActor(background);
		background.setZIndex(0);
	}

	/**
	 * Adds a window to a table
	 * @param title title of the table. When setting the title it will use the window skin
	 *        with a title, if null it will instead use a default window skin without a
	 *        title
	 * @param innerTable the inner table of the window
	 * @param table the table to add the window to
	 * @param hider optional GUI hider
	 * @param createdActors optional adds the window to this list (if not null)
	 * @return created window
	 */
	public Window addWindow(String title, AlignTable innerTable, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {
		WindowStyle windowStyle;
		if (title != null) {
			windowStyle = mStyles.window.title;
		} else {
			windowStyle = mStyles.window.noTitle;
			title = "";
		}

		float windowPadding = mStyles.vars.paddingInner;

		Window window = new Window(title, windowStyle);
		window.add(innerTable).pad(windowPadding);
		window.layout();
		table.add(window).setSize(innerTable.getWidth() + windowPadding * 2, innerTable.getHeight() + windowPadding * 2);

		doExtraActionsOnActors(hider, createdActors, window);

		return window;
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
	public ImageScrollButton addImageScrollButton(ScrollWhen scrollWhen, float width, float height, ButtonStyles style, AlignTable table,
			ArrayList<Actor> createdActors) {
		ImageScrollButton imageScrollButton = new ImageScrollButton(style.getStyle(), scrollWhen);
		table.add(imageScrollButton).setSize(width, height);

		doExtraActionsOnActors(null, createdActors, imageScrollButton);

		return imageScrollButton;
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
	public Cell addTextButton(String text, TextButtonStyles style, AlignTable table, ButtonListener listener, GuiHider hider,
			ArrayList<Actor> createdActors) {
		TextButton button = new TextButton(text, style.getStyle());

		Cell cell = table.add(button);

		// Fitting text, height padding
		if (style.isFitText()) {
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
		}
		// Else - Use default button size
		else {
			cell.setSize(mStyles.vars.textButtonWidth, mStyles.vars.textButtonHeight);
		}


		if (listener != null) {
			button.addListener(listener);
		}

		doExtraActionsOnActors(hider, createdActors, button);

		return cell;
	}

	/**
	 * @return last created error label
	 */
	public Label getLastCreatedErrorLabel() {
		return mCreatedErrorLabelLast;
	}

	/**
	 * Create an error label.
	 * @param labelText text to display before the error
	 * @param labelIsSection set to true if the label is a section (different styles are
	 *        used for true/false).
	 * @param table the table to add the error text to
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return created error label. Can also be accessed via
	 *         {@link #getLastCreatedErrorLabel()} directly after calling this method.
	 */
	public Label addErrorLabel(String labelText, boolean labelIsSection, AlignTable table, ArrayList<Actor> createdActors) {
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

		doExtraActionsOnActors(null, createdActors, label, mCreatedErrorLabelLast);

		return mCreatedErrorLabelLast;
	}

	/**
	 * Adds a text field with an optional label header
	 * @param sectionText optional text for the label, if null no label is added
	 * @param defaultText default text in the text field
	 * @param listener text field listener
	 * @param table the table to add the text field to
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @param errorLabel set to true to create an error label (only works if sectionText
	 *        isn't null). This label can be accessed by calling
	 *        {@link #getLastCreatedErrorLabel()} directly after this method.
	 * @return Created text field
	 */
	public TextField addTextField(String sectionText, boolean errorLabel, String defaultText, TextFieldListener listener, AlignTable table,
			ArrayList<Actor> createdActors) {
		// Label
		if (sectionText != null) {
			// If error label, wrap the both labels in another table with fixed width
			if (errorLabel) {
				addErrorLabel(sectionText, true, table, createdActors);
			} else {
				addSection(sectionText, table, null, createdActors);
			}
		}

		TextField textField = new TextField(defaultText, mStyles.textField.standard);
		listener.setTextField(textField);
		listener.setDefaultText(defaultText);

		// Set width and height
		table.row();
		table.add(textField).setSize(mStyles.vars.textFieldWidth, mStyles.vars.rowHeight);

		doExtraActionsOnActors(null, createdActors, textField);

		return textField;
	}

	/**
	 * Adds a password text field with an optional label header
	 * @param sectionText optional text for the label, if null no label is added
	 * @param defaultText default text in the text field
	 * @param listener text field listener
	 * @param table the table to add the text field to
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @param errorLabel set to true to create an error label (only works if sectionText
	 *        isn't null). This label can be accessed by calling
	 *        {@link #getLastCreatedErrorLabel()} directly after this method.
	 * @return Created text field
	 */
	public TextField addPasswordField(String sectionText, boolean errorLabel, String defaultText, TextFieldListener listener, AlignTable table,
			ArrayList<Actor> createdActors) {
		TextField textField = addTextField(sectionText, errorLabel, defaultText, listener, table, createdActors);
		textField.setPasswordMode(true);
		textField.setPasswordCharacter('*');
		listener.setTextField(textField);
		return textField;
	}

	/**
	 * Adds a text area with an optional label header
	 * @param sectionText optional text for the label, if null no label is added
	 * @param defaultText default text in the text field
	 * @param listener text field listener
	 * @param table the table to add the text field to
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return Created text field
	 */
	public TextArea addTextArea(String sectionText, String defaultText, TextFieldListener listener, AlignTable table, ArrayList<Actor> createdActors) {
		return addTextArea(sectionText, defaultText, mStyles.vars.textFieldWidth, listener, table, createdActors);
	}

	/**
	 * Adds a text area with an optional label header
	 * @param sectionText optional text for the label, if null no label is added
	 * @param defaultText default text in the text field
	 * @param width set the width of the text area
	 * @param listener text field listener
	 * @param table the table to add the text field to
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return Created text field
	 */
	public TextArea addTextArea(String sectionText, String defaultText, float width, TextFieldListener listener, AlignTable table,
			ArrayList<Actor> createdActors) {
		// Label
		addSection(sectionText, table, null, createdActors);

		TextArea textArea = new TextArea(defaultText, mStyles.textField.standard);
		listener.setTextField(textArea);
		listener.setDefaultText(defaultText);

		// Set width and height
		table.row();
		table.add(textArea).setSize(width, mStyles.vars.textAreaHeight);

		doExtraActionsOnActors(null, createdActors, textArea);

		return textArea;
	}

	/**
	 * Adds a selection box with optional label header
	 * @param <SelectType> Type that's stored in the select box
	 * @param sectionText optional text for the label (if not null)
	 * @param items all selectable items
	 * @param listener selection box listener
	 * @param table the table to add the selection box to
	 * @param createdActors optional adds all created elements to this list
	 * @return created selection box
	 */
	public <SelectType> SelectBox<SelectType> addSelectBox(String sectionText, SelectType[] items, SelectBoxListener listener, AlignTable table,
			ArrayList<Actor> createdActors) {
		addSection(sectionText, table, null, createdActors);

		SelectBox<SelectType> selectBox = new SelectBox<>(mStyles.select.standard);
		selectBox.setItems(items);

		listener.setSelectBox(selectBox);

		table.row();
		table.add(selectBox).setSize(mStyles.vars.textFieldWidth, mStyles.vars.rowHeight);

		doExtraActionsOnActors(null, createdActors, selectBox);

		return selectBox;
	}

	/**
	 * Adds a min and max slider with section text to a table. These sliders are
	 * synchronized.
	 * @param text optional section text for the sliders
	 * @param min minimum value of the sliders
	 * @param max maximum value of the sliders
	 * @param stepSize step size of the sliders
	 * @param minSliderListener slider listener for the min slider
	 * @param maxSliderListener slider listener for the max slider
	 * @param table adds all UI elements to this table
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @param invoker optional adds the ability to undo changes (if not null)
	 * @return Created min and max sliders;
	 */
	public SliderMinMaxWrapper addSliderMinMax(String text, float min, float max, float stepSize, SliderListener minSliderListener,
			SliderListener maxSliderListener, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors, Invoker invoker) {
		// Label
		if (text != null) {
			Label label = addPanelSection(text, table, null);
			doExtraActionsOnActors(hider, createdActors, label);
		}

		// Sliders
		Slider minSlider = addSlider("Min", min, max, stepSize, minSliderListener, table, hider, createdActors, invoker);
		Slider maxSlider = addSlider("Max", min, max, stepSize, maxSliderListener, table, hider, createdActors, invoker);

		minSliderListener.setGreaterSlider(maxSlider);
		maxSliderListener.setLesserSlider(minSlider);

		SliderMinMaxWrapper minMaxWrapper = new SliderMinMaxWrapper();
		minMaxWrapper.min = minSlider;
		minMaxWrapper.max = maxSlider;

		return minMaxWrapper;
	}

	/**
	 * Adds a slider to a table
	 * @param text optional text before the slider (if not null)
	 * @param min minimum value of the slider
	 * @param max maximum value of the slider
	 * @param stepSize step size of the slider
	 * @param sliderListener listens to slider changes
	 * @param table adds all UI elements to this table
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @param invoker optional adds the ability to undo changes (if not null)
	 * @return created slider element
	 */
	public Slider addSlider(String text, float min, float max, float stepSize, SliderListener sliderListener, AlignTable table, GuiHider hider,
			ArrayList<Actor> createdActors, Invoker invoker) {
		if (mStyles == null) {
			throw new IllegalStateException("init() has not been called!");
		}

		table.row();

		// Label
		Label label = null;
		if (text != null) {
			label = new Label(text, mStyles.label.standard);
			table.add(label).setWidth(mStyles.vars.sliderLabelWidth);
		}

		// Slider
		float sliderWidth = mStyles.vars.sliderWidth;
		if (text == null) {
			sliderWidth += mStyles.vars.sliderLabelWidth;
		}
		Slider slider = new Slider(min, max, stepSize, false, mStyles.slider.standard);
		table.add(slider).setWidth(sliderWidth);

		// Text field
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setMaxLength(4);
		table.add(textField).setWidth(mStyles.vars.textFieldNumberWidth);

		// Set slider listener
		sliderListener.init(slider, textField, invoker);

		if (label != null) {
			doExtraActionsOnActors(hider, createdActors, label, slider, textField);
		} else {
			doExtraActionsOnActors(hider, createdActors, slider, textField);
		}

		return slider;
	}

	/**
	 * Add rating widget to the table
	 * @param touchable if the rating can be changed
	 * @param table the table to add the rating widget to
	 * @param hider optional hider for the widget
	 * @return created rating widget
	 */
	public RatingWidget addRatingWidget(Touchable touchable, AlignTable table, GuiHider hider) {
		RatingWidget rating = new RatingWidget(mStyles.rating.stars, 5, touchable);

		table.add(rating);

		doExtraActionsOnActors(hider, null, rating);

		return rating;
	}

	/**
	 * Add a label
	 * @param text the text of the label
	 * @param wrap if the label should be wrapped
	 * @param table the table to add the label to
	 * @param labelStyle style of the label
	 * @return created label
	 */
	public Label addLabel(String text, boolean wrap, AlignTable table, ISkinNames labelStyle) {
		Label label = new Label(text, (LabelStyle) SkinNames.getResource(labelStyle));
		label.setWrap(wrap);
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
	public Label addLabel(String text, boolean wrap, AlignTable table) {
		Label label = new Label(text, mStyles.label.standard);
		label.setWrap(wrap);
		table.add(label);

		return label;
	}

	/**
	 * Add a header label
	 * @param text the text of the header
	 * @param table the table to add the header to
	 * @return created label
	 */
	public Label addHeader(String text, AlignTable table) {
		Label label = new Label(text, mStyles.label.header);
		table.row().setAlign(Horizontal.CENTER);
		table.add(label);

		return label;
	}

	/**
	 * Add an icon with a label
	 * @param icon the icon to show
	 * @param text text after the icon
	 * @param table the table to add the icon to
	 * @param hider optional hider for icon and label
	 * @return created label for the text after the icon
	 */
	public Label addIconLabel(IImageNames icon, String text, AlignTable table, GuiHider hider) {
		table.row().setAlign(Horizontal.LEFT, Vertical.MIDDLE);

		// Image
		Image image = new Image(SkinNames.getDrawable(icon));
		table.add(image);

		// Label
		Label label = new Label(text, mStyles.label.standard);
		table.add(label);

		doExtraActionsOnActors(hider, null, image, label);

		return label;
	}

	/**
	 * Add an image button with a label after the button
	 * @param icon the icon to show
	 * @param text text to display somewhere
	 * @param textPosition location of the text relative to the button
	 * @param table the table to add the icon to
	 * @param hider optional hider for icon and label
	 * @param createdActors all created actors
	 * @return created button
	 */
	public ImageButton addImageButtonLabel(ISkinNames icon, String text, Positions textPosition, AlignTable table, GuiHider hider,
			ArrayList<Actor> createdActors) {
		if (textPosition == Positions.LEFT || textPosition == Positions.RIGHT) {
			table.row().setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		}

		// Actors
		ImageButton imageButton = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));
		Label label = new Label(text, mStyles.label.standard);

		float buttonWidth = imageButton.getPrefWidth();

		// Layout correctly
		switch (textPosition) {
		case BOTTOM: {
			AlignTable innerTable = new AlignTable();
			innerTable.setKeepWidth(true).setWidth(buttonWidth);
			innerTable.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
			innerTable.add(imageButton);
			innerTable.row().setHeight(mStyles.vars.rowHeight);
			innerTable.add(label);
			table.add(innerTable);
			doExtraActionsOnActors(hider, createdActors, innerTable);
			break;
		}

		case LEFT:
			table.add(label).setPadRight(mStyles.vars.paddingInner);
			table.add(label);
			break;

		case RIGHT:
			table.add(imageButton).setPadRight(mStyles.vars.paddingInner);
			table.add(label);
			break;

		case TOP: {
			AlignTable innerTable = new AlignTable();
			innerTable.setKeepWidth(true).setWidth(buttonWidth);
			innerTable.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
			innerTable.row().setHeight(mStyles.vars.rowHeight);
			innerTable.add(label);
			innerTable.row();
			innerTable.add(imageButton);
			table.add(innerTable);
			doExtraActionsOnActors(hider, createdActors, innerTable);
			break;
		}
		}

		doExtraActionsOnActors(hider, createdActors, imageButton, label);

		return imageButton;
	}

	/**
	 * Add button padding to the table
	 * @param table table to add the button padding to
	 */
	public void addButtonPadding(AlignTable table) {
		table.getCell().setPadRight(mStyles.vars.paddingButton);
	}

	/**
	 * Add a panel section label
	 * @param text section label text
	 * @param table the table to add the text to
	 * @param hider optional hider to hide the label
	 * @return label that was created
	 */
	public Label addPanelSection(String text, AlignTable table, GuiHider hider) {
		Label label = new Label(text, mStyles.label.panelSection);
		table.row().setHeight(mStyles.vars.rowHeightSection);
		table.add(label).setAlign(Vertical.MIDDLE);

		doExtraActionsOnActors(hider, null, label);

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
	private Label addSection(String text, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {
		if (text != null) {
			Label label = new Label(text, mStyles.label.standard);
			table.row();
			table.add(label).setHeight(mStyles.vars.rowHeight);

			doExtraActionsOnActors(hider, createdActors, label);
			return label;
		} else {
			return null;
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
	public ImageButton addToolButton(ISkinNames icon, ButtonGroup group, AlignTable table, ArrayList<Actor> createdActors) {
		ImageButton button = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));

		if (group != null) {
			group.add(button);
		}
		table.add(button);

		doExtraActionsOnActors(null, createdActors, button);

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
	public void addCheckboxPadding(AlignTable table) {
		table.getCell().setPadRight(mStyles.vars.paddingCheckBox);
	}

	/**
	 * Adds a single checkbox with text before the checkbox
	 * @param text the text to display before the checkbox
	 * @param listener button listener that listens when it's checked etc
	 * @param table the table to add the checkbox to
	 * @param tooltipText optional tooltip message for all elements (if not null)
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return created checkbox
	 */
	public CheckBox addPanelCheckBox(String text, ButtonListener listener, AlignTable table, String tooltipText, GuiHider hider,
			ArrayList<Actor> createdActors) {

		table.row().setFillWidth(true);
		Label label = new Label(text, mStyles.label.standard);
		table.add(label);

		table.add().setFillWidth(true);

		CheckBox checkBox = new CheckBox("", CheckBoxStyles.CHECK_BOX.getStyle());
		table.add(checkBox);

		checkBox.addListener(listener);

		doExtraActionsOnActors(tooltipText, hider, createdActors, label, checkBox);

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

	/**
	 * Add an empty tab widget. Lets you add tabs to widget by calling
	 * @param table where to add the tab widget
	 * @return the created tab widget
	 */
	public TabWidget startTabWidget(AlignTable table) {
		table.row();
		mTabWidget = new TabWidget();
		mTabWidget.setFillHeight(true);
		mTabWidget.setBackground(new Background(mStyles.color.widgetBackground));
		mTabWidget.setContentWidth(mStyles.vars.rightPanelWidth);
		table.add(mTabWidget);
		return mTabWidget;
	}

	/**
	 * Ends the current tab widget
	 */
	public void endTabWidget() {
		mTabWidget = null;
	}

	/**
	 * Add a tab to the created tab widget
	 * @param icon the image of the tab
	 * @param table will show this table when this tab is selected
	 * @param hider optional listens to the hider.
	 */
	public void addTab(ISkinNames icon, AlignTable table, HideListener hider) {
		ImageButtonStyle style = SkinNames.getResource(icon);

		if (hider == null) {
			hider = new HideListener(true);
		}
		mTabWidget.addTab(style, table, hider);
	}

	/**
	 * Create generic tabs for a table.
	 * @param table adds the tabs to this table
	 * @param parentHider parent hider for all tab hiders
	 * @param tabs tab information for all tabs to create, will set the button for these
	 * @param createdActors optional adds all tabs to this list (if not null)
	 * @param invoker optional ability to undo which tab is selected (if not null)
	 */
	public void addTabs(AlignTable table, GuiHider parentHider, ArrayList<TabWrapper> tabs, ArrayList<Actor> createdActors, Invoker invoker) {
		GuiCheckCommandCreator checkCommandCreator = null;
		if (invoker != null) {
			checkCommandCreator = new GuiCheckCommandCreator(invoker);
		}
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.setMinCheckCount(1);
		buttonGroup.setMaxCheckCount(1);

		table.row();

		for (TabWrapper tab : tabs) {
			tab.createButton();
			Cell cell = table.add(tab.button);
			buttonGroup.add(tab.button);
			parentHider.addToggleActor(tab.button);
			if (tab.hider != null) {
				tab.hider.setButton(tab.button);
				parentHider.addChild(tab.hider);
			}

			if (checkCommandCreator != null) {
				tab.button.addListener(checkCommandCreator);
			}

			if (createdActors != null) {
				createdActors.add(tab.button);
			}


			// Special tab handling
			// Radio button - padding
			if (tab instanceof TabRadioWrapper) {
				// Add padding if not last button
				if (tabs.indexOf(tab) != tabs.size() - 1) {
					cell.setPadRight(mStyles.vars.paddingCheckBox);
				}
			}
		}
	}

	/**
	 * Set tooltip, add actors to hider, add to created actors, or any combination.
	 * @param tooltipText creates a tooltip for all actors (if not null)
	 * @param hider add all actors to the hider (if not null)
	 * @param createdActors add all actors to this list (if not null)
	 * @param actors all actors that should be processed
	 */
	@Deprecated
	private void doExtraActionsOnActors(String tooltipText, GuiHider hider, ArrayList<Actor> createdActors, Actor... actors) {
		// Tooltip
		if (tooltipText != null) {
			for (Actor actor : actors) {
				new TooltipListener(actor, tooltipText);
			}
		}

		// Hider
		if (hider != null) {
			for (Actor actor : actors) {
				hider.addToggleActor(actor);
			}
		}

		// Add created actors
		if (createdActors != null) {
			for (Actor actor : actors) {
				createdActors.add(actor);
			}
		}
	}

	/**
	 * Set tooltip, add actors to hider, add to created actors, or any combination.
	 * @param hider add all actors to the hider (if not null)
	 * @param createdActors add all actors to this list (if not null)
	 * @param actors all actors that should be processed
	 */
	private void doExtraActionsOnActors(GuiHider hider, ArrayList<Actor> createdActors, Actor... actors) {
		// Hider
		if (hider != null) {
			for (Actor actor : actors) {
				hider.addToggleActor(actor);
			}
		}

		// Add created actors
		if (createdActors != null) {
			for (Actor actor : actors) {
				createdActors.add(actor);
			}
		}
	}

	/**
	 * @return true if the UiFactory has been initialized
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * Initializes the UiFactory
	 */
	public void init() {
		if (mInitialized) {
			return;
		}

		mStyles = new UiStyles();

		mStyles.slider.standard = SkinNames.getResource(SkinNames.General.SLIDER_DEFAULT);
		mStyles.textField.standard = SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT);
		mStyles.label.standard = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);
		mStyles.label.panelSection = SkinNames.getResource(SkinNames.General.LABEL_PANEL_SECTION);
		mStyles.label.errorSectionInfo = SkinNames.getResource(SkinNames.General.LABEL_ERROR_SECTION_INFO);
		mStyles.label.errorSection = SkinNames.getResource(SkinNames.General.LABEL_ERROR_SECTION);
		mStyles.label.error = SkinNames.getResource(SkinNames.General.LABEL_ERROR);
		mStyles.label.highlight = SkinNames.getResource(SkinNames.General.LABEL_HIGHLIGHT);
		mStyles.label.tooltip = SkinNames.getResource(SkinNames.General.LABEL_TOOLTIP);
		mStyles.label.header = SkinNames.getResource(SkinNames.General.LABEL_HEADER);
		mStyles.select.standard = SkinNames.getResource(SkinNames.General.SELECT_BOX_DEFAULT);
		mStyles.rating.stars = SkinNames.getResource(SkinNames.General.RATING_DEFAULT);
		mStyles.window.title = SkinNames.getResource(SkinNames.General.WINDOW_TITLE);
		mStyles.window.noTitle = SkinNames.getResource(SkinNames.General.WINDOW_NO_TITLE);
		mStyles.scrollPane.windowBackground = SkinNames.getResource(SkinNames.General.SCROLL_PANE_WINDOW_BACKGROUND);
		mStyles.scrollPane.noBackground = SkinNames.getResource(SkinNames.General.SCROLL_PANE_DEFAULT);


		// Colors
		mStyles.color.sceneBackground = SkinNames.getResource(SkinNames.GeneralVars.SCENE_BACKGROUND_COLOR);
		mStyles.color.widgetBackground = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR);
		mStyles.color.widgetInnerBackground = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_INNER_BACKGROUND_COLOR);

		// Vars
		mStyles.vars.barUpperLowerHeight = SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT);
		mStyles.vars.paddingButton = SkinNames.getResource(SkinNames.GeneralVars.PADDING_BUTTONS);
		mStyles.vars.paddingCheckBox = SkinNames.getResource(SkinNames.GeneralVars.PADDING_CHECKBOX);
		mStyles.vars.paddingOuter = SkinNames.getResource(SkinNames.GeneralVars.PADDING_OUTER);
		mStyles.vars.paddingInner = SkinNames.getResource(SkinNames.GeneralVars.PADDING_INNER);
		mStyles.vars.paddingExplore = SkinNames.getResource(SkinNames.GeneralVars.PADDING_EXPLORE);
		mStyles.vars.paddingTransparentTextButton = SkinNames.getResource(SkinNames.GeneralVars.PADDING_TRANSPARENT_TEXT_BUTTON);
		mStyles.vars.textFieldNumberWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_NUMBER_WIDTH);
		mStyles.vars.textFieldWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_WIDTH);
		mStyles.vars.sliderWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_WIDTH);
		mStyles.vars.sliderLabelWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_LABEL_WIDTH);
		mStyles.vars.rowHeight = SkinNames.getResource(SkinNames.GeneralVars.ROW_HEIGHT);
		mStyles.vars.rowHeightSection = SkinNames.getResource(SkinNames.GeneralVars.ROW_HEIGHT_SECTION);
		mStyles.vars.textAreaHeight = SkinNames.getResource(SkinNames.GeneralVars.TEXT_AREA_HEIGHT);
		mStyles.vars.textButtonHeight = SkinNames.getResource(SkinNames.GeneralVars.TEXT_BUTTON_HEIGHT);
		mStyles.vars.textButtonWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_BUTTON_WIDTH);
		mStyles.vars.rightPanelWidth = SkinNames.getResource(SkinNames.GeneralVars.RIGHT_PANEL_WIDTH);

		// Button styles
		ButtonStyles.PRESS.setStyle((ButtonStyle) SkinNames.getResource(SkinNames.General.BUTTON_PRESS));
		ButtonStyles.TOGGLE.setStyle((ButtonStyle) SkinNames.getResource(SkinNames.General.BUTTON_TOGGLE));

		// Text buttons
		TextButtonStyles.FILLED_PRESS.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_FLAT_PRESS));
		TextButtonStyles.FILLED_TOGGLE.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_FLAT_TOGGLE));
		TextButtonStyles.TRANSPARENT_PRESS.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TRANSPARENT_PRESS));
		TextButtonStyles.TRANSPARENT_TOGGLE.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TRANSPARENT_TOGGLE));
		TextButtonStyles.TAG.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TAG));

		// Checkbox styles
		CheckBoxStyles.CHECK_BOX.setStyle((CheckBoxStyle) SkinNames.getResource(SkinNames.General.CHECK_BOX_DEFAULT));
		CheckBoxStyles.RADIO.setStyle((CheckBoxStyle) SkinNames.getResource(SkinNames.General.CHECK_BOX_RADIO));
	}

	/**
	 * Different positions
	 */
	@SuppressWarnings("javadoc")
	public enum Positions {
		LEFT,
		RIGHT,
		TOP,
		BOTTOM,
	}

	/**
	 * Different bar locations
	 */
	public enum BarLocations {
		/** Top of the screen */
		TOP,
		/** Bottom of the screen */
		BOTTOM,
		/** Both top and bottom */
		TOP_BOTTOM,

		;
		/**
		 * If the location contains the specified enumeration name. E.g. if singleLocation
		 * is TOP it will return true for the TOP and TOP_BOTTOM enumerations.
		 * @param singleLocation should be either TOP or BOTTOM.
		 * @return true if the location contains the specified location.
		 */
		private boolean contains(BarLocations singleLocation) {
			return name().contains(singleLocation.name());
		}

	}

	/**
	 * Different check box styles
	 */
	public enum CheckBoxStyles {
		/** Regular check box */
		CHECK_BOX,
		/** Radio button */
		RADIO,

		;

		/**
		 * Set the Scene2D text button style
		 * @param style the text button style
		 */
		private void setStyle(CheckBoxStyle style) {
			mStyle = style;
		}

		/**
		 * @return get the text button style associated with this enumeration
		 */
		private CheckBoxStyle getStyle() {
			return mStyle;
		}

		/** The style variable */
		private CheckBoxStyle mStyle = null;
	}

	/**
	 * Different button styles
	 */
	public enum ButtonStyles {
		/** Default press */
		PRESS,
		/** Default toggle */
		TOGGLE,

		;

		/**
		 * Set the Scene2D text button style
		 * @param style the text button style
		 */
		private void setStyle(ButtonStyle style) {
			mStyle = style;
		}

		/**
		 * @return get the text button style associated with this enumeration
		 */
		private ButtonStyle getStyle() {
			return mStyle;
		}


		/** Style for the button */
		ButtonStyle mStyle = null;
	}

	/**
	 * Different text button styles
	 */
	public enum TextButtonStyles {
		/** Filled with default color, can be pressed */
		FILLED_PRESS,
		/** Filled with default color, can be toggled/checked */
		FILLED_TOGGLE,
		/** Transparent (only text is visible), can be pressed */
		TRANSPARENT_PRESS(true),
		/** Transparent (only text is visible), can be toggled/checked */
		TRANSPARENT_TOGGLE(true),
		/** Tag button */
		TAG(true),

		;

		/**
		 * Default constructor
		 */
		private TextButtonStyles() {
			// Does nothing
		}

		/**
		 * Sets if the button should fit the text
		 * @param fitText true if the button should fit the text
		 */
		private TextButtonStyles(boolean fitText) {
			mFitText = fitText;
		}

		/**
		 * Set the Scene2D text button style
		 * @param style the text button style
		 */
		private void setStyle(TextButtonStyle style) {
			mStyle = style;
		}

		/**
		 * @return get the text button style associated with this enumeration
		 */
		private TextButtonStyle getStyle() {
			return mStyle;
		}

		/**
		 * @return true if the button should fit the text
		 */
		private boolean isFitText() {
			return mFitText;
		}

		/** The style variable */
		private TextButtonStyle mStyle = null;
		/** Button should fit the text */
		private boolean mFitText = false;
	}

	/**
	 * @return a new radio tab wrapper instance
	 */
	public TabRadioWrapper createTabRadioWrapper() {
		return new TabRadioWrapper();
	}

	/**
	 * @return a new image tab wrapper instance
	 */
	public TabImageWrapper createTabImageWrapper() {
		return new TabImageWrapper();
	}

	/**
	 * Wrapper for min and max sliders
	 */
	public class SliderMinMaxWrapper {
		/** Minimum slider */
		public Slider min;
		/** Maximum slider */
		public Slider max;
	}

	/**
	 * Interface for creating tab-like buttons
	 */
	public abstract class TabWrapper {
		/**
		 * Creates the button for the tab.
		 */
		abstract void createButton();

		/** Tab button */
		public Button button = null;
		/** Optional Hider for the tab */
		public HideListener hider = null;
	}

	/**
	 * Tab information wrapper
	 */
	public class TabImageWrapper extends TabWrapper {
		@Override
		public void createButton() {
			button = new ImageButton((ImageButtonStyle) SkinNames.getResource(imageName));
		}

		/** Image name */
		public ISkinNames imageName = null;
	}

	/**
	 * Radio button information wrapper
	 */
	public class TabRadioWrapper extends TabWrapper {
		@Override
		public void createButton() {
			button = new CheckBox(text, CheckBoxStyles.RADIO.getStyle());
		}

		/** Button text */
		public String text = null;
	}

	/**
	 * @return UiStyles
	 */
	public UiStyles getStyles() {
		return mStyles;
	}

	/**
	 * Container for all ui styles
	 */
	@SuppressWarnings("javadoc")
	public static class UiStyles {
		public Sliders slider = new Sliders();
		public TextFields textField = new TextFields();
		public Labels label = new Labels();
		public CheckBoxes checkBox = new CheckBoxes();
		public Variables vars = new Variables();
		public SelectBoxes select = new SelectBoxes();
		public Colors color = new Colors();
		public Ratings rating = new Ratings();
		public Windows window = new Windows();
		public ScrollPanes scrollPane = new ScrollPanes();

		public static class ScrollPanes {
			public ScrollPaneStyle noBackground = null;
			public ScrollPaneStyle windowBackground = null;
		}

		public static class Ratings {
			public RatingWidgetStyle stars = null;
		}

		public static class SelectBoxes {
			public SelectBoxStyle standard = null;
		}

		public static class Colors {
			public Color sceneBackground = null;
			public Color widgetBackground = null;
			public Color widgetInnerBackground = null;
		}

		public static class Variables {
			public float barUpperLowerHeight = 0;
			public float textFieldNumberWidth = 0;
			public float sliderWidth = 0;
			public float sliderLabelWidth = 0;
			public float paddingButton = 0;
			public float paddingCheckBox = 0;
			public float paddingOuter = 0;
			public float paddingInner = 0;
			public float paddingExplore = 0;
			public float paddingTransparentTextButton = 0;
			public float rowHeight = 0;
			public float rowHeightSection = 0;
			public float textAreaHeight = 0;
			public float textFieldWidth = 0;
			public float textButtonHeight = 0;
			public float textButtonWidth = 0;
			public float rightPanelWidth = 0;
		}

		public static class Sliders {
			public SliderStyle standard = null;
		}

		public static class TextFields {
			public TextFieldStyle standard = null;
		}

		public static class Labels {
			public LabelStyle standard = null;
			public LabelStyle panelSection = null;
			public LabelStyle errorSectionInfo = null;
			public LabelStyle errorSection = null;
			public LabelStyle error = null;
			public LabelStyle highlight = null;
			public LabelStyle tooltip = null;
			public LabelStyle header = null;
		}

		public static class CheckBoxes {
			// public CheckBoxStyle radio = null;
			// public CheckBoxStyle checkBox = null;
		}

		public static class Windows {
			public WindowStyle title = null;
			public WindowStyle noTitle = null;
		}
	}

	/** Current tab widget */
	private TabWidget mTabWidget = null;
	/** Last created error label */
	private Label mCreatedErrorLabelLast = null;
	/** If the factory has been initialized */
	private boolean mInitialized = false;
	/** All skins and styles */
	private UiStyles mStyles = null;
	/** Instance of the Ui Factory */
	private static UiFactory mInstance = null;
}
