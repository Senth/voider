package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.ImageScrollButton;
import com.spiddekauga.utils.scene.ui.ImageScrollButton.ScrollWhen;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiStyles.ButtonStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

/**
 * Gui for theme select scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ThemeSelectGui extends Gui {

	/**
	 * Sets the scene
	 * @param scene
	 */
	void setScene(ThemeSelectScene scene) {
		mScene = scene;
	}

	@Override
	public void initGui() {
		super.initGui();

		initBackground();
		initButtons();
	}

	/**
	 * Initialize the scrolling background
	 */
	private void initBackground() {
		ImageScrollButton background = new ImageScrollButton(ButtonStyles.PRESS.getStyle(), ScrollWhen.ALWAYS);
		background.setTouchable(Touchable.disabled);

		// Set size
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		width += background.getPadX() * 2;
		height += background.getPadY() * 2;
		background.setSize(width, height);

		// Offset position
		float padLeft = background.getPadLeft();
		float padBottom = background.getPadBottom();
		background.setPosition(-padLeft, -padBottom);

		// Add layers
		float topLayerSpeed = SkinNames.getResource(SkinNames.EditorVars.THEME_TOP_LAYER_SPEED);
		float bottomLayerSpeed = SkinNames.getResource(SkinNames.EditorVars.THEME_BOTTOM_LAYER_SPEED);
		Texture bottomLayer = ResourceCacheFacade.get(mScene.getTheme().getBottomLayer());
		Texture topLayer = ResourceCacheFacade.get(mScene.getTheme().getTopLayer());
		background.addLayer(bottomLayer, bottomLayerSpeed);
		background.addLayer(topLayer, topLayerSpeed);

		addActor(background);
		background.setZIndex(0);
	}

	/**
	 * Initialize buttons
	 */
	private void initButtons() {
		mMainTable.setAlignTable(Horizontal.CENTER, Vertical.BOTTOM);


		// Cancel
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.cancel();
			}
		};
		mUiFactory.button.addText("Cancel", TextButtonStyles.FILLED_PRESS, mMainTable, listener, null, null);
		mUiFactory.button.addPadding(mMainTable);

		// Select
		listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.select();
			}
		};
		mUiFactory.button.addText("Select", TextButtonStyles.FILLED_PRESS, mMainTable, listener, null, null);

		mMainTable.row().setHeight(mUiFactory.getStyles().vars.paddingSeparator);
	}

	/** Theme select scene */
	private ThemeSelectScene mScene = null;
}
