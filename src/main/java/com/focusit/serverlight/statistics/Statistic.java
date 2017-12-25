package com.focusit.serverlight.statistics;

import javax.management.*;
import javax.management.openmbean.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Class to hold data and calculate descriptive statistics.
 *
 * Usage scenario:
 * <ul>
 *     <li>create instance with desired parameters</li>
 *     <li>call addData() to add a measure</li>
 *     <li>call get() to retrieve gathered statistics</li>
 *     <li>call reset to erase any stored measures/calculated statistics, if needed</li>
 * </ul>
 */
public class Statistic implements StatisticMBean {

    private volatile Measurements measurements;
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
		measurements = new Measurements(itemDuration, itemDurationUnit, totalDuration, totalDurationUnit);
	}

	/**
	 * Add measurement to the current time interval
	 * @param data measurement
	 * @return timestamp of the beginning of current time interval
	 */
	public void addData(double data) {
	    Measurements localMeasurements = measurements;
        localMeasurements.addData(data);
	}

	private CompositeType getCompositeType() {
		try {
			return new CompositeType("CalculatedStatistics", "Statistics",
					new String[]{"DateTime", "min", "max", "mean", "stddev", "p50", "p95", "p99", "p999", "sum", "count"},
					new String[]{"Date Time", "min", "max", "mean", "std dev", "50%", "95%", "99%", "99.9%", "sum", "count"},
					new OpenType[]{SimpleType.DATE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.LONG});
		} catch (OpenDataException e) {
			e.printStackTrace();
		}
		return null;
	}

	private CompositeData getCompositeDataFromIntervalStatistics(long time, CalculatedStatistics stat) throws OpenDataException {
		Map<String, Object> data = stat.asMap();
		data.put("DateTime", new Date(time));
		return new CompositeDataSupport(compositeType, data);
	}

	public List<CalculatedStatistics> get(){
        Measurements localMeasurements = measurements;
	    ArrayList<CalculatedStatistics> result = new ArrayList<>();
        localMeasurements.getData().forEach(item-> result.add(item.getStatistics()));
	    return result;
    }

	@Override
	public List<CompositeData> getData() {
        Measurements localMeasurements = measurements;

        List<CompositeData> result = new ArrayList<>();
		Collection<StatisticCalculator> stat = localMeasurements.getData();
		stat.forEach((v)->{
			if(v.getStatistics().count>0){
				try {
					result.add(getCompositeDataFromIntervalStatistics(v.getTimestamp(), v.getStatistics()));
				} catch (OpenDataException e) {
					e.printStackTrace(System.err);
				}
			}
		});
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

    @Override
    public void reset() {
        Measurements localMeasurements = measurements;
        measurements = new Measurements(localMeasurements.getIntervalTimeDurationMs(), TimeUnit.MILLISECONDS, localMeasurements.getTotalTimeMs(), TimeUnit.MILLISECONDS);
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
