package com.spiddekauga.voider;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.scene.ui.Align;
import com.spiddekauga.utils.scene.ui.AlignTable;
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
		//testEnemyEditor();
		//testEditor();
		testStage();
	}

	/**
	 * Testing stage
	 */
	@SuppressWarnings("unused")
	private void testStage() {
		mStage = new Stage();
		AlignTable table = new AlignTable();
		table.setWidth(Gdx.graphics.getWidth());
		table.setHeight(Gdx.graphics.getHeight());
		table.setTableAlign(Align.CENTER | Align.MIDDLE);
		table.setRowAlign(Align.CENTER | Align.MIDDLE);

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


		Button button = new TextButton("New Enemy", textStyle);
		table.add(button);
		button = new TextButton("Save", textStyle);
		table.add(button);
		button = new TextButton("Load", textStyle);
		table.add(button);
		button = new TextButton("Duplicate", textStyle);
		table.add(button);
		mStage.addActor(table);

		// Movement
		table.row();
		Label label = new Label("Movement", labelStyle);
		table.add(label);
		label = new Label("Speed", labelStyle);
		table.add(label);
		table.row();

		// Path
		CheckBox checkBox = new CheckBox("Path", checkBoxStyle);
		buttonGroup.add(checkBox);
		table.add(checkBox);
		checkBox = new CheckBox("Stationary", checkBoxStyle);
		buttonGroup.add(checkBox);
		table.add(checkBox);
		checkBox = new CheckBox("AI", checkBoxStyle);
		buttonGroup.add(checkBox);
		table.add(checkBox);
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

		if (mStage != null) {
			mStage.act();
			mStage.draw();
		}

		GameTime.update(Gdx.graphics.getDeltaTime());
		SceneSwitcher.update();
	}

	@Override
	public void resize(int width, int height) {
		if (mStage != null) {
			mStage.setViewport(width, height, true);
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
}
