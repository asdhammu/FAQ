package edu.utdallas;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.utdallas.model.QuesAnswer;

public class FAQUtil {

	public static void createBagOfWords() {

		ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();

		try {
			elasticSearchUtil.createIndex("bagofword");

			List<QuesAnswer> list = elasticSearchUtil.getAllQuestion();

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

}
