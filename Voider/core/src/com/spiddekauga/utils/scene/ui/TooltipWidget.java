package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.VisibilityChangeListener.VisibilityChangeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Widget for displaying tooltips in editors
 */
public class TooltipWidget extends WidgetGroup {
/**
 * Constructor which sets the youtube image
 * @param tooltipImage a drawable to display to most left
 * @param youtubeImage an image button to display to the right of the YouTube link text
 * @param labelStyle style for labels
 * @param padding padding between tooltips
 */
public TooltipWidget(Drawable tooltipImage, ImageButtonStyle youtubeImage, LabelStyle labelStyle, float padding) {
	addActor(mTable);
	mTable.setAlignTable(Horizontal.LEFT, Vertical.BOTTOM);
	mTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
	mTable.setKeepWidth(true);
	mTable.setWidth(Gdx.graphics.getWidth());

	// Populate table
	mTable.setPaddingCellDefault(0, padding, 0, 0);
	mTable.row().setFillWidth(true);

	// Tooltip icon
	mTooltipImage.setDrawable(tooltipImage);
	mTable.add(mTooltipImage);

	// Three levels of tooltips
	// 1
	Label label = new Label("", labelStyle);
	mTooltipLabels.add(label);
	mTable.add(label);

	// Pad
	label = new Label("->", labelStyle);
	label.setVisible(false);
	mTooltipSpacing.add(label);
	mTable.add(label);

	// 2
	label = new Label("", labelStyle);
	mTooltipLabels.add(label);
	mTable.add(label);

	// Pad
	label = new Label("->", labelStyle);
	label.setVisible(false);
	mTooltipSpacing.add(label);
	mTable.add(label);

	// 3
	label = new Label("", labelStyle);
	mTooltipLabels.add(label);
	mTable.add(label);


	// Fill between
	mTable.add().setPad(0).setFillWidth(true);


	// Youtube
	mYoutubeLabel = new Label("", labelStyle);
	mYoutubeLabel.addListener(mUrlListener);
	mTable.add(mYoutubeLabel);

	mYoutubeButton = new ImageButton(youtubeImage);
	mYoutubeButton.addListener(mUrlListener);
	mTable.add(mYoutubeButton).setPadRight(0);
	mYoutubeButton.setVisible(false);
}

/**
 * Add a tooltip to the widget
 * @param actor the actor the tooltip is bound to
 * @param tooltip the tooltip to display when the actor is hovered, active (depends on tooltip
 * settings)
 */
public void add(Actor actor, ITooltip tooltip) {
	mTooltips.put(actor, tooltip);
	mActors.put(tooltip, actor);
	actor.addListener(mActorListener);

	if (tooltip.isPermanent()) {
		ArrayList<Actor> permanents = mSortedPermanents.get(tooltip.getLevel());

		if (permanents == null) {
			permanents = new ArrayList<>();
			mSortedPermanents.put(tooltip.getLevel(), permanents);
		}

		permanents.add(actor);
	}
}

/**
 * Add a tooltip for several actors
 * @param actors the actors the tooltip is bound to
 * @param tooltip the tooltip to display when the actor is hovered, active (depends on tooltip
 * settings)
 */
public void add(ArrayList<Actor> actors, ITooltip tooltip) {
	for (Actor actor : actors) {
		add(actor, tooltip);
	}
}

/**
 * Get tooltip of the specified event
 * @param event get the tooltip from the specified event
 * @return tooltip from the specified event. null if not found
 */
private ITooltip getTooltipFromEvent(Event event) {
	Actor actor = event.getTarget();
	do {
		ITooltip tooltip = mTooltips.get(actor);
		if (tooltip != null) {
			return tooltip;
		}

		actor = actor.getParent();

	} while (actor != null);

	return null;
}

/**
 * Handle enter event for a tooltip
 * @param tooltip the tooltip that should be displayed when entered
 */
private void handleEnter(ITooltip tooltip) {
	if (!tooltip.isYoutubeOnly()) {
		setTemporaryTooltip(tooltip);
	}

	if (tooltip.hasYoutubeLink()) {
		setYoutubeLink(tooltip);
	}
}

/**
 * Handle exit event for a tooltip
 * @param tooltip the tooltip that should be hidden again when exited
 */
private void handleExit(ITooltip tooltip) {
	if (!tooltip.isYoutubeOnly()) {
		clearTemporaryTooltip(tooltip);
	}
}

/**
 * Handle pressed event for a tooltip
 * @param tooltip the tooltip that might be activated (if it can be permanent)
 */
private void handlePressed(ITooltip tooltip) {
	if (tooltip.isPermanent()) {
		setPermanentTooltip(tooltip);
	}
}

/**
 * Handle invisible event for the current permanent tooltip
 */
private void handleInvisible() {
	// Remove current, get parent
	mPermanentTooltips.remove(mPermanentTooltips.size() - 1);
	ITooltip parentTooltip = null;

	if (!mPermanentTooltips.isEmpty()) {
		parentTooltip = mPermanentTooltips.get(mPermanentTooltips.size() - 1);
	}

	// Parent available
	if (parentTooltip != null) {
		Actor actor = mActors.get(parentTooltip);

		// Visible -> Set labels
		if (actor != null && actor.isVisible()) {
			setLabels(mPermanentTooltips);
		}
		// Invisible -> Recursive
		else {
			handleInvisible();
		}
	}
	// Search all permanents for a visible and selected one
	else {
		Actor foundVisibleCheckedActor = null;
		Iterator<Entry<Integer, ArrayList<Actor>>> permanentIt = mSortedPermanents.entrySet().iterator();
		while (permanentIt.hasNext() && foundVisibleCheckedActor == null) {
			ArrayList<Actor> actors = permanentIt.next().getValue();

			Iterator<Actor> actorIt = actors.iterator();
			while (actorIt.hasNext() && foundVisibleCheckedActor == null) {
				Actor actor = actorIt.next();

				if (actor instanceof Button) {
					if (((Button) actor).isChecked() && actor.isVisible()) {
						foundVisibleCheckedActor = actor;
					}
				}
			}
		}

		// Set new permanent tooltip
		if (foundVisibleCheckedActor != null) {
			ITooltip tooltip = mTooltips.get(foundVisibleCheckedActor);
			setPermanentTooltip(tooltip);
		}
	}
}

/**
 * Set a temporary tooltip
 * @param tooltip the tooltip to set as a temporary
 */
private void setTemporaryTooltip(ITooltip tooltip) {
	mTemporaryTooltips.clear();
	addHierarchyToList(tooltip, mTemporaryTooltips);
	setLabels(mTemporaryTooltips);
}

/**
 * Clear a temporary tooltip
 * @param tooltip the temporary tooltip must be this tooltip to work
 */
private void clearTemporaryTooltip(ITooltip tooltip) {
	if (isCurrentTemporaryTooltip(tooltip)) {
		mTemporaryTooltips.clear();
		setLabels(mPermanentTooltips);
	}
}

/**
 * Checks if this is the current temporary tooltip
 * @param tooltip the tooltip to check
 * @return true if the tooltip is the current temporary tooltip
 */
private boolean isCurrentTemporaryTooltip(ITooltip tooltip) {
	return mTemporaryTooltips.size() > 0 && tooltip == mTemporaryTooltips.get(mTemporaryTooltips.size() - 1);
}

/**
 * Checks if this is the current permanent tooltip
 * @param tooltip the tooltip to check
 * @return true if the tooltip is the current permanent tooltip
 */
private boolean isCurrentPermanentTooltip(ITooltip tooltip) {
	return mPermanentTooltips.size() > 0 && tooltip == mPermanentTooltips.get(mPermanentTooltips.size() - 1);
}

/**
 * Set a permanent tooltip. Automatically clears the temporary tooltip.
 * @param tooltip the tooltip to set as a permanent tooltip
 */
private void setPermanentTooltip(ITooltip tooltip) {
	mPermanentTooltips.clear();
	mTemporaryTooltips.clear();

	addHierarchyToList(tooltip, mPermanentTooltips);
	setLabels(mPermanentTooltips);
}

/**
 * Add tooltip hierarchy to the specified tooltip list
 * @param tooltip the tooltip hierarchy to add
 * @param list add hierarchy to this list
 */
private static void addHierarchyToList(ITooltip tooltip, ArrayList<ITooltip> list) {
	ITooltip currentTooltip = tooltip;
	do {
		list.add(0, currentTooltip);
		currentTooltip = currentTooltip.getParent();
	} while (currentTooltip != null);
}

/**
 * Sets the labels correctly depending on the number of tooltips that are visible
 * @param tooltips
 */
private void setLabels(ArrayList<ITooltip> tooltips) {
	for (int i = 0; i < mTooltipLabels.size(); ++i) {
		Label label = mTooltipLabels.get(i);
		String tooltipText = "";
		String hotkey = "";
		if (i < tooltips.size()) {
			ITooltip tooltip = tooltips.get(i);
			tooltipText = tooltip.getText();

			// Set hotkey on Desktop
			if (tooltip.hasHotkey() && Gdx.app.getType() == ApplicationType.Desktop) {
				hotkey = "(" + tooltip.getHotkey() + ") ";
			}
		}
		label.setText(hotkey + tooltipText);
	}

	// Show/hide the correct amount of tooltip spacing —>
	for (int i = 0; i < mTooltipSpacing.size(); ++i) {
		if (i < tooltips.size() - 1) {
			mTooltipSpacing.get(i).setVisible(true);
		} else {
			mTooltipSpacing.get(i).setVisible(false);
		}
	}
}

/**
 * Set YouTube link
 * @param tooltip the tooltip to display the youtube link for
 */
private void setYoutubeLink(ITooltip tooltip) {
	mUrlListener.setUrl(tooltip.getYoutubeLink());
	mYoutubeLabel.setText(tooltip.getText());
	mYoutubeButton.setVisible(true);
	mYoutubeButton.invalidateHierarchy();
}

/**
 * Sets the margin (outside) for this TooltipWidget.
 * @param top margin at the top
 * @param right margin to the right
 * @param bottom margin at the bottom
 * @param left margin to the left
 * @return this TooltipWidget for chaining
 */
public TooltipWidget setMargin(float top, float right, float bottom, float left) {
	mTable.setMargin(top, right, bottom, left);
	return this;
}

/**
 * Set the margin (outside) for this TooltipWidget.
 * @param margin margin to the left, right, top, and bottom
 * @return this TooltipWidget for chaining
 */
public TooltipWidget setMargin(float margin) {
	mTable.setMargin(margin);
	return this;
}

/**
 * @return top margin
 */
public float getMarginTop() {
	return mTable.getMarginTop();
}

/**
 * @return right margin
 */
public float getMarginRight() {
	return mTable.getMarginRight();
}

/**
 * @return bottom margin
 */
public float getMarginBottom() {
	return mTable.getMarginBottom();
}

/**
 * @return left margin
 */
public float getMarginLeft() {
	return mTable.getMarginLeft();
}

@Override
@Deprecated
public void setHeight(float height) {
	throw new IllegalAccessError("Cannot change height of tooltip widget!");
}

;

@Override
public void setWidth(float width) {
	mTable.setWidth(width);
}

@Override
public void layout() {
	mTable.layout();
}

@Override
public float getHeight() {
	return mTable.getHeight();
}

@Override
public float getWidth() {
	return mTable.getWidth();
}

@Override
public float getMinWidth() {
	return mTable.getMinWidth();
}

@Override
public float getMinHeight() {
	return mTable.getMinHeight();
}

@Override
public float getPrefWidth() {
	return mTable.getPrefWidth();
}

@Override
public float getPrefHeight() {
	return mTable.getPrefHeight();
}

@Override
public float getMaxWidth() {
	return mTable.getMaxWidth();
}

@Override
public float getMaxHeight() {
	return mTable.getMaxHeight();
}

/** Listener for actors */
private EventListener mActorListener = new EventListener() {
	@Override
	public boolean handle(Event event) {
		// Hover
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;

			// Enter
			if (inputEvent.getType() == Type.enter) {
				ITooltip tooltip = getTooltipFromEvent(event);
				if (tooltip != null) {
					handleEnter(tooltip);
				}
			}
			// Exit
			else if (inputEvent.getType() == Type.exit) {
				// Only exit if mouse actually left
				Actor actor = event.getTarget();
				if (!SceneUtils.isStagePointInsideActor(actor, inputEvent.getStageX(), inputEvent.getStageY())) {
					ITooltip tooltip = getTooltipFromEvent(event);
					if (tooltip != null) {
						handleExit(tooltip);
					}
				}
			}
		}
		// Pressed or visible/invisible
		else if (event instanceof ChangeEvent) {
			Actor actor = event.getTarget();
			if (actor instanceof Button) {
				// Pressed
				if (((Button) actor).isChecked()) {
					ITooltip tooltip = getTooltipFromEvent(event);
					handlePressed(tooltip);
				}

				// Invisible and current
				if (event instanceof VisibilityChangeEvent && !actor.isVisible()) {
					ITooltip tooltip = getTooltipFromEvent(event);
					if (tooltip.shouldHideWhenHidden() && isCurrentPermanentTooltip(tooltip)) {
						handleInvisible();
					}
				}
			}
		}

		return false;
	}
};


/** Table where the tooltips are located */
private AlignTable mTable = new AlignTable();
/** Tooltips bound to specific actors */
private HashMap<Actor, ITooltip> mTooltips = new HashMap<>();
/** Actors bound to the tooltips */
private HashMap<ITooltip, Actor> mActors = new HashMap<>();
/** All permanents sorted with by levels, higher level first */
private SortedMap<Integer, ArrayList<Actor>> mSortedPermanents = new TreeMap<>(new Comparator<Integer>() {
	@Override
	public int compare(Integer o1, Integer o2) {
		return o1.intValue() - o2.intValue();
	}
});
/** Temporary tooltips */
private ArrayList<ITooltip> mTemporaryTooltips = new ArrayList<>();
/** Permanent tooltips */
private ArrayList<ITooltip> mPermanentTooltips = new ArrayList<>();
/** The tooltip labels */
private ArrayList<Label> mTooltipLabels = new ArrayList<>();
/** Tooltip spacing labels, i.e. —> */
private ArrayList<Label> mTooltipSpacing = new ArrayList<>();
/** YouTube Label */
private Label mYoutubeLabel = null;
/** YouTube image button */
private ImageButton mYoutubeButton = null;
/** Tooltip image button */
private Image mTooltipImage = new Image();
/** URL listener */
private GotoUrlListener mUrlListener = new GotoUrlListener();



/**
 * Interface for tooltips. Tip! Use this as an interface for enumerations for all known tooltips
 */
public static interface ITooltip {
	/**
	 * @return text of the tooltip
	 */
	String getText();

	/**
	 * @return hotkey of the tooltip. null if none exist
	 */
	String getHotkey();

	/**
	 * @return youtube link to tutorial. null if none exist
	 */
	String getYoutubeLink();

	/**
	 * @return true if the tooltip should only display the youtube link, i.e. no hover or permanent
	 * state
	 */
	boolean isYoutubeOnly();

	/**
	 * @return true if the tooltip is permanent (i.e. will stay active once pressed)
	 */
	boolean isPermanent();

	/**
	 * @return true if the tooltip has a youtube link
	 */
	boolean hasYoutubeLink();

	/**
	 * @return true if the tooltip has a hotkey
	 */
	boolean hasHotkey();

	/**
	 * @return parent tooltip. If null this is a root tooltip
	 */
	ITooltip getParent();

	/**
	 * @return permanent level for the tooltip. Higher value is higher priority. Only valid if the
	 * tooltip is permanent.
	 */
	int getLevel();

	/**
	 * @return true if the tooltip should be hide when the tooltips is hidden. Only valid for
	 * permanents
	 */
	boolean shouldHideWhenHidden();
}

/**
 * Listens to actors and opens the specified URL when pressed.
 */
private class GotoUrlListener extends ButtonListener {
	/** Current link to open */
	private String mUrl = "";

	@Override
	public boolean handle(Event event) {
		// Pressed for buttons
		Actor actor = event.getListenerActor();
		if (actor instanceof Button) {
			super.handle(event);
		}
		// Not button
		else {
			if (event instanceof InputEvent) {
				if (((InputEvent) event).getType() == Type.touchDown) {
					openUrl();
				}
			}
		}
		return false;
	}

	@Override
	protected void onPressed(Button button) {
		openUrl();
	}

	/**
	 * Opens the current URL
	 */
	private void openUrl() {
		if (mUrl != null && !mUrl.isEmpty()) {
			Gdx.app.getNet().openURI(mUrl);
		}
	}

	/**
	 * Set the url to open when the actors are pressed
	 * @param url the url to go to when any of the actors are pressed
	 */
	public void setUrl(String url) {
		mUrl = url;
	}
}/**
 * Custom tooltips
 */
public static class CustomTooltip implements ITooltip {
	/** Text for the tooltip */
	private String mText;
	/** Hotkey for tooltip */
	private String mHotkey = null;
	/** YouTube link */
	private String mYoutubeLink = null;
	/** If the tooltip is YouTube-only */
	private boolean mYoutubeOnly = false;
	/** Parent tooltip */
	private ITooltip mParent;
	/** Level of the tooltip */
	private Integer mPermanentLevel = null;
	/** Should hide when hidden */
	private boolean mHideWhenHidden = true;

	/**
	 * Constructs a custom tooltip. Not a permanent.
	 * @param text tooltip text to display
	 */
	public CustomTooltip(String text) {
		mText = text;
	}

	/**
	 * Constructs a custom tooltip. Not a permanent.
	 * @param text tooltip text to display
	 * @param youtubeLink link to YouTube tutorial
	 */
	public CustomTooltip(String text, String youtubeLink) {
		mText = text;
		mYoutubeLink = youtubeLink;
	}

	/**
	 * Constructs a custom tooltip. Not a permanent.
	 * @param text tooltip text to display
	 * @param youtubeLink link to YouTube tutorial
	 * @param parent parent tooltip. Set to null if this is a root tooltip
	 */
	public CustomTooltip(String text, String youtubeLink, ITooltip parent) {
		mText = text;
		mYoutubeLink = youtubeLink;
		mParent = parent;
	}

	/**
	 * Constructs a costum tooltip
	 * @param text tooltip text to display
	 * @param youtubeLink link to youtube totorial
	 * @param parent parent tooltip. Set to null if this is a root tooltip
	 * @param permanentLevel set to the level of priority the permanent should have. Set to null if
	 * you don't want this tooltip to be a permanent
	 */
	public CustomTooltip(String text, String youtubeLink, ITooltip parent, Integer permanentLevel) {
		mText = text;
		mYoutubeLink = youtubeLink;
		mPermanentLevel = permanentLevel;
		mParent = parent;
	}

	/**
	 * Constructs a custom tooltip
	 * @param text tooltip text to display
	 * @param youtubeLink link to YouTube tutorial
	 * @param permanentLevel set to the level of priority the permanent should have. Set to null if
	 * you don't want this tooltip to be a permanent
	 */
	public CustomTooltip(String text, String youtubeLink, Integer permanentLevel) {
		mText = text;
		mYoutubeLink = youtubeLink;
		mPermanentLevel = permanentLevel;
	}

	/**
	 * Constructs a custom tooltip
	 * @param text tooltip text to display
	 * @param youtubeLink link to YouTube tutorial, may be null
	 * @param parent parent tooltip. Set to null if this is a root tooltip
	 * @param permanentLevel set to the level of priority the permanent should have. Set to null if
	 * you don't want this tooltip to be a permanent
	 * @param hotkey a hotkey for the tooltip, may be null @param youtubeLink link to YouTube
	 * tutorial, may be null
	 * @param youtubeOnly set to true to only show the youtube link and no hover messages.
	 * @param hideWhenHidden true (default) to hide the tooltip if the actor is hidden. If false the
	 * tooltip will be shown even though the actor is hidden.
	 */
	public CustomTooltip(String text, String youtubeLink, ITooltip parent, Integer permanentLevel, String hotkey, boolean youtubeOnly,
						 boolean hideWhenHidden) {
		mText = text;
		mPermanentLevel = permanentLevel;
		mParent = parent;
		mHotkey = hotkey;
		mYoutubeLink = youtubeLink;
		mYoutubeOnly = youtubeOnly;
		mHideWhenHidden = hideWhenHidden;
	}

	/**
	 * Sets hide when hidden
	 * @param hideWhenHidden true (default) to hide the tooltip if the actor is hidden. If false the
	 * tooltip will be shown even though the actor is hidden.
	 */
	public void setHideWhenHidden(boolean hideWhenHidden) {
		mHideWhenHidden = hideWhenHidden;
	}

	@Override
	public String getText() {
		return mText;
	}

	@Override
	public String getHotkey() {
		return mHotkey;
	}

	@Override
	public String getYoutubeLink() {
		return mYoutubeLink;
	}

	@Override
	public boolean isYoutubeOnly() {
		return mYoutubeOnly;
	}

	@Override
	public boolean isPermanent() {
		return mPermanentLevel != null;
	}

	@Override
	public boolean hasYoutubeLink() {
		return mYoutubeLink != null;
	}

	@Override
	public boolean hasHotkey() {
		return mHotkey != null;
	}

	@Override
	public ITooltip getParent() {
		return mParent;
	}

	@Override
	public int getLevel() {
		return mPermanentLevel;
	}

	@Override
	public boolean shouldHideWhenHidden() {
		return mHideWhenHidden;
	}
}

}
