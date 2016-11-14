package com.spiddekauga.voider.network.misc;

import com.spiddekauga.voider.network.entities.IMethodEntity;

import java.util.ArrayList;

/**
 * Method for reporting bugs to the server
 */
public class BugReportMethod implements IMethodEntity {
/** All bugs to report */
public ArrayList<BugReportEntity> bugs = new ArrayList<>();

@Override
public MethodNames getMethodName() {
	return MethodNames.BUG_REPORT;
}
}
