package com.healthtrail.common.utils.jackson;

/**
 * Jackson序列化/反序列化异常
 *
 * @author valarchie
 */
public class JacksonException extends RuntimeException {

    public JacksonException(String message, Exception e) {
        super(message, e);
    }

}
