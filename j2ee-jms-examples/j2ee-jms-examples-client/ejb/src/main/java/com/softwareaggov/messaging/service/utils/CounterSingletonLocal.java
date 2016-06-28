package com.softwareaggov.messaging.service.utils;

/**
 * Created by fabien.sanglier on 6/28/16.
 */
public interface CounterSingletonLocal {
    String[] getAllCounterNames();
    int getCount(String key);
    void increment(String key);
    int reset(String counterName);
}
