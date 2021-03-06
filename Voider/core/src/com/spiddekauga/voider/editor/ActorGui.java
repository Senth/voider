package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.ColorTintPicker;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.SelectBoxListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Collision;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual;
import com.spiddekauga.voider.editor.IActorEditor.Tools;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.DrawImages;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.EditorIcons;
import com.spiddekauga.voider.scene.ui.ButtonFactory.TabImageWrapper;
import com.spiddekauga.voider.scene.ui.ButtonFactory.TabWrapper;
import com.spiddekauga.voider.utils.Messages;

import java.util.ArrayList;

/**
 * Has some common methods for gui
 */
public abstract class ActorGui extends EditorGui {

/** Hides custom draw tool menu */
private HideListener mDrawToolHider = null;
/** All widget variables */
private InnerWidgets mWidgets = new InnerWidgets();
/** The active editor */
private IActorEditor mActorEditor = null;

@Override
public void onDestroy() {
	mWidgets.visual.table.dispose();
	mWidgets.collision.table.dispose();
	mInfoTable.dispose();
	mWidgets.color.table.dispose();

	mDrawToolHider.dispose();
	mWidgets.collision.hider.dispose();
	mWidgets.visual.hider.dispose();

	super.onDestroy();
}

@Override
public void onCreate() {
	super.onCreate();

	// Tabs
	mWidgets.collision.table.setAlignTable(Horizontal.LEFT, Vertical.TOP);
	mWidgets.collision.table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
	mWidgets.visual.table.setPreferences(mWidgets.collision.table);

	initVisual();
	initToolMenu();
	initInfoTable();
	initSettingsMenu();
}

@Override
public void resetValues() {
	super.resetValues();


	resetVisuals();
	resetColor();
	resetInfoOptions();
	resetCollision();
	resetTools();
}

@Override
void resetCollisionBoxes() {
	if (mSettingTabs == null) {
		return;
	}

	super.resetCollisionBoxes();

	// Tab widget
	createCollisionBoxes(mSettingTabs);

	// Tool
	createCollisionBoxes(mToolMenu);

	// Upper/Lower borders
	float width = Gdx.graphics.getWidth();
	float height = mUiFactory.getStyles().vars.barUpperLowerHeight;
	createCollisionBox(0, 0, width, height);
	createCollisionBox(0, Gdx.graphics.getHeight() - height, width, height);
}

@Override
public void setInfoNameError(String errorText) {
	mWidgets.info.nameError.setText(errorText);
	mWidgets.info.nameError.pack();
}

/**
 * Reset the actor's shape
 */
private void resetVisuals() {
	// Visuals
	mWidgets.visual.startAngle.setValue(mActorEditor.getStartingAngle());
	mWidgets.visual.rotationSpeed.setValue(mActorEditor.getRotationSpeed());

	// Shape
	if (mWidgets.visual.circleRadius != null) {
		mWidgets.visual.circleRadius.setValue(mActorEditor.getShapeRadius());
	}
	if (mWidgets.visual.rectangleWidth != null) {
		mWidgets.visual.rectangleWidth.setValue(mActorEditor.getShapeWidth());
	}
	if (mWidgets.visual.rectangleHeight != null) {
		mWidgets.visual.rectangleHeight.setValue(mActorEditor.getShapeHeight());
	}
	if (mWidgets.visual.triangleWidth != null) {
		mWidgets.visual.triangleWidth.setValue(mActorEditor.getShapeWidth());
	}
	if (mWidgets.visual.triangleHeight != null) {
		mWidgets.visual.triangleHeight.setValue(mActorEditor.getShapeHeight());
	}

	if (mActorEditor.getShapeType() != null) {
		switch (mActorEditor.getShapeType()) {
		case CIRCLE:
			mWidgets.visual.shapeCircle.setChecked(true);
			break;

		case RECTANGLE:
			mWidgets.visual.shapeRectangle.setChecked(true);
			break;

		case TRIANGLE:
			mWidgets.visual.shapeTriangle.setChecked(true);
			break;

		case CUSTOM:
			mWidgets.visual.shapeCustom.setChecked(true);
			break;

		case IMAGE:
			mWidgets.visual.shapeImage.setChecked(true);
			break;
		}
	}

	// Image
	if (mWidgets.visual.imageScale != null) {
		mWidgets.visual.imageAngleMin.setValue(mActorEditor.getShapeImageAngleMin());
		mWidgets.visual.imageDistMin.setValue(mActorEditor.getShapeImageDistMin());
		mWidgets.visual.imageScale.setValue(mActorEditor.getShapeImageScale());
		mWidgets.visual.imageDrawOutline.setChecked(mActorEditor.isDrawOnlyOutline());
		mWidgets.visual.shapeImageSelect.setSelected(mActorEditor.getDrawImage());

		if (mActorEditor.isShapeImageUpdatedContinuously()) {
			mWidgets.visual.imageUpdateOn.setChecked(true);
		} else {
			mWidgets.visual.imageUpdateOff.setChecked(true);
		}
	}
}

/**
 * Reset color
 */
private void resetColor() {
	// Only if the color has been initialized
	if (mWidgets.color.picker != null) {
		mWidgets.color.picker.setPickColor(mActorEditor.getColor());
	}
}

/**
 * Reset info options
 */
private void resetInfoOptions() {
	mWidgets.info.name.setText(mActorEditor.getName());
	mWidgets.info.description.setText(mActorEditor.getDescription());
	mWidgets.info.description.setTextFieldListener(null);
}

/**
 * Reset collision options
 */
private void resetCollision() {
	// If one variable has been initialized, all has...
	if (mWidgets.collision.damage != null) {
		mWidgets.collision.damage.setValue(mActorEditor.getCollisionDamage());
		mWidgets.collision.destroyOnCollide.setChecked(mActorEditor.isDestroyedOnCollide());
	}
}

/**
 * Reset tool widget selection
 */
void resetTools() {
	switch (mActorEditor.getActiveTool()) {
	case ADD_MOVE_CORNER:
		mWidgets.tool.addMoveCorner.setChecked(true);
		break;

	case DELETE:
		mWidgets.tool.delete.setChecked(true);
		break;

	case DRAW_APPEND:
		mWidgets.tool.drawAppend.setChecked(true);
		break;

	case DRAW_ERASE:
		mWidgets.tool.drawErase.setChecked(true);
		break;

	case MOVE:
		mWidgets.tool.move.setChecked(true);
		break;

	case REMOVE_CORNER:
		mWidgets.tool.removeCorner.setChecked(true);
		break;

	case CENTER_SET:
		mWidgets.tool.setCenter.setChecked(true);
		break;

	default:
		mWidgets.tool.move.setChecked(true);
		break;
	}
}

/**
 * Initializes visual options
 */
private void initVisual() {
	IC_Visual icVisual = getVisualConfig();
	ArrayList<TabWrapper> tabs = new ArrayList<>();
	String type = getResourceTypeNameCapital();

	mWidgets.visual.hider.addToggleActor(mWidgets.visual.table);
	AlignTable table = mWidgets.visual.table;

	// Starting angle
	mUiFactory.text.addPanelSection("Starting Direction", table, null);
	SliderListener sliderListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setStartingAngle(newValue);
		}
	};
	mWidgets.visual.startAngle = mUiFactory.addSlider("Angle", type + "Visual_StartDirection", 0, 360, 1, sliderListener, table, null,
			mDisabledWhenPublished);


	// Rotation speed
	mUiFactory.text.addPanelSection("Rotation", table, null);
	sliderListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setRotationSpeed(newValue);
		}
	};
	mWidgets.visual.rotationSpeed = mUiFactory.addSlider("Speed", type + "Visual_RotateSpeed", icVisual.getRotateSpeedMin(),
			icVisual.getRotateSpeedMax(), icVisual.getRotateSpeedStepSize(), sliderListener, table, null, mDisabledWhenPublished);


	// Different shape tabs
	mUiFactory.text.addPanelSection("Shape", table, null);

	// Circle
	TabImageWrapper circleTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.CIRCLE_SHAPE);
	circleTab.setListener(new ButtonListener() {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.setShapeType(ActorShapeTypes.CIRCLE);
			mActorEditor.resetCenterOffset();
		}
	});
	tabs.add(circleTab);

	// Rectangle
	TabImageWrapper rectangleTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.RECTANGLE_SHAPE);
	rectangleTab.setListener(new ButtonListener() {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.setShapeType(ActorShapeTypes.RECTANGLE);
			mActorEditor.resetCenterOffset();
		}
	});
	tabs.add(rectangleTab);

	// Triangle
	TabImageWrapper triangleTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.TRIANGLE_SHAPE);
	triangleTab.setListener(new ButtonListener() {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.setShapeType(ActorShapeTypes.TRIANGLE);
			mActorEditor.resetCenterOffset();
		}
	});
	tabs.add(triangleTab);

	// Custom (draw)
	TabImageWrapper customTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.DRAW_CUSTOM_SHAPE);
	customTab.setHider(new HideListener(true) {
		@Override
		protected void onHide() {
			resetCollisionBoxes();
			mActorEditor.deactivateTools();
		}

		@Override
		protected void onShow() {
			resetCollisionBoxes();
			mActorEditor.activateTools();
		}
	});
	customTab.setListener(new ButtonListener() {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.setShapeType(ActorShapeTypes.CUSTOM);
			mActorEditor.resetCenterOffset();
		}
	});
	mDrawToolHider = customTab.getHider();
	tabs.add(customTab);

	// Shape from image
	TabImageWrapper imageTab = null;
	if (getClass() == ShipEditorGui.class) {
		imageTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.SHAPE_FROM_IMAGE);
		imageTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mActorEditor.setShapeType(ActorShapeTypes.IMAGE);
			}
		});
		tabs.add(imageTab);
	}


	// Create tabs
	mUiFactory.button.addTabs(table, mWidgets.visual.hider, false, mDisabledWhenPublished, mInvoker, tabs);

	// Set buttons
	mWidgets.visual.shapeCircle = circleTab.getButton();
	mWidgets.visual.shapeRectangle = rectangleTab.getButton();
	mWidgets.visual.shapeTriangle = triangleTab.getButton();
	mWidgets.visual.shapeCustom = customTab.getButton();
	if (imageTab != null) {
		mWidgets.visual.shapeImage = imageTab.getButton();
	}


	// Set tooltip
	mTooltip.add(customTab.getButton(), Messages.EditorTooltips.VISUAL_CUSTOM);


	// Circle
	// Radius
	sliderListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setShapeRadius(newValue);
		}
	};
	mWidgets.visual.circleRadius = mUiFactory.addSlider("Radius", type + "Visual_CircleRadius", icVisual.getRadiusMin(), icVisual.getRadiusMax(),
			icVisual.getRadiusStepSize(), sliderListener, table, circleTab.getHider(), mDisabledWhenPublished);


	// Rectangle
	// Width
	SliderListener widthListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setShapeWidth(newValue);
		}
	};
	mWidgets.visual.rectangleWidth = mUiFactory.addSlider("Width", type + "Visual_RectangleWidth", icVisual.getSizeMin(), icVisual.getSizeMax(),
			icVisual.getSizeStepSize(), widthListener, table, rectangleTab.getHider(), mDisabledWhenPublished);

	// Height
	SliderListener heightListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setShapeHeight(newValue);
		}
	};
	mWidgets.visual.rectangleHeight = mUiFactory.addSlider("Height", type + "Visual_RectangleHeight", icVisual.getSizeMin(),
			icVisual.getSizeMax(), icVisual.getSizeStepSize(), heightListener, table, rectangleTab.getHider(), mDisabledWhenPublished);


	// Triangle
	// Width
	mWidgets.visual.triangleWidth = mUiFactory.addSlider("Width", type + "Visual_TriangleWidth", icVisual.getSizeMin(), icVisual.getSizeMax(),
			icVisual.getSizeStepSize(), widthListener, table, triangleTab.getHider(), mDisabledWhenPublished);

	// Height
	mWidgets.visual.triangleHeight = mUiFactory.addSlider("Height", type + "Visual_TriangleHeight", icVisual.getSizeMin(), icVisual.getSizeMax(),
			icVisual.getSizeStepSize(), heightListener, table, triangleTab.getHider(), mDisabledWhenPublished);

	// Image
	if (imageTab != null) {
		initVisualImageSettings(imageTab.getHider());
	}
}

