package com.softwareaggov.messaging.service.processor;

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
@Local(CalculationProcessingLocal.class)
public class CalculationProcessingBean implements CalculationProcessingLocal {
    private static Logger log = LoggerFactory.getLogger(CalculationProcessingBean.class);

    private transient Random rnd;

    @PostConstruct
    protected void init() {
        rnd = new Random(System.currentTimeMillis());
    }

    @Override
    public String performMultiplicationFromStrings(String nb1, String nb2) {
        String result;
        try {
            result = new Long(performMultiplication(Long.parseLong(nb1), Long.parseLong(nb2))).toString();
        } catch (NumberFormatException nfe) {
            log.error("Error parsing the numbers", nfe);
            result = "NaN";
        }
        return result;
    }

    @Override
    public Long getRandomNumber() {
        return rnd.nextLong();
    }

    private long performMultiplication(long nb1, long nb2) {
        return nb1 * nb2;
    }

}