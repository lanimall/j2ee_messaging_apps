j2ee-jms-examples: JMS example using multiple Java EE 6 patterns
==============================================================================================

Author: [Fabien Sanglier](mailto:Fabien.Sanglier@softwareag.com)
Project Source: <https://github.com/SoftwareAG/universalmessaging-samples/tree/master/J2EE_SimplePubSub>

What is it?
-----------
2 example applications for JMS Publishing and JMS Subscribing leveraging J2EE constructs with JCA Resource Adapters.
These examples have been tested and "should" work with the [Software AG Universal Messaging](http://www2.softwareag.com/it/products/terracotta/universal_messaging.aspx) resource adapter
on both JBOSS EAP 6 and IBM Websphere 8.x platforms.

For any issue, please submit an issue on github.

Content
-------

* SimpleJmsSend
  * Sends JMS messages to UM queues/topics via JCA proxies (JCA Admin objects and Conection Factories) using the resource Adapter. Multiple approach to test:
    * "Send And Forget"
    * "Send And Wait For Reply"
* SimpleJmsConsume
  * Consumes JMS messages from queues/topics using the UM Resource Adapter,
  * Ability to perform some mock processing (eg. sleep time, mock exceptions at intervals, etc...),
  * Ability to call remote EJBs in the SimpleJmsSend application in order to chain message processing with extra message sending,
  * Ability to reply to another queue if the "replyTo" field is specified in the message (or if a default "replyTo" is set)
* j2ee-jms-examples-libs
  * Shared library that contains global code and especially the custom-built JMSHelper that encapsulate simple JMS contructs
  * A simple counter (accessible via servlet) is also made available to track various metrics (message sent, message consumed, processing success, processing errors, etc...)
  
Resource Adapter Guides
-----------------------

Detail guides to setup Software AG Universal Messaging Resource Adapter on both JBOSS EAP 6 and IBM Websphere 8.x are availble at:

* [Integration and Configuration of SoftwareAG’s Universal Messaging with JBOSS EAP 6.1](http://techcommunity.softwareag.com/web/guest/pwiki/-/wiki/Main/Integration+and+Configuration+of+SoftwareAG’s+Universal+Messaging+with+JBOSS+EAP+6.1)
* [Integration and configuration of sofwareag’s universal messaging with ibm websphere application server](http://techcommunity.softwareag.com/web/guest/pwiki/-/wiki/Main/integration+and+configuration+of+sofwareag’s+universal+messaging+with+ibm+websphere+application+server)

Application Building and Configuration Guide
--------------------------------------------

TDB...