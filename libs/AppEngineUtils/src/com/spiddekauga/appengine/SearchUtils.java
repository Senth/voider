package com.spiddekauga.appengine;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;

/**
 * Search utilities for Google App Engine
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SearchUtils {
	/**
	 * Index a document
	 * @param indexName name of the index to put the document in
	 * @param document the document to index
	 * @return true if successfully added document to index
	 */
	public static boolean indexDocument(String indexName, Document document) {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

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
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

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

	/** Put limit */
	private static final int PUT_LIMIT = 200;
}
