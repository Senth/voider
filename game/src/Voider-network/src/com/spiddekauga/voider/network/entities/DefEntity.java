package com.spiddekauga.voider.network.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Common class for def entities
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class DefEntity implements IEntity {
	/** Def name */
	public String name = null;
	/** Definition type */
	public DefTypes type = null;
	/** Creator key */
	public String creatorKey = null;
	/** Creator name */
	public String creator = null;
	/** Original creator key */
	public String originalCreatorKey = null;
	/** Original creator */
	public String originalCreator = null;
	/** Description */
	public String description = null;
	/** PNG-bytes (optional) */
	public byte[] png = null;
	/** drawable texture, only used on client */
	public Object drawable = null;
	/** Copy parent id, if copied from another definition. Should be null otherwise */
	public UUID copyParentId = null;
	/** resource id */
	public UUID resourceId = null;
	/** Date */
	public Date date = null;
	/** All dependencies */
	public ArrayList<UUID> dependencies = new ArrayList<>();
}
