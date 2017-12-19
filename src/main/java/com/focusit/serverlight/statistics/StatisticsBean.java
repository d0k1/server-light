package com.focusit.serverlight.statistics;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class StatisticsBean {
    private Statistic statistic;

    public StatisticsBean(Statistic statistic){
        this.statistic = statistic;
    }

    public void registerBean(ObjectName name) throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(statistic, name);
    }
}
