package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

/**
 * Changes the value of a selection box
 */
public class CGuiSelectBox extends CGui {
private SelectBox<?> mSelectBox;
private int mNewIndex;
private int mOldIndex;

/**
 * Creates a command that will change the value of a selection box
 * @param selectBox the SelectBox to change the value of
 * @param newIndex new selected index
 * @param oldIndex old selected index
 */
public CGuiSelectBox(SelectBox<?> selectBox, int newIndex, int oldIndex) {
	mSelectBox = selectBox;
	mNewIndex = newIndex;
	mOldIndex = oldIndex;
}

@Override
public boolean execute() {
	boolean success = setTemporaryName(mSelectBox);
	if (success) {
		mSelectBox.setSelectedIndex(mNewIndex);
		// mSelectBox.fire(new ChangeListener.ChangeEvent());
		setOriginalName(mSelectBox);
	}
	return success;
}

@Override
public boolean undo() {
	boolean success = setTemporaryName(mSelectBox);

	if (success) {
		mSelectBox.setSelectedIndex(mOldIndex);
		// mSelectBox.fire(new ChangeListener.ChangeEvent());
		setOriginalName(mSelectBox);
	}

	return success;
}
}
