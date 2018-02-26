package com.softwareaggov.messaging.service.endpoints.jms.mdb;

import com.softwareaggov.messaging.service.endpoints.jms.mdb.processor.MDBProcessorLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenBean;
import javax.jms.MessageListener;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@MessageDriven(name = "CalculationProcessingMDB")
public class CalculationProcessingMDB extends AbstractConsumeReply implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(CalculationProcessingMDB.class);

    @EJB(beanName = "CalculationMDBProcessorBean", beanInterface = MDBProcessorLocal.class)
    private MDBProcessorLocal messageProcessing;

    @Override
    protected MDBProcessorLocal getMessageProcessing() {
        return messageProcessing;
    }
}