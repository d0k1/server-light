package com.focusit.serverlight.statistics;

import javax.management.openmbean.CompositeData;
import java.util.List;

public interface StatisticMBean {
    String getName();
    void reset();
    List<CompositeData> getData();
}
