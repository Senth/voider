package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.UiPanelFactory.TabImageWrapper;
import com.spiddekauga.utils.scene.ui.UiPanelFactory.TabWrapper;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.editor.IActorEditor.Tools;
import com.spiddekauga.voider.editor.commands.CActorEditorCenterReset;
import com.spiddekauga.voider.editor.commands.CDefHasValidName;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.EditorIcons;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * Has some common methods for gui
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class ActorGui extends EditorGui {

	@Override
	public void dispose() {
		mWidgets.visual.table.dispose();
		mWidgets.collision.table.dispose();
		mWidgets.info.table.dispose();

		mDrawToolHider.dispose();
		mWidgets.collision.hider.dispose();
		mWidgets.visual.hider.dispose();

		super.dispose();
	}

	@Override
	public void initGui() {
		super.initGui();

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

		// Options
		mWidgets.info.name.setText(mActorEditor.getName());
		mWidgets.info.description.setText(mActorEditor.getDescription());
		mWidgets.info.description.setTextFieldListener(null);


		// Collision
		// If one variable has been initialized, all has...
		if (mWidgets.collision.damage != null) {
			mWidgets.collision.damage.setValue(mActorEditor.getCollisionDamage());
			mWidgets.collision.destroyOnCollide.setChecked(mActorEditor.isDestroyedOnCollide());
		}

		resetTools();
	}

	/**
	 * An optional settings menu for the editor. E.g. to switch between visuals and weapon
	 * settings in enemy editor.
	 */
	protected void initSettingsMenu() {
		mSettingTabs.setMargin(getTopBottomPadding(), mStyles.vars.paddingOuter, getTopBottomPadding(), mStyles.vars.paddingOuter)
				.setAlign(Horizontal.RIGHT, Vertical.TOP).setTabAlign(Horizontal.RIGHT).setFillHeight(true)
				.setBackground(new Background(mStyles.colors.widgetBackground)).setPaddingContent(mStyles.vars.paddingInner)
				.setContentWidth((Float) SkinNames.getResource(SkinNames.GeneralVars.RIGHT_PANEL_WIDTH));

		getStage().addActor(mSettingTabs);
	}

	/**
	 * Reset the actor's shape
	 */
	void resetVisuals() {
		// Visuals
		mWidgets.visual.startAngle.setValue(mActorEditor.getStartingAngle());
		mWidgets.visual.rotationSpeed.setValue(mActorEditor.getRotationSpeed());

		// Shape
		if (mWidgets.visual.shapeCircleRadius != null) {
			mWidgets.visual.shapeCircleRadius.setValue(mActorEditor.getShapeRadius());
		}
		if (mWidgets.visual.shapeRectangleWidth != null) {
			mWidgets.visual.shapeRectangleWidth.setValue(mActorEditor.getShapeWidth());
		}
		if (mWidgets.visual.shapeRectangleHeight != null) {
			mWidgets.visual.shapeRectangleHeight.setValue(mActorEditor.getShapeHeight());
		}
		if (mWidgets.visual.shapeTriangleWidth != null) {
			mWidgets.visual.shapeTriangleWidth.setValue(mActorEditor.getShapeWidth());
		}
		if (mWidgets.visual.shapeTriangleHeight != null) {
			mWidgets.visual.shapeTriangleHeight.setValue(mActorEditor.getShapeHeight());
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
			}
		}
	}

	/**
	 * Sets the actor editor for this GUI
	 * @param actorEditor editor bound to this GUI
	 */
	protected void setActorEditor(IActorEditor actorEditor) {
		mActorEditor = actorEditor;
		mInvoker = mActorEditor.getInvoker();
	}

	/**
	 * Initializes actor options
	 */
	protected void initInfoTable() {
		mWidgets.info.table.setName("info-table");
		mWidgets.info.table.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		mWidgets.info.table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		mWidgets.info.table.setPadding(mStyles.vars.paddingInner);
		TextFieldListener listener;


		// Name
		listener = new TextFieldListener(mInvoker) {
			@Override
			protected void onChange(String newText) {
				mActorEditor.setName(newText);
			}
		};
		mWidgets.info.name = mUiFactory.addTextField("Name", Messages.replaceName(Messages.Editor.NAME_FIELD_DEFAULT, getResourceTypeName()),
				listener, mWidgets.info.table, mDisabledWhenPublished);


		// Description
		listener = new TextFieldListener(mInvoker) {
			@Override
			protected void onChange(String newText) {
				mActorEditor.setDescription(newText);
			}
		};
		mWidgets.info.description = mUiFactory.addTextArea("Description",
				Messages.replaceName(Messages.Editor.DESCRIPTION_FIELD_DEFAULT, getResourceTypeName()), listener, mWidgets.info.table,
				mDisabledWhenPublished);
	}

	@Override
	protected void showInfoDialog() {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);
		msgBox.setTitle("Info");
		msgBox.content(mWidgets.info.table);
		msgBox.addCancelOkButtonAndKeys("OK", new CDefHasValidName(msgBox, this, mActorEditor, getResourceTypeName()));
		showMsgBox(msgBox);
	}

	/**
	 * Initializes visual options
	 */
	protected void initVisual() {
		mWidgets.visual.hider.addToggleActor(mWidgets.visual.table);
		AlignTable table = mWidgets.visual.table;

		// Starting angle
		mUiFactory.addPanelSection("Starting Direction", table, null);
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setStartingAngle(newValue);
			}
		};
		mWidgets.visual.startAngle = mUiFactory.addSlider("Angle", 0, 360, 1, sliderListener, table,
				Messages.replaceName(Messages.Tooltip.Actor.Visuals.STARTING_ANGLE, getResourceTypeName()), null, mDisabledWhenPublished, mInvoker);


		// Rotation speed
		mUiFactory.addPanelSection("Rotation", table, null);
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setRotationSpeed(newValue);
			}
		};
		mWidgets.visual.rotationSpeed = mUiFactory.addSlider("Speed", Editor.Actor.Visual.ROTATE_SPEED_MIN, Editor.Actor.Visual.ROTATE_SPEED_MAX,
				Editor.Actor.Visual.ROTATE_SPEED_STEP_SIZE, sliderListener, table,
				Messages.replaceName(Messages.Tooltip.Actor.Visuals.ROTATION_SPEED, getResourceTypeName()), null, mDisabledWhenPublished, mInvoker);


		// Different shape tabs
		// Circle
		TabImageWrapper circleTab = mUiFactory.createTabImageWrapper();
		circleTab.imageName = SkinNames.EditorIcons.CIRCLE_SHAPE;
		circleTab.tooltipText = Messages.replaceName(Messages.Tooltip.Actor.Visuals.CIRCLE, getResourceTypeName());
		circleTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				if (mActorEditor.getShapeType() != ActorShapeTypes.CIRCLE) {
					mActorEditor.setShapeType(ActorShapeTypes.CIRCLE);
					mActorEditor.resetCenterOffset();
				}
			}
		};

		// Rectangle
		TabImageWrapper rectangleTab = mUiFactory.createTabImageWrapper();
		rectangleTab.imageName = SkinNames.EditorIcons.RECTANGLE_SHAPE;
		rectangleTab.tooltipText = Messages.replaceName(Messages.Tooltip.Actor.Visuals.RECTANGLE, getResourceTypeName());
		rectangleTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				if (mActorEditor.getShapeType() != ActorShapeTypes.RECTANGLE) {
					mActorEditor.setShapeType(ActorShapeTypes.RECTANGLE);
					mActorEditor.resetCenterOffset();
				}
			}
		};

		// Triangle
		TabImageWrapper triangleTab = mUiFactory.createTabImageWrapper();
		triangleTab.imageName = SkinNames.EditorIcons.TRIANGLE_SHAPE;
		triangleTab.tooltipText = Messages.replaceName(Messages.Tooltip.Actor.Visuals.TRIANGLE, getResourceTypeName());
		triangleTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				if (mActorEditor.getShapeType() != ActorShapeTypes.TRIANGLE) {
					mActorEditor.setShapeType(ActorShapeTypes.TRIANGLE);
					mActorEditor.resetCenterOffset();
				}
			}
		};

		// Custom (draw)
		TabImageWrapper customTab = mUiFactory.createTabImageWrapper();
		customTab.imageName = SkinNames.EditorIcons.DRAW_CUSTOM_SHAPE;
		customTab.tooltipText = Messages.replaceName(Messages.Tooltip.Actor.Visuals.DRAW, getResourceTypeName());
		customTab.hider = new HideListener(true) {
			@Override
			protected void onShow() {
				if (mActorEditor.getShapeType() != ActorShapeTypes.CUSTOM) {
					mActorEditor.setShapeType(ActorShapeTypes.CUSTOM);
					mActorEditor.resetCenterOffset();
				}
			}
		};
		mDrawToolHider = customTab.hider;

		// Create tabs
		@SuppressWarnings("unchecked") ArrayList<TabWrapper> tabs = Pools.arrayList.obtain();
		tabs.add(circleTab);
		tabs.add(rectangleTab);
		tabs.add(triangleTab);
		tabs.add(customTab);
		mUiFactory.addTabs(table, mWidgets.visual.hider, tabs, mDisabledWhenPublished, mInvoker);
		Pools.arrayList.free(tabs);
		tabs = null;

		// Set buttons
		mWidgets.visual.shapeCircle = circleTab.button;
		mWidgets.visual.shapeRectangle = rectangleTab.button;
		mWidgets.visual.shapeTriangle = triangleTab.button;
		mWidgets.visual.shapeCustom = customTab.button;


		// Circle
		// Radius
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeRadius(newValue);
			}
		};
		mWidgets.visual.shapeCircleRadius = mUiFactory.addSlider("Radius", Enemy.Visual.RADIUS_MIN, Enemy.Visual.RADIUS_MAX,
				Enemy.Visual.RADIUS_STEP_SIZE, sliderListener, table, null, circleTab.hider, mDisabledWhenPublished, mInvoker);


		// Rectangle
		// Width
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeWidth(newValue);
				mWidgets.visual.shapeTriangleWidth.setValue(newValue);
			}
		};
		mWidgets.visual.shapeRectangleWidth = mUiFactory.addSlider("Width", Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX,
				Enemy.Visual.SIZE_STEP_SIZE, sliderListener, table, null, rectangleTab.hider, mDisabledWhenPublished, mInvoker);

		// Height
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeWidth(newValue);
				mWidgets.visual.shapeTriangleHeight.setValue(newValue);
			}
		};
		mWidgets.visual.shapeRectangleHeight = mUiFactory.addSlider("Height", Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX,
				Enemy.Visual.SIZE_STEP_SIZE, sliderListener, table, null, rectangleTab.hider, mDisabledWhenPublished, mInvoker);


		// Triangle
		// Width
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeWidth(newValue);
				mWidgets.visual.shapeRectangleWidth.setValue(newValue);
			}
		};
		mWidgets.visual.shapeTriangleWidth = mUiFactory.addSlider("Width", Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE,
				sliderListener, table, null, triangleTab.hider, mDisabledWhenPublished, mInvoker);

		// Height
		sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeWidth(newValue);
				mWidgets.visual.shapeRectangleHeight.setValue(newValue);
			}
		};
		mWidgets.visual.shapeTriangleHeight = mUiFactory.addSlider("Height", Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX,
				Enemy.Visual.SIZE_STEP_SIZE, sliderListener, table, null, triangleTab.hider, mDisabledWhenPublished, mInvoker);
	}

	/**
	 * Initializes the toolbox
	 */
	private void initToolMenu() {
		ButtonGroup buttonGroup = new ButtonGroup();
		ButtonListener buttonListener;

		mDrawToolHider.addToggleActor(mToolMenu);


		// Delete
		mToolMenu.row();
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.DELETE);
				}
			}
		};
		mWidgets.tool.delete = mUiFactory.addToolButton(EditorIcons.DELETE, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.DELETE, getResourceTypeName()), mDisabledWhenPublished);


		// Move
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.MOVE);
				}
			}
		};
		mWidgets.tool.move = mUiFactory.addToolButton(EditorIcons.MOVE, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.MOVE, getResourceTypeName()), mDisabledWhenPublished);


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Terrain draw_append
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.DRAW_APPEND);
				}
			}
		};
		mWidgets.tool.drawAppend = mUiFactory.addToolButton(EditorIcons.TERRAIN_DRAW_APPEND, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.DRAW_APPEND, "terrain"), mDisabledWhenPublished);


		// Terrain draw_erase
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.DRAW_ERASE);
				}
			}
		};
		mWidgets.tool.drawErase = mUiFactory.addToolButton(EditorIcons.DRAW_ERASE, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.DRAW_ERASE, "terrain"), mDisabledWhenPublished);

		// add_move_corner
		mToolMenu.row();
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.ADD_MOVE_CORNER);
				}
			}
		};
		mWidgets.tool.addMoveCorner = mUiFactory.addToolButton(EditorIcons.ADD_MOVE_CORNER, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.ADJUST_ADD_MOVE_CORNER, "terrain"), mDisabledWhenPublished);

		// remove_corner
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.REMOVE_CORNER);
				}
			}
		};
		mWidgets.tool.removeCorner = mUiFactory.addToolButton(EditorIcons.REMOVE_CORNER, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.ADJUST_REMOVE_CORNER, "terrain"), mDisabledWhenPublished);


		// --------- SEPARATOR -----------
		mUiFactory.addToolSeparator(mToolMenu);


		// Set center
		buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.SET_CENTER);
				}
			}
		};
		mWidgets.tool.setCenter = mUiFactory.addToolButton(EditorIcons.SET_CENTER, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.SET_CENTER, getResourceTypeName()), mDisabledWhenPublished);

		// Reset center
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed() {
				mInvoker.execute(new CActorEditorCenterReset(mActorEditor));
			}
		};
		mUiFactory.addToolButton(EditorIcons.RESET_CENTER, buttonListener, buttonGroup, mToolMenu,
				Messages.replaceName(Messages.Tooltip.Tools.RESET_CENTER, getResourceTypeName()), mDisabledWhenPublished);
	}

	/**
	 * Initializes collision options, this is optional
	 */
	protected void initCollision() {
		// Collision damage
		mUiFactory.addPanelSection("Collision Damage", mWidgets.collision.table, null);
		SliderListener sliderListener = new SliderListener() {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setCollisionDamage(newValue);
			}
		};
		mWidgets.collision.damage = mUiFactory.addSlider(null, Editor.Actor.Collision.DAMAGE_MIN, Editor.Actor.Collision.DAMAGE_MAX,
				Editor.Actor.Collision.DAMAGE_STEP_SIZE, sliderListener, mWidgets.collision.table, Messages.Tooltip.Actor.Collision.DAMAGE, null,
				mDisabledWhenPublished, mInvoker);


		// Collision destroy
		mUiFactory.addPanelSection("Collision Destruction", mWidgets.collision.table, null);
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(boolean checked) {
				mActorEditor.setDestroyOnCollide(checked);
			}
		};
		mWidgets.collision.destroyOnCollide = mUiFactory.addCheckBox("Destroy", buttonListener, mWidgets.collision.table,
				Messages.replaceName(Messages.Tooltip.Actor.Collision.DESTROY_ON_COLLIDE, getResourceTypeName()), null, mDisabledWhenPublished);
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
	 * @return collision table
	 */
	protected AlignTable getCollisionTable() {
		return mWidgets.collision.table;
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

		case SET_CENTER:
			mWidgets.tool.setCenter.setChecked(true);
			break;

		default:
			mWidgets.tool.move.setChecked(true);
			break;
		}
	}

	/**
	 * All the widgets which state can be changed and thus reset
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		VisualWidgets visual = new VisualWidgets();
		InfoWidgets info = new InfoWidgets();
		CollisionWidgets collision = new CollisionWidgets();
		ToolWidgets tool = new ToolWidgets();

		/**
		 * Tool widgets
		 */
		static class ToolWidgets {
			Button move = null;
			Button delete = null;
			Button drawAppend = null;
			Button drawErase = null;
			Button addMoveCorner = null;
			Button removeCorner = null;
			Button setCenter = null;
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

			Slider shapeCircleRadius = null;
			Slider shapeTriangleWidth = null;
			Slider shapeTriangleHeight = null;
			Slider shapeRectangleWidth = null;
			Slider shapeRectangleHeight = null;
		}

		/**
		 * General options
		 */
		static class InfoWidgets {
			AlignTable table = new AlignTable();
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

	/** Hides custom draw tool menu */
	private HideListener mDrawToolHider = null;
	/** Invoker */
	protected Invoker mInvoker = null;
	/** Setting widget */
	protected TabWidget mSettingTabs = new TabWidget();

	/** All widget variables */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** The active editor */
	private IActorEditor mActorEditor = null;
}
