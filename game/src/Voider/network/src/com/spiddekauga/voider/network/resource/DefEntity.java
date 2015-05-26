package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Common class for def entities
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class DefEntity implements IEntity {
	/** Def name */
	public String name = null;
	/** Definition type */
	public UploadTypes type = null;
	/** Creator key */
	public String revisedByKey = null;
	/** Creator name */
	public String revisedBy = null;
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

	// Other temporary variables (never used between networks)
	/** Revision to load */
	public int revision = -1;

	private static final long serialVersionUID = 1L;
}
