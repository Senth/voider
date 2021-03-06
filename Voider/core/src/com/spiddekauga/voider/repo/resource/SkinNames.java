package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.graphics.Color;
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
import com.spiddekauga.voider.resources.InternalDeps;


/**
 * All skin names
 */
@SuppressWarnings("javadoc")
public class SkinNames {


/**
 * Method for getting drawables
 * @param imageName name of the drawable to get
 * @return drawable image
 */
public static Drawable getDrawable(IImageNames imageName) {
	Skin skin = ResourceCacheFacade.get(imageName.getSkinName());
	if (skin == null) {
		throw new IllegalStateException("Skin " + imageName.getSkinName() + " hasn't been loaded");
	}
	return skin.getDrawable(imageName.toString());
}

/**
 * Method for getting a texture region
 * @param imageName name of the texture region to get
 * @return texture region if found
 */
public static TextureRegion getRegion(IImageNames imageName) {
	Skin skin = ResourceCacheFacade.get(imageName.getSkinName());
	if (skin == null) {
		throw new IllegalStateException("Skin " + imageName.getSkinName() + " hasn't been loaded");
	}

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
	if (skin == null) {
		throw new IllegalStateException("Skin " + skinName.getSkinName() + " hasn't been loaded");
	}

	return (ResourceType) skin.get(skinName.toString(), skinName.getClassType());
}

/**
 * @param imageName check if this image has been loaded
 * @return true if the specified resource has been loaded
 */
public static boolean isLoaded(IImageNames imageName) {
	return ResourceCacheFacade.isLoaded(imageName.getSkinName());
}

/**
 * @param skinName check if this skin has been loaded
 * @return true if the specified resource has been loaded
 */
public static boolean isLoaded(ISkinNames skinName) {
	return ResourceCacheFacade.isLoaded(skinName.getSkinName());
}

/**
 * Editor icon names
 */
public enum EditorIcons implements ISkinNames {
	/** Add a corner between two corners in a shape, or move an existing corner */
	ADD_MOVE_CORNER,
	/** Enemy shoot in one specific direction */
	AIM_DIRECTION,
	/**
	 * The enemy will shoot in front of the player (i.e. where the player will be if s/he continues
	 * to move in the same direction)
	 */
	AIM_IN_FRONT_PLAYER,
	/** The enemy will shoot in its moving direction */
	AIM_MOVEMENT,
	/**
	 * The enemy will shoot on the player, or rather in the direction where the player is currently
	 * located
	 */
	AIM_ON_PLAYER,
	/** The enemy will shoot in a circle (or in a straight line) */
	AIM_ROTATE,
	/** Go to the bullet editor */
	BULLET_EDITOR,
	/** When the bullet editor is selected, i.e. we're in the bullet editor */
	BULLET_EDITOR_SELECTED,
	/**
	 * Opens a dialog where the player will be able to select which bullet the enemy should use
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
	/** Play music in level editor */
	MUSIC_PLAY,
	/** Stop music in level editor */
	MUSIC_STOP,
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
	 * How enemies should move in the path. Back and forth means once it reached the end it will
	 * move backwards on the path
	 */
	PATH_BACK_AND_FORTH,
	/**
	 * How enemies should move in the path. Loop means once the enemy reaches the end it will go
	 * directly (i.e. not along the path) to the start and go through the path again and again...
	 */
	PATH_LOOP,
	/**
	 * How enemies should move in the path ONCE means just once. Then it will just continue in a
	 * straight line when it reaches the end of the path
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
	/**
	 * Resets the center of the shape (only visible when actor shape is set to draw)
	 */
	RESET_CENTER,
	/** Test run the current level */
	RUN,
	/** Test run the level from the start */
	RUN_FROM_START,
	/** Save the resource */
	SAVE,
	/** Screen shot */
	SCREENSHOT,
	/** Select one or several actors */
	SELECT,
	/** Set the center of the shape (only visible when actor shape is set to draw) */
	SET_CENTER,
	/** Shape of actor from an image */
	SHAPE_FROM_IMAGE,
	/** Small ship editor icon */
	SHIP_EDITOR,
	/** Selected ship editor icon */
	SHIP_EDITOR_SELECTED,
	/** Ship settings tab */
	SHIP_SETTINGS,
	/** Show background in level editor */
	SHOW_BACKGROUND,
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
	ZOOM_RESET,;

