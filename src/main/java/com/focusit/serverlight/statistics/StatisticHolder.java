package com.focusit.serverlight.statistics;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class StatisticHolder {
	
	private final ConcurrentHashMap<Long, StatisticItem> items = new ConcurrentHashMap<>();
	private final long itemDuration;
	private final long startMillis;
	private final int maxItems;
	
	public StatisticHolder(long itemDuration, long startMillis, int maxItems) {
		this.itemDuration = itemDuration;
		this.startMillis = startMillis;
		this.maxItems = maxItems;
		
		init(this.startMillis);
	}
	
	public Map<Long, Double> getMin(){
		return null;
	}

	public Map<Long, Double> getMax(){
		return null;
	}

	public Map<Long, Double> getMean(){
		return null;
	}

	public Map<Long, Double> getStddev(){
		return null;
	}

	private void init(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		long midnight = cal.getTime().getTime();
		items.put(midnight, new StatisticItem());
		
		for(int i=1;i<maxItems;i++) {
			midnight+=itemDuration;
			items.put(midnight, new StatisticItem());
		}
	}
	
	public void addData(long millis, double value) {
		// start = 0
		// duration 300
		// 634
		long multiplier = millis / itemDuration;
		long key = startMillis+itemDuration*multiplier;
		items.get(key).addData(value);
	}

	class StatisticItem {
		DoubleArrayList data = new DoubleArrayList();
		
		public void addData(double item) {
			data.add(item);
		}
		
		public double getQuantile(double phi) {
			return Descriptive.quantile(data, phi);
		}
		
		public double getSumm() {
			return Descriptive.sum(data);
		}
	}
}
