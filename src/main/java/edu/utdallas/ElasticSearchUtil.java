package edu.utdallas;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.utdallas.model.QuesAnswer;

public class ElasticSearchUtil {

	RestHighLevelClient client = null;
	ObjectMapper mapper = null;

	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchUtil.class);
	private static final String INDEX = "quesans";
	private static final String TYPE = "quesans";

	public ElasticSearchUtil() {

		client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
		mapper = new ObjectMapper();
	}

	/**
	 * Create elastic search index
	 * @param indexName
	 * @throws IOException
	 */
	public void createIndex(String indexName) throws IOException {

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
		createIndexRequest.settings(
				Settings.builder().put("index.number_of_shards", 3).put("index.mapping.total_fields.limit", 10000));

		client.indices().create(createIndexRequest);
	}

	/**
	 * Create index with index name and mappping
	 * @param indexName
	 * @param mapping
	 * @throws Exception
	 */
	public void createIndex(String indexName, String mapping) throws Exception {

		LOG.info("entered create Index");

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
		createIndexRequest.settings(
				Settings.builder().put("index.number_of_shards", 3).put("index.mapping.total_fields.limit", 10000));

		createIndexRequest.mapping(indexName, mapping, XContentType.JSON);

		client.indices().create(createIndexRequest);

		LOG.info("index created");
	}

	/**
	 * add all question to elastic search
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void addAllQuestions() throws JsonParseException, JsonMappingException, IOException {

		List<QuesAnswer> list = mapper.readValue(
				new File(System.getProperty("user.dir") + "/src/main/java/edu/utdallas/questions.json"),
				mapper.getTypeFactory().constructCollectionType(List.class, QuesAnswer.class));
		int i = 1;
		for (QuesAnswer a : list) {

			Map<String, Object> jsonMap = new HashMap<String, Object>();
			jsonMap.put("ques", a.getQues());
			jsonMap.put("ans", a.getAns());

			IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, Integer.toString(i)).source(jsonMap);

			client.index(indexRequest);
			i++;
		}

	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 *//*
	public List<QuesAnswer> getAllQuestion() throws IOException {

		SearchResponse response = matchAll(INDEX, TYPE);

		List<QuesAnswer> list = new ArrayList<QuesAnswer>();

		for (SearchHit hit : response.getHits().getHits()) {

			QuesAnswer answer = new QuesAnswer();
			answer.setQues(hit.getSourceAsMap().get("ques").toString());
			answer.setAns(hit.getSourceAsMap().get("ans").toString());
			answer.setId(hit.getId());
			list.add(answer);
		}

		return list;

	}*/

	/**
	 * 
	 * @param index
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public SearchResponse matchAll(String index, String type) throws IOException {
		
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		searchSourceBuilder.size(50);
		searchRequest.source(searchSourceBuilder);

		SearchResponse response = client.search(searchRequest);
		return response;
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public QuesAnswer getQuesAnswerById(String id) throws IOException {

		GetRequest getRequest = new GetRequest(INDEX, TYPE, id);
		GetResponse response = client.get(getRequest);

		QuesAnswer answer = new QuesAnswer();
		answer.setQues(response.getSourceAsMap().get("ques").toString());
		answer.setAns(response.getSourceAsMap().get("ans").toString());

		return answer;
	}
	
	
	/**
	 * 
	 * @param index
	 * @param type
	 * @param id
	 * @param map
	 * @throws IOException
	 */
	public void indexValue(String index, String type, String id, Map<String, String> map) throws IOException {

		IndexRequest indexRequest = new IndexRequest(index, type, id).source(map);

		client.index(indexRequest);

	}

	/**
	 * close the elastic search connection
	 * @throws IOException
	 */
	public void close() throws IOException {
		client.close();
	}
}
