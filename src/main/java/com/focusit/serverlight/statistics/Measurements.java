package com.focusit.serverlight.statistics;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Measurements {

    private AtomicReference<IntervalStatistics> data = new AtomicReference<>();
    private ConcurrentLinkedQueue<IntervalStatistics> history = new ConcurrentLinkedQueue<>();
    private final ReentrantLock lock = new ReentrantLock();

    private IntervalStatistics newInterval(long timestamp, long duration){
        IntervalStatistics statistics = data.get();
        history.add(statistics);
        IntervalStatistics newStatistics = new IntervalStatistics();
        data.set(newStatistics);
        return newStatistics;
    }

    public void addData(double value){
        IntervalStatistics statistics = data.get();

        long timestamp = new Date().getTime();
        if(statistics==null || timestamp>statistics.timestamp+statistics.duration){
            lock.lock();

            try{
                statistics = newInterval(statistics.timestamp+statistics.duration, statistics.duration);
            } finally {
                lock.unlock();
            }
        }
    }
}
