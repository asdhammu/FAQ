package edu.utdallas;

import java.util.PriorityQueue;

import edu.utdallas.model.QuesAnswer;

public class Callable {

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