/**
 * Initializes the toolbox
 */
private void initToolMenu() {
	ButtonGroup<ImageButton> buttonGroup = new ButtonGroup<>();

	mDrawToolHider.addToggleActor(mToolMenu);


	// Zoom in
	mToolMenu.row();
	mWidgets.tool.zoomIn = mUiFactory.button.addTool(EditorIcons.ZOOM_IN, null, mToolMenu, null);
	mTooltip.add(mWidgets.tool.zoomIn, Messages.EditorTooltips.TOOL_ZOOM_IN_ACTOR);
	new ButtonListener(mWidgets.tool.zoomIn) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.executeTool(Tools.ZOOM_IN);
		}
	};

	// Zoom out
	mWidgets.tool.zoomOut = mUiFactory.button.addTool(EditorIcons.ZOOM_OUT, null, mToolMenu, null);
	mTooltip.add(mWidgets.tool.zoomOut, Messages.EditorTooltips.TOOL_ZOOM_OUT_ACTOR);
	new ButtonListener(mWidgets.tool.zoomOut) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.executeTool(Tools.ZOOM_OUT);
		}
	};

	// Pan
	mToolMenu.row();
	mWidgets.tool.pan = mUiFactory.button.addTool(EditorIcons.PAN, buttonGroup, mToolMenu, null);
	mTooltip.add(mWidgets.tool.pan, Messages.EditorTooltips.TOOL_PAN_ACTOR);
	new ButtonListener(mWidgets.tool.pan) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.switchTool(Tools.PAN);
		}
	};

	// Reset zoom
	Button button = mUiFactory.button.addTool(EditorIcons.ZOOM_RESET, null, mToolMenu, null);
	mTooltip.add(button, Messages.EditorTooltips.TOOL_ZOOM_RESET_ACTOR);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.executeTool(Tools.ZOOM_RESET);
		}
	};


	// --------- SEPARATOR -----------
	mUiFactory.button.addToolSeparator(mToolMenu);


	// Move
	mWidgets.tool.move = mUiFactory.button.addTool(EditorIcons.MOVE, buttonGroup, mToolMenu, mDisabledWhenPublished);
	mTooltip.add(mWidgets.tool.move, Messages.EditorTooltips.TOOL_MOVE_ACTOR);
	new ButtonListener(mWidgets.tool.move) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.switchTool(Tools.MOVE);
		}
	};

	// Delete
	mWidgets.tool.delete = mUiFactory.button.addTool(EditorIcons.DELETE, buttonGroup, mToolMenu, mDisabledWhenPublished);
	mTooltip.add(mWidgets.tool.delete, Messages.EditorTooltips.TOOL_DELETE_ACTOR);
	new ButtonListener(mWidgets.tool.delete) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.switchTool(Tools.DELETE);
		}
	};


	// --------- SEPARATOR -----------
	mUiFactory.button.addToolSeparator(mToolMenu);


	// Terrain draw_append
	mWidgets.tool.drawAppend = mUiFactory.button.addTool(EditorIcons.TERRAIN_DRAW_APPEND, buttonGroup, mToolMenu, mDisabledWhenPublished);
	mTooltip.add(mWidgets.tool.drawAppend, Messages.EditorTooltips.TOOL_DRAW_APPEND_ACTOR);
	new ButtonListener(mWidgets.tool.drawAppend) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.switchTool(Tools.DRAW_APPEND);
		}
	};


	// Terrain draw_erase
	mWidgets.tool.drawErase = mUiFactory.button.addTool(EditorIcons.DRAW_ERASE, buttonGroup, mToolMenu, mDisabledWhenPublished);
	mTooltip.add(mWidgets.tool.drawErase, Messages.EditorTooltips.TOOL_DRAW_ERASE_ACTOR);
	new ButtonListener(mWidgets.tool.drawErase) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.switchTool(Tools.DRAW_ERASE);
		}
	};

	// add_move_corner
	mToolMenu.row();
	mWidgets.tool.addMoveCorner = mUiFactory.button.addTool(EditorIcons.ADD_MOVE_CORNER, buttonGroup, mToolMenu, mDisabledWhenPublished);
	mTooltip.add(mWidgets.tool.addMoveCorner, Messages.EditorTooltips.TOOL_DRAW_CORNER_ADD_ACTOR);
	new ButtonListener(mWidgets.tool.addMoveCorner) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.switchTool(Tools.ADD_MOVE_CORNER);
		}
	};

	// remove_corner
	mWidgets.tool.removeCorner = mUiFactory.button.addTool(EditorIcons.REMOVE_CORNER, buttonGroup, mToolMenu, mDisabledWhenPublished);
	mTooltip.add(mWidgets.tool.removeCorner, Messages.EditorTooltips.TOOL_DRAW_CORNER_REMOVE_ACTOR);
	new ButtonListener(mWidgets.tool.removeCorner) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.switchTool(Tools.REMOVE_CORNER);
		}
	};


	// --------- SEPARATOR -----------
	mUiFactory.button.addToolSeparator(mToolMenu);


	// Set center
	mWidgets.tool.setCenter = mUiFactory.button.addTool(EditorIcons.SET_CENTER, buttonGroup, mToolMenu, mDisabledWhenPublished);
	mTooltip.add(mWidgets.tool.setCenter, Messages.EditorTooltips.TOOL_CENTER_SET);
	new ButtonListener(mWidgets.tool.setCenter) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.switchTool(Tools.CENTER_SET);
		}
	};

	// Reset center
	button = mUiFactory.button.addTool(EditorIcons.RESET_CENTER, null, mToolMenu, mDisabledWhenPublished);
	mTooltip.add(button, Messages.EditorTooltips.TOOL_CENTER_RESET);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mActorEditor.executeTool(Tools.CENTER_RESET);
		}
	};
}

