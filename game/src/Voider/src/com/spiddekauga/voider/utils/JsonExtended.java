package com.spiddekauga.voider.utils;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Extended JSON class that can handle reading some new class types
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class JsonExtended extends Json {
	@SuppressWarnings("rawtypes")
	@Override
	public void writeValue(Object value, Class knownType, Class element) {
		if (value == null) {
			super.writeValue(value, knownType, element);
			return;
		}

		// UUID
		if (value instanceof UUID) {
			writeObjectStart(UUID.class, null);
			super.writeValue("uuid", value.toString(), knownType, element);
			writeObjectEnd();
			return;
		}


		// Use default
		super.writeValue(value, knownType, element);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> T readValue(Class<T> type, Class elementType, Object jsonData) {
		if (jsonData == null) {
			return null;
		}

		if (jsonData instanceof OrderedMap) {
			// Get type if unknown
			OrderedMap<String, Object> jsonMap = (OrderedMap<String, Object>)jsonData;
			String className = "class" == null ? null : (String)jsonMap.remove("class");
			if (className != null) {
				try {
					type = (Class<T>)Class.forName(className);
				} catch (ClassNotFoundException ex) {
				}
			}

			// UUID
			if (type == UUID.class) {
				return (T) UUID.fromString((String)((OrderedMap<String, Object>)jsonData).get("uuid"));
			}
		}

		return super.readValue(type, elementType, jsonData);
	}
}
