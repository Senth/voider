package com.spiddekauga.utils.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Value;

/**
 * Waits for and combines the result from multiple MapReduce jobs

 * @param <ReturnType> what the jobs return
 */
public class CombineMapResults<ReturnType> extends Job1<List<ReturnType>, List<MapReduceResult<ReturnType>>> {
	private static final long serialVersionUID = -7143355720759335535L;

	@Override
	public Value<List<ReturnType>> run(List<MapReduceResult<ReturnType>> mapOutputs) throws Exception {
		List<ReturnType> combinedList = new ArrayList<>();
		for (MapReduceResult<ReturnType> mapReduceResult : mapOutputs) {
			combinedList.add(mapReduceResult.getOutputResult());
		}
		return immediate(combinedList);
	}
}
