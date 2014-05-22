package com.spiddekauga.voider.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.IdentityMap;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.network.entities.RevisionEntity;
import com.spiddekauga.voider.repo.InternalNames;
import com.spiddekauga.voider.repo.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;

/**
 * Common pools used in the program
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Pools {
	/** Vector2 pool */
	public static Pool<Vector2> vector2 = new Pool<>(Vector2.class, 16, 500);
	/** Color pool */
	public static Pool<Color> color = new Pool<>(Color.class, 16, 100);
	/** ArrayList pool */
	@SuppressWarnings("rawtypes")
	public static Pool<ArrayList> arrayList = new Pool<>(ArrayList.class, 16, 100);
	/** Hash set */
	@SuppressWarnings("rawtypes")
	public static Pool<HashSet> hashSet = new Pool<>(HashSet.class, 16, 100);
	/** Hash map */
	@SuppressWarnings("rawtypes")
	public static Pool<HashMap> hashMap = new Pool<>(HashMap.class, 16, 100);
	/** Identity Hash Map */
	@SuppressWarnings("rawtypes")
	public static Pool<IdentityMap> identityMap = new Pool<>(IdentityMap.class, 6, 50);
	/** Stack */
	@SuppressWarnings("rawtypes")
	public static Pool<Stack> stack = new Pool<>(Stack.class, 5, 20);
	/** Cells for tables */
	public static Pool<Cell> cell = new Pool<>(Cell.class, 50, 250);
	/** Row for tables */
	public static Pool<Row> row = new Pool<>(Row.class, 10, 100);
	/** Resource Items */
	public static Pool<ResourceItem> resourceItem = new Pool<>(ResourceItem.class, 50, 400);
	/** Bullets */
	public static Pool<BulletActor> bullet = new Pool<>(BulletActor.class, 100, 1000);
	/** Time bullets */
	public static Pool<TimeBullet> timeBullet = new Pool<>(TimeBullet.class, 100, 1000);
	/** Label pool */
	public static com.badlogic.gdx.utils.Pool<Label> label = new com.badlogic.gdx.utils.Pool<Label>(10, 100) {
		@Override
		protected Label newObject() {
			Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
			return new Label("", skin);
		}
	};
	/** Kryo */
	public static KryoVoiderPool kryo = new KryoVoiderPool(5, 20);
	/** Revision information */
	public static Pool<RevisionEntity> revisionInfo = new Pool<>(RevisionEntity.class);
}
