package edu.utdallas.algorithm;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.util.CoreMap;
import edu.utdallas.factory.ElasticSearchFactory;
import edu.utdallas.model.QuesAnswer;
import edu.utdallas.util.ElasticSearchUtil;
import edu.utdallas.util.FAQUtil;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

public class ClassifierAlogrithm {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassifierAlogrithm.class);	
	
	public PriorityQueue<QuesAnswer> getTopTenQuestions(String query, StanfordCoreNLP pipeline, Morphology morphology, Dictionary dictionary, Set<String> stopWords ){
		
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
		
		StringBuilder posBuilder = new StringBuilder();
		StringBuilder lemmaBuilder = new StringBuilder();
		StringBuilder stemBuilder = new StringBuilder();

		StringBuilder exclueStopWords = new StringBuilder();
		Set<String> hypernymSet = new HashSet<String>();
		Set<String> synonymSet = new HashSet<String>();
		Set<String> hyponymSet = new HashSet<String>();
		
		try {
			Annotation document = new Annotation(query);			
			pipeline.annotate(document);

			
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);

			for(CoreMap sentence: sentences) {
				
				FAQUtil.extractFeatures(morphology, dictionary, stopWords, sentence, posBuilder, lemmaBuilder, stemBuilder, exclueStopWords, hypernymSet, synonymSet, hyponymSet);				
			}
			
			SearchHit[] hits = FAQUtil.getFeatures();
			Map<String, Integer> countMap = new HashMap<String, Integer>();
			for(SearchHit hit: hits) {
				int count = 0;
				Map<String, Object> map = hit.getSourceAsMap();
				Set<String> posSet = new HashSet<String>();
				String[] pos = ((String) map.get("pos")).split(",");
				String quesId = (String) map.get("quesId");
				for(String s: pos) {
					posSet.add(s);
				}
				
				String[] queryPos = posBuilder.toString().split(",");
				
				for(String q: queryPos) {
					if(posSet.contains(q)) {
						count++;
					}
				}
				
				if(countMap.containsKey(quesId)) {
					
					int c = countMap.get(quesId);
					c+=count;
					countMap.put(quesId, c);
				}else {
					countMap.put(quesId, count);
				}							
				
			}
			

			for(Map.Entry<String, Integer> m: countMap.entrySet()) {
				QuesAnswer quesAnswer = elasticSearchUtil.getQuesAnswerById(m.getKey());				
				quesAnswer.setCount(m.getValue());
				priorityQueue.add(quesAnswer);	
			}
			
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} catch (JWNLException e) {
			LOGGER.error(e.getMessage());
		}
		
		
		
		
		return priorityQueue;
	}
}
