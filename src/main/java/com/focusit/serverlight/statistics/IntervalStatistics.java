package com.focusit.serverlight.statistics;

import java.io.Serializable;

public class IntervalStatistics implements Serializable {
    public double min;
    public double max;
    public double mean;
    public double stddev;
    public double p50;
    public double p95;
    public double p99;
    public double p999;
    public double sum;
    public long count;
}
