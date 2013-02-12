package com.spiddekauga.voider.scene;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.CommandSequence;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.Config.Editor.Bullet;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.editor.BulletEditorGui;
import com.spiddekauga.voider.editor.EnemyEditorGui;
import com.spiddekauga.voider.editor.IActorEditor;
import com.spiddekauga.voider.editor.commands.AeDuplicate;
import com.spiddekauga.voider.editor.commands.AeLoad;
import com.spiddekauga.voider.editor.commands.AeNew;
import com.spiddekauga.voider.editor.commands.AeSave;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;

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


		// Options
		mWidgets.option.name.setText(mActorEditor.getName());
		mWidgets.option.description.setText(mActorEditor.getDescription());
		mWidgets.option.description.setTextFieldListener(null);
	}

	/**
	 * Sets the actor editor for this GUI
	 * @param actorEditor editor bound to this GUI
	 */
	protected void setActorEditor(IActorEditor actorEditor) {
		mActorEditor = actorEditor;
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
		new TextFieldListener(textField, "Name") {
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
		new TextFieldListener(textField, "Write your description here...") {
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

		TextButtonStyle textToggleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		final TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);

		// New Enemy
		Button button = new TextButton("New", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					if (mActorEditor.isUnsaved()) {
						Button yes = new TextButton("Yes", textStyle);
						Button no = new TextButton("No", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						CommandSequence saveAndNew = new CommandSequence(new AeSave(mActorEditor), new AeNew(mActorEditor));

						mMsgBox.clear();
						mMsgBox.setTitle("New Enemy");
						mMsgBox.content("Your current " + actorName + " is unsaved.\n" +
								"Do you want to save it before creating a new " + actorName + "?");
						mMsgBox.button(yes, saveAndNew);
						mMsgBox.button(no, new AeNew(mActorEditor));
						mMsgBox.button(cancel);
						mMsgBox.show(getStage());
					} else {
						mActorEditor.newActor();
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
					mActorEditor.saveActor();
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
						Button yes = new TextButton("Yes", textStyle);
						Button no = new TextButton("No", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						CommandSequence saveAndLoad = new CommandSequence(new AeSave(mActorEditor), new AeLoad(mActorEditor));

						mMsgBox.clear();
						mMsgBox.setTitle("Load Enemy");
						mMsgBox.content("Your current " + actorName + " is unsaved.\n" +
								"Do you want to save it before loading another " + actorName + "?");
						mMsgBox.button(yes, saveAndLoad);
						mMsgBox.button(no, new AeLoad(mActorEditor));
						mMsgBox.button(cancel);
						mMsgBox.show(getStage());
					} else {
						mActorEditor.loadActor();
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
						Button yes = new TextButton("Yes", textStyle);
						Button no = new TextButton("No", textStyle);
						Button cancel = new TextButton("Cancel", textStyle);

						CommandSequence saveAndDuplicate = new CommandSequence(new AeSave(mActorEditor), new AeDuplicate(mActorEditor));

						mMsgBox.clear();
						mMsgBox.setTitle("Load Enemy");
						mMsgBox.content("Your current " + actorName + " is unsaved.\n" +
								"Do you want to save it before duplicating it?");
						mMsgBox.button(yes, saveAndDuplicate);
						mMsgBox.button(no, new AeDuplicate(mActorEditor));
						mMsgBox.button(cancel);
						mMsgBox.show(getStage());
					} else {
						mActorEditor.duplicateActor();
					}
				}
				return true;
			}
		});
		mMainTable.add(button);
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

		mVisualTable.setScalable(false);


		// Starting angle
		Label label = new Label("Starting angle", labelStyle);
		mVisualTable.add(label);

		mVisualTable.row();
		Slider slider = new Slider(0, 360, 1, false, sliderStyle);
		mWidgets.visual.startAngle = slider;
		mVisualTable.add(slider);
		TextField textField = new TextField("", textFieldStyle);
		textField.setWidth(Enemy.TEXT_FIELD_NUMBER_WIDTH);
		mVisualTable.add(textField);

		new SliderListener(slider, textField) {
			@Override
			protected void onChange(float newValue) {
				mActorEditor.setStartingAngle(newValue);
			}
		};


		// Different shapes
		mVisualTable.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		HideListener circleHider = null;
		if (containsShape(ActorShapeTypes.CIRCLE, actorShapeTypes)) {
			Button button = new TextButton("Circle", toggleStyle);
			mWidgets.visual.shapeCircle = button;
			mVisualTable.add(button);
			buttonGroup.add(button);
			circleHider = new HideListener(button, true) {
				@Override
				protected void onShow() {
					mActorEditor.setShapeType(ActorShapeTypes.CIRCLE);
				}
			};
		}

		HideListener rectangleHider = null;
		if (containsShape(ActorShapeTypes.RECTANGLE, actorShapeTypes)) {
			Button button = new TextButton("Rect", toggleStyle);
			mWidgets.visual.shapeRectangle = button;
			mVisualTable.add(button);
			buttonGroup.add(button);
			rectangleHider = new HideListener(button, true) {
				@Override
				protected void onShow() {
					mActorEditor.setShapeType(ActorShapeTypes.RECTANGLE);
				}
			};
		}

		HideListener triangleHider = null;
		if (containsShape(ActorShapeTypes.TRIANGLE, actorShapeTypes)) {
			Button button = new TextButton("Triangle", toggleStyle);
			mWidgets.visual.shapeTriangle = button;
			mVisualTable.add(button);
			buttonGroup.add(button);
			triangleHider = new HideListener(button, true) {
				@Override
				protected void onShow() {
					mActorEditor.setShapeType(ActorShapeTypes.TRIANGLE);
				}
			};
		}

		HideListener lineHider = null;
		if (containsShape(ActorShapeTypes.LINE, actorShapeTypes)) {
			Button button = new TextButton("Line", toggleStyle);
			mWidgets.visual.shapeLine = button;
			mVisualTable.add(button);
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
			mVisualTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
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
			textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
			circleHider.addToggleActor(textField);
			new SliderListener(slider, textField) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeRadius(newValue);
				}
			};
		}


		// Rectangle
		if (containsShape(ActorShapeTypes.RECTANGLE, actorShapeTypes)) {
			mVisualTable.row();
			label = new Label("Width", labelStyle);
			mVisualTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
			rectangleHider.addToggleActor(label);

			if (this instanceof EnemyEditorGui) {
				slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			} else if (this instanceof BulletEditorGui) {
				slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			}
			mWidgets.visual.shapeRectangleWidth = slider;
			mVisualTable.add(slider);
			rectangleHider.addToggleActor(slider);

			textField = new TextField("", textFieldStyle);
			mVisualTable.add(textField);
			textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
			rectangleHider.addToggleActor(textField);
			new SliderListener(slider, textField) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeWidth(newValue);
					if (mWidgets.visual.shapeTriangleWidth != null) {
						mWidgets.visual.shapeTriangleWidth.setValue(newValue);
					}
					if (mWidgets.visual.shapeLineLength != null) {
						mWidgets.visual.shapeLineLength.setValue(newValue);
					}
				}
			};

			mVisualTable.row();
			label = new Label("Height", labelStyle);
			mVisualTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
			rectangleHider.addToggleActor(label);

			if (this instanceof EnemyEditorGui) {
				slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			} else if (this instanceof BulletEditorGui) {
				slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			}
			mWidgets.visual.shapeRectangleHeight = slider;
			mVisualTable.add(slider);
			rectangleHider.addToggleActor(slider);

			textField = new TextField("", textFieldStyle);
			mVisualTable.add(textField);
			textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
			rectangleHider.addToggleActor(textField);
			new SliderListener(slider, textField) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeHeight(newValue);
					if (mWidgets.visual.shapeTriangleHeight != null) {
						mWidgets.visual.shapeTriangleHeight.setValue(newValue);
					}
				}
			};
		}


		// Triangle
		if (containsShape(ActorShapeTypes.TRIANGLE, actorShapeTypes)) {
			mVisualTable.row();
			label = new Label("Width", labelStyle);
			mVisualTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
			triangleHider.addToggleActor(label);

			if (this instanceof EnemyEditorGui) {
				slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			} else if (this instanceof BulletEditorGui) {
				slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			}
			mWidgets.visual.shapeTriangleWidth = slider;
			mVisualTable.add(slider);
			triangleHider.addToggleActor(slider);

			textField = new TextField("", textFieldStyle);
			mVisualTable.add(textField);
			textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
			triangleHider.addToggleActor(textField);
			new SliderListener(slider, textField) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeWidth(newValue);
					if (mWidgets.visual.shapeRectangleWidth != null) {
						mWidgets.visual.shapeRectangleWidth.setValue(newValue);
					}
					if (mWidgets.visual.shapeLineLength != null) {
						mWidgets.visual.shapeLineLength.setValue(newValue);
					}
				}
			};

			if (this instanceof EnemyEditorGui) {
				slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			} else if (this instanceof BulletEditorGui) {
				slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			}
			mVisualTable.row();
			label = new Label("Height", labelStyle);
			mVisualTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
			triangleHider.addToggleActor(label);

			mWidgets.visual.shapeTriangleHeight = slider;
			mVisualTable.add(slider);
			triangleHider.addToggleActor(slider);

			textField = new TextField("", textFieldStyle);
			mVisualTable.add(textField);
			textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
			triangleHider.addToggleActor(textField);
			new SliderListener(slider, textField) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeHeight(newValue);
					if (mWidgets.visual.shapeRectangleHeight != null) {
						mWidgets.visual.shapeRectangleHeight.setValue(newValue);
					}
				}
			};
		}

		// Line
		if (containsShape(ActorShapeTypes.LINE, actorShapeTypes)) {
			mVisualTable.row();
			label = new Label("Width", labelStyle);
			mVisualTable.add(label).setPadRight(Enemy.LABEL_PADDING_BEFORE_SLIDER);
			lineHider.addToggleActor(label);

			if (this instanceof EnemyEditorGui) {
				slider = new Slider(Enemy.Visual.SIZE_MIN, Enemy.Visual.SIZE_MAX, Enemy.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			} else if (this instanceof BulletEditorGui) {
				slider = new Slider(Bullet.Visual.SIZE_MIN, Bullet.Visual.SIZE_MAX, Bullet.Visual.SIZE_STEP_SIZE, false, sliderStyle);
			}
			mWidgets.visual.shapeLineLength = slider;
			mVisualTable.add(slider);
			lineHider.addToggleActor(slider);

			textField = new TextField("", textFieldStyle);
			mVisualTable.add(textField);
			textField.setWidth(Editor.Enemy.TEXT_FIELD_NUMBER_WIDTH);
			lineHider.addToggleActor(textField);
			new SliderListener(slider, textField) {
				@Override
				protected void onChange(float newValue) {
					mActorEditor.setShapeWidth(newValue);
					if (mWidgets.visual.shapeRectangleWidth != null) {
						mWidgets.visual.shapeRectangleWidth.setValue(newValue);
					}
					if (mWidgets.visual.shapeTriangleWidth != null) {
						mWidgets.visual.shapeTriangleWidth.setValue(newValue);
					}
				}
			};
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
	 * All the widgets which state can be changed and thus reset
	 */
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		VisualWidgets visual = new VisualWidgets();
		OptionWidgets option = new OptionWidgets();

		/**
		 * Visual widgets
		 */
		static class VisualWidgets {
			Slider startAngle = null;

			// Shapes
			Button shapeCircle = null;
			Button shapeTriangle = null;
			Button shapeRectangle = null;
			Button shapeLine = null;
			Button shapeCustom = null;

			Slider shapeCircleRadius = null;
			Slider shapeTriangleWidth = null;
			Slider shapeTriangleHeight = null;
			Slider shapeRectangleWidth = null;
			Slider shapeRectangleHeight = null;
			Slider shapeLineLength = null;
		}

		/**
		 * General options
		 */
		static class OptionWidgets {
			TextField name = null;
			TextField description = null;
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

	// Hiders
	protected HideListener mVisualHider = new HideListener(true);
	/** Hides options options :D:D:D */
	protected HideListener mOptionHider = new HideListener(true);

	/** All widget variables */
	private InnerWidgets mWidgets = new InnerWidgets();
	/** The active editor */
	private IActorEditor mActorEditor = null;
}
