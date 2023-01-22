package com.ian.vca.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "transactionProcessingError")
public class TransactionProcessingError {

	@Field(type = FieldType.Auto)
	private BigInteger customerId;
	
	@Field(type = FieldType.Auto)
	private Integer destinationId;
	
	@Field(type = FieldType.Auto)
	private BigDecimal amount;
	
	@Field(type = FieldType.Date, format = DateFormat.basic_date_time)
	 private Instant transactionTime;
}
