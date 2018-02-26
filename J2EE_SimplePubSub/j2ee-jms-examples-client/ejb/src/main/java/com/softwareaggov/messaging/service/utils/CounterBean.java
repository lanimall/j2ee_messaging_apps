package com.softwareaggov.messaging.service.utils;

import com.softwareaggov.messaging.libs.utils.Counter;
import com.softwareaggov.messaging.libs.utils.impl.CounterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;

/**
 * Created by fabien.sanglier on 6/28/16.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(CounterLocal.class)
public class CounterBean implements CounterLocal {
    private static Logger log = LoggerFactory.getLogger(CounterBean.class);

    private Counter counter;

    @PostConstruct
    public void initialize() {
        counter = new CounterImpl();
    }

    @Override
    public String[] getAllCounterNames() {
        return counter.getAllCounterNames();
    }

    @Override
    public long getCount(String key) {
        return counter.getCount(key);
    }

    @Override
    public long incrementAndGet(String key) {
        return counter.incrementAndGet(key);
    }

    @Override
    public long reset(String counterName) {
        return counter.reset(counterName);
    }

    @Override
    public long getCountRate(String key) {
        return counter.getCountRate(key);
    }
}
