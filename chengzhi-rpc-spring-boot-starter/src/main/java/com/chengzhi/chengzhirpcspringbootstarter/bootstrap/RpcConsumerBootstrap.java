package com.chengzhi.chengzhirpcspringbootstarter.bootstrap;

import com.chengzhi.chengrpc.proxy.ServiceProxyFactory;
import com.chengzhi.chengzhirpcspringbootstarter.annotation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Field;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class RpcConsumerBootstrap implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        //遍历对象的所有属性
        Field[] declaredFields = beanClass.getDeclaredFields();
        for(Field field : declaredFields){
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if(rpcReference != null){
                //为属性生成代理对象
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if(interfaceClass == void.class){
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
                try{
                    field.set(bean, proxyObject);
                    field.setAccessible(false);
                } catch (Exception e) {
                    throw new RuntimeException("字段对象注入失败",e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
