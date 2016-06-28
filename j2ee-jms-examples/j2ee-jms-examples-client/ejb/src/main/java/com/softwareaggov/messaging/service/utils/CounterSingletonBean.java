package com.softwareaggov.messaging.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fabien.sanglier on 6/28/16.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(CounterSingletonLocal.class)
public class CounterSingletonBean implements CounterSingletonLocal {
    private static Logger log = LoggerFactory.getLogger(CounterSingletonBean.class);

    private ConcurrentHashMap<String, Integer> counters = null;

    @PostConstruct
    public void initialize() {
        this.counters = new ConcurrentHashMap();
    }

    @Override
    public String[] getAllCounterNames() {
        List<String> keys = new ArrayList<String>();
        for(String keyName : counters.keySet()){
            keys.add(keyName);
        }
        return keys.toArray(new String[keys.size()]);
    }

    @Override
    public int getCount(String key) {
        int count = (counters.containsKey(key))?counters.get(key):0;
        log.debug(String.format("getting counter for key %s = %d", key, count));
        return count;
    }

    @Override
    public void increment(String key) {
        log.debug("incrementing counter key " + key);
        if (counters.putIfAbsent(key, new Integer(1)) == null) {
            log.debug("initialized new key and put count to 1");
            return;
        }

        Integer old;
        do {
            old = counters.get(key);
            log.debug("old key " + old);
        } while (!counters.replace(key, old, old+1)); // Assumes no removal.

        log.debug("new count post increment:" + getCount(key));
    }

    @Override
    public int reset(String key) {
        log.debug("resetting counter key " + key);

        return counters.replace(key, new Integer(0));
    }
}
