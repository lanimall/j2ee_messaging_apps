package com.softwareag.messaging.utils;

import org.jboss.security.vault.SecurityVaultException;
import org.jboss.security.vault.SecurityVaultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import java.util.Map;
import java.util.Properties;

/**
 * Created by FabienSanglier on 7/22/14.
 */

@Singleton
//@Startup
public class SysPropsDecrypterStartupBean {
    private static Logger log = LoggerFactory.getLogger(SysPropsDecrypterStartupBean.class);
    private static final String PROPS_PREFIX = "com.webmethods.jms";

    public SysPropsDecrypterStartupBean() { }

    @PostConstruct
    private void autoDecrypt(){
        Properties properties = System.getProperties();
        for (Map.Entry entry : properties.entrySet()) {
            String propKey = entry.getKey().toString();

            if(propKey.startsWith(PROPS_PREFIX)){
                String propValue = entry.getValue().toString();
                if(log.isDebugEnabled())
                    log.debug(String.format("%s = %s", propKey, propValue));

                if (SecurityVaultUtil.isVaultFormat(propValue)) {
                    try {
                        log.debug(String.format("Vault encrypted value detected for property %s...decrypting it", propKey));
                        System.setProperty(propKey, SecurityVaultUtil.getValueAsString(propValue));
                    } catch (SecurityVaultException e) {
                        log.error("Could not decrypt the value...moving on", e);
                    }
                }
            }
        }
    }
}