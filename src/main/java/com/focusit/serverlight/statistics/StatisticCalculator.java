package com.focusit.serverlight.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Internal calculator
 */
class StatisticCalculator {
    private final long timestamp;
    private ConcurrentLinkedQueue<Double> storage = new ConcurrentLinkedQueue<>();
    private volatile CalculatedStatistics statistics = new CalculatedStatistics();
    private final AtomicBoolean freezed = new AtomicBoolean(false);
    private final ReentrantLock calcLock = new ReentrantLock();

    public StatisticCalculator(long timestamp) {
        this.timestamp = timestamp;
    }

    public void freeze(){
        calcLock.lock();
        try {
            if(!freezed.get()) {
                calculate();
                freezed.set(true);
                storage.clear();
            }
        } finally {
            calcLock.unlock();
        }
    }

    public boolean addData(double item) {
        if(freezed.get()){
            return false;
        }

        storage.add(item);
        return true;
    }

    public boolean calculate(){
        if (freezed.get()) {
            return false;
        }

        calcLock.lock();
        try {
            CalculatedStatistics intervalStatistics = new CalculatedStatistics();

            if (!storage.isEmpty()) {
                DescriptiveStatistics data = new DescriptiveStatistics();
                storage.forEach(item -> data.addValue(item));
                intervalStatistics.count = data.getN();
                intervalStatistics.max = data.getMax();
                intervalStatistics.mean = data.getMean();
                intervalStatistics.min = data.getMin();
                intervalStatistics.stddev = data.getStandardDeviation();
                intervalStatistics.p50 = data.getPercentile(50);
                intervalStatistics.p95 = data.getPercentile(95);
                intervalStatistics.p99 = data.getPercentile(99);
                intervalStatistics.p999 = data.getPercentile(99.9);
                intervalStatistics.sum = data.getSum();
            }

            statistics = intervalStatistics;

            return true;
        } finally {
            calcLock.unlock();
        }
    }

    public CalculatedStatistics getStatistics(){
        return statistics;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
