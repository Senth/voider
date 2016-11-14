package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.IEntity;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Wrapper for resource a resource revision to upload or download
 */
public class ResourceRevisionEntity implements IEntity {
/** Resource id */
public UUID resourceId;
/** Type */
public UploadTypes type;
/** Revisions */
public ArrayList<RevisionEntity> revisions = new ArrayList<>();
}
