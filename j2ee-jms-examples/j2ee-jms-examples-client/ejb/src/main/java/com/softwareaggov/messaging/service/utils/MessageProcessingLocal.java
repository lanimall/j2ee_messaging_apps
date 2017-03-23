package com.softwareaggov.messaging.service.utils;

import javax.jms.Message;

/**
 * Created by fabien.sanglier on 6/23/16.
 */
public interface MessageProcessingLocal {
    void processReqReplyResponseMessage(Message rcvMessage);
    void processSimpleQueueResponseMessage(Message rcvMessage);
}
