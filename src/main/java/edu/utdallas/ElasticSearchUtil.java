package edu.utdallas;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.utdallas.model.QuesAnswer;

public class ElasticSearchUtil {

	RestHighLevelClient client = null;	
	ObjectMapper mapper = null;
	
	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchUtil.class);
	
	public ElasticSearchUtil() {
		
		client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")
						)
				);
		mapper = new ObjectMapper();
	}
	
	
	
	public void createIndex(String indexName, String mapping) throws Exception {
				
		LOG.info("entered create Index");
		
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);  
		createIndexRequest.settings(Settings.builder().put("index.number_of_shards",3));
		
		createIndexRequest.mapping(indexName, mapping, XContentType.JSON);
		
		client.indices().create(createIndexRequest);
		
		LOG.info("index created");
	}
	
	
	public void BulkRequest() throws JsonParseException, JsonMappingException, IOException {
		
		
		List<QuesAnswer> list = mapper.readValue("questions.json", new TypeReference<List<QuesAnswer>>(){});
		
		
		
	}
	
		
	
	public void close() throws IOException {
		client.close();
	}
}
