package com.giants.feign.proxy;

import com.giants.analyse.profiler.ExecutionTimeProfiler;
import feign.InvocationHandlerFactory;
import feign.Target;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * GiantsInvocationHandler TODO
 * date time: 2021/6/9 19:06
 * Copyright 2021 github.com/vencent-lu/giants-boot. All rights reserved.
 *
 * @author vencent-lu
 * @since 1.0
 */
public class GiantsInvocationHandler implements InvocationHandler {

    private InvocationHandler invocationHandler;

    private boolean logCallStackTimeAnalyse = false;

    private boolean logArguments = false;

    public GiantsInvocationHandler(Target target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {
        this.invocationHandler = new InvocationHandlerFactory.Default().create(target, dispatch);
    }

    public GiantsInvocationHandler(Target target,
                                   Map<Method, InvocationHandlerFactory.MethodHandler> dispatch,
                                   boolean logCallStackTimeAnalyse,
                                   boolean logArguments) {
        this.invocationHandler = new InvocationHandlerFactory.Default().create(target, dispatch);
        this.logCallStackTimeAnalyse = logCallStackTimeAnalyse;
        this.logArguments = logArguments;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (this.logCallStackTimeAnalyse && ExecutionTimeProfiler.getEntry() != null) {
            try{
                ExecutionTimeProfiler.enter(this.getMethod(method, args));
                return this.invocationHandler.invoke(proxy, method, args);
            } catch (Throwable e) {
                throw e;
            } finally {
                ExecutionTimeProfiler.release();
            }
        } else {
            return this.invocationHandler.invoke(proxy, method, args);
        }
    }

    private String getMethod(Method method, Object[] arguments) {
        String targetName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        StringBuffer sb = new StringBuffer();
        sb.append(targetName).append(".").append(methodName);
        if (logArguments) {
            sb.append("(");
            if ((arguments != null) && (arguments.length != 0)) {
                for (int i = 0; i < arguments.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(String.valueOf(arguments[i]));
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public boolean isLogCallStackTimeAnalyse() {
        return logCallStackTimeAnalyse;
    }

    public void setLogCallStackTimeAnalyse(boolean logCallStackTimeAnalyse) {
        this.logCallStackTimeAnalyse = logCallStackTimeAnalyse;
    }

    public boolean isLogArguments() {
        return logArguments;
    }

    public void setLogArguments(boolean logArguments) {
        this.logArguments = logArguments;
    }
}
