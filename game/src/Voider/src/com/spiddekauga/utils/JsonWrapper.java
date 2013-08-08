/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.spiddekauga.utils;

import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.Pools;


/** Reads/writes Java objects to/from JSON, automatically. See the wiki for usage:
 * https://code.google.com/p/libgdx/wiki/JsonParsing
 * @author Nathan Sweet
 * @author Matteus Magnusson <matteus.magnusson@gmail.com> Extended
 * the JSON class to support other types, including UUID and any type of
 * keys for maps.*/
public class JsonWrapper extends Json {
	/**
	 * Default constructor. Automatically sets the output type to the
	 * one set in Config
	 * 
	 * Adds ability to serialize
	 * <ul>
	 * <li>UUID</li>
	 * <li>FixtureDef</li>
	 * <li>Shape</li>
	 * </úl>
	 */
	public JsonWrapper() {
		super(Config.JSON_OUTPUT_TYPE);

		addSerializeObjects();
	}

	/**
	 * Adds ability to serialize other objects in the Json file
	 */
	protected void addSerializeObjects() {
		addSerializeUUID();
		addSerializeFixtureDef();
		addSerializeShape();
		addSerializeMap();
	}

	/**
	 * Add ability to serialize UUID
	 */
	private void addSerializeUUID() {
		setSerializer(UUID.class, new Json.Serializer<UUID>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void write(Json json, UUID object, Class knownType) {
				json.writeObjectStart(UUID.class, null);
				writeValue("uuid", object.toString());
				json.writeObjectEnd();
			}

			@SuppressWarnings("rawtypes")
			@Override
			public UUID read(Json json, JsonValue jsonData, Class type) {
				return jsonData.child == null ? null : UUID.fromString(jsonData.child.asString());
			}
		});
	}

	/**
	 * Add ability to serialize FixtureDef
	 */
	private void addSerializeFixtureDef() {
		setSerializer(FixtureDef.class, new Json.Serializer<FixtureDef>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void write(Json json, FixtureDef object, Class knownType) {
				json.writeObjectStart(FixtureDef.class, null);
				writeValue("density", object.density);
				writeValue("friction", object.friction);
				writeValue("isSensor", object.isSensor);
				writeValue("restitution", object.restitution);
				writeValue("filter", object.filter);
				writeValue("shape", object.shape);
				json.writeObjectEnd();
			}

			@SuppressWarnings("rawtypes")
			@Override
			public FixtureDef read(Json json, JsonValue jsonData, Class type) {
				if (jsonData.child == null) {
					return null;
				}

				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.density = readValue("density", float.class, jsonData);
				fixtureDef.friction = readValue("friction", float.class, jsonData);
				fixtureDef.restitution = readValue("restitution", float.class, jsonData);
				fixtureDef.isSensor = readValue("isSensor",  boolean.class, jsonData);
				fixtureDef.shape = readValue("shape", Shape.class, jsonData);

				// Filter
				Filter filter = readValue("filter", Filter.class, jsonData);
				fixtureDef.filter.categoryBits = filter.categoryBits;
				fixtureDef.filter.groupIndex = filter.groupIndex;
				fixtureDef.filter.maskBits = filter.maskBits;

				return fixtureDef;
			}
		});
	}

	/**
	 * Add ability to serialize Shape
	 */
	private void addSerializeShape() {
		setSerializer(Shape.class, new Json.Serializer<Shape>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void write(Json json, Shape object, Class knownType) {
				writeObjectStart(Shape.class, null);
				writeValue("type", object.getType());

				// Shape specific actions
				switch (object.getType()) {
				case Circle:
					CircleShape circle = (CircleShape)object;
					writeValue("position", circle.getPosition());
					writeValue("radius", circle.getRadius());
					break;


				case Polygon: {
					PolygonShape polygon = (PolygonShape)object;
					if (polygon.getVertexCount() >= 3) {
						Vector2[] vertices = new Vector2[polygon.getVertexCount()];
						for (int i = 0; i < polygon.getVertexCount(); ++i) {
							vertices[i] = Pools.vector2.obtain();
							polygon.getVertex(i, vertices[i]);
						}
						writeValue("vertices", vertices);
						for (Vector2 vertex : vertices) {
							Pools.vector2.free(vertex);
						}
					} else {
						writeValue("vertices", (Object)null);
					}
					break;
				}


				case Edge:
					EdgeShape edge = (EdgeShape)object;
					Vector2 tempVector = Pools.vector2.obtain();
					edge.getVertex1(tempVector);
					writeValue("vertex1", tempVector);
					edge.getVertex2(tempVector);
					writeValue("vertex2", tempVector);
					Pools.vector2.free(tempVector);
					break;


				case Chain: {
					ChainShape chainShape = (ChainShape)object;
					if (chainShape.getVertexCount() >= 3) {
						// If first and same vertex is the same, it's a loop
						Vector2 firstVertex = Pools.vector2.obtain();
						Vector2 lastVertex = Pools.vector2.obtain();
						chainShape.getVertex(0, firstVertex);
						chainShape.getVertex(chainShape.getVertexCount() - 1, lastVertex);

						int cVertices = 0;
						if (firstVertex.equals(lastVertex)) {
							cVertices = chainShape.getVertexCount() - 1;
							writeValue("loop", true);
						} else {
							cVertices = chainShape.getVertexCount();
							writeValue("loop", false);
						}

						Vector2[] vertices = new Vector2[cVertices];
						for (int i = 0; i < cVertices; ++i) {
							vertices[i] = Pools.vector2.obtain();
							chainShape.getVertex(i, vertices[i]);
						}
						writeValue("vertices", vertices);
						for (Vector2 vertex : vertices) {
							Pools.vector2.free(vertex);
						}
					} else {
						writeValue("vertices", (Object)null);
					}
					break;
				}
				}

				writeObjectEnd();
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Shape read(Json json, JsonValue jsonData, Class type) {
				if (jsonData.child == null) {
					return null;
				}

				Shape newShape = null;

				// Shape
				Shape.Type shapeType = readValue("type", Shape.Type.class, jsonData);
				switch (shapeType) {
				case Circle:
					float radius = readValue("radius", float.class, jsonData);
					Vector2 position = readValue("position", Vector2.class, jsonData);
					CircleShape circle = new CircleShape();
					newShape = circle;
					circle.setPosition(position);
					circle.setRadius(radius);
					break;


				case Polygon: {
					Vector2[] vertices = readValue("vertices", Vector2[].class, jsonData);
					PolygonShape polygon = new PolygonShape();
					newShape = polygon;
					if (vertices != null) {
						polygon.set(vertices);
					}
					break;
				}


				case Edge:
					Vector2 vertex1 = readValue("vertex1", Vector2.class, jsonData);
					Vector2 vertex2 = readValue("vertex2", Vector2.class, jsonData);
					EdgeShape edge = new EdgeShape();
					newShape = edge;
					edge.set(vertex1, vertex2);
					break;


				case Chain: {
					Vector2[] vertices = readValue("vertices", Vector2[].class, jsonData);
					ChainShape chainShape = new ChainShape();
					newShape = chainShape;
					if (vertices != null) {
						boolean loop = readValue("loop", boolean.class, jsonData);
						if (loop) {
							chainShape.createLoop(vertices);
						} else {
							chainShape.createChain(vertices);
						}
					}
					break;
				}
				}

				return newShape;

			}
		});
	}
}
