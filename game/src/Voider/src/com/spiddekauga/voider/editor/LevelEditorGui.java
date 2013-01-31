package com.spiddekauga.voider.editor;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.CheckedListener;
import com.spiddekauga.voider.editor.LevelEditor.PickupTools;
import com.spiddekauga.voider.editor.LevelEditor.StaticTerrainTools;
import com.spiddekauga.voider.editor.LevelEditor.Tools;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the level editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class LevelEditorGui extends Gui {
	/**
	 * Sets the level editor this GUI will act on.
	 * @param levelEditor the scene this GUI will act on
	 */
	public void setLevelEditor(LevelEditor levelEditor) {
		mLevelEditor = levelEditor;
	}

	@Override
	public void initGui() {
		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);
		mMenuTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMenuTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);
		mPickupTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mPickupTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);
		mStaticTerrainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mStaticTerrainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);

		initPickup();
		initStaticTerrain();
		initMenu();

		mMenuTable.setTransform(true);
		mMenuTable.invalidate();
		mMainTable.setTransform(true);
		mMainTable.invalidate();

		switchTool(mStaticTerrainTable);
	}

	/**
	 * Initializes the main menu
	 */
	private void initMenu() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);

		mMenuTable = new AlignTable();
		mMenuTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);
		ButtonGroup toggleGroup = new ButtonGroup();
		Button button = new TextButton("Static Terrain", textToogleStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.STATIC_TERRAIN);
					switchTool(mStaticTerrainTable);
				}
			}
		};
		toggleGroup.add(button);
		mMenuTable.add(button);
		mMenuTable.row();

		button = new TextButton("Pickup", textToogleStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.switchTool(Tools.PICKUP);
					switchTool(mPickupTable);
				}
			}
		};
		toggleGroup.add(button);
		mMenuTable.add(button);
		mMenuTable.row();

		button = new TextButton("SAVE", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mLevelEditor.save();
				}
				return true;
			}
		});
		mMenuTable.add(button);
		mMenuTable.row();

		button = new TextButton("LOAD", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mLevelEditor.load();
				}
				return true;
			}
		});
		mMenuTable.add(button);
		mMenuTable.row();

		button = new TextButton("NEW", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mLevelEditor.newLevel();
				}
				return true;
			}
		});
		mMenuTable.add(button);
		mMenuTable.row();

		button = new TextButton("RUN", textStyle);
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (isButtonPressed(event)) {
					mLevelEditor.runFromHere();
				}
				return true;
			}
		});
		mMenuTable.add(button);

		mMainTable.add(mMenuTable);
		mMainTable.setTransform(true);

		mMainTable.invalidate();
	}

	/**
	 * Initializes Pickup tool GUI
	 */
	private void initPickup() {
		mPickupTable.setName(Tools.PICKUP.toString() + "-table");
		mPickupTable.setRowAlign(Horizontal.RIGHT, Vertical.BOTTOM);

		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle textStyle = editorSkin.get("toggle", TextButtonStyle.class);

		ButtonGroup toggleGroup = new ButtonGroup();
		Button button = new TextButton("25HP", textStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupTool(PickupTools.ADD_HEALTH_25);
				}
			}
		};
		toggleGroup.add(button);
		mPickupTable.add(button);
		mPickupTable.row();

		button = new TextButton("50HP", textStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupTool(PickupTools.ADD_HEALTH_50);
				}
			}
		};
		toggleGroup.add(button);
		mPickupTable.add(button);
		mPickupTable.row();

		button = new TextButton("Remove", textStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupTool(PickupTools.REMOVE);
				}
			}
		};
		toggleGroup.add(button);
		mPickupTable.add(button);
		mPickupTable.row();

		button = new TextButton("Move", textStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setPickupTool(PickupTools.MOVE);
				}
			}
		};
		toggleGroup.add(button);
		mPickupTable.add(button);


		mPickupTable.setTransform(true);
		mPickupTable.invalidate();
	}

	/**
	 * Initializes the static terrain
	 */
	private void initStaticTerrain() {
		mStaticTerrainTable.setName(Tools.STATIC_TERRAIN.toString() + "-table");
		mStaticTerrainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);

		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
		TextButtonStyle textStyle = editorSkin.get(StaticTerrainTools.MOVE.getStyleName(), TextButtonStyle.class);
		ImageButtonStyle imageStyle = editorSkin.get(StaticTerrainTools.ADD.getStyleName(), ImageButtonStyle.class);

		ButtonGroup toggleGroup = new ButtonGroup();
		Button button = new ImageButton(imageStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainTool(StaticTerrainTools.ADD);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);
		mStaticTerrainTable.row();

		button = new TextButton("Remove", textStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainTool(StaticTerrainTools.REMOVE);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);
		mStaticTerrainTable.row();

		button = new TextButton("Move", textStyle);
		new CheckedListener(button) {
			@Override
			public void onChange(boolean checked) {
				if (checked) {
					mLevelEditor.setStaticTerrainTool(StaticTerrainTools.MOVE);
				}
			}
		};
		toggleGroup.add(button);
		mStaticTerrainTable.add(button);


		mStaticTerrainTable.setTransform(true);
		mStaticTerrainTable.invalidate();
	}

	/**
	 * Switches the GUI to the selected tool
	 * @param toolTable the tool's table we want to activate
	 */
	private void switchTool(AlignTable toolTable) {
		mMainTable.clear();
		toolTable.invalidate();
		mMainTable.add(toolTable);
		mMainTable.add(mMenuTable);
		mMainTable.invalidate();
	}


	/** Wrapper for what tool is currently active */
	private AlignTable mMenuTable = new AlignTable();
	/** Pickup table */
	private AlignTable mPickupTable = new AlignTable();
	/** Static terrain table */
	private AlignTable mStaticTerrainTable = new AlignTable();

	/** Level editor the GUI will act on */
	private LevelEditor mLevelEditor = null;
}
