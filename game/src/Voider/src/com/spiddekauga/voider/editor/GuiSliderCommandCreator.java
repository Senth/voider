package com.spiddekauga.voider.editor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config.Gui;

/**
 * Listens to a slider and creates appropriate #CGuiSlider commands
 * that will change the value of the slider.
 * 
 * @section Usage
 * To add sliders to listen to, use Slider.addListener(GuiSliderCommandCreator)
 * and it will handle the rest.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GuiSliderCommandCreator implements EventListener {
	/**
	 * Creates an empty GuiSliderCommandCreator with the specified invoker to
	 * send commands to.
	 * @param invoker the invoker to send the commands to
	 */
	public GuiSliderCommandCreator(Invoker invoker) {
		mInvoker = invoker;
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
			Actor target = event.getTarget();
			if (target instanceof Slider) {
				// Send command if the slider value has changed
				sendCommand((Slider) target);
			}
		}

		return false;
	}

	/**
	 * Creates a new slider value changed command
	 * @param slider the slider that changed
	 */
	private void sendCommand(Slider slider) {
		// Only create the command if it wasn't sent by an invoker
		if (slider.getName() == null || !slider.getName().equals(Gui.GUI_INVOKER_TEMP_NAME)) {
			// TODO set slider value?
		}
	}

	/** Invoker to send the commands to */
	private Invoker mInvoker;
}
