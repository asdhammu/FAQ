package edu.utdallas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import edu.utdallas.model.QuesAnswer;

public class FAQUtil {

	private static ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();

	public static void createBagOfWords() {

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
			e.printStackTrace();
		} finally {
			try {
				elasticSearchUtil.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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
