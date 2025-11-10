package com.chengzhi.chengrpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author 徐晟智
 * @version 1.0
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //根据方法的返回值类型，生成特定的默认值对象
        Class<?> returnType = method.getReturnType();
        log.info("mock invoke {}",method.getName());
        return getDefaultObject(returnType);
    }

    /**
     * 生成指定类型的默认值对象
     * @param returnType
     * @return
     */
    private Object getDefaultObject(Class<?> returnType) {
        if(returnType.isPrimitive()){
            if(returnType == boolean.class){
                return false;
            }else if(returnType == short.class){
                return  (short) 0;
            }else if(returnType == int.class){
                return 0;
            }else if(returnType == long.class){
                return 0L;
            }
        }
        return null;
    }
}
