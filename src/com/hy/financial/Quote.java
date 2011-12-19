/*
 * ZINNOVA TECHNOLOGIES CONFIDENTIAL
 * __________________________________
 * Copyright (c) 2011 Zinnova Technologies Inc. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Zinnova Technologies Inc and its suppliers. 
 * The intellectual and technical concepts contained
 * herein are proprietary to Zinnova Technologies Inc
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * The information contained herein may not be transferred, published,
 * disclosed, reproduced, or used by any party without prior and expressive
 * written permission of Zinnova Tehcnologies Inc.
 */
package com.hy.financial;


/**
 * @author hyang
 * Created on: Aug 23, 2011
 */
public class Quote implements Comparable<Quote> {

	private String symbol;
	private String lateDay;
	private float latePrice;
	private String earlyDay;
	private float earlyPrice;
	private float change;
	
	public Quote(String symbol, 
		String lateDay,
		float latePrice,
		String earlyDay,
		float earlyPrice,
		float change) {
		this.symbol = symbol;
		this.lateDay = lateDay;
		this.latePrice = latePrice;
		this.earlyDay = earlyDay;
		this.earlyPrice = earlyPrice;
		this.change = change;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Quote o) {
		return change < o.change ? -1 : (change == o.change? 0: 1);
	}

	public String toString() {
		return symbol + " " + lateDay + " " + latePrice + " " + earlyDay + " " + earlyPrice + " " + change;
	}
}
