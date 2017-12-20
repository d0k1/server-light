package com.focusit.serverlight.specs.statistic

import com.focusit.serverlight.statistics.IntervalStatistics
import com.focusit.serverlight.statistics.Statistic
import spock.lang.Specification

import java.util.concurrent.TimeUnit


class StatisticSpec extends Specification {

    def "statistics can store and retrieve data"(){
        Statistic stat = new Statistic("com.focusit.serverlight.statistics", "5sec", new Date().getTime(), 2, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);

        when:
        stat.addData(3.14);

        Thread.sleep(3000);

        stat.addData(6.14);

        Map<Long, IntervalStatistics> data = stat.getRaw();
        then:
        data.size()==2
    }
}