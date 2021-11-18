package com.giants.feign.configuration;

import com.giants.feign.codec.FeignExceptionDecoder;
import com.giants.feign.querymap.BeanQueryMapNestEncoder;
import com.giants.feign.targeter.DefaultTargeter;
import com.giants.feign.targeter.GiantsFeignTargeter;
import feign.Feign;
import feign.QueryMapEncoder;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.apache.commons.collections.MapUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.Targeter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GiantsFeignConfiguration TODO
 * date time: 2021/11/17 16:59
 * Copyright 2021 github.com/vencent-lu/giants-feign Inc. All rights reserved.
 *
 * @author vencent-lu
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(GiantsFeignProperties.class)
public class GiantsFeignConfiguration {

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .retryer(Retryer.NEVER_RETRY);
    }

    @Bean
    public QueryMapEncoder createQueryMapEncoder() {
        return new BeanQueryMapNestEncoder();
    }

    @Bean
    public ErrorDecoder createFeignExceptionDecoder(GiantsFeignProperties giantsFeignProperties) {
        FeignExceptionDecoder feignExceptionDecoder = new FeignExceptionDecoder();
        if (giantsFeignProperties.getResponseExceptionStatus() != null) {
            feignExceptionDecoder.setResponseExceptionStatus(giantsFeignProperties.getResponseExceptionStatus());
        }
        return feignExceptionDecoder;
    }

    @Bean
    @ConditionalOnMissingBean
    public Targeter createFeignTargeter(GiantsFeignProperties giantsFeignProperties) {
        if (MapUtils.isEmpty(giantsFeignProperties.getClientVersionMap())) {
            return new DefaultTargeter();
        }
        GiantsFeignTargeter giantsFeignTargeter = new GiantsFeignTargeter();
        giantsFeignTargeter.setClientVersionMap(giantsFeignProperties.getClientVersionMap());
        return giantsFeignTargeter;
    }

}
