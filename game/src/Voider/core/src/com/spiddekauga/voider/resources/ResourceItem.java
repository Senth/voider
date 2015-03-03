package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.scene.Scene;

/**
 * Wrapper to simplify the queue. Only available for this pacakage
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceItem {
	/**
	 * Checks whether the resources are the same
	 * @param object another queue item
	 * @return true if the parameter is a queue item and they have the same resourceId.
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else if (object == null) {
			return false;
		} else if (object.getClass() == this.getClass()) {
			return ((ResourceItem) object).id.equals(id);
		} else if (object instanceof UUID) {
			return id.equals(object);
		} else {
			return false;
		}
	}

	/**
	 * Constructor that sets the resource id and type
	 * @param resourceId id of the resource
	 * @param revision the revision of the resource
	 */
	public ResourceItem(UUID resourceId, int revision) {
		this(null, resourceId, revision);
	}

	/**
	 * Constructor that sets the resource id and type
	 * @param scene to use for the resource
	 * @param resourceId id of the resource
	 * @param revision the revision of the resourc
	 */
	public ResourceItem(Scene scene, UUID resourceId, int revision) {
		this.scene = scene;
		this.id = resourceId;
		this.revision = revision;
	}

	/**
	 * Default constructor for queue item, used for when reading instances from json
	 */
	public ResourceItem() {
		// Does nothing
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/** Number of references */
	@Tag(96) public int count = 1;
	/** Scene */
	@Tag(97) public Scene scene = null;
	/** Unique id */
	@Tag(98) public UUID id = null;
	/** revision of the resource */
	@Tag(101) public int revision = -1;
}
