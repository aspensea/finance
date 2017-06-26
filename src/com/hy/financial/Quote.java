/*
 * Copyright (c) 2011 Sageville Inc. All Rights Reserved.
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
	private float weekAgoPrice;
	private float change;
	private boolean isBT;
	private float weekAgoDiff;
	
	public Quote(String symbol, 
		String lateDay,
		float latePrice,
		String earlyDay,
		float earlyPrice,
		float weekAgoPrice,
		float change,
		boolean isBT,
				 float weekAgoDiff) {
		this.setSymbol(symbol);
		this.lateDay = lateDay;
		this.latePrice = latePrice;
		this.earlyDay = earlyDay;
		this.earlyPrice = earlyPrice;
		this.weekAgoPrice = weekAgoPrice;
		this.change = change;
		this.isBT = isBT;
		this.weekAgoDiff = weekAgoDiff;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Quote o) {
		return change < o.change ? -1 : (change == o.change? 0: 1);
	}

	@Override
	public String toString() {
		return getSymbol() + "\t" + lateDay + "\t" + latePrice + "\t" + earlyDay + "\t" + earlyPrice + "\t" + change
				+ "\t" + weekAgoPrice + "\t" + weekAgoDiff
				+ (isBT ?"\t*BT*" : "\t");
	}

    /**
     * @return the symbol
     */
    String getSymbol()
    {
        return symbol;
    }

    /**
     * @param symbol the symbol to set
     */
    void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }
}
