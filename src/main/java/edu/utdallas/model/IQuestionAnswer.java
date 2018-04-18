package edu.utdallas.model;

import java.util.PriorityQueue;

public interface IQuestionAnswer {

	PriorityQueue<QuesAnswer> getTopTenQuestions(String query);
}

