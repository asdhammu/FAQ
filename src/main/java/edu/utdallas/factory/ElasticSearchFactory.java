package edu.utdallas.factory;

import edu.utdallas.util.ElasticSearchUtil;

public class ElasticSearchFactory {

	private static final ElasticSearchUtil SEARCH_UTIL = new ElasticSearchUtil();
	
	public ElasticSearchFactory() {
	}
	
	public static ElasticSearchUtil getSearchUtil() {
		
		return SEARCH_UTIL;
	}
	
}
