package com.spiddekauga.voider.backup;

import com.spiddekauga.voider.network.misc.BlobDownloadMethod;

/**
 * A test action
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TestAction extends Action {

	/**
	 *
	 */
	public TestAction() {
		setBackupDir("NOTHING");
	}

	@Override
	public void execute() {
		BlobDownloadMethod blobDownloadMethod = new BlobDownloadMethod(
				"AMIfv96GuIxrVyWyJLNIXfMbROWgZeDWN62gC_OyDs3SFljg-Xsh0_vK2WTsJT4iQNebH4MYHMGFgrP4kh0dr1rRRQ_QnSWjyzJD1gfQhS_cxZJvI88gL5TOC3GZJJFbIDqWKVLWJcwV7LtktEhB5OQ7L563y0GjmA");

		while (true) {
			callServerMethod(blobDownloadMethod);
		}
	}

}
