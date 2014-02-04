package com.spiddekauga.utils.kryo;

import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes AtomicInteger just as an Integer
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AtomicIntegerSerializer extends Serializer<AtomicInteger> {
	{
		setImmutable(true);
	}

	@Override
	public void write(Kryo kryo, Output output, AtomicInteger object) {
		output.writeInt(object.get(), false);
	}

	@Override
	public AtomicInteger read(Kryo kryo, Input input, Class<AtomicInteger> type) {
		return new AtomicInteger(input.readInt(false));
	}
}