/**
 * Initializes actor options
 */
protected void initInfoTable() {
	mInfoTable.setName("info-table");
	mInfoTable.setAlignTable(Horizontal.LEFT, Vertical.TOP);
	mInfoTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
	mInfoTable.setPad(mUiFactory.getStyles().vars.paddingInner);
	TextFieldListener listener;


	// Name
	listener = new TextFieldListener(mInvoker) {
		@Override
		protected void onChange(String newText) {
			mActorEditor.setName(newText);
		}
	};
	mWidgets.info.name = mUiFactory.addTextField("Name", true, Messages.replaceName(Messages.Editor.NAME_FIELD_DEFAULT, getResourceTypeName()),
			listener, mInfoTable, mDisabledWhenPublished);
	mWidgets.info.name.setMaxLength(ConfigIni.getInstance().editor.general.getNameLengthMax());
	mWidgets.info.nameError = mUiFactory.text.getLastCreatedErrorLabel();


	// Description
	listener = new TextFieldListener(mInvoker) {
		@Override
		protected void onChange(String newText) {
			mActorEditor.setDescription(newText);
		}
	};
	mWidgets.info.description = mUiFactory.addTextArea("Description", false,
			Messages.replaceName(Messages.Editor.DESCRIPTION_FIELD_DEFAULT, getResourceTypeName()), listener, mInfoTable, mDisabledWhenPublished);
	mWidgets.info.description.setMaxLength(ConfigIni.getInstance().editor.general.getDescriptionLengthMax());
}

