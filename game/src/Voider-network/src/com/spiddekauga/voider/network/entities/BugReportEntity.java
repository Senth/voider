package com.spiddekauga.voider.network.entities;

import java.util.Date;

/**
 * Bug report information
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReportEntity implements IEntity {
	/** User that's reporting */
	public String userKey;
	/** Last action */
	public String lastAction;
	/** Second last action */
	public String secondLastAction;
	/** Third last action */
	public String thirdLastAction;
	/** Expected outcome */
	public String expectedOutcome;
	/** Actual outcome */
	public String actualOutcome;
	/** Additional description */
	public String description;
	/** Date of the report */
	public Date date;
	/** The exception that was thrown, optional */
	public Exception exception = null;
}