	/** skin name of the icon */
	private String mName;

	/**
	 * Creates a more user-friendly name for the enumeration
	 */
	EditorIcons() {
		mName = super.toString().toLowerCase();
	}

	@Override
	public Class<?> getClassType() {
		return ImageButtonStyle.class;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}

	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_EDITOR;
	}


}

/**
 * Editor images
 */
public enum EditorImages implements IImageNames {
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
	/** Enemy not spawned image */
	ENEMY_NOT_SPAWNED,;

	private String mName;

	/**
	 * Used default JSON name for enum
	 */
	EditorImages() {
		mName = name().toLowerCase();
	}

	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_EDITOR;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}


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
	TERRAIN_ALPHA_DEFAULT(Float.class),;

	/** skin name of the icon */
	private String mName;
	/** The type of the class */
	private Class<?> mType;

	/**
	 * Creates a more user-friendly name for the enumeration
	 * @param type the class type
	 */
	EditorVars(Class<?> type) {
		mName = super.toString().toLowerCase();
		mType = type;
	}

	@Override
	public Class<?> getClassType() {
		return mType;
	}

	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_EDITOR;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}
}

/**
 * Game UI
 */
public enum Game implements ISkinNames {
	/** Health bar for the game */
	HEALTH_BAR(SliderStyle.class),;

	private String mName;
	private Class<?> mType;

	/**
	 * Creates a more user-friendly name for the enumeration
	 * @param type class type
	 */
	Game(Class<?> type) {
		mName = super.toString().toLowerCase();
		mType = type;
	}

	@Override
	public Class<?> getClassType() {
		return mType;
	}


	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_GAME;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}

}

/**
 * Game Images
 */
public enum GameImages implements IImageNames {
	/** Filled life shuttle, to show how many lives the player has */
	LIFE_FILLED("shuttle_lives_yes"),
	/** Empty shuttle life */
	LIFE_EMPTY("shuttle_lives_no"),;

	/** skin name of the icon */
	private String mName;

	/**
	 * Used default JSON name for enum
	 */
	GameImages() {
		mName = name().toLowerCase();
	}

	/**
	 * Create a custom name for the enumeration
	 * @param jsonName name in the json-file
	 */
	GameImages(String jsonName) {
		mName = jsonName;
	}

	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_GAME;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}
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
	 * Transparent button padding (so text doesn't touch the borders) This is padding on all sides
	 */
	PADDING_TRANSPARENT_TEXT_BUTTON(Float.class),
	/** Top padding on a new paragraph row */
	PADDING_PARAGRAPH(Float.class),
	/** Background colors for widgets */
	WIDGET_BACKGROUND_COLOR(Color.class),
	/**
	 * Inner widget background color. I.e. if there is a table inside a widget. E.g. enemy list in
	 * level editor.
	 */
	WIDGET_INNER_BACKGROUND_COLOR(Color.class),
	/** Tag widget in explore and loading screen */
	TAG_BAR_WIDTH(Float.class),
	/** Standard icon and thus row height */
	ROW_HEIGHT(Float.class),
	/** Row height for panel section (smaller text) */
	ROW_HEIGHT_SECTION(Float.class),
	/** Fade in time for message boxes */
	MSGBOX_FADE_IN(Float.class),
	/** Fade out time for message boxes */
	MSGBOX_FADE_OUT(Float.class),
	/** Fade in time for progress bar */
	PROGRESSBAR_FADE_IN(Float.class),
	/** Fade out time for progress bar */
	PROGRESSBAR_FADE_OUT(Float.class),
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
	/** Width for notification messages */
	NOTIFICATION_WIDTH(Float.class),
	/** Notification background color */
	NOTIFICATION_BACKGROUND_COLOR(Color.class),
	/** How long notification messages are shown, in seconds */
	NOTIFICATION_TIME(Float.class),
	/** Fade in duration of notification messages, in seconds */
	NOTIFICATION_FADE_IN(Float.class),
	/** Fade out duration of notification messages, in seconds */
	NOTIFICATION_FADE_OUT(Float.class),
	/** Width of settings window */
	SETTINGS_WIDTH(Float.class),
	/** Height of settings window */
	SETTINGS_HEIGHT(Float.class),
	/** Color for paths in the editor */
	PATH_COLOR(Color.class),;

	private String mName;
	private Class<?> mType;

	/**
	 * Creates a more user-friendly name for the enumeration
	 * @param type the class type
	 */
	GeneralVars(Class<?> type) {
		mType = type;
		mName = super.toString().toLowerCase();
	}

	/**
	 * Create a custom name for the enumeration
	 * @param type the class type
	 * @param jsonName name in the json-file
	 */
	GeneralVars(Class<?> type, String jsonName) {
		mType = type;
		mName = jsonName;
	}

	@Override
	public Class<?> getClassType() {
		return mType;
	}


	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_GENERAL;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}
}

