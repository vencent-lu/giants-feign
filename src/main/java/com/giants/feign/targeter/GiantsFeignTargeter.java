package com.giants.feign.targeter;

import feign.Feign;
import feign.Target;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.openfeign.Targeter;

import java.util.Map;

/**
 * GiantsFeignTargeter TODO
 * date time: 2021/11/17 14:52
 * Copyright 2021 github.com/vencent-lu/giants-feign Inc. All rights reserved.
 *
 * @author vencent-lu
 * @since 1.1
 */
public class GiantsFeignTargeter implements Targeter {

    private Map<String, String> clientVersionMap;

    @Override
    public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context,
                        Target.HardCodedTarget<T> target) {
        String version = this.getClientVersion(target.name());
        if (StringUtils.isEmpty(version)) {
            return feign.target(target);
        }
        return feign.target(new GiantsTarget<>(target, version));
    }

    public static class GiantsTarget<T> extends Target.HardCodedTarget<T> {

        private final String version;

        public GiantsTarget(Target<T> target, String version) {
            super(target.type(), target.name(),
                    new StringBuilder(target.url()).append('-').append(version.replace('.','-')).toString());
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }

    public String getClientVersion(String name) {
        if (this.clientVersionMap == null) {
            return null;
        }
        return this.clientVersionMap.get(name);
    }

    public void setClientVersionMap(Map<String, String> clientVersionMap) {
        this.clientVersionMap = clientVersionMap;
    }
}