/**
 * @return visual options for the actor gui
 */
protected abstract IC_Visual getVisualConfig();

/**
 * Initializes image shape settings
 * @param imageHider
 */
private void initVisualImageSettings(GuiHider imageHider) {
	AlignTable table = mWidgets.visual.table;
	IC_Visual icVisual = getVisualConfig();
	String type = getResourceTypeNameCapital();

	// Update
	mUiFactory.text.addPanelSection("Update Continuously", table, imageHider);
	ArrayList<TabWrapper> tabs = new ArrayList<>();
	TabImageWrapper onTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.ON);
	onTab.setListener(new ButtonListener() {
		@Override
		protected void onChecked(Button button, boolean checked) {
			mActorEditor.setShapeImageUpdateContinuously(checked);
		}
	});
	tabs.add(onTab);

	TabImageWrapper offTab = mUiFactory.button.createTabImageWrapper(SkinNames.EditorIcons.OFF);
	tabs.add(offTab);

	mUiFactory.button.addTabs(table, imageHider, false, mDisabledWhenPublished, mInvoker, tabs);
	tabs = null;

	mWidgets.visual.imageUpdateOn = onTab.getButton();
	mWidgets.visual.imageUpdateOff = offTab.getButton();

	// Draw outline
	ButtonListener buttonListener = new ButtonListener() {
		@Override
		protected void onChecked(Button button, boolean checked) {
			mActorEditor.setDrawOnlyOutline(checked);
		}

		;
	};
	mWidgets.visual.imageDrawOutline = mUiFactory.button.addPanelCheckBox("Draw outline", buttonListener, table, imageHider,
			mDisabledWhenPublished);

	// Select image
	SelectBoxListener<DrawImages> selectBoxListener = new SelectBoxListener<DrawImages>(mInvoker) {
		@Override
		protected void onSelectionChanged(int itemIndex) {
			mActorEditor.setDrawImage(mSelectBox.getSelected());
		}
	};
	mUiFactory.text.addPanelSection("Image", table, imageHider);
	mWidgets.visual.shapeImageSelect = mUiFactory.addSelectBox(null, DrawImages.values(), selectBoxListener, table, imageHider,
			mDisabledWhenPublished);
	mWidgets.visual.shapeImageSelect.setMaxListCount(7);

	// Scale
	SliderListener sliderListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setShapeImageScale(newValue);
		}
	};
	mWidgets.visual.imageScale = mUiFactory.addSlider("Scale", type + "Visual_ImageScale", icVisual.getImageScaleMin(),
			icVisual.getImageScaleMax(), icVisual.getImageScaleStepSize(), sliderListener, table, imageHider, mDisabledWhenPublished);

	// Min Distance between points
	sliderListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setShapeImageDistMin(newValue);
		}
	};
	mWidgets.visual.imageDistMin = mUiFactory.addSlider("Dist", type + "Visual_ImageDist", icVisual.getImageDistMin(),
			icVisual.getImageDistMax(), icVisual.getImageDistStepSize(), sliderListener, table, imageHider, mDisabledWhenPublished);

	// Min Angle between points
	sliderListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setShapeImageAngleMin(newValue);
		}
	};
	mWidgets.visual.imageAngleMin = mUiFactory.addSlider("Angle", type + "Visual_ImageAngle", icVisual.getImageAngleMin(),
			icVisual.getImageAngleMax(), icVisual.getImageAngleStepSize(), sliderListener, table, imageHider, mDisabledWhenPublished);
}

