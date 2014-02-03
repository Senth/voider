package com.spiddekauga.voider.network;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Common class for all definitions sent via network
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Def extends Resource {
	/** External dependencies */
	@Tag(1) public Map<UUID, AtomicInteger> externalDependencies = new HashMap<>();
	/** Internal dependencies */
	@Tag(2) public Set<String> internalDependencies = new HashSet<>();
	/** Name of the definition */
	@Tag(3) public String name;
	/** Original creator (if duplicated the definition) */
	@Tag(4) public String originalCreator = null;
	/** Creator of this definition */
	@Tag(5) public String creator;
	/** Description */
	@Tag(6) public String description;
	/** Saved date for the definition */
	@Tag(7) public Date date;
	/** Revision, number of save */
	@Tag(8) public int revision;
	/** When duplicated, this is the id of the resource we duplicated/copied */
	@Tag(9) public UUID copyParentId = null;
}
