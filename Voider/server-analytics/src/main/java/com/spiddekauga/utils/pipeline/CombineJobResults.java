package com.spiddekauga.utils.pipeline;

import java.util.List;

import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Value;

/**
 * Waits for and combines the result from multiple jobs
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <ReturnType> what the jobs return
 */
public class CombineJobResults<ReturnType> extends Job1<List<ReturnType>, List<ReturnType>> {
	private static final long serialVersionUID = -7143355720759335535L;

	@Override
	public Value<List<ReturnType>> run(List<ReturnType> jobOutputs) throws Exception {
		return immediate(jobOutputs);
	}
}
