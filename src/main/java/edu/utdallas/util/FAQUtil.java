package edu.utdallas.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
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

				Map<String, String> countToValue = new HashMap<String, String>();

				for (Map.Entry<String, String> m : map.entrySet()) {

					if (countToValue.containsKey(m.getValue())) {
						String value = countToValue.get(m.getValue()) + " " + m.getKey();
						countToValue.put(m.getValue(), value);
					} else {
						countToValue.put(m.getValue(), m.getKey());
					}
				}

				elasticSearchUtil.indexValue("bagofword", "doc", quesAns.getId(), countToValue);
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

	public static void createFeatures() {

		LOGGER.info("Starting creating bag of words");
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Morphology morphology = new Morphology();
		try {
			List<QuesAnswer> list = getAllQuestion();

			for (QuesAnswer ques : list) {

				Annotation document = new Annotation(ques.getQues() + " " + ques.getAns());

				pipeline.annotate(document);

				List<CoreMap> sentences = document.get(SentencesAnnotation.class);

				// SemanticHeadFinder finder = new SemanticHeadFinder();

				for (CoreMap sentence : sentences) {

					Map<String, String> map = new HashMap<String, String>();
					// traversing the words in the current sentence
					// a CoreLabel is a CoreMap with additional token-specific methods
					StringBuilder posBuilder = new StringBuilder();
					StringBuilder lemmaBuilder = new StringBuilder();
					StringBuilder stemBuilder = new StringBuilder();
					for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
						// this is the text of the token
						String word = token.get(TextAnnotation.class);
						// this is the POS tag of the token
						String pos = token.get(PartOfSpeechAnnotation.class);
						// this is the NER label of the token
						String ne = token.get(NamedEntityTagAnnotation.class);

						String lemma = token.lemma();

						String stem = morphology.stem(word);
						
						lemmaBuilder.append(lemma).append(",");
						posBuilder.append(pos).append(",");
						stemBuilder.append(stem).append(",");

					}

					posBuilder.setLength(posBuilder.length() - 1);
					lemmaBuilder.setLength(lemmaBuilder.length() - 1);
					stemBuilder.setLength(stemBuilder.length()-1);
					map.put("quesId", ques.getId());
					map.put("pos", posBuilder.toString());
					map.put("lemma", lemmaBuilder.toString());
					map.put("stem", stemBuilder.toString());
					// this is the parse tree of the current sentence

					Tree tree = sentence.get(TreeAnnotation.class);
					// System.out.println("parse tree:\n" + tree.toString());

					map.put("parseTree", tree.toString());

					// System.out.println("head" + finder.determineHead(tree));

					// this is the Stanford dependency graph of the current sentence
					SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
					map.put("dependencyGraph", dependencies.toString());
					// System.out.println("dependency graph:\n" + dependencies.toString());

					elasticSearchUtil.indexValue("feature", "doc", map);
					
				}
				
				LOGGER.info("Feature for " + ques.getId() + " question created");

			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

		
		LOGGER.info("features created");
	}

}
