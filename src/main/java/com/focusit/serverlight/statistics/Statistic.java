package com.focusit.serverlight.statistics;

import javax.management.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class Statistic implements StatisticMBean {
	
	private final MeasurementsHolder holder;
	private final String name;

	public Statistic(String name, long itemDuration, TimeUnit itemDurationUnit, long totalDuration, TimeUnit totalDurationUnit) {
		this.name = name;
		holder = new MeasurementsHolder(itemDurationUnit.toMillis(itemDuration), totalDurationUnit.toMillis(totalDuration));
	}
	
	public void addData(double data) {
		long time = new Date().getTime();
		holder.addData(time, data);
	}

	public Map<Long, IntervalStatistics> getRawStatistics(int test){
		return holder.getAllIntervalStatistics();
	}

	//@Override
	public List<List<Object>> getStatistics() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	public Map<Long, ConcurrentLinkedQueue<Double>> getRawData(){
		return holder.getRawDataAllIntervals();
	}

	public void registerMBean(String name) throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
		ObjectName mbeanName = new ObjectName(name);
		new StatisticsBean(this).registerBean(mbeanName);
	}

	public static void main(String[] args) throws MalformedObjectNameException, InstanceAlreadyExistsException, NotCompliantMBeanException, MBeanRegistrationException, IOException {
		Statistic stat = new Statistic("test5m",5, TimeUnit.MINUTES, 48, TimeUnit.HOURS);
		stat.registerMBean("com.focusit.serverlight.statistics:type=Statistic5m");
		System.in.read();
	}
}
