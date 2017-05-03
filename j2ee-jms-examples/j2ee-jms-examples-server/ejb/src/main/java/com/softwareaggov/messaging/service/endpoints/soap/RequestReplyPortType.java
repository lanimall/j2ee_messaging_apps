package com.softwareaggov.messaging.service.endpoints.soap;

import javax.jws.WebService;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@WebService(
        name = "RequestReplyPortType",
        targetNamespace = "http://com.softwareaggov.messaging/j2ee-jms-examples/requestreply"
)
public interface RequestReplyPortType {
    String performMultiplicationFromStrings(String nb1, String nb2);
    Long getRandomNumber();
}
