package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.utils.event.UpdateEvent;

/**
 * Information passed to main menu when logging in
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginInfo {
	/** Update information */
	public UpdateEvent updateInfo = null;
	/** Message of the Day */
	public ArrayList<Motd> motds = null;
}
