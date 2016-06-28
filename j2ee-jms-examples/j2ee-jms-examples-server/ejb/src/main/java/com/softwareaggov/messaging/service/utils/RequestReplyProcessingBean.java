package com.softwareaggov.messaging.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(RequestReplyProcessingLocal.class)
public class RequestReplyProcessingBean implements RequestReplyProcessingLocal {
    private static Logger log = LoggerFactory.getLogger(RequestReplyProcessingBean.class);

    @Override
    public int performMultiplication(int nb1, int nb2) {
        return nb1 * nb2;
    }

    @Override
    public String performMultiplicationFromStrings(String nb1, String nb2) {
        String response;
        try {
            response = new Integer(performMultiplication(Integer.parseInt(nb1), Integer.parseInt(nb2))).toString();
        } catch (NumberFormatException e) {
            response = "NAN";
        }
        return response;
    }
}