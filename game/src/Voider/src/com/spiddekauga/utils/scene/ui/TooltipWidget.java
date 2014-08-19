package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;

/**
 * Widget for displaying tooltips in editors
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TooltipWidget extends WidgetGroup {
	/**
	 * Constructor which sets the youtube image
	 * @param tooltipImage a drawable to display to most left
	 * @param youtubeImage a drawable to display to the right of the youtube link text
	 * @param labelStyle style for labels
	 * @param padding padding between tooltips
	 */
	public TooltipWidget(Drawable tooltipImage, Drawable youtubeImage, LabelStyle labelStyle, float padding) {
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
		mTable.add().setPadding(0).setFillWidth(true);


		// Youtube
		mYoutubeLabel = new Label("", labelStyle);
		mYoutubeLabel.addListener(mUrlListener);
		mTable.add(mYoutubeLabel);

		mYoutubeImage.setDrawable(youtubeImage);
		mYoutubeImage.addListener(mUrlListener);
		mTable.add(mYoutubeImage).setPadRight(0);
		mYoutubeImage.setVisible(false);
	}

	/**
	 * Add a tooltip to the widget
	 * @param actor the actor the tooltip is bound to
	 * @param tooltip the tooltip to display when the actor is hovered, active (depends on
	 *        tooltip settings)
	 */
	public void add(Actor actor, ITooltip tooltip) {
		mTooltips.put(actor, tooltip);
		actor.addListener(mActorListener);
	}

	/**
	 * Add a tooltip for several actors
	 * @param actors the actors the tooltip is bound to
	 * @param tooltip the tooltip to display when the actor is hovered, active (depends on
	 *        tooltip settings)
	 */
	public void add(ArrayList<Actor> actors, ITooltip tooltip) {
		for (Actor actor : actors) {
			mTooltips.put(actor, tooltip);
			actor.addListener(mActorListener);
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
		if (mTemporaryTooltips.size() > 0 && tooltip == mTemporaryTooltips.get(mTemporaryTooltips.size() - 1)) {
			mTemporaryTooltips.clear();
			setLabels(mPermanentTooltips);
		}
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
				if (tooltip.hasHotkey()) {
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
	 * Set youtube link
	 * @param tooltip the tooltip to display the youtube link for
	 */
	private void setYoutubeLink(ITooltip tooltip) {
		mUrlListener.setUrl(tooltip.getYoutubeLink());
		mYoutubeLabel.setText(tooltip.getText());
		mYoutubeImage.setVisible(true);
		mYoutubeImage.invalidateHierarchy();
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
	};

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
			// Pressed
			else if (event instanceof ChangeEvent) {
				Actor actor = event.getTarget();
				if (actor instanceof Button) {
					if (((Button) actor).isChecked()) {
						ITooltip tooltip = getTooltipFromEvent(event);
						handlePressed(tooltip);
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
	/** Temporary tooltips */
	private ArrayList<ITooltip> mTemporaryTooltips = new ArrayList<>();
	/** Permanent tooltips */
	private ArrayList<ITooltip> mPermanentTooltips = new ArrayList<>();
	/** The tooltip labels */
	private ArrayList<Label> mTooltipLabels = new ArrayList<>();
	/** Tooltip spacing labels, i.e. —> */
	private ArrayList<Label> mTooltipSpacing = new ArrayList<>();
	/** Youtube Label */
	private Label mYoutubeLabel = null;
	/** Youtube image button */
	private Image mYoutubeImage = new Image();
	/** Tooltip image button */
	private Image mTooltipImage = new Image();
	/** Url listener */
	private GotoUrlListener mUrlListener = new GotoUrlListener();

	/**
	 * Listens to actors and opens the specified urls when pressed.
	 */
	private class GotoUrlListener implements EventListener {
		@Override
		public boolean handle(Event event) {
			// Pressed for buttons
			Actor actor = event.getTarget();
			if (actor instanceof Button) {
				if (event instanceof ChangeEvent) {
					if (((Button) actor).isChecked()) {
						openUrl();
					}
				}
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

		/**
		 * Opens the current url
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

		/** Current link to open */
		private String mUrl = "";
	}

	/**
	 * Interface for tooltips. Tip! Use this as an interface for enumerations for all
	 * known tooltips
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
		 * @return true if the tooltip should only display the youtube link, i.e. no hover
		 *         or permanent state
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
	}

	/**
	 * Custom tooltips
	 */
	public static class CustomTooltip implements ITooltip {
		/**
		 * /** Constructs a costum tooltip
		 * @param text tooltip text to display
		 */
		public CustomTooltip(String text) {
			mText = text;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube tutorial
		 */
		public CustomTooltip(String text, String youtubeLink) {
			mText = text;
			mYoutubeLink = youtubeLink;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube tutorial
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
		 * @param permanent set to true if the tooltip should stay after being clicked,
		 *        i.e. not only hover
		 */
		public CustomTooltip(String text, String youtubeLink, ITooltip parent, boolean permanent) {
			mText = text;
			mYoutubeLink = youtubeLink;
			mPermanent = permanent;
			mParent = parent;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube tutorial
		 * @param permanent true if permanent
		 */
		public CustomTooltip(String text, String youtubeLink, boolean permanent) {
			mText = text;
			mYoutubeLink = youtubeLink;
			mPermanent = permanent;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube tutorial, may be null
		 * @param parent parent tooltip. Set to null if this is a root tooltip
		 * @param permanent set to true if the tooltip should stay after being clicked,
		 *        i.e. not only hover
		 * @param hotkey a hotkey for the tooltip, may be null @param youtubeLink link to
		 *        youtube tutorial, may be null
		 * @param youtubeOnly set to true to only show the youtube link and no hover
		 *        messages.
		 */
		public CustomTooltip(String text, String youtubeLink, ITooltip parent, boolean permanent, String hotkey, boolean youtubeOnly) {
			mText = text;
			mPermanent = permanent;
			mParent = parent;
			mHotkey = hotkey;
			mYoutubeLink = youtubeLink;
			mYoutubeOnly = youtubeOnly;
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
			return mPermanent;
		}

		@Override
		public ITooltip getParent() {
			return mParent;
		}

		@Override
		public boolean hasYoutubeLink() {
			return mYoutubeLink != null;
		}

		@Override
		public boolean hasHotkey() {
			return mHotkey != null;
		}

		/** Text for the tooltip */
		private String mText;
		/** True if tooltip is permanent, will stay after pressed button */
		private boolean mPermanent;
		/** Hotkey for tooltip */
		private String mHotkey = null;
		/** YouTube link */
		private String mYoutubeLink = null;
		/** If the tooltip is YouTube-only */
		private boolean mYoutubeOnly = false;
		/** Parent tooltip */
		private ITooltip mParent;

	}

}
