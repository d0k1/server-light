package com.focusit.serverlight.statistics;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Class to hold data and provide statistics.
 *
 * Usage scenario:
 * <ul>
 *     <li>create instance</li>
 *     <li>call addData(). addData() will return a timestamp of beginning of the time interval where you've put measurement</li>
 *     <li>when addData() return new value it means you've put a measurement into a new time interval so you can calc previous time interval statistics and sweep it's raw data by calling freeze()</li>
 *     <li>call getData()/getRaw() to retrieve statistics</li>
 * </ul>
 */
public class Statistic implements StatisticMBean {
	
	private final MeasurementsHolder holder;
	private final String name;
	private volatile ObjectInstance mbeanInstance;
	private final CompositeType compositeType = getCompositeType();
	private String path;

	/**
	 * Constructor
	 *
	 * Created instance will create a list of time intervals starting from current day, 00:00:00.000 till totalDuration.
	 * Each time interval will save data for itemDuration
	 *
	 * @param path path of mbean
	 * @param name name of mbean
	 * @param itemDuration interval duration
	 * @param itemDurationUnit interval duration unit, e.g. hours, seconds, etc
	 * @param totalDuration time limit to store data
	 * @param totalDurationUnit time limit unit
	 */
	public Statistic(String path, String name, long itemDuration, TimeUnit itemDurationUnit, long totalDuration, TimeUnit totalDurationUnit) {
		this.name = name;
		this.path = path;
		holder = new MeasurementsHolder(itemDurationUnit.toMillis(itemDuration), totalDurationUnit.toMillis(totalDuration));
	}

	/**
	 * Constructor
	 *
	 * Created instance will create a list of time intervals starting from startTime till totalDuration.
	 * Each time interval will save data for itemDuration
	 *
	 * @param path path of mbean
	 * @param name name of mbean
	 * @param startTime initial timestamp
	 * @param itemDuration interval duration
	 * @param itemDurationUnit interval duration unit, e.g. hours, seconds, etc
	 * @param totalDuration time limit to store data
	 * @param totalDurationUnit time limit unit
	 */
	public Statistic(String path, String name, long startTime, long itemDuration, TimeUnit itemDurationUnit, long totalDuration, TimeUnit totalDurationUnit) {
		this.name = name;
		this.path = path;
		holder = new MeasurementsHolder(startTime, itemDurationUnit.toMillis(itemDuration), totalDurationUnit.toMillis(totalDuration));
	}

	/**
	 * Add measurement to the current time interval
	 * @param data measurement
	 * @return timestamp of the beginning of current time interval
	 */
	public long addData(double data) {
		long time = new Date().getTime();
		return holder.addData(time, data);
	}

	public void freeze(){
		freeze(new Date().getTime());
	}

	public void freeze(long millis){
		holder.freeze(millis);
	}

	private CompositeType getCompositeType() {
		try {
			return new CompositeType("IntervalStatistics", "Statistics",
					new String[]{"DateTime", "min", "max", "mean", "stddev", "p50", "p95", "p99", "p999", "sum", "count", "lastVersion"},
					new String[]{"Date Time", "min", "max", "mean", "std dev", "50%", "95%", "99%", "99.9%", "sum", "count", "last version"},
					new OpenType[]{SimpleType.DATE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.LONG, SimpleType.LONG});
		} catch (OpenDataException e) {
			e.printStackTrace();
		}
		return null;
	}

	private CompositeData getCompositeDataFromIntervalStatistics(long time, IntervalStatistics stat) throws OpenDataException {
		Map<String, Object> data = stat.asMap();
		data.put("DateTime", new Date(time));
		return new CompositeDataSupport(compositeType, data);
	}

	@Override
	public List<CompositeData> getData() {
		List<CompositeData> result = new ArrayList<>();
		Map<Long, IntervalStatistics> stat = holder.getAllIntervalStatistics();
		stat.forEach((k,v)->{
			if(v.count>0){
				try {
					result.add(getCompositeDataFromIntervalStatistics(k, v));
				} catch (OpenDataException e) {
					e.printStackTrace(System.err);
				}
			}
		});
		return result;
	}

	public Map<Long, IntervalStatistics> getRaw(){
		Map<Long, IntervalStatistics> result = new HashMap<>();
		Map<Long, IntervalStatistics> stat = holder.getAllIntervalStatistics();
		stat.forEach((k,v)->{
			if(v.count>0){
				result.put(k, v);
			}
		});
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	public Map<Long, ConcurrentLinkedQueue<Double>> getMeasurements(){
		return holder.getRawDataAllIntervals();
	}

	public void registerMBean() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
		ObjectName mbeanName = new ObjectName(path+":type="+name);
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		mbeanInstance = server.registerMBean(this, mbeanName);
	}

	public void unregisterBean() throws MBeanRegistrationException, InstanceNotFoundException {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		server.unregisterMBean(mbeanInstance.getObjectName());
	}
}
