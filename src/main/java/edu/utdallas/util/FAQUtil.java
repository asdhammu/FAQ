package edu.utdallas.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

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
	
	public static SearchHit[] getFeatures() throws IOException{
		
		SearchResponse response = elasticSearchUtil.matchAll("feature", "doc");

		return response.getHits().getHits();
	}

	public static void createFeatures() throws JWNLException {

		LOGGER.info("Starting creating features");

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Morphology morphology = new Morphology();
		Dictionary dictionary = null;
		try {

			dictionary = Dictionary.getDefaultResourceInstance();

		} catch (JWNLException e) {
			e.printStackTrace();
		}
		try {
			List<QuesAnswer> list = getAllQuestion();

			Set<String> stopWords = getStopWords();
			
			for (QuesAnswer ques : list) {

				Annotation document = new Annotation(ques.getQues() + " " + ques.getAns());

				pipeline.annotate(document);

				List<CoreMap> sentences = document.get(SentencesAnnotation.class);

				for (CoreMap sentence : sentences) {

					Map<String, String> map = new HashMap<String, String>();
					// traversing the words in the current sentence
					// a CoreLabel is a CoreMap with additional token-specific methods
					StringBuilder posBuilder = new StringBuilder();
					StringBuilder lemmaBuilder = new StringBuilder();
					StringBuilder stemBuilder = new StringBuilder();

					StringBuilder exclueStopWords = new StringBuilder();
					Set<String> hypernymSet = new HashSet<String>();
					Set<String> synonymSet = new HashSet<String>();
					Set<String> hyponymSet = new HashSet<String>();
					
					extractFeatures(morphology, dictionary, stopWords, sentence, posBuilder, lemmaBuilder, stemBuilder,
							exclueStopWords, hypernymSet, synonymSet, hyponymSet);
					
					posBuilder.setLength(posBuilder.length() - 1);
					lemmaBuilder.setLength(lemmaBuilder.length() - 1);
					stemBuilder.setLength(stemBuilder.length() - 1);
					exclueStopWords.setLength(exclueStopWords.length()-1);
					map.put("quesId", ques.getId());
					map.put("pos", posBuilder.toString());
					map.put("lemma", lemmaBuilder.toString());
					map.put("stem", stemBuilder.toString());

					
					
					map.put("hypernym", convertToString(hypernymSet));
					map.put("synonym", convertToString(synonymSet));
					map.put("hyponym", convertToString(hyponymSet));
					map.put("excludeStop", exclueStopWords.toString());
					Tree tree = sentence.get(TreeAnnotation.class);

					map.put("parseTree", tree.toString());

					// this is the Stanford dependency graph of the current sentence
					SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
					map.put("dependencyGraph", dependencies.toString());

					elasticSearchUtil.indexValue("feature", "doc", map);

				}

				LOGGER.info("Feature for " + ques.getId() + " question created");

			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

		LOGGER.info("features created");
	}

	
	public static String convertToString(Set<String> set) {
		
		StringBuilder builder = new StringBuilder();
		
		for(String s:set) {
			builder.append(s).append(",");
		}
		if(builder.length()>1) {
			builder.setLength(builder.length()-1);
		}
		
		return builder.toString();
				
	}
	
	public static void extractFeatures(Morphology morphology, Dictionary dictionary, Set<String> stopWords,
			CoreMap sentence, StringBuilder posBuilder, StringBuilder lemmaBuilder, StringBuilder stemBuilder,
			StringBuilder exclueStopWords, Set<String> hypernymSet, Set<String> synonymSet, Set<String> hyponymSet)
			throws JWNLException {
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			// this is the text of the token
			String word = token.get(TextAnnotation.class);
			// this is the POS tag of the token
			String pos = token.get(PartOfSpeechAnnotation.class);

			String lemma = token.lemma();

			String stem = morphology.stem(word);

			lemmaBuilder.append(lemma).append(",");
			posBuilder.append(word.toLowerCase()).append("_").append(pos).append(",");
			stemBuilder.append(stem).append(",");

			if(!stopWords.contains(word)) {
				exclueStopWords.append(word).append(",");
			}
			
			if (pos.equalsIgnoreCase("NN") || pos.equalsIgnoreCase("NNP")) {
				IndexWord set = dictionary.lookupIndexWord(POS.NOUN, word);

				if (set == null) {
					continue;
				}
				List<Synset> synset = set.getSenses();

				for (Synset syn : synset) {

					if (PointerUtils.getSynonyms(syn).size() > 0) {
						List<net.sf.extjwnl.data.Word> words = PointerUtils.getSynonyms(syn).get(0)
								.getSynset().getWords();

						for (net.sf.extjwnl.data.Word w : words) {
							synonymSet.add(w.getLemma());
						}
					}

					if (PointerUtils.getDirectHypernyms(syn).size() > 0) {
						List<net.sf.extjwnl.data.Word> words = PointerUtils.getDirectHypernyms(syn).get(0)
								.getSynset().getWords();

						for (net.sf.extjwnl.data.Word w : words) {
							hypernymSet.add(w.getLemma());
						}
					}

					if (PointerUtils.getDirectHyponyms(syn).size() > 0) {
						List<net.sf.extjwnl.data.Word> words = PointerUtils.getDirectHyponyms(syn).get(0)
								.getSynset().getWords();

						for (net.sf.extjwnl.data.Word w : words) {
							hyponymSet.add(w.getLemma());
						}

					}

				}

			}

		}
	}
	
	
	
	
	public static Set<String> getStopWords() throws IOException{
	
	  LOGGER.info("Loading stop words");
      Set<String> set = new HashSet<String>();
	  File file = new File(System.getProperty("user.dir") + "/src/main/resources/stopWords.txt");
	 
	  BufferedReader br = new BufferedReader(new FileReader(file));
	 
	  String st;
	  while ((st = br.readLine()) != null) {	    
	    set.add(st);
	  }
		
	  br.close();
	
	  LOGGER.info("Stop words loaded");
	  
	  return set;
	  
	}
	

}
