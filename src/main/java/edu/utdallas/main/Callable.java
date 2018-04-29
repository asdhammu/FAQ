package edu.utdallas.main;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.utdallas.algorithm.BagOfWords;
import edu.utdallas.algorithm.ClassifierAlogrithm;
import edu.utdallas.model.QuesAnswer;
import edu.utdallas.util.FAQUtil;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

public class Callable {

	public static final Logger LOGGER = LoggerFactory.getLogger(Callable.class);
	
	public static void main(String[] args) {

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Morphology morphology = new Morphology();
		Dictionary dictionary = null;
		Set<String> stopWords = null;
		try {

			dictionary = Dictionary.getDefaultResourceInstance();
			stopWords = FAQUtil.getStopWords();
		} catch (JWNLException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {			
			LOGGER.error(e.getMessage());
		}
		 
		Scanner scanner = new Scanner(System.in);
		
		while(true) {
			
			LOGGER.info("Enter your query:-");
			String query = scanner.nextLine();
			
			if(query.equalsIgnoreCase("exit")) {
				break;
			}			
			/*BagOfWords bagOfWords = new BagOfWords();
			
			PriorityQueue<QuesAnswer> list = bagOfWords.getTopTenQuestions(query);
			int i=0;
			while (list.peek() != null) {

				if(i>10) {
					break;
				}
				QuesAnswer peek = list.peek();
				
				System.out.println(peek.getQues() + " " + peek.getCount());

				list.remove();
				i++;
			}*/
			
			ClassifierAlogrithm algorithm = new ClassifierAlogrithm();
			
			//algorithm.getTopTenQuestions(query, pipeline, morphology, dictionary, stopWords);
			
			PriorityQueue<QuesAnswer> list = algorithm.getTopTenQuestions(query, pipeline, morphology, dictionary, stopWords);
			int i=0;
			while (list.peek() != null) {

				if(i>10) {
					break;
				}
				QuesAnswer peek = list.peek();
				
				System.out.println(peek.getQues() + " " + peek.getCount());

				list.remove();
				i++;
			}
			
		}
		
		scanner.close();
		LOGGER.info("program exit");
		
		
	}
}
