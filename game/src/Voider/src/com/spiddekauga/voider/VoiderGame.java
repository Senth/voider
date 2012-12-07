package com.spiddekauga.voider;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.voider.game.ActorDef;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.actors.Types;

/**
 * The main application, i.e. start point
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class VoiderGame implements ApplicationListener {

	@Override
	public void create() {
		mScenes = new Scene[Scenes.values().length];

		mScenes[Scenes.GAME.ordinal()] = new GameScene();

		/** @TODO display splash screen */
		mActiveScene.push(Scenes.GAME.ordinal());

		testMethod();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (mScenes[mActiveScene.getFirst()] != null) {
			//mScenes[mActiveScene.getFirst()].run();
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	/**
	 * Just a test method, remove later
	 * @TODO remove
	 */
	private void testMethod() {
		CircleShape shape = new CircleShape();
		shape.setRadius(2f);
		shape.setPosition(new Vector2(0, 0));
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0.1f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.3f;
		ActorDef actorDef = new ActorDef(100, Types.PLAYER, null, "player", fixtureDef);

		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			SecretKey secretKey = keyGen.generateKey();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * All the different scenes
	 */
	private Scene[] mScenes;
	/**
	 * A stack of the active scenes. This makes it easier to make back
	 * key, return to the previous scene.
	 */
	private LinkedList<Integer> mActiveScene = new LinkedList<Integer>();
}
