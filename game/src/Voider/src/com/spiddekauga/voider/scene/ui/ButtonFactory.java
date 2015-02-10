package com.spiddekauga.voider.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.spiddekauga.utils.commands.GuiCheckCommandCreator;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.ImageScrollButton;
import com.spiddekauga.utils.scene.ui.ImageScrollButton.ScrollWhen;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.voider.repo.analytics.listener.AnalyticsButtonListener;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.ISkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.ButtonStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.sound.SoundPlayer;
import com.spiddekauga.voider.sound.Sounds;

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
	 * Add a tab to a created tab widget
	 * @param icon the image of the tab
	 * @param table will show this table when this tab is selected
	 * @param hider listens to the hider.
	 * @param tabWidget the tab widget to add the tab to
	 * @return created tab button
	 */
	public ImageButton addTab(ISkinNames icon, AlignTable table, HideListener hider, TabWidget tabWidget) {
		ImageButton button = createImage(icon);
		tabWidget.addTab(button, table, hider);
		return button;
	}

	/**
	 * Add a scrollable tab to a created tab widget
	 * @param icon the image of the tab
	 * @param table will show this table when this tab is selected
	 * @param hider listens to the hider.
	 * @param tabWidget the tab widget to add the tab to
	 * @return created tab button
	 */
	public ImageButton addTabScroll(ISkinNames icon, AlignTable table, HideListener hider, TabWidget tabWidget) {
		ImageButton button = createImage(icon);
		tabWidget.addTabScroll(button, table, hider);
		return button;

	}

	/**
	 * Add a tab to a created tab widget
	 * @param icon the image of the tab
	 * @param table will show this table when this tab is selected
	 * @param tabWidget the tab widget to add the tab to
	 * @return created tab button
	 */
	public ImageButton addTab(ISkinNames icon, AlignTable table, TabWidget tabWidget) {
		return addTab(icon, table, new HideListener(true), tabWidget);
	}

	/**
	 * Add a scrollable tab to a created tab widget
	 * @param icon the image of the tab
	 * @param table will show this table when this tab is selected
	 * @param tabWidget the tab widget to add the tab to
	 * @return created tab button
	 */
	public ImageButton addTabScroll(ISkinNames icon, AlignTable table, TabWidget tabWidget) {
		return addTabScroll(icon, table, new HideListener(true), tabWidget);
	}

	/**
	 * Create an image button
	 * @param icon name of the image icon
	 * @return created image button
	 */
	public ImageButton createImage(ISkinNames icon) {
		ImageButton imageButton = new ImageButton((ImageButtonStyle) SkinNames.getResource(icon));
		new AnalyticsButtonListener(imageButton, icon.toString());
		imageButton.addListener(mButtonSoundListener);
		return imageButton;
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
		ImageButton imageButton = createImage(icon);
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
		new AnalyticsButtonListener(imageScrollButton, style.toString());
		imageScrollButton.addListener(mButtonSoundListener);

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
		new AnalyticsButtonListener(imageButton, icon.toString());
		imageButton.addListener(mButtonSoundListener);

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
		TextButton textButton = new TextButton(text, style.getStyle());
		new AnalyticsButtonListener(textButton, text);
		textButton.addListener(mButtonSoundListener);
		return textButton;
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

			// Text is too long, increase button width
			if (button.getPrefWidth() > mStyles.vars.textButtonWidth) {
				cell.resetWidth();
			}
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
		new AnalyticsButtonListener(button, icon.toString());
		button.addListener(mButtonSoundListener);

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
		table.row().setHeight(mStyles.vars.paddingOuter);
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
		new AnalyticsButtonListener(checkBox, text);

		UiFactory.doExtraActionsOnActors(hider, createdActors, label, checkBox);

		return checkBox;
	}

	/**
	 * Create a checkbox button
	 * @param text the text to display on the checkbox
	 * @param style which checkbox style to use
	 * @return created checkbox
	 */
	public CheckBox createCheckBox(String text, CheckBoxStyles style) {
		CheckBox checkBox = new CheckBox(text, style.getStyle());
		new AnalyticsButtonListener(checkBox, text);
		float imageWidth = checkBox.getImage().getWidth();
		checkBox.getImageCell().width(imageWidth);
		checkBox.getLabelCell().padLeft(mStyles.vars.paddingCheckBoxText);
		checkBox.layout();
		checkBox.left();

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
		CheckBox checkBox = createCheckBox(text, style);

		table.add(checkBox);
		if (group != null) {
			group.add(checkBox);
		}

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
		table.getRow().setHeight(mStyles.vars.rowHeight);

		return checkBox;
	}

	/**
	 * Adds checkboxes for all enumerations
	 * @param enumerations display name of checkbox and place in array
	 * @param style TODO
	 * @param hider optional hider
	 * @param group optional button group
	 * @param vertical true for the checkboxes to be in a vertical layout, false for
	 *        horizontal
	 * @param table where to add the buttons
	 * @param buttons empty checkbox array, created button is added to this array
	 */
	public void addEnumCheckboxes(Enum<?>[] enumerations, CheckBoxStyles style, GuiHider hider, ButtonGroup group, boolean vertical,
			AlignTable table, Button[] buttons) {
		if (buttons.length != enumerations.length) {
			throw new RuntimeException("Enumeration doesn't have same length as buttons. Enums:\n " + enumerations.toString());
		}

		for (int i = 0; i < enumerations.length; i++) {
			Enum<?> enumeration = enumerations[i];
			Button button = null;
			if (vertical) {
				button = addCheckBoxRow(enumeration.toString(), style, null, group, table);
			} else {
				button = addCheckBox(enumeration.toString(), style, null, group, table);
			}
			if (hider != null) {
				hider.addToggleActor(button);
			}
			buttons[i] = button;
			new AnalyticsButtonListener(button, enumeration.toString());
		}
	}

	/**
	 * Add and set an enumeration image button to the specified table
	 * @param enumeration for determining place in array
	 * @param image button image
	 * @param hider optional hider
	 * @param table where to add the button
	 * @param buttons the button array, created button is added to this array
	 */
	public void addEnumButton(Enum<?> enumeration, ISkinNames image, GuiHider hider, AlignTable table, Button[] buttons) {
		buttons[enumeration.ordinal()] = mUiFactory.button.addImage(image, table, hider, null);
		new AnalyticsButtonListener(buttons[enumeration.ordinal()], enumeration.toString());
	}

	/**
	 * Create generic tabs for a table.
	 * @param table adds the tabs to this table
	 * @param parentHider parent hider for all tab hiders
	 * @param vertical true if the tabs should be vertically aligned
	 * @param createdActors optional adds all tabs to this list (if not null)
	 * @param invoker optional ability to undo which tab is selected (if not null)
	 * @param tabs tab information for all tabs to create, will set the button for these
	 */
	public void addTabs(AlignTable table, GuiHider parentHider, boolean vertical, ArrayList<Actor> createdActors, Invoker invoker, TabWrapper... tabs) {
		GuiCheckCommandCreator checkCommandCreator = null;
		if (invoker != null) {
			checkCommandCreator = new GuiCheckCommandCreator(invoker);
		}
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.setMinCheckCount(1);
		buttonGroup.setMaxCheckCount(1);

		if (!vertical) {
			table.row();
		}

		for (int i = 0; i < tabs.length; i++) {
			TabWrapper tab = tabs[i];
			tab.createButton();
			if (vertical) {
				table.row();
			}
			table.add(tab.mButton);
			buttonGroup.add(tab.mButton);
			if (parentHider != null) {
				parentHider.addToggleActor(tab.mButton);
				parentHider.addChild(tab.mHider);
			}
			tab.mHider.addButton(tab.mButton);

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
				// Vertical
				if (vertical) {
					table.getRow().setHeight(mStyles.vars.rowHeight);
				}
				// Horizontal - Add padding between checkboxes
				else {
					if (i != tabs.length - 1) {
						addCheckBoxPadding(table);
					}
				}
			}
		}
	}

	/**
	 * Create generic tabs for a table.
	 * @param table adds the tabs to this table
	 * @param parentHider parent hider for all tab hiders
	 * @param vertical true if the tabs should be vertically aligned
	 * @param createdActors optional adds all tabs to this list (if not null)
	 * @param invoker optional ability to undo which tab is selected (if not null)
	 * @param tabs tab information for all tabs to create, will set the button for these
	 */
	public void addTabs(AlignTable table, GuiHider parentHider, boolean vertical, ArrayList<Actor> createdActors, Invoker invoker,
			ArrayList<? extends TabWrapper> tabs) {
		TabWrapper[] array = new TabWrapper[tabs.size()];
		tabs.toArray(array);
		addTabs(table, parentHider, vertical, createdActors, invoker, array);
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
	 * Creates a radio tab button
	 * @param text text to display on the radio button
	 * @return a new radio tab wrapper instance
	 */
	public TabRadioWrapper createTabRadioWrapper(String text) {
		return new TabRadioWrapper(text);
	}

	/**
	 * @return sound button listener
	 */
	public EventListener getSoundListener() {
		return mButtonSoundListener;
	}

	/**
	 * Plays different sound depending on what button event is used
	 */
	private EventListener mButtonSoundListener = new EventListener() {
		@Override
		public boolean handle(Event event) {
			if (event instanceof InputEvent) {
				InputEvent inputEvent = (InputEvent) event;
				switch (inputEvent.getType()) {
				case enter:
					if (shouldPlaySound(event)) {
						mSoundPlayer.play(Sounds.UI_BUTTON_HOVER);
					}
					break;
				case touchDown:
					if (shouldPlaySound(event)) {
						mSoundPlayer.play(Sounds.UI_BUTTON_CLICK);
					}
					break;
				default:
					// Does nothing
					break;
				}
			}

			return false;
		}

		/**
		 * Tests whether we should play a sound for this event
		 */
		private boolean shouldPlaySound(Event event) {
			if (event.getListenerActor() == event.getTarget()) {
				return false;
			}

			Button button = (Button) event.getListenerActor();
			if (!button.isVisible()) {
				return false;
			}

			if (button.isDisabled()) {
				return false;
			}

			return true;
		}

		private SoundPlayer mSoundPlayer = SoundPlayer.getInstance();
	};

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
			new AnalyticsButtonListener(mButton, mImageName.toString());
			mButton.addListener(mButtonSoundListener);
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
			mButton = createCheckBox(mText, CheckBoxStyles.RADIO);
		}

		/** Button text */
		private String mText = null;
	}
}
