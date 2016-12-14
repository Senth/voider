package com.spiddekauga.voider.servlets.web;

import com.spiddekauga.voider.server.util.VoiderController;

/**
 * Download the game
 */
public class Download extends VoiderController {
@Override
protected void onGet() {
	redirect("download.jsp");
}
}
