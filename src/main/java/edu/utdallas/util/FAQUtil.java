package edu.utdallas.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.factory.ElasticSearchFactory;
import edu.utdallas.model.QuesAnswer;

public class FAQUtil {

	private static ElasticSearchUtil elasticSearchUtil = ElasticSearchFactory.getSearchUtil();

	private static final Logger LOGGER = LoggerFactory.getLogger(FAQUtil.class);
	
	public static void createBagOfWords() {

		LOGGER.info("Starting creating bag of words");
		try {

			elasticSearchUtil.createIndex("bagofword");

			List<QuesAnswer> list = getAllQuestion();

			for (QuesAnswer quesAns : list) {
				Map<String, String> map = new HashMap<String, String>();

				String[] appended = (quesAns.getQues().trim() + " " + quesAns.getAns().trim()).split(" ");

				for (String s : appended) {

					if (map.containsKey(s)) {
						map.put(s, Integer.toString(Integer.parseInt(map.get(s)) + 1));
					} else {
						map.put(s, Integer.toString(1));
					}

				}

				elasticSearchUtil.indexValue("bagofword", "doc", quesAns.getId(), map);
			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		
		LOGGER.info("Bag of words created");

	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<QuesAnswer> getAllQuestion() throws IOException {

		SearchResponse response = elasticSearchUtil.matchAll("quesans", "quesans");

		List<QuesAnswer> list = new ArrayList<QuesAnswer>();

		for (SearchHit hit : response.getHits().getHits()) {

			QuesAnswer answer = new QuesAnswer();
			answer.setQues(hit.getSourceAsMap().get("ques").toString());
			answer.setAns(hit.getSourceAsMap().get("ans").toString());
			answer.setId(hit.getId());
			list.add(answer);
		}

		return list;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static SearchHit[] getBagOfWords() throws IOException {

		SearchResponse response = elasticSearchUtil.matchAll("bagofword", "doc");

		return response.getHits().getHits();

	}

}
