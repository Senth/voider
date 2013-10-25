package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceSelectable;
import com.spiddekauga.voider.utils.Pools;

/**
 * Current selection in the level editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Selection implements ISelection {

	@Override
	public void selectResource(IResource resource) {
		mSelectedResources.add(resource);

		if (resource instanceof IResourceSelectable) {
			((IResourceSelectable) resource).setSelected(true);
		}

		for (ISelectionListener listener : mListeners) {
			listener.onResourceSelected(resource);
		}
	}

	@Override
	public void selectResources(ArrayList<IResource> resources) {
		for (IResource resource : resources) {
			selectResource(resource);
		}
	}

	@Override
	public void selectResources(IResource[] resources) {
		for (IResource resource : resources) {
			selectResource(resource);
		}
	}

	@Override
	public void deselectResource(IResource resource) {
		boolean removed = mSelectedResources.remove(resource);

		if (removed) {
			if (resource instanceof IResourceSelectable) {
				((IResourceSelectable) resource).setSelected(false);
			}

			for (ISelectionListener listener : mListeners) {
				listener.onResourceDeselected(resource);
			}
		}
	}

	@Override
	public void deselectResources(ArrayList<IResource> resources) {
		for (IResource resource : resources) {
			deselectResource(resource);
		}
	}

	@Override
	public void deselectResources(IResource[] resources) {
		for (IResource resource : resources) {
			deselectResource(resource);
		}
	}

	@Override
	public ArrayList<IResource> getSelectedResources() {
		return mSelectedResources;
	}

	@Override
	public Class<? extends IResource> getMostCommonSelectedResourceType() {
		HashMap<Class<? extends IResource>, AtomicInteger> cTypeInstances = new HashMap<Class<? extends IResource>, AtomicInteger>();


		// Count instances
		for (IResource resource : mSelectedResources) {
			AtomicInteger count = cTypeInstances.get(resource.getClass());

			if (count == null) {
				count = new AtomicInteger(0);
				cTypeInstances.put(resource.getClass(), count);
			}

			count.incrementAndGet();
		}


		// Check highest count
		Class<? extends IResource> highestTypeCount = null;
		int highestCount = 0;

		for (Entry<Class<? extends IResource>, AtomicInteger> entry : cTypeInstances.entrySet()) {
			if (entry.getValue().get() > highestCount) {
				highestCount = entry.getValue().get();
				highestTypeCount = entry.getKey();
			}
		}

		return highestTypeCount;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <ResourceType extends IResource> ArrayList<ResourceType> getSelectedResourcesOfType(Class<ResourceType> type) {
		ArrayList<ResourceType> selectedActors = Pools.arrayList.obtain();

		for (IResource selectedResource : mSelectedResources) {
			if (selectedResource.getClass() == type) {
				selectedActors.add((ResourceType) selectedResource);
			}
		}

		return selectedActors;
	}


	@Override
	public void clearSelection() {
		for (IResource resource : mSelectedResources) {
			if (resource instanceof IResourceSelectable) {
				((IResourceSelectable) resource).setSelected(false);
			}

			for (ISelectionListener listener : mListeners) {
				listener.onResourceDeselected(resource);
			}
		}

		mSelectedResources.clear();
	}

	@Override
	public boolean isSelected(IResource resource) {
		return mSelectedResources.contains(resource);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <ResourceType extends IResource> ResourceType getFirstSelectedResourceOfType(Class<ResourceType> type) {
		for (IResource resource : mSelectedResources) {
			if (resource.getClass() == type) {
				return (ResourceType) resource;
			}
		}

		return null;
	}

	@Override
	public void addListener(ISelectionListener listener) {
		mListeners.add(listener);
	}

	@Override
	public void removeListener(ISelectionListener listener) {
		mListeners.remove(listener);
	}

	@Override
	public boolean isEmpty() {
		return mSelectedResources.isEmpty();
	}

	@Override
	public int getSize() {
		return mSelectedResources.size();
	}

	/** All listeners */
	private ArrayList<ISelectionListener> mListeners = new ArrayList<ISelectionListener>();
	/** Current resource selection */
	private ArrayList<IResource> mSelectedResources = new ArrayList<IResource>();
}
