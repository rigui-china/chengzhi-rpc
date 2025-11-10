package com.chengzhi.chengrpc.registry;

/**
 * @author 徐晟智
 * @version 1.0
 */

import com.chengzhi.chengrpc.spi.SpiLoader;

/**
 * 注册中心工厂对象
 */
public class RegistryFactory {

    static {
        SpiLoader.load(Registry.class);
    }
    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
