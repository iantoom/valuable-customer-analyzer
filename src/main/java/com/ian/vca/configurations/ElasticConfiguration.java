package com.ian.vca.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.ian.vca.models")
@ComponentScan(basePackages = "com.ian.vca.models")
public class ElasticConfiguration extends ReactiveElasticsearchConfiguration{
	
	@Value("${elastic.host}")
	private String elasticHost;
	
	@Value("${elastic.scheme}")
	private String elasticScheme;
	
	@Value("${elastic.username}")
	private String elasticUsername;
	
	@Value("${elastic.password}")
	private String elasticPassword;

	@Override
	public ClientConfiguration clientConfiguration() {
		
		return ClientConfiguration.builder()           
				.connectedTo(elasticHost)
				.withBasicAuth(elasticUsername, elasticPassword)
				.build();
	}
	
}
