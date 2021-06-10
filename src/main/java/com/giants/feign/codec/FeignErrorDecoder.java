package com.giants.feign.codec;

import com.alibaba.fastjson.JSON;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * FeignErrorDecoder TODO
 * date time: 2021/6/4 14:14
 * Copyright 2021 github.com/vencent-lu/giants-boot Inc. All rights reserved.
 * @author vencent-lu
 * @since 1.0
 */
public class FeignErrorDecoder implements ErrorDecoder {

    private final static Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 600) {
            try {
                return (Exception)JSON.parse(Util.toString(response.body().asReader(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                logger.error("Exception deserialization failed !",e);
                return new Default().decode(methodKey, response);
            }
        }
        return new Default().decode(methodKey, response);
    }
}
