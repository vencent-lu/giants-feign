package com.giants.feign.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * GiantsFeignProperties TODO
 * date time: 2021/11/17 16:18
 * Copyright 2021 github.com/vencent-lu/giants-feign Inc. All rights reserved.
 *
 * @author vencent-lu
 * @since 1.0
 */
@ConfigurationProperties(prefix = "giants.feign")
public class GiantsFeignProperties {

    /**
     * FeignExceptionDecoder 反序列化异常对应状态码
     */
    private Integer responseExceptionStatus;

    /**
     * Feign client 版本号
     */
    private Map<String, String> clientVersionMap;

    public Integer getResponseExceptionStatus() {
        return responseExceptionStatus;
    }

    public void setResponseExceptionStatus(Integer responseExceptionStatus) {
        this.responseExceptionStatus = responseExceptionStatus;
    }

    public Map<String, String> getClientVersionMap() {
        return clientVersionMap;
    }

    public void setClientVersionMap(Map<String, String> clientVersionMap) {
        this.clientVersionMap = clientVersionMap;
    }
}
