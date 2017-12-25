package com.focusit.serverlight.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class StatisticItem {
    private final long timestamp;
    private final long duration;
    private ConcurrentLinkedQueue<Double> storage = new ConcurrentLinkedQueue<>();
    private volatile IntervalStatistics statistics = new IntervalStatistics();
    private final AtomicLong lastVersion = new AtomicLong();
    private final AtomicBoolean freezed = new AtomicBoolean(false);
    private final ReentrantLock calcLock = new ReentrantLock();

    public StatisticItem(long timestamp, long duration) {
        this.timestamp = timestamp;
        this.duration = duration;
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

        lastVersion.incrementAndGet();
        storage.add(item);
        return true;
    }

    public boolean calculate(){
        IntervalStatistics oldStat = statistics;

        if (oldStat.lastVersion != null && oldStat.lastVersion.get() == lastVersion.get()) {
            return true;
        }

        if (freezed.get()) {
            return false;
        }

        calcLock.lock();
        try {
            IntervalStatistics intervalStatistics = new IntervalStatistics();

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

            intervalStatistics.lastVersion.set(lastVersion.get());
            statistics = intervalStatistics;

            return true;
        } finally {
            calcLock.unlock();
        }
    }

    public IntervalStatistics getStatistics(){
        return statistics;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }
}
