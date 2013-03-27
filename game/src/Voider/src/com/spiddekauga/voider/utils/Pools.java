package com.spiddekauga.voider.utils;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.Row;

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
	/** Cells for tables */
	public static Pool<Cell> cell = new Pool<Cell>(Cell.class, 16, 250);
	/** Row for tables */
	public static Pool<Row> row = new Pool<Row>(Row.class, 16, 100);
}