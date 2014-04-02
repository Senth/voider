package com.spiddekauga.appengine;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.Query.Builder;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchQueryException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;

/**
 * Search utilities for Google App Engine
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SearchUtils {
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
	public static boolean indexDocuments(String indexName, ArrayList<Document> documents) {
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

		optionsBuilder.setLimit(limit);

		if (cursor != null) {
			optionsBuilder.setCursor(cursor);
		}

		Builder queryBuilder = Query.newBuilder().setOptions(optionsBuilder);

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
	 * @param minSize the minimum size of the tokens/auto-complete, if a word is
	 * shorter than this size it will still be added as a token.
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

	/** Put limit */
	private static final int PUT_LIMIT = 200;
}
