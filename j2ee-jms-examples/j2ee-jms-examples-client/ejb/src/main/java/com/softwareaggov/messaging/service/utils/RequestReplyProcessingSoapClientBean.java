package com.softwareaggov.messaging.service.utils;

import com.softwareaggov.messaging.service.soapclient.RequestReplyProcessing;
import com.softwareaggov.messaging.service.soapclient.RequestReplyService;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * Created by fabien.sanglier on 6/29/16.
 */
@Stateless(mappedName = "RequestReplyProcessingSoapClientBean")
@Local(RequestReplyProcessingClientLocal.class)
public class RequestReplyProcessingSoapClientBean implements RequestReplyProcessingClientLocal {

    private transient RequestReplyProcessing servicePort;

    public RequestReplyProcessingSoapClientBean() {
    }

    @PostConstruct
    public void initialize() {
        RequestReplyService service = new RequestReplyService();
        this.servicePort = (RequestReplyProcessing)service.getRequestReplyPort();
    }

    public Long getRandomNumber(){
        return servicePort.getRandomNumber();
    }

    public String performMultiplicationFromStrings(String nb1, String nb2) {
        return servicePort.performMultiplicationFromStrings(nb1, nb2);
    }
}
