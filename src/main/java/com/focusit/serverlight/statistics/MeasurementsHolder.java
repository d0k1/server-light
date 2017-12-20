package com.focusit.serverlight.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds measurements grouped by time interval to calculate basic descriptive statistics
 */
public class MeasurementsHolder implements Serializable {
	
	private final ConcurrentHashMap<Long, StatisticItem> items = new ConcurrentHashMap<>();
	private final long itemDurationMs;
	private final long maxTimeMs;
	private final long startTimeMs;
	
	public MeasurementsHolder(long itemDurationMs, long maxTimeMs) {
		this(null, itemDurationMs, maxTimeMs, Calendar.getInstance().getTimeZone().getID());
	}

	public MeasurementsHolder(long startTimeMs, long itemDurationMs, long maxTimeMs) {
		this(startTimeMs, itemDurationMs, maxTimeMs, Calendar.getInstance().getTimeZone().getID());
	}

	public MeasurementsHolder(Long startMs, long itemDurationMs, long maxTimeMs, String timezone) {
		this.itemDurationMs = itemDurationMs;
		this.maxTimeMs = maxTimeMs;

		if(startMs==null) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(TimeZone.getTimeZone(timezone));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			startTimeMs = cal.getTime().getTime();
		} else {
			startTimeMs = (startMs / itemDurationMs) * itemDurationMs;
		}

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
		if(v==null){
			return null;
		}
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

	public long addData(long millis, double value) {
		long key = getTimeKey(millis);
		items.get(key).addData(value);
		return key;
	}

	public void freeze(long millis){
		long key = getTimeKey(millis);
		items.get(key).freeze();
	}

	class StatisticItem {
		private ConcurrentLinkedQueue<Double> storage = new ConcurrentLinkedQueue<>();
		private volatile IntervalStatistics statistics = new IntervalStatistics();
		private final AtomicLong lastVersion = new AtomicLong();
		private final AtomicBoolean freezed = new AtomicBoolean(false);
		private final ReentrantLock calcLock = new ReentrantLock();

		public void freeze(){
			calcLock.lock();
			try {
				if(!freezed.get()) {
					calculate();
					freezed.set(true);
					storage.clear();
				}
			} finally {
				calcLock.unlock();
			}
		}

		public boolean addData(double item) {
			if(freezed.get()){
				return false;
			}

			lastVersion.incrementAndGet();
			storage.add(item);
			return true;
		}

		public boolean calculate(){
			IntervalStatistics oldStat = statistics;

			if (oldStat.lastVersion != null && oldStat.lastVersion.get() == lastVersion.get()) {
				return true;
			}

			if (freezed.get()) {
				return false;
			}

			calcLock.lock();
			try {
				IntervalStatistics intervalStatistics = new IntervalStatistics();

				if (!storage.isEmpty()) {
					DescriptiveStatistics data = new DescriptiveStatistics();
					storage.forEach(item -> data.addValue(item));
					intervalStatistics.count = data.getN();
					intervalStatistics.max = data.getMax();
					intervalStatistics.mean = data.getMean();
					intervalStatistics.min = data.getMin();
					intervalStatistics.stddev = data.getStandardDeviation();
					intervalStatistics.p50 = data.getPercentile(50);
					intervalStatistics.p95 = data.getPercentile(95);
					intervalStatistics.p99 = data.getPercentile(99);
					intervalStatistics.p999 = data.getPercentile(99.9);
					intervalStatistics.sum = data.getSum();
				}

				intervalStatistics.lastVersion.set(lastVersion.get());
				statistics = intervalStatistics;

				return true;
			} finally {
				calcLock.unlock();
			}
		}

		public IntervalStatistics getStatistics(){
			return statistics;
		}
	}
}
