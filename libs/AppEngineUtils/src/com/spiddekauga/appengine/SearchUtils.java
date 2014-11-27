package com.spiddekauga.appengine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Field.FieldType;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchQueryException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;


/**
 * Search utilities for Google App Engine
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SearchUtils {
	/**
	 * Retrieve a document
	 * @param indexName name of the index the document exists in
	 * @param documentId id of the document to get
	 * @return the found document, null if not found
	 */
	public static Document getDocument(String indexName, String documentId) {
		Index index = getIndex(indexName);
		return index.get(documentId);
	}

	/**
	 * Remove a document
	 * @param indexName name of the index the document exists in
	 * @param document the document to remove
	 */
	public static void deleteDocument(String indexName, Document document) {
		deleteDocumentById(indexName, document.getId());
	}

	/**
	 * Remove a document
	 * @param indexName name of the index the document exists in
	 * @param documentId id of the document to remove
	 */
	public static void deleteDocumentById(String indexName, String documentId) {
		Index index = getIndex(indexName);
		index.delete(documentId);
	}

	/**
	 * Remove a batch of documents
	 * @param indexName name of the index all documents exists in
	 * @param documents all documents to remove
	 */
	public static void deleteDocuments(String indexName, Iterable<Document> documents) {
		ArrayList<String> documentIds = new ArrayList<>();

		for (Document document : documents) {
			documentIds.add(document.getId());
		}

		deleteDocumentsById(indexName, documentIds);
	}

	/**
	 * Remove a batch of documents
	 * @param indexName name of the index all documents exists in
	 * @param documentIds ids of all documents to remove
	 */
	public static void deleteDocumentsById(String indexName, List<String> documentIds) {
		Index index = getIndex(indexName);

		// If OK size
		if (documentIds.size() <= PUT_LIMIT) {
			index.delete(documentIds);
		}
		// Too large set -> Split
		else {
			int fromIndex = 0;
			do {
				int toIndex = fromIndex + PUT_LIMIT;
				if (toIndex > documentIds.size()) {
					toIndex = documentIds.size();
				}

				List<String> subList = documentIds.subList(fromIndex, toIndex);
				index.delete(subList);
			} while (fromIndex < documentIds.size());
		}
	}

	/**
	 * Index a document
	 * @param indexName name of the index to put the document in
	 * @param document the document to index
	 * @return true if successfully added document to index
	 */
	public static boolean indexDocument(String indexName, Document document) {
		Index index = getIndex(indexName);

		boolean retry = false;
		do {
			try {
				retry = false;
				index.put(document);
			} catch (PutException e) {
				if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
					retry = true;
				}
			}
		} while (retry);

		return true;
	}

	/**
	 * Indexes several documents (in batches if necessary)
	 * @param indexName name of the index to put the document in
	 * @param documents all the documents to index
	 * @return true if successfully added document to index
	 */
	public static boolean indexDocuments(String indexName, List<Document> documents) {
		Index index = getIndex(indexName);

		// If OK size
		if (documents.size() <= PUT_LIMIT) {
			return indexDocuments(index, documents);
		}
		// Too large set -> Split
		else {
			int fromIndex = 0;
			boolean success = true;
			do {
				int toIndex = fromIndex + PUT_LIMIT;
				if (toIndex > documents.size()) {
					toIndex = documents.size();
				}

				List<Document> subList = documents.subList(fromIndex, toIndex);
				success = indexDocuments(index, subList);

			} while (success && fromIndex < documents.size());
			return success;
		}
	}

	/**
	 * Indexes several documents (in batches if necessary)
	 * @param index where to put the document
	 * @param documents all the documents to index
	 * @return true if successfully added document to index
	 */
	private static boolean indexDocuments(Index index, Iterable<Document> documents) {
		boolean retry = false;
		do {
			try {
				retry = false;
				index.put(documents);
			} catch (PutException e) {
				if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
					retry = true;
				}
			}
		} while (retry);

		return true;
	}

	/**
	 * @param indexName name of the index to get
	 * @return the actual index with the specified name
	 */
	private static Index getIndex(String indexName) {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

		return index;
	}

	/**
	 * Search for documents
	 * @param indexName name of the index to search in
	 * @param searchQuery the search string to use
	 * @param limit maximum number of results
	 * @param cursor continue the search from this cursor, if null does a new search
	 * @return all found documents
	 */
	public static Results<ScoredDocument> search(String indexName, String searchQuery, int limit, Cursor cursor) {
		Index index = getIndex(indexName);

		QueryOptions.Builder optionsBuilder = QueryOptions.newBuilder();

		// Create cursor if not exists
		Cursor cursorToUse = cursor;
		if (cursor == null) {
			cursorToUse = Cursor.newBuilder().build();
		}

		optionsBuilder.setLimit(limit);
		optionsBuilder.setCursor(cursorToUse);

		Query.Builder queryBuilder = Query.newBuilder().setOptions(optionsBuilder);

		Results<ScoredDocument> foundDocuments = null;
		boolean retry = false;
		do {
			try {
				retry = false;
				foundDocuments = index.search(queryBuilder.build(searchQuery));
			} catch (SearchQueryException e) {
				if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
					retry = true;
				}
			}
		} while (retry);

		return foundDocuments;
	}

	/**
	 * Split words into smaller parts so these can be auto-completed
	 * @param text the text to tokenize
	 * @param minSize the minimum size of the tokens/auto-complete, if a word is shorter
	 *        than this size it will still be added as a token.
	 * @return auto-complete compatible tokenized text
	 */
	public static String tokenizeAutocomplete(String text, int minSize) {
		if (minSize <= 0) {
			throw new IllegalArgumentException("minSize has to be higher than 0");
		}

		String[] words = splitTextToWords(text);
		String tokens = "";
		for (String word : words) {
			if (word.length() > minSize) {
				for (int i = 0; i <= word.length() - minSize; ++i) {
					for (int currentLength = minSize; currentLength <= word.length() - i; ++currentLength) {
						tokens += word.substring(i, i + currentLength) + " ";
					}
				}
			} else {
				tokens += word + " ";
			}
		}

		return tokens;
	}

	/**
	 * Splits a text into words.
	 * @param text the text to split into words
	 * @return all words in the text
	 */
	public static String[] splitTextToWords(String text) {
		return text.split("[^0-9a-zA-Z']+");
	}

	/**
	 * Converts a boolean to an atom
	 * @param value boolean value to convert
	 * @return an atom string that represents the boolean value
	 */
	private static String getAtom(boolean value) {
		return value ? TRUE : FALSE;
	}

	/**
	 * Converts a string boolean atom to a java boolean value
	 * @param atomBoolean the atom boolean value
	 * @return java boolean value
	 */
	private static boolean getBoolean(String atomBoolean) {
		if (atomBoolean != null) {
			if (atomBoolean.equals(TRUE)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get a boolean value from an atom field
	 * @param document the document to get the number from
	 * @param fieldName name of the field
	 * @return value of the boolean, but will also return false if not found.
	 */
	public static boolean getBoolean(Document document, String fieldName) {
		String atom = getValue(document, fieldName, FieldType.ATOM);
		return getBoolean(atom);
	}

	/**
	 * Get text either from a text, atom, or HTML field
	 * @param document the document to get the text from
	 * @param fieldName name of the field
	 * @return text, null if the field doesn't exist or isn't a text, atom, or HTML field.
	 */
	public static String getText(Document document, String fieldName) {
		return getValue(document, fieldName, FieldType.TEXT, FieldType.ATOM, FieldType.HTML);
	}

	/**
	 * Get a float number from the specified field. If multiple fields exists with this
	 * name the first valid field will be returned.
	 * @param document the document to get the number from
	 * @param fieldName name of the field
	 * @return float value of the field, 0 if the field doesn't exist or isn't a number
	 */
	public static float getFloat(Document document, String fieldName) {
		Double number = getValue(document, fieldName, FieldType.NUMBER);
		if (number != null) {
			return number.floatValue();
		} else {
			return 0;
		}
	}

	/**
	 * Get the first correct value from a field
	 * @param <ObjectType> the type to get
	 * @param document document to get the field from
	 * @param fieldName name of the field
	 * @param fieldTypes valid field types the value can be in
	 * @return first valid field value, null if none was found
	 */
	@SuppressWarnings("unchecked")
	public static <ObjectType> ObjectType getValue(Document document, String fieldName, FieldType... fieldTypes) {
		Iterable<Field> fields = document.getFields(fieldName);
		if (fields != null) {
			for (Field field : fields) {
				if (field.getType() != null) {
					for (FieldType fieldType : fieldTypes) {
						if (field.getType() == fieldType) {
							Object object = null;
							switch (fieldType) {
							case ATOM:
								object = field.getAtom();
								break;
							case DATE:
								object = field.getDate();
								break;
							case GEO_POINT:
								object = field.getGeoPoint();
								break;
							case HTML:
								object = field.getHTML();
								break;
							case NUMBER:
								object = field.getNumber();
								break;
							case TEXT:
								object = field.getText();
								break;
							}

							if (object != null) {
								return (ObjectType) object;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates and a new tokenized text field. Same as calling
	 * {@link #createField(String, String, boolean)} with true.
	 * @param name the name of the field
	 * @param text the text to fill the field with
	 * @return field builder
	 */
	public static Field.Builder createField(String name, String text) {
		return createField(name, text, true);
	}

	/**
	 * Creates a new text field that can be tokenized
	 * @param name the name of the field
	 * @param text the text to fill the field with
	 * @param tokenize set to true to tokenize the field
	 * @return field builder
	 */
	public static Field.Builder createField(String name, String text, boolean tokenize) {
		if (tokenize) {
			return createField(name, text, TOKENIZE_LENGTH);
		} else {
			return Field.newBuilder().setName(name).setText(text);
		}
	}

	/**
	 * Creates a new text field that is tokenized to the specified length.
	 * @param name the name of the field
	 * @param text the text to fill the field with
	 * @param tokenLength minimum length of tokens (minimum value is 1)
	 * @return field builder
	 */
	public static Field.Builder createField(String name, String text, int tokenLength) {
		String tokenizedText = tokenizeAutocomplete(text, tokenLength);
		return createField(name, tokenizedText, false);
	}

	/**
	 * Creates a boolean field (atom field)
	 * @param name the name of the field
	 * @param value the boolean value
	 * @return correct field builder
	 */
	public static Field.Builder createFiled(String name, boolean value) {
		return Field.newBuilder().setName(name).setAtom(getAtom(value));
	}

	/**
	 * Creates a number field
	 * @param name the name of the field
	 * @param value the number value of the field
	 * @return correct field builder
	 */
	public static Field.Builder createField(String name, Number value) {
		return Field.newBuilder().setName(name).setNumber(value.doubleValue());
	}

	/**
	 * Creates a date field
	 * @param name the name of the field
	 * @param date value of the field
	 * @return correct field builder
	 */
	public static Field.Builder createField(String name, Date date) {
		return Field.newBuilder().setName(name).setDate(date);
	}

	/**
	 * Creates an atom field
	 * @param name the name of the field
	 * @param atom the text to store as atom field
	 * @return correct field
	 */
	public static Field.Builder createFieldAtom(String name, String atom) {
		return Field.newBuilder().setName(name).setAtom(atom);
	}

	/**
	 * Add and a new tokenized text field.
	 * @param builder the builder to add the field to
	 * @param name the name of the field
	 * @param text the text to fill the field with
	 */
	public static void addField(Document.Builder builder, String name, String text) {
		builder.addField(createField(name, text, true));
	}

	/**
	 * Add a new text field that can be tokenized
	 * @param builder the builder to add the field to
	 * @param name the name of the field
	 * @param text the text to fill the field with
	 * @param tokenize set to true to tokenize the field
	 */
	public static void addField(Document.Builder builder, String name, String text, boolean tokenize) {
		builder.addField(createField(name, text, tokenize));
	}

	/**
	 * Add a new text field that is tokenized to the specified length.
	 * @param builder the builder to add the field to
	 * @param name the name of the field
	 * @param text the text to fill the field with
	 * @param tokenLength minimum length of tokens (minimum value is 1)
	 */
	public static void addField(Document.Builder builder, String name, String text, int tokenLength) {
		builder.addField(createField(name, text, tokenLength));
	}

	/**
	 * Add a boolean field (atom field)
	 * @param builder the builder to add the field to
	 * @param name the name of the field
	 * @param value the boolean value
	 */
	public static void addField(Document.Builder builder, String name, boolean value) {
		builder.addField(createFiled(name, value));
	}

	/**
	 * Add a number field
	 * @param builder the builder to add the field to
	 * @param name the name of the field
	 * @param value the number value of the field
	 */
	public static void addField(Document.Builder builder, String name, Number value) {
		builder.addField(createField(name, value));
	}

	/**
	 * Add a date field
	 * @param builder the builder to add the field to
	 * @param name the name of the field
	 * @param date value of the field
	 */
	public static void addField(Document.Builder builder, String name, Date date) {
		builder.addField(createField(name, date));
	}

	/**
	 * Add an atom field
	 * @param builder the builder to add the field to
	 * @param name the name of the field
	 * @param atom the text to store as an atom
	 */
	public static void addFieldAtom(Document.Builder builder, String name, String atom) {
		builder.addField(createFieldAtom(name, atom));
	}

	/**
	 * Class for helping to build search strings
	 */
	public static class Builder {
		/**
		 * Push/Begin a parenthesis
		 * @return this for chaining
		 */
		public Builder pushParenthesis() {
			addSpace();
			mStringBuilder.append("(");
			mcParenthesises++;
			return this;
		}

		/**
		 * Pop/End a parenthesis
		 * @return this for chaining
		 */
		public Builder popParenthesis() {
			mStringBuilder.append(")");
			mcParenthesises--;
			return this;
		}

		/**
		 * Adds an AND operator
		 * @return this for chaining
		 */
		public Builder and() {
			addSpace();
			mStringBuilder.append(CombineOperators.AND);
			return this;
		}

		/**
		 * Adds an or operator
		 * @return this for chaining
		 */
		public Builder or() {
			addSpace();
			mStringBuilder.append(CombineOperators.OR);
			return this;
		}

		/**
		 * Search for fields that are either true or false
		 * @param fieldName the field to search for
		 * @param value true or false
		 * @return this for chaining
		 */
		public Builder bool(String fieldName, boolean value) {
			if (value) {
				return isTrue(fieldName);
			} else {
				return isFalse(fieldName);
			}
		}

		/**
		 * Search for fields with this name that are true
		 * @param fieldName the field name to search for
		 * @return this for chaining
		 */
		public Builder isTrue(String fieldName) {
			addSpace();
			mStringBuilder.append(FieldOperators.EQUAL.combine(fieldName, TRUE));
			return this;
		}

		/**
		 * Search for fields with this name that are false
		 * @param fieldName the field name to search for
		 * @return this for chaining
		 */
		public Builder isFalse(String fieldName) {
			addSpace();
			mStringBuilder.append(FieldOperators.EQUAL.combine(fieldName, FALSE));
			return this;
		}

		/**
		 * Adds text to search
		 * @param text search for text
		 * @return this for chaining
		 */
		public Builder text(String text) {
			addSpace();
			mStringBuilder.append(text);
			return this;
		}

		/**
		 * Search for a text in a specific field. Automatically adds quotation marks at
		 * the beginning and end if none exists.
		 * @param text search for this text. Automatically adds quotation marks at the
		 *        beginning and end if none exists.
		 * @param fieldName the field name to search in
		 * @return this for chaining
		 */
		public Builder text(String text, String fieldName) {
			addSpace();
			mStringBuilder.append(FieldOperators.EQUAL.combine(fieldName, quote(text)));
			return this;
		}

		/**
		 * Search for a text in several specific fields. Automatically adds quotation
		 * marks at the beginning and end if none exists
		 * @param text search for this text. Automatically adds quotation marks at the
		 *        beginning and end if none exists.
		 * @param operator if searching in several fields use this operator between
		 * @param fieldNames all fields to search for the text in
		 * @return this for chaining
		 */
		public Builder text(String text, CombineOperators operator, String... fieldNames) {
			if (fieldNames.length > 0) {
				addSpace();
				String quotedText = quote(text);

				if (fieldNames.length > 1) {
					pushParenthesis();
				}

				for (int i = 0; i < fieldNames.length; ++i) {
					// Don't add operator before the first
					if (i != 0) {
						mStringBuilder.append(" ").append(operator);
					}

					// Add text field
					text(quotedText, fieldNames[i]);
				}

				if (fieldNames.length > 1) {
					popParenthesis();
				}
			}

			return this;
		}

		/**
		 * Search for different texts in the same field. Automatically adds quotation
		 * marks at the beginning and end if none exists.
		 * @param fieldName the field name to search in
		 * @param texts Search for all these texts. Automatically adds quotation marks at
		 *        the beginning and end if none exists.
		 * @return this for chaining
		 */
		public Builder text(String fieldName, String... texts) {
			if (texts.length > 0) {
				addSpace();

				if (texts.length > 1) {
					pushParenthesis();
				}

				for (int i = 0; i < texts.length; ++i) {
					// Don't add operator before the first
					if (i != 0) {
						or();
					}

					// Add text field
					text(texts[i], fieldName);
				}

				if (texts.length > 1) {
					popParenthesis();
				}
			}

			return this;
		}

		/**
		 * Quote the text to search for (if necessary)
		 * @param text the text to quote
		 * @return quoted text
		 */
		public String quote(String text) {
			String newText = text;
			if (text.contains(" ")) {
				// Check front
				if (!text.substring(0, 1).equals("\"")) {
					newText = "\"" + text;
				}
				// Check back
				if (!text.substring(text.length() - 1, text.length()).equals("\"")) {
					newText += "\"";
				}
			}
			return newText;
		}

		/**
		 * Add space in front of the next thing if necessary
		 */
		private void addSpace() {
			if (mStringBuilder.length() >= 1) {
				String lastChar = mStringBuilder.substring(mStringBuilder.length() - 1, mStringBuilder.length());
				if (!(lastChar.equals(" ") || lastChar.equals("("))) {
					mStringBuilder.append(" ");
				}
			}
		}

		/**
		 * Build the search string
		 * @return compiled search string
		 * @throws UnmatchedParenthesis if you didn't call {@link #pushParenthesis()} and
		 *         {@link #popParenthesis()} the same amount of time.
		 */
		public String build() {
			if (mcParenthesises != 0) {
				throw new UnmatchedParenthesis();
			}
			return mStringBuilder.toString();
		}


		/**
		 * Operators for the fields. Have skipped not as it should rather not be used
		 */
		@SuppressWarnings("javadoc")
		public enum FieldOperators {
			EQUAL(":"),
			LESS("<"),
			LESS_OR_EQUAL("<="),
			GREATER(">"),
			GREATER_OR_EQUAL(">="),

			;

			private FieldOperators(String textRepresentation) {
				mTextRepresentation = textRepresentation;
			}

			@Override
			public String toString() {
				return mTextRepresentation;
			}

			/**
			 * Create a full text representation with both field and values
			 * @param fieldName name of the field
			 * @param value the value of the field
			 */
			private String combine(String fieldName, String value) {
				return fieldName + toString() + value;
			}

			private String mTextRepresentation;
		}

		/**
		 * Combine operators
		 */
		@SuppressWarnings("javadoc")
		public enum CombineOperators {
			AND,
			OR,

			;

			@Override
			public String toString() {
				return name();
			}
		}

		/**
		 * Thrown when the number of parenthesis are unmatched
		 */
		public static class UnmatchedParenthesis extends RuntimeException {


			private static final long serialVersionUID = -1422426982731705689L;
		}

		private int mcParenthesises = 0;
		private StringBuilder mStringBuilder = new StringBuilder();
	}

	/** True field value */
	private static final String TRUE = "1";
	/** False field value */
	private static final String FALSE = "0";
	/** Tokenize length */
	private static final int TOKENIZE_LENGTH = 1;

	/** Put limit */
	private static final int PUT_LIMIT = 200;
}
