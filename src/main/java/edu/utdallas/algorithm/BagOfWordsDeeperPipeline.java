package edu.utdallas.algorithm;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.factory.ElasticSearchFactory;
import edu.utdallas.model.IQuestionAnswer;
import edu.utdallas.model.QuesAnswer;
import edu.utdallas.util.ElasticSearchUtil;

public class BagOfWordsDeeperPipeline implements IQuestionAnswer{

	private static final Logger LOGGER = LoggerFactory.getLogger(BagOfWords.class);

	private static ElasticSearchUtil elasticSearchUtil = ElasticSearchFactory.getSearchUtil();
	
	public PriorityQueue<QuesAnswer> getTopTenQuestions(String query) {
		
		return null;
	}

}
