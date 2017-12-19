package com.focusit.serverlight.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Holds measurements grouped by time interval to calculate basic descriptive statistics
 */
public class MeasurementsHolder implements Serializable {
	
	private final ConcurrentHashMap<Long, StatisticItem> items = new ConcurrentHashMap<>();
	private final long itemDurationMs;
	private final long maxTimeMs;
	private final long startTimeMs;
	
	public MeasurementsHolder(long itemDurationMs, long maxTimeMs) {
		this(itemDurationMs, maxTimeMs, Calendar.getInstance().getTimeZone().getID());
	}

	public MeasurementsHolder(long itemDurationMs, long maxTimeMs, String timezone) {
		this.itemDurationMs = itemDurationMs;
		this.maxTimeMs = maxTimeMs;

		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone(timezone));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		startTimeMs = cal.getTime().getTime();
		init();
	}

	public long getStartTimeMs(){
		return startTimeMs;
	}

	public int getCapacity(){
		return (int) (this.maxTimeMs / this.itemDurationMs);
	}

	public IntervalStatistics getIntervalStatistics(long timeMs) {
		StatisticItem v = items.get(getTimeKey(timeMs));
		v.calculate();

		return v.getStatistics();
	}

	public Map<Long, IntervalStatistics> getAllIntervalStatistics(){
		TreeMap<Long, IntervalStatistics> result = new TreeMap<>();
		items.forEach((k,v)->{
			v.calculate();
			IntervalStatistics item = v.getStatistics();
			result.put(k, item);
		});
		return result;
	}

	public ConcurrentLinkedQueue<Double> getRawDataByInterval(long timeMs){
		StatisticItem v = items.get(getTimeKey(timeMs));
		return v.storage;
	}

	public Map<Long, ConcurrentLinkedQueue<Double>> getRawDataAllIntervals(){
		TreeMap<Long, ConcurrentLinkedQueue<Double>> result = new TreeMap<>();
		items.forEach((k,v)-> result.put(k, v.storage));
		return result;
	}

	private void init() {

		items.put(startTimeMs, new StatisticItem());

		long ms = startTimeMs;
		int capacity = getCapacity();
		for(int i = 1; i< capacity; i++) {
			ms+= itemDurationMs;
			items.put(ms, new StatisticItem());
		}
	}

	public long getTimeKey(long millis){
		if(millis<startTimeMs){
			throw new IllegalArgumentException();
		}

		long multiplier = millis / itemDurationMs;
		long key = itemDurationMs*multiplier;

		return key;
	}

	public void addData(long millis, double value) {
		long key = getTimeKey(millis);
		items.get(key).addData(value);
	}

	class StatisticItem {
		private ConcurrentLinkedQueue<Double> storage = new ConcurrentLinkedQueue<>();
		private volatile IntervalStatistics statistics = new IntervalStatistics();
		private DescriptiveStatistics data = new DescriptiveStatistics();

		public void addData(double item) {
			storage.add(item);
		}

		public synchronized void calculate(){
			storage.forEach(item-> data.addValue(item));
			IntervalStatistics item = new IntervalStatistics();
			item.count = getCount();
			item.max = getMax();
			item.mean = getMean();
			item.min = getMin();
			item.stddev = getStddev();
			item.p50 = getPercentile(50);
			item.p95 = getPercentile(95);
			item.p99 = getPercentile(99);
			item.p999 = getPercentile(99.9);
			item.sum = getSum();

			statistics = item;
		}

		private long getCount(){
			return data.getN();
		}

		private double getMin(){
			return data.getMin();
		}

		private double getMax(){
			return data.getMax();
		}

		private double getMean(){
			return data.getMean();
		}

		private double getStddev(){
			return data.getStandardDeviation();
		}

		public IntervalStatistics getStatistics(){
			return statistics;
		}

		public double getPercentile(double p) {
			return data.getPercentile(p);
		}

		public double getSum() {
			return data.getSum();
		}
	}
}
