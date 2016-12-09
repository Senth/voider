package com.spiddekauga.utils.scene.ui;

/**
 * GUI events
 */
public class GuiEvent {
private Gui mGui;
private EventTypes mEventType;

/**
 * Create a new GuiEvent
 * @param type of event
 * @param gui which GUI posted the event
 */
GuiEvent(EventTypes type, Gui gui) {
	mEventType = type;
	mGui = gui;
}

/**
 * @return the GUI that posted the event
 */
public Gui getGui() {
	return mGui;
}

public EventTypes getEventType() {
	return mEventType;
}

/**
 * The different types of events
 */
public enum EventTypes {
	CREATE,
	RESUME,
	PAUSE,
	DESTROY,
}
}
