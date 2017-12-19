package com.focusit.serverlight.statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface StatisticMBean {
    String getName();
    Map<Long, ConcurrentLinkedQueue<Double>> getRawData();
    Map<Long, IntervalStatistics> getRawStatistics(int test);
//    List<List<Object>> getStatistics();
}
