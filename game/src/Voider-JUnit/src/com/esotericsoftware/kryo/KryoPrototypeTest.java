package com.esotericsoftware.kryo;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Prototype tests for Kryo
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("javadoc")
public class KryoPrototypeTest {
	@Test
	public void testGeneral() {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Output output = new Output(byteOut);

		TestGeneral testGeneral = new TestGeneral();
		testGeneral.number = 1;
		testGeneral.anotherNumber = 2;
		testGeneral.text = "This is my text";

		mKryo.writeObject(output, testGeneral);
		output.close();

		Input input = new Input(byteOut.toByteArray());

		TestGeneral kryoGeneral = mKryo.readObject(input, TestGeneral.class);

		assertEquals("numbers", testGeneral.number, kryoGeneral.number);
		assertEquals("another number", testGeneral.anotherNumber, kryoGeneral.anotherNumber, 0.001f);
		assertEquals("Text", testGeneral.text, kryoGeneral.text);
	}

	@Test
	public void testTaggedFieldSerializer() {

	}

	@Test
	public void testCompatibleFieldSerializer() {

	}

	@Test
	public void testUUID() {

	}

	@Test
	public void testFixtureDef() {

	}

	@Test
	public void testShape() {

	}

	@Test
	public void testObjectMap() {

	}

	Kryo mKryo = new Kryo();

	private static class TestGeneral {
		int number;
		String text = null;
		float anotherNumber;
	}
}
