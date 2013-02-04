package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.AlignTable;

/**
 * Base class for all GUI containing windows
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Gui {
	/**
	 * Default constructor
	 */
	public Gui() {
		mStage.addActor(mMainTable);
		mMainTable.setKeepSize(true);
		mMainTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mMainTable.setName("MainTable");
	}

	/**
	 * Resizes the GUI object to the appropriate size
	 * @param width new width of the GUI
	 * @param height new height of the GUI
	 */
	public void resize(int width, int height) {
		mStage.setViewport(width, height, true);
		mMainTable.setSize(width, height);
	}

	/**
	 * Adds additional actors to the GUI
	 * @param actor is added to the stage
	 */
	public void addActor(Actor actor) {
		mStage.addActor(actor);
	}

	/**
	 * Resets the GUI and adds the main table again. This
	 * will remove any actors that have been added manually through #addActor(Actor)
	 */
	public void reset() {
		mStage.clear();
		mStage.addActor(mMainTable);
	}

	/**
	 * Initializes the GUI
	 */
	public abstract void initGui();

	/**
	 * Resets the value of the GUI
	 */
	public void resetValues() {
		// Does nothing
	}

	/**
	 * Renders the GUI
	 */
	public final void render() {
		mStage.act(Gdx.graphics.getDeltaTime());
		mStage.draw();
	}

	/**
	 * @return scene of this GUI
	 */
	public Stage getStage() {
		return mStage;
	}

	/**
	 * Checks if a button is checked (from the event).
	 * @param event checks if the target inside the event is a button and it's checked
	 * @return checked button. If the target isn't a button or the button isn't checked
	 * it returns null.
	 */
	protected static Button getCheckedButton(Event event) {
		if (event.getTarget() instanceof Button) {
			Button button  = (Button)event.getTarget();
			if (button.isChecked()) {
				return button;
			}
		}
		return null;
	}

	/**
	 * Checks if a button is checked
	 * @param event checks if the target inside the event is a button and it's checked
	 * @return true if the button is checked, false if the target isn't a button or the
	 * button isn't checked.
	 */
	protected static boolean isButtonChecked(Event event) {
		if (event.getTarget() instanceof Button) {
			return ((Button)event.getTarget()).isChecked();
		}
		return false;
	}

	/**
	 * Checks if a button is pressed
	 * @param event checks if the target inside the event is a button and it's pressed
	 * @return true if the button is pressed, false if the target isn't a button or the
	 * button isn't checked.
	 */
	protected static boolean isButtonPressed(Event event) {
		if (event.getTarget() instanceof Button) {
			return ((Button)event.getTarget()).isPressed();
		}
		return false;
	}

	/**
	 * Adds inner table to the outer table
	 * @param innerTable the table to add to the outerTable, if null outerTable will
	 * only be cleared.
	 * @param outerTable the table to clear and then add innerTable to.
	 */
	protected static void addInnerTable(AlignTable innerTable, AlignTable outerTable) {
		outerTable.clear();

		if (innerTable != null) {
			outerTable.add(innerTable);
			innerTable.invalidateHierarchy();
		}

		outerTable.invalidateHierarchy();
	}


	/** Main table for the layout */
	protected AlignTable mMainTable = new AlignTable();
	/** Stage for the GUI */
	private Stage mStage = new Stage();
}
