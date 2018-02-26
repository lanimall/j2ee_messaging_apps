package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.compareTests;

import com.softwareaggov.messaging.libs.jms.CachedConnectionFactory;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherLocal;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsSendAndForgetBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Created by fabien.sanglier on 6/28/16.
 */

@Stateless(name = "JmsSendAndForgetCachedConnectionTestService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(JmsPublisherLocal.class)
public class JmsSendAndForgetCachedConnectionTestBean extends JmsSendAndForgetBean implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetCachedConnectionTestBean.class);

    @Resource(name = "jms/someManagedCF")
    private ConnectionFactory jmsConnectionFactory;

    @Resource(name = "jms/someManagedDestination")
    private Destination jmsDestination;

    @Override
    public ConnectionFactory getJmsConnectionFactory() {
        return new CachedConnectionFactory(jmsConnectionFactory);
    }

    @Override
    public Destination getJmsDestination() {
        return jmsDestination;
    }
}