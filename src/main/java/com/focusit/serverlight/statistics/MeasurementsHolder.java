//package com.focusit.serverlight.statistics;
//
//import java.io.Serializable;
//import java.util.Calendar;
//import java.util.Map;
//import java.util.TimeZone;
//import java.util.TreeMap;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
///**
// * Holds measurements grouped by time interval to calculate basic descriptive statistics
// */
//public class MeasurementsHolder implements Serializable {
//	private final ConcurrentHashMap<Long, StatisticItem> items = new ConcurrentHashMap<>();
//	private final long itemDurationMs;
//	private final long maxTimeMs;
//	private final long startTimeMs;
//
//	public MeasurementsHolder(long itemDurationMs, long maxTimeMs) {
//		this(null, itemDurationMs, maxTimeMs, Calendar.getInstance().getTimeZone().getID());
//	}
//
//	public MeasurementsHolder(long startTimeMs, long itemDurationMs, long maxTimeMs) {
//		this(startTimeMs, itemDurationMs, maxTimeMs, Calendar.getInstance().getTimeZone().getID());
//	}
//
//	public MeasurementsHolder(Long startMs, long itemDurationMs, long maxTimeMs, String timezone) {
//		this.itemDurationMs = itemDurationMs;
//		this.maxTimeMs = maxTimeMs;
//
//		if(startMs==null) {
//			Calendar cal = Calendar.getInstance();
//			cal.setTimeZone(TimeZone.getTimeZone(timezone));
//			cal.set(Calendar.HOUR_OF_DAY, 0);
//			cal.set(Calendar.MINUTE, 0);
//			cal.set(Calendar.SECOND, 0);
//			cal.set(Calendar.MILLISECOND, 0);
//
//			startTimeMs = cal.getTime().getTime();
//		} else {
//			startTimeMs = (startMs / itemDurationMs) * itemDurationMs;
//		}
//
//		init();
//	}
//
//	public long getStartTimeMs(){
//		return startTimeMs;
//	}
//
//	public int getCapacity(){
//		return (int) (this.maxTimeMs / this.itemDurationMs);
//	}
//
//	public IntervalStatistics getIntervalStatistics(long timeMs) {
//		StatisticItem v = items.get(getTimeKey(timeMs));
//		if(v==null){
//			return null;
//		}
//		v.calculate();
//
//		return v.getStatistics();
//	}
//
//	public Map<Long, IntervalStatistics> getAllIntervalStatistics(){
//		TreeMap<Long, IntervalStatistics> result = new TreeMap<>();
//		items.forEach((k,v)->{
//			v.calculate();
//			IntervalStatistics item = v.getStatistics();
//			result.put(k, item);
//		});
//		return result;
//	}
//
//	public ConcurrentLinkedQueue<Double> getRawDataByInterval(long timeMs){
//		StatisticItem v = items.get(getTimeKey(timeMs));
//		return v.storage;
//	}
//
//	public Map<Long, ConcurrentLinkedQueue<Double>> getRawDataAllIntervals(){
//		TreeMap<Long, ConcurrentLinkedQueue<Double>> result = new TreeMap<>();
//		items.forEach((k,v)-> result.put(k, v.storage));
//		return result;
//	}
//
//	private void init() {
//
//		items.put(startTimeMs, new StatisticItem(timestamp, duration));
//
//		long ms = startTimeMs;
//		int capacity = getCapacity();
//		for(int i = 1; i< capacity; i++) {
//			ms+= itemDurationMs;
//			items.put(ms, new StatisticItem(timestamp, duration));
//		}
//	}
//
//	public long getTimeKey(long millis){
//		if(millis<startTimeMs){
//			throw new IllegalArgumentException();
//		}
//
//		long multiplier = millis / itemDurationMs;
//		long key = itemDurationMs*multiplier;
//
//		return key;
//	}
//
//	public long addData(long millis, double value) {
//		long key = getTimeKey(millis);
//		items.get(key).addData(value);
//		return key;
//	}
//
//	public void freeze(long millis){
//		long key = getTimeKey(millis);
//		items.get(key).freeze();
//	}
//
//}
