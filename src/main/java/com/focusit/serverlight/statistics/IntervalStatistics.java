package com.focusit.serverlight.statistics;

import javax.management.openmbean.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class IntervalStatistics implements Serializable {
    public long timestamp;
    public long duration;
    public double min;
    public double max;
    public double mean;
    public double stddev;
    public double p50;
    public double p95;
    public double p99;
    public double p999;
    public double sum;
    public long count=0;
    public AtomicLong lastVersion = new AtomicLong(-1);

    Map<String, Object> asMap(){
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", timestamp);
        result.put("duration", duration);
        result.put("min", min);
        result.put("max", max);
        result.put("mean", mean);
        result.put("stddev", stddev);
        result.put("p50", p50);
        result.put("p95", p95);
        result.put("p99", p99);
        result.put("p999", p999);
        result.put("sum", sum);
        result.put("count", count);
        result.put("lastVersion", lastVersion.get());
        return result;
    }
}
