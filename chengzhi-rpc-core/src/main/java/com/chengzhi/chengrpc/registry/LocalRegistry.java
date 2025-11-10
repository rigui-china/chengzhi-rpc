package com.chengzhi.chengrpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class LocalRegistry {

    /**
     * 注册信息存储
     */
    private static final Map<String,Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册本地服务
     * @param serviceName
     * @param serviceClass
     */
    public static void registerService(String serviceName, Class<?> serviceClass) {
        map.put(serviceName,serviceClass);
    }

    /**
     * 获取服务
     * @param serviceName
     * @return
     */
    public static Class<?> getService(String serviceName){
        return map.get(serviceName);
    }

    /**
     * 删除服务
     * @param serviceName
     */
    public static void removeService(String serviceName){
        map.remove(serviceName);
    }
}
