package com.spiddekauga.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Buffer utilities
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Buffers {
	/**
	 * Fully reads an input stream and returns the result as a whole array.
	 * Uses a temporary buffer size of 512 bytes. If you know the approximately
	 * size of the input stream, use {@link #readBytes(InputStream, int)} instead.
	 * @param inputStream the input to read
	 * @return the stream as a single byte buffer
	 */
	public static byte[] readBytes(InputStream inputStream) {
		return readBytes(inputStream, BUFFER_SIZE_DEFAULT);
	}

	/**
	 * Fully reads an input stream and returns the result as a whole array
	 * @param inputStream the input to read
	 * @param tempBufferSize temporary buffer size
	 * @return the stream as a single byte buffer, null if inputStream is closed
	 * or something else.
	 */
	public static byte[] readBytes(InputStream inputStream, int tempBufferSize) {
		ArrayList<byte[]> byteBuffers = new ArrayList<>();
		int cReadBytesTotal = 0;

		do {
			byte[] readBytes = new byte[tempBufferSize];
			int cReadBytes = 0;
			try {
				cReadBytes = inputStream.read(readBytes, 0, tempBufferSize);
			} catch (IOException e) {
			}


			if (cReadBytes > 0) {
				cReadBytesTotal += cReadBytes;


				// Read less than maximum, end of file
				if (cReadBytes < tempBufferSize) {
					// Copy value to a smaller buffer and append it to byte buffers
					byte[] smallBuffer = new byte[cReadBytes];
					System.arraycopy(readBytes, 0, smallBuffer, 0, cReadBytes);
					byteBuffers.add(smallBuffer);
					break;
				} else {
					byteBuffers.add(readBytes);
				}
			} else {
				break;
			}

		} while (true);

		// Merge all to one big buffer
		byte[] wholeByteArray = new byte[cReadBytesTotal];
		int offset = 0;
		for (byte[] partBuffer : byteBuffers) {
			System.arraycopy(partBuffer, 0, wholeByteArray, offset, partBuffer.length);
			offset += partBuffer.length;
		}

		return wholeByteArray;
	}

	/** Default buffer size */
	private static final int BUFFER_SIZE_DEFAULT = 512;
}
