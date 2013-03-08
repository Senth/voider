package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.CommandSequence;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.CheckedListener;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Bullet;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.editor.commands.CActorEditorCenterReset;
import com.spiddekauga.voider.editor.commands.CEditorDuplicate;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorSave;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.DrawActorTool.States;
import com.spiddekauga.voider.scene.Gui;

/**
 * Has some common methods for gui
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorGui extends Gui {

	@Override
	public void resetValues() {
		// Visuals
		mWidgets.visual.startAngle.setValue(mActorEditor.getStartingAngle());
		mWidgets.visual.rotationSpeed.setValue(mActorEditor.getRotationSpeed());

		// Shape
		if (mWidgets.visual.shapeCircleRadius != null) {
			mWidgets.visual.shapeCircleRadius.setValue(mActorEditor.getShapeRadius());
		}
		if (mWidgets.visual.shapeWidth != null) {
			mWidgets.visual.shapeWidth.setValue(mActorEditor.getShapeWidth());
		}
		if (mWidgets.visual.shapeHeight != null) {
			mWidgets.visual.shapeHeight.setValue(mActorEditor.getShapeHeight());
		}
		if (mWidgets.visual.shapeTriangleWidth != null) {
			mWidgets.visual.shapeTriangleWidth.setValue(mActorEditor.getShapeWidth());
		}
		if (mWidgets.visual.shapeTriangleHeight != null) {
			mWidgets.visual.shapeTriangleHeight.setValue(mActorEditor.getShapeHeight());
		}
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

		case LINE:
			mWidgets.visual.shapeLine.setChecked(true);
			break;

		case CUSTOM:
			mWidgets.visual.shapeCustom.setChecked(true);
			break;
		}

		// Custom shape
		if (mWidgets.visual.shapeCustom != null) {
			switch (mActorEditor.getDrawActorToolState()) {
			case ADD_CORNER:
				mWidgets.visual.customShapeAddMove.setChecked(true);
				break;

			case REMOVE:
				mWidgets.visual.customShapeRemove.setChecked(true);
				break;

			case SET_CENTER:
				mWidgets.visual.customShapeSetCenter.setChecked(true);
				break;

			default:
				Gdx.app.error("ActorGui", "Invalid draw actor tool state! " + mActorEditor.getDrawActorToolState());
			}
		}


		// Options
		mWidgets.option.name.setText(mActorEditor.getName());
		mWidgets.option.description.setText(mActorEditor.getDescription());
		mWidgets.option.description.setTextFieldListener(null);


		// Collision
		// If one variable has been initialized, all has...
		if (mWidgets.collision.damage != null) {
			mWidgets.collision.damage.setValue(mActorEditor.getCollisionDamage());
			mWidgets.collision.destroyOnCollide.setChecked(mActorEditor.shallDestroyOnCollide());
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
	protected void initOptions() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);

		mOptionTable.setScalable(false);
		mOptionTable.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mOptionTable.setName("optiontable");
		mOptionTable.setKeepSize(true);

		Label label = new Label("Name", labelStyle);
		mOptionTable.add(label);

		mOptionTable.row().setFillWidth(true);
		TextField textField = new TextField("", textFieldStyle);
		mOptionTable.add(textField).setFillWidth(true);
		mWidgets.option.name = textField;
		new TextFieldListener(textField, "Name", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mActorEditor.setName(newText);
			}
		};

		mOptionTable.row();
		label = new Label("Description", labelStyle);
		mOptionTable.add(label);

		mOptionTable.row().setFillWidth(true).setFillHeight(true).setAlign(Horizontal.LEFT, Vertical.TOP);
		textField = new TextField("shit", textFieldStyle);
		mOptionTable.add(textField).setFillWidth(true).setFillHeight(true);
		mWidgets.option.description = textField;
		new TextFieldListener(textField, "Write your description here...", mInvoker) {
			@Override
			protected void onChange(String newText) {
				mActorEditor.setDescription(newText);
			}
		};
	}

	/**
	 * Initializes file menu
	 * @param actorName name of the actor, this will be displayed in message boxes
	 */
	protected void initFileMenu(final String actorName) {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		final TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);

		// New Enemy
		Button button = new TextButton("New", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mActorEditor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Discard current", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						Command save = new CEditorSave(mActorEditor);
						Command newCommand = new CEditorNew(mActorEditor);
						Command saveAndNew = new CommandSequence(save, newCommand);

						mMsgBox.clear();
						mMsgBox.setTitle("New Enemy");
						mMsgBox.content("Your current " + actorName + " is unsaved.\n" +
								"Do you want to save it before creating a new " + actorName + "?");
						mMsgBox.button(yes, saveAndNew);
						mMsgBox.button(no, newCommand);
						mMsgBox.button(cancel);
						mMsgBox.key(Keys.BACK, null);
						mMsgBox.key(Keys.ESCAPE, null);
						mMsgBox.show(getStage());
					} else {
						mActorEditor.newDef();
					}
				}
				return true;
			}
		});
		mMainTable.add(button);

		// Save
		button = new TextButton("Save", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mActorEditor.saveDef();
				}
				return true;
			}
		});
		mMainTable.add(button);

		// Load
		button = new TextButton("Load", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mActorEditor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Load anyway", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						CommandSequence saveAndLoad = new CommandSequence(new CEditorSave(mActorEditor), new CEditorLoad(mActorEditor));

						mMsgBox.clear();
						mMsgBox.setTitle("Load Enemy");
						mMsgBox.content("Your current " + actorName + " is unsaved.\n" +
								"Do you want to save it before loading another " + actorName + "?");
						mMsgBox.button(yes, saveAndLoad);
						mMsgBox.button(no, new CEditorLoad(mActorEditor));
						mMsgBox.button(cancel);
						mMsgBox.key(Keys.BACK, null);
						mMsgBox.key(Keys.ESCAPE, null);
						mMsgBox.show(getStage());
					} else {
						mActorEditor.loadDef();
					}
				}
				return true;
			}
		});
		mMainTable.add(button);

		// Duplicate
		button = new TextButton("Duplicate", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mActorEditor.isUnsaved()) {
						Button yes = new TextButton("Save first", textStyle);
						Button no = new TextButton("Duplicate anyway", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						CommandSequence saveAndDuplicate = new CommandSequence(new CEditorSave(mActorEditor), new CEditorDuplicate(mActorEditor));

						mMsgBox.clear();
						mMsgBox.setTitle("Load Enemy");
						mMsgBox.content("Your current " + actorName + " is unsaved.\n" +
								"Do you want to save it before duplicating it?");
						mMsgBox.button(yes, saveAndDuplicate);
						mMsgBox.button(no, new CEditorDuplicate(mActorEditor));
						mMsgBox.button(cancel);
						mMsgBox.key(Keys.BACK, null);
						mMsgBox.key(Keys.ESCAPE, null);
						mMsgBox.show(getStage());
					} else {
						mActorEditor.duplicateDef();
					}
				}
				return true;
			}
		});
		mMainTable.add(button);

		// Undo/Redo
		if (mActorEditor.hasUndo()) {
			button = new TextButton("Undo", textStyle);
			button.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (isButtonPressed(event)) {
						mActorEditor.undo();
					}
					return true;
				}
			});
			mMainTable.add(button);

			button = new TextButton("Redo", textStyle);
			button.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (isButtonPressed(event)) {
						mActorEditor.redo();
					}
					return true;
				}
			});
			mMainTable.add(button);
		}
	}

	/**
	 * Initializes visual options
	 * @param actorShapeTypes all shapes that shall be initializes
	 */
	protected void initVisual(ActorShapeTypes... actorShapeTypes) {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = editorSkin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);
		TextButtonStyle toggleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textButtonStyle = editorSkin.get("default", TextButtonStyle.class);

		mVisualTable.setScalable(false);


		// Starting angle
		Label label = new Label("Starting angle", labelStyle);
		mVisualTable.add(label);

		mVisualTable.row();
		Slider slider = new Slider(0, 360, 1, false, sliderStyle);
		mWidgets.visual.startAngle = slider;
		mVisualTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mVisualTable.add(textField);

		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setStartingAngle(newValue);
			}
		};


		// Rotation speed
		mVisualTable.row();
		label = new Label("Rotation speed", labelStyle);
		mVisualTable.add(label);

		mVisualTable.row();
		slider = new Slider(Editor.Actor.Visual.ROTATE_SPEED_MIN, Editor.Actor.Visual.ROTATE_SPEED_MAX, Editor.Actor.Visual.ROTATE_SPEED_STEP_SIZE, false, sliderStyle);
		mWidgets.visual.rotationSpeed = slider;
		mVisualTable.add(slider);
		textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mVisualTable.add(textField);

		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setRotationSpeed(newValue);
			}
		};


		// Different shapes
		GuiCheckCommandCreator shapeChecker = new GuiCheckCommandCreator(mInvoker);
		mVisualTable.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		HideListener circleHider = null;
		if (containsShape(ActorShapeTypes.CIRCLE, actorShapeTypes)) {
			Button button = new TextButton("Circle", toggleStyle);
			mWidgets.visual.shapeCircle = button;
			mVisualTable.add(button);
			button.addListener(shapeChecker);
			buttonGroup.add(button);
			circleHider = new HideListener(button, true) {
				@Override
				protected void onShow() {
					mActorEditor.setShapeType(ActorShapeTypes.CIRCLE);
				}
			};
		}

		HideListener rectangleHider = null;
		boolean hasRectangle = false;
		if (containsShape(ActorShapeTypes.RECTANGLE, actorShapeTypes)) {
			hasRectangle = true;
			Button button = new TextButton("Rect", toggleStyle);
			mWidgets.visual.shapeRectangle = button;
			mVisualTable.add(button);
			button.addListener(shapeChecker);
			buttonGroup.add(button);
			rectangleHider = new HideListener(button, true) {
				@Override
				protected void onShow() {
					mActorEditor.setShapeType(ActorShapeTypes.RECTANGLE);
				}
			};
		}

		HideListener triangleHider = null;
		boolean hasTriangle = false;
		if (containsShape(ActorShapeTypes.TRIANGLE, actorShapeTypes)) {
			hasTriangle = true;
			Button button = new TextButton("Triangle", toggleStyle);
			mWidgets.visual.shapeTriangle = button;
			mVisualTable.add(button);
			button.addListener(shapeChecker);
			buttonGroup.add(button);
			triangleHider = new HideListener(button, true) {
				@Override
				protected void onShow() {
					mActorEditor.setShapeType(ActorShapeTypes.TRIANGLE);
				}
			};
		}

		HideListener lineHider = null;
		boolean hasLine = false;
		if (containsShape(ActorShapeTypes.LINE, actorShapeTypes)) {
			hasLine = true;
			Button button = new TextButton("Line", toggleStyle);
			mWidgets.visual.shapeLine = button;
			mVisualTable.add(button);
			button.addListener(shapeChecker);
			buttonGroup.add(button);
			lineHider = new HideListener(button, true) {
				@Override
				protected void onShow() {
					mActorEditor.setShapeType(ActorShapeTypes.LINE);
				}
			};
		}

		HideListener customHider = null;
		if (containsShape(ActorShapeTypes.CUSTOM, actorShapeTypes)) {
			Button button = new TextButton("Draw", toggleStyle);
			mWidgets.visual.shapeCustom = button;
			mVisualTable.add(button);
			button.addListener(shapeChecker);
			buttonGroup.add(button);
			customHider = new HideListener(button, true) {
				@Override
				protected void onShow() {
					mActorEditor.setShapeType(ActorShapeTypes.CUSTOM);
				}
			};
		}


		// Circle
		if (containsShape(ActorShapeTypes.CIRCLE, actorShapeTypes)) {
			mVisualTable.row();
			label = new Label("Radius", labelStyle);
			mVisualTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
			circleHider.addToggleActor(label);

			// Enemy
			if (this instanceof EnemyEditorGui) {
				slider = new Slider(Enemy.Visual.RADIUS_MIN, Enemy.Visual.RADIUS_MAX, Enemy.Visual.RADIUS_STEP_SIZE, false, sliderStyle);
			} else if (this instanceof BulletEditorGui) {
				slider = new Slider(Bullet.Visual.RADIUS_MIN, Bullet.Visual.RADIUS_MAX, Bullet.Visual.RADIUS_STEP_SIZE, false, sliderStyle);
			}
			mWidgets.visual.shapeCircleRadius = slider;
			mVisualTable.add(slider);
			circleHider.addToggleActor(slider);

			textField = new TextField("", textFieldStyle);
			mVisualTable.add(textField);
			textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
			circleHider.addToggleActor(textField);
			new SliderListener(slider, textField, mInvoker) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeRadius(newValue);
				}
			};
		}

		// Create shape width
		if (hasTriangle || hasRectangle || hasLine) {
			mVisualTable.row();
			label = new Label("Width", labelStyle);
			mVisualTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
			if (hasRectangle) {
				rectangleHider.addToggleActor(label);
			}
			if (hasTriangle) {
				triangleHider.addToggleActor(label);
			}
			if (hasLine) {
				lineHider.addToggleActor(label);
			}

			if (this instanceof EnemyEditorGui) {
				slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			} else if (this instanceof BulletEditorGui) {
				slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			}
			mWidgets.visual.shapeWidth = slider;
			mVisualTable.add(slider);
			if (hasRectangle) {
				rectangleHider.addToggleActor(slider);
			}
			if (hasTriangle) {
				triangleHider.addToggleActor(slider);
			}
			if (hasLine) {
				lineHider.addToggleActor(slider);
			}

			textField = new TextField("", textFieldStyle);
			mVisualTable.add(textField);
			textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
			if (hasRectangle) {
				rectangleHider.addToggleActor(textField);
			}
			if (hasTriangle) {
				triangleHider.addToggleActor(textField);
			}
			if (hasLine) {
				lineHider.addToggleActor(textField);
			}
			new SliderListener(slider, textField, mInvoker) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeWidth(newValue);
				}
			};
		}

		// Create shape height
		if (hasRectangle || hasTriangle) {
			mVisualTable.row();
			label = new Label("Height", labelStyle);
			mVisualTable.add(label).setPadRight(Editor.LABEL_PADDING_BEFORE_SLIDER);
			if (hasRectangle) {
				rectangleHider.addToggleActor(label);
			}
			if (hasTriangle) {
				triangleHider.addToggleActor(label);
			}

			if (this instanceof EnemyEditorGui) {
				slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			} else if (this instanceof BulletEditorGui) {
				slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			}
			mWidgets.visual.shapeHeight = slider;
			mVisualTable.add(slider);
			if (hasRectangle) {
				rectangleHider.addToggleActor(slider);
			}
			if (hasTriangle) {
				triangleHider.addToggleActor(slider);
			}

			textField = new TextField("", textFieldStyle);
			mVisualTable.add(textField);
			textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
			if (hasRectangle) {
				rectangleHider.addToggleActor(textField);
			}
			if (hasTriangle) {
				triangleHider.addToggleActor(textField);
			}
			new SliderListener(slider, textField, mInvoker) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeHeight(newValue);
				}
			};
		}

		// Custom
		if (containsShape(ActorShapeTypes.CUSTOM, actorShapeTypes)) {
			mVisualTable.row();
			buttonGroup = new ButtonGroup();

			// Add/Move
			GuiCheckCommandCreator shapeCustomChecker = new GuiCheckCommandCreator(mInvoker);
			Button button = new TextButton("Add/Move", toggleStyle);
			button.addListener(shapeCustomChecker);
			button.setName("add/move");
			mWidgets.visual.customShapeAddMove = button;
			buttonGroup.add(button);
			customHider.addToggleActor(button);
			new CheckedListener(button) {
				@Override
				protected void onChange(boolean checked) {
					if (checked) {
						mActorEditor.setDrawActorToolState(States.ADD_CORNER);
					}
				}
			};
			mVisualTable.add(button);

			// Remove
			button = new TextButton("Remove", toggleStyle);
			button.addListener(shapeCustomChecker);
			button.setName("remove");
			mWidgets.visual.customShapeRemove = button;
			buttonGroup.add(button);
			customHider.addToggleActor(button);
			new CheckedListener(button) {
				@Override
				protected void onChange(boolean checked) {
					if (checked) {
						mActorEditor.setDrawActorToolState(States.REMOVE);
					}
				}
			};
			mVisualTable.add(button);

			// Set center
			button = new TextButton("Set center", toggleStyle);
			button.addListener(shapeCustomChecker);
			button.setName("set center");
			mWidgets.visual.customShapeSetCenter = button;
			HideListener setCenterHider = new HideListener(button, true);
			buttonGroup.add(button);
			customHider.addToggleActor(button);
			customHider.addChild(setCenterHider);
			new CheckedListener(button) {
				@Override
				protected void onChange(boolean checked) {
					if (checked) {
						mActorEditor.setDrawActorToolState(States.SET_CENTER);
					}
				}
			};
			mVisualTable.add(button);


			mVisualTable.row(Horizontal.RIGHT, Vertical.TOP);
			button = new TextButton("Reset center", textButtonStyle);
			setCenterHider.addToggleActor(button);
			button.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (isButtonPressed(event)) {
						mInvoker.execute(new CActorEditorCenterReset(mActorEditor));
					}
					return true;
				}
			});
			mVisualTable.add(button);
		}


		if (circleHider != null) {
			mVisualHider.addChild(circleHider);
		}
		if (rectangleHider != null) {
			mVisualHider.addChild(rectangleHider);
		}
		if (triangleHider != null) {
			mVisualHider.addChild(triangleHider);
		}
		if (lineHider != null) {
			mVisualHider.addChild(lineHider);
		}
		if (customHider != null) {
			mVisualHider.addChild(customHider);
		}
	}

	/**
	 * Initializes collision options, this is optional
	 */
	protected void initCollision() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		LabelStyle labelStyle = skin.get("default", LabelStyle.class);
		SliderStyle sliderStyle = skin.get("default", SliderStyle.class);
		TextFieldStyle textFieldStyle = skin.get("default", TextFieldStyle.class);
		CheckBoxStyle checkBoxStyle = skin.get("default", CheckBoxStyle.class);

		// Collision damage
		Label label = new Label("Collision Damage", labelStyle);
		mCollisionTable.add(label);

		mCollisionTable.row();
		Slider slider = new Slider(Editor.Actor.Collision.DAMAGE_MIN, Editor.Actor.Collision.DAMAGE_MAX, Editor.Actor.Collision.DAMAGE_STEP_SIZE, false, sliderStyle);
		mWidgets.collision.damage = slider;
		mCollisionTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Editor.TEXT_FIELD_NUMBER_WIDTH);
		mCollisionTable.add(textField);
		new SliderListener(slider, textField, mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setCollisionDamage(newValue);
			}
		};

		// Collision destroy
		mCollisionTable.row();
		Button button = new CheckBox("Destroy on collide", checkBoxStyle);
		mWidgets.collision.destroyOnCollide = button;
		mCollisionTable.add(button);
		new CheckedListener(button) {
			@Override
			protected void onChange(boolean checked) {
				mActorEditor.setDestroyOnCollide(checked);
			}
		};
	}

	/**
	 * All the widgets which state can be changed and thus reset
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		VisualWidgets visual = new VisualWidgets();
		OptionWidgets option = new OptionWidgets();
		CollisionWidgets collision = new CollisionWidgets();

		/**
		 * Visual widgets
		 */
		static class VisualWidgets {
			Slider startAngle = null;
			Slider rotationSpeed = null;

			// Shapes
			Button shapeCircle = null;
			Button shapeTriangle = null;
			Button shapeRectangle = null;
			Button shapeLine = null;
			Button shapeCustom = null;

			Slider shapeCircleRadius = null;
			Slider shapeTriangleWidth = null;
			Slider shapeTriangleHeight = null;
			Slider shapeWidth = null;
			Slider shapeHeight = null;
			Slider shapeLineLength = null;

			// Custom shape
			Button customShapeAddMove = null;
			Button customShapeRemove = null;
			Button customShapeSetCenter = null;
		}

		/**
		 * General options
		 */
		static class OptionWidgets {
			TextField name = null;
			TextField description = null;
		}

		/**
		 * Collision options
		 */
		static class CollisionWidgets {
			Slider damage = null;
			Button destroyOnCollide = null;
		}
	}

	/**
	 * Checks whether the specified array contains the specified shape
	 * @param shape the shape to look for
	 * @param array the array to look in for the shape
	 * @return true if the array contains shape
	 */
	private boolean containsShape(ActorShapeTypes shape, ActorShapeTypes[] array) {
		for (ActorShapeTypes currentShape : array) {
			if (currentShape == shape) {
				return true;
			}
		}

		return false;
	}

	// Tables
	/** Container for all visual options */
	protected AlignTable mVisualTable = new AlignTable();
	/** Container for all general options, such as name description. */
	protected AlignTable mOptionTable = new AlignTable();
	/** Container for all collision options */
	protected AlignTable mCollisionTable = new AlignTable();

	// Hiders
	/** Hides visual table */
	protected HideListener mVisualHider = new HideListener(true);
	/** Hides options options :D:D:D */
	protected HideListener mOptionHider = new HideListener(true);
	/** Hides collision options */
	protected HideListener mCollisionHider = new HideListener(true);
	/** Invoker */
	protected Invoker mInvoker = null;

	/** All widget variables */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** The active editor */
	private IActorEditor mActorEditor = null;
}
