package com.spiddekauga.voider.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.ColorTintPicker;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.ImageScrollButton;
import com.spiddekauga.utils.scene.ui.ImageScrollButton.ScrollWhen;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.RatingWidget;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.SelectBoxListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.IImageNames;
import com.spiddekauga.voider.resources.SkinNames.ISkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiStyles.ButtonStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.utils.Pools;

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
	 * Create 'update message box' to show an update message dialog
	 * @param message message to display
	 * @param changeLog all new changes to display
	 * @param gui GUI class to create the message box in
	 */
	public void createUpdateMessageBox(final String message, final String changeLog, final Gui gui) {
		MsgBoxExecuter msgBox = gui.getFreeMsgBox(true);

		final int width = Gdx.graphics.getWidth() / 2;
		final int maxHeight = Gdx.graphics.getHeight() / 2;

		msgBox.setTitle("Update Required");
		Label label = new Label(message, mStyles.label.highlight);
		label.setWrap(true);
		label.setWidth(width);
		label.setAlignment(Align.center);
		msgBox.content(label);

		// Add change-log
		msgBox.button("ChangeLog");
		new ButtonListener((Button) msgBox.getButtonCell().getActor()) {
			@Override
			protected void onPressed(Button button) {
				MsgBoxExecuter changeLogMsgBox = gui.getFreeMsgBox(true);
				changeLogMsgBox.setTitle("ChangeLog");
				changeLogMsgBox.content("Changes since your current version", Align.center).padBottom(mStyles.vars.paddingSeparator);
				changeLogMsgBox.contentRow();


				Label label = new Label(changeLog, mStyles.label.standard);
				label.setWrap(true);
				label.setWidth(width);


				// Too high, use scroll pane
				label.layout();
				if (label.getHeight() > maxHeight) {
					ScrollPane scrollPane = new ScrollPane(label, mStyles.scrollPane.noBackground);
					scrollPane.setFadeScrollBars(false);
					changeLogMsgBox.content(scrollPane).size(width, maxHeight);
				} else {
					changeLogMsgBox.content(label);
				}


				changeLogMsgBox.addCancelButtonAndKeys("OK");

				gui.showMsgBox(changeLogMsgBox);
			}
		};

		msgBox.addCancelButtonAndKeys("OK");
		gui.showMsgBox(msgBox);
	}

	/**
	 * Create a tooltip widget
	 * @return new tooltip widget
	 */
	public TooltipWidget createTooltipWidget() {
		Drawable tooltipImage = SkinNames.getDrawable(SkinNames.EditorImages.TOOLTIP);
		ImageButtonStyle youtubeImage = SkinNames.getResource(SkinNames.EditorIcons.YOUTUBE);

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

		doExtraActionsOnActors(hider, createdActors, button);

		return cell;
	}

	/**
	 * Create a comment table
	 * @param username name to display. Can be empty if no name should be displayed
	 * @param comment the comment to display
	 * @param date date of the comment
	 * @param usePadding if there should be padding above the name and date.
	 * @param createdActors (optional) All created actors (except the returned table) is
	 *        added to this. In the specified order: username, comment, date.
	 * @return table with the comment
	 */
	public AlignTable createComment(String username, String comment, String date, boolean usePadding, ArrayList<Actor> createdActors) {
		float width = mStyles.vars.rightPanelWidth;
		AlignTable table = new AlignTable();
		table.setWidth(width);
		Row row = table.row().setFillWidth(true).setAlign(Vertical.BOTTOM).setPadBottom(mStyles.vars.paddingOuter);

		if (usePadding) {
			row.setPadTop(mStyles.vars.paddingInner);
		}

		// Name
		LabelStyle style = SkinNames.getResource(SkinNames.General.LABEL_COMMENT_NAME);
		Label nameLabel = new Label(username, style);
		table.add(nameLabel);

		// Padding
		table.add().setFillWidth(true);

		// Date
		style = SkinNames.getResource(SkinNames.General.LABEL_COMMENT_DATE);
		Label dateLabel = new Label(date, style);
		table.add(dateLabel);

		// Comment
		style = SkinNames.getResource(SkinNames.General.LABEL_COMMENT);
		Label commentLabel = new Label(comment, style);
		commentLabel.setWrap(true);
		table.row();
		table.add(commentLabel).setWidth(width);

		// Add to created actors
		doExtraActionsOnActors(null, createdActors, nameLabel, commentLabel, dateLabel);

		return table;
	}

	/**
	 * Create a scrollable list for all available themes.
	 * @param width available width for scroll pane
	 * @param height available height for scroll pane
	 * @param checkable true if the buttons should be checkable, otherwise they can only
	 *        be pressed.
	 * @param listener listens to the button presses
	 * @param selectedTheme the default theme to be set as selected
	 * @return created scroll pane.
	 */
	public ScrollPane createThemeList(float width, float height, boolean checkable, ButtonListener listener, Themes selectedTheme) {
		AlignTable table = new AlignTable();
		table.setName("theme-table");
		table.setPaddingCellDefault(0, mStyles.vars.paddingInner, 0, 0);
		ScrollPane scrollPane = new ScrollPane(table, mStyles.scrollPane.noBackground);
		scrollPane.setSize(width, height);

		// Calculate button sizes
		float buttonHeight = height - mStyles.vars.rowHeight * 2;
		float ratio = SkinNames.getResource(SkinNames.EditorVars.THEME_DISPLAY_RATIO);
		float buttonWidth = ratio * buttonHeight;

		float topLayerSpeed = SkinNames.getResource(SkinNames.EditorVars.THEME_TOP_LAYER_SPEED);
		float bottomLayerSpeed = SkinNames.getResource(SkinNames.EditorVars.THEME_BOTTOM_LAYER_SPEED);

		@SuppressWarnings("unchecked")
		ArrayList<Actor> createdActors = Pools.arrayList.obtain();

		ButtonGroup buttonGroup = checkable ? new ButtonGroup() : null;

		for (Themes theme : Themes.values()) {
			ButtonStyle buttonStyle;
			if (checkable) {
				buttonStyle = ButtonStyles.TOGGLE.getStyle();
			} else {
				buttonStyle = theme == selectedTheme ? ButtonStyles.SELECTED_PRESSABLE.getStyle() : ButtonStyles.PRESS.getStyle();
			}

			// Create image
			ImageScrollButton button = new ImageScrollButton(buttonStyle, ScrollWhen.ALWAYS);
			button.addListener(listener);
			if (checkable) {
				buttonGroup.add(button);
			}

			// Add layers
			Texture bottomLayer = ResourceCacheFacade.get(theme.getBottomLayer());
			Texture topLayer = ResourceCacheFacade.get(theme.getTopLayer());
			button.addLayer(bottomLayer, bottomLayerSpeed);
			button.addLayer(topLayer, topLayerSpeed);

			// Add to table and get label
			createdActors.clear();
			Cell cell = addIconLabel(button, theme.toString(), Positions.BOTTOM, null, table, null, createdActors);
			cell.setSize(buttonWidth, buttonHeight);
			table.getCell().setWidth(buttonWidth);
			Label label = (Label) createdActors.get(createdActors.size() - 1);

			button.setUserObject(new ThemeSelectorData(theme, label));

			// Set correct selected
			if (theme == selectedTheme) {
				button.setChecked(true);
				label.setStyle(mStyles.label.highlight);
			}
		}

		Pools.arrayList.free(createdActors);

		// Remove padding from last table
		table.getCell().setPadRight(0);
		table.layout();

		return scrollPane;
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
	 * @return Created min and max sliders;
	 */
	public SliderMinMaxWrapper addSliderMinMax(String text, float min, float max, float stepSize, SliderListener minSliderListener,
			SliderListener maxSliderListener, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {
		// Label
		if (text != null) {
			Label label = addPanelSection(text, table, null);
			doExtraActionsOnActors(hider, createdActors, label);
		}

		// Sliders
		Slider minSlider = addSlider("Min", min, max, stepSize, minSliderListener, table, hider, createdActors);
		Slider maxSlider = addSlider("Max", min, max, stepSize, maxSliderListener, table, hider, createdActors);

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
	 * @return created slider element
	 */
	public Slider addSlider(String text, float min, float max, float stepSize, SliderListener sliderListener, AlignTable table, GuiHider hider,
			ArrayList<Actor> createdActors) {
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
		sliderListener.init(slider, textField);

		if (label != null) {
			doExtraActionsOnActors(hider, createdActors, label, slider, textField);
		} else {
			doExtraActionsOnActors(hider, createdActors, slider, textField);
		}

		return slider;
	}

	/**
	 * Add a color slider to the table
	 * @param table the table to add this to
	 * @param createdActors optional, all created actors
	 * @param colors all the colors to show in the color picker
	 * @return created slider
	 */
	public ColorTintPicker addColorTintPicker(AlignTable table, ArrayList<Actor> createdActors, Color... colors) {
		table.row().setFillWidth(true);

		// Picking color
		ColorTintPicker picker = new ColorTintPicker(false, mStyles.slider.colorPicker, colors);
		table.add(picker).setFillWidth(true);

		doExtraActionsOnActors(null, createdActors, picker);

		return picker;
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
	 * @param fillWidth fills the width between text and icon
	 * @param table the table to add the icon to
	 * @param hider optional hider for icon and label
	 * @return created label for the text after the icon
	 */
	public Label addIconLabel(IImageNames icon, String text, boolean fillWidth, AlignTable table, GuiHider hider) {
		table.row().setAlign(Horizontal.LEFT, Vertical.MIDDLE).setFillWidth(fillWidth);

		// Image
		Image image = new Image(SkinNames.getDrawable(icon));
		table.add(image);

		// Label
		Label label = new Label(text, mStyles.label.standard);
		table.add(label).setFillWidth(fillWidth);

		doExtraActionsOnActors(hider, null, image, label);

		return label;
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
	public ImageButton addImageButtonLabel(ISkinNames icon, String text, Positions textPosition, LabelStyle textStyle, AlignTable table,
			GuiHider hider, ArrayList<Actor> createdActors) {
		if (textPosition == Positions.LEFT || textPosition == Positions.RIGHT) {
			table.row().setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		}

		// Actors
		ImageButton imageButton = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));
		addIconLabel(imageButton, text, textPosition, textStyle, table, hider, createdActors);

		return imageButton;
	}

	/**
	 * Add an image button with a label after the button
	 * @param icon the icon to show
	 * @param text text to display somewhere
	 * @param textPosition location of the text relative to the button
	 * @param textStyle optional style of the text (set to null to use default)
	 * @param table the table to add the icon to
	 * @param hider optional hider for icon and label
	 * @param createdActors all created actors
	 * @return created icon
	 */
	public Image addIconLabel(IImageNames icon, String text, Positions textPosition, LabelStyle textStyle, AlignTable table, GuiHider hider,
			ArrayList<Actor> createdActors) {
		if (textPosition == Positions.LEFT || textPosition == Positions.RIGHT) {
			table.row().setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		}

		// Actors
		Image image = new Image(SkinNames.getDrawable(icon));
		addIconLabel(image, text, textPosition, textStyle, table, hider, createdActors);

		return image;
	}

	/**
	 * Add a button label
	 * @param icon the icon to add
	 * @param text text to display somewhere
	 * @param textPosition location of the text relative to the button
	 * @param textStyle style of the text
	 * @param table the table to add the icon to
	 * @param hider optional hider for icon and label
	 * @param createdActors all created actors
	 * @return cell with the icon
	 */
	private Cell addIconLabel(Actor icon, String text, Positions textPosition, LabelStyle textStyle, AlignTable table, GuiHider hider,
			ArrayList<Actor> createdActors) {
		Cell cell = null;

		Label label;
		if (textStyle != null) {
			label = new Label(text, textStyle);
		} else {
			label = new Label(text, mStyles.label.standard);
		}
		label.pack();

		float iconWidth = icon.getWidth();
		if (icon instanceof Layout) {
			iconWidth = ((Layout) icon).getPrefWidth();
		}

		float maxWidth = label.getPrefWidth() > iconWidth ? label.getPrefWidth() : iconWidth;

		AlignTable innerTable = null;

		// Layout correctly
		switch (textPosition) {
		case BOTTOM:
			innerTable = new AlignTable();
			innerTable.setKeepWidth(true).setWidth(maxWidth);
			innerTable.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
			cell = innerTable.add(icon);
			innerTable.row().setHeight(mStyles.vars.rowHeight);
			innerTable.add(label);
			table.add(innerTable);
			doExtraActionsOnActors(hider, createdActors, innerTable);
			break;


		case LEFT:
			table.add(label).setPadRight(mStyles.vars.paddingInner);
			cell = table.add(icon);
			break;

		case RIGHT:
			cell = table.add(icon).setPadRight(mStyles.vars.paddingInner);
			table.add(label);
			break;

		case TOP:
			innerTable = new AlignTable();
			innerTable.setKeepWidth(true).setWidth(maxWidth);
			innerTable.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);
			innerTable.row().setHeight(mStyles.vars.rowHeight);
			innerTable.add(label);
			innerTable.row();
			cell = innerTable.add(icon);
			table.add(innerTable);
			doExtraActionsOnActors(hider, createdActors, innerTable);
			break;
		}

		if (innerTable != null) {
			innerTable.setAlignTable(table.getRow().getAlign());
			doExtraActionsOnActors(hider, createdActors, innerTable, icon, label);
		} else {
			doExtraActionsOnActors(hider, createdActors, icon, label);
		}

		return cell;
	}

	/**
	 * Add button padding to the table
	 * @param table table to add the button padding to
	 */
	public void addButtonPadding(AlignTable table) {
		table.getCell().setPadRight(mStyles.vars.paddingButton);
	}

	/**
	 * Add button padding between all the cells in a row
	 * @param row the row to add padding to
	 */
	public void addButtonPadding(Row row) {
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
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return created checkbox
	 */
	public CheckBox addPanelCheckBox(String text, ButtonListener listener, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {

		table.row().setFillWidth(true);
		Label label = new Label(text, mStyles.label.standard);
		table.add(label);

		table.add().setFillWidth(true);

		CheckBox checkBox = new CheckBox("", CheckBoxStyles.CHECK_BOX.getStyle());
		table.add(checkBox);

		checkBox.addListener(listener);

		doExtraActionsOnActors(hider, createdActors, label, checkBox);

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


		// checkBox.getImageCell().padRight(pad);


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
			Cell cell = table.add(tab.mButton);
			buttonGroup.add(tab.mButton);
			parentHider.addToggleActor(tab.mButton);
			tab.mHider.setButton(tab.mButton);
			parentHider.addChild(tab.mHider);

			if (tab.mButtonListener != null) {
				tab.mButton.addListener(tab.mButtonListener);
			}

			if (checkCommandCreator != null) {
				tab.mButton.addListener(checkCommandCreator);
			}

			if (createdActors != null) {
				createdActors.add(tab.mButton);
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
	 * Add button padding to the last cell
	 * @param position where to add the padding
	 * @param table the table to add the padding to
	 */
	public void addButtonPadding(AlignTable table, Positions position) {
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
	 * Create a default right panel widget
	 * @return default right panel widget
	 */
	public TabWidget createRightPanel() {
		TabWidget tabWidget = new TabWidget();

		// Margin, padding and height
		float topBottomMargin = mStyles.vars.barUpperLowerHeight + mStyles.vars.paddingOuter;
		tabWidget.setMargin(topBottomMargin, mStyles.vars.paddingOuter, topBottomMargin, mStyles.vars.paddingOuter);
		tabWidget.setFillHeight(true);
		tabWidget.setPad(mStyles.vars.paddingInner);
		tabWidget.setContentWidth(mStyles.vars.rightPanelWidth);

		// Alignment
		tabWidget.setAlign(Horizontal.RIGHT, Vertical.TOP).setTabAlign(Horizontal.RIGHT);

		// Background
		tabWidget.setBackground(new Background(mStyles.color.widgetBackground));

		return tabWidget;
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
	 * Creates a radio tab button
	 * @param text text to display on the radio button
	 * @return a new radio tab wrapper instance
	 */
	public TabRadioWrapper createTabRadioWrapper(String text) {
		return new TabRadioWrapper(text);
	}

	/**
	 * Creates a tab button with an image
	 * @param imageName name of the button image
	 * @return a new image tab wrapper instance
	 */
	public TabImageWrapper createTabImageWrapper(ISkinNames imageName) {
		return new TabImageWrapper(imageName);
	}

	/**
	 * Wrapper for a theme and label. Used in theme list selector
	 */
	public class ThemeSelectorData {
		/**
		 * Private constructor, enforces that only UiFactory can create these objects.
		 * @param theme theme that is displayed
		 * @param label text that is displayed below the theme image
		 */
		private ThemeSelectorData(Themes theme, Label label) {
			this.theme = theme;
			this.label = label;
		}

		/** Selected theme */
		public Themes theme;
		/** Label for the theme */
		public Label label;
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

		/**
		 * @return tab button
		 */
		public Button getButton() {
			return mButton;
		}

		/**
		 * @return hider
		 */
		public HideListener getHider() {
			return mHider;
		}

		/**
		 * Set the hide listener. Useful when you want to use something else than the
		 * default hider
		 * @param hider
		 */
		public void setHider(HideListener hider) {
			mHider = hider;
		}

		/**
		 * Sets a button listener
		 * @param buttonListener listens to the button
		 */
		public void setListener(ButtonListener buttonListener) {
			mButtonListener = buttonListener;
		}

		/** Optional button listener */
		private ButtonListener mButtonListener = null;
		/** Tab button */
		protected Button mButton = null;
		/** Hider for the tab */
		private HideListener mHider = new HideListener(true);
	}

	/**
	 * Tab information wrapper
	 */
	public class TabImageWrapper extends TabWrapper {
		/**
		 * Sets the image for the tab
		 * @param imageName name of the image
		 */
		private TabImageWrapper(ISkinNames imageName) {
			mImageName = imageName;
		}

		@Override
		public void createButton() {
			mButton = new ImageButton((ImageButtonStyle) SkinNames.getResource(mImageName));
		}

		/** Image name */
		private ISkinNames mImageName = null;
	}

	/**
	 * Radio button information wrapper
	 */
	public class TabRadioWrapper extends TabWrapper {
		/**
		 * Sets the text for the radio button
		 * @param text text to display
		 */
		private TabRadioWrapper(String text) {
			mText = text;
		}

		@Override
		public void createButton() {
			mButton = new CheckBox(mText, CheckBoxStyles.RADIO.getStyle());
		}

		/** Button text */
		private String mText = null;
	}

	/**
	 * @return UiStyles
	 */
	public UiStyles getStyles() {
		return mStyles;
	}


	/** Current tab widget */
	private TabWidget mTabWidget = null;
	/** Last created error label */
	private Label mCreatedErrorLabelLast = null;
	/** If the factory has been initialized */
	private boolean mInitialized = false;
	/** Contains all styles */
	private UiStyles mStyles = null;
	private static UiFactory mInstance = null;
}
