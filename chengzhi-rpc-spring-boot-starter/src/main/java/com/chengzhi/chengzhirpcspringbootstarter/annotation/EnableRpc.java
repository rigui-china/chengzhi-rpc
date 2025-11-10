package com.chengzhi.chengzhirpcspringbootstarter.annotation;

import com.chengzhi.chengzhirpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.chengzhi.chengzhirpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.chengzhi.chengzhirpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 徐晟智
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {
    /**
     * 需要启动 server
     * @return
     */
    boolean needServer() default true;
}
