package com.spiddekauga.voider.app;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ResourceTextureButton;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the prototype
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PrototypeGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		mSkin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		mMainTable.setAlignTable(Horizontal.RIGHT, Vertical.BOTTOM);
		mMainTable.setAlignRow(Horizontal.RIGHT, Vertical.TOP);

		initScrollPane();
		initEnemies();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * Initialize window with enemy buttons
	 */
	private void initScrollPane() {
		Table table = new Table();
		mWindowTable = table;
		ScrollPane scrollPane = new ScrollPane(mWindowTable, mSkin, SkinNames.General.SCROLL_PANE_WINDOW_BACKGROUND.toString());
		scrollPane.setWidth(190);
		scrollPane.setHeight(400);
		mMainTable.add(scrollPane);
	}

	/**
	 * Add all enemies to the window
	 */
	private void initEnemies() {
		ArrayList<EnemyActorDef> enemyDefs = ResourceCacheFacade.getAll(ExternalTypes.ENEMY_DEF);

		int multipleCount = 5;
		int cColumEnemy = 0;
		for (int i = 0; i < multipleCount; ++i) {
			// Here is the actual code for creating an enemy
			for (EnemyActorDef enemyDef : enemyDefs) {
				Button button = new ResourceTextureButton(enemyDef, mSkin, SkinNames.General.IMAGE_BUTTON_DEFAULT.toString());

				if (cColumEnemy == ENEMIES_PER_COLUMN) {
					cColumEnemy = 0;
					mWindowTable.row();
				}

				mWindowTable.add(button).size(BUTTON_SIZE);
				cColumEnemy++;
			}
		}
	}

	/** General skin */
	private Skin mSkin = null;
	/** Window table */
	private Table mWindowTable = null;

	// Constants for testing
	/** Button size */
	private static final float BUTTON_SIZE = 60;
	/** Enemies per column */
	private static final int ENEMIES_PER_COLUMN = 3;
}
