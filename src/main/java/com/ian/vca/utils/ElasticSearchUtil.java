package com.ian.vca.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting.SortOrder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ElasticSearchUtil {
	
	private final String SEARCH = "_search";
	private final String CREATE_OR_DELETE = "_doc";
	private final String CREATE_ID = "_create";
	private final String UPDATE = "_update";

	@Autowired
	private ObjectMapper objectMapper;
	
	public <T> RequestBuilder<T> create(Class<T> entity) {
		return new RequestBuilder<>(HttpMethod.POST, entity, CREATE_OR_DELETE, objectMapper);
	}
	
	public <T> RequestBuilder<T> createWithId(Class<T> entity) {
		return new RequestBuilder<>(HttpMethod.POST, entity, CREATE_ID, objectMapper);
	}
	
	public <T> RequestBuilder<T> search(Class<T> entity) {
		return new RequestBuilder<>(HttpMethod.GET, entity, SEARCH, objectMapper);
	}
	
	public <T> RequestBuilder<T> searchById(Class<T> entity, String entityId) {
		return new RequestBuilder<>(HttpMethod.GET, entity, CREATE_OR_DELETE, objectMapper, entityId);
	}
	
	public <T> RequestBuilder<T> update(Class<T> entity, String entityId) {
		return new RequestBuilder<>(HttpMethod.POST, entity, UPDATE, objectMapper, entityId);
	}
	
	public <T> RequestBuilder<T> delete(Class<T> entity, String entityId) {
		return new RequestBuilder<>(HttpMethod.DELETE, entity, CREATE_OR_DELETE, objectMapper, entityId);
	}
	
	public List<Map<String, Object>> extractHits(HttpEntity responseEntity) throws ParseException, IOException {
		
		String responseString = EntityUtils.toString(responseEntity);
		HashMap<String, Object> responseMap = objectMapper.readValue(responseString, new TypeReference<HashMap<String, Object>>() {});
		
		boolean successParentHits = responseMap.containsKey("hits");
		
		if (successParentHits) {
			@SuppressWarnings("unchecked")
			Map<String, Object> parentHits = (Map<String, Object>) responseMap.get("hits");
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> childHits = (List<Map<String, Object>>) parentHits.get("hits");
			
			return childHits;
		} else {
			return new LinkedList<>();
		}
	}
	
	public Long extractSearchTotalElement(HttpEntity responseEntity) throws ParseException, IOException {
		
		String responseString = EntityUtils.toString(responseEntity);
		HashMap<String, Object> responseMap = objectMapper.readValue(responseString, new TypeReference<HashMap<String, Object>>() {});
		
		boolean successParentHits = responseMap.containsKey("hits");
		
		if (successParentHits) {
			@SuppressWarnings("unchecked")
			Map<String, Object> parentHits = (Map<String, Object>) responseMap.get("hits");
			@SuppressWarnings("unchecked")
			Map<String, Object> total = (Map<String, Object>) parentHits.get("total");
			Integer value = (Integer) total.get("value");
			
			return Long.valueOf(value);
		} else {
			return 0l;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> extractAggregationBuckets(HttpEntity responseEntity, String aggregationName) throws ParseException, IOException {
		
		String responseString = EntityUtils.toString(responseEntity);
		HashMap<String, Object> responseMap = objectMapper.readValue(responseString, new TypeReference<HashMap<String, Object>>() {});
		
		boolean successFetchAggregations = responseMap.containsKey("aggregations");
		if (successFetchAggregations) {
			
			Map<String, Object> aggregations = (Map<String, Object>) responseMap.get("aggregations");
			Map<String, Object> aggregationsResult = (Map<String, Object>) aggregations.get(aggregationName);
			List<Map<String, Object>> buckets = (List<Map<String, Object>>) aggregationsResult.get("buckets");
			
			return buckets;
		} else {
			return new LinkedList<>();
		}
	}

	public static class RequestBuilder<T> {
		
		private final ObjectMapper objectMapper;

		private HttpMethod httpMethod;
		private Class<T> entity;
		private String operation;
		private String entityId;
		private Map<String, Object> requestBody;

		public RequestBuilder(HttpMethod httpMethod, Class<T> entity, String operation, ObjectMapper objectMapper) {
			this(httpMethod, entity, operation, objectMapper, null);
		};
		
		public RequestBuilder(HttpMethod httpMethod, Class<T> entity, String operation, ObjectMapper objectMapper, String entityId) {
			this.objectMapper = objectMapper;
			this.httpMethod = httpMethod;
			this.entity = entity;
			this.operation = operation;
			this.entityId = entityId;
			this.requestBody = new HashMap<>();
		};

		public RequestBuilder<T> collapse(String field) {
			Map<String, Object> collapseField = new HashMap<>();
			collapseField.put("field", field);
			this.requestBody.put("collapse", collapseField);
			return this;
		}

		public RequestBuilder<T> from(Integer from) {
			this.requestBody.put("from", from);
			return this;
		}

		public RequestBuilder<T> size(Integer size) {
			this.requestBody.put("size", size);
			return this;
		}

		public RequestBuilder<T> sort(String field, SortOrder direction) {
			boolean exists = this.requestBody.containsKey("sort");

			if (exists) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sortList = (List<Map<String, Object>>) this.requestBody.get("sort");
				Map<String, Object> sortField = new HashMap<>();
				Map<String, Object> sortDirection = new HashMap<>();

				sortDirection.put("order", direction);
				sortField.put(field, sortDirection);

				sortList.add(sortField);
			} else {
				List<Map<String, Object>> sortList = new LinkedList<>();
				Map<String, Object> sortField = new HashMap<>();
				Map<String, Object> sortDirection = new HashMap<>();

				sortDirection.put("order", direction.toString());
				sortField.put(field, sortDirection);

				sortList.add(sortField);
				this.requestBody.put("sort", sortList);
			}
			return this;
		}
		
		public <V> RequestBuilder<T> booleanFilter(String field, V value) {

			Map<String, Object> booleanQuery = getOrCreateFieldTree(this.requestBody, "query", "bool");
			List<Map<String, Object>> filterQuery = getOrCreateArrayField(booleanQuery, "filter");

			Map<String, Object> termParam = new HashMap<>();
			termParam.put(field, value);
			Map<String, Object> termQuery = new HashMap<>();
			termQuery.put("term", termParam);

			filterQuery.add(termQuery);

			return this;
		}
		
		public RequestBuilder<T> booleanFilterRange(String field, Long valueMin, Long valueMax) {
			
			Map<String, Object> booleanQuery = getOrCreateFieldTree(this.requestBody, "query", "bool");
			List<Map<String, Object>> filterQuery = getOrCreateArrayField(booleanQuery, "filter");
			
			Map<String, Object> fieldParams = new HashMap<>();
			fieldParams.put("gte", valueMin);
			fieldParams.put("lte", valueMax);
			
			Map<String, Object> filteredFieldName = new HashMap<>();
			filteredFieldName.put(field, fieldParams);
			
			Map<String, Object> rangeQuery = new HashMap<>();
			rangeQuery.put("range", filteredFieldName);
			
			filterQuery.add(rangeQuery);
			
			return this;
		}
		
		public <V> RequestBuilder<T> wildcard(String field, V value) {
			
			Map<String, Object> wildcardQuery = getOrCreateFieldTree(this.requestBody, "query", "wildcard");
			Map<String, Object> fieldParam = new HashMap<>();
			
			fieldParam.put("value", value);
			wildcardQuery.put(field, fieldParam);
			
			return this;
		}
		
		public <V> RequestBuilder<T> addDocUpdate(String field, V value) {
			Map<String, Object> docQuery = getOrCreateFieldTree(this.requestBody, "doc");
			docQuery.put(field, value);
			return this;
		}
		
		public RequestBuilder<T> matchPhrasePrefix(String field, String value) {
			Map<String, Object> matchPhrasePrefix = getOrCreateFieldTree(this.requestBody, "query", "match_phrase_prefix");
			matchPhrasePrefix.put(field, value);
			
			return this;
		}
		
		public RequestBuilder<T> aggregateTerms(String aggregateName, String field, Integer size) {
			
			log.info("Generate Aggregate terms");
			Map<String, Object> aggsTerm = getOrCreateFieldTree(this.requestBody, "aggs", aggregateName, "terms");
			aggsTerm.put("field", field);
			aggsTerm.put("size", size);
			
			log.info("Generated aggregate terms: {}", aggsTerm);
			
			return this;
		}
		
		public RequestBuilder<T> subAgggregateTopHits(String[] parentAggregatePath, String subAggregateName, 
				Integer size, String[] includeFieldSource) {
			log.info("Generate sub aggregate top hits");
			
			Map<String, Object> parentAggregate = getOrCreateFieldTree(this.requestBody, parentAggregatePath);
			Map<String, Object> subAggregate = getOrCreateFieldTree(parentAggregate, "aggs", subAggregateName, "top_hits");
			Map<String, Object> includedSources = new HashMap<>();
			includedSources.put("includes", includeFieldSource);
			
			subAggregate.put("size", size);
			subAggregate.put("_source", includedSources);
			
			log.info("Generated sub aggregate top hits: {}", parentAggregate);
			
			return this;
		}
		
		@SuppressWarnings("unchecked")
		private Map<String, Object> getOrCreateFieldTree(Map<String, Object> root, String... fields) {
			
			List<String> fieldsTree = Arrays.asList(fields);
			
			Map<String, Object> currentField = root;
			for (String field : fieldsTree) {
				boolean fieldExists = currentField.containsKey(field);
				
				if (fieldExists) {
					currentField = (Map<String, Object>) currentField.get(field);
				} else {
					Map<String, Object> newField = new HashMap<>();
					currentField.put(field, newField);
					currentField = newField;
				}
			}
			
			return currentField;
		}
		
		@SuppressWarnings("unchecked")
		private List<Map<String, Object>> getOrCreateArrayField(Map<String, Object> root, String field) {
			
			boolean fieldExists = root.containsKey(field);
			List<Map<String, Object>> arrayField = null;
			if (fieldExists) {
				arrayField = (List<Map<String, Object>>) root.get(field);
			} else {
				arrayField = new LinkedList<>();
				root.put(field, arrayField);
			}
			
			return arrayField;
		}
		
		public Request build() throws JsonProcessingException {
			return this.build(null);
		}
		
		public Request build(T entityInstance) throws JsonProcessingException {
			
			String indexName = this.entity.getAnnotation(Document.class).indexName();
			
			StringBuilder urlBuilder = new StringBuilder("/");
			urlBuilder.append(indexName);
			urlBuilder.append("/");
			urlBuilder.append(operation);
			
			if(entityId != null) {
				urlBuilder.append("/");
				urlBuilder.append(entityId);
			}
			
			Object bodyObject = entityInstance == null ? this.requestBody : entityInstance;
			Request request = new Request(httpMethod.name(), urlBuilder.toString());
			String jsonRequestBody = objectMapper.writeValueAsString(bodyObject);
			
			log.info("Sending request body to elastic: {}", jsonRequestBody);
			log.info("METHOD ELASTIC: {}", request.getMethod());
			log.info("URI ELASTIC: {}", urlBuilder.toString());
			
			request.setOptions(RequestOptions.DEFAULT);
			request.setJsonEntity(jsonRequestBody);
			
			return request;
		}
	}

}
