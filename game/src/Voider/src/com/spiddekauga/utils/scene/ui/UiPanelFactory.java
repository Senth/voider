package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.ISkinNames;
import com.spiddekauga.voider.utils.Pools;

/**
 * Factory for creating UI objects, more specifically combined UI objects.
 * This factory class gets its default settings from general.json
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UiPanelFactory {
	/**
	 * Creates an empty UI Factory. Call {@link #init()} to initialize
	 * all styles
	 */
	public UiPanelFactory() {
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
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
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
			ArrayList<Actor> createdActors,
			Invoker invoker
			) {
		@SuppressWarnings("unchecked")
		ArrayList<Slider> sliders = Pools.arrayList.obtain();

		// Label
		Label label = addLabelSection(text, table, null);
		doExtraActionsOnActors(tooltipText, hider, createdActors, label);

		// Sliders
		Slider minSlider = addSlider("Min", min, max, stepSize, minSliderListener, table, tooltipText, hider, createdActors, invoker);
		Slider maxSlider = addSlider("Max", min, max, stepSize, maxSliderListener, table, tooltipText, hider, createdActors, invoker);
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
	 * @param createdActors optional adds all created elements to this list (if not null)
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
			ArrayList<Actor> createdActors,
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

		if (label != null) {
			doExtraActionsOnActors(tooltipText, hider, createdActors, label, slider, textField);
		} else {
			doExtraActionsOnActors(tooltipText, hider, createdActors, slider, textField);
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
	 * Adds a single checkbox with text before the checkbox
	 * @param text the text to display before the checkbox
	 * @param listener button listener that listens when it's checked etc
	 * @param table the table to add the checkbox to
	 * @param tooltipText optional tooltip message for all elements (if not null)
	 * @param hider optional hider to add the elements to (if not null)
	 * @param createdActors optional adds all created elements to this list (if not null)
	 * @return created checkbox
	 */
	public CheckBox addCheckBox(
			String text,
			ButtonListener listener,
			AlignTable table,
			String tooltipText,
			GuiHider hider,
			ArrayList<Actor> createdActors
			) {

		table.row().setFillWidth(true);
		Label label = new Label(text, mStyles.label.standard);
		table.add(label).setFillWidth(true);

		CheckBox checkBox = new CheckBox("", mStyles.checkBox.checkBox);
		table.add(checkBox);

		listener.setButton(checkBox);

		doExtraActionsOnActors(tooltipText, hider, createdActors, label, checkBox);

		return checkBox;
	}

	/**
	 * Create tabs inside an panel.
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

		table.row();

		for (TabWrapper tab : tabs) {
			tab.button = new ImageButton((ImageButtonStyle) SkinNames.getResource(tab.imageName));
			table.add(tab.button);
			buttonGroup.add(tab.button);
			tab.hider.setButton(tab.button);
			parentHider.addChild(tab.hider);

			if (checkCommandCreator != null) {
				tab.button.addListener(checkCommandCreator);
			}

			if (createdActors != null) {
				createdActors.add(tab.button);
			}

			if (tab.tooltipText != null) {
				new TooltipListener(tab.button, tab.tooltipText);
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
		mStyles.vars.paddingInner = SkinNames.getResource(SkinNames.GeneralVars.PADDING_INNER);
		mStyles.vars.textFieldNumberWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_NUMBER_WIDTH);
		mStyles.vars.sliderWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_WIDTH);
		mStyles.vars.sliderLabelWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_LABEL_WIDTH);

	}

	/**
	 * Tab information wrapper
	 */
	public static class TabWrapper {
		/**
		 * Creates empty (and invalid) tab information wrapper
		 */
		public TabWrapper() {
			// Does nothing
		}

		/**
		 * Initializes a correct TabWrapper
		 * @param imageName name of the button image
		 * @param hider hideListener for this button
		 * @param tooltipText optional tooltip message (if not null)
		 */
		public TabWrapper(ISkinNames imageName, HideListener hider, String tooltipText) {
			this.imageName = imageName;
			this.hider = hider;
			this.tooltipText = tooltipText;
		}

		/** Image name */
		public ISkinNames imageName = null;
		/** Optional tooltip text */
		public String tooltipText = null;
		/** Hider for the tab */
		public HideListener hider = null;
		/** Button that was created for this tab */
		public ImageButton button = null;
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
			float paddingInner = 0;
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
