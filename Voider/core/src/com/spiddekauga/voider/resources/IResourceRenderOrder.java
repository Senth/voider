package com.spiddekauga.voider.resources;

import com.spiddekauga.voider.Config.Graphics.RenderOrders;


/**
 * Render order of the resource

 */
public interface IResourceRenderOrder extends IResource {
	/**
	 * @return render order of the resource determining in what order it should be
	 *         rendered. Lower value means higher priority (or above everything else).
	 */
	RenderOrders getRenderOrder();
}
