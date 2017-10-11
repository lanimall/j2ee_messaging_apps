package com.softwareaggov.messaging.service.processor;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public interface CalculationProcessingLocal {
    String performMultiplicationFromStrings(String nb1, String nb2);

    Long getRandomNumber();
}