/**
 * Sets the actor editor for this GUI
 * @param actorEditor editor bound to this GUI
 */
protected void setActorEditor(ActorEditor actorEditor) {
	setEditor(actorEditor);
	mActorEditor = actorEditor;
}

/**
 * Initializes color options
 * @param colors all the colors to choose between
 */
protected void initColor(Color... colors) {
	AlignTable table = mWidgets.color.table;

	mUiFactory.text.addPanelSection(getResourceTypeNameCapital() + " Color", table, null);

	mWidgets.color.picker = mUiFactory.addColorTintPicker(getResourceTypeNameCapital() + "_Color", table, null, mDisabledWhenPublished, colors);
	new SliderListener(mWidgets.color.picker, null, mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setColor(mWidgets.color.picker.getPickColor());
		}
	};
}

/**
 * Initializes collision options, this is optional
 */
protected void initCollision() {
	IC_Collision icCollision = ConfigIni.getInstance().editor.actor.collision;

	// Collision damage
	mUiFactory.text.addPanelSection("Collision", mWidgets.collision.table, null);
	SliderListener sliderListener = new SliderListener(mInvoker) {
		@Override
		protected void onChange(float newValue) {
			mActorEditor.setCollisionDamage(newValue);
		}
	};
	mWidgets.collision.damage = mUiFactory.addSlider("Damage", getResourceTypeNameCapital() + "Collision_Damage", icCollision.getDamageMin(),
			icCollision.getDamageMax(), icCollision.getDamageStepSize(), sliderListener, mWidgets.collision.table, null, mDisabledWhenPublished);


	// Collision destroy
	ArrayList<Actor> createdActors = new ArrayList<>();
	createdActors.add(mUiFactory.text.addPanelSection("Collision Destruction", mWidgets.collision.table, null));
	ButtonListener buttonListener = new ButtonListener() {
		@Override
		protected void onChecked(Button button, boolean checked) {
			mActorEditor.setDestroyOnCollide(checked);
		}
	};
	mWidgets.collision.destroyOnCollide = mUiFactory.button.addPanelCheckBox("Destroy", buttonListener, mWidgets.collision.table, null,
			createdActors);
	mTooltip.add(createdActors, Messages.EditorTooltips.COLLISION_DESTROY);
	mDisabledWhenPublished.addAll(createdActors);
}

