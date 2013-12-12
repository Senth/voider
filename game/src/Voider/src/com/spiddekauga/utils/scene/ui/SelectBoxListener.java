package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

/**
 * Listens to a selection box and calls onSelectionChanged(Object) and
 * onSelectionChanged(int) when the selection has been changed. Override
 * any of these methods.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SelectBoxListener implements EventListener {
	/**
	 * Creates the selection box and automatically adds this class as a
	 * listener.
	 * @param selectBox the selectionBox we should listen to
	 */
	public SelectBoxListener(SelectBox selectBox) {
		mSelectBox = selectBox;
		mSelectBox.addListener(this);
		mLastSelectedIndex = mSelectBox.getSelectionIndex();
	}

	@Override
	public boolean handle(Event event) {
		if (mLastSelectedIndex != mSelectBox.getSelectionIndex()) {
			mLastSelectedIndex = mSelectBox.getSelectionIndex();

			onSelectionChanged(mLastSelectedIndex);
			onSelectionChanged(mSelectBox.getSelection());

			return true;
		}
		return false;
	}

	/**
	 * Called when the selection has been changed. Override this method
	 * @param selectedItem the string that was selected
	 * @see #onSelectionChanged(int)
	 */
	protected void onSelectionChanged(String selectedItem) {
		// Does nothing
	}

	/**
	 * Called when the selection has been changed. Override this method.
	 * @param itemIndex the index of the newly selected item
	 * @see #onSelectionChanged(String)
	 */
	protected void onSelectionChanged(int itemIndex) {
		// Does nothing.
	}

	/** The selection box we're listening to */
	protected SelectBox mSelectBox;
	/** Last selected item */
	private int mLastSelectedIndex;
}
