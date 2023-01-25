package com.ian.vca.configurations;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.ian.vca.models")
@ComponentScan(basePackages = "com.ian.vca.models")
public class ElasticConfiguration {

	@Bean
    public RestClient restClient() {
    	
    	HttpHost httpHost = new HttpHost("localhost", 9200, "http");
    	RestClientBuilder builder = RestClient.builder(httpHost);
    	
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials("ian", "ianelastic"));
		
		// customize rest client with extra configuration
		HttpClientConfigCallback httpClientConfigCallback = (httpClientBuilder) -> {
			// add authorization
			httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			return httpClientBuilder;
		};
	
		builder.setHttpClientConfigCallback(httpClientConfigCallback);
    	return builder.build();
    }
}
