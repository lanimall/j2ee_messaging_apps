package com.softwareaggov.messaging.service.publish.soap;

/**
 * Created by fabien.sanglier on 6/29/16.
 */
public interface RequestReplyClientLocal {
    Long getRandomNumber();
    String performMultiplicationFromStrings(String nb1, String nb2);
}
