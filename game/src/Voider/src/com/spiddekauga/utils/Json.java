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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.SerializationException;
import com.spiddekauga.voider.utils.Pools;

/**
 * Reads/writes Java objects to/from JSON, automatically.
 * @author Nathan Sweet
 * @author Matteus Magnusson <matteus.magnusson@gmail.com> Extended
 * the JSON class to support other types, including UUID and any type of
 * keys for maps.
 */
@SuppressWarnings("javadoc")
public class Json {
	/**
	 * Default constructor, sets the output to minimal.
	 */
	public Json () {
		outputType = OutputType.minimal;
	}

	/**
	 * Constructs a Json with the specified output type
	 * @param outputType how the json should be output
	 */
	public Json (OutputType outputType) {
		this.outputType = outputType;
	}

	/**
	 * If we shall ignore unknown fields
	 * @param ignoreUnknownFields set to true if we shall ignore unknown fields
	 */
	public void setIgnoreUnknownFields (boolean ignoreUnknownFields) {
		this.ignoreUnknownFields = ignoreUnknownFields;
	}

	/**
	 * Sets the output type of the json, e.g. minimal.
	 * @param outputType how the json should be output
	 */
	public void setOutputType (OutputType outputType) {
		this.outputType = outputType;
	}

	/**
	 * Adds a class tag for the type
	 * @param tag string version of the class
	 * @param type class type of the class
	 */
	public void addClassTag (String tag, Class<?> type) {
		tagToClass.put(tag, type);
		classToTag.put(type, tag);
	}

	/**
	 * Returns the class of the specified tag
	 * @param tag string "tag" version of the class
	 * @return class type
	 * @throws SerializationException if the class type for the tag wasn't found
	 */
	public Class<?> getClass (String tag) {
		Class<?> type = tagToClass.get(tag);
		if (type != null) {
			return type;
		}
		try {
			return Class.forName(tag);
		} catch (ClassNotFoundException ex) {
			throw new SerializationException(ex);
		}
	}

	/**
	 * @param type class type
	 * @return tag of the specified class type
	 */
	public String getTag (Class<?> type) {
		String tag = classToTag.get(type);
		if (tag != null) {
			return tag;
		}
		return type.getName();
	}

	/**
	 * Sets the name of the JSON field to store the Java class name or class tag when required to avoid ambiguity during
	 * deserialization. Set to null to never output this information, but be warned that deserialization may fail.
	 * @param typeName name of the class
	 * */
	public void setTypeName (String typeName) {
		this.typeName = typeName;
	}

	/**
	 * Sets th serializer to use for the specifid class type
	 * @param <T> Class to serialize
	 * @param type Class to serialize
	 * @param serializer
	 */
	public <T> void setSerializer (Class<T> type, Serializer<T> serializer) {
		classToSerializer.put(type, serializer);
	}

	/**
	 * @param <T> class type
	 * @param type class type
	 * @return serializer for the specified type
	 */
	@SuppressWarnings("unchecked")
	public <T> Serializer<T> getSerializer (Class<T> type) {
		return (Serializer<T>) classToSerializer.get(type);
	}

	/**
	 * If we shall use prototypes
	 * @param usePrototypes set to true to use prototypes
	 */
	public void setUsePrototypes (boolean usePrototypes) {
		this.usePrototypes = usePrototypes;
	}

