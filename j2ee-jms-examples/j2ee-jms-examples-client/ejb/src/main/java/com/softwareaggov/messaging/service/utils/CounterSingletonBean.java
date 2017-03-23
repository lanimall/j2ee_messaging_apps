package com.softwareaggov.messaging.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fabien.sanglier on 6/28/16.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(CounterSingletonLocal.class)
public class CounterSingletonBean implements CounterSingletonLocal {
    private static Logger log = LoggerFactory.getLogger(CounterSingletonBean.class);

    private ConcurrentHashMap<String, Long> counters;

    private volatile Long lastCounterCheckpointTime;
    private volatile HashMap<String, Long> counterPreviousCheckpoint;
    private volatile HashMap<String, Long> countersRates;

    @PostConstruct
    public void initialize() {
        this.counters = new ConcurrentHashMap<String, Long>();
        this.counterPreviousCheckpoint = new HashMap<String, Long>();
        this.countersRates = new HashMap<String, Long>();
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
    public long getCount(String key) {
        long count = (counters.containsKey(key))?counters.get(key):0L;
        log.debug(String.format("getting counter for key %s = %d", key, count));
        return count;
    }

    @Override
    public long getCountRate(String key) {
        long rate = (countersRates.containsKey(key))?countersRates.get(key):0L;
        log.debug(String.format("getting counter rate for key %s = %d", key, rate));
        return rate;
    }

    @Override
    public long incrementAndGet(String key) {
        log.debug("incrementing counter key " + key);
        if (counters.putIfAbsent(key, new Long(1)) == null) {
            log.debug("initialized new key and put count to 1");
            return 1L;
        }

        Long oldVal, newVal;
        do {
            oldVal = counters.get(key);
            newVal = oldVal + 1;
        } while (!counters.replace(key, oldVal, newVal)); // Assumes no removal.

        log.debug("new count post increment:" + newVal.toString());
        return newVal;
    }

    @Override
    public long reset(String key) {
        log.debug("resetting counter key " + key);
        return counters.replace(key, new Long(0));
    }

    @Schedule(hour = "*", minute = "*", second = "*/5", persistent = false, info="rate calculation timer")
    public void calculateRates() {
        long now = new Date().getTime();
        long timeSinceLastCheckpoint = 0L;
        if(null != lastCounterCheckpointTime) {
            timeSinceLastCheckpoint = now - lastCounterCheckpointTime;
        }

        for (Map.Entry<String, Long> entry : counters.entrySet()) {
            //make copies to make sure nothing changes during the processing here
            String currentCounterKey = entry.getKey();
            long currentCount = entry.getValue();

            if(counterPreviousCheckpoint.containsKey(currentCounterKey) && timeSinceLastCheckpoint > 0) {
                long diffCount = currentCount - counterPreviousCheckpoint.get(currentCounterKey);
                if(diffCount > 0)
                    countersRates.put(currentCounterKey, diffCount * 1000 / timeSinceLastCheckpoint);
                else
                    countersRates.put(currentCounterKey, 0L);
            } else {
                countersRates.put(currentCounterKey, 0L);
            }

            //save the current values in the previous checkpoint hashmap
            counterPreviousCheckpoint.put(currentCounterKey, currentCount);
        }

        lastCounterCheckpointTime = now;
    }
}
