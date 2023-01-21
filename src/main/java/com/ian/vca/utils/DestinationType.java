package com.ian.vca.utils;

import java.math.BigInteger;

public enum DestinationType {

	MARKET_PLACE(BigInteger.valueOf(1), "Shopper"),
	TIME_DEPOSIT(BigInteger.valueOf(2), "Saver"),
	MUTUAL_FUNDS(BigInteger.valueOf(3), "Investor"),
	STOCK_MARKET(BigInteger.valueOf(4), "Trader"),
	OTHER_ACCOUNT(BigInteger.valueOf(-1), "Transfer_hub");

	BigInteger destinationId;
	String tag;
	DestinationType(BigInteger destinationId, String tag) {
		this.destinationId = destinationId;
		this.tag = tag;
	}
	
	public static DestinationType resolve(BigInteger destinationId) {
		for (DestinationType status : values()) {
			if (status.destinationId.equals(destinationId)) {
				return status;
			}
		}
		return OTHER_ACCOUNT;
	}
}