/**
 * General images
 */
public enum GeneralImages implements IImageNames {
	INFO_DATE,
	INFO_EDIT,
	INFO_PLAYER,
	INFO_PLAY_COUNT,
	STAR,
	INFO_TAGS,
	INFO_BOOKMARK("bookmark"),
	INFO_AIM_TYPE,
	INFO_BULLET_DAMAGE,
	INFO_BULLET_SPEED,
	INFO_COLLISION_DAMAGE,
	INFO_DESTROY_ON_COLLIDE,
	INFO_ENEMY_HAS_WEAPON,
	INFO_LEVEL_DIFFICULTY,
	INFO_LEVEL_FRUSTRATION,
	INFO_LEVEL_LENGTH,
	INFO_SPEED,
	INFO_MOVEMENT_TYPE,
	INFO_SCORE_TOP,
	INFO_SCORE_PLAYER,
	/** Screenshot placeholder for levels */
	SCREENSHOT_PLACEHOLDER,
	MESSAGE_SUCCESS,
	MESSAGE_ERROR,
	MESSAGE_HIGHLIGHT,
	MESSAGE_INFO,
	BACKGROUND_SPACE,
	SYNC_CLOUD,
	SYNC_DEVICE,
	SHUTTLE_LARGE,
	/** Background for window settings */
	WINDOW_SETTINGS,;

	private String mName;

	/**
	 * Used default JSON name for enum
	 */
	GeneralImages() {
		mName = name().toLowerCase();
	}

	/**
	 * Create a custom name for the enumeration
	 * @param jsonName name in the json-file
	 */
	GeneralImages(String jsonName) {
		mName = jsonName;
	}

	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_GENERAL;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}
}

/**
 * General UI elements
 */
