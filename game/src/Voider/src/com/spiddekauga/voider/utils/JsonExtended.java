package com.spiddekauga.voider.utils;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;

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
			super.writeValue((Object)value.toString(), knownType, element);
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

		// UUID
		if (jsonData instanceof String) {
			if (type == UUID.class) {
				return (T) UUID.fromString((String)jsonData);
			}
		}

		return super.readValue(type, elementType, jsonData);
	}
}
