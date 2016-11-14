package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Button;

import java.util.ArrayList;

/**
 * Common button listener for enumeration buttons (checkboxes, images, or something else) where
 * multiple checkboxes can be checked. Use {@link #getChecked()} to get all currently checked
 * buttons as enumerations
 * @param <EnumType> type of enumeration that is stored
 */
public class ButtonEnumListener<EnumType extends Enum<?>> extends ButtonListener {
private Button[] mButtons;
private EnumType[] mEnums;

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
 * @return the first checked enumeration, null if none were checked
 */
public EnumType getCheckedFirst() {
	for (EnumType enumeration : mEnums) {
		Button button = mButtons[enumeration.ordinal()];
		if (button.isChecked()) {
			return enumeration;
		}
	}

	return null;
}

/**
 * @return all checked buttons as enumerations
 */
public ArrayList<EnumType> getChecked() {
	ArrayList<EnumType> list = new ArrayList<>();

	for (EnumType enumeration : mEnums) {
		Button button = mButtons[enumeration.ordinal()];
		if (button.isChecked()) {
			list.add(enumeration);
		}
	}

	return list;
}
}