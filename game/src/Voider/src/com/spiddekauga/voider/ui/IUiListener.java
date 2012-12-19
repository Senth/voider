package com.spiddekauga.voider.ui;

/**
 * Listens to UI events
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IUiListener {
	/**
	 * Called when an event has been sent
	 * @param event information about the event
	 */
	public void onUiEvent(UiEvent event);
}
