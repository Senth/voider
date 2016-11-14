package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.stat.Tags;
import com.spiddekauga.voider.repo.stat.StatLocalRepo;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.ui.UiFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

/**
 * Scene for tagging levels or something else
 */
public class TagScene extends Scene {
/** Local stat repository */
private StatLocalRepo mStatLocalRepo = StatLocalRepo.getInstance();
/** The resource that is being tagged */
private UUID mResourceId;

/**
 * Creates a tag scene for a specific resource
 * @param resourceId id of the resource to tag
 */
public TagScene(UUID resourceId) {
	super(new TagSceneGui());
	mResourceId = resourceId;

	getGui().setScene(this);
	setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);
}

@Override
protected TagSceneGui getGui() {
	return (TagSceneGui) super.getGui();
}

/**
 * Add a tag
 * @param tag the tag that should be added
 */
void setTag(Tags tag) {
	mStatLocalRepo.addTag(mResourceId, tag);
}

/**
 * Continue to next scene
 */
void continueToNextScene() {
	setOutcome(Outcomes.NOT_APPLICAPLE);
}

/**
 * @return random tags to be displayed
 */
ArrayList<Tags> getRandomTags() {
	ArrayList<Tags> randomTags = new ArrayList<>();
	ArrayList<Tags> availableTags = new ArrayList<>();
	Collections.addAll(availableTags, Tags.values());
	Random random = new Random(System.nanoTime());

	for (int i = 0; i < Config.Community.TAGS_TO_DISPLAY; ++i) {
		int randomIndex = random.nextInt(availableTags.size());
		Tags randomTag = availableTags.get(randomIndex);
		availableTags.remove(randomIndex);
		randomTags.add(randomTag);
	}

	return randomTags;
}
}
