package com.focusit.serverlight.specs.statistic

import com.focusit.serverlight.statistics.CalculatedStatistics
import com.focusit.serverlight.statistics.Statistic
import spock.lang.Specification

import javax.management.MBeanServer
import javax.management.openmbean.CompositeDataSupport
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit


class StatisticSpec extends Specification {

    def "statistics can store and retrieve data"(){
        Statistic stat = new Statistic("com.focusit.serverlight.statistics", "500ms", 500, TimeUnit.MILLISECONDS, 60, TimeUnit.SECONDS);

        when:
        stat.addData(3.14);

        Thread.sleep(1500);

        stat.addData(6.14);

        Collection<CalculatedStatistics> data = stat.get()
        then:
        data.size()==2
    }

    def "statistics can be resetteed"(){
        Statistic stat = new Statistic("com.focusit.serverlight.statistics", "500ms", 500, TimeUnit.MILLISECONDS, 60, TimeUnit.SECONDS);

        when:
        stat.addData(3.14);

        Thread.sleep(1500);

        stat.addData(6.14);

        Collection<CalculatedStatistics> data = stat.get()
        stat.reset();
        Collection<CalculatedStatistics> data2 = stat.get()
        then:
        data.size()==2
        data2.size()==0
    }

    def "statistics can register mbean"(){
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Statistic stat = new Statistic("com.focusit.serverlight.statistics", "2sec", 2, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);

        stat.registerMBean();
        String mbeanName = "com.focusit.serverlight.statistics:type=2sec"
        GroovyMBean bean = new GroovyMBean(server, mbeanName)

        stat.addData(1.0)
        stat.addData(2.0)
        when:
            String name = bean.getProperty("Name")

            List<CompositeDataSupport> data = bean.getProperty("Data")

            long count = data.get(0).get("count");
            long max = data.get(0).get("max");
            long sum = data.get(0).get("sum");
        then:
            name.equals("2sec")
            count==2
            max==2
            sum==3
    }
}