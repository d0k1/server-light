package com.focusit.serverlight.specs.statistic

import com.focusit.serverlight.statistics.Measurements
import com.focusit.serverlight.statistics.StatisticItem
import spock.lang.Specification


class MeasurementsSpec extends Specification {

    def "round time is working correctly"() {
        Measurements measurements = new Measurements(5000, 100);
        when:
        long rounded0 = measurements.roundTime(7500, 5000)
        long rounded1 = measurements.roundTime(9999, 5000)
        long rounded2 = measurements.roundTime(29999, 5000)
        then:
        rounded0 == 5000
        rounded1 == rounded0
        rounded2 == 25000
    }

    def "measurements can hold data"() {
        Measurements measurements = new Measurements(5000, 100);
        when:
        measurements.addData(1.0)
        measurements.addData(2.0)
        Collection<StatisticItem> statistics = measurements.getData()
        then:
        statistics.size()==1
        statistics.iterator().next().statistics.count==2
        statistics.iterator().next().statistics.min==1.0
        statistics.iterator().next().statistics.max==2.0
        statistics.iterator().next().statistics.mean==1.5
    }

    def "measurements can hold data for multiple intervals"() {
        Measurements measurements = new Measurements(500, 100);
        when:
        measurements.addData(1.0)
        Thread.currentThread().sleep(1000);
        measurements.addData(2.0)
        Collection<StatisticItem> statistics = measurements.getData()
        Iterator<StatisticItem> iterator = statistics.iterator()
        then:
        statistics.size()==2
        iterator.next().statistics.count==1
        iterator.next().statistics.count==1
    }

    def "measurements can calculate time intervals correctly"(){
        Measurements measurements = new Measurements(5000, 100);
    }

}