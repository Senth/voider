package com.spiddekauga.voider.scene.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.spiddekauga.utils.scene.ui.RatingWidget.RatingWidgetStyle;
import com.spiddekauga.voider.repo.resource.SkinNames;

/**
 * Container for all UI styles
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public class UiStyles {
	public Sliders slider = new Sliders();
	public TextFields textField = new TextFields();
	public Labels label = new Labels();
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
		public Color notificationBackground = null;
	}

	public static class Variables {
		public float barUpperLowerHeight = 0;
		public float textFieldNumberWidth = 0;
		public float sliderLabelWidth = 0;
		public float paddingButton = 0;
		public float paddingCheckBox = 0;
		public float paddingCheckBoxText = 0;
		public float paddingOuter = 0;
		public float paddingInner = 0;
		public float paddingExplore = 0;
		public float paddingTransparentTextButton = 0;
		public float paddingSeparator = 0;
		public float rowHeight = 0;
		public float rowHeightSection = 0;
		public float textAreaHeight = 0;
		public float textFieldWidth = 0;
		public float textButtonHeight = 0;
		public float textButtonWidth = 0;
		public float paddingParagraph = 0;
		public float rightPanelWidth = 0;
		float settingsWidth = 0;
		float settingsHeight = 0;
	}

	public static class Sliders {
		public SliderStyle standard = null;
		public SliderStyle colorPicker = null;
	}

	public static class TextFields {
		public TextFieldStyle standard = null;
	}

	static class Labels {
		LabelStyle errorSectionInfo = null;
		LabelStyle errorSection = null;
	}

	public static class Windows {
		public WindowStyle title = null;
		public WindowStyle noTitle = null;
	}

	/**
	 * Various label styles
	 */
	public enum LabelStyles {
		DEFAULT,
		PANEL_SECTION,
		ERROR,
		HIGHLIGHT,
		WARNING,
		SUCCESS,
		TOOLTIP,
		/** Extra information */
		INFO_EXTRA,
		/** Names of the resources that will be publish */
		PUBLISH_NAME,
		/** Larger text */
		HEADER,
		/** Default text style for text fields */
		TEXT_FIELD_DEFAULT,
		/** Text style for paths */
		PATH,


		;

		/**
		 * Set the Scene2D text button style
		 * @param style the text button style
		 */
		private void setStyle(LabelStyle style) {
			mStyle = style;
		}

		/**
		 * @return get the text button style associated with this enumeration
		 */
		public LabelStyle getStyle() {
			return mStyle;
		}

		private LabelStyle mStyle = null;
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
		CheckBoxStyle getStyle() {
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
		/** Selected and has hover and down effects */
		SELECTED_PRESSABLE,
		/** Always displayed as checked */
		SELECTED,

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
		public ButtonStyle getStyle() {
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
		TRANSPARENT_PRESS,
		/** Transparent (only text is visible), can be toggled/checked */
		TRANSPARENT_TOGGLE,
		/** Tag button */
		TAG,
		/** Text link */
		LINK,

		;

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
		public TextButtonStyle getStyle() {
			return mStyle;
		}

		/** The style variable */
		private TextButtonStyle mStyle = null;
	}

	/**
	 * Initializes all style variables
	 */
	UiStyles() {
		slider.standard = SkinNames.getResource(SkinNames.General.SLIDER_DEFAULT);
		slider.colorPicker = SkinNames.getResource(SkinNames.General.SLIDER_COLOR_PICKER);
		textField.standard = SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT);
		select.standard = SkinNames.getResource(SkinNames.General.SELECT_BOX_DEFAULT);
		rating.stars = SkinNames.getResource(SkinNames.General.RATING_DEFAULT);
		window.title = SkinNames.getResource(SkinNames.General.WINDOW_TITLE);
		window.noTitle = SkinNames.getResource(SkinNames.General.WINDOW_NO_TITLE);
		scrollPane.windowBackground = SkinNames.getResource(SkinNames.General.SCROLL_PANE_WINDOW_BACKGROUND);
		scrollPane.noBackground = SkinNames.getResource(SkinNames.General.SCROLL_PANE_DEFAULT);


		// Colors
		color.sceneBackground = SkinNames.getResource(SkinNames.GeneralVars.SCENE_BACKGROUND_COLOR);
		color.widgetBackground = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR);
		color.widgetInnerBackground = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_INNER_BACKGROUND_COLOR);
		color.notificationBackground = SkinNames.getResource(SkinNames.GeneralVars.NOTIFICATION_BACKGROUND_COLOR);

		// Vars
		vars.barUpperLowerHeight = SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT);
		vars.paddingButton = SkinNames.getResource(SkinNames.GeneralVars.PADDING_BUTTONS);
		vars.paddingCheckBox = SkinNames.getResource(SkinNames.GeneralVars.PADDING_CHECKBOX);
		vars.paddingCheckBoxText = SkinNames.getResource(SkinNames.GeneralVars.PADDING_CHECKBOX_TEXT);
		vars.paddingOuter = SkinNames.getResource(SkinNames.GeneralVars.PADDING_OUTER);
		vars.paddingInner = SkinNames.getResource(SkinNames.GeneralVars.PADDING_INNER);
		vars.paddingExplore = SkinNames.getResource(SkinNames.GeneralVars.PADDING_EXPLORE);
		vars.paddingSeparator = SkinNames.getResource(SkinNames.GeneralVars.PADDING_SEPARATOR);
		vars.paddingParagraph = SkinNames.getResource(SkinNames.GeneralVars.PADDING_PARAGRAPH);
		vars.paddingTransparentTextButton = SkinNames.getResource(SkinNames.GeneralVars.PADDING_TRANSPARENT_TEXT_BUTTON);
		vars.textFieldNumberWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_NUMBER_WIDTH);
		vars.textFieldWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_WIDTH);
		vars.sliderLabelWidth = SkinNames.getResource(SkinNames.GeneralVars.SLIDER_LABEL_WIDTH);
		vars.rowHeight = SkinNames.getResource(SkinNames.GeneralVars.ROW_HEIGHT);
		vars.rowHeightSection = SkinNames.getResource(SkinNames.GeneralVars.ROW_HEIGHT_SECTION);
		vars.textAreaHeight = SkinNames.getResource(SkinNames.GeneralVars.TEXT_AREA_HEIGHT);
		vars.textButtonHeight = SkinNames.getResource(SkinNames.GeneralVars.TEXT_BUTTON_HEIGHT);
		vars.textButtonWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_BUTTON_WIDTH);
		vars.rightPanelWidth = SkinNames.getResource(SkinNames.GeneralVars.RIGHT_PANEL_WIDTH);
		vars.settingsWidth = SkinNames.getResource(SkinNames.GeneralVars.SETTINGS_WIDTH);
		vars.settingsHeight = SkinNames.getResource(SkinNames.GeneralVars.SETTINGS_HEIGHT);

		// Label styles
		label.errorSectionInfo = SkinNames.getResource(SkinNames.General.LABEL_ERROR_SECTION_INFO);
		label.errorSection = SkinNames.getResource(SkinNames.General.LABEL_ERROR_SECTION);
		LabelStyles.DEFAULT.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_DEFAULT));
		LabelStyles.PANEL_SECTION.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_PANEL_SECTION));
		LabelStyles.ERROR.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_ERROR));
		LabelStyles.WARNING.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_WARNING));
		LabelStyles.HIGHLIGHT.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_HIGHLIGHT));
		LabelStyles.TOOLTIP.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_TOOLTIP));
		LabelStyles.HEADER.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_HEADER));
		LabelStyles.TEXT_FIELD_DEFAULT.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_TEXT_FIELD_DEFAULT));
		LabelStyles.SUCCESS.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_SUCCESS));
		LabelStyles.PUBLISH_NAME.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_PUBLISH_NAME));
		LabelStyles.PATH.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_PATH));
		LabelStyles.INFO_EXTRA.setStyle((LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_INFO_EXTRA));


		// Button styles
		ButtonStyles.PRESS.setStyle((ButtonStyle) SkinNames.getResource(SkinNames.General.BUTTON_PRESS));
		ButtonStyles.TOGGLE.setStyle((ButtonStyle) SkinNames.getResource(SkinNames.General.BUTTON_TOGGLE));
		ButtonStyles.SELECTED.setStyle((ButtonStyle) SkinNames.getResource(SkinNames.General.BUTTON_SELECTED));
		ButtonStyles.SELECTED_PRESSABLE.setStyle((ButtonStyle) SkinNames.getResource(SkinNames.General.BUTTON_SELECTED_PRESSABLE));

		// Text buttons
		TextButtonStyles.FILLED_PRESS.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_FLAT_PRESS));
		TextButtonStyles.FILLED_TOGGLE.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_FLAT_TOGGLE));
		TextButtonStyles.TRANSPARENT_PRESS.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TRANSPARENT_PRESS));
		TextButtonStyles.TRANSPARENT_TOGGLE.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TRANSPARENT_TOGGLE));
		TextButtonStyles.TAG.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TAG));
		TextButtonStyles.LINK.setStyle((TextButtonStyle) SkinNames.getResource(SkinNames.General.TEXT_BUTTON_LINK));

		// Checkbox styles
		CheckBoxStyles.CHECK_BOX.setStyle((CheckBoxStyle) SkinNames.getResource(SkinNames.General.CHECK_BOX_DEFAULT));
		CheckBoxStyles.RADIO.setStyle((CheckBoxStyle) SkinNames.getResource(SkinNames.General.CHECK_BOX_RADIO));
	}
}
