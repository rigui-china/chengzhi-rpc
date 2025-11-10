package com.chengzhi.chengzhirpcspringbootstarter.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 徐晟智
 * @version 1.0
 */

/**
 * 服务消费者注解（用于注入服务）
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RpcReference {
    /**
     * 服务接口类
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本
     */
    String serviceVersion() default "1.0";

    /**
     * 负载均衡器
     * @return
     */
    String loadBalancer() default "roundRobin";

    /**
     * 容错策略
     * @return
     */
    String tolerantStrategy() default "failFast";

    /**
     * 模拟调用
     * @return
     */
    boolean mock() default false;
}
