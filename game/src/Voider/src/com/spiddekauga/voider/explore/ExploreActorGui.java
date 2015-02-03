package com.spiddekauga.voider.explore;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.repo.analytics.listener.AnalyticsButtonListener;
import com.spiddekauga.voider.repo.resource.SkinNames;

/**
 * Common GUI class for exploring actors. Override this class to make the layout and
 * actions more specific
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreActorGui extends ExploreGui {
	/**
	 * Hidden constructor
	 */
	protected ExploreActorGui() {
		// Does nothing
	}

	/**
	 * Creates actor content table
	 * @param actor the actor to create
	 * @param selected true if the actor is selected
	 * @return table with actor image and name
	 */
	@Override
	protected AlignTable createContentActor(final DefEntity actor, boolean selected) {
		AlignTable table = new AlignTable();
		table.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		ImageButtonStyle defaultImageStyle = SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_TOGGLE);
		ImageButtonStyle imageButtonStyle = new ImageButtonStyle(defaultImageStyle);
		imageButtonStyle.imageUp = (Drawable) actor.drawable;

		Button button = new ImageButton(imageButtonStyle);
		button.setChecked(selected);
		table.row().setFillWidth(true);
		table.add(button).setFillWidth(true).setKeepAspectRatio(true);

		new ButtonListener(button) {
			@Override
			protected void onChecked(Button button, boolean checked) {
				if (checked) {
					mScene.setSelected(actor);
				}
			}

			@Override
			protected void onDown(Button button) {
				mWasCheckedOnDown = button.isChecked();
			}

			@Override
			protected void onUp(Button button) {
				if (mWasCheckedOnDown) {
					mScene.selectAction();
				}
			}

			/** If this actor was selected before */
			private boolean mWasCheckedOnDown = false;
		};
		addContentButton(button);

		// Analytics
		String fullActorType = actor.getClass().getSimpleName();
		int actorNameIndex = fullActorType.indexOf("DefEntity");
		String actorType = fullActorType;
		if (actorNameIndex != -1) {
			actorType = fullActorType.substring(0, actorNameIndex);
		}
		new AnalyticsButtonListener(button, "Explore" + actorType + " _Select", actor.name + " (" + actor.resourceId + ":" + actor.revision + ")");


		// Actor name
		table.row();
		mUiFactory.text.add(actor.name, table);
		table.getCell().setHeight(mUiFactory.getStyles().vars.rowHeight);

		return table;
	}

	/**
	 * Sets the actor scene
	 * @param scene
	 */
	protected void setActorScene(ExploreActorScene scene) {
		mScene = scene;
	}

	@Override
	protected void onFetchMoreContent() {
		// Does nothing
	}

	@Override
	protected float getMaxActorWidth() {
		return Config.Actor.SAVE_TEXTURE_SIZE;
	}

	private ExploreActorScene mScene = null;
}
