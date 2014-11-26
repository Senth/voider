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
	 * Get a float number from the specified field. If multiple fields exists with this
	 * name the first valid field will be returned.
	 * @param document the document to get the number from
	 * @param fieldName name of the field
	 * @return float value of the field, 0 if the field doesn't exist or isn't a number
	 */
	public static float getFloat(Document document, String fieldName) {
		Iterable<Field> fields = document.getFields(fieldName);
		if (fields != null) {
			for (Field field : fields) {
				if (field.getType() != null && field.getType() == FieldType.NUMBER) {
					Double number = field.getNumber();
					if (number != null) {
						return number.floatValue();
					}
				}
			}
		}
		return 0;
	}

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
				index.put(document);
				retry = false;
			} catch (PutException e) {
				if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
					retry = true;
				} else {
					return false;
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
				index.put(documents);
				retry = false;
			} catch (PutException e) {
				if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
					retry = true;
				} else {
					return false;
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
		try {
			foundDocuments = index.search(queryBuilder.build(searchQuery));
		} catch (SearchQueryException e) {
			// Do nothing
		}

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
	public static String getAtom(boolean value) {
		return value ? TRUE : FALSE;
	}

	/**
	 * Converts a string boolean atom to a java boolean value
	 * @param atomBoolean the atom boolean value
	 * @return java boolean value
	 */
	public static boolean getBoolean(String atomBoolean) {
		if (atomBoolean != null) {
			if (atomBoolean.equals(TRUE)) {
				return true;
			}
		}

		return false;
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

	/** True field value */
	private static final String TRUE = "1";
	/** False field value */
	private static final String FALSE = "0";
	/** Tokenize length */
	private static final int TOKENIZE_LENGTH = 1;

	/** Put limit */
	private static final int PUT_LIMIT = 200;
}
