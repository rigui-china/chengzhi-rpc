package com.chengzhi.chengrpc.loadbalancer;

/**
 * @author 徐晟智
 * @version 1.0
 */

public interface LoadBalancerKeys {

    String ROUND_ROBIN = "roundRobin";

    String RANDOM = "random";

    String CONSISTENT_HASH = "consistentHash";
}
