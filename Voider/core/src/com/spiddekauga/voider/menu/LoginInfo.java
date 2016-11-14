package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.utils.event.UpdateEvent;

import java.util.ArrayList;

/**
 * Information passed to main menu when logging in
 */
public class LoginInfo {
/** Update information */
public UpdateEvent updateInfo = null;
/** Message of the Day */
public ArrayList<Motd> motds = null;
}
