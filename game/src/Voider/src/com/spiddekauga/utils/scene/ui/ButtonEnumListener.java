package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;

/**
 * Common button listener for enumeration buttons (checkboxes, images, or something else)
 * where multiple checkboxes can be checked. Use {@link #getChecked()} to get all
 * currently checked buttons as enumerations
 * @param <EnumType> type of enumeration that is stored
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ButtonEnumListener<EnumType extends Enum<?>> extends ButtonListener {
	/**
	 * Creates and binds the buttons to the enumerations
	 * @param buttons all buttons
	 * @param enums all enumerations
	 */
	public ButtonEnumListener(Button[] buttons, EnumType[] enums) {
		if (buttons.length != enums.length) {
			throw new IllegalArgumentException("buttons.length != enums.length in EnumButtonListener");
		}

		mButtons = buttons;
		mEnums = enums;

		for (Button button : mButtons) {
			button.addListener(this);
		}
	}

	/**
	 * @return all checked buttons as enumerations
	 */
	protected ArrayList<EnumType> getChecked() {
		ArrayList<EnumType> list = new ArrayList<>();

		for (EnumType enumeration : mEnums) {
			Button button = mButtons[enumeration.ordinal()];
			if (button.isChecked()) {
				list.add(enumeration);
			}
		}

		return list;
	}

	private Button[] mButtons;
	private EnumType[] mEnums;
}