package com.focusit.serverlight.statistics;

import java.util.List;

public interface StatisticMBean {
    String getName();
    List<List<Object>> getStatistics();
}
