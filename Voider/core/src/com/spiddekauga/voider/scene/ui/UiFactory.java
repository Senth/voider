package com.spiddekauga.voider.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.utils.Maths.MagnitudeWrapper;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.ColorTintPicker;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.ImageScrollButton;
import com.spiddekauga.utils.scene.ui.RatingWidget;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.ScrollWhen;
import com.spiddekauga.utils.scene.ui.SelectBoxListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget;
import com.spiddekauga.voider.game.LevelBackground;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.repo.analytics.listener.AnalyticsColorTintPickerListener;
import com.spiddekauga.voider.repo.analytics.listener.AnalyticsSliderListener;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.scene.ui.UiStyles.ButtonStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;

import java.util.ArrayList;

/**
 * Factory for creating UI objects, more specifically combined UI objects. This factory class gets
 * its default settings from general.json
 */
public class UiFactory implements Disposable {
private static UiFactory mInstance = null;
/** Create labels and text */
public LabelFactory text = new LabelFactory();
/** Create buttons */
public ButtonFactory button = new ButtonFactory();
/** Create message boxes */
public MsgBoxFactory msgBox = new MsgBoxFactory();
/** Contains all styles */
UiStyles mStyles = null;
/** If the factory has been initialized */
private boolean mInitialized = false;

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
	ImageButtonStyle youtubeImage = SkinNames.getResource(SkinNames.EditorIcons.YOUTUBE);

	TooltipWidget tooltipWidget = new TooltipWidget(tooltipImage, youtubeImage, LabelStyles.TOOLTIP.getStyle(), mStyles.vars.paddingInner);
	tooltipWidget.setMargin(mStyles.vars.paddingOuter);

	float width = Gdx.graphics.getWidth() - mStyles.vars.paddingOuter * 2;
	tooltipWidget.setWidth(width);

	return tooltipWidget;
}

/**
 * Add top, bottom, or both bars to the scene
 * @param barLocation where the bar should be located
 * @param moveToBack set to true to move the bar to the back
 * @param stage the stage to add the bar to
 */
public void addBar(BarLocations barLocation, boolean moveToBack, Stage stage) {
	// Top
	if (barLocation.contains(BarLocations.TOP)) {
		addSingleBar(BarLocations.TOP, moveToBack, stage);
	}

	// Bottom
	if (barLocation.contains(BarLocations.BOTTOM)) {
		addSingleBar(BarLocations.BOTTOM, moveToBack, stage);
	}
}

/**
 * Adds a bar at the specified coordinate
 * @param singleLocation one location to add the bar to
 * @param moveToBack set ot true to move the back to the back
 * @param stage the stage to add the bar to
 */
private void addSingleBar(final BarLocations singleLocation, boolean moveToBack, Stage stage) {
	final float height = mStyles.vars.barUpperLowerHeight;
	Background background = new Background(mStyles.color.widgetBackground) {
		@Override
		public void validate() {
			// Set width
			setWidth(Gdx.graphics.getWidth());

			// Set y position
			float y = 0;
			if (singleLocation == BarLocations.TOP) {
				y = Gdx.graphics.getHeight() - height;
			}
			setPosition(0, y);

			super.validate();
		}
	};
	background.setHeight(height);
	stage.addActor(background);
	if (moveToBack) {
		background.setZIndex(0);
	}
}

/**
 * Adds a window to a table
 * @param title title of the table. When setting the title it will use the window skin with a title,
 * if null it will instead use a default window skin without a title
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
 * Add actors to hider, add to created actors, or any combination.
 * @param hider add all actors to the hider (if not null)
 * @param createdActors add all actors to this list (if not null)
 * @param actors all actors that should be processed
 */
