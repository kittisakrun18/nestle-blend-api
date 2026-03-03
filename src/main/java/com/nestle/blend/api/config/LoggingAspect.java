package com.nestle.blend.api.config;

import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.nestle.blend.api.controller.*.*(..))")
    public void logBeforeMethod(){
        ThreadContext.put("logDynamicKey", String.valueOf(UUID.randomUUID()));
    }

    @After("execution(* com.nestle.blend.api.controller.*.*(..))")
    public void logAfterMethod(){
        ThreadContext.clearAll();
    }
}
