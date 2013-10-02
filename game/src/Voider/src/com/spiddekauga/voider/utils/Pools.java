package com.spiddekauga.voider.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.IdentityMap;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.resources.ResourceNames;

/**
 * Common pools used in the program
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Pools {
	/** Vector2 pool */
	public static Pool<Vector2> vector2 = new Pool<Vector2>(Vector2.class, 16, 500);
	/** Color pool */
	public static Pool<Color> color = new Pool<Color>(Color.class, 16, 100);
	/** ArrayList pool */
	@SuppressWarnings("rawtypes")
	public static Pool<ArrayList> arrayList = new Pool<ArrayList>(ArrayList.class, 16, 100);
	/** Hash set */
	@SuppressWarnings("rawtypes")
	public static Pool<HashSet> hashSet = new Pool<HashSet>(HashSet.class, 16, 100);
	/** Identity Hash Map */
	@SuppressWarnings("rawtypes")
	public static Pool<IdentityMap> identityMap = new Pool<IdentityMap>(IdentityMap.class, 6, 50);
	/** Stack */
	@SuppressWarnings("rawtypes")
	public static Pool<Stack> stack = new Pool<Stack>(Stack.class, 5, 20);
	/** Cells for tables */
	public static Pool<Cell> cell = new Pool<Cell>(Cell.class, 50, 250);
	/** Row for tables */
	public static Pool<Row> row = new Pool<Row>(Row.class, 10, 100);
	/** Resource Items */
	public static Pool<ResourceItem> resourceItem = new Pool<ResourceItem>(ResourceItem.class, 50, 400);
	/** Bullets */
	public static Pool<BulletActor> bullet = new Pool<BulletActor>(BulletActor.class, 100, 1000);
	/** Time bullets */
	public static Pool<TimeBullet> timeBullet = new Pool<TimeBullet>(TimeBullet.class, 100, 1000);
	/** Label pool */
	public static Pool<Label> label = new Pool<Label>(Label.class, 10, 100) {
		@Override
		protected Label newObject() {
			Skin skin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
			return new Label("", skin);
		}
	};
	/** Kryo */
	public static KryoPool kryo = new KryoPool(5, 20);
}
