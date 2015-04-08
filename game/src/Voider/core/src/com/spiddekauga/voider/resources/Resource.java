package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.kryo.KryoPostRead;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.Pools;

/**
 * Base class for all resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class Resource implements IResource, KryoPostRead {
	@Override
	public UUID getId() {
		return mUniqueId;
	}

	@Override
	public void setId(UUID id) {
		mUniqueId = id;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		} else if (object.getClass() == this.getClass()) {
			if (mUniqueId.equals(((IResource) object).getId())) {
				if (this instanceof IResourceRevision && object instanceof IResourceRevision) {
					return ((IResourceRevision) this).getRevision() == ((IResourceRevision) object).getRevision();
				} else {
					return true;
				}
			}
		} else if (object instanceof UUID) {
			Config.Debug.deprecatedException();
			Gdx.app.error("Resource", "Deprecated use of Resource.equals(UUID)");
			Thread.dumpStack();
			return mUniqueId.equals(object);
		}
		return false;
	}

	/**
	 * Does a shallow copy from another resource and sets it to this.
	 * @param resource the definition to copy from
	 */
	public void set(Resource resource) {
		assert (getClass() == resource.getClass());

		if (this instanceof Disposable) {
			((Disposable) this).dispose();
		}
		mUniqueId = resource.mUniqueId;
		mListeners = resource.mListeners;
	}

	/**
	 * Creates an exact copy of this object
	 * @param <ResourceType> the type of this resource
	 * @return copy of this resource
	 * @see #copyNewResource() creates a duplicate of this resource changing at least its
	 *      id.
	 */
	@SuppressWarnings("unchecked")
	public final <ResourceType> ResourceType copy() {
		Kryo kryo = Pools.kryo.obtain();
		ResourceType copy = (ResourceType) kryo.copy(this);
		Pools.kryo.free(kryo);
		return copy;
	}

	/**
	 * Creates a duplicate of this object. In general this means the resource will get a
	 * new id. Derived classes can override this behavior and add additional changes.
	 * @param <ResourceType> the type of this resource
	 * @return duplicate of this object
	 * @see #copy() creates an exact copy of this object
	 */
	@SuppressWarnings("unchecked")
	public <ResourceType> ResourceType copyNewResource() {
		Kryo kryo = Pools.kryo.obtain();
		ResourceType copy = (ResourceType) kryo.copy(this);
		Pools.kryo.free(kryo);

		((Resource) copy).mUniqueId = UUID.randomUUID();

		return copy;
	}

	@Override
	public void postRead() {
		if (mListeners == null) {
			mListeners = new ArrayList<>();
		}
	}

	@Override
	public int hashCode() {
		return mUniqueId.hashCode();
	}

	@Override
	public void removeBoundResource(IResource boundResource, List<Command> commands) {
		if (boundResource instanceof IResourceChangeListener) {
			final IResourceChangeListener changeListener = (IResourceChangeListener) boundResource;
			if (mListeners.contains(changeListener)) {
				Command command = new Command() {
					@Override
					public boolean undo() {
						mListeners.add(changeListener);
						return true;
					}

					@Override
					public boolean execute() {
						mListeners.remove(changeListener);
						return true;
					}
				};
				commands.add(command);
			}
		}
	}

	@Override
	public void addChangeListener(IResourceChangeListener listener) {
		if (mListeners == null) {
			mListeners = new ArrayList<IResourceChangeListener>();
		}
		mListeners.add(listener);
	}

	@Override
	public void removeChangeListener(IResourceChangeListener listener) {
		if (mListeners != null) {
			mListeners.remove(listener);
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

	/**
	 * @return simple name of the class and it's id
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + mUniqueId;
	}

	/** Unique id of the resource */
	@Tag(1) protected UUID mUniqueId = null;
	/** Listeners of the resource */
	@Tag(2) private ArrayList<IResourceChangeListener> mListeners = new ArrayList<>();

}