/**
 * @return visual table
 */
protected AlignTable getVisualTable() {
	return mWidgets.visual.table;
}

/**
 * @return visual hider
 */
protected HideListener getVisualHider() {
	return mWidgets.visual.hider;
}

/**
 * @return color table
 */
protected AlignTable getColorTable() {
	return mWidgets.color.table;
}

/**
 * @return collision table
 */
protected AlignTable getCollisionTable() {
	return mWidgets.collision.table;
}

/**
 * All the widgets which state can be changed and thus reset
 */
private static class InnerWidgets {
	VisualWidgets visual = new VisualWidgets();
	InfoWidgets info = new InfoWidgets();
	CollisionWidgets collision = new CollisionWidgets();
	ToolWidgets tool = new ToolWidgets();
	ColorWidgets color = new ColorWidgets();

	/**
	 * Tool widgets
	 */
	static class ToolWidgets {
		Button move = null;
		Button delete = null;
		Button pan = null;
		Button zoomIn = null;
		Button zoomOut = null;
		Button drawAppend = null;
		Button drawErase = null;
		Button addMoveCorner = null;
		Button removeCorner = null;
		Button setCenter = null;
	}

	/**
	 * Color widgets
	 */
	static class ColorWidgets {
		AlignTable table = new AlignTable();
		ColorTintPicker picker = null;
	}

