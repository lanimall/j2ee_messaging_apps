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
 */com.softwareaggov.messaging.libs.utils;

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
