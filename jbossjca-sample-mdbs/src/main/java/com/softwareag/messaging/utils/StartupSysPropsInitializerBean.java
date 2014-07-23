package com.softwareag.messaging.utils;

import org.jboss.security.vault.SecurityVaultException;
import org.jboss.security.vault.SecurityVaultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by FabienSanglier on 7/22/14.
 */
@Singleton
@Startup
public class StartupSysPropsInitializerBean {
    private static Logger log = LoggerFactory.getLogger(StartupSysPropsInitializerBean.class);

    private static final String PROP_RESOURCE = "wmjms.properties";
    private static final String PROPS_PREFIX = "com.webmethods.jms";

    @PostConstruct
    private void startup(){
        Properties properties = new Properties();
        try {
            log.info(String.format("Loading %s into system properties...", PROP_RESOURCE));

            //this file should be loaded in classpath
            InputStream st = this.getClass().getClassLoader().getResourceAsStream(PROP_RESOURCE);
            if(null != st)
                properties.load(st);
        } catch (IOException e) {
            log.error("Error reading properties.", e);
        }

        log.info(String.format("%d properties found", properties.size()));
        if(properties.size() > 0) {
            for (Map.Entry entry : properties.entrySet()) {
                String propKey = entry.getKey().toString();
                String propValue = entry.getValue().toString();

                if (SecurityVaultUtil.isVaultFormat(propValue)) {
                    try {
                        log.debug(String.format("Vault encrypted value detected for property %s...decrypting it", propKey));
                        System.setProperty(propKey, SecurityVaultUtil.getValueAsString(propValue));
                    } catch (SecurityVaultException e) {
                        log.error("Could not decrypt the value...moving on", e);
                    }
                } else {
                    log.debug(String.format("%s = %s", propKey, propValue));
                    System.setProperty(propKey, propValue);
                }
            }
        }
    }

    @PreDestroy
    private void shutdown(){
        //clearing the properties related to com.webmethods.jms
        log.info(String.format("Clearing %s from system properties...", PROPS_PREFIX));

        Properties properties = System.getProperties();
        List<String> propsToclear = new ArrayList<String>();
        if(properties.size() > 0) {
            for (Map.Entry entry : properties.entrySet()) {
                if(entry.getKey().toString().startsWith(PROPS_PREFIX)){
                    propsToclear.add((String)entry.getKey());
                }
            }
        }

        if(propsToclear.size() > 0){
            for (String propKey : propsToclear) {
                log.debug(String.format("Clearing property %s", propKey));
                System.clearProperty(propKey);
            }
        }
    }
}