package com.softwareaggov.messaging.service.publish.soap;

import com.softwareaggov.messaging.service.soapclient.RequestReplyService;
import com.softwareaggov.messaging.service.soapclient.RequestReplyServicePortType;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;
import java.net.URL;

/**
 * Created by fabien.sanglier on 6/29/16.
 */
@Stateless(mappedName = "RequestReplySoapJmsClientBean")
@Local(RequestReplyClientLocal.class)
public class RequestReplySoapJmsClientBean implements RequestReplyClientLocal {

    private transient RequestReplyServicePortType servicePort;
    private final String WSDL_LOCATION = "/META-INF/wsdl/RequestReplyJmsService.wsdl";

    public RequestReplySoapJmsClientBean() {
    }

    @PostConstruct
    public void initialize() {
        URL serviceWsdl = RequestReplySoapHttpClientBean.class.getResource(WSDL_LOCATION);
        RequestReplyService service = new RequestReplyService(serviceWsdl);
        this.servicePort = service.getRequestReplyServicePort();
    }

    public Long getRandomNumber(){
        return servicePort.getRandomNumber();
    }

    public String performMultiplicationFromStrings(String nb1, String nb2) {
        return servicePort.performMultiplicationFromStrings(nb1, nb2);
    }
}