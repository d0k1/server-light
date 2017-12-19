package com.focusit.serverlight.specs.statistic

import com.focusit.serverlight.statistics.IntervalStatistics
import com.focusit.serverlight.statistics.MeasurementsHolder
import spock.lang.Specification

import java.util.concurrent.ConcurrentLinkedQueue

class MeasurementsHolderSpec extends Specification {

    def "can create holder with a hour interval to hold data for 48 hours"() {
        given:
        MeasurementsHolder holder = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000);
        when:
        int capacity = holder.capacity
        then:
        capacity == 48
    }

    def "holder checks given interval"() {
        given:
        MeasurementsHolder holder = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000);

        when:
        holder.addData(0, 1.0)
        then:
        true
        thrown(IllegalArgumentException)
    }

    def "holder can hold measurements and return statistics for a particular interval"() {
        given:
        MeasurementsHolder holder = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000)
        long date = new Date().getTime()

        when:
        holder.addData(date, 1.0)
        holder.addData(date, 2.0)

        IntervalStatistics stat = holder.getIntervalStatistics(date + 2)
        then:
        stat.max == 2
        stat.min == 1
        stat.count == 2
        stat.sum == 3
    }

    def "holder can return statistics for the same particular interval multiple times"() {
        given:
        MeasurementsHolder holder = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000)
        long date = new Date().getTime()

        when:
        holder.addData(date, 1.0)
        holder.addData(date, 2.0)

        IntervalStatistics stat = holder.getIntervalStatistics(date + 2)
        IntervalStatistics stat2 = holder.getIntervalStatistics(date + 2)
        then:
        stat.max == stat2.max
        stat.min == stat2.min
        stat.count == stat2.count
        stat.sum == stat2.sum
    }

    def "holder can hold measurements and return statistics for all intervals"() {
        given:
        MeasurementsHolder holder = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000)
        long date = new Date().getTime()

        when:
        holder.addData(date, 1.0)
        holder.addData(date, 2.0)

        Map<Long, IntervalStatistics> stats = holder.getAllIntervalStatistics()
        long key = holder.getTimeKey(date);
        IntervalStatistics stat = stats.get(key)
        then:
        stat.max == 2
        stat.min == 1
        stat.count == 2
        stat.sum == 3
    }

    def "holder can use timezone to initialize time intervals"(){
        when:
        MeasurementsHolder holder_utc5 = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000, "America/New_York")
        MeasurementsHolder holder_utc = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000, "Asia/Singapore")
        then:
        holder_utc.getStartTimeMs()!=holder_utc5.getStartTimeMs()
    }

    def "holder can hold measurements and return raw data for a particular interval"() {
        given:
        MeasurementsHolder holder = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000)
        long date = new Date().getTime()

        when:
        holder.addData(date, 1.0)
        holder.addData(date, 2.0)

        ConcurrentLinkedQueue<Double> raw = holder.getRawDataByInterval(date+2);
        Iterator<Double> iterator = raw.iterator();
        then:
        raw.size()==2
        iterator.next()==1.0
        iterator.next()==2.0
    }

    def "holder can hold measurements and return raw data for all intervals"() {
        given:
        MeasurementsHolder holder = new MeasurementsHolder(60 * 60 * 1000, 48 * 60 * 60 * 1000)
        long date = new Date().getTime()

        when:
        holder.addData(date, 1.0)
        holder.addData(date, 2.0)

        Map<Long, ConcurrentLinkedQueue<Double>> stats = holder.getRawDataAllIntervals()
        long key = holder.getTimeKey(date);
        ConcurrentLinkedQueue<Double> raw = stats.get(key)
        Iterator<Double> iterator = raw.iterator();
        then:
        raw.size()==2
        iterator.next()==1.0
        iterator.next()==2.0
    }
}