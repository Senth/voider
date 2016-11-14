package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.utils.Pool.Poolable;

import java.util.UUID;

/**
 * Identifier for a user resource
 */
class UserResourceIdentifier implements Poolable {
/** Latest resource */
static final int LATEST_REVISION = -1;
/** UUID of the resource */
UUID resourceId = null;
/** revision of the resource */
int revision = LATEST_REVISION;

/**
 * Default constructor
 */
UserResourceIdentifier() {
	// Does nothing
}

/**
 * Sets the resource and revision
 * @param resourceId the resource id
 * @param revision the revision to use
 */
UserResourceIdentifier(UUID resourceId, int revision) {
	this.resourceId = resourceId;
	this.revision = revision;
}

/**
 * Sets the resource and revision
 * @param resourceId the resource id
 * @param revision the revision to use
 * @return this object for chaining
 */
UserResourceIdentifier set(UUID resourceId, int revision) {
	this.resourceId = resourceId;
	this.revision = revision;
	return this;
}

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
	result = prime * result + revision;
	return result;
}

@Override
public boolean equals(Object obj) {
	if (this == obj) {
		return true;
	}
	if (obj == null) {
		return false;
	}
	if (getClass() != obj.getClass()) {
		return false;
	}
	UserResourceIdentifier other = (UserResourceIdentifier) obj;
	if (resourceId == null) {
		if (other.resourceId != null) {
			return false;
		}
	} else if (!resourceId.equals(other.resourceId)) {
		return false;
	}
	if (revision != other.revision) {
		return false;
	}
	return true;
}

@Override
public String toString() {
	if (resourceId != null) {
		if (revision >= 0) {
			return resourceId.toString() + "(" + revision + ")";
		} else {
			return resourceId.toString();
		}
	} else {
		return "null";
	}
}

@Override
public void reset() {
	resourceId = null;
	revision = LATEST_REVISION;
}
}
