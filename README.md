j2ee_messaging_apps: JMS Messaging Apps using common Java EE patterns and Resource Adapter JCA patterns
=========================================================================================================

Author: [Fabien Sanglier](mailto:Fabien.Sanglier@softwareag.com)

Project Source: <https://github.com/lanimall/j2ee_messaging_apps>

What is it?
-----------
Modular J2EE messaging applications that rely on common JCA Resource Adapters to interact with Messaging provider such as SoftwareAG Universal Messaging.
Using these application, it's easy to create different message pub/sub designs by "plugging" multiple apps together via EJB lookups.
These J2EE apps are compliant with EJB specs and JCA specs, and have been tested on both JBOSS EAP 6 and IBM Websphere 8.x platforms 
interacting with the the [Software AG Universal Messaging Server](http://www2.softwareag.com/it/products/terracotta/universal_messaging.aspx) via its JCA-compliant resource adapter.
Resource Adapter.

Important Note:
This project is open-sourced and provided AS-IS without any warrenty...and is not supported by SoftwareAG. 
For any issue, please submit an issue on github and the developper community will make every attempts to fix them asap.

Content
-------

* SimpleJmsSend
  * Sends JMS messages to UM queues/topics via JCA proxies (JCA Admin objects and Conection Factories) using the Resource Adapter. Multiple approach to test:
    * "Send And Forget"
    * "Send And Wait For Reply"
* SimpleJmsConsume
  * Consumes JMS messages from queues/topics using the UM Resource Adapter,
  * Ability to perform some mock processing (eg. sleep time, mock exceptions at intervals, etc...),
  * Ability to call remote EJBs that implement the JmsPublisherRemote interface (eg. the ones in the SimpleJmsSend application) in order to chain message comsumption with extra message sending,
  * Ability to reply to another queue if the "replyTo" field is specified in the message (or if a default "replyTo" is set)
* libs
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