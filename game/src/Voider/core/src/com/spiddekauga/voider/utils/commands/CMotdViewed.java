package com.spiddekauga.voider.utils.commands;

import com.spiddekauga.utils.commands.CRun;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.repo.misc.SettingRepo;

/**
 * Set the MOTD as viewed
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CMotdViewed extends CRun {
	/**
	 * @param motd the message of the day to set as viewed
	 */
	public CMotdViewed(Motd motd) {
		mMotd = motd;
	}

	@Override
	public boolean execute() {
		SettingRepo.getInstance().info().setLatestMotdDate(mMotd);
		return true;
	}

	private Motd mMotd;
}
