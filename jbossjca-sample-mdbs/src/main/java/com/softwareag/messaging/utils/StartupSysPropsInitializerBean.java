package com.softwareag.messaging.utils;

import org.jboss.security.vault.SecurityVaultException;
import org.jboss.security.vault.SecurityVaultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Created by FabienSanglier on 7/22/14.
 */

@Singleton
//@Startup
public class StartupSysPropsInitializerBean {
    private static Logger log = LoggerFactory.getLogger(StartupSysPropsInitializerBean.class);

    private static final String PROP_RESOURCE = "wmjms.properties";
    private static final String PROPS_PREFIX = "com.webmethods.jms";

    public StartupSysPropsInitializerBean() { }

    @PostConstruct
    private void setSysPropertiesFromFile(){
        Properties properties = new Properties();
        try {
            log.info(String.format("Loading classpath property file %s into system properties...", PROP_RESOURCE));

            //this file should be loaded in classpath
            InputStream st = this.getClass().getClassLoader().getResourceAsStream(PROP_RESOURCE);
            if(null != st)
                properties.load(st);
            else
                log.warn(String.format("Property file %s not found in classpath", PROP_RESOURCE));
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
}