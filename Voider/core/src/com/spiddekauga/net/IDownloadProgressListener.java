package com.spiddekauga.net;

/**
 * Listens to file downloads
 */
public interface IDownloadProgressListener extends IProgressListener {
/**
 * Called whenever a file has been downloaded
 * @param cComplete total number of downloaded files
 * @param cTotal total number of files to download
 */
void handleFileDownloaded(int cComplete, int cTotal);
}
