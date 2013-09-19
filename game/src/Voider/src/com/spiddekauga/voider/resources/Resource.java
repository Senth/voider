package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.Pools;

/**
 * Base class for all resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Resource implements IResource, Json.Serializable {
	@Override
	public UUID getId() {
		return mUniqueId;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		} else if (object.getClass() == this.getClass()) {
			if (mUniqueId.equals(((IResource)object).getId())) {
				if (this instanceof IResourceRevision && object instanceof IResourceRevision) {
					return ((IResourceRevision)this).getRevision() == ((IResourceRevision)object).getRevision();
				} else {
					return true;
				}
			}
		} else if (object instanceof UUID) {
			return mUniqueId.equals(object);
		}
		return false;
	}

	/**
	 * Creates an exact copy of this object
	 * @param <ResourceType> the type of this resource
	 * @return copy of this resource
	 * @see #copyNewResource() creates a duplicate of this resource changing at least its id.
	 */
	@SuppressWarnings("unchecked")
	public final <ResourceType> ResourceType copy() {
		Kryo kryo = Pools.kryo.obtain();
		ResourceType copy = (ResourceType) kryo.copy(this);
		Pools.kryo.free(kryo);
		return copy;
	}

	/**
	 * Creates a duplicate of this object. In general this means the resource will
	 * get a new id. Derived classes can override this behavior and add additional
	 * changes.
	 * @param <ResourceType> the type of this resource
	 * @return duplicate of this object
	 * @see #copy() creates an exact copy of this object
	 */
	public <ResourceType> ResourceType copyNewResource() {
		ResourceType copy = copy();
		((Resource)copy).mUniqueId = UUID.randomUUID();
		return copy;
	}

	@Override
	public int hashCode() {
		return mUniqueId.hashCode();
	}

	@Override
	public void getReferences(ArrayList<UUID> references) {
		if (mListenerIds != null) {
			references.addAll(mListenerIds);
		}
	}

	@Override
	public boolean bindReference(IResource resource) {
		if (resource instanceof IResourceChangeListener) {
			addChangeListener((IResourceChangeListener) resource);
			return true;
		}

		return false;
	}

	@Override
	public boolean addBoundResource(IResource boundResource)  {
		if (boundResource instanceof IResourceChangeListener) {
			addChangeListener((IResourceChangeListener) boundResource);
			return true;
		}

		return false;
	}

	@Override
	public boolean removeBoundResource(IResource boundResource) {
		if (boundResource instanceof IResourceChangeListener) {
			removeChangeListener((IResourceChangeListener)boundResource);
			return true;
		}

		return false;
	}

	@Override
	public void addChangeListener(IResourceChangeListener listener) {
		if (mListeners == null) {
			mListeners = new ArrayList<IResourceChangeListener>();
		}
		if (mListenerIds == null) {
			mListenerIds = new ArrayList<UUID>();
		}
		mListeners.add(listener);
		mListenerIds.add(listener.getId());
	}

	@Override
	public void removeChangeListener(IResourceChangeListener listener) {
		if (mListeners != null) {
			mListeners.remove(listener);
			mListenerIds.remove(listener.getId());
		}
	}

	/**
	 * Sends an change event to the listeners
	 * @param type what was changed?
	 */
	protected void sendChangeEvent(IResourceChangeListener.EventTypes type) {
		if (mListeners != null) {
			for (IResourceChangeListener listener : mListeners) {
				listener.onResourceChanged(this, type);
			}
		}
	}

	@Override
	public void write(Json json) {
		json.writeValue("Config.REVISION", Config.REVISION);
		json.writeValue("mUniqueId", mUniqueId);
		json.writeValue("mListenerIds", mListenerIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, JsonValue jsonData) {
		mUniqueId = json.readValue("mUniqueId", UUID.class, jsonData);
		mListenerIds = json.readValue("mListenerIds", ArrayList.class, jsonData);
	}

	/** Unique id of the resource */
	@Tag(1) protected UUID mUniqueId = null;
	/** Listeners of the resource */
	@Tag(2) private ArrayList<IResourceChangeListener> mListeners = null;
	/** Listener ids */
	@Deprecated
	private ArrayList<UUID> mListenerIds = null;

}
