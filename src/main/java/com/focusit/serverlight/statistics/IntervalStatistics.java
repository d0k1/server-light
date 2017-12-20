package com.focusit.serverlight.statistics;

import javax.management.openmbean.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

public class IntervalStatistics implements Serializable, CompositeData {
    public double min;
    public double max;
    public double mean;
    public double stddev;
    public double p50;
    public double p95;
    public double p99;
    public double p999;
    public double sum;
    public long count=0;
    public AtomicLong lastVersion = new AtomicLong(-1);

    @Override
    public CompositeType getCompositeType() {
        try {
            return new CompositeType("IntervalStatistics", "Statistics",
                    new String[]{"min", "max", "mean", "stddev", "p50", "p95", "p99", "p999", "sum", "count", "lastVersion"},
                    new String[]{"min", "max", "mean", "std dev", "50%", "95%", "99%", "99.9%", "sum", "count", "last version"},
                    new OpenType[]{SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.LONG, SimpleType.LONG});
        } catch (OpenDataException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public Object[] getAll(String[] keys) {
        return new Object[0];
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Collection<?> values() {
        return null;
    }
}
