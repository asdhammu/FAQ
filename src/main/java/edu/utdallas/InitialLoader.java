package edu.utdallas;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialLoader {

	public static final Logger LOG = LoggerFactory.getLogger(InitialLoader.class);
	
	public static void main(String[] args) throws IOException {
		
		
		ElasticSearchUtil searchUtil = new ElasticSearchUtil();
		
		String mapping = "  {\n" +
			    "    \"quesans\": {\n" +
			    "      \"properties\": {\n" +
			    "        \"ques\": {\n" +
			    "          \"type\": \"text\"\n" +
			    "        },\n" +
			    "		 \"ans\": {\n" +
			    "          \"type\": \"text\"\n" +
			    
			    "        }\n" +
			    "      }\n" +
			    "    }\n" +
			    "  }";	
		
		
		try {
			
			searchUtil.createIndex("quesans", mapping);			
			searchUtil.addAllQuestions();		
			
			FAQUtil.createBagOfWords();
			
		}catch(Exception e) {			
			LOG.error(e.getMessage());
		}finally {		
			searchUtil.close();
		}
	}
	
}
