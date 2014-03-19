package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.BugReportEntity;

/**
 * Method for reporting bugs to the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReportMethod implements IMethodEntity {
	/** All bugs to report */
	public ArrayList<BugReportEntity> bugs = new ArrayList<>();

	@Override
	public String getMethodName() {
		return "bug-report";
	}
}
