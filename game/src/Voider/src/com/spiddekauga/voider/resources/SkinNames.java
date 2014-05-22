package com.spiddekauga.voider.resources;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.utils.scene.ui.AnimationWidget.AnimationWidgetStyle;
import com.spiddekauga.utils.scene.ui.RatingWidget.RatingWidgetStyle;
import com.spiddekauga.voider.repo.InternalNames;
import com.spiddekauga.voider.repo.ResourceCacheFacade;


/**
 * All skin names
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SkinNames {
	/**
	 * Editor icon names
	 */
	public enum EditorIcons implements ISkinNames {
		/** Add a corner between two corners in a shape, or move an existing corner */
		ADD_MOVE_CORNER,
		/** Enemy shoot in one specific direction */
		AIM_DIRECTION,
		/**
		 * The enemy will shoot in front of the player (i.e. where the player will be if
		 * s/he continues to move in the same direction)
		 */
		AIM_IN_FRONT_PLAYER,
		/** The enemy will shoot in its moving direction */
		AIM_MOVEMENT,
		/**
		 * The enemy will shoot on the player, or rather in the direction where the player
		 * is currently located
		 */
		AIM_ON_PLAYER,
		/** The enemy will shoot in a circle (or in a straight line) */
		AIM_ROTATE,
		/** Go to the bullet editor */
		BULLET_EDITOR,
		/** Used in editor selection menu */
		BULLET_EDITOR_BIG,
		/** When the bullet editor is selected, i.e. we're in the bullet editor */
		BULLET_EDITOR_SELECTED,
		/**
		 * Opens a dialog where the player will be able to select which bullet the enemy
		 * should use
		 */
		BULLET_SELECT,
		/** Go to the campaign editor */
		CAMPAIGN_EDITOR,
		/** Used in editor selection menu */
		CAMPAIGN_EDITOR_BIG,
		/** When the campaign editor is selected, i.e. we're in the campaign editor */
		CAMPAIGN_EDITOR_SELECTED,
		/** Deselects all selected actors, maybe does something else? */
		CANCEL,
		/** Shape of actor (enemy or bullet) */
		CIRCLE_SHAPE,
		/** Collision options for enemies */
		COLLISION,
		/** Delete all selected actors */
		DELETE,
		/** Appends to the custom draw shape. Only enemy and bullet */
		DRAW_APPEND,
		/** Setting this allows the player to draw their own enemies and bullets */
		DRAW_CUSTOM_SHAPE,
		/** Add or remove a part to or from the shape */
		DRAW_ERASE,
		/** Duplicates the current actor or level */
		DUPLICATE,
		/** Add an enemy to the level */
		ENEMY_ADD,
		/** Go to the enemy editor */
		ENEMY_EDITOR,
		/** Used in editor selection menu */
		ENEMY_EDITOR_BIG,
		/** When the enemy editor is selected, i.e. we're in the enemy editor */
		ENEMY_EDITOR_SELECTED,
		/** Opens a dialog where the player will be able to select which enemy to add */
		ENEMY_SELECT,
		/** Set activate trigger for enemies. I.e. binds the enemy to a trigger */
		ENEMY_SET_ACTIVATE_TRIGGER,
		/** Set deactivate trigger for enemies. I.e. binds the enemy to a trigger */
		ENEMY_SET_DEACTIVATE_TRIGGER,
		/**
		 * When checked it shows which enemies will be spawned when testing the level from
		 * the current position
		 */
		ENEMY_SPAWN_HIGHLIGHT,
		/** Grid button for turning it on/off */
		GRID,
		/** Make the grid to be rendered above all other resources */
		GRID_ABOVE,
		/** Information or options for the current level or actor */
		INFO,
		/** Go to the level editor */
		LEVEL_EDITOR,
		/** Used in editor selection menu */
		LEVEL_EDITOR_BIG,
		/** When the level editor is selected, i.e. we're in the level editor */
		LEVEL_EDITOR_SELECTED,
		/** Loads another resource */
		LOAD,
		/** Move the selected actors */
		MOVE,
		/** Movement options for enemies */
		MOVEMENT,
		/** Creates a new resource */
		NEW,
		/** Turn something off (e.g. enemy weapons or turn movement) */
		OFF,
		/** Turns something on (e.g. enemy weapons or turn movement) */
		ON,
		/** Tool to pan the screen */
		PAN,
		/** Add or continue on a path in the level (can move corners too) */
		PATH_ADD,
		/**
		 * How enemies should move in the path. Back and forth means once it reached the
		 * end it will move backwards on the path
		 */
		PATH_BACK_AND_FORTH,
		/**
		 * How enemies should move in the path. Loop means once the enemy reaches the end
		 * it will go directly (i.e. not along the path) to the start and go through the
		 * path again and again...
		 */
		PATH_LOOP,
		/**
		 * How enemies should move in the path ONCE means just once. Then it will just
		 * continue in a straight line when it reaches the end of the path
		 */
		PATH_ONCE,
		/** Remove corner from a path */
		PATH_REMOVE_CORNER,
		/** Add a pickup to the level */
		PICKUP_ADD,
		/** Opens a dialog where the player will be able to select which pickup to add */
		PICKUP_SELECT,
		/** Publishes a resource */
		PUBLISH,
		/** Shape of actor (enemy or bullet) */
		RECTANGLE_SHAPE,
		/** Redo */
		REDO,
		/** Removes a corner from an actor shape */
		REMOVE_CORNER,
		/** Resets the center of the shape (only visible when actor shape is set to draw) */
		RESET_CENTER,
		/** Test run the current level */
		RUN,
		/** Save the resource */
		SAVE,
		/** Screen shot */
		SCREENSHOT,
		/** Select one or several actors */
		SELECT,
		/** Set the center of the shape (only visible when actor shape is set to draw) */
		SET_CENTER,
		/** As {@link #ADD_MOVE_CORNER} but for terrain */
		TERRAIN_ADD_MOVE_CORNER,
		/** As {@link #DRAW_APPEND} but for terrain */
		TERRAIN_DRAW_APPEND,
		/** As {@link #DRAW_ERASE} but for terrain */
		TERRAIN_DRAW_ERASE,
		/** As {@link #REMOVE_CORNER} but for terrain */
		TERRAIN_REMOVE_CORNER,
		/** Shape of actor (enemy or bullet) */
		TRIANGLE_SHAPE,
		/** Add a trigger to the level */
		TRIGGER_ADD,
		/** Undo */
		UNDO,
		/** All visual options for enemies and bullets */
		VISUALS,
		/** Weapon options for enemies and bullets */
		WEAPON,

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 */
		private EditorIcons() {
			mName = super.toString().toLowerCase();
		}

		@Override
		public Class<?> getClassType() {
			return ImageButtonStyle.class;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_EDITOR;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
	}

	/**
	 * Editor tooltips
	 */
	public enum EditorTooltips {

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 */
		private EditorTooltips() {
			mName = super.toString().toLowerCase();
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
	}

	/**
	 * Editor variables
	 */
	public enum EditorVars implements ISkinNames {
		/** Enemy will be activated when test running from here, line color */
		ENEMY_ACTIVATE_ON_TEST_RUN_OUTLINE_COLOR(Color.class),
		/** Line width of outline color when the enemy will be activated */
		ENEMY_ACTIVATE_ON_TEST_RUN_OUTLINE_WIDTH(Float.class),
		/** Color of line between enemy and activate trigger */
		ENEMY_ACTIVATE_TRIGGER_LINE_COLOR(Color.class),
		/** Color of line between enemy and deactivate trigger */
		ENEMY_DEACTIVATE_TRIGGER_LINE_COLOR(Color.class),
		/** Grid color */
		GRID_COLOR(Color.class),
		/** Grid milestone color */
		GRID_MILESTONE_COLOR(Color.class),
		/** Color above and below the level in the editor */
		LEVEL_ABOVE_BELOW_COLOR(Color.class),
		/** Padding between editor menu and tools */
		PADDING_BETWEEN_BAR_AND_TOOLS(Float.class),
		/** Width of text fields containing only numbers */
		TEXT_FIELD_NUMBER_WIDTH(Float.class),
		/** Wait window fade in time */
		WAIT_WINDOW_FADE_IN(Float.class),
		/** Wait window fade out time */
		WAIT_WINDOW_FADE_OUT(Float.class),
		/** Background color for various widgets */
		WIDGET_BACKGROUND_COLOR(Color.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private EditorVars(
				Class<?> type) {
			mName = super.toString().toLowerCase();
			mType = type;
		}

		@Override
		public Class<?> getClassType() {
			return mType;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_EDITOR;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
		/** The type of the class */
		private Class<?> mType;
	}

	/**
	 * Game names
	 */
	public enum Game implements ISkinNames {
		/** Health bar for the game */
		HEALTH_BAR(SliderStyle.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type class type
		 */
		private Game(
				Class<?> type) {
			mName = super.toString().toLowerCase();
			mType = type;
		}

		@Override
		public Class<?> getClassType() {
			return mType;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_GAME;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
		/** The class type */
		private Class<?> mType;
	}

	/**
	 * General variables
	 */
	public enum GeneralVars implements ISkinNames {
		/** Upper and lower bar height */
		BAR_UPPER_LOWER_HEIGHT(Float.class),
		/** Maximum text width for select def scenes */
		LOAD_ACTOR_SIZE_MAX(Float.class),
		/** Padding after label */
		PADDING_AFTER_LABEL(Float.class),
		/** Default padding for rows and cells */
		PADDING_DEFAULT(Float.class),
		/** Separator padding */
		PADDING_SEPARATOR(Float.class),
		/** Left and right window padding */
		@Deprecated
		PADDING_WINDOW_LEFT_RIGHT(Float.class),
		/** Outer padding of panels */
		PADDING_OUTER(Float.class),
		/** Inside padding of panels */
		PADDING_INNER(Float.class),
		/** Padding between radio or checkbox buttons */
		PADDING_CHECKBOX(Float.class),
		/** Background colors for widgets */
		WIDGET_BACKGROUND_COLOR(Color.class),
		/** Tag widget in explore and loading screen */
		TAG_BAR_WIDTH(Float.class),
		/** Stardard icon and thus row height */
		ICON_ROW_HEIGHT(Float.class),
		/** Second of fade in for the wait window */
		WAIT_WINDOW_FADE_IN(Float.class),
		/** Seconds of fade out for the wait window */
		WAIT_WINDOW_FADE_OUT(Float.class),
		/** Width of text fields with numbers in them */
		TEXT_FIELD_NUMBER_WIDTH(Float.class),
		/** Width of right panel */
		RIGHT_PANEL_WIDTH(Float.class),
		/** Width of sliders */
		SLIDER_WIDTH(Float.class),
		/** Width of text/labels before sliders */
		SLIDER_LABEL_WIDTH(Float.class),
		/** Dark text */
		TEXT_DARK(Color.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private GeneralVars(
				Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private GeneralVars(
				Class<?> type, String jsonName) {
			mType = type;
			mName = jsonName;
		}

		@Override
		public Class<?> getClassType() {
			return mType;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_GENERAL;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
		/** Class type */
		private Class<?> mType;
	}

	/**
	 * General images
	 */
	public enum GeneralImages implements IImageNames {
		/** Date */
		DATE,
		/** Edited */
		EDIT,
		/** Player */
		PLAYER,
		/** Number of plays */
		PLAYS,
		/** Star */
		STAR,
		/** Tag */
		TAG,
		/** Like */
		LIKE,
		/** Screenshot placeholder for levels */
		SCREENSHOT_PLACEHOLDER,

		;

		/**
		 * Used default JSON name for enum
		 */
		private GeneralImages() {
			mName = name().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param jsonName name in the json-file
		 */
		private GeneralImages(
				String jsonName) {
			mName = jsonName;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_GENERAL;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
	}

	/**
	 * General UI elements
	 */
	public enum General implements ISkinNames {
		/** Wait animation texture */
		ANIMATION_WAIT(AnimationWidgetStyle.class, "wait"),
		/** Check box that uses check boxes */
		CHECK_BOX_DEFAULT(CheckBoxStyle.class, "default"),
		/** Check box that uses the radio button style */
		CHECK_BOX_RADIO(CheckBoxStyle.class, "radio"),
		/** Create button */
		CREATE(ImageButtonStyle.class),
		/** Explore button */
		EXPLORE(ImageButtonStyle.class),
		/** Image button default */
		IMAGE_BUTTON_DEFAULT(ImageButtonStyle.class, "default"),
		/** Stub image button */
		IMAGE_BUTTON_STUB(ImageButtonStyle.class, "stub"),
		/** Stub togglable image button */
		IMAGE_BUTTON_STUB_TOGGLE(ImageButtonStyle.class, "stub_toggle"),
		/** Image button toggle */
		IMAGE_BUTTON_TOGGLE(ImageButtonStyle.class, "toggle"),
		/** Info button on front screen */
		INFO(ImageButtonStyle.class),
		/** Default label */
		LABEL_DEFAULT(LabelStyle.class, "default"),
		/** Error messages style */
		LABEL_ERROR(LabelStyle.class, "error"),
		/** Label for highlights */
		LABEL_HIGHLIGHT(LabelStyle.class, "highlight"),
		/** Success messages */
		LABEL_SUCCESS(LabelStyle.class, "success"),
		/** Panel section label style */
		LABEL_PANEL_SECTION(LabelStyle.class, "panel_section"),
		/** List default */
		LIST_DEFAULT(ListStyle.class, "default"),
		/** Logout button */
		LOGOUT(ImageButtonStyle.class),
		/** Options button on front screen */
		OPTIONS(ImageButtonStyle.class),
		/** Play button */
		PLAY(ImageButtonStyle.class),
		/** Rating widget default */
		RATING_DEFAULT(RatingWidgetStyle.class, "default"),
		/** Scroll pane default, no background */
		SCROLL_PANE_DEFAULT(ScrollPaneStyle.class, "default"),
		/** Scroll pane with background */
		SCROLL_PANE_WINDOW_BACKGROUND(ScrollPaneStyle.class, "background"),
		/** Select box default */
		SELECT_BOX_DEFAULT(SelectBoxStyle.class, "default"),
		/** Slider default */
		SLIDER_DEFAULT(SliderStyle.class, "default"),
		/** Loading bar slider */
		SLIDER_LOADING_BAR(SliderStyle.class, "loading_bar"),
		/** Stub image */
		STUB(null),
		/** Text button default style */
		TEXT_BUTTON_PRESS(TextButtonStyle.class, "default"),
		/** Text button that always is selected */
		TEXT_BUTTON_SELECTED(TextButtonStyle.class, "selected"),
		/** Text button that can toggle */
		TEXT_BUTTON_TOGGLE(TextButtonStyle.class, "toggle"),
		/** Flat text button */
		TEXT_BUTTON_FLAT_PRESS(TextButtonStyle.class, "flat_press"),
		/** Flat toggleable text button */
		TEXT_BUTTON_FLAT_TOGGLE(TextButtonStyle.class, "flat_toggle"),
		/** Text field default */
		TEXT_FIELD_DEFAULT(TextFieldStyle.class, "default"),
		/** Window default style without title */
		WINDOW_DEFAULT(WindowStyle.class, "default"),
		/** Modal window with no title */
		WINDOW_MODAL(WindowStyle.class, "modal"),
		/** Modal window with title */
		WINDOW_MODAL_TITLE(WindowStyle.class, "modal_title"),
		/** Window no title */
		WINDOW_NO_TITLE(WindowStyle.class, "default"),
		/** Window with title */
		WINDOW_TITLE(WindowStyle.class, "title"),
		/** Color tab */
		COLOR(ImageButtonStyle.class),
		/** Game continue */
		GAME_CONTINUE(ImageButtonStyle.class),
		/** Replay the level */
		REPLAY(ImageButtonStyle.class),
		/** Spiddekauga info big */
		SPIDDEKAUGA_INFO(ImageButtonStyle.class, "info_big"),
		/** Login big */
		LOGIN_BIG(ImageButtonStyle.class),
		/** Player big */
		PLAYER_BIG(ImageButtonStyle.class),
		/** Settings big */
		SETTINGS_BIG(ImageButtonStyle.class),
		/** Comments tab */
		COMMENTS(ImageButtonStyle.class),
		/** Image tab */
		IMAGES(ImageButtonStyle.class),
		/** Overview tab */
		OVERVIEW(ImageButtonStyle.class),
		/** Tags tab */
		TAGS(ImageButtonStyle.class),
		/** Tag button */
		TAG(ImageButtonStyle.class),
		/** Like button */
		LIKE(ImageButtonStyle.class),
		/** Featured big (explore) */
		FEATURED_BIG(ImageButtonStyle.class),
		/** Search (explore) */
		SEARCH_BIG(ImageButtonStyle.class),
		/** Browse big (explore) */
		BROWSE_BIG(ImageButtonStyle.class),
		/** Featured (explore) */
		FEATURED(ImageButtonStyle.class),
		/** Search (explore) */
		SEARCH(ImageButtonStyle.class),
		/** Browse (explore) */
		BROWSE(ImageButtonStyle.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private General(
				Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private General(
				Class<?> type, String jsonName) {
			mType = type;
			mName = jsonName;
		}

		@Override
		public Class<?> getClassType() {
			return mType;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_GENERAL;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
		/** Class type */
		private Class<?> mType;
	}


	/**
	 * Interface for skin names
	 */
	public interface ISkinNames {
		/**
		 * @return class type
		 */
		Class<?> getClassType();

		/**
		 * @return skin name
		 */
		InternalNames getSkinName();
	}

	/**
	 * Interface for image names
	 */
	public interface IImageNames {
		/**
		 * @return skin name
		 */
		InternalNames getSkinName();
	}

	/**
	 * Theme names
	 */
	public enum Theme implements ISkinNames {
		/** Bottom layer of the background */
		BOTTOM_LAYER(Texture.class),
		/** Top layer of the background */
		TOP_LAYER(Texture.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private Theme(
				Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private Theme(
				Class<?> type, String jsonName) {
			mType = type;
			mName = jsonName;
		}

		@Override
		public Class<?> getClassType() {
			return mType;
		}

		@Override
		public InternalNames getSkinName() {
			return null;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
		/** Class type */
		private Class<?> mType;
	}

	/**
	 * Method for getting drawables
	 * @param imageName name of the drawable to get
	 * @return drawable image
	 */
	public static Drawable getDrawable(IImageNames imageName) {
		Skin skin = ResourceCacheFacade.get(imageName.getSkinName());
		return skin.getDrawable(imageName.toString());
	}

	/**
	 * Method for getting a skin name from the loaded resources
	 * @param <ResourceType> The resource type to return
	 * @param skinName the skin name to get the resource from
	 * @return the resource that was fetched :)
	 */
	@SuppressWarnings("unchecked")
	public static <ResourceType> ResourceType getResource(ISkinNames skinName) {
		Skin skin = ResourceCacheFacade.get(skinName.getSkinName());
		return (ResourceType) skin.get(skinName.toString(), skinName.getClassType());
	}
}
