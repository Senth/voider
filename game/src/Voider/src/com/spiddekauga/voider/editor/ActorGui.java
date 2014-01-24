package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Bullet;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.editor.IActorEditor.Tools;
import com.spiddekauga.voider.editor.commands.CActorEditorCenterReset;
import com.spiddekauga.voider.editor.commands.CDefHasValidName;
import com.spiddekauga.voider.editor.commands.GuiCheckCommandCreator;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Messages;

/**
 * Has some common methods for gui
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorGui extends EditorGui {

	@Override
	public void dispose() {
		mWidgets.visual.table.dispose();
		mWidgets.collision.table.dispose();
		mWidgets.info.table.dispose();
		mWidgets.visualToolMenu.table.dispose();

		mDrawToolHider.dispose();
		mCollisionHider.dispose();
		mVisualHider.dispose();

		super.dispose();
	}

	@Override
	public void initGui() {
		super.initGui();

		mWidgets.collision.table.setPreferences(mMainTable);
		mWidgets.visual.table.setPreferences(mMainTable);
		mWidgets.visualToolMenu.table.setPreferences(mToolMenu);

		initVisual();
		initToolMenu();
		initInfoTable();
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
		mWidgets.info.table.setTableAlign(Horizontal.LEFT, Vertical.TOP);

		Label label = new Label("Name", mStyles.label.standard);
		mWidgets.info.table.add(label);

		int width = (int) (Gdx.graphics.getWidth()*0.4f);

		mWidgets.info.table.row();
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setMaxLength(Config.Editor.NAME_LENGTH_MAX);
		mWidgets.info.table.add(textField).setWidth(width);
		mWidgets.info.name = textField;
		new TooltipListener(textField, "Name", Messages.replaceName(Messages.Tooltip.Actor.Option.NAME, getResourceTypeName()));
		new TextFieldListener(textField, "Name", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mActorEditor.setName(newText);
			}
		};

		mWidgets.info.table.row();
		label = new Label("Description", mStyles.label.standard);
		mWidgets.info.table.add(label);

		mWidgets.info.table.row().setAlign(Horizontal.LEFT, Vertical.TOP);
		textField = new TextField("", mStyles.textField.standard);
		textField.setMaxLength(Config.Editor.DESCRIPTION_LENGTH_MAX);
		mWidgets.info.table.add(textField).setSize(width, (int) (Gdx.graphics.getHeight()*0.5f));
		mWidgets.info.description = textField;
		new TooltipListener(textField, "Description", Messages.replaceName(Messages.Tooltip.Actor.Option.DESCRIPTION, getResourceTypeName()));
		new TextFieldListener(textField, "Write your description here...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mActorEditor.setDescription(newText);
			}
		};
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
		mVisualHider.addToggleActor(mWidgets.visual.table);


		// Starting angle
		Label label = new Label("Starting angle", mStyles.label.standard);
		mWidgets.visual.table.add(label);
		new TooltipListener(label, "Starting angle", Messages.replaceName(Messages.Tooltip.Actor.Visuals.STARTING_ANGLE, getResourceTypeName()));

		mWidgets.visual.table.row();
		Slider slider = new Slider(0, 360, 1, false, mStyles.slider.standard);
		mWidgets.visual.startAngle = slider;
		mWidgets.visual.table.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.visual.table.add(textField);
		new TooltipListener(slider, "Starting angle", Messages.replaceName(Messages.Tooltip.Actor.Visuals.STARTING_ANGLE, getResourceTypeName()));
		new TooltipListener(textField, "Starting angle", Messages.replaceName(Messages.Tooltip.Actor.Visuals.STARTING_ANGLE, getResourceTypeName()));
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setStartingAngle(newValue);
			}
		};


		// Rotation speed
		mWidgets.visual.table.row();
		label = new Label("Rotation speed", mStyles.label.standard);
		new TooltipListener(label, "Rotation speed", Messages.replaceName(Messages.Tooltip.Actor.Visuals.ROTATION_SPEED, getResourceTypeName()));
		mWidgets.visual.table.add(label);

		mWidgets.visual.table.row();
		slider = new Slider(Editor.Actor.Visual.ROTATE_SPEED_MIN, Editor.Actor.Visual.ROTATE_SPEED_MAX, Editor.Actor.Visual.ROTATE_SPEED_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.visual.rotationSpeed = slider;
		mWidgets.visual.table.add(slider);
		textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.visual.table.add(textField);
		new TooltipListener(slider, "Rotation speed", Messages.replaceName(Messages.Tooltip.Actor.Visuals.ROTATION_SPEED, getResourceTypeName()));
		new TooltipListener(textField, "Rotation speed", Messages.replaceName(Messages.Tooltip.Actor.Visuals.ROTATION_SPEED, getResourceTypeName()));
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setRotationSpeed(newValue);
			}
		};


		// Different shapes
		GuiCheckCommandCreator shapeChecker = new GuiCheckCommandCreator(mInvoker);
		mWidgets.visual.table.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		HideListener circleHider = null;
		Button button;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Circle", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.CIRCLE_SHAPE.toString());
		}
		mWidgets.visual.shapeCircle = button;
		mWidgets.visual.table.add(button);
		button.addListener(shapeChecker);
		buttonGroup.add(button);
		new TooltipListener(button, "Circle", Messages.replaceName(Messages.Tooltip.Actor.Visuals.CIRCLE, getResourceTypeName()));
		circleHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				if (mActorEditor.getShapeType() != ActorShapeTypes.CIRCLE) {
					mActorEditor.setShapeType(ActorShapeTypes.CIRCLE);
					mActorEditor.resetCenterOffset();
				}
			}
		};

		HideListener rectangleHider = null;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Rectangle", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.RECTANGLE_SHAPE.toString());
		}
		mWidgets.visual.shapeRectangle = button;
		mWidgets.visual.table.add(button);
		button.addListener(shapeChecker);
		buttonGroup.add(button);
		new TooltipListener(button, "Rectangle", Messages.replaceName(Messages.Tooltip.Actor.Visuals.RECTANGLE, getResourceTypeName()));
		rectangleHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				if (mActorEditor.getShapeType() != ActorShapeTypes.RECTANGLE) {
					mActorEditor.setShapeType(ActorShapeTypes.RECTANGLE);
					mActorEditor.resetCenterOffset();
				}
			}
		};

		HideListener triangleHider = null;
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Triangle", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.TRIANGLE_SHAPE.toString());
		}
		mWidgets.visual.shapeTriangle = button;
		mWidgets.visual.table.add(button);
		button.addListener(shapeChecker);
		buttonGroup.add(button);
		new TooltipListener(button, "Triangle", Messages.replaceName(Messages.Tooltip.Actor.Visuals.TRIANGLE, getResourceTypeName()));
		triangleHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				if (mActorEditor.getShapeType() != ActorShapeTypes.TRIANGLE) {
					mActorEditor.setShapeType(ActorShapeTypes.TRIANGLE);
					mActorEditor.resetCenterOffset();
				}
			}
		};


		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Draw", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.DRAW_CUSTOM_SHAPE.toString());
		}
		mWidgets.visual.shapeCustom = button;
		mWidgets.visual.table.add(button);
		button.addListener(shapeChecker);
		buttonGroup.add(button);
		new TooltipListener(button, "Draw", Messages.replaceName(Messages.Tooltip.Actor.Visuals.DRAW, getResourceTypeName()));
		mDrawToolHider = new HideListener(button, true) {
			@Override
			protected void onShow() {
				if (mActorEditor.getShapeType() != ActorShapeTypes.CUSTOM) {
					mActorEditor.setShapeType(ActorShapeTypes.CUSTOM);
					mActorEditor.resetCenterOffset();
				}
			}
		};


		// Circle
		mWidgets.visual.table.row();
		label = new Label("Radius", mStyles.label.standard);
		mWidgets.visual.table.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		circleHider.addToggleActor(label);

		if (this instanceof EnemyEditorGui) {
			slider = new Slider(Enemy.Visual.RADIUS_MIN, Enemy.Visual.RADIUS_MAX, Enemy.Visual.RADIUS_STEP_SIZE, false, mStyles.slider.standard);
		} else if (this instanceof BulletEditorGui) {
			slider = new Slider(Bullet.Visual.RADIUS_MIN, Bullet.Visual.RADIUS_MAX, Bullet.Visual.RADIUS_STEP_SIZE, false, mStyles.slider.standard);
		}
		mWidgets.visual.shapeCircleRadius = slider;
		mWidgets.visual.table.add(slider);
		circleHider.addToggleActor(slider);

		textField = new TextField("", mStyles.textField.standard);
		mWidgets.visual.table.add(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		circleHider.addToggleActor(textField);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeRadius(newValue);
			}
		};

		// Create shape width (rectangle)
		// Need to create duplicates as two hiders can't use the same element.
		mWidgets.visual.table.row();
		label = new Label("Width", mStyles.label.standard);
		mWidgets.visual.table.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		rectangleHider.addToggleActor(label);

		if (this instanceof EnemyEditorGui) {
			slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, mStyles.slider.standard);
		} else if (this instanceof BulletEditorGui) {
			slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, mStyles.slider.standard);
		}
		mWidgets.visual.shapeRectangleWidth = slider;
		mWidgets.visual.table.add(slider);
		rectangleHider.addToggleActor(slider);

		textField = new TextField("", mStyles.textField.standard);
		mWidgets.visual.table.add(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		rectangleHider.addToggleActor(textField);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeWidth(newValue);
				mWidgets.visual.shapeTriangleWidth.setValue(newValue);
			}
		};

		// Create shape width (triangle)
		// Need to create duplicates as two hiders can't use the same element.
		label = new Label("Width", mStyles.label.standard);
		mWidgets.visual.table.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		triangleHider.addToggleActor(label);

		if (this instanceof EnemyEditorGui) {
			slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, mStyles.slider.standard);
		} else if (this instanceof BulletEditorGui) {
			slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, mStyles.slider.standard);
		}
		mWidgets.visual.shapeTriangleWidth = slider;
		mWidgets.visual.table.add(slider);
		triangleHider.addToggleActor(slider);

		textField = new TextField("", mStyles.textField.standard);
		mWidgets.visual.table.add(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		triangleHider.addToggleActor(textField);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeWidth(newValue);
				mWidgets.visual.shapeRectangleWidth.setValue(newValue);
			}
		};

		// Create shape height (rectangle)
		mWidgets.visual.table.row();
		label = new Label("Height", mStyles.label.standard);
		mWidgets.visual.table.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		rectangleHider.addToggleActor(label);

		if (this instanceof EnemyEditorGui) {
			slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, mStyles.slider.standard);
		} else if (this instanceof BulletEditorGui) {
			slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, mStyles.slider.standard);
		}
		mWidgets.visual.shapeRectangleHeight = slider;
		mWidgets.visual.table.add(slider);
		rectangleHider.addToggleActor(slider);

		textField = new TextField("", mStyles.textField.standard);
		mWidgets.visual.table.add(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		rectangleHider.addToggleActor(textField);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeHeight(newValue);
				mWidgets.visual.shapeTriangleHeight.setValue(newValue);
			}
		};

		// Create shape height (triangle)
		mWidgets.visual.table.row();
		label = new Label("Height", mStyles.label.standard);
		mWidgets.visual.table.add(label).setPadRight(mStyles.vars.paddingAfterLabel);
		triangleHider.addToggleActor(label);

		if (this instanceof EnemyEditorGui) {
			slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, mStyles.slider.standard);
		} else if (this instanceof BulletEditorGui) {
			slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, mStyles.slider.standard);
		}
		mWidgets.visual.shapeTriangleHeight = slider;
		mWidgets.visual.table.add(slider);
		triangleHider.addToggleActor(slider);

		textField = new TextField("", mStyles.textField.standard);
		mWidgets.visual.table.add(textField);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		triangleHider.addToggleActor(textField);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setShapeHeight(newValue);
				mWidgets.visual.shapeRectangleHeight.setValue(newValue);
			}
		};


		mVisualHider.addChild(circleHider);
		mVisualHider.addChild(rectangleHider);
		mVisualHider.addChild(triangleHider);
		mVisualHider.addChild(mDrawToolHider);

		mMainTable.add(mWidgets.visual.table);
	}

	/**
	 * Initializes the toolbox
	 */
	private void initToolMenu() {
		ButtonGroup buttonGroup = new ButtonGroup();
		Button button;
		GuiCheckCommandCreator shapeCustomChecker = new GuiCheckCommandCreator(mInvoker);

		mWidgets.visualToolMenu.table.setPreferences(mToolMenu);
		mToolMenu.add(mWidgets.visualToolMenu.table);
		mDrawToolHider.addToggleActor(mWidgets.visualToolMenu.table);

		// Move
		mWidgets.visualToolMenu.table.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Move", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.MOVE.toString());
		}
		mWidgets.tool.move = button;
		button.addListener(shapeCustomChecker);
		buttonGroup.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, "Move", Messages.replaceName(Messages.Tooltip.Tools.MOVE, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.MOVE);
				}
			}
		};
		mWidgets.visualToolMenu.table.add(button);

		// Delete
		mWidgets.visualToolMenu.table.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Delete", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.DELETE.toString());
		}
		mWidgets.tool.delete = button;
		button.addListener(shapeCustomChecker);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Delete", Messages.replaceName(Messages.Tooltip.Tools.DELETE, getResourceTypeName()));
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.DELETE);
				}
			}
		};
		mWidgets.visualToolMenu.table.add(button);

		// Append
		mWidgets.visualToolMenu.table.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Draw/Append", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.DRAW_APPEND.toString());
		}
		mWidgets.tool.drawAppend = button;
		button.addListener(shapeCustomChecker);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Draw/Append", Messages.replaceName(Messages.Tooltip.Tools.DRAW_APPEND, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.DRAW_APPEND);
				}
			}
		};
		mWidgets.visualToolMenu.table.add(button);

		// Add Remove (draw/erase)
		mWidgets.visualToolMenu.table.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Draw/Erase", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.DRAW_ERASE.toString());
		}
		mWidgets.tool.drawErase = button;
		button.addListener(shapeCustomChecker);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Draw/Erase", Messages.replaceName(Messages.Tooltip.Tools.DRAW_ERASE, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.DRAW_ERASE);
				}
			}
		};
		mWidgets.visualToolMenu.table.add(button);

		// Add corner
		mWidgets.visualToolMenu.table.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Add/Move corner", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.ADD_MOVE_CORNER.toString());
		}
		mWidgets.tool.addMoveCorner = button;
		button.addListener(shapeCustomChecker);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Add/Move corner", Messages.replaceName(Messages.Tooltip.Tools.ADJUST_ADD_MOVE_CORNER, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.ADD_MOVE_CORNER);
				}
			}
		};
		mWidgets.visualToolMenu.table.add(button);

		// Remove corner
		mWidgets.visualToolMenu.table.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Remove corner", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.REMOVE_CORNER.toString());
		}
		mWidgets.tool.removeCorner = button;
		button.addListener(shapeCustomChecker);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Remove corner", Messages.replaceName(Messages.Tooltip.Tools.ADJUST_REMOVE_CORNER, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.REMOVE_CORNER);
				}
			}
		};
		mWidgets.visualToolMenu.table.add(button);

		// Set center
		mWidgets.visualToolMenu.table.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Set center", mStyles.textButton.toggle);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.SET_CENTER.toString());
		}
		mWidgets.tool.setCenter = button;
		button.addListener(shapeCustomChecker);
		buttonGroup.add(button);
		tooltipListener = new TooltipListener(button, "Set center", Messages.replaceName(Messages.Tooltip.Tools.SET_CENTER, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mActorEditor.switchTool(Tools.SET_CENTER);
				}
			}
		};
		mWidgets.visualToolMenu.table.add(button);


		// Reset center
		mWidgets.visualToolMenu.table.row();
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Reset center", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, SkinNames.EditorIcons.RESET_CENTER.toString());
		}
		tooltipListener = new TooltipListener(button, "Reset center", Messages.replaceName(Messages.Tooltip.Tools.RESET_CENTER, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mInvoker.execute(new CActorEditorCenterReset(mActorEditor));
			}
		};
		mWidgets.visualToolMenu.table.add(button);
	}

	/**
	 * Initializes collision options, this is optional
	 */
	protected void initCollision() {
		// Collision damage
		Label label = new Label("Collision Damage", mStyles.label.standard);
		mWidgets.collision.table.add(label);

		mWidgets.collision.table.row();
		Slider slider = new Slider(Editor.Actor.Collision.DAMAGE_MIN, Editor.Actor.Collision.DAMAGE_MAX, Editor.Actor.Collision.DAMAGE_STEP_SIZE, false, mStyles.slider.standard);
		mWidgets.collision.damage = slider;
		mWidgets.collision.table.add(slider);
		TextField textField = new TextField("", mStyles.textField.standard);
		textField.setWidth(mStyles.vars.textFieldNumberWidth);
		mWidgets.collision.table.add(textField);
		new TooltipListener(label, "Collision Damage", Messages.replaceName(Messages.Tooltip.Actor.Collision.DAMAGE, getResourceTypeName()));
		new TooltipListener(textField, "Collision Damage", Messages.replaceName(Messages.Tooltip.Actor.Collision.DAMAGE, getResourceTypeName()));
		new TooltipListener(slider, "Collision Damage", Messages.replaceName(Messages.Tooltip.Actor.Collision.DAMAGE, getResourceTypeName()));
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setCollisionDamage(newValue);
			}
		};

		// Collision destroy
		mWidgets.collision.table.row();
		Button button = new CheckBox("Destroy on collide", mStyles.checkBox.radio);
		mWidgets.collision.destroyOnCollide = button;
		mWidgets.collision.table.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, "Destroy on collide", Messages.replaceName(Messages.Tooltip.Actor.Collision.DESTROY_ON_COLLIDE, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onChecked(boolean checked) {
				mActorEditor.setDestroyOnCollide(checked);
			}
		};

		mMainTable.add(mWidgets.collision.table);
	}

	/**
	 * @return visual table
	 */
	protected AlignTable getVisualTable() {
		return mWidgets.visual.table;
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
		VisualToolMenuWidgets visualToolMenu = new VisualToolMenuWidgets();
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
		 * Visual tool box/menu
		 */
		static class VisualToolMenuWidgets {
			AlignTable table = new AlignTable();
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
			protected AlignTable table = new AlignTable();
		}
	}

	// Hiders
	/** Hides visual table */
	protected HideListener mVisualHider = new HideListener(true);
	/** Hides collision options */
	protected HideListener mCollisionHider = new HideListener(true);
	/** Hides custom draw tool menu */
	private HideListener mDrawToolHider = null;
	/** Invoker */
	protected Invoker mInvoker = null;

	/** All widget variables */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** The active editor */
	private IActorEditor mActorEditor = null;
}