static void doExtraActionsOnActors(GuiHider hider, ArrayList<Actor> createdActors, Actor... actors) {
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
 * Create a comment table
 * @param username name to display. Can be empty if no name should be displayed
 * @param comment the comment to display
 * @param date date of the comment
 * @param usePadding if there should be padding above the name and date.
 * @param createdActors (optional) All created actors (except the returned table) is added to this.
 * In the specified order: username, comment, date.
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
 * @param checkable true if the buttons should be checkable, otherwise they can only be pressed.
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

	ArrayList<Actor> createdActors = new ArrayList<>();

	ButtonGroup<ImageScrollButton> buttonGroup = checkable ? new ButtonGroup<ImageScrollButton>() : null;

	for (Themes theme : Themes.values()) {
		ButtonStyle buttonStyle;
		if (checkable) {
			buttonStyle = ButtonStyles.TOGGLE.getStyle();
		} else {
			buttonStyle = theme == selectedTheme ? ButtonStyles.SELECTED_PRESSABLE.getStyle() : ButtonStyles.PRESS.getStyle();
		}

		// Create image
		ImageScrollButton button = new ImageScrollButton(buttonStyle, ScrollWhen.ALWAYS);
		if (checkable) {
			buttonGroup.add(button);
		}

		// Add to table and get label
		createdActors.clear();
		Cell cell = addIconLabel(button, theme.toString(), Positions.BOTTOM, null, table, null, createdActors);
		cell.setSize(buttonWidth, buttonHeight);
		table.getCell().setWidth(buttonWidth);
		Label label = (Label) createdActors.get(createdActors.size() - 1);

		button.setUserObject(new ThemeSelectorData(theme, label));

		// Add layers
		LevelBackground levelBackground = theme.createBackground((int) button.getHeight());
		button.addLayer(levelBackground.getBottomLayer(), bottomLayerSpeed);
		button.addLayer(levelBackground.getTopLayer(), topLayerSpeed);

		// Set correct selected
		if (theme == selectedTheme) {
			button.setChecked(true);
			label.setStyle(LabelStyles.HIGHLIGHT.getStyle());
		}
		button.addListener(listener);
	}

	// Remove padding from last table
	table.getCell().setPadRight(0);
	table.layout();

	return scrollPane;
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
Cell addIconLabel(Actor icon, String text, Positions textPosition, LabelStyles textStyle, AlignTable table, GuiHider hider,
				  ArrayList<Actor> createdActors) {
	Cell cell = null;

	Label label;
	if (textStyle != null) {
		label = new Label(text, textStyle.getStyle());
	} else {
		label = new Label(text, LabelStyles.ICON.getStyle());
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
 * Adds a password text field with an optional label header
 * @param sectionText optional text for the label, if null no label is added
 * @param defaultText default text in the text field
 * @param listener text field listener
 * @param table the table to add the text field to
 * @param createdActors optional adds all created elements to this list (if not null)
 * @param errorLabel set to true to create an error label (only works if sectionText isn't null).
 * This label can be accessed by calling {@link LabelFactory#getLastCreatedErrorLabel()} directly
 * after this method.
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
 * Adds a text field with an optional label header
 * @param sectionText optional text for the label, if null no label is added
 * @param errorLabel set to true to create an error label (only works if sectionText isn't null).
 * This label can be accessed by calling {@link LabelFactory#getLastCreatedErrorLabel()} directly
 * after this method.
 * @param defaultText default text in the text field
 * @param listener text field listener
 * @param table the table to add the text field to
 * @param createdActors optional adds all created elements to this list (if not null)
 * @return Created text field
 */
public TextField addTextField(String sectionText, boolean errorLabel, String defaultText, TextFieldListener listener, AlignTable table,
							  ArrayList<Actor> createdActors) {
	return addTextField(sectionText, errorLabel, defaultText, mStyles.vars.textFieldWidth, listener, table, createdActors);
}

/**
 * Adds a text field with an optional label header
 * @param sectionText optional text for the label, if null no label is added
 * @param errorLabel set to true to create an error label (only works if sectionText isn't null).
 * This label can be accessed by calling {@link LabelFactory#getLastCreatedErrorLabel()} directly
 * after this method.
 * @param defaultText default text in the text field
 * @param width set the width of the text field
 * @param listener text field listener
 * @param table the table to add the text field to
 * @param createdActors optional adds all created elements to this list (if not null)
 * @return Created text field
 */
public TextField addTextField(String sectionText, boolean errorLabel, String defaultText, float width, TextFieldListener listener,
							  AlignTable table, ArrayList<Actor> createdActors) {
	TextField textField = new TextField(defaultText, mStyles.textField.standard);
	addTextField(textField, sectionText, errorLabel, defaultText, width, listener, table, createdActors);
	return textField;
}

/**
 * Adds a text field with an optional label header
 * @param textField the text field or area that will be added
 * @param sectionText optional text for the label, if null no label is added
 * @param errorLabel set to true to create an error label (only works if sectionText isn't null).
 * This label can be accessed by calling {@link LabelFactory#getLastCreatedErrorLabel()} directly
 * after this method.
 * @param defaultText default text in the text field
 * @param width the width of the text field
 * @param listener text field listener
 * @param table the table to add the text field to
 * @param createdActors optional adds all created elements to this list (if not null)
 */
private void addTextField(TextField textField, String sectionText, boolean errorLabel, String defaultText, float width,
						  TextFieldListener listener, AlignTable table, ArrayList<Actor> createdActors) {
	// Label
	if (sectionText != null) {
		// If error label, wrap the both labels in another table with fixed width
		if (errorLabel) {
			text.addError(sectionText, true, table, createdActors);
		} else {
			text.addSection(sectionText, table, null, createdActors);
		}
		table.getRow().setWidth(width);
		table.getCell().setWidth(width);
	}

	if (listener != null) {
		listener.setTextField(textField);
		listener.setDefaultText(defaultText);
	}

	// Set width and height
	table.row();
	table.add(textField).setSize(width, mStyles.vars.rowHeight);

	doExtraActionsOnActors(null, createdActors, textField);
}

/**
 * Adds a text area with an optional label header
 * @param sectionText optional text for the label, if null no label is added
 * @param defaultText default text in the text field
 * @param listener text field listener
 * @param table the table to add the text field to
 * @param createdActors optional adds all created elements to this list (if not null)
 * @param errorLabel set to true to create an error label (only works if sectionText isn't null).
 * This label can be accessed by calling {@link LabelFactory#getLastCreatedErrorLabel()} directly
 * after this method.
 * @return Created text field
 */
public TextArea addTextArea(String sectionText, boolean errorLabel, String defaultText, TextFieldListener listener, AlignTable table,
							ArrayList<Actor> createdActors) {
	return addTextArea(sectionText, false, defaultText, mStyles.vars.textFieldWidth, listener, table, createdActors);
}

/**
 * Adds a text area with an optional label header
 * @param sectionText optional text for the label, if null no label is added
 * @param defaultText default text in the text field
 * @param width set the width of the text area
 * @param listener text field listener
 * @param table the table to add the text field to
 * @param createdActors optional adds all created elements to this list (if not null)
 * @param errorLabel set to true to create an error label (only works if sectionText isn't null).
 * This label can be accessed by calling {@link LabelFactory#getLastCreatedErrorLabel()} directly
 * after this method.
 * @return Created text field
 */
public TextArea addTextArea(String sectionText, boolean errorLabel, String defaultText, float width, TextFieldListener listener, AlignTable table,
							ArrayList<Actor> createdActors) {
	TextArea textArea = new TextArea("", mStyles.textField.standard);
	addTextField(textArea, sectionText, errorLabel, defaultText, width, listener, table, createdActors);
	table.getCell().setSize(width, mStyles.vars.textAreaHeight);
	return textArea;
}

/**
 * Adds a selection box with optional label header
 * @param <SelectType> Type that's stored in the select box
 * @param sectionText optional text for the label (if not null)
 * @param items all selectable items
 * @param listener selection box listener
 * @param table the table to add the selection box to
 * @param hider optional hider for the select box
 * @param createdActors optional adds all created elements to this list
 * @return created selection box
 */
public <SelectType> SelectBox<SelectType> addSelectBox(String sectionText, SelectType[] items, SelectBoxListener<SelectType> listener,
													   AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {
	if (sectionText != null) {
		text.addSection(sectionText, table, hider, createdActors);
	}

	SelectBox<SelectType> selectBox = new SelectBox<>(mStyles.select.standard);
	selectBox.setItems(items);

	listener.setSelectBox(selectBox);

	table.row();
	table.add(selectBox).setSize(mStyles.vars.textFieldWidth, mStyles.vars.rowHeight);

	doExtraActionsOnActors(hider, createdActors, selectBox);

	return selectBox;
}

/**
 * Adds a min and max slider with section text to a table. These sliders are synchronized.
 * @param text optional section text for the sliders
 * @param name analytics event name if null will use 'text' as event name.
 * @param min minimum value of the sliders
 * @param max maximum value of the sliders
 * @param stepSize step size of the sliders
 * @param minSliderListener slider listener for the min slider
 * @param maxSliderListener slider listener for the max slider
 * @param table adds all UI elements to this table
 * @param hider optional hider to add the elements to (if not null)
 * @param createdActors optional adds all created elements to this list (if not null)
 * @return Created min and max sliders;
 * @throw IllegalArgumentException if 'text' and 'name' are null or empty
 */
public SliderMinMaxWrapper addSliderMinMax(String text, String name, float min, float max, float stepSize, SliderListener minSliderListener,
										   SliderListener maxSliderListener, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors) {
	if ((text == null || text.isEmpty()) && (name == null || name.isEmpty())) {
		throw new IllegalArgumentException("Both 'text' and 'name' parameters are null or empty");
	}

	// Label
	if (text != null) {
		Label label = this.text.addPanelSection(text, table, null);
		doExtraActionsOnActors(hider, createdActors, label);
	}

	// Event name
	String eventName = name;
	if (name == null || name.isEmpty()) {
		eventName = text;
	}

	// Sliders
	Slider minSlider = addSlider("Min", eventName + "_min", min, max, stepSize, minSliderListener, table, hider, createdActors);
	Slider maxSlider = addSlider("Max", eventName + "_max", min, max, stepSize, maxSliderListener, table, hider, createdActors);

	minSliderListener.setGreaterSlider(maxSliderListener);
	maxSliderListener.setLesserSlider(minSliderListener);

	SliderMinMaxWrapper minMaxWrapper = new SliderMinMaxWrapper();
	minMaxWrapper.min = minSlider;
	minMaxWrapper.max = maxSlider;

	return minMaxWrapper;
}

/**
 * Adds a slider with a text field to a table
 * @param text optional text before the slider (if not null)
 * @param name analytics event name if null will use 'text' as event name
 * @param min minimum value of the slider
 * @param max maximum value of the slider
 * @param stepSize step size of the slider
 * @param sliderListener listens to slider changes
 * @param table adds all UI elements to this table
 * @param hider optional hider to add the elements to (if not null)
 * @param createdActors optional adds all created elements to this list (if not null)
 * @return created slider element
 * @throw IllegalArgumentException if 'text' and 'name' are null or empty
 */
public Slider addSlider(String text, String name, float min, float max, float stepSize, SliderListener sliderListener, AlignTable table,
						GuiHider hider, ArrayList<Actor> createdActors) {
	if (mStyles == null) {
		throw new IllegalStateException("init() has not been called!");
	} else if ((text == null || text.isEmpty()) && (name == null || name.isEmpty())) {
		throw new IllegalArgumentException("Both 'text' and 'name' parameters are null or empty");
	}

	table.row().setFillWidth(true).setHeight(mStyles.vars.rowHeight);

	// Label
	Label label = null;
	if (text != null) {
		label = new Label(text, LabelStyles.DEFAULT.getStyle());
		table.add(label).setWidth(mStyles.vars.sliderLabelWidth);
	}

	// Event name
	String eventName = name;
	if (eventName == null || eventName.isEmpty()) {
		eventName = text;
	}

	// Slider
	Slider slider = new Slider(min, max, stepSize, false, mStyles.slider.standard);
	table.add(slider).setFillWidth(true);

	// Analytics
	slider.addListener(new AnalyticsSliderListener(eventName));

	// Text field
	TextField textField = new TextField("", mStyles.textField.standard);
	textField.setMaxLength(calculateTextFieldCharacters(min, max, stepSize));
	table.add(textField).setWidth(mStyles.vars.textFieldNumberWidth).setPadLeft(mStyles.vars.paddingInner);

	// Set slider listener
	sliderListener.add(slider, textField);

	if (label != null) {
		doExtraActionsOnActors(hider, createdActors, label, slider, textField);
	} else {
		doExtraActionsOnActors(hider, createdActors, slider, textField);
	}

	return slider;
}

/**
 * Calculate how many characters are needed to be displayed in the slider text field for all values
 * to be shown. I.e. order of magnitude (sort of)
 * @param min minimum slider value
 * @param max maximum slider value
 * @param stepSize
 * @return maximum character count for slider text field
 */
private static int calculateTextFieldCharacters(float min, float max, float stepSize) {
	MagnitudeWrapper minMag = Maths.calculateMagnitude(min);
	MagnitudeWrapper maxMag = Maths.calculateMagnitude(max);
	MagnitudeWrapper stepMag = Maths.calculateMagnitude(stepSize);
	int textWidth = 0;

	// Highest integer magnitude
	textWidth += Math.max(minMag.getInt(), maxMag.getInt());

	// Highest decimal value
	textWidth += Math.max(Math.max(minMag.getDec(), maxMag.getDec()), stepMag.getDec());


	// Extra characters
	// Minus
	if (min < 0) {
		textWidth++;
	}

	// Decimal point
	if (minMag.getDec() > 0 || maxMag.getDec() > 0 || stepMag.getDec() > 0) {
		textWidth++;
	}

	return textWidth;
}

/**
 * Add a color slider to the table
 * @param name analytics event name
 * @param table the table to add this to
 * @param hider optional hider for the color picker
 * @param createdActors optional, all created actors
 * @param colors all the colors to show in the color picker
 * @return created slider
 */
public ColorTintPicker addColorTintPicker(String name, AlignTable table, GuiHider hider, ArrayList<Actor> createdActors, Color... colors) {
	if (name == null || name.isEmpty()) {
		throw new IllegalArgumentException("'name' is null or empty");
	}

	table.row().setFillWidth(true);

	// Picking color
	ColorTintPicker picker = new ColorTintPicker(false, mStyles.slider.colorPicker, colors);
	table.add(picker).setFillWidth(true);

	picker.addListener(new AnalyticsColorTintPickerListener(name));

	doExtraActionsOnActors(hider, createdActors, picker);

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
	RatingWidget rating = createRatingWidget(touchable);

	table.add(rating);

	doExtraActionsOnActors(hider, null, rating);

	return rating;
}

/**
 * Creates a rating widget
 * @param touchable if the rating can be changed
 * @return created rating widget
 */
public RatingWidget createRatingWidget(Touchable touchable) {
	return new RatingWidget(mStyles.rating.stars, 5, touchable);
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
	Label label = new Label(text, LabelStyles.DEFAULT.getStyle());
	table.add(label).setFillWidth(fillWidth);

	doExtraActionsOnActors(hider, null, image, label);

	return label;
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
public Image addIconLabel(IImageNames icon, String text, Positions textPosition, LabelStyles textStyle, AlignTable table, GuiHider hider,
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
	tabWidget.setActionButtonHeight(mStyles.vars.textButtonHeight);
	tabWidget.setActionButtonPad(mStyles.vars.paddingButton);

	// Alignment
	tabWidget.setAlignTab(Horizontal.RIGHT).setAlign(Horizontal.RIGHT, Vertical.TOP);

	// Background
	tabWidget.setBackground(new Background(mStyles.color.widgetBackground));

	// Scrollpane style
	tabWidget.setScrollPaneStyle(mStyles.scrollPane.noBackground);

	return tabWidget;
}

/**
 * Create settings tab window
 * @param header header information
 * @param table the main table to add the settings to
 * @return default settings tab window
 */
public TabWidget addSettingsWindow(String header, AlignTable table) {
	table.setAlignTable(Horizontal.CENTER, Vertical.MIDDLE);

	// Header
	float headerPad = mStyles.vars.paddingParagraph;
	Label headerLabel = text.addHeader(header, table);
	table.getCell().setPadBottom(headerPad).setPadLeft(mStyles.vars.rowHeight);
	headerLabel.validate();
	float headerHeight = headerLabel.getHeight() + headerPad;

	// Tab widget
	TabWidget tabWidget = new TabWidget();
	table.row();
	table.add(tabWidget);

	tabWidget.setPad(mStyles.vars.paddingInner);
	tabWidget.setTabPosition(Positions.LEFT);
	tabWidget.setAlignTab(Vertical.TOP);
	tabWidget.setContentWidth(mStyles.vars.settingsWidth);
	tabWidget.setContentHeight(mStyles.vars.settingsHeight);

	// Background for settings widget
	tabWidget.setBackground(new Image(SkinNames.getDrawable(SkinNames.GeneralImages.WINDOW_SETTINGS)));

	// Bottom padding so the settings window is in the middle of the screen
	table.row().setHeight(headerHeight);

	return tabWidget;
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
	if (!mInitialized) {
		mStyles = new UiStyles();
		text.init(mStyles);
		button.init(mStyles);
		msgBox.init(mStyles);

		mInitialized = true;
	}
}

@Override
public void dispose() {
	mInitialized = false;
}

/**
 * @return UiStyles
 */
public UiStyles getStyles() {
	return mStyles;
}

/**
 * Different positions
 */
@SuppressWarnings("javadoc")
public enum Positions {
	LEFT,
	RIGHT,
	TOP,
	BOTTOM,;

	/**
	 * @return true if this position is either left or right
	 */
	public boolean isLeftOrRight() {
		return this == LEFT || this == RIGHT;
	}

	/**
	 * @return true if this position is either top or bottom
	 */
	public boolean isTopOrBottom() {
		return this == TOP || this == BOTTOM;
	}
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
	TOP_BOTTOM,;

	/**
	 * If the location contains the specified enumeration name. E.g. if singleLocation is TOP it
	 * will return true for the TOP and TOP_BOTTOM enumerations.
	 * @param singleLocation should be either TOP or BOTTOM.
	 * @return true if the location contains the specified location.
	 */
	private boolean contains(BarLocations singleLocation) {
		return name().contains(singleLocation.name());
	}

}

/**
 * Wrapper for a theme and label. Used in theme list selector
 */
public class ThemeSelectorData {
	/** Selected theme */
	public Themes theme;
	/** Label for the theme */
	public Label label;

	/**
	 * Private constructor, enforces that only UiFactory can create these objects.
	 * @param theme theme that is displayed
	 * @param label text that is displayed below the theme image
	 */
	private ThemeSelectorData(Themes theme, Label label) {
		this.theme = theme;
		this.label = label;
	}
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
}
