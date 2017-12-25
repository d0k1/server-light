package com.focusit.serverlight.statistics;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculated descriptive statistics
 */
public class CalculatedStatistics implements Serializable {
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

    Map<String, Object> asMap(){
        Map<String, Object> result = new HashMap<>();
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
        return result;
    }
}
