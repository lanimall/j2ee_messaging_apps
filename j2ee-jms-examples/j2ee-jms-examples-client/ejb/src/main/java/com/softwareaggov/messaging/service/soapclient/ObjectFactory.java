
package com.softwareaggov.messaging.service.soapclient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.softwareaggov.messaging.service.soapclient package. 
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

    private final static QName _GetRandomNumberResponse_QNAME = new QName("http://com.softwareaggov.messaging/jaxws/jms", "getRandomNumberResponse");
    private final static QName _PerformMultiplicationFromStrings_QNAME = new QName("http://com.softwareaggov.messaging/jaxws/jms", "performMultiplicationFromStrings");
    private final static QName _GetRandomNumber_QNAME = new QName("http://com.softwareaggov.messaging/jaxws/jms", "getRandomNumber");
    private final static QName _PerformMultiplicationFromStringsResponse_QNAME = new QName("http://com.softwareaggov.messaging/jaxws/jms", "performMultiplicationFromStringsResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.softwareaggov.messaging.service.soapclient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PerformMultiplicationFromStrings }
     * 
     */
    public PerformMultiplicationFromStrings createPerformMultiplicationFromStrings() {
        return new PerformMultiplicationFromStrings();
    }

    /**
     * Create an instance of {@link GetRandomNumberResponse }
     * 
     */
    public GetRandomNumberResponse createGetRandomNumberResponse() {
        return new GetRandomNumberResponse();
    }

    /**
     * Create an instance of {@link PerformMultiplicationFromStringsResponse }
     * 
     */
    public PerformMultiplicationFromStringsResponse createPerformMultiplicationFromStringsResponse() {
        return new PerformMultiplicationFromStringsResponse();
    }

    /**
     * Create an instance of {@link GetRandomNumber }
     * 
     */
    public GetRandomNumber createGetRandomNumber() {
        return new GetRandomNumber();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRandomNumberResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://com.softwareaggov.messaging/jaxws/jms", name = "getRandomNumberResponse")
    public JAXBElement<GetRandomNumberResponse> createGetRandomNumberResponse(GetRandomNumberResponse value) {
        return new JAXBElement<GetRandomNumberResponse>(_GetRandomNumberResponse_QNAME, GetRandomNumberResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformMultiplicationFromStrings }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://com.softwareaggov.messaging/jaxws/jms", name = "performMultiplicationFromStrings")
    public JAXBElement<PerformMultiplicationFromStrings> createPerformMultiplicationFromStrings(PerformMultiplicationFromStrings value) {
        return new JAXBElement<PerformMultiplicationFromStrings>(_PerformMultiplicationFromStrings_QNAME, PerformMultiplicationFromStrings.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRandomNumber }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://com.softwareaggov.messaging/jaxws/jms", name = "getRandomNumber")
    public JAXBElement<GetRandomNumber> createGetRandomNumber(GetRandomNumber value) {
        return new JAXBElement<GetRandomNumber>(_GetRandomNumber_QNAME, GetRandomNumber.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformMultiplicationFromStringsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://com.softwareaggov.messaging/jaxws/jms", name = "performMultiplicationFromStringsResponse")
    public JAXBElement<PerformMultiplicationFromStringsResponse> createPerformMultiplicationFromStringsResponse(PerformMultiplicationFromStringsResponse value) {
        return new JAXBElement<PerformMultiplicationFromStringsResponse>(_PerformMultiplicationFromStringsResponse_QNAME, PerformMultiplicationFromStringsResponse.class, null, value);
    }

}
