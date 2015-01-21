package com.spiddekauga.voider.network.entities.misc;

import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Bug report information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReportEntity implements IEntity {
	/** User that's reporting, null if anonymous */
	public String userKey = null;
	/** Subject */
	public String subject;
	// /** Last action */
	// public String lastAction;
	// /** Second last action */
	// public String secondLastAction;
	/** Additional description */
	public String description;
	/** Date of the report */
	public Date date;
	/** Additional information such as exception or last actions, optional */
	public String additionalInformation = "";
	/** System information */
	public String systemInformation;
	/** id of the report, only used locally */
	public UUID id = UUID.randomUUID();
}
