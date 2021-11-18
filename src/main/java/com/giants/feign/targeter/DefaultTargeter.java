package com.giants.feign.targeter;

import feign.Feign;
import feign.Target;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.openfeign.Targeter;

/**
 * DefaultTargeter TODO
 * date time: 2021/11/17 17:57
 * Copyright 2021 github.com/vencent-lu/giants-feign Inc. All rights reserved.
 *
 * @author vencent-lu
 * @since 1.1
 */
public class DefaultTargeter implements Targeter {
    @Override
    public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context,
                        Target.HardCodedTarget<T> target) {
        return feign.target(target);
    }
}
