package edu.utdallas.main;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.algorithm.BagOfWords;
import edu.utdallas.model.QuesAnswer;

public class Callable {

	public static final Logger LOGGER = LoggerFactory.getLogger(Callable.class);
	
	public static void main(String[] args) {

		String query = "coinbase";
		BagOfWords bagOfWords = new BagOfWords();
		
		PriorityQueue<QuesAnswer> list = bagOfWords.getTopTenQuestions(query);

		while (list.peek() != null) {

			QuesAnswer peek = list.peek();
			
			System.out.println(peek.getQues() + " " + peek.getCount());

			list.remove();
		}
	}
}