	/**
	 * Visual widgets
	 */
	static class VisualWidgets {
		AlignTable table = new AlignTable();
		HideListener hider = new HideListener(true);

		Slider startAngle = null;
		Slider rotationSpeed = null;

		// Shapes
		Button shapeCircle = null;
		Button shapeTriangle = null;
		Button shapeRectangle = null;
		Button shapeCustom = null;
		Button shapeImage = null;

		// Settings
		Slider circleRadius = null;
		Slider triangleWidth = null;
		Slider triangleHeight = null;
		Slider rectangleWidth = null;
		Slider rectangleHeight = null;

		// Image settings
		Button imageUpdateOn = null;
		Button imageUpdateOff = null;
		Button imageDrawOutline = null;
		Slider imageScale = null;
		Slider imageAngleMin = null;
		Slider imageDistMin = null;
		SelectBox<DrawImages> shapeImageSelect = null;
	}

	/**
	 * General options
	 */
	static class InfoWidgets {
		Label nameError = null;
		TextField name = null;
		TextField description = null;
	}

	/**
	 * Collision options
	 */
	static class CollisionWidgets {
		Slider damage = null;
		Button destroyOnCollide = null;
		AlignTable table = new AlignTable();
		HideListener hider = new HideListener(true);
	}
}
}
