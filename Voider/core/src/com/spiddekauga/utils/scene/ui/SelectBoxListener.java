package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.commands.CGuiSelectBox;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.Config.Gui;

/**
 * Listens to a selection box and calls onSelectionChanged(Object) and onSelectionChanged(int) when
 * the selection has been changed. Override any of these methods.
 * @param <ItemType> stored items in the selection box
 */
public class SelectBoxListener<ItemType> implements EventListener {
/** The selection box we're listening to */
protected SelectBox<ItemType> mSelectBox = null;
/** Invoker */
private Invoker mInvoker = null;
/** Last selected item */
private int mLastSelectedIndex = 0;

/**
 * Creates an invalid selection box listener. To use it call {@link #setSelectBox(SelectBox)}
 */
public SelectBoxListener() {
	// Does nothing
}

/**
 * Creates an invalid selection box listener. To use it call
 * @param invoker ability to undo/redo selections
 */
public SelectBoxListener(Invoker invoker) {
	mInvoker = invoker;
}


/**
 * Creates the selection box and automatically adds this class as a listener.
 * @param selectBox the selectionBox we should listen to
 */
public SelectBoxListener(SelectBox<ItemType> selectBox) {
	setSelectBox(selectBox);
}

/**
 * Sets the selection box
 * @param selectBox the selection box that should be listened to
 */
public void setSelectBox(SelectBox<ItemType> selectBox) {
	mSelectBox = selectBox;
	mSelectBox.addListener(this);
	mLastSelectedIndex = mSelectBox.getSelectedIndex();
}

/**
 * Creates the selection box and automatically adds this class as a listener.
 * @param selectBox the selectionBox we should listen to
 * @param invoker ability to undo/redo selections
 */
public SelectBoxListener(SelectBox<ItemType> selectBox, Invoker invoker) {
	mInvoker = invoker;
	setSelectBox(selectBox);
}

@Override
public boolean handle(Event event) {
	// Key was pressed
	if (event instanceof InputEvent) {
		InputEvent inputEvent = (InputEvent) event;
		if (inputEvent.getType() == Type.keyDown) {
			// ESC, BACK, ENTER -> Unfocus
			if (inputEvent.getKeyCode() == Input.Keys.ENTER || KeyHelper.isBackPressed(inputEvent.getKeyCode())) {
				mSelectBox.getStage().setKeyboardFocus(null);
				return true;
			}
			// Redo
			else if (KeyHelper.isRedoPressed(inputEvent.getKeyCode())) {
				if (mInvoker != null) {
					mInvoker.redo();
				}
			}
			// Undo
			else if (KeyHelper.isUndoPressed(inputEvent.getKeyCode())) {
				if (mInvoker != null) {
					mInvoker.undo();
				}
			}
		}
	}

	if (mLastSelectedIndex != mSelectBox.getSelectedIndex()) {
		onSelectionChanged(mSelectBox.getSelectedIndex());

		// Send invoker commands
		if (mInvoker != null) {
			if (!Gui.GUI_INVOKER_TEMP_NAME.equals(mSelectBox.getName())) {
				mInvoker.execute(new CGuiSelectBox(mSelectBox, mSelectBox.getSelectedIndex(), mLastSelectedIndex));
			}
		}

		mLastSelectedIndex = mSelectBox.getSelectedIndex();

		return true;
	}
	return false;
}

/**
 * Called when the selection has been changed. Override this method.
 * @param itemIndex the index of the newly selected item
 */
protected void onSelectionChanged(int itemIndex) {
	// Does nothing.
}
}
