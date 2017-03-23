package com.softwareaggov.messaging.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.jws.WebService;
import java.util.Random;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(RequestReplyProcessingLocal.class)
@Remote(RequestReplyProcessingRemote.class)
@WebService
        (
                targetNamespace = "http://com.softwareaggov.messaging/jaxws/jms",
                portName = "RequestReplyPort",
                serviceName = "RequestReplyService",
                name = "RequestReplyProcessing"
        )
public class RequestReplyProcessingBean implements RequestReplyProcessingLocal, RequestReplyProcessingRemote {
    private static Logger log = LoggerFactory.getLogger(RequestReplyProcessingBean.class);

    private transient Random rnd;

    @PostConstruct
    protected void init(){
        rnd = new Random(System.currentTimeMillis());
    }

    private long performMultiplication(long nb1, long nb2) {
        return nb1 * nb2;
    }

    @Override
    public String performMultiplicationFromStrings(String nb1, String nb2) {
        String response;
        try {
            response = new Long(performMultiplication(Long.parseLong(nb1), Long.parseLong(nb2))).toString();
        } catch (NumberFormatException e) {
            response = "NAN";
        }
        return response;
    }

    @Override
    public Long getRandomNumber() {
        return rnd.nextLong();
    }
}