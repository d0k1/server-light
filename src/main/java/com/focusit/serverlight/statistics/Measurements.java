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

/**
 * Container to hold measurements.
 * Uses fixed size queue to hold historical data
 */
public class Measurements {

    private AtomicReference<StatisticCalculator> data = new AtomicReference<>();
    private ConcurrentLinkedQueue<StatisticCalculator> history = new ConcurrentLinkedQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final long durationMs;
    // field to get linked queue size in the faster possible way
    private final AtomicLong count = new AtomicLong(0L);
    // limit for internal linked queue
    private final long capacity;

    public Measurements(long duration, TimeUnit durationUnits, long totalTime, TimeUnit totalTimeUnits) {
        this.durationMs = durationUnits.toMillis(duration);
        long maxTime = totalTimeUnits.toMillis(totalTime);

        long intervals = maxTime / durationMs;
        if(maxTime % durationMs!=0){
            intervals+=1;
        }

        this.capacity = intervals;
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

    /**
     * Method adds a new time interval into internal queue.
     * Polls an the oldest item of the queue if needed to limit queue size
     * Method in not thread safe so it if called within a critical section(via reentrant lock)
     * @param timestamp
     * @param duration
     * @return
     */
    private StatisticCalculator newInterval(long timestamp, long duration){
        StatisticCalculator statistics = data.get();
        if(statistics!=null) {
            statistics.freeze();
        }
        StatisticCalculator newStatistics = new StatisticCalculator(timestamp);
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
        StatisticCalculator currentInterval = data.get();
        long timestamp = new Date().getTime();

        // Double check locking
        if(currentInterval==null || timestamp>currentInterval.getTimestamp()+durationMs){

            lock.lock();

            currentInterval = data.get();
            timestamp = new Date().getTime();

            if(currentInterval==null || timestamp>currentInterval.getTimestamp()+durationMs) {

                try {
                    if (currentInterval == null) {
                        timestamp = roundTime(timestamp, durationMs);
                    } else {
                        timestamp = currentInterval.getTimestamp();
                    }
                    currentInterval = newInterval(timestamp + durationMs, durationMs);

                } finally {
                    lock.unlock();
                }
            }
        }

        currentInterval.addData(value);
    }

    public Collection<StatisticCalculator> getData(){
        List<StatisticCalculator> result = new ArrayList<>();
        history.forEach(item->{item.calculate();result.add(item);});
        return result;
    }
}
