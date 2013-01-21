package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.WorldScene;

/**
 * Editor for creating and editing enemies
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyEditor extends WorldScene implements EventListener {
	/**
	 * Creates the enemy editor
	 */
	public EnemyEditor() {
		mEnemyActor.setDef(mDef);
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			initGui();
		}
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void loadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.loadAllOf(EnemyActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.unloadAllOf(EnemyActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("EnemyEditor", "UndefinedResourceTypeException: " + e);
		}
	}

	@Override
	public boolean handle(Event event) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Initializes the GUI
	 */
	private void initGui() {
		mGui.align(Align.top | Align.right);
		mGui.setTransform(true);

		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);

		Table rowTable = new Table();
		rowTable.setTransform(true);

		// New Enemy
		Button button = new TextButton("New Enemy", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event.getTarget() instanceof Button) {
					if (((Button)event.getTarget()).isPressed()) {
						/** @TODO Check if player want to save old actor */

						mDef = new EnemyActorDef();
						mEnemyActor.setDef(mDef);
					}
				}
				return true;
			}
		});
		rowTable.add(button);

		// Save
		button = new TextButton("Save", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event.getTarget() instanceof Button) {
					if (((Button)event.getTarget()).isPressed()) {
						ResourceSaver.save(mDef);
					}
				}
				return true;
			}
		});
		rowTable.add(button);

		// Load
		button = new TextButton("Load", textStyle);
		/** @TODO load enemy actor, use browser */
		rowTable.add(button);

		// Duplicate
		button = new TextButton("Duplicate", textStyle);
		/** @TODO duplicate enemy actor, use browser */
		rowTable.add(button);
		mGui.add(rowTable);


		// Movement
		mGui.row();
		Label label = new Label("Movement", labelStyle);
		mGui.add(label);

		// Type of movement?
		mGui.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		CheckBoxStyle checkBoxStyle = editorSkin.get("default", CheckBoxStyle.class);
		CheckBox checkBox = new CheckBox("Path", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				addInnerTableIfChecked(event, mPathTable, mMovementTable);
				return true;
			}
		});
		buttonGroup.add(checkBox);
		rowTable = new Table();
		rowTable.add(checkBox);

		checkBox = new CheckBox("Stationary", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				addInnerTableIfChecked(event, null, mMovementTable);
				return true;
			}
		});
		buttonGroup.add(checkBox);
		rowTable.add(checkBox);

		checkBox = new CheckBox("AI", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				addInnerTableIfChecked(event, mAiTable, mMovementTable);
				return true;
			}
		});
		buttonGroup.add(checkBox);
		rowTable.add(checkBox);
		mGui.add(rowTable);
		mGui.row();
		mGui.add(mMovementTable);

		// Path Movement
		buttonGroup = new ButtonGroup();
		checkBox = new CheckBox("One time", checkBoxStyle);
		buttonGroup.add(checkBox);
		mPathTable.add(checkBox);

		checkBox = new CheckBox("Back & Forth", checkBoxStyle);
		buttonGroup.add(checkBox);
		mPathTable.add(checkBox);

		checkBox = new CheckBox("Loop", checkBoxStyle);
		buttonGroup.add(checkBox);
		mPathTable.add(checkBox);

		button = new TextButton("Set path", textToogleStyle);
		mPathTable.row();
		mPathTable.add(button);

		scaleGui();
	}

	/**
	 * Checks if a button is checked, if it is then it sets the innerTable of
	 * the outerTable.
	 * @param event the event that was fired
	 * @param innerTable the table to add to the outerTable, if null outerTable will
	 * only be cleared.
	 * @param outerTable the table to clear and then add innerTable to.
	 */
	private void addInnerTableIfChecked(Event event, Table innerTable, Table outerTable) {
		if (event.getTarget() instanceof Button) {
			Button button = (Button)event.getTarget();
			if (button.isChecked()) {
				outerTable.clear();

				if (innerTable != null) {
					outerTable.add(innerTable);
				}

				outerTable.invalidateHierarchy();
			}
		}
	}

	/** Current enemy actor */
	private EnemyActor mEnemyActor = new EnemyActor();
	/** Current enemy actor definition */
	private EnemyActorDef mDef = new EnemyActorDef();
	/** If actor has been saved since edit */
	private boolean mActorSavedSinceLastEdit = false;

	/** Container for the different movement variables */
	private Table mMovementTable = new Table();
	/** Table for path movement */
	private Table mPathTable = new Table();
	/** Table for AI movement */
	private Table mAiTable = new Table();
}
