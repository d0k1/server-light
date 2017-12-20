package com.focusit.serverlight.statistics;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class Statistic implements StatisticMBean {
	
	private final MeasurementsHolder holder;
	private final String name;
	private volatile ObjectInstance mbeanInstance;

	public Statistic(String name, long itemDuration, TimeUnit itemDurationUnit, long totalDuration, TimeUnit totalDurationUnit) {
		this.name = name;
		holder = new MeasurementsHolder(itemDurationUnit.toMillis(itemDuration), totalDurationUnit.toMillis(totalDuration));
	}

	public Statistic(String name, long startTime, long itemDuration, TimeUnit itemDurationUnit, long totalDuration, TimeUnit totalDurationUnit) {
		this.name = name;
		holder = new MeasurementsHolder(startTime, itemDurationUnit.toMillis(itemDuration), totalDurationUnit.toMillis(totalDuration));
	}

	public void addData(double data) {
		long time = new Date().getTime();
		holder.addData(time, data);
	}

	@Override
	public List<List<Object>> getStatistics() {
		List<List<Object>> result = new ArrayList<>();
		Map<Long, IntervalStatistics> stat = holder.getAllIntervalStatistics();
		stat.forEach((k,v)->{
			if(v.count>0){
				List<Object> row = new ArrayList<>();

				row.add(new Date(k));

				row.add(v.count);
				row.add(v.min);
				row.add(v.mean);
				row.add(v.stddev);
				row.add(v.p50);
				row.add(v.p95);
				row.add(v.p99);
				row.add(v.p999);
				row.add(v.max);
				row.add(v.sum);
				result.add(row);
			}
		});
		return result;
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
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		mbeanInstance = server.registerMBean(this, mbeanName);
	}

	public void unregisterBean() throws MBeanRegistrationException, InstanceNotFoundException {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		server.unregisterMBean(mbeanInstance.getObjectName());
	}

	public static void main(String[] args) throws MalformedObjectNameException, InstanceAlreadyExistsException, NotCompliantMBeanException, MBeanRegistrationException, IOException, InterruptedException, InstanceNotFoundException {
		Statistic stat = new Statistic("5sec", new Date().getTime(), 5, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);
		stat.registerMBean("com.focusit.serverlight.statistics:type=Statistic5m");
		stat.addData(3.14);
		Thread.sleep(6000);
		stat.addData(3.14);
		System.in.read();
		stat.unregisterBean();
		System.in.read();
	}
}