	public void setElementType (Class<?> type, String fieldName, Class<?> elementType) {
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) {
			fields = cacheFields(type);
		}
		FieldMetadata metadata = fields.get(fieldName);
		if (metadata == null) {
			throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
		}
		metadata.elementType = elementType;
	}

	private ObjectMap<String, FieldMetadata> cacheFields (Class<?> type) {
		ArrayList<Field> allFields = new ArrayList<Field>();
		Class<?> nextClass = type;
		while (nextClass != Object.class) {
			Collections.addAll(allFields, nextClass.getDeclaredFields());
			nextClass = nextClass.getSuperclass();
		}

		ObjectMap<String, FieldMetadata> nameToField = new ObjectMap<String, FieldMetadata>();
		for (int i = 0, n = allFields.size(); i < n; i++) {
			Field field = allFields.get(i);

			int modifiers = field.getModifiers();
			if (Modifier.isTransient(modifiers)) {
				continue;
			}
			if (Modifier.isStatic(modifiers)) {
				continue;
			}
			if (field.isSynthetic()) {
				continue;
			}

			if (!field.isAccessible()) {
				try {
					field.setAccessible(true);
				} catch (AccessControlException ex) {
					continue;
				}
			}

			nameToField.put(field.getName(), new FieldMetadata(field));
		}
		typeToFields.put(type, nameToField);
		return nameToField;
	}

	public String toJson (Object object) {
		return toJson(object, object == null ? null : object.getClass(), (Class<?>)null);
	}

	public String toJson (Object object, Class<?> knownType) {
		return toJson(object, knownType, (Class<?>)null);
	}

	/** @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public String toJson (Object object, Class<?> knownType, Class<?> elementType) {
		StringWriter buffer = new StringWriter();
		toJson(object, knownType, elementType, buffer);
		return buffer.toString();
	}

	public void toJson (Object object, FileHandle file) {
		toJson(object, object == null ? null : object.getClass(), null, file);
	}

	/** @param knownType May be null if the type is unknown. */
	public void toJson (Object object, Class<?> knownType, FileHandle file) {
		toJson(object, knownType, null, file);
	}

	/** @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public void toJson (Object object, Class<?> knownType, Class<?> elementType, FileHandle file) {
		Writer writer = null;
		try {
			writer = file.writer(false);
			toJson(object, knownType, elementType, writer);
		} catch (Exception ex) {
			throw new SerializationException("Error writing file: " + file, ex);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ignored) {
			}
		}
	}

	public void toJson (Object object, Writer writer) {
		toJson(object, object == null ? null : object.getClass(), null, writer);
	}

	/** @param knownType May be null if the type is unknown. */
	public void toJson (Object object, Class<?> knownType, Writer writer) {
		toJson(object, knownType, null, writer);
	}

	/** @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public void toJson (Object object, Class<?> knownType, Class<?> elementType, Writer writer) {
		if (!(writer instanceof JsonWriter)) {
			writer = new JsonWriter(writer);
		}
		((JsonWriter)writer).setOutputType(outputType);
		this.writer = (JsonWriter)writer;
		try {
			writeValue(object, knownType, elementType);
		} finally {
			this.writer = null;
		}
	}

	public void writeFields (Object object) {
		Class<?> type = object.getClass();

		Object[] defaultValues = getDefaultValues(type);

		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) {
			fields = cacheFields(type);
		}
		int i = 0;
		for (FieldMetadata metadata : fields.values()) {
			Field field = metadata.field;
			try {
				Object value = field.get(object);

				if (defaultValues != null) {
					Object defaultValue = defaultValues[i++];
					if (value == null && defaultValue == null) {
						continue;
					}
					if (value != null && defaultValue != null && value.equals(defaultValue)) {
						continue;
					}
				}

				if (debug) {
					System.out.println("Writing field: " + field.getName() + " (" + type.getName() + ")");
				}
				writer.name(field.getName());
				writeValue(value, field.getType(), metadata.elementType);
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
			} catch (SerializationException ex) {
				ex.addTrace(field + " (" + type.getName() + ")");
				throw ex;
			} catch (Exception runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(field + " (" + type.getName() + ")");
				throw ex;
			}
		}
	}

	private Object[] getDefaultValues (Class<?> type) {
		if (!usePrototypes) {
			return null;
		}
		if (classToDefaultValues.containsKey(type)) {
			return classToDefaultValues.get(type);
		}
		Object object;
		try {
			object = newInstance(type);
		} catch (Exception ex) {
			classToDefaultValues.put(type, null);
			return null;
		}

		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) {
			fields = cacheFields(type);
		}

		Object[] values = new Object[fields.size];
		classToDefaultValues.put(type, values);

		int i = 0;
		for (FieldMetadata metadata : fields.values()) {
			Field field = metadata.field;
			try {
				values[i++] = field.get(object);
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
			} catch (SerializationException ex) {
				ex.addTrace(field + " (" + type.getName() + ")");
				throw ex;
			} catch (RuntimeException runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(field + " (" + type.getName() + ")");
				throw ex;
			}
		}
		return values;
	}

	public void writeField (Object object, String name) {
		writeField(object, name, name, null);
	}

	/** @param elementType May be null if the type is unknown. */
	public void writeField (Object object, String name, Class<?> elementType) {
		writeField(object, name, name, elementType);
	}

	public void writeField (Object object, String fieldName, String jsonName) {
		writeField(object, fieldName, jsonName, null);
	}

	/** @param elementType May be null if the type is unknown. */
	public void writeField (Object object, String fieldName, String jsonName, Class<?> elementType) {
		Class<?> type = object.getClass();
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) {
			fields = cacheFields(type);
		}
		FieldMetadata metadata = fields.get(fieldName);
		if (metadata == null) {
			throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
		}
		Field field = metadata.field;
		if (elementType == null) {
			elementType = metadata.elementType;
		}
		try {
			if (debug) {
				System.out.println("Writing field: " + field.getName() + " (" + type.getName() + ")");
			}
			writer.name(jsonName);
			writeValue(field.get(object), field.getType(), elementType);
		} catch (IllegalAccessException ex) {
			throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
		} catch (SerializationException ex) {
			ex.addTrace(field + " (" + type.getName() + ")");
			throw ex;
		} catch (Exception runtimeEx) {
			SerializationException ex = new SerializationException(runtimeEx);
			ex.addTrace(field + " (" + type.getName() + ")");
			throw ex;
		}
	}

	/** @param value May be null. */
	public void writeValue (String name, Object value) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		if (value == null) {
			writeValue(value, null, null);
		}
		else {
			writeValue(value, value.getClass(), null);
		}
	}

	/** @param value May be null.
	 * @param knownType May be null if the type is unknown. */
	public void writeValue (String name, Object value, Class<?> knownType) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeValue(value, knownType, null);
	}

	/** @param value May be null.
	 * @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public void writeValue (String name, Object value, Class<?> knownType, Class<?> elementType) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeValue(value, knownType, elementType);
	}

	/** @param value May be null. */
	public void writeValue (Object value) {
		if (value == null) {
			writeValue(value, null, null);
		}
		else {
			writeValue(value, value.getClass(), null);
		}
	}

	/** @param value May be null.
	 * @param knownType May be null if the type is unknown. */
	public void writeValue (Object value, Class<?> knownType) {
		writeValue(value, knownType, null);
	}

	/** @param value May be null.
	 * @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	@SuppressWarnings("unchecked")
	public void writeValue (Object value, Class<?> knownType, Class<?> elementType) {
		try {
			if (value == null) {
				writer.value(null);
				return;
			}

			Class<?> actualType = value.getClass();

			if (actualType.isPrimitive() || actualType == String.class || actualType == Integer.class || actualType == Boolean.class
					|| actualType == Float.class || actualType == Long.class || actualType == Double.class || actualType == Short.class
					|| actualType == Byte.class || actualType == Character.class) {
				writer.value(value);
				return;
			}

			// UUID
			if (value instanceof UUID) {
				writeObjectStart(UUID.class, null);
				writeValue("uuid", value.toString(), knownType, elementType);
				writeObjectEnd();
				return;
			}

			// FixtureDef
			if (value instanceof FixtureDef) {
				writeFixtureDef((FixtureDef) value);
				return;
			}

			// Shape
			if (value instanceof Shape) {
				writeShape((Shape) value);
				return;
			}

			if (value instanceof Serializable) {
				writeObjectStart(actualType, knownType);
				((Serializable)value).write(this);
				writeObjectEnd();
				return;
			}

			Serializer<Object> serializer = (Serializer<Object>) classToSerializer.get(actualType);
			if (serializer != null) {
				serializer.write(this, value, knownType);
				return;
			}

			if (value instanceof Array) {
				if (knownType != null && actualType != knownType) {
					throw new SerializationException("Serialization of an Array other than the known type is not supported.\n"
							+ "Known type: " + knownType + "\nActual type: " + actualType);
				}
				writeArrayStart();
				Array<?> array = (Array<?>)value;
				for (int i = 0, n = array.size; i < n; i++) {
					writeValue(array.get(i), elementType, null);
				}
				writeArrayEnd();
				return;
			}

			if (value instanceof Collection) {
				if (knownType != null && actualType != knownType && actualType != ArrayList.class) {
					throw new SerializationException("Serialization of a Collection other than the known type is not supported.\n"
							+ "Known type: " + knownType + "\nActual type: " + actualType);
				}
				writeArrayStart();
				for (Object item : (Collection<?>)value) {
					writeValue(item, elementType, null);
				}
				writeArrayEnd();
				return;
			}

			if (actualType.isArray()) {
				if (elementType == null) {
					elementType = actualType.getComponentType();
				}
				int length = java.lang.reflect.Array.getLength(value);
				writeArrayStart();
				for (int i = 0; i < length; i++) {
					writeValue(java.lang.reflect.Array.get(value, i), elementType, null);
				}
				writeArrayEnd();
				return;
			}

			if (value instanceof OrderedMap) {
				if (knownType == null) {
					knownType = OrderedMap.class;
				}
				writeObjectStart(actualType, knownType);
				OrderedMap<Object, ?> map = (OrderedMap<Object, ?>)value;
				for (Object key : map.orderedKeys()) {
					writeMapEntry(key, map.get(key));
				}
				writeObjectEnd();
				return;
			}

			if (value instanceof ArrayMap) {
				if (knownType == null) {
					knownType = ArrayMap.class;
				}
				writeObjectStart(actualType, knownType);
				ArrayMap<?, ?> map = (ArrayMap<?, ?>)value;
				for (int i = 0, n = map.size; i < n; i++) {
					writeMapEntry(map.keys[i], map.values[i]);
				}
				writeObjectEnd();
				return;
			}

			if (value instanceof ObjectMap) {
				if (knownType == null) {
					knownType = OrderedMap.class;
				}
				writeObjectStart(actualType, knownType);
				if (((ObjectMap<?, ?>)value).size > 0) {
					for (Entry<?, ?> entry : ((ObjectMap<?, ?>)value).entries()) {
						writeMapEntry(entry.key, entry.value);
					}
				}
				writeObjectEnd();
				return;
			}

			if (value instanceof Map) {
				if (knownType == null) {
					knownType = OrderedMap.class;
				}
				writeObjectStart(actualType, knownType);
				for (Map.Entry<?, ?> entry : ((Map<?, ?>)value).entrySet()) {
					writeMapEntry(entry.getKey(), entry.getValue());
				}
				writeObjectEnd();
				return;
			}

			if (actualType.isEnum()) {
				writer.value(value);
				return;
			}

			writeObjectStart(actualType, knownType);
			writeFields(value);
			writeObjectEnd();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	private void writeFixtureDef(FixtureDef fixtureDef) {
		writeObjectStart(FixtureDef.class, null);
		writeValue("density", fixtureDef.density);
		writeValue("friction", fixtureDef.friction);
		writeValue("isSensor", fixtureDef.isSensor);
		writeValue("restitution", fixtureDef.restitution);
		writeValue("filter", fixtureDef.filter);
		writeValue("shape", fixtureDef.shape);
		writeObjectEnd();
	}

	private void writeShape(Shape shape) {
		writeObjectStart(Shape.class, null);
		writeValue("type", shape.getType());

		// Shape specific actions
		switch (shape.getType()) {
		case Circle:
			CircleShape circle = (CircleShape)shape;
			writeValue("position", circle.getPosition());
			writeValue("radius", circle.getRadius());
			break;

		case Polygon: {
			PolygonShape polygon = (PolygonShape)shape;
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
			EdgeShape edge = (EdgeShape)shape;
			Vector2 tempVector = Pools.vector2.obtain();
			edge.getVertex1(tempVector);
			writeValue("vertex1", tempVector);
			edge.getVertex2(tempVector);
			writeValue("vertex2", tempVector);
			Pools.vector2.free(tempVector);
			break;

		case Chain: {
			ChainShape chainShape = (ChainShape)shape;
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

	private void writeMapEntry(Object key, Object value) {
		writeObjectStart(convertToString(key));
		writeValue("keyType", key.getClass().getName());
		writeValue("valueType", value.getClass().getName());
		writeValue("key", key);
		writeValue("value", value);
		writeObjectEnd();
	}

	public void writeObjectStart (String name) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeObjectStart();
	}

	/** @param knownType May be null if the type is unknown. */
	public void writeObjectStart (String name, Class<?> actualType, Class<?> knownType) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeObjectStart(actualType, knownType);
	}

	public void writeObjectStart () {
		try {
			writer.object();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	/** @param knownType May be null if the type is unknown. */
	public void writeObjectStart (Class<?> actualType, Class<?> knownType) {
		try {
			writer.object();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		if (knownType == null || knownType != actualType) {
			writeType(actualType);
		}
	}

	public void writeObjectEnd () {
		try {
			writer.pop();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeArrayStart (String name) {
		try {
			writer.name(name);
			writer.array();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeArrayStart () {
		try {
			writer.array();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeArrayEnd () {
		try {
			writer.pop();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeType (Class<?> type) {
		if (typeName == null) {
			return;
		}
		String className = classToTag.get(type);
		if (className == null) {
			className = type.getName();
		}
		try {
			writer.set(typeName, className);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		if (debug) {
			System.out.println("Writing type: " + type.getName());
		}
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Reader reader) {
		return readValue(type, null, new JsonReader().parse(reader));
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class<?> elementType, Reader reader) {
		return readValue(type, elementType, new JsonReader().parse(reader));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, InputStream input) {
		return readValue(type, null, new JsonReader().parse(input));
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class<?> elementType, InputStream input) {
		return readValue(type, elementType, new JsonReader().parse(input));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, FileHandle file) {
		try {
			return readValue(type, null, new JsonReader().parse(file));
		} catch (Exception ex) {
			throw new SerializationException("Error reading file: " + file, ex);
		}
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class<?> elementType, FileHandle file) {
		try {
			return readValue(type, elementType, new JsonReader().parse(file));
		} catch (Exception ex) {
			throw new SerializationException("Error reading file: " + file, ex);
		}
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, char[] data, int offset, int length) {
		return readValue(type, null, new JsonReader().parse(data, offset, length));
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class<?> elementType, char[] data, int offset, int length) {
		return readValue(type, elementType, new JsonReader().parse(data, offset, length));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, String json) {
		return readValue(type, null, new JsonReader().parse(json));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class<?> elementType, String json) {
		return readValue(type, elementType, new JsonReader().parse(json));
	}

	public void readField (Object object, String name, Object jsonData) {
		readField(object, name, name, null, jsonData);
	}

	public void readField (Object object, String name, Class<?> elementType, Object jsonData) {
		readField(object, name, name, elementType, jsonData);
	}

	public void readField (Object object, String fieldName, String jsonName, Object jsonData) {
		readField(object, fieldName, jsonName, null, jsonData);
	}

	/** @param elementType May be null if the type is unknown. */
	@SuppressWarnings("unchecked")
	public void readField (Object object, String fieldName, String jsonName, Class<?> elementType, Object jsonData) {
		OrderedMap<String, ?> jsonMap = (OrderedMap<String, ?>)jsonData;
		Class<?> type = object.getClass();
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) {
			fields = cacheFields(type);
		}
		FieldMetadata metadata = fields.get(fieldName);
		if (metadata == null) {
			throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
		}
		Field field = metadata.field;
		Object jsonValue = jsonMap.get(jsonName);
		if (jsonValue == null) {
			return;
		}
		if (elementType == null) {
			elementType = metadata.elementType;
		}
		try {
			field.set(object, readValue(field.getType(), elementType, jsonValue));
		} catch (IllegalAccessException ex) {
			throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
		} catch (SerializationException ex) {
			ex.addTrace(field.getName() + " (" + type.getName() + ")");
			throw ex;
		} catch (RuntimeException runtimeEx) {
			SerializationException ex = new SerializationException(runtimeEx);
			ex.addTrace(field.getName() + " (" + type.getName() + ")");
			throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	public void readFields (Object object, Object jsonData) {
		OrderedMap<String, Object> jsonMap = (OrderedMap<String, Object>)jsonData;
		Class<?> type = object.getClass();
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) {
			fields = cacheFields(type);
		}
		for (Entry<String, Object> entry : jsonMap.entries()) {
			FieldMetadata metadata = fields.get(entry.key);
			if (metadata == null) {
				if (ignoreUnknownFields) {
					if (debug) {
						System.out.println("Ignoring unknown field: " + entry.key + " (" + type.getName() + ")");
					}
					continue;
				}
				else {
					throw new SerializationException("Field not found: " + entry.key + " (" + type.getName() + ")");
				}
			}
			Field field = metadata.field;
			if (entry.value == null) {
				continue;
			}
			try {
				field.set(object, readValue(field.getType(), metadata.elementType, entry.value));
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
			} catch (SerializationException ex) {
				ex.addTrace(field.getName() + " (" + type.getName() + ")");
				throw ex;
			} catch (RuntimeException runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(field.getName() + " (" + type.getName() + ")");
				throw ex;
			}
		}
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	@SuppressWarnings("unchecked")
	public <T> T readValue (String name, Class<T> type, Object jsonData) {
		OrderedMap<String, ?> jsonMap = (OrderedMap<String, ?>)jsonData;
		return readValue(type, null, jsonMap.get(name));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	@SuppressWarnings("unchecked")
	public <T> T readValue (String name, Class<T> type, T defaultValue, Object jsonData) {
		OrderedMap<String, ?> jsonMap = (OrderedMap<String, ?>)jsonData;
		Object jsonValue = jsonMap.get(name);
		if (jsonValue == null) {
			return defaultValue;
		}
		return readValue(type, null, jsonValue);
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	@SuppressWarnings("unchecked")
	public <T> T readValue (String name, Class<T> type, Class<?> elementType, Object jsonData) {
		OrderedMap<String, ?> jsonMap = (OrderedMap<String, ?>)jsonData;
		return readValue(type, elementType, jsonMap.get(name));
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	@SuppressWarnings("unchecked")
	public <T> T readValue (String name, Class<T> type, Class<?> elementType, T defaultValue, Object jsonData) {
		OrderedMap<String, ?> jsonMap = (OrderedMap<String, ?>)jsonData;
		Object jsonValue = jsonMap.get(name);
		if (jsonValue == null) {
			return defaultValue;
		}
		return readValue(type, elementType, jsonValue);
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (Class<T> type, Class<?> elementType, T defaultValue, Object jsonData) {
		return readValue(type, elementType, jsonData);
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (Class<T> type, Object jsonData) {
		return readValue(type, null, jsonData);
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	@SuppressWarnings({ "unchecked" })
	public <T> T readValue (Class<T> type, Class<?> elementType, Object jsonData) {
		if (jsonData == null) {
			return null;
		}

		if (jsonData instanceof UUID) {
			return (T)jsonData;
		}
		if (jsonData instanceof FixtureDef) {
			return (T)jsonData;
		}

		if (jsonData instanceof OrderedMap) {
			OrderedMap<String, Object> jsonMap = (OrderedMap<String, Object>)jsonData;

			String className = typeName == null ? null : (String)jsonMap.remove(typeName);
			if (className != null) {
				try {
					type = (Class<T>)Class.forName(className);
				} catch (ClassNotFoundException ex) {
					type = (Class<T>) tagToClass.get(className);
					if (type == null) {
						throw new SerializationException(ex);
					}
				}
			}

			// UUID
			if (type == UUID.class) {
				return (T) UUID.fromString((String)((OrderedMap<String, Object>)jsonData).get("uuid"));
			}
			if (type == FixtureDef.class) {
				return (T) readFixtureDef(jsonMap);
			}
			if (type == Shape.class) {
				return (T) readShape(jsonMap);
			}


			Object object;
			if (type != null) {
				Serializer<?> serializer = classToSerializer.get(type);
				if (serializer != null) {
					return (T)serializer.read(this, jsonMap, type);
				}

				if (type == Vector2.class) {
					object = Pools.vector2.obtain();
				} else {
					object = newInstance(type);
				}

				if (object instanceof Serializable) {
					((Serializable)object).read(this, jsonMap);
					return (T)object;
				}

				if (object instanceof HashMap) {
					HashMap<Object, Object> result = (HashMap<Object,Object>)object;
					readMapEntries(result, jsonMap);
					return (T)result;
				}
			}
			else {
				object = new OrderedMap<Object, Object>();
			}

			if (object instanceof OrderedMap) {
				// Did we just save an object or did we save a map?
				// Maps should be read through map entries, whereas objects
				// are read as usual. Maps have entries which in turn has both
				// keyType, valueType, key, and value.
				if (isSavedMap((OrderedMap<Object, Object>) object)) {
					OrderedMap<Object, Object> result = (OrderedMap<Object, Object>)object;
					readMapEntries(result, jsonMap);
					return (T)result;
				} else {
					OrderedMap<Object, Object> result = (OrderedMap<Object, Object>)object;
					for (String key : jsonMap.orderedKeys()) {
						result.put(key, readValue(elementType, null, jsonMap.get(key)));
					}
					return (T)result;
				}
			}

			if (object instanceof ObjectMap) {
				ObjectMap<Object, Object> result = (ObjectMap<Object, Object>)object;
				readMapEntries(result, jsonMap);
				return (T)result;
			}

			readFields(object, jsonMap);
			return (T)object;
		}

		if (type != null) {
			Serializer<?> serializer = classToSerializer.get(type);
			if (serializer != null) {
				return (T)serializer.read(this, jsonData, type);
			}
		}

		if (jsonData instanceof Array) {
			Array<?> array = (Array<?>)jsonData;
			if (type == null || Array.class.isAssignableFrom(type)) {
				Array<Object> newArray = new Array<Object>(array.size);
				for (int i = 0, n = array.size; i < n; i++) {
					newArray.add(readValue(elementType, null, array.get(i)));
				}
				return (T)newArray;
			}
			if (ArrayList.class.isAssignableFrom(type)) {
				ArrayList<Object> newArray = new ArrayList<Object>(array.size);
				for (int i = 0, n = array.size; i < n; i++) {
					newArray.add(readValue(elementType, null, array.get(i)));
				}
				return (T)newArray;
			}
			if (type.isArray()) {
				Class<?> componentType = type.getComponentType();
				if (elementType == null) {
					elementType = componentType;
				}
				Object newArray = java.lang.reflect.Array.newInstance(componentType, array.size);
				for (int i = 0, n = array.size; i < n; i++) {
					java.lang.reflect.Array.set(newArray, i, readValue(elementType, null, array.get(i)));
				}
				return (T)newArray;
			}
			throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")");
		}

		if (jsonData instanceof Float) {
			Float floatValue = (Float)jsonData;
			try {
				if (type == null || type == float.class || type == Float.class) {
					return (T)floatValue;
				}
				if (type == int.class || type == Integer.class) {
					return (T)(Integer)floatValue.intValue();
				}
				if (type == long.class || type == Long.class) {
					return (T)(Long)floatValue.longValue();
				}
				if (type == double.class || type == Double.class) {
					return (T)(Double)floatValue.doubleValue();
				}
				if (type == short.class || type == Short.class) {
					return (T)(Short)floatValue.shortValue();
				}
				if (type == byte.class || type == Byte.class) {
					return (T)(Byte)floatValue.byteValue();
				}
			} catch (NumberFormatException ignored) {
			}
			jsonData = String.valueOf(jsonData);
		}

		if (jsonData instanceof Boolean) {
			jsonData = String.valueOf(jsonData);
		}

		if (jsonData instanceof String) {
			String string = (String)jsonData;
			if (type == null || type == String.class) {
				return (T)jsonData;
			}
			try {
				if (type == int.class || type == Integer.class) {
					return (T)Integer.valueOf(string);
				}
				if (type == float.class || type == Float.class) {
					return (T)Float.valueOf(string);
				}
				if (type == long.class || type == Long.class) {
					return (T)Long.valueOf(string);
				}
				if (type == double.class || type == Double.class) {
					return (T)Double.valueOf(string);
				}
				if (type == short.class || type == Short.class) {
					return (T)Short.valueOf(string);
				}
				if (type == byte.class || type == Byte.class) {
					return (T)Byte.valueOf(string);
				}
			} catch (NumberFormatException ignored) {
			}
			if (type == boolean.class || type == Boolean.class) {
				return (T)Boolean.valueOf(string);
			}
			if (type == char.class || type == Character.class) {
				return (T)(Character)string.charAt(0);
			}
			if (type.isEnum()) {
				Object[] constants = type.getEnumConstants();
				for (int i = 0, n = constants.length; i < n; i++) {
					if (string.equals(constants[i].toString())) {
						return (T)constants[i];
					}
				}
			}
			if (type == CharSequence.class) {
				return (T)string;
			}
			throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")");
		}

		return (T) jsonData;
	}

	private FixtureDef readFixtureDef(OrderedMap<String, Object> jsonData) {
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

	private Shape readShape(OrderedMap<String, Object> jsonData) {
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

	@SuppressWarnings("unchecked")
	private boolean isSavedMap(OrderedMap<Object, Object> map) {
		if (map instanceof OrderedMap) {
			if (map.size > 0) {
				Entry<Object, Object> entry = map.entries().next();
				if (entry.value instanceof OrderedMap) {
					OrderedMap<Object, Object> value = (OrderedMap<Object, Object>) entry.value;
					if (value.size == 4) {
						if (value.containsKey("keyType") &&
								value.containsKey("valueType") &&
								value.containsKey("key") &&
								value.containsKey("value"))
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private void readMapEntries(Object result, OrderedMap<String, ?> jsonMap) {
		for (String key : jsonMap.orderedKeys()) {
			OrderedMap<String, Object> entry = (OrderedMap<String, Object>) jsonMap.get(key);
			String keyType = readValue("keyType", String.class, entry);
			String valueType = readValue("valueType", String.class, entry);
			Object keyObject = null;
			Object valueObject = null;
			try {
				keyObject = readValue("key", Class.forName(keyType), entry);
				valueObject = readValue("value", Class.forName(valueType), entry);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (result instanceof ObjectMap) {
				((ObjectMap<Object, Object>)result).put(keyObject, valueObject);
			} else if (result instanceof Map) {
				((Map<Object, Object>)result).put(keyObject, valueObject);
			}
		}
	}

	private String convertToString (Object object) {
		if (object instanceof Class) {
			return ((Class<?>)object).getName();
		}
		return String.valueOf(object);
	}

	private Object newInstance (Class<?> type) {
		try {
			return type.newInstance();
		} catch (Exception ex) {
			try {
				// Try a private constructor.
				Constructor<?> constructor = type.getDeclaredConstructor();
				constructor.setAccessible(true);
				return constructor.newInstance();
			} catch (SecurityException ignored) {
			} catch (NoSuchMethodException ignored) {
				if (type.isArray()) {
					throw new SerializationException("Encountered JSON object when expected array of type: " + type.getName(), ex);
				}
				else if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers())) {
					throw new SerializationException("Class cannot be created (non-static member class): " + type.getName(), ex);
				}
				else {
					throw new SerializationException("Class cannot be created (missing no-arg constructor): " + type.getName(), ex);
				}
			} catch (Exception privateConstructorException) {
				ex = privateConstructorException;
			}
			throw new SerializationException("Error constructing instance of class: " + type.getName(), ex);
		}
	}

	public String prettyPrint (Object object) {
		return prettyPrint(object, 0);
	}

	public String prettyPrint (String json) {
		return prettyPrint(json, 0);
	}

	public String prettyPrint (Object object, int singleLineColumns) {
		return prettyPrint(toJson(object), singleLineColumns);
	}

	public String prettyPrint (String json, int singleLineColumns) {
		StringBuilder buffer = new StringBuilder(512);
		prettyPrint(new JsonReader().parse(json), buffer, 0, singleLineColumns);
		return buffer.toString();
	}

	@SuppressWarnings("unchecked")
	private void prettyPrint (Object object, StringBuilder buffer, int indent, int singleLineColumns) {
		if (object instanceof OrderedMap) {
			OrderedMap<String, ?> map = (OrderedMap<String, ?>)object;
			if (map.size == 0) {
				buffer.append("{}");
			} else {
				boolean newLines = !isFlat(map);
				int start = buffer.length();
				outer:
					while (true) {
						buffer.append(newLines ? "{\n" : "{ ");
						int i = 0;
						for (String key : map.orderedKeys()) {
							if (newLines) {
								indent(indent, buffer);
							}
							buffer.append(outputType.quoteName(key));
							buffer.append(": ");
							prettyPrint(map.get(key), buffer, indent + 1, singleLineColumns);
							if (i++ < map.size - 1) {
								buffer.append(",");
							}
							buffer.append(newLines ? '\n' : ' ');
							if (!newLines && buffer.length() - start > singleLineColumns) {
								buffer.setLength(start);
								newLines = true;
								continue outer;
							}
						}
						break;
					}
				if (newLines) {
					indent(indent - 1, buffer);
				}
				buffer.append('}');
			}
		} else if (object instanceof Array) {
			Array<?> array = (Array<?>)object;
			if (array.size == 0) {
				buffer.append("[]");
			} else {
				boolean newLines = !isFlat(array);
				int start = buffer.length();
				outer:
					while (true) {
						buffer.append(newLines ? "[\n" : "[ ");
						for (int i = 0, n = array.size; i < n; i++) {
							if (newLines) {
								indent(indent, buffer);
							}
							prettyPrint(array.get(i), buffer, indent + 1, singleLineColumns);
							if (i < array.size - 1) {
								buffer.append(",");
							}
							buffer.append(newLines ? '\n' : ' ');
							if (!newLines && buffer.length() - start > singleLineColumns) {
								buffer.setLength(start);
								newLines = true;
								continue outer;
							}
						}
						break;
					}
				if (newLines) {
					indent(indent - 1, buffer);
				}
				buffer.append(']');
			}
		} else if (object instanceof String) {
			buffer.append(outputType.quoteValue(object));
		} else if (object instanceof Float) {
			Float floatValue = (Float)object;
			int intValue = floatValue.intValue();
			buffer.append(floatValue - intValue == 0 ? intValue : object);
		} else if (object instanceof Boolean) {
			buffer.append(object);
		} else if (object == null) {
			buffer.append("null");
		}
		else {
			throw new SerializationException("Unknown object type: " + object.getClass());
		}
	}

	static private boolean isFlat (ObjectMap<?, ?> map) {
		for (Entry<?, ?> entry : map.entries()) {
			if (entry.value instanceof ObjectMap) {
				return false;
			}
			if (entry.value instanceof Array) {
				return false;
			}
		}
		return true;
	}

	static private boolean isFlat (Array<?> array) {
		for (int i = 0, n = array.size; i < n; i++) {
			Object value = array.get(i);
			if (value instanceof ObjectMap) {
				return false;
			}
			if (value instanceof Array) {
				return false;
			}
		}
		return true;
	}

	static private void indent (int count, StringBuilder buffer) {
		for (int i = 0; i < count; i++) {
			buffer.append('\t');
		}
	}

	static private class FieldMetadata {
		Field field;
		Class<?> elementType;

		public FieldMetadata (Field field) {
			this.field = field;

			Type genericType = field.getGenericType();
			if (genericType instanceof ParameterizedType) {
				Type[] actualTypes = ((ParameterizedType)genericType).getActualTypeArguments();
				if (actualTypes.length == 1) {
					Type actualType = actualTypes[0];
					if (actualType instanceof Class) {
						elementType = (Class<?>)actualType;
					}
					else if (actualType instanceof ParameterizedType) {
						elementType = (Class<?>)((ParameterizedType)actualType).getRawType();
					}
				}
			}
		}
	}

	static public interface Serializer<T> {
		public void write (Json json, T object, Class<?> knownType);

		public T read (Json json, Object jsonData, Class<?> type);
	}

	static abstract public class ReadOnlySerializer<T> implements Serializer<T> {
		public void write (Json json, T object, Class<?> knownType) {
		}

		abstract public T read (Json json, Object jsonData, Class<?> type);
	}

	static public interface Serializable {
		public void write (Json json);

		public void read (Json json, OrderedMap<String, Object> jsonData);
	}

	private static final boolean debug = false;

	private JsonWriter writer;
	private String typeName = "class";
	private boolean usePrototypes = true;
	private OutputType outputType;
	private final ObjectMap<Class<?>, ObjectMap<String, FieldMetadata>> typeToFields = new ObjectMap<Class<?>, ObjectMap<String, FieldMetadata>>();
	private final ObjectMap<String, Class<?>> tagToClass = new ObjectMap<String, Class<?>>();
	private final ObjectMap<Class<?>, String> classToTag = new ObjectMap<Class<?>, String>();
	private final ObjectMap<Class<?>, Serializer<?>> classToSerializer = new ObjectMap<Class<?>, Serializer<?>>();
	private final ObjectMap<Class<?>, Object[]> classToDefaultValues = new ObjectMap<Class<?>, Object[]>();
	private boolean ignoreUnknownFields;
}
