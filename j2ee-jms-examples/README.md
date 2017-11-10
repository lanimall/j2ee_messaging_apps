j2ee-jms-examples: JMS example using multiple Java EE 6 patterns
==============================================================================================
Author: Fabien Sanglier
Source: <https://github.com/>  

What is it?
-----------
Collection of simple JMS pub/sub examples using JCA Resource Adapters constructs.
The examples have been tested with [Software AG Universal Messaging](http://www2.softwareag.com/it/products/terracotta/universal_messaging.aspx) 
on both JBOSS EAP 6 and IBM Websphere 8.x

Content
-------

* SimpleJmsSend
  * Sends JMS messages to queue/topic proxies (JCA Admin objects) using the resource Adapter following either a simple "Send And Forget" approach, or a "Send And Wait For Reply".
* SimpleJmsConsume
  * Consumes JMS messages from queues using the UM Resource Adapter, perform some mock processing, and optionnaly reply to another queue if the "replyTo" field is specified in the consumed message)
* j2ee-jms-examples-libs
  * Shared library that contains global code and especially the custom-built JMSHelper that encapsulate simple JMS contructs.
  
Resource Adapter Guides
-----------------------

Detail guides to setup Software AG Universal Messaging Resource Adapter on both JBOSS EAP 6 and IBM Websphere 8.x are availble at:

* [Integration and Configuration of SoftwareAG’s Universal Messaging with JBOSS EAP 6.1](http://techcommunity.softwareag.com/web/guest/pwiki/-/wiki/Main/Integration+and+Configuration+of+SoftwareAG’s+Universal+Messaging+with+JBOSS+EAP+6.1)
* [Integration and configuration of sofwareag’s universal messaging with ibm websphere application server](http://techcommunity.softwareag.com/web/guest/pwiki/-/wiki/Main/integration+and+configuration+of+sofwareag’s+universal+messaging+with+ibm+websphere+application+server)