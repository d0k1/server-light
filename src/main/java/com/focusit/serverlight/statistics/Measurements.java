package com.focusit.serverlight.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Measurements {

    private AtomicReference<StatisticItem> data = new AtomicReference<>();
    private ConcurrentLinkedQueue<StatisticItem> history = new ConcurrentLinkedQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final long durationMs;
    private final AtomicLong count = new AtomicLong(0L);
    private final long capacity;

    public Measurements(long durationMs, long capacity){
        this.durationMs = durationMs;
        this.capacity = capacity;
    }

    public Measurements(long durationMs, long totalTime, TimeUnit totalTimeUnits) {
        this.durationMs = durationMs;
        long maxTime = totalTimeUnits.toMillis(totalTime);
        this.capacity = maxTime / durationMs;
    }

    public long getIntervalTimeDurationMs(){
        return durationMs;
    }

    public long getTotalTimeMs(){
        return capacity*durationMs;
    }

    public long getCapacity(){
        return capacity;
    }

    private StatisticItem newInterval(long timestamp, long duration){
        StatisticItem statistics = data.get();
        if(statistics!=null) {
            statistics.freeze();
        }
        StatisticItem newStatistics = new StatisticItem(timestamp, duration);
        data.set(newStatistics);
        long currentCount = count.get();
        if(currentCount+1>capacity) {
            history.poll();
        } else {
            count.incrementAndGet();
        }
        history.add(newStatistics);

        return newStatistics;
    }

    public long roundTime(long timestamp, long duration){
        long multiplier = timestamp / duration;
        return duration*multiplier;
    }

    public void addData(double value){
        StatisticItem statistics = data.get();
        long timestamp = new Date().getTime();

        if(statistics==null || timestamp>statistics.getTimestamp()+durationMs){
            lock.lock();

            try{
                if(statistics==null){
                    timestamp = roundTime(timestamp, durationMs);
                } else {
                    timestamp = statistics.getTimestamp();
                }
                statistics = newInterval(timestamp+durationMs, durationMs);
            } finally {
                lock.unlock();
            }
        }

        statistics.addData(value);
    }

    public Collection<StatisticItem> getData(){
        List<StatisticItem> result = new ArrayList<>();
        history.forEach(item->{item.calculate();result.add(item);});
        return result;
    }
}
