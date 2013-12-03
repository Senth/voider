package com.spiddekauga.voider.app;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the prototype
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PrototypeGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		mSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.BOTTOM);
		mMainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);

		initWindow();
		initEnemies();
	}

	@Override
	public void dispose() {
		super.dispose();
		mWindowTable.dispose();
	}

	/**
	 * Sets the scene
	 * @param scene the prototype scene
	 */
	void setScene(PrototypeScene scene) {
		mScene = scene;
	}

	/**
	 * Initialize window with enemy buttons
	 */
	private void initWindow() {
		if (mWindowTable == null) {
			mWindowTable = new AlignTable();
		}
		mWindowTable.setRowPaddingDefault(2);
		mWindowTable.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mWindowTable.setRowAlign(Horizontal.LEFT, Vertical.TOP);

		Window window = new Window("", mSkin, SkinNames.General.WINDOW_DEFAULT.toString());
		window.setWidth(190);
		window.setHeight(400);
		mWindow = window;
		mMainTable.add(window);

		mWindow.align(Align.center | Align.top);
		mWindow.setName("Window");
		//		window.add(mWindowTable);
	}

	/**
	 * Add all enemies to the window
	 */
	private void initEnemies() {
		ArrayList<EnemyActorDef> enemyDefs = ResourceCacheFacade.getAll(mScene, EnemyActorDef.class);

		int multipleCount = 5;
		int cColumEnemy = 0;
		for (int i = 0; i < multipleCount; ++i) {
			// Here is the actual code for creating an enemy
			for (EnemyActorDef enemyDef : enemyDefs) {
				Button button = new TextButton("", mSkin, SkinNames.General.TEXT_BUTTON_PRESS.toString());

				if (cColumEnemy == ENEMIES_PER_COLUMN) {
					cColumEnemy = 0;
					mWindow.row();
				}

				mWindow.add(button).size(BUTTON_SIZE);
				cColumEnemy++;
			}
		}
	}

	/** General skin */
	private Skin mSkin = null;
	/** The window */
	private Window mWindow = null;
	/** Window table */
	private AlignTable mWindowTable = null;
	/** Scene of the prototype */
	private PrototypeScene mScene = null;

	// Constants for testing
	/** Button size */
	private static final float BUTTON_SIZE = 60;
	/** Enemies per column */
	private static final int ENEMIES_PER_COLUMN = 3;
}
