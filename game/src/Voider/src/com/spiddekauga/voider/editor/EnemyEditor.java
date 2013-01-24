package com.spiddekauga.voider.editor;

import java.lang.reflect.Field;

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
import com.badlogic.gdx.utils.SnapshotArray;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.actors.PlayerActor;
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
		//		Actor.setEditorActive(true);
		mPlayerActor = new PlayerActor();
		setEnemyDef();
		createExamplePaths();

		try {
			mfEnemyOnceReachEnd = EnemyActor.class.getDeclaredField("mPathOnceReachedEnd");
			mfEnemyOnceReachEnd.setAccessible(true);
		} catch (Exception e) {
			Gdx.app.error("EnemyEditor", "Could not access mPathOnceReachEnd");
		}
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			initGui();
		}
		Actor.setPlayerActor(mPlayerActor);
	}

	@Override
	public void update() {
		super.update();

		switch (mDef.getMovementType()) {
		case AI:
		case STATIONARY:
			mEnemyActor.update(Gdx.graphics.getDeltaTime());
			break;

		case PATH:
			mEnemyPathLoop.update(Gdx.graphics.getDeltaTime());
			mEnemyPathOnce.update(Gdx.graphics.getDeltaTime());
			mEnemyPathBackAndForth.update(Gdx.graphics.getDeltaTime());

			// Reset Once enemy ever 4 seconds
			if (mfEnemyOnceReachEnd != null) {
				try {
					if ((Boolean)mfEnemyOnceReachEnd.get(mEnemyPathOnce)) {
						if (mEnemyPathOnceOutOfBoundsTime != 0.0f) {
							if (mEnemyPathOnceOutOfBoundsTime + Config.Editor.ENEMY_ONCE_RESET_TIME <= GameTime.getTotalTimeElapsed()) {
								mEnemyPathOnce.resetPathMovement();
								mEnemyPathOnceOutOfBoundsTime = 0.0f;
							}
						} else {
							mEnemyPathOnceOutOfBoundsTime = GameTime.getTotalTimeElapsed();
						}
					}
				} catch (Exception e) {
					Gdx.app.error("EnemyEditor", "Could not access mPathOnceReachEnd");
				}
			}
			break;
		}
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);
		scalePathLabels();
	}

	@Override
	public void onDisposed() {
		mPlayerActor.dispose();
		mEnemyActor.dispose();
		mEnemyPathBackAndForth.dispose();
		mEnemyPathLoop.dispose();
		mEnemyPathOnce.dispose();
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

		// Speed
		rowTable = new Table();
		label = new Label("Speed", labelStyle);
		rowTable.add(label);
		rowTable.padRight(mGui.getPrefWidth() - label.getPrefWidth());
		mGui.row();
		mGui.add(rowTable);

		// Type of movement?
		MovementTypes movementType = mDef.getMovementType();
		mDef.setMovementType(null);
		// Path
		mGui.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		CheckBoxStyle checkBoxStyle = editorSkin.get("default", CheckBoxStyle.class);
		CheckBox checkBox = new CheckBox("Path", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event) && mDef.getMovementType() != MovementTypes.PATH) {
					addInnerTable(mPathTable, mMovementTable);
					mStage.addActor(mPathLabels);
					mDef.setMovementType(MovementTypes.PATH);
					mEnemyActor.destroyBody();
					createPathBodies();
				}

				return true;
			}
		});
		buttonGroup.add(checkBox);
		checkBox.setChecked(movementType == MovementTypes.PATH);
		rowTable = new Table();
		checkBox.padRight(label.getPrefHeight() * 0.5f);
		rowTable.add(checkBox);


		// Stationary
		checkBox = new CheckBox("Stationary", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event) && mDef.getMovementType() != MovementTypes.STATIONARY) {
					addInnerTable(null, mMovementTable);
					mDef.setMovementType(MovementTypes.STATIONARY);
					clearExamplePaths();
					createEnemyBody();
				}
				return true;
			}
		});
		buttonGroup.add(checkBox);
		checkBox.padRight(label.getPrefHeight() * 0.5f);
		checkBox.setChecked(movementType == MovementTypes.STATIONARY);
		rowTable.add(checkBox);

		// AI
		checkBox = new CheckBox("AI", checkBoxStyle);
		checkBox.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonChecked(event) && mDef.getMovementType() != MovementTypes.AI) {
					addInnerTable(mAiTable, mMovementTable);
					mDef.setMovementType(MovementTypes.AI);
					clearExamplePaths();
					createEnemyBody();
				}
				return true;
			}
		});
		buttonGroup.add(checkBox);
		checkBox.setChecked(movementType == MovementTypes.AI);
		rowTable.add(checkBox);
		mGui.add(rowTable);
		mGui.row();
		mGui.add(mMovementTable);

		// Labels for movement
		label = new Label("Back and Forth", labelStyle);
		Table wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		label = new Label("Loop", labelStyle);
		wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();

		label = new Label("Once", labelStyle);
		wrapTable = new Table();
		wrapTable.add(label);
		mPathLabels.add(wrapTable);
		mPathLabels.row();


		scalePathLabels();
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
		Vector2[] screenPos = new Vector2[4];
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i] = Pools.obtain(Vector2.class);
			nodes[i] = Pools.obtain(Vector2.class);
		}
		// X-area: From middle of screen to 1/6 of the screen width
		// Y-area: Height of each path should be 1/5. Offset it with 1/20 so it doesn't touch the borders
		float spaceBetween = Gdx.graphics.getHeight() * 0.1f;
		float height = Gdx.graphics.getHeight() * 0.2f;
		float heightOffset = height + spaceBetween;
		float initialOffset = spaceBetween;
		// 0
		screenPos[0].set(Gdx.graphics.getWidth() * 0.5f, initialOffset + height);
		// 1
		screenPos[1].set(screenPos[0]);
		screenPos[1].y = initialOffset;
		// 2
		screenPos[2].set(screenPos[1]);
		screenPos[2].x = Gdx.graphics.getWidth() / 6f;
		// 3
		screenPos[3].set(screenPos[2]);
		screenPos[3].y = initialOffset + height;


		// BACK AND FORTH
		mPathBackAndForth.setPathType(PathTypes.BACK_AND_FORTH);
		mEnemyPathBackAndForth.setPath(mPathBackAndForth);
		for (int i = 0; i < nodes.length; ++i) {
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			mPathBackAndForth.addNodeToBack(nodes[i]);
		}

		// LOOP
		mPathLoop.setPathType(PathTypes.LOOP);
		mEnemyPathLoop.setPath(mPathLoop);
		// Offset all y values so we don't get same path
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i].y += heightOffset;
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			mPathLoop.addNodeToBack(nodes[i]);
		}

		// ONCE
		mPathOnce.setPathType(PathTypes.ONCE);
		mEnemyPathOnce.setPath(mPathOnce);
		// Offset all y values so we don't get same path
		for (int i = 0; i < nodes.length; ++i) {
			screenPos[i].y += heightOffset;
			screenToWorldCoord(mCamera, screenPos[i], nodes[i], true);
			mPathOnce.addNodeToBack(nodes[i]);
		}

		// Free stuff
		for (int i = 0; i < nodes.length; ++i) {
			Pools.free(nodes[i]);
			Pools.free(screenPos[i]);
		}
	}

	/**
	 * Creates enemy bodies of the paths
	 */
	private void createPathBodies() {
		mPathOnce.setWorld(mWorld);
		mPathLoop.setWorld(mWorld);
		mPathBackAndForth.setWorld(mWorld);
		mEnemyPathOnce.createBody();
		mEnemyPathOnce.resetPathMovement();
		mEnemyPathLoop.createBody();
		mEnemyPathLoop.resetPathMovement();
		mEnemyPathBackAndForth.createBody();
		mEnemyPathBackAndForth.resetPathMovement();
		mEnemyPathOnceOutOfBoundsTime = 0f;
	}

	/**
	 * Clears all the example paths
	 */
	private void clearExamplePaths() {
		mPathOnce.setWorld(null);
		mPathLoop.setWorld(null);
		mPathBackAndForth.setWorld(null);
		mEnemyPathOnce.destroyBody();
		mEnemyPathLoop.destroyBody();
		mEnemyPathBackAndForth.destroyBody();

		// Clear GUI text
		mStage.clear();
		mStage.addActor(mGui);
	}

	/**
	 * Scale label for paths
	 */
	private void scalePathLabels() {
		float spaceBetween = Gdx.graphics.getHeight() * 0.1f;
		float height = Gdx.graphics.getHeight() * 0.2f;
		float initialOffset = spaceBetween + height * 0.5f + spaceBetween + height;

		mPathLabels.setPosition(Gdx.graphics.getWidth() / 3f, initialOffset);



		// Fix padding
		SnapshotArray<com.badlogic.gdx.scenes.scene2d.Actor> actors = mPathLabels.getChildren();
		// Reset padding first
		for (int i = 0; i < actors.size - 1; ++i) {
			if (actors.get(i) instanceof Table) {
				Table table = (Table) actors.get(i);
				table.padBottom(0);
				table.invalidateHierarchy();
			}
		}

		for (int i = 0; i < actors.size - 1; ++i) {
			if (actors.get(i) instanceof Table) {
				Table table = (Table) actors.get(i);
				table.padBottom(spaceBetween + height - table.getPrefHeight());
				table.invalidateHierarchy();
			}
		}
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

	/**
	 * Create enemy test body (for AI and STATIONARY)
	 */
	private void createEnemyBody() {
		if (mEnemyActor.getBody() == null) {
			mEnemyActor.createBody();
		}
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
	/** When the ONCE enemy path actor was removed */
	private float mEnemyPathOnceOutOfBoundsTime = 0.0f;
	/** Field for accessing when the ONCE enemy actor reached the end */
	private Field mfEnemyOnceReachEnd = null;
	/** Player actor, for the enemies to work properly */
	private PlayerActor mPlayerActor = null;


	/** Container for the different movement variables */
	private Table mMovementTable = new Table();
	/** Table for path movement */
	private Table mPathTable = new Table();
	/** Table for path lables, these are added directly to the stage */
	private Table mPathLabels = new Table();
	/** Table for AI movement */
	private Table mAiTable = new Table();
}