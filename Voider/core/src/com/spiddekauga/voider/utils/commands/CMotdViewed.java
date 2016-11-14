package com.spiddekauga.voider.utils.commands;

import com.spiddekauga.utils.commands.CRun;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.user.User;

/**
 * Set the MOTD as viewed
 */
public class CMotdViewed extends CRun {
private Motd mMotd;

/**
 * @param motd the message of the day to set as viewed
 */
public CMotdViewed(Motd motd) {
	mMotd = motd;
}

@Override
public boolean execute() {
	if (User.getGlobalUser().isLoggedIn()) {
		SettingRepo.getInstance().info().setLatestMotdDate(mMotd);
	}
	return true;
}
}
