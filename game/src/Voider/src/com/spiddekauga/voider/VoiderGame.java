package com.spiddekauga.voider;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.voider.editor.BulletEditor;
import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * The main application, i.e. start point
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class VoiderGame implements ApplicationListener {

	@Override
	public void create() {
		// Init various classes
		Config.init();
		ResourceSaver.init();
		ResourceCacheFacade.init();


		/** @TODO set main menu as start screen */

		//testGame();
		//		testBulletEditor();
		testEnemyEditor();
		//		testEditor();
		//testStage();
	}

	/**
	 * Testing stage
	 */
	@SuppressWarnings("unused")
	private void testStage() {
		mStage = new Stage();
		table = new AlignTable();
		table.setWidth(Gdx.graphics.getWidth());
		table.setHeight(Gdx.graphics.getHeight());
		table.setTableAlign(Horizontal.CENTER, Vertical.MIDDLE);
		table.setRowAlign(Horizontal.LEFT, Vertical.BOTTOM);
		mStage.addActor(table);

		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.finishLoading();
		} catch (UndefinedResourceTypeException e) {
			e.printStackTrace();
		}
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);
		ButtonGroup buttonGroup = new ButtonGroup();
		CheckBoxStyle checkBoxStyle = editorSkin.get("default", CheckBoxStyle.class);
		ImageButtonStyle imageStyle = editorSkin.get("add", ImageButtonStyle.class);

		Row row = table.row();
		Button button = new TextButton("New Enemy", textStyle);
		Cell cell = table.add(button);
		button = new TextButton("Save", textStyle);
		table.add(button);
		button = new TextButton("Load", textStyle);
		cell = table.add(button);
		cell.setAlign(Horizontal.RIGHT, Vertical.MIDDLE);
		button = new TextButton("Duplicate", textStyle);
		cell = table.add(button);
		cell.setAlign(Horizontal.LEFT, Vertical.MIDDLE);

		row = table.row(Horizontal.LEFT, Vertical.TOP);
		row.setPadBottom(20);
		button = new TextButton("New Enemy", textStyle);
		cell = table.add(button);
		button = new TextButton("Save", textStyle);
		table.add(button);
		button = new TextButton("Load", textStyle);
		cell = table.add(button);
		button = new TextButton("Duplicate", textStyle);
		cell = table.add(button);

		row = table.row();
		button = new TextButton("New Enemy", textStyle);
		cell = table.add(button);
		button = new TextButton("Save", textStyle);
		table.add(button);
		button = new TextButton("Load", textStyle);
		cell = table.add(button);
		button = new TextButton("Duplicate", textStyle);
		cell = table.add(button);

		table.row();
		button = new ImageButton(imageStyle);
		cell = table.add(button);
		button = new TextButton("test", textStyle);
		cell = table.add(button);

		table.setTransform(true);
		table.invalidate();
		Gdx.input.setInputProcessor(mStage);
	}

	/**
	 * Testing the editor
	 */
	private void testEditor() {
		LevelEditor levelEditor = new LevelEditor();

		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		levelEditor.setLevel(level);
		SceneSwitcher.switchTo(levelEditor);
	}

	/**
	 * Testing the enemy editor
	 */
	private void testEnemyEditor() {
		SceneSwitcher.switchTo(new EnemyEditor());
	}

	/**
	 * Testing the bullet editor
	 */
	private void testBulletEditor() {
		SceneSwitcher.switchTo(new BulletEditor());
	}

	/**
	 * testing to start a game
	 */
	@SuppressWarnings("unused")
	private void testGame() {
		GameScene gameScene = new GameScene(false);

		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		gameScene.setLevel(level);
		SceneSwitcher.switchTo(gameScene);
	}

	@Override
	public void dispose() {
		ResourceCacheFacade.dispose();
		Config.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		GameTime.updateGlobal(Gdx.graphics.getDeltaTime());

		if (mStage != null) {
			mStage.act();
			mStage.draw();
		}

		SceneSwitcher.update();
	}

	@Override
	public void resize(int width, int height) {
		if (mStage != null) {
			mStage.setViewport(width, height, true);
			table.setWidth(width);
			table.setHeight(height);
		}
		SceneSwitcher.resize(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	/** Testing stage */
	private Stage mStage = null;
	/** Testing table */
	private AlignTable table = null;
}
