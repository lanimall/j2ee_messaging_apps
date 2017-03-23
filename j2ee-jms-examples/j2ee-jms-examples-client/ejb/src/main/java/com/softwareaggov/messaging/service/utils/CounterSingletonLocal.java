package com.softwareaggov.messaging.service.utils;

/**
 * Created by fabien.sanglier on 6/28/16.
 */
public interface CounterSingletonLocal {
    String[] getAllCounterNames();
    long getCount(String key);
    long incrementAndGet(String key);
    long reset(String counterName);
    long getCountRate(String key);
}
