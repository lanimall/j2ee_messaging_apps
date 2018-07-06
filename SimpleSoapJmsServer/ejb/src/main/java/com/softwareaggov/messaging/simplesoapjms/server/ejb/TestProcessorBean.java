/*
 * Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softwareaggov.messaging.simplesoapjms.server.ejb;

import com.softwareaggov.messaging.libs.utils.Counter;
import com.softwareaggov.messaging.libs.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import java.io.IOException;

/**
 * Created by fabien.sanglier on 7/3/18.
 */
@Stateless(name = "TestProcessorService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(TestProcessorLocal.class)
public class TestProcessorBean implements TestProcessor {
    private static Logger log = LoggerFactory.getLogger(TestProcessorBean.class);

    @EJB(beanName = "CounterService")
    protected Counter messageProcessingCounter;

    @Resource(name = "replyPayloadFilePath")
    private String replyPayloadFilePath = null;

    private transient String replyPayload;

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    @PostConstruct
    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");

        try {
            replyPayload = FileUtils.getFileContent(replyPayloadFilePath);
        } catch (IOException e) {
            throw new EJBException("Could not load file identified by path: " + replyPayloadFilePath);
        }
    }

    @PreDestroy
    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
    }

    @Override
    public String processAndReply(String content) {
        if(log.isDebugEnabled())
            log.debug("content:" + content);

        messageProcessingCounter.incrementAndGet(getBeanName() + "-processAndReply");

        // if the reply payload is null, simply reply with the size of the input content
        String replyPayloadStr = null;
        if(null == replyPayload){
            replyPayloadStr = new StringBuilder()
                    .append("Hello! Your request contained ")
                    .append(((null != content)? new Integer(content.length()).toString(): "0"))
                    .append(" characters!")
                    .toString();
        } else {
            replyPayloadStr = replyPayload;
        }

        return replyPayloadStr;
    }

    @Override
    public void processOneWay(String content) {
        if(log.isDebugEnabled())
            log.debug("content:" + content);

        messageProcessingCounter.incrementAndGet(getBeanName() + "-processOneWay");

        // do nothing aside from logging the content if in debug
    }
}
