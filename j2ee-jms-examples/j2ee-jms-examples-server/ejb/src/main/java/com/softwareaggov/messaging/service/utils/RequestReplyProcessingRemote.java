package com.softwareaggov.messaging.service.utils;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public interface RequestReplyProcessingRemote extends java.rmi.Remote {
    String performMultiplicationFromStrings(String nb1, String nb2) throws java.rmi.RemoteException;
    Long getRandomNumber() throws java.rmi.RemoteException;
}
