package com.spiddekauga.voider.explore;

import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.resource.DefEntity;

/**
 * Common GUI class for exploring actors. Override this class to make the layout and actions more
 * specific
 */
public class ExploreActorGui extends ExploreGui {
private ExploreActorScene mScene = null;

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

	// Image button
	addImageButtonField(actor, selected, table);

	// Actor name
	addFieldName(actor, table);

	return table;
}

@Override
protected void onFetchMoreContent() {
	// Does nothing
}

@Override
protected float getMaxActorWidth() {
	return Config.Actor.SAVE_TEXTURE_SIZE;
}

/**
 * Sets the actor scene
 */
void setActorScene(ExploreActorScene scene) {
	mScene = scene;
}
}
