/*
 * Copyright 2012 by
 *
 * Software AG, Uhlandstrasse 12, D-64297 Darmstadt, GERMANY
 *
 * All rights reserved
 *
 * This software is the confidential and proprietary
 * information of Software AG ('Confidential Information').
 * You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license
 * agreement you entered into with Software AG or its distributors.
 */

package com.softwareag.messaging.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConfig {
    public static final String APP_CONFIGPATH_DEFAULT = "application.properties";
    public static final String APP_CONFIGPATH_ENVPROP = "app.config.path";
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    //singleton instance
    private static AppConfig instance;
    //wrapper property
    private final PropertyUtils propertyUtils;

    private AppConfig(String propertyFile) throws Exception {
        propertyUtils = new PropertyUtils(propertyFile);
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {  //1
                if (instance == null) {
                    try {
                        String propLocation = "";
                        if (null != System.getProperty(APP_CONFIGPATH_ENVPROP)) {
                            propLocation = System.getProperty(APP_CONFIGPATH_ENVPROP);
                            log.info(APP_CONFIGPATH_ENVPROP + " environment property specified: Loading application configuration from " + propLocation);
                        } else {
                            log.info("Loading application configuration from classpath " + APP_CONFIGPATH_DEFAULT);
                            propLocation = APP_CONFIGPATH_DEFAULT;
                        }

                        instance = new AppConfig(propLocation);
                    } catch (Exception e) {
                        log.error("Could not load the property file", e);
                    }
                }
            }
        }
        return instance;
    }

    public PropertyUtils getPropertyHelper() {
        return propertyUtils;
    }
}
