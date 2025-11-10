package com.chengzhi.chengrpc.loadbalancer;

import com.chengzhi.chengrpc.spi.SpiLoader;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFUALT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
