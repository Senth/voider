package com.spiddekauga.voider.app;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ImageScrollButton;
import com.spiddekauga.utils.scene.ui.ScrollWhen;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiStyles.ButtonStyles;

/**
 * GUI for the prototype
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PrototypeGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setAlignTable(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setAlignRow(Horizontal.CENTER, Vertical.MIDDLE);

		TextureAtlas atlas = ResourceCacheFacade.get(PrototypeScene.BACKGROUND_TO_USE);

		ImageScrollButton button = new ImageScrollButton(ButtonStyles.TOGGLE.getStyle(), ScrollWhen.ALWAYS);
		button.addLayer(atlas, "bottom", 50);
		button.addLayer(atlas, "top", 100);
		mMainTable.add(button).setSize(900, 600);
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
