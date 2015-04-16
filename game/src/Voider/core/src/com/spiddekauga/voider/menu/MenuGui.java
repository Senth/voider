package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * Common class for all menus used by Main Menu Scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class MenuGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		setBackground(SkinNames.GeneralImages.BACKGROUND_SPACE, true);
	}
}
