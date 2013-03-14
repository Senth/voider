package com.spiddekauga.utils.scene.ui;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Vector2Pool;

/**
 * Listens to a GUI actor to display a tooltip for it.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TooltipListener implements EventListener {
	/**
	 * Creates a tooltip listener that will listen to the specified
	 * actor. This will automatically add itself as a listener to
	 * the actor.
	 * @param actor the GUI actor to listen to
	 * @param title the title of the tooltip window
	 * @param message the message in the tooltip
	 */
	public TooltipListener(Actor actor, String title, String message) {
		mMessage = message;
		mActor = actor;
		mTitle = title;
		mActor.addListener(this);

		if (mWindow == null) {
			Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

			mWindow = new Window("", editorSkin);
			mWindow.setModal(false);
			mWindow.addListener(this);
		}
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			//			 WINDOW (Hover events)
			if (((InputEvent) event).getType() == Type.enter) {
				if (mActor.isAscendantOf(event.getTarget())) {
					scheduleShowWindowTask();
				}
			} else if (((InputEvent) event).getType() == Type.exit) {
				// Only do something if the cursor is outside the actor
				if (!isCursorInsideActor()) {
					handleHoverExit();
				}

			} else if (((InputEvent) event).getType() == Type.mouseMoved) {
				// Update position if just moved
				if (isWindowDisplayingThis()) {
					updateWindowPosition();

					// Remove window, we're outside of the actor
					if (!isCursorInsideActor()) {
						handleHoverExit();
					}
				}
				// Reset shown window
				else if (mShowWindowTask != null) {
					cancelShowWindowTask();
					scheduleShowWindowTask();
				}
			}

			// MSG BOX (Press events)
			else if (((InputEvent) event).getType() == Type.touchDown) {
				// Always skip if window is shown
				if (!isWindowShown()) {
					scheduleShowMsgBoxTask();
				}
			}
			else if (((InputEvent) event).getType() == Type.touchUp) {
				cancelShowMsgBoxTask();
			}
		}

		return false;
	}

	/**
	 * Creates and schedules a new show window task
	 */
	private void scheduleShowWindowTask() {
		if (mShowWindowTask == null) {
			mShowWindowTask = new ShowWindowTask();
			Timer.schedule(mShowWindowTask, Config.Gui.TOOLTIP_HOVER_SHOW);
		}
	}

	/**
	 * Cancels the window task and sets it to null
	 */
	private void cancelShowWindowTask() {
		if (mShowWindowTask != null) {
			mShowWindowTask.cancel();
			mShowWindowTask = null;
		}
	}

	/**
	 * Creates and schedules a new show msg box task
	 */
	private void scheduleShowMsgBoxTask() {
		if (mShowMsgBoxTask == null) {
			mShowMsgBoxTask = new ShowMsgBoxTask();
			Timer.schedule(mShowMsgBoxTask, Config.Gui.TOOLTIP_PRESS_SHOW);
		}
	}

	/**
	 * Cancels the show msg box task and sets it to null
	 */
	private void cancelShowMsgBoxTask() {
		if (mShowMsgBoxTask != null) {
			mShowMsgBoxTask.cancel();
			mShowMsgBoxTask = null;
		}
	}

	/**
	 * Handles when the cursor moves out from the actor
	 */
	private void handleHoverExit() {
		// Fade out window, then remove it from stage if it is shown.
		if (mWindow.getStage() != null) {
			mWindow.addAction(Actions.sequence(Actions.fadeOut(Config.Gui.TOOLTIP_HOVER_FADE_DURATION, Interpolation.fade), Actions.removeActor()));
		}

		cancelShowWindowTask();
	}

	/**
	 * Updates the window position to the current cursor position. The position
	 * will be one of the corners of the window. The ordering is as follows
	 * \li Lower right corner
	 * \li Lower left corner
	 * \li Upper right corner
	 * \li Upper left corner
	 * If the window would be outside of the screen, it will chose a position with
	 * a lower priority until it finds one where the whole window is inside the screen.
	 */
	private static void updateWindowPosition() {
		if (mWindow.getStage() != null) {
			int windowWidth = (int) mWindow.getWidth();
			int windowHeight = (int) mWindow.getHeight();
			int screenWidth = Gdx.graphics.getWidth();
			int screenHeight = Gdx.graphics.getHeight();

			int cursorX = Gdx.input.getX();
			int cursorY = Gdx.input.getY();

			// Lower right corner
			if (cursorX - windowWidth >= 0 && cursorY - windowHeight >= 0) {
				mWindow.setPosition(cursorX - windowWidth, screenHeight - cursorY);
			}
			// Lower left corner
			else if (cursorX + windowWidth <= screenWidth && cursorY - windowHeight >= 0) {
				mWindow.setPosition(cursorX + windowWidth, screenHeight - cursorY);
			}
			// Upper right corner
			else if (cursorX - windowWidth >= 0 && cursorY + windowHeight <= screenHeight) {
				mWindow.setPosition(cursorX - windowWidth, screenHeight - (cursorY + windowHeight));
			}
			// Upper left corner
			else if (cursorX + windowWidth <= screenWidth && cursorY + windowHeight <= screenHeight) {
				mWindow.setPosition(cursorX + windowWidth, screenHeight - (cursorY + windowHeight));
			}
		}
	}

	/**
	 * @return true if the cursor is inside the current actor
	 */
	private boolean isCursorInsideActor() {
		int cursorX = Gdx.input.getX();
		int cursorY = Gdx.graphics.getHeight() - Gdx.input.getY();

		Vector2 min = Vector2Pool.obtain();
		min.set(mActor.getX(), mActor.getY());
		mActor.localToStageCoordinates(min);


		int minX = (int) min.x;
		int minY = (int) min.y;
		int maxX = (int) (min.x + mActor.getWidth());
		int maxY = (int) (min.y + mActor.getHeight());

		if (cursorX <= minX || cursorX >= maxX || cursorY <= minY || cursorY >= maxY) {
			return false;
		}

		return true;
	}

	/**
	 * Shows the window
	 */
	private void showWindow() {
		Stage stage = SceneSwitcher.getStage();
		if (stage != null) {
			stage.addActor(mWindow);
			mWindow.clearActions();
			mWindow.clearChildren();
			mWindow.setTitle(mTitle);
			mWindow.add(mMessage);
			mWindow.pack();
			mWindow.addAction(Actions.fadeIn(Config.Gui.TOOLTIP_HOVER_FADE_DURATION, Interpolation.fade));
			updateWindowPosition();
		} else {
			Gdx.app.error("TooltipListener", "Stage is not when showing window!");
		}
	}

	/**
	 * Shows the message box
	 */
	private void showMsgBox() {
		Gui gui = SceneSwitcher.getGui();
		if (gui != null) {
			Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
			TextButtonStyle buttonStyle = editorSkin.get("default", TextButtonStyle.class);

			mMsgBox = SceneSwitcher.getGui().getFreeMsgBox();
			Button okButton = new TextButton("OK", buttonStyle);
			mMsgBox.button(okButton);
			mMsgBox.setTitle(mTitle);
			mMsgBox.content(mMessage);
			gui.showMsgBox(mMsgBox);
		}
	}

	/**
	 * @return true if the window is currently shown
	 */
	private boolean isWindowShown() {
		return mWindow.getStage() != null;
	}

	/**
	 * @return true if the window is currently displaying this tooltip
	 */
	private boolean isWindowDisplayingThis() {
		return isWindowShown() && mWindow.getTitle().equals(mTitle);
	}

	/**
	 * Timer task that shows the window
	 */
	private class ShowWindowTask extends Task {
		@Override
		public void run() {
			showWindow();
			mShowWindowTask = null;
		}
	}

	/**
	 * Timer task that shows the message box
	 */
	private class ShowMsgBoxTask extends Task {
		@Override
		public void run() {
			showMsgBox();
			mShowMsgBoxTask = null;
		}
	}

	/** Title of the window */
	private String mTitle;
	/** Message to display in the tooltip */
	private String mMessage;
	/** GUI Actor we're listening to */
	private Actor mActor;
	/** Task that shows the window */
	private Task mShowWindowTask = null;
	/** Task that show the message box */
	private Task mShowMsgBoxTask = null;

	/** Window for all tooltip listeners (as only one tooltip can be
	 * displayed at the same time this is static */
	private static Window mWindow = null;
	/** Message box for mobile devices */
	private static MsgBoxExecuter mMsgBox = null;
}