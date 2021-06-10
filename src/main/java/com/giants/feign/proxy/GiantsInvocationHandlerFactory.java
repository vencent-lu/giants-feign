package com.giants.feign.proxy;

import feign.InvocationHandlerFactory;
import feign.Target;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * GiantsInvocationHandlerFactory TODO
 * date time: 2021/6/9 19:00
 * Copyright 2021 github.com/vencent-lu/giants-boot Inc. All rights reserved.
 *
 * @author vencent-lu
 * @since 1.0
 */
public class GiantsInvocationHandlerFactory implements InvocationHandlerFactory {

    private boolean logCallStackTimeAnalyse = false;

    private boolean logArguments = false;

    public GiantsInvocationHandlerFactory() {
    }

    public GiantsInvocationHandlerFactory(boolean logCallStackTimeAnalyse, boolean logArguments) {
        this.logCallStackTimeAnalyse = logCallStackTimeAnalyse;
        this.logArguments = logArguments;
    }

    @Override
    public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
        return new GiantsInvocationHandler(target, dispatch, this.logCallStackTimeAnalyse, this.logArguments);
    }

    public boolean isLogArguments() {
        return logArguments;
    }

    public void setLogArguments(boolean logArguments) {
        this.logArguments = logArguments;
    }
}
