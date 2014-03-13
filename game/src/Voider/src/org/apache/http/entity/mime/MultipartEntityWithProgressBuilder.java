package org.apache.http.entity.mime;


import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.Args;

import com.spiddekauga.utils.IOutstreamProgressListener;


/**
 * Wrapper for a MultipartEntityWithProgressBuilder as keeps track of the progress. Mostly copied
 * from MultipartEntityWithProgressBuilder. Only added progress listeners
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MultipartEntityWithProgressBuilder {
	/**
	 * Creates the builder
	 * @return new builder
	 */
	public static MultipartEntityWithProgressBuilder create() {
		return new MultipartEntityWithProgressBuilder();
	}

	/**
	 * Hidden constructor
	 */
	MultipartEntityWithProgressBuilder() {
		super();
	}

	/**
	 * Set the HTTP mode
	 * @param mode
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder setMode(final HttpMultipartMode mode) {
		this.mMode = mode;
		return this;
	}

	/**
	 * Set browser compatible mode
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder setLaxMode() {
		this.mMode = HttpMultipartMode.BROWSER_COMPATIBLE;
		return this;
	}

	/**
	 * Set strict mode
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder setStrictMode() {
		this.mMode = HttpMultipartMode.STRICT;
		return this;
	}

	/**
	 * Set the boundary?
	 * @param boundary
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder setBoundary(final String boundary) {
		this.mBoundary = boundary;
		return this;
	}

	/**
	 * Set the charset
	 * @param charset
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder setCharset(final Charset charset) {
		this.mCharset = charset;
		return this;
	}

	/**
	 * Add a progress listener
	 * @param listeners progress listener that will listen to upload bytes
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addProgressListener(final IOutstreamProgressListener... listeners) {
		for (IOutstreamProgressListener listener : listeners) {
			mProgressListeners.add(listener);
		}
		return this;
	}

	/**
	 * Add a progress listener
	 * @param listeners progress listener that will listen to upload bytes
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addProgressListener(final List<IOutstreamProgressListener> listeners) {
		for (IOutstreamProgressListener listener : listeners) {
			mProgressListeners.add(listener);
		}
		return this;
	}

	/**
	 * Add a body part
	 * @param bodyPart
	 * @return this for chaining
	 */
	MultipartEntityWithProgressBuilder addPart(final FormBodyPart bodyPart) {
		if (bodyPart == null) {
			return this;
		}
		if (this.mBodyParts == null) {
			this.mBodyParts = new ArrayList<FormBodyPart>();
		}
		this.mBodyParts.add(bodyPart);
		return this;
	}

	/**
	 * Add a form part
	 * @param name part/element name
	 * @param contentBody data
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addPart(final String name, final ContentBody contentBody) {
		Args.notNull(name, "Name");
		Args.notNull(contentBody, "Content body");
		return addPart(new FormBodyPart(name, contentBody));
	}

	/**
	 * Add text body
	 * @param name part/element name
	 * @param text text data
	 * @param contentType type of text
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addTextBody(
			final String name, final String text, final ContentType contentType) {
		return addPart(name, new StringBody(text, contentType));
	}

	/**
	 * Add text body
	 * @param name part/element name
	 * @param text text data
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addTextBody(
			final String name, final String text) {
		return addTextBody(name, text, ContentType.DEFAULT_TEXT);
	}

	/**
	 * Add binary body
	 * @param name part/element name
	 * @param b bytes to send
	 * @param contentType content type
	 * @param filename filename that will be used when uploading
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addBinaryBody(
			final String name, final byte[] b, final ContentType contentType, final String filename) {
		return addPart(name, new ByteArrayBody(b, contentType, filename));
	}

	/**
	 * Add binary body
	 * @param name part/element name
	 * @param b bytes to send
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addBinaryBody(
			final String name, final byte[] b) {
		return addBinaryBody(name, b, ContentType.DEFAULT_BINARY, null);
	}

	/**
	 * Add binary body
	 * @param name part/element name
	 * @param file file to send
	 * @param contentType content type
	 * @param filename name of the file
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addBinaryBody(
			final String name, final File file, final ContentType contentType, final String filename) {
		return addPart(name, new FileBody(file, contentType, filename));
	}

	/**
	 * Add binary body
	 * @param name part/element name
	 * @param file file to send
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addBinaryBody(
			final String name, final File file) {
		return addBinaryBody(name, file, ContentType.DEFAULT_BINARY, file != null ? file.getName() : null);
	}

	/**
	 * Add binary body
	 * @param name part/element name
	 * @param stream the stream to send
	 * @param contentType
	 * @param filename name of the file
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addBinaryBody(
			final String name, final InputStream stream, final ContentType contentType,
			final String filename) {
		return addPart(name, new InputStreamBody(stream, contentType, filename));
	}

	/**
	 * Add binary body
	 * @param name part/element name
	 * @param stream stream to send
	 * @return this for chaining
	 */
	public MultipartEntityWithProgressBuilder addBinaryBody(final String name, final InputStream stream) {
		return addBinaryBody(name, stream, ContentType.DEFAULT_BINARY, null);
	}

	/**
	 * Generate the content type
	 * @param boundary
	 * @param charset
	 * @return content type as a string
	 */
	private String generateContentType(
			final String boundary,
			final Charset charset) {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("multipart/form-data; boundary=");
		buffer.append(boundary);
		if (charset != null) {
			buffer.append("; charset=");
			buffer.append(charset.name());
		}
		return buffer.toString();
	}

	/**
	 * Generate the boundary
	 * @return package size?
	 */
	private String generateBoundary() {
		final StringBuilder buffer = new StringBuilder();
		final Random rand = new Random();
		final int count = rand.nextInt(11) + 30; // a random size from 30 to 40
		for (int i = 0; i < count; i++) {
			buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}
		return buffer.toString();
	}

	/**
	 * Builds the actual entity
	 * @return new HttpEntity
	 */
	MultipartFormEntityWithProgress buildEntity() {
		final String st = mSubType != null ? mSubType : DEFAULT_SUBTYPE;
		final Charset cs = mCharset;
		final String b = mBoundary != null ? mBoundary : generateBoundary();
		final List<FormBodyPart> bps = mBodyParts != null ? new ArrayList<FormBodyPart>(mBodyParts) :
			Collections.<FormBodyPart>emptyList();
		final HttpMultipartMode m = mMode != null ? mMode : HttpMultipartMode.STRICT;
		final AbstractMultipartForm form;
		switch (m) {
		case BROWSER_COMPATIBLE:
			form = new HttpBrowserCompatibleMultipart(st, cs, b, bps);
			break;
		case RFC6532:
			form = new HttpRFC6532Multipart(st, cs, b, bps);
			break;
		default:
			form = new HttpStrictMultipart(st, cs, b, bps);
		}

		return new MultipartFormEntityWithProgress(form, generateContentType(b, cs), form.getTotalLength(), mProgressListeners);
	}

	/**
	 * @return new HttpEntity from the build
	 */
	public HttpEntity build() {
		return buildEntity();
	}

	/**
	 * The pool of ASCII chars to be used for generating a multipart boundary.
	 */
	private final static char[] MULTIPART_CHARS =
			"-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();

	/** Default subtype */
	private final static String DEFAULT_SUBTYPE = "form-data";

	/** Current subtype */
	private String mSubType = DEFAULT_SUBTYPE;
	/** HTTP mode, strict/compatible */
	private HttpMultipartMode mMode = HttpMultipartMode.STRICT;
	/** Boundary?? */
	private String mBoundary = null;
	/** Character set */
	private Charset mCharset = null;
	/** The body parts */
	private List<FormBodyPart> mBodyParts = null;
	/** All progress listeners */
	private List<IOutstreamProgressListener> mProgressListeners = new ArrayList<>();
}
