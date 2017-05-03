package com.softwareaggov.messaging.service.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.Random;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Local(RequestReplyProcessing.class)
public class RequestReplyProcessingBean implements RequestReplyProcessing {
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