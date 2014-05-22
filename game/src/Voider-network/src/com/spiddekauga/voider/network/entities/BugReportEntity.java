package com.spiddekauga.voider.network.entities;

import java.util.Date;
import java.util.UUID;

/**
 * Bug report information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReportEntity implements IEntity {
	/** User that's reporting */
	public String userKey;
	/** Subject */
	public String subject;
	/** Last action */
	public String lastAction;
	/** Second last action */
	public String secondLastAction;
	/** Additional description */
	public String description;
	/** Date of the report */
	public Date date;
	/** The exception that was thrown, optional */
	public String exception = "";
	/** System information */
	public String systemInformation;
	/** id of the report, only used locally */
	public UUID id = UUID.randomUUID();
}
