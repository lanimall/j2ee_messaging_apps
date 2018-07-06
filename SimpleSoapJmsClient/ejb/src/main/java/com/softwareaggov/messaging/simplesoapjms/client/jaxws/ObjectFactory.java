
package com.softwareaggov.messaging.simplesoapjms.client.jaxws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.softwareaggov.messaging.simplesoapjms.client.jaxws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ProcessAndReply_QNAME = new QName("http://soap.ejb.server.simplesoapjms.messaging.softwareaggov.com/", "processAndReply");
    private final static QName _ProcessAndReplyResponse_QNAME = new QName("http://soap.ejb.server.simplesoapjms.messaging.softwareaggov.com/", "processAndReplyResponse");
    private final static QName _ProcessOneWay_QNAME = new QName("http://soap.ejb.server.simplesoapjms.messaging.softwareaggov.com/", "processOneWay");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.softwareaggov.messaging.simplesoapjms.client.jaxws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProcessOneWay }
     * 
     */
    public ProcessOneWay createProcessOneWay() {
        return new ProcessOneWay();
    }

    /**
     * Create an instance of {@link ProcessAndReplyResponse }
     * 
     */
    public ProcessAndReplyResponse createProcessAndReplyResponse() {
        return new ProcessAndReplyResponse();
    }

    /**
     * Create an instance of {@link ProcessAndReply }
     * 
     */
    public ProcessAndReply createProcessAndReply() {
        return new ProcessAndReply();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessAndReply }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.ejb.server.simplesoapjms.messaging.softwareaggov.com/", name = "processAndReply")
    public JAXBElement<ProcessAndReply> createProcessAndReply(ProcessAndReply value) {
        return new JAXBElement<ProcessAndReply>(_ProcessAndReply_QNAME, ProcessAndReply.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessAndReplyResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.ejb.server.simplesoapjms.messaging.softwareaggov.com/", name = "processAndReplyResponse")
    public JAXBElement<ProcessAndReplyResponse> createProcessAndReplyResponse(ProcessAndReplyResponse value) {
        return new JAXBElement<ProcessAndReplyResponse>(_ProcessAndReplyResponse_QNAME, ProcessAndReplyResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessOneWay }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.ejb.server.simplesoapjms.messaging.softwareaggov.com/", name = "processOneWay")
    public JAXBElement<ProcessOneWay> createProcessOneWay(ProcessOneWay value) {
        return new JAXBElement<ProcessOneWay>(_ProcessOneWay_QNAME, ProcessOneWay.class, null, value);
    }

}
