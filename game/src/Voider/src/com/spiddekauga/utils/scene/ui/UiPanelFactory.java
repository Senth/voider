package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.ISkinNames;

/**
 * Factory for creating UI objects, more specifically combined UI objects. This factory
 * class gets its default settings from general.json
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UiPanelFactory {
	/**
	 * Creates an empty UI Factory. Call {@link #init()} to initialize all styles
	 */
	public UiPanelFactory() {
		// Does nothing
	}

	/**
	 * Adds a text field with an optional label header
	 * @param labelText optional text for the label, if null no label is added
	 * @param defaultText default text in the text field
	 * @param listener text field listener
	 * @param table the table to add the text field to
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return Created text field
	 */
	public TextField addTextField(String labelText, String defaultText, TextFieldListener listener, AlignTable table, ArrayList<Actor> createdActors) {
		// Label
		if (labelText != null) {
			Label label = addSection(labelText, table, null);
			doExtraActionsOnActors(null, null, createdActors, label);
			table.row();
		}

		TextField textField = new TextField(defaultText, mStyles.textField.standard);
		listener.setTextField(textField);
		listener.setDefaultText(defaultText);

		// Set width and height
		table.add(textField).setSize(mStyles.vars.textFieldWidth, mStyles.vars.rowHeight);

		doExtraActionsOnActors(null, null, createdActors, textField);

		return textField;
	}

	/**
	 * Adds a text area with an optional label header
	 * @param labelText optional text for the label, if null no label is added
	 * @param defaultText default text in the text field
	 * @param listener text field listener
	 * @param table the table to add the text field to
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return Created text field
	 */
	public TextArea addTextArea(String labelText, String defaultText, TextFieldListener listener, AlignTable table, ArrayList<Actor> createdActors) {
		// Label
		if (labelText != null) {
			Label label = addSection(labelText, table, null);
			doExtraActionsOnActors(null, null, createdActors, label);
			table.row();
		}

		TextArea textArea = new TextArea(defaultText, mStyles.textField.standard);
		listener.setTextField(textArea);
		listener.setDefaultText(defaultText);

		// Set width and height
		table.add(textArea).setSize(mStyles.vars.textFieldWidth, mStyles.vars.textAreaHeight);

		doExtraActionsOnActors(null, null, createdActors, textArea);

		return textArea;
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
	 * @param tooltipText optional tooltip message for all elements (if not null)
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @param invoker optional adds the ability to undo changes (if not null)
	 * @return Created min and max sliders;
	 */
	public SliderMinMaxWrapper addSliderMinMax(String text, float min, float max, float stepSize, SliderListener minSliderListener,
			SliderListener maxSliderListener, AlignTable table, String tooltipText, GuiHider hider, ArrayList<Actor> createdActors, Invoker invoker) {
		// Label
		if (text != null) {
			Label label = addPanelSection(text, table, null);
			doExtraActionsOnActors(tooltipText, hider, createdActors, label);
		}

		// Sliders
		Slider minSlider = addSlider("Min", min, max, stepSize, minSliderListener, table, tooltipText, hider, createdActors, invoker);
		Slider maxSlider = addSlider("Max", min, max, stepSize, maxSliderListener, table, tooltipText, hider, createdActors, invoker);

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
	 * @param tooltipText optional tooltip message for all elements (if not null)
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @param invoker optional adds the ability to undo changes (if not null)
	 * @return created slider element
	 */
	public Slider addSlider(String text, float min, float max, float stepSize, SliderListener sliderListener, AlignTable table, String tooltipText,
			GuiHider hider, ArrayList<Actor> createdActors, Invoker invoker) {
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
			doExtraActionsOnActors(tooltipText, hider, createdActors, label, slider, textField);
		} else {
			doExtraActionsOnActors(tooltipText, hider, createdActors, slider, textField);
		}

		return slider;
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
		table.row();
		table.add(label);

		if (hider != null) {
			hider.addToggleActor(label);
		}

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
		Label label = new Label(text, mStyles.label.standard);
		table.row();
		table.add(label).setHeight(mStyles.vars.rowHeight);

		if (hider != null) {
			hider.addToggleActor(label);
		}

		return label;
	}


	/**
	 * Adds a tool icon to the specified table
	 * @param icon icon for the tool
	 * @param listener button listener that listens when the button is checked etc
	 * @param group the button group the tools belong to
	 * @param table the table to add the tool to
	 * @param tooltipText optional tooltip message for all elements (if not null)
	 * @param createdActors optional adds the tool button to this list (if not null)
	 * @return created tool icon button
	 */
	public ImageButton addToolButton(ISkinNames icon, ButtonListener listener, ButtonGroup group, AlignTable table, String tooltipText,
			ArrayList<Actor> createdActors) {
		ImageButton button = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));

		listener.setButton(button);
		group.add(button);

		doExtraActionsOnActors(tooltipText, null, createdActors, button);

		return button;
	}

	/**
	 * Adds a tool separator to the specified table
	 * @param table add tool separator
	 */
	public void addToolSeparator(AlignTable table) {
		table.row();
		table.add().setPadBottom(mStyles.vars.paddingOuter);
		table.row();
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
	public CheckBox addCheckBox(String text, ButtonListener listener, AlignTable table, String tooltipText, GuiHider hider,
			ArrayList<Actor> createdActors) {

		table.row().setFillWidth(true);
		Label label = new Label(text, mStyles.label.standard);
		table.add(label);

		table.add().setFillWidth(true);

		CheckBox checkBox = new CheckBox("", mStyles.checkBox.checkBox);
		table.add(checkBox);

		listener.setButton(checkBox);

		doExtraActionsOnActors(tooltipText, hider, createdActors, label, checkBox);

		return checkBox;
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

			if (tab.tooltipText != null) {
				new TooltipListener(tab.button, tab.tooltipText);
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
	 * Initializes the UiFactory
	 */
	public void init() {
		mStyles = new UiStyles();

		mStyles.textButton.press = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_PRESS);
		mStyles.textButton.toggle = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TOGGLE);
		mStyles.textButton.selected = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_SELECTED);
		mStyles.slider.standard = SkinNames.getResource(SkinNames.General.SLIDER_DEFAULT);
		mStyles.textField.standard = SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT);
		mStyles.label.standard = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);
		mStyles.label.panelSection = SkinNames.getResource(SkinNames.General.LABEL_PANEL_SECTION);
		mStyles.checkBox.checkBox = SkinNames.getResource(SkinNames.General.CHECK_BOX_DEFAULT);
		mStyles.checkBox.radio = SkinNames.getResource(SkinNames.General.CHECK_BOX_RADIO);

		// Colors
		mStyles.colors.widgetBackground = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR);

		// Vars
		mStyles.vars.paddingCheckBox = SkinNames.getResource(SkinNames.GeneralVars.PADDING_CHECKBOX);
		mStyles.vars.paddingOuter = SkinNames.getResource(SkinNames.GeneralVars.PADDING_OUTER);
		mStyles.vars.textFieldNumberWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_NUMBER_WIDTH);
		mStyles.vars.textFieldWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_WIDTH);
		mStyles.vars.sliderWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_WIDTH);
		mStyles.vars.sliderLabelWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_LABEL_WIDTH);
		mStyles.vars.rowHeight = SkinNames.getResource(SkinNames.GeneralVars.ICON_ROW_HEIGHT);
		mStyles.vars.textAreaHeight = SkinNames.getResource(SkinNames.GeneralVars.TEXT_AREA_HEIGHT);
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
		/** Optional tooltip text */
		public String tooltipText = null;
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
			button = new CheckBox(text, mStyles.checkBox.radio);
		}

		/** Button text */
		public String text = null;
	}

	/**
	 * Container for all ui styles
	 */
	@SuppressWarnings("javadoc")
	private static class UiStyles {
		TextButtons textButton = new TextButtons();
		Sliders slider = new Sliders();
		TextFields textField = new TextFields();
		Labels label = new Labels();
		CheckBoxes checkBox = new CheckBoxes();
		Variables vars = new Variables();
		Colors colors = new Colors();

		static class Variables {
			float textFieldNumberWidth = 0;
			float sliderWidth = 0;
			float sliderLabelWidth = 0;
			float paddingCheckBox = 0;
			float paddingOuter = 0;
			float rowHeight = 0;
			float textAreaHeight = 0;
			float textFieldWidth = 0;
		}

		static class Colors {
			Color widgetBackground = null;
		}

		static class TextButtons {
			TextButtonStyle press = null;
			TextButtonStyle toggle = null;
			TextButtonStyle selected = null;
		}

		static class Sliders {
			SliderStyle standard = null;
		}

		static class TextFields {
			TextFieldStyle standard = null;
		}

		static class Labels {
			LabelStyle standard = null;
			LabelStyle panelSection = null;
		}

		static class CheckBoxes {
			CheckBoxStyle radio = null;
			CheckBoxStyle checkBox = null;
		}
	}

	/** All skins and styles */
	private UiStyles mStyles = null;
}
