package com.spiddekauga.voider.utils;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.voider.game.actors.BulletActor;

/**
 * Common pools used in the program
 */
public class Pools {
/** Vector2 pool */
public static Pool<Vector2> vector2 = new Pool<>(Vector2.class, 16, 500);
/** Cells for tables */
public static Pool<Cell> cell = new Pool<>(Cell.class, 50, 250);
/** Row for tables */
public static Pool<Row> row = new Pool<>(Row.class, 10, 100);
/** Bullets */
public static Pool<BulletActor> bullet = new Pool<>(BulletActor.class, 100, 1000);
/** Time bullets */
public static Pool<TimeBullet> timeBullet = new Pool<>(TimeBullet.class, 100, 1000);
/** Kryo */
public static KryoVoiderPool kryo = new KryoVoiderPool(5, 20);
}
