package edu.utdallas.algorithm;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.factory.ElasticSearchFactory;
import edu.utdallas.model.IQuestionAnswer;
import edu.utdallas.model.QuesAnswer;
import edu.utdallas.util.ElasticSearchUtil;
import edu.utdallas.util.FAQUtil;

public class BagOfWords implements IQuestionAnswer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BagOfWords.class);	

	public PriorityQueue<QuesAnswer> getTopTenQuestions(String query) {

		LOGGER.info("query is:- " + query);

		ElasticSearchUtil elasticSearchUtil = ElasticSearchFactory.getSearchUtil();
		
		PriorityQueue<QuesAnswer> priorityQueue = new PriorityQueue<QuesAnswer>(10, new Comparator<QuesAnswer>() {
			public int compare(QuesAnswer o1, QuesAnswer o2) {

				if (o2.getCount() < o1.getCount()) {
					return -1;
				}
				if (o2.getCount() > o1.getCount()) {
					return 1;
				}

				return 0;
			}
		});

		try {

			String[] querySplit = query.split(" ");

			SearchHit[] hits = FAQUtil.getBagOfWords();

			for (SearchHit hit : hits) {

				int count = 0;

				Map<String, Object> map = hit.getSourceAsMap();

				
				for (Map.Entry<String, Object> m : map.entrySet()) {

					String[] split = ((String)m.getValue()).split(" ");
					
					for(String sp:split) {
						for (String s : querySplit) {
							if (s.equalsIgnoreCase(sp)) {
								count += Integer.parseInt((String) m.getKey());
							}
						}
					}					
				}

				QuesAnswer quesAnswer = elasticSearchUtil.getQuesAnswerById(hit.getId());
				quesAnswer.setId(hit.getId());
				quesAnswer.setCount(count);

				priorityQueue.add(quesAnswer);
			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

		return priorityQueue;
	}

}
