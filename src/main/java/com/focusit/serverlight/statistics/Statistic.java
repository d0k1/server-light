package com.focusit.serverlight.statistics;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Statistic {
	
	private final Map<String, StatisticHolder> holders = new HashMap<>();
	
	public Statistic() {
		long startDate = new Date().getTime();
		holders.put("05m", new StatisticHolder(30*1000, startDate, 48*60*2));
		holders.put("1m", new StatisticHolder(60*1000, startDate, 48*60));
		holders.put("5m", new StatisticHolder(5*60*1000, startDate, 48*60/5));
		holders.put("10m", new StatisticHolder(10*60*1000, startDate, 48*60/10));
		holders.put("15m", new StatisticHolder(15*60*1000, startDate, 48*60/15));
		holders.put("30m", new StatisticHolder(30*60*1000, startDate, 48*60/30));
		holders.put("1h", new StatisticHolder(60*60*1000, startDate, 48));
		holders.put("2h", new StatisticHolder(2*60*60*1000, startDate, 48/2));
		holders.put("4h", new StatisticHolder(4*60*60*1000, startDate, 48/4));
		holders.put("8h", new StatisticHolder(8*60*60*1000, startDate, 48/8));
		holders.put("12h", new StatisticHolder(12*60*60*1000, startDate, 48/12));
	}
	
	public void addData(double data) {
		long time = new Date().getTime();
		holders.values().forEach(holder->{
			holder.addData(time, data);
		});
	}
	
}
