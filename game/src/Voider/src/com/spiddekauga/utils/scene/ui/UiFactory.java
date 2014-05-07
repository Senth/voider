package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Pools;

/**
 * Factory for creating UI objects, more specifically combined UI objects.
 * This factory class gets its default settings from general.json
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UiFactory {
	/**
	 * Creates an empty UI Factory. Call {@link #init()} to initialize
	 * all styles
	 */
	public UiFactory() {
		// Does nothing
	}

	/**
	 * Adds a min and max slider with section text to a table.
	 * These sliders are synchronized.
	 * @param text section text for the sliders
	 * @param min minimum value of the sliders
	 * @param max maximum value of the sliders
	 * @param stepSize step size of the sliders
	 * @param minSliderListener slider listener for the min slider
	 * @param maxSliderListener slider listener for the max slider
	 * @param table adds all UI elements to this table
	 * @param tooltipText optional tooltip message for all elements (if not null)
	 * @param hider optional hider to add the elments to (if not null)
	 * @param actorList optional adds all elements to this list (if not null)
	 * @param invoker optional adds the ability to undo changes (if not null)
	 * @return Min and max sliders in an array in that order.
	 */
	public ArrayList<Slider> addSliderMinMax(
			String text,
			float min,
			float max,
			float stepSize,
			SliderListener minSliderListener,
			SliderListener maxSliderListener,
			AlignTable table,
			String tooltipText,
			GuiHider hider,
			ArrayList<Actor> actorList,
			Invoker invoker
			) {
		@SuppressWarnings("unchecked")
		ArrayList<Slider> sliders = Pools.arrayList.obtain();

		// Label
		Label label = addLabelSection(text, table, hider);

		if (actorList != null) {
			actorList.add(label);
		}

		if (tooltipText != null) {
			new TooltipListener(label, tooltipText);
		}

		Slider minSlider = addSlider("Min", min, max, stepSize, minSliderListener, table, tooltipText, hider, actorList, invoker);
		Slider maxSlider = addSlider("Max", min, max, stepSize, maxSliderListener, table, tooltipText, hider, actorList, invoker);
		sliders.add(minSlider);
		sliders.add(maxSlider);

		minSliderListener.setGreaterSlider(maxSlider);
		maxSliderListener.setLesserSlider(minSlider);

		return sliders;
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
	 * @param actorList optional adds all elements to this list (if not null)
	 * @param invoker optional adds the ability to undo changes (if not null)
	 * @return created slider element
	 */
	public Slider addSlider(
			String text,
			float min,
			float max,
			float stepSize,
			SliderListener sliderListener,
			AlignTable table,
			String tooltipText,
			GuiHider hider,
			ArrayList<Actor> actorList,
			Invoker invoker
			) {
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
		table.add(textField).setWidth(mStyles.vars.textFieldNumberWidth);

		// Set slider listener
		sliderListener.init(slider, textField, invoker);

		// Add tooltip
		if (tooltipText != null) {
			if (label != null) {
				new TooltipListener(label, tooltipText);
			}
			new TooltipListener(slider, tooltipText);
			new TooltipListener(textField, tooltipText);
		}

		// Add to hider
		if (hider != null) {
			if (label != null) {
				hider.addToggleActor(label);
			}
			hider.addToggleActor(slider);
			hider.addToggleActor(textField);
		}

		// Add to actor list
		if (actorList != null) {
			if (label != null) {
				actorList.add(label);
			}
			actorList.add(slider);
			actorList.add(textField);
		}

		return slider;
	}

	/**
	 * Add a section label
	 * @param text section label text
	 * @param table the table to add the text to
	 * @param hider optional hider to hide the label
	 * @return label that was created
	 */
	public Label addLabelSection(String text, AlignTable table, GuiHider hider) {
		Label label = new Label(text, mStyles.label.panelSection);
		table.row();
		table.add(label);

		if (hider != null) {
			hider.addToggleActor(label);
		}

		return label;
	}

	/**
	 * Initializes the UiFactory
	 */
	public void init() {
		mStyles = new UiStyles();

		mStyles.skin.general = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		mStyles.skin.editor = ResourceCacheFacade.get(InternalNames.UI_EDITOR);
		mStyles.textButton.press = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_PRESS);
		mStyles.textButton.toggle = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TOGGLE);
		mStyles.textButton.selected = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_SELECTED);
		mStyles.slider.standard = SkinNames.getResource(SkinNames.General.SLIDER_DEFAULT);
		mStyles.textField.standard = SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT);
		mStyles.label.standard = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);
		mStyles.label.error = SkinNames.getResource(SkinNames.General.LABEL_ERROR);
		mStyles.label.highlight = SkinNames.getResource(SkinNames.General.LABEL_HIGHLIGHT);
		mStyles.label.success = SkinNames.getResource(SkinNames.General.LABEL_SUCCESS);
		mStyles.label.panelSection = SkinNames.getResource(SkinNames.General.LABEL_PANEL_SECTION);
		mStyles.checkBox.checkBox = SkinNames.getResource(SkinNames.General.CHECK_BOX_DEFAULT);
		mStyles.checkBox.radio = SkinNames.getResource(SkinNames.General.CHECK_BOX_RADIO);
		mStyles.scrollPane.noBackground = SkinNames.getResource(SkinNames.General.SCROLL_PANE_DEFAULT);
		mStyles.scrollPane.windowBackground = SkinNames.getResource(SkinNames.General.SCROLL_PANE_WINDOW_BACKGROUND);

		// Colors
		mStyles.colors.widgetBackground = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR);

		// Vars
		mStyles.vars.paddingDefault = SkinNames.getResource(SkinNames.GeneralVars.PADDING_DEFAULT);
		mStyles.vars.paddingSeparator = SkinNames.getResource(SkinNames.GeneralVars.PADDING_SEPARATOR);
		mStyles.vars.paddingAfterLabel = SkinNames.getResource(SkinNames.GeneralVars.PADDING_AFTER_LABEL);
		mStyles.vars.paddingOuter = SkinNames.getResource(SkinNames.GeneralVars.PADDING_OUTER);
		mStyles.vars.paddingInner = SkinNames.getResource(SkinNames.GeneralVars.PADDING_INNER);
		mStyles.vars.textFieldNumberWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_NUMBER_WIDTH);
		mStyles.vars.barUpperLowerHeight = SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT);
		mStyles.vars.sliderWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_WIDTH);
		mStyles.vars.sliderLabelWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_LABEL_WIDTH);
	}

	/**
	 * Container for all ui styles
	 */
	@SuppressWarnings("javadoc")
	private static class UiStyles {
		TextButtons textButton = new TextButtons();
		Sliders slider = new Sliders();
		TextFields textField = new TextFields();
		Skins skin = new Skins();
		Labels label = new Labels();
		CheckBoxes checkBox = new CheckBoxes();
		ScrollPanes scrollPane = new ScrollPanes();
		Variables vars = new Variables();
		Colors colors = new Colors();

		static class Variables {
			float paddingDefault = 0;
			float paddingSeparator = 0;
			float paddingAfterLabel = 0;
			float paddingOuter = 0;
			float paddingInner = 0;
			float barUpperLowerHeight = 0;
			float textFieldNumberWidth = 0;
			float sliderWidth = 0;
			float sliderLabelWidth = 0;
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

		static class Skins {
			Skin general = null;
			Skin editor = null;
		}

		static class Labels {
			LabelStyle standard = null;
			LabelStyle error = null;
			LabelStyle highlight = null;
			LabelStyle success = null;
			LabelStyle panelSection = null;
		}

		static class CheckBoxes {
			CheckBoxStyle radio = null;
			CheckBoxStyle checkBox = null;
		}

		static class ScrollPanes {
			ScrollPaneStyle noBackground;
			ScrollPaneStyle windowBackground;
		}
	}

	/** All skins and styles */
	private UiStyles mStyles = null;
}
