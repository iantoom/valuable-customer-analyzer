package com.ian.vca.models.elastic;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName = "transaction_processing_error")
public class TransactionProcessingError {

	@Field(type = FieldType.Auto)
	private Long customerId;
	
	@Field(type = FieldType.Auto)
	private String customerName;
	
	@Field(type = FieldType.Auto)
	private Integer destinationId;
	
	@Field(type = FieldType.Auto)
	private Double amount;
	
	@Field(type = FieldType.Date, format = DateFormat.basic_date_time_no_millis)
	private Instant transactionTime;
	
	@Field(type = FieldType.Keyword)
	private String errorMessage;
}
