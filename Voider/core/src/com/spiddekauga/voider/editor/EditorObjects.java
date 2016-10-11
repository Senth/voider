package com.spiddekauga.voider.editor;

import java.util.HashMap;
import java.util.UUID;

import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourceRenderShape;
import com.spiddekauga.voider.resources.IResourceUpdate;

/**
 * Contains all editor objects that should be rendered and updated
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class EditorObjects {
	/**
	 * Adds an object
	 * @param resource
	 */
	public void add(IResource resource) {
		mResources.put(resource.getId(), resource);
	}

	/**
	 * Removes a resource
	 * @param resource
	 */
	public void remove(IResource resource) {
		mResources.remove(resource.getId());
	}

	/**
	 * Update all resource
	 * @param deltaTime time elapsed since last frame
	 */
	public void update(float deltaTime) {
		for (IResource resource : mResources.values()) {
			if (resource instanceof IResourceUpdate) {
				((IResourceUpdate) resource).update(deltaTime);
			}
		}
	}

	/**
	 * Render all resources
	 * @param shapeRenderer
	 */
	public void renderShapes(ShapeRendererEx shapeRenderer) {
		for (IResource resource : mResources.values()) {
			if (resource instanceof IResourceEditorRender) {
				((IResourceEditorRender) resource).renderEditor(shapeRenderer);
			}
			if (resource instanceof IResourceRenderShape) {
				((IResourceRenderShape) resource).renderShape(shapeRenderer);
			}
		}
	}


	/** All resources */
	private HashMap<UUID, IResource> mResources = new HashMap<>();
}
