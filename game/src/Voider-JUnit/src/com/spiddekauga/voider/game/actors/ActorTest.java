package com.spiddekauga.voider.game.actors;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoPrototypeTest;
import com.spiddekauga.utils.SerializableTaggedFieldSerializer;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.DefTest;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneStub;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;

/**
 * Base class for all actor tests.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorTest {
	/**
	 * Initialize ActorTest.
	 */
	@BeforeClass
	public static void beforeClass() {
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		Config.init();
		ResourceSaver.init();
		ResourceNames.useTestPath();
		ResourceNames.init();
		ResourceCacheFacade.init();
		SceneSwitcher.switchTo(mScene);

		mWorld = new World(new Vector2(), true);
		Actor.setWorld(mWorld);

		mKryo.register(ActorStub.class, new SerializableTaggedFieldSerializer(mKryo, ActorStub.class));
		mKryo.register(ActorDefStub.class, new SerializableTaggedFieldSerializer(mKryo, ActorDefStub.class));
	}

	/**
	 * After class
	 */
	@AfterClass
	public static void afterClass() {
		mWorld.dispose();
		Config.dispose();
		Pools.kryo.free(mKryo);
	}

	/**
	 * Test variables of actor def
	 */
	@Test
	public void testActorDefWriteRead() {
		ActorDefStub actorDef = new ActorDefStub();
		actorDef.setMaxLife(99.99f);
		actorDef.setCollisionDamage(10);
		actorDef.setDestroyOnCollide(true);

		BodyDef bodyDef = actorDef.getBodyDef();
		bodyDef.active = true;
		bodyDef.allowSleep = true;
		bodyDef.angle = 15.0f;
		bodyDef.angularDamping = 3;
		bodyDef.angularVelocity = 17.33f;
		bodyDef.awake = true;
		bodyDef.bullet = true;
		bodyDef.fixedRotation = true;
		bodyDef.gravityScale = 2.111f;
		bodyDef.linearDamping = 66;
		bodyDef.linearVelocity.set(11,12);
		bodyDef.position.set(0.0001f, 0.0002f);
		bodyDef.type = BodyType.StaticBody;

		VisualVars visualVars = actorDef.getVisualVars();
		visualVars.addCorner(new Vector2());
		visualVars.addCorner(new Vector2(1, 0));
		visualVars.addCorner(new Vector2(1, 1));
		visualVars.addCorner(new Vector2(0, 1));
		visualVars.setCenterOffset(new Vector2(77, 67));
		visualVars.setColor(new Color(0.1f, 0.2f, 0.3f, 0.4f));
		visualVars.setShapeHeight(15);
		visualVars.setShapeRadius(3);
		visualVars.setShapeWidth(12);
		visualVars.setShapeType(ActorShapeTypes.RECTANGLE);

		ActorDefStub copyActorDef = KryoPrototypeTest.copy(actorDef, ActorDefStub.class, mKryo);
		testActorDefEquals(actorDef, copyActorDef);
		copyActorDef.dispose();

		copyActorDef = mKryo.copy(actorDef);
		testActorDefEquals(actorDef, copyActorDef);

		actorDef.dispose();
		copyActorDef.dispose();
	}

	/**
	 * Test variables of actor
	 */
	@Test
	public void testActorWriteRead() {
		PickupActorDef actorDef = new PickupActorDef();
		actorDef.setMaxLife(10);
		ActorStub actor = new ActorStub();
		actor.setDef(actorDef);
		actor.decreaseLife(5);
		actor.setPosition(new Vector2(1,1));

		// Need to save and load def
		ResourceSaver.save(actorDef);
		ResourceCacheFacade.load(mScene, actorDef.getId(), PickupActorDef.class, actorDef.getRevision(), false);
		ResourceCacheFacade.finishLoading();

		ActorStub copyActor = KryoPrototypeTest.copy(actor, ActorStub.class, mKryo);
		testActorEquals(actor, copyActor);
		copyActor.dispose();

		copyActor = mKryo.copy(actor);
		testActorEquals(actor, copyActor);

		copyActor.dispose();
		actor.dispose();
		actorDef.dispose();

		ResourceSaver.clearResources(PickupActorDef.class);
	}

	/**
	 * Tests if the two actors have equal variables (not only UUID)
	 * @param expected the original actor
	 * @param actual the copied or loaded actor
	 */
	protected static void testActorEquals(Actor expected, Actor actual) {
		assertEquals(expected.getLife(), actual.getLife(), 0);
		assertEquals(expected.getPosition(), actual.getPosition());
		assertEquals(expected.isActive(), actual.isActive());
		assertEquals(expected.getDef(), actual.getDef());
	}

	/**
	 * Tests if the two actors definitions have equal variables (not only UUID)
	 * @param expected the original actor definition
	 * @param actual the copied or loaded actor definition
	 */
	protected static void testActorDefEquals(ActorDef expected, ActorDef actual) {
		DefTest.testEquals(expected, actual);

		assertEquals(expected.getMaxLife(), actual.getMaxLife(), 0);
		assertEquals(expected.getCollisionDamage(), actual.getCollisionDamage(), 0);
		assertEquals(expected.isDestroyedOnCollide(), actual.isDestroyedOnCollide());

		testBodyDef(expected.getBodyDef(), actual.getBodyDef());
		testVisualVarsEquals(expected.getVisualVars(), actual.getVisualVars());
	}

	/**
	 * Tests body def
	 * @param expected original body def
	 * @param actual copied or loaded body def
	 */
	private static void testBodyDef(BodyDef expected, BodyDef actual) {
		assertEquals(expected.active, actual.active);
		assertEquals(expected.allowSleep, actual.allowSleep);
		assertEquals(expected.angle, actual.angle, 0);
		assertEquals(expected.angularDamping, actual.angularDamping, 0);
		assertEquals(expected.angularVelocity, actual.angularVelocity, 0);
		assertEquals(expected.awake, actual.awake);
		assertEquals(expected.bullet, actual.bullet);
		assertEquals(expected.fixedRotation, actual.fixedRotation);
		assertEquals(expected.gravityScale, actual.gravityScale, 0);
		assertEquals(expected.linearDamping, actual.linearDamping, 0);
		assertEquals(expected.linearVelocity, actual.linearVelocity);
		assertEquals(expected.position, actual.position);
		assertEquals(expected.type, actual.type);
	}

	/**
	 * Tests visual vars
	 * @param expected the original visual vars
	 * @param actual the copied or loaded visual vars
	 */
	private static void testVisualVarsEquals(VisualVars expected, VisualVars actual) {
		assertEquals(expected.getColor(), actual.getColor());
		assertEquals(expected.getShapeType(), actual.getShapeType());
		assertEquals(expected.getShapeRadius(), actual.getShapeRadius(), 0);
		assertEquals(expected.getShapeHeight(), actual.getShapeHeight(), 0);
		assertEquals(expected.getShapeWidth(), actual.getShapeWidth(), 0);
		assertEquals(expected.getCenterOffset(), actual.getCenterOffset());
		assertEquals(expected.getCornerCount(), actual.getCornerCount());
		assertEquals(expected.getCorners(), actual.getCorners());
		assertEquals(expected.getActorType(), actual.getActorType());
	}


	/** World used by actors */
	private static World mWorld = null;
	/** Kryo object to be used for read/writing */
	protected static Kryo mKryo = Pools.kryo.obtain();
	/** Scene used for testing */
	protected static Scene mScene = new SceneStub();
}
