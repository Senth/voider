package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

/**
 * Listens to a selection box and calls onSelectionChanged(Object) and
 * onSelectionChanged(int) when the selection has been changed. Override any of these
 * methods.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SelectBoxListener implements EventListener {
	/**
	 * Creates an invalid selection box listener. To use it call
	 * {@link #setSelectBox(SelectBox)}
	 */
	public SelectBoxListener() {
		// Does nothing
	}

	/**
	 * Creates the selection box and automatically adds this class as a listener.
	 * @param selectBox the selectionBox we should listen to
	 */
	public SelectBoxListener(SelectBox<?> selectBox) {
		setSelectBox(selectBox);
	}

	/**
	 * Sets the selection box
	 * @param selectBox the selection box that should be listened to
	 */
	public void setSelectBox(SelectBox<?> selectBox) {
		mSelectBox = selectBox;
		mSelectBox.addListener(this);
		mLastSelectedIndex = mSelectBox.getSelectedIndex();
	}


	@Override
	public boolean handle(Event event) {
		if (mLastSelectedIndex != mSelectBox.getSelectedIndex()) {
			mLastSelectedIndex = mSelectBox.getSelectedIndex();

			onSelectionChanged(mLastSelectedIndex);

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

	/** The selection box we're listening to */
	protected SelectBox<?> mSelectBox = null;
	/** Last selected item */
	private int mLastSelectedIndex = 0;
}
