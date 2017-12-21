package com.focusit.serverlight.specs.statistic

import com.focusit.serverlight.statistics.IntervalStatistics
import com.focusit.serverlight.statistics.Statistic
import spock.lang.Specification

import java.util.concurrent.TimeUnit


class StatisticSpec extends Specification {

    def "statistics can store and retrieve data"(){
        Statistic stat = new Statistic("com.focusit.serverlight.statistics", "2sec", new Date().getTime(), 2, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);

        when:
        stat.addData(3.14);

        Thread.sleep(3000);

        stat.addData(6.14);

        Map<Long, IntervalStatistics> data = stat.getRaw();
        then:
        data.size()==2
    }

    def "statistics can register mbean"(){
        Statistic stat = new Statistic("com.focusit.serverlight.statistics", "2sec", new Date().getTime(), 2, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);
        Statistic stat2 = new Statistic("com.focusit.serverlight.statistics", "1sec", new Date().getTime(), 1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);

        when:
        stat.registerMBean();
        stat2.registerMBean();

        stat.addData(3.14);
        stat2.addData(3.24);

        Thread.sleep(3000);

        stat.addData(6.14);
        stat2.addData(6.24);

        Map<Long, IntervalStatistics> data = stat.getRaw();
        then:
        data.size()==2
    }
}