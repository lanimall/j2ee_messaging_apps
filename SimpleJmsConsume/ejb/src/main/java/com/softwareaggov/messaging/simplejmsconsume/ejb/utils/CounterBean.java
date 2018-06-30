/*
 * Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softwareaggov.messaging.simplejmsconsume.ejb.utils;

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
