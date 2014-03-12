package com.spiddekauga.utils.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

/**
 * This serializer is for serializing tags including some additional serialization.<br/>
 * Write follows these steps:
 * <ol>
 * <li>Check if object implements KryoPreWrite to call object.preWrite()</li>
 * <li>Call TaggedFieldSerializer.write(Kryo,Output)</li>
 * <li>Check if object implements KryoSerializable to call object.write(Kryo,Output)</li>
 * </ol><br/>
 * <br/>
 * Read follows these steps:
 * <ol>
 * <li>Call TaggedFieldSerializer.read(Kryo,Input)</li>
 * <li>Check if object implements KryoSerializable to call object.write(Kryo,Input)</li>
 * <li>Check if object implements KryoPostRead to call object.postRead()</li>
 * </ol>
 *
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SerializableTaggedFieldSerializer extends TaggedFieldSerializer<Object> {

	/**
	 * Creates the serializable tagged field serializer.
	 * @param kryo the kryo object to serialize the class in
	 * @param type the type to serialize
	 */
	public SerializableTaggedFieldSerializer(Kryo kryo, Class<?> type) {
		super(kryo, type);
	}

	@Override
	public void write(Kryo kryo, Output output, Object object) {
		if (object instanceof KryoPreWrite) {
			((KryoPreWrite) object).preWrite();
		}
		super.write(kryo, output, object);
		if (object instanceof KryoSerializable) {
			((KryoSerializable) object).write(kryo, output);
		}
		if (object instanceof KryoPostWrite) {
			((KryoPostWrite) object).postWrite();
		}
	}

	@Override
	public Object read(Kryo kryo, Input input, Class<Object> type) {
		Object object = super.read(kryo, input, type);
		if (object instanceof KryoSerializable) {
			((KryoSerializable) object).read(kryo, input);
		}
		if (object instanceof KryoPostRead) {
			((KryoPostRead) object).postRead();
		}
		return object;
	}

	@Override
	public Object copy(Kryo kryo, Object original) {
		Object copy = super.copy(kryo, original);
		if (copy instanceof KryoTaggedCopyable) {
			((KryoTaggedCopyable) copy).copy(original);
		}
		return copy;
	}
}
