/**
 * @file
 * @version 0.1
 * Copyright © Matteus Magnusson
 *
 * @section LICENSE
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details at
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.spiddekauga.voider.resources.IResource;

/**
 * Interface for listening to trigger events
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface ITriggerListener extends IResource {
	/**
	 * Called when the trigger has been triggered.
	 * @param action the action the listener should take (i.e. what it was
	 * associated with).
	 */
	public void onTriggered(TriggerAction action);

	/**
	 * @return all trigger information necessary for binding triggers
	 */
	public ArrayList<TriggerInfo> getTriggerInfos();
}
