package com.spiddekauga.voider.resources;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
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
import com.spiddekauga.utils.ColorArray;
import com.spiddekauga.utils.scene.ui.AnimationWidget.AnimationWidgetStyle;
import com.spiddekauga.utils.scene.ui.RatingWidget.RatingWidgetStyle;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;


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
		/** When the bullet editor is selected, i.e. we're in the bullet editor */
		BULLET_EDITOR_SELECTED,
		/**
		 * Opens a dialog where the player will be able to select which bullet the enemy
		 * should use
		 */
		BULLET_SELECT,
		/** Go to the campaign editor */
		CAMPAIGN_EDITOR,
		/** When the campaign editor is selected, i.e. we're in the campaign editor */
		CAMPAIGN_EDITOR_SELECTED,
		/** Deselects all selected actors, maybe does something else? */
		CANCEL,
		/** Shape of actor (enemy or bullet) */
		CIRCLE_SHAPE,
		/** Collision options for enemies */
		COLLISION,
		/** Color tab */
		COLOR,
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
		/** enemy add tab */
		ENEMY_ADD_TAB,
		/** Button to add another enemy to the list */
		ENEMY_ADD_TO_LIST,
		/** Enemy settings tab */
		ENEMY_INFO,
		/** Go to the enemy editor */
		ENEMY_EDITOR,
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
		/** When the level editor is selected, i.e. we're in the level editor */
		LEVEL_EDITOR_SELECTED,
		/** Loads another resource */
		LOAD,
		/** Move the selected actors */
		MOVE,
		/** Movement options for enemies */
		MOVEMENT,
		/** AI enemy movement */
		MOVEMENT_AI,
		/** Path enemy movement */
		MOVEMENT_PATH,
		/** Stationary enemy */
		MOVEMENT_STATIONARY,
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
		/** Path tab in level editor */
		PATH_TAB,
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
		/** YouTube icon */
		YOUTUBE,
		/** Tool - Zoom in */
		ZOOM_IN,
		/** Tool - Zoom out */
		ZOOM_OUT,
		/** Tool - Reset zoom */
		ZOOM_RESET,

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
	 * Editor images
	 */
	public enum EditorImages implements IImageNames {
		/** YouTube icon */
		YOUTUBE,
		/** Tooltip */
		TOOLTIP,
		/** Enemy AI movement save image */
		MOVEMENT_AI_SAVE,
		/** Enemy path movement save image */
		MOVEMENT_PATH_SAVE,
		/** Enemy stationary save image */
		MOVEMENT_STATIONARY_SAVE,
		/** Back and forth image */
		PATH_BACK_AND_FORTH,
		/** Loop image */
		PATH_LOOP,
		/** Once image */
		PATH_ONCE,

		;

		/**
		 * Used default JSON name for enum
		 */
		private EditorImages() {
			mName = name().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param jsonName name in the json-file
		 */
		private EditorImages(String jsonName) {
			mName = jsonName;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_EDITOR;
		}

		/** skin name of the icon */
		private String mName;
	}

	/**
	 * Editor variables
	 */

	public enum EditorVars implements ISkinNames {
		/** Enemy will be activated when test running from here, line color */
		ENEMY_ACTIVATE_ON_TEST_RUN_COLOR(Color.class),
		/** Line width of outline color when the enemy will be activated */
		ENEMY_ACTIVATE_ON_TEST_RUN_RADIUS(Float.class),
		/** Color of line between enemy and activate trigger */
		ENEMY_ACTIVATE_TRIGGER_LINE_COLOR(Color.class),
		/** Color of line between enemy and deactivate trigger */
		ENEMY_DEACTIVATE_TRIGGER_LINE_COLOR(Color.class),
		/** Width of the outline for selected actors */
		SELECTED_OUTLINE_WIDTH(Float.class),
		/** Color of the selected outline for actors */
		SELECTED_COLOR_ACTOR(Color.class),
		/** Color of the selected utilities */
		SELECTED_COLOR_UTILITY(Color.class),
		/** Grid color */
		GRID_COLOR(Color.class),
		/** Grid milestone color */
		GRID_MILESTONE_COLOR(Color.class),
		/** Color above and below the level in the editor */
		LEVEL_ABOVE_BELOW_COLOR(Color.class),
		/** Background color for various widgets */
		WIDGET_BACKGROUND_COLOR(Color.class),
		/** Top layer speed when selecting theme */
		THEME_TOP_LAYER_SPEED(Float.class),
		/** Top layer speed when selecting theme */
		THEME_BOTTOM_LAYER_SPEED(Float.class),
		/** How much wider the theme button is than the height */
		THEME_DISPLAY_RATIO(Float.class),
		/** Path color */
		PATH_COLOR(Color.class),

		// Actor colors
		/** Color picking values for terrain */
		TERRAIN_COLOR_PICKER(ColorArray.class),
		/** Default terrain color */
		TERRAIN_COLOR_DEFAULT(Color.class),
		/** Color picking values for enemies */
		ENEMY_COLOR_PICKER(ColorArray.class),
		/** Default enemy color */
		ENEMY_COLOR_DEFAULT(Color.class),
		/** Bullet color picker */
		BULLET_COLOR_PICKER(ColorArray.class),
		/** Default bullet color */
		BULLET_COLOR_DEFAULT(Color.class),
		/** Default player color */
		PLAYER_COLOR_DEFAULT(Color.class),

		// Terrain alpha values
		/** Starting alpha value */
		TERRAIN_ALPHA_START(Float.class),
		/** Ending alpha value */
		TERRAIN_ALPHA_END(Float.class),
		/** Default terrain alpha value */
		TERRAIN_ALPHA_DEFAULT(Float.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private EditorVars(Class<?> type) {
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
	 * Game UI
	 */
	public enum Game implements ISkinNames {
		/** Health bar for the game */
		HEALTH_BAR(SliderStyle.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type class type
		 */
		private Game(Class<?> type) {
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
	 * Game Images
	 */
	public enum GameImages implements IImageNames {
		/** Filled life shuttle, to show how many lives the player has */
		LIFE_FILLED("shuttle_lives_yes"),
		/** Empty shuttle life */
		LIFE_EMPTY("shuttle_lives_no"),

		;

		/**
		 * Used default JSON name for enum
		 */
		private GameImages() {
			mName = name().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param jsonName name in the json-file
		 */
		private GameImages(String jsonName) {
			mName = jsonName;
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
	}

	/**
	 * Game Variables
	 */
	public enum GameVars implements ISkinNames {
		/** Speed for the top layer of the level background */
		TOP_LAYER_SPEED(Float.class),
		/** Speed for the bottom layer of the level background */
		BOTTOM_LAYER_SPEED(Float.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private GameVars(Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private GameVars(Class<?> type, String jsonName) {
			mType = type;
			mName = jsonName;
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
		/** Class type */
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
		PADDING_LABEL_AFTER(Float.class),
		/** Padding between buttons */
		PADDING_BUTTONS(Float.class),
		/** Separator padding */
		PADDING_SEPARATOR(Float.class),
		/** Outer padding of panels */
		PADDING_OUTER(Float.class),
		/** Inside padding of panels */
		PADDING_INNER(Float.class),
		/** Padding between radio or checkbox buttons */
		PADDING_CHECKBOX(Float.class),
		/** Padding between the checkbox icon and the text */
		PADDING_CHECKBOX_TEXT(Float.class),
		/** Padding between rows in Explore/Load scenes */
		PADDING_EXPLORE(Float.class),
		/**
		 * Transparent button padding (so text doesn't touch the borders) This is padding
		 * on all sides
		 */
		PADDING_TRANSPARENT_TEXT_BUTTON(Float.class),
		/** Background colors for widgets */
		WIDGET_BACKGROUND_COLOR(Color.class),
		/**
		 * Inner widget background color. I.e. if there is a table inside a widget. E.g.
		 * enemy list in level editor.
		 */
		WIDGET_INNER_BACKGROUND_COLOR(Color.class),
		/** Tag widget in explore and loading screen */
		TAG_BAR_WIDTH(Float.class),
		/** Standard icon and thus row height */
		ROW_HEIGHT(Float.class),
		/** Row height for panel section (smaller text) */
		ROW_HEIGHT_SECTION(Float.class),
		/** Second of fade in for the wait window */
		WAIT_WINDOW_FADE_IN(Float.class),
		/** Seconds of fade out for the wait window */
		WAIT_WINDOW_FADE_OUT(Float.class),
		/** Text area height */
		TEXT_AREA_HEIGHT(Float.class),
		/** Default text button height */
		TEXT_BUTTON_HEIGHT(Float.class),
		/** Default text button width */
		TEXT_BUTTON_WIDTH(Float.class),
		/** Width of text fields with numbers in them */
		TEXT_FIELD_NUMBER_WIDTH(Float.class),
		/** Default text field width */
		TEXT_FIELD_WIDTH(Float.class),
		/** Width of right panel */
		RIGHT_PANEL_WIDTH(Float.class),
		/** Width of sliders */
		SLIDER_WIDTH(Float.class),
		/** Width of text/labels before sliders */
		SLIDER_LABEL_WIDTH(Float.class),
		/** Dark text */
		TEXT_DARK(Color.class),
		/** Scene background color */
		SCENE_BACKGROUND_COLOR(Color.class),
		/** Table width for score screen */
		SCORE_SCREEN_WIDTH(Float.class),
		/** Row height of "My Score" and "My Highscore" */
		SCORE_LABEL_HEIGHT(Float.class),
		/** Table width for highscore */
		HIGHSCORE_SCREEN_WIDTH(Float.class),
		/** Width for highscore placement */
		HIGHSCORE_PLACEMENT_WIDTH(Float.class),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private GeneralVars(Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private GeneralVars(Class<?> type, String jsonName) {
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
		/** Bookmark */
		BOOKMARK,
		/** Screenshot placeholder for levels */
		SCREENSHOT_PLACEHOLDER,
		/** Successful message */
		MESSAGE_SUCCESSFUL,
		/** Failed message */
		MESSAGE_UNSUCCESSFUL,
		/** Space background */
		BACKGROUND_SPACE,
		/** Sync from cloud */
		SYNC_CLOUD,
		/** Sync from device */
		SYNC_DEVICE,

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
		private GeneralImages(String jsonName) {
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
		/** Regular press button */
		BUTTON_PRESS(ButtonStyle.class, "press"),
		/** Toggle button */
		BUTTON_TOGGLE(ButtonStyle.class, "toggle"),
		/** Only displayed as selected */
		BUTTON_SELECTED(ButtonStyle.class, "selected"),
		/** Displayed as selected, but has over and down states */
		BUTTON_SELECTED_PRESSABLE(ButtonStyle.class, "selected_pressable"),
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
		/** Editor name label style */
		LABEL_EDITOR_NAME(LabelStyle.class, "editor_name"),
		/** In front of error (usually above/below text fields). */
		LABEL_ERROR_SECTION_INFO(LabelStyle.class, "error_section_info"),
		/** Section error message (usually above/below text fields). */
		LABEL_ERROR_SECTION(LabelStyle.class, "error_section"),
		/** Tooltip label */
		LABEL_TOOLTIP(LabelStyle.class, "tooltip"),
		/** Label for my score */
		LABEL_MY_SCORE(LabelStyle.class, "my_score"),
		/** Label for top score */
		LABEL_TOP_SCORE(LabelStyle.class, "top_score"),
		/** Header label */
		LABEL_HEADER(LabelStyle.class, "header"),
		/** Default text for text fields */
		LABEL_TEXT_FIELD_DEFAULT(LabelStyle.class, "textfield_default"),
		/** Player name of the comment */
		LABEL_COMMENT_NAME(LabelStyle.class, "comment_name"),
		/** Date of the comment */
		LABEL_COMMENT_DATE(LabelStyle.class, "comment_date"),
		/** A Comment */
		LABEL_COMMENT(LabelStyle.class, "comment"),
		/** List default */
		LIST_DEFAULT(ListStyle.class, "default"),
		/** Logout button */
		LOGOUT(ImageButtonStyle.class),
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
		/** Color picker */
		SLIDER_COLOR_PICKER(SliderStyle.class, "color_picker"),
		/** Text button default style */
		TEXT_BUTTON_TRANSPARENT_PRESS(TextButtonStyle.class, "transparent"),
		/** Text button that can toggle */
		TEXT_BUTTON_TRANSPARENT_TOGGLE(TextButtonStyle.class, "transparent_toggle"),
		/** Flat text button */
		TEXT_BUTTON_FLAT_PRESS(TextButtonStyle.class, "flat_press"),
		/** Flat toggleable text button */
		TEXT_BUTTON_FLAT_TOGGLE(TextButtonStyle.class, "flat_toggle"),
		/** Tag button */
		TEXT_BUTTON_TAG(TextButtonStyle.class, "tag"),
		/** Link */
		TEXT_BUTTON_LINK(TextButtonStyle.class, "link"),
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
		/** New game */
		GAME_NEW(ImageButtonStyle.class),
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
		BOOKMARK(ImageButtonStyle.class),
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
		/** Bullet editor icon in menus */
		EDITOR_BULLET_BIG(ImageButtonStyle.class, "bullet_editor_big"),
		/** Enemy editor icon in menus */
		EDITOR_ENEMY_BIG(ImageButtonStyle.class, "enemy_editor_big"),
		/** Level editor icon in menus */
		EDITOR_LEVEL_BIG(ImageButtonStyle.class, "level_editor_big"),
		/** Campaign editor icon in menus */
		EDITOR_CAMPAIGN_BIG(ImageButtonStyle.class, "campaign_editor_big"),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private General(Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private General(Class<?> type, String jsonName) {
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
	 * Credit images/logos. These are shown in this specific order
	 */
	@SuppressWarnings("javadoc")
	public enum CreditImages implements IImageNames {
		SPIDDEKAUGA_WHITE,
		LIBGDX,


		;

		/**
		 * Used default JSON name for enum
		 */
		private CreditImages() {
			mName = name().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param jsonName name in the json-file
		 */
		private CreditImages(String jsonName) {
			mName = jsonName;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_CREDITS;
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
	 * Credits UI (actually stored in general_ui)
	 */
	public enum CreditsUi implements ISkinNames {
		/** Credit section (e.g. programming, UI Design) */
		LABEL_SECTION(LabelStyle.class, "credit_section"),
		/** Credit name (e.g. Matteus Magnusson) */
		LABEL_NAME(LabelStyle.class, "credit_name"),

		;
		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private CreditsUi(Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private CreditsUi(Class<?> type, String jsonName) {
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
	 * Credits Variables
	 */
	public enum CreditsVars implements ISkinNames {
		/** Padding before a header (e.g. Credits and various logos at end) */
		PADDING_HEADER(Float.class),
		/** Padding before each section (e.g. Programming, UI Design, etc.) */
		PADDING_SECTION(Float.class),
		/** Padding before each logo */
		PADDING_LOGO(Float.class),
		/** Credits scroll speed (pixels per second) */
		SCROLL_SPEED(Float.class),
		/** How long time to wait (in seconds) before showing credits again */
		RESTART_TIME(Float.class),

		;
		/**
		 * Creates a more user-friendly name for the enumeration
		 * @param type the class type
		 */
		private CreditsVars(Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private CreditsVars(Class<?> type, String jsonName) {
			mType = type;
			mName = jsonName;
		}

		@Override
		public Class<?> getClassType() {
			return mType;
		}

		@Override
		public InternalNames getSkinName() {
			return InternalNames.UI_CREDITS;
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
		private Theme(Class<?> type) {
			mType = type;
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param type the class type
		 * @param jsonName name in the json-file
		 */
		private Theme(Class<?> type, String jsonName) {
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
	 * Method for getting a texture region
	 * @param imageName name of the texture region to get
	 * @return texture region if found
	 */
	public static TextureRegion getRegion(IImageNames imageName) {
		Skin skin = ResourceCacheFacade.get(imageName.getSkinName());
		return skin.getRegion(imageName.toString());
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