public enum General implements ISkinNames {
	ANIMATION_WAIT(AnimationWidgetStyle.class, "wait"),
	BUTTON_PRESS(ButtonStyle.class, "press"),
	BUTTON_TOGGLE(ButtonStyle.class, "toggle"),
	BUTTON_SELECTED(ButtonStyle.class, "selected"),
	BUTTON_SELECTED_PRESSABLE(ButtonStyle.class, "selected_pressable"),
	BUTTON_CHECK_BOX("checkbox"),
	CHECK_BOX_DEFAULT(CheckBoxStyle.class, "default"),
	CHECK_BOX_RADIO(CheckBoxStyle.class, "radio"),
	CREATE,
	IMAGE_BUTTON_DEFAULT("default"),
	IMAGE_BUTTON_STUB("stub"),
	IMAGE_BUTTON_STUB_TOGGLE("stub_toggle"),
	IMAGE_BUTTON_TOGGLE("toggle"),
	LABEL_DEFAULT(LabelStyle.class, "default"),
	LABEL_ICON(LabelStyle.class, "icon"),
	LABEL_NOTIFICATION_ERROR(LabelStyle.class, "notification_error"),
	LABEL_NOTIFICATION_WARNING(LabelStyle.class, "notification_warning"),
	LABEL_NOTIFICATION_SUCCESS(LabelStyle.class, "notification_success"),
	LABEL_NOTIFICATION_INFO(LabelStyle.class, "notification_info"),
	LABEL_ERROR(LabelStyle.class, "error"),
	LABEL_HIGHLIGHT(LabelStyle.class, "highlight"),
	LABEL_WARNING(LabelStyle.class, "warning"),
	LABEL_SUCCESS(LabelStyle.class, "success"),
	LABEL_PANEL_SECTION(LabelStyle.class, "panel_section"),
	LABEL_EDITOR_NAME(LabelStyle.class, "editor_name"),
	LABEL_ERROR_SECTION_INFO(LabelStyle.class, "error_section_info"),
	LABEL_ERROR_SECTION(LabelStyle.class, "error_section"),
	LABEL_TOOLTIP(LabelStyle.class, "tooltip"),
	LABEL_HEADER(LabelStyle.class, "header"),
	LABEL_HEADER2(LabelStyle.class, "header2"),
	LABEL_TEXT_FIELD_DEFAULT(LabelStyle.class, "textfield_default"),
	LABEL_COMMENT_NAME(LabelStyle.class, "comment_name"),
	LABEL_COMMENT_DATE(LabelStyle.class, "comment_date"),
	LABEL_COMMENT(LabelStyle.class, "comment"),
	LABEL_PUBLISH_NAME(LabelStyle.class, "publish_name"),
	LABEL_PATH(LabelStyle.class, "path"),
	LABEL_INFO_EXTRA(LabelStyle.class, "info_extra"),
	LIST_DEFAULT(ListStyle.class, "default"),
	LOGOUT,
	PLAY,
	RATING_DEFAULT(RatingWidgetStyle.class, "default"),
	SCROLL_PANE_DEFAULT(ScrollPaneStyle.class, "default"),
	SCROLL_PANE_WINDOW_BACKGROUND(ScrollPaneStyle.class, "background"),
	SELECT_BOX_DEFAULT(SelectBoxStyle.class, "default"),
	SLIDER_DEFAULT(SliderStyle.class, "default"),
	SLIDER_LOADING_BAR(SliderStyle.class, "loading_bar"),
	SLIDER_COLOR_PICKER(SliderStyle.class, "color_picker"),
	TEXT_BUTTON_TRANSPARENT_PRESS(TextButtonStyle.class, "transparent"),
	TEXT_BUTTON_TRANSPARENT_TOGGLE(TextButtonStyle.class, "transparent_toggle"),
	TEXT_BUTTON_FLAT_PRESS(TextButtonStyle.class, "flat_press"),
	TEXT_BUTTON_FLAT_TOGGLE(TextButtonStyle.class, "flat_toggle"),
	TEXT_BUTTON_TAG(TextButtonStyle.class, "tag"),
	TEXT_BUTTON_LINK(TextButtonStyle.class, "link"),
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
	COLOR,
	/** Replay the level */
	REPLAY,
	BACK_BIG,
	/** Comments tab */
	COMMENTS,
	/** Image tab */
	IMAGES,
	/** Overview tab */
	OVERVIEW,
	TAGS,
	/** Tag button */
	TAG,
	BOOKMARK,
	EXPLORE_LOCAL,
	EXPLORE_ONLINE_SEARCH,
	EXPLORE_ONLINE,
	EDITOR_BULLET_BIG("bullet_editor_big"),
	EDITOR_ENEMY_BIG("enemy_editor_big"),
	EDITOR_LEVEL_BIG("level_editor_big"),
	EDITOR_CAMPAIGN_BIG("campaign_editor_big"),
	EDITOR_SHIP_BIG("ship_editor_big"),
	SETTINGS_ACCOUNT,
	SETTINGS_GENERAL("settings_interface"),
	SETTINGS_NETWORK,
	SETTINGS_DISPLAY("settings_resolution"),
	SETTINGS_DEBUG,
	SETTINGS_SOUND,
	SETTINGS_STATISTICS,
	/** Search filter options in explore */
	SEARCH_FILTER,
	PANEL_BUG,
	PANEL_PLAYER,
	PANEL_SHOP,
	PANEL_VOTE,
	PANEL_ACHIEVEMENTS,
	PANEL_REDDIT,
	PANEL_SETTINGS,
	PANEL_LOGOUT,
	PANEL_INFO,
	PANEL_CHANGELOG,
	PANEL_TERMS,
	TERMS_BIG,
	CHANGELOG_BIG,
	INFO_BIG,
	CONTINUE,
	ICON_SIZE_SMALL,
	ICON_SIZE_MEDIUM,
	ICON_SIZE_LARGE,;

