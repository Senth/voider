package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
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
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
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
public class EnemyEditor extends WorldScene {
	/**
	 * Creates the enemy editor
	 */
	public EnemyEditor() {
		setEnemyDef();
		createExamplePaths();
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
				if (isButtonPressed(event)) {
					/** @TODO Check if player want to save old actor */

					mDef = new EnemyActorDef();
					setEnemyDef();
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
				if (isButtonPressed(event)) {
					ResourceSaver.save(mDef);
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
		rowTable = new Table();
		Label label = new Label("Movement", labelStyle);
		rowTable.padTop(label.getPrefHeight() * 0.5f);
		rowTable.add(label);
		mGui.add(rowTable);

		// Type of movement?
		// Path
		mGui.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		CheckBoxStyle checkBoxStyle = editorSkin.get("default", CheckBoxStyle.class);
		CheckBox checkBox = new CheckBox("Path", checkBoxStyle);
		buttonGroup.add(checkBox);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event)) {
					addInnerTable(mPathTable, mMovementTable);
					mDef.setMovementType(MovementTypes.PATH);
				}

				return true;
			}
		});
		checkBox.setChecked(mDef.getMovementType() == MovementTypes.PATH);
		rowTable = new Table();
		checkBox.padRight(label.getPrefHeight() * 0.5f);
		rowTable.add(checkBox);

		// Stationary
		checkBox = new CheckBox("Stationary", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event)) {
					addInnerTable(null, mMovementTable);
					mDef.setMovementType(MovementTypes.STATIONARY);
				}
				return true;
			}
		});
		buttonGroup.add(checkBox);
		checkBox.padRight(label.getPrefHeight() * 0.5f);
		checkBox.setChecked(mDef.getMovementType() == MovementTypes.STATIONARY);
		rowTable.add(checkBox);

		// AI
		checkBox = new CheckBox("AI", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event)) {
					addInnerTable(mAiTable, mMovementTable);
					mDef.setMovementType(MovementTypes.AI);
				}
				return true;
			}
		});
		buttonGroup.add(checkBox);
		checkBox.setChecked(mDef.getMovementType() == MovementTypes.AI);
		rowTable.add(checkBox);
		mGui.add(rowTable);
		mGui.row();
		mGui.add(mMovementTable);


		//		// Path Movement
		//		// Once
		//		buttonGroup = new ButtonGroup();
		//		checkBox = new CheckBox("Once", checkBoxStyle);
		//		checkBox.setChecked(mPath.getPathType() == PathTypes.ONCE);
		//		buttonGroup.add(checkBox);
		//		checkBox.addListener(new EventListener() {
		//			@Override
		//			public boolean handle(Event event) {
		//				if (isButtonChecked(event)) {
		//					mPath.setPathType(PathTypes.ONCE);
		//				}
		//				return true;
		//			}
		//		});
		//		checkBox.padRight(label.getPrefHeight() * 0.5f);
		//		mPathTable.add(checkBox);
		//
		//		// Back and Forth
		//		checkBox = new CheckBox("Back & Forth", checkBoxStyle);
		//		checkBox.setChecked(mPath.getPathType() == PathTypes.BACK_AND_FORTH);
		//		checkBox.addListener(new EventListener() {
		//			@Override
		//			public boolean handle(Event event) {
		//				if (isButtonChecked(event)) {
		//					mPath.setPathType(PathTypes.BACK_AND_FORTH);
		//				}
		//				return true;
		//			}
		//		});
		//		checkBox.padRight(label.getPrefHeight() * 0.5f);
		//		buttonGroup.add(checkBox);
		//		mPathTable.add(checkBox);
		//
		//		// Loop
		//		checkBox = new CheckBox("Loop", checkBoxStyle);
		//		checkBox.setChecked(mPath.getPathType() == PathTypes.LOOP);
		//		checkBox.addListener(new EventListener() {
		//			@Override
		//			public boolean handle(Event event) {
		//				mPath.setPathType(PathTypes.LOOP);
		//				return true;
		//			}
		//		});
		//		buttonGroup.add(checkBox);
		//		mPathTable.add(checkBox);
		//
		//		button = new TextButton("Clear path", textStyle);
		//		mPathTable.row();
		//		mPathTable.add();
		//		mPathTable.add(button);

		scaleGui();
	}

	/**
	 * Adds inner table to the outer table
	 * @param innerTable the table to add to the outerTable, if null outerTable will
	 * only be cleared.
	 * @param outerTable the table to clear and then add innerTable to.
	 */
	private static void addInnerTable(Table innerTable, Table outerTable) {
		outerTable.clear();

		if (innerTable != null) {
			outerTable.add(innerTable);
		}

		outerTable.invalidateHierarchy();
	}

	/**
	 * Creates the example paths that are used
	 */
	private void createExamplePaths() {
		// All paths should be like each other, so the player clearly sees the
		// difference between how they work

		// Create a shape like this:
		// 2 ------- 1
		// 3 |     | 0
		Vector2[] nodes = new Vector2[4];
		for (int i = 0; i < nodes.length; ++i) {
			nodes[i] = Pools.obtain(Vector2.class);
		}
		// X-area: From middle of screen to 1/4 of the screen width
		// Y-area: Height of each path should be 1/4. Offset it with 1/20 so it doesn't touch the borders
		Vector2 screenPos = Pools.obtain(Vector2.class);
		float offset = Gdx.graphics.getHeight() * 0.05f;
		// 0
		screenPos.set(Gdx.graphics.getWidth() * 0.5f, offset);
		screenToWorldCoord(mCamera, screenPos, nodes[0], false);
		// 1
		screenPos.y += Gdx.graphics.getHeight() * 0.25f;
		screenToWorldCoord(mCamera, screenPos, nodes[1], false);
		// 2
		screenPos.x = Gdx.graphics.getWidth() * 0.25f;
		screenToWorldCoord(mCamera, screenPos, nodes[2], false);
		// 3
		screenPos.y = offset;
		screenToWorldCoord(mCamera, screenPos, nodes[3], false);


		// ONCE
		mPathOnce.setPathType(PathTypes.ONCE);
		mPathOnce.setWorld(mWorld);
		for (Vector2 node : nodes) {
			mPathOnce.addNodeToBack(node);
		}

		// LOOP
		//		mPathLoop.setPathType(PathTypes.LOOP);
		//		//mPathLoop.setWorld(mWorld);
		//		// Offset all y values so we don't get same path
		//		for (Vector2 node : nodes) {
		//			node.y += Gdx.graphics.getHeight() * 0.25f + offset;
		//			mPathLoop.addNodeToBack(node);
		//		}
		//
		//		// BACK AND FORTH
		//		mPathBackAndForth.setPathType(PathTypes.BACK_AND_FORTH);
		//		//mPathBackAndForth.setWorld(mWorld);
		//		// Offset all y values so we don't get same path
		//		for (Vector2 node : nodes) {
		//			node.y += Gdx.graphics.getHeight() * 0.25f + offset;
		//			mPathBackAndForth.addNodeToBack(node);
		//		}

		// Free stuff
		for (Vector2 node : nodes) {
			Pools.free(node);
		}
		Pools.free(screenPos);
	}

	/**
	 * Sets the definition for the enemy actors. Uses the definition from
	 * mDef.
	 */
	private void setEnemyDef() {
		mEnemyActor.setDef(mDef);
		mEnemyPathOnce.setDef(mDef);
		mEnemyPathLoop.setDef(mDef);
		mEnemyPathBackAndForth.setDef(mDef);
	}

	/** Current enemy actor */
	private EnemyActor mEnemyActor = new EnemyActor();
	/** Enemy actor for path once */
	private EnemyActor mEnemyPathOnce = new EnemyActor();
	/** Enemy actor for path loop */
	private EnemyActor mEnemyPathLoop = new EnemyActor();
	/** Enemy actor for path back and forth */
	private EnemyActor mEnemyPathBackAndForth = new EnemyActor();
	/** Current enemy actor definition */
	private EnemyActorDef mDef = new EnemyActorDef();
	/** If actor has been saved since edit */
	private boolean mActorSavedSinceLastEdit = false;
	/** Display path how once works */
	private Path mPathOnce = new Path();
	/** Display path how loop works */
	private Path mPathLoop = new Path();
	/** Display path how back and forth works */
	private Path mPathBackAndForth = new Path();


	/** Container for the different movement variables */
	private Table mMovementTable = new Table();
	/** Table for path movement */
	private Table mPathTable = new Table();
	/** Table for AI movement */
	private Table mAiTable = new Table();
}
