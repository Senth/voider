package com.spiddekauga.voider.resources;

import java.util.Date;
import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.network.entities.BugReportEntity;


/**
 * Bug report to store locally
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BugReportDef extends Resource {
	/**
	 * Default constructor, sets UUID
	 */
	public BugReportDef() {
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * @return a new network entity from this Bug Report resource
	 */
	public BugReportEntity toNetworkEntity() {
		BugReportEntity entity = new BugReportEntity();

		entity.userKey = userKey;
		entity.lastAction = lastAction;
		entity.secondLastAction = secondLastAction;
		entity.thirdLastAction = thirdLastAction;
		entity.expectedOutcome = expectedOutcome;
		entity.actualOutcome = actualOutcome;
		entity.description = description;
		entity.exception = exception;
		entity.date = date;

		return entity;
	}

	/** User that's reporting */
	@Tag(113) public String userKey;
	/** Last action */
	@Tag(114) public String lastAction;
	/** Second last action */
	@Tag(115) public String secondLastAction;
	/** Third last action */
	@Tag(116) public String thirdLastAction;
	/** Expected outcome */
	@Tag(117) public String expectedOutcome;
	/** Actual outcome */
	@Tag(118) public String actualOutcome;
	/** Additional description */
	@Tag(119) public String description;
	/** Date of the report */
	@Tag(120) public Date date;
	/** The exception that was thrown, optional */
	@Tag(121) public Exception exception = null;
}