	private String mName;
	private Class<?> mType;

	/**
	 * Creates an ImageButtonStyle enum
	 */
	General() {
		this(ImageButtonStyle.class);
	}

	/**
	 * @param type the class type
	 */
	General(Class<?> type) {
		mType = type;
		mName = super.toString().toLowerCase();
	}

	/**
	 * Creates an ImageButtonStyle enum
	 * @param jsonName name in the json-file
	 */
	General(String jsonName) {
		this(ImageButtonStyle.class, jsonName);
	}

	/**
	 * Create a custom name for the enumeration
	 * @param type the class type
	 * @param jsonName name in the json-file
	 */
	General(Class<?> type, String jsonName) {
		mType = type;
		mName = jsonName;
	}

	@Override
	public Class<?> getClassType() {
		return mType;
	}


	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_GENERAL;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}
}

/**
 * Credit images/logos. These are shown in this specific order
 */
public enum CreditImages implements IImageNames {
	SPIDDEKAUGA_WHITE,
	LIBGDX,;

	private String mName;

	/**
	 * Used default JSON name for enum
	 */
	CreditImages() {
		mName = name().toLowerCase();
	}

	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_CREDITS;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}
}

/**
 * Credits UI (actually stored in general_ui)
 */
public enum CreditsUi implements ISkinNames {
	/** Credit name (e.g. Matteus Magnusson) */
	LABEL_NAME(LabelStyle.class, "credit_name"),;

	private String mName;
	private Class<?> mType;

	/**
	 * Creates a more user-friendly name for the enumeration
	 * @param type the class type
	 */
	CreditsUi(Class<?> type) {
		mType = type;
		mName = super.toString().toLowerCase();
	}

	/**
	 * Create a custom name for the enumeration
	 * @param type the class type
	 * @param jsonName name in the json-file
	 */
	CreditsUi(Class<?> type, String jsonName) {
		mType = type;
		mName = jsonName;
	}

	@Override
	public Class<?> getClassType() {
		return mType;
	}


	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_GENERAL;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}
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
	RESTART_TIME(Float.class),;

	private String mName;
	private Class<?> mType;

	/**
	 * Creates a more user-friendly name for the enumeration
	 * @param type the class type
	 */
	CreditsVars(Class<?> type) {
		mType = type;
		mName = super.toString().toLowerCase();
	}

	/**
	 * Create a custom name for the enumeration
	 * @param type the class type
	 * @param jsonName name in the json-file
	 */
	CreditsVars(Class<?> type, String jsonName) {
		mType = type;
		mName = jsonName;
	}

	@Override
	public Class<?> getClassType() {
		return mType;
	}


	@Override
	public InternalDeps getSkinName() {
		return InternalDeps.UI_CREDITS;
	}

	/**
	 * @return name of the icon inside the skin
	 */
	@Override
	public String toString() {
		return mName;
	}
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
	InternalDeps getSkinName();
}

/**
 * Interface for image names
 */
public interface IImageNames {
	/**
	 * @return skin name
	 */
	InternalDeps getSkinName();
}
}
