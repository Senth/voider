package com.spiddekauga.voider.network.misc;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for reporting bugs to the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BugReportMethod implements IMethodEntity {
	/** All bugs to report */
	public ArrayList<BugReportEntity> bugs = new ArrayList<>();

	@Override
	public MethodNames getMethodName() {
		return MethodNames.BUG_REPORT;
	}
}
