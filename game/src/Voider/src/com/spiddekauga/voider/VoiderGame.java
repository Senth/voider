package com.spiddekauga.voider;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * The main application, i.e. start point
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class VoiderGame implements ApplicationListener {

	/** SKIN LOCATION, CHANGE THIS! */
	private static final String SKIN_LOCATION = "ui/editor.json";

	@Override
	public void create() {
		// Init various classes
		Config.init();
		ResourceSaver.init();
		ResourceCacheFacade.init();


		/** @TODO set main menu as start screen */

		//testGame();
		testEnemyEditor();
		//testEditor();
		//testStage();
	}

	/**
	 * Testing stage
	 */
	@SuppressWarnings("unused")
	private void testStage() {
		mStage = new Stage();
		FileHandle skinFile = Gdx.files.internal(SKIN_LOCATION);
		Skin skin = new Skin(skinFile);
		ButtonStyle buttonStyle = skin.get("add", ButtonStyle.class);
		ImageButtonStyle imageStyle = skin.get("add", ImageButtonStyle.class);
		TextButtonStyle textStyle = skin.get("default", TextButtonStyle.class);

		// TOP LEFT - Does not scale button (9patch image)
		// Probably same error as BOTTOM LEFT
		Button button = new Button(buttonStyle);
		button.setTransform(true);
		button.setPosition(0, 350);
		button.setScale(0.5f);
		button.invalidate();
		mStage.addActor(button);


		// TOP MIDDLE - Can be scaled appropriately with setSize, the 9patch
		// scales appropriately (9patch image)
		button = new Button(buttonStyle);
		button.setTransform(true);
		button.setPosition(350, 350);
		button.setSize(button.getPrefWidth()*0.5f, button.getPrefHeight()*0.5f);
		button.invalidate();
		mStage.addActor(button);


		// TOP RIGHT - TABLE Cannot scale button in any way...
		// Should be same size as TOP MIDDLE
		// Border is 9patch, inner image is regular png
		// ---
		// Testing scaling with some text buttons too
		// I know scaling text isn't good, but this displays that the button
		// itself isn't scaling, just the text
		Table tableScale = new Table();
		button = new ImageButton(imageStyle);
		button.setTransform(true);
		button.setScale(0.5f);
		button.invalidate();
		tableScale.setTransform(true);
		tableScale.add(button);
		tableScale.row();
		button = new TextButton("Upscaled", textStyle);
		button.setTransform(true);
		button.setScale(2.0f);
		button.invalidate();
		tableScale.add(button);
		tableScale.row();
		button = new TextButton("Downscaled", textStyle);
		button.setTransform(true);
		button.setScale(0.5f);
		button.invalidate();
		tableScale.add(button);

		tableScale.align(Align.top | Align.right);
		tableScale.setHeight(480);
		tableScale.setPosition(800, 0);
		tableScale.invalidateHierarchy();
		mStage.addActor(tableScale);



		// BOTTOM LEFT - Image Button, only inner image is scaled with setScale
		// Same with TextButton and probably other buttons too
		// Border is 9patch, inner image is regular png
		button = new ImageButton(imageStyle);
		button.setTransform(true);
		button.setScale(0.5f);
		mStage.addActor(button);


		// BOTTOM MIDDLE - Image Button, use of setSize scales the image appropriately
		// Border is 9patch, inner image is regular png
		button = new ImageButton(imageStyle);
		button.setTransform(true);
		button.setPosition(350, 0);
		button.setSize(button.getPrefWidth()*0.5f, button.getPrefHeight()*0.5f);
		button.invalidate();
		mStage.addActor(button);


		// BOTTOM RIGHT - TABLE Cannot scale button using setSize
		// Border is 9patch, inner image is regular png
		tableScale = new Table();
		button = new ImageButton(imageStyle);
		button.setTransform(true);
		button.setSize(button.getPrefWidth()*0.5f, button.getPrefWidth()*0.5f);
		button.invalidate();
		tableScale.setTransform(true);
		tableScale.add(button);
		tableScale.row();
		button = new TextButton("Upscaled", textStyle);
		button.setTransform(true);
		button.setSize(button.getPrefWidth()*2.0f, button.getPrefWidth()*2.0f);
		button.invalidate();
		tableScale.add(button);
		tableScale.row();
		button = new TextButton("Downscaled", textStyle);
		button.setTransform(true);
		button.setSize(button.getPrefWidth()*0.5f, button.getPrefWidth()*0.5f);
		button.invalidate();
		tableScale.add(button);

		tableScale.align(Align.bottom | Align.right);
		tableScale.setPosition(800, 0);
		tableScale.invalidateHierarchy();
		mStage.addActor(tableScale);


		// CENTER - scaling the table with buttonImage does not produce the same
		// result as scaling the button (the border gets scaled)
		// Should be the same as TOP middle or BOTTOM middle
		// Border is 9patch, inner image is regular png
		tableScale = new Table();
		button = new ImageButton(imageStyle);
		button.setTransform(true);
		tableScale.setTransform(true);
		tableScale.add(button);
		tableScale.align(Align.center);
		tableScale.setPosition(400, 240);
		tableScale.setScale(0.5f);
		tableScale.invalidateHierarchy();
		mStage.addActor(tableScale);

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
