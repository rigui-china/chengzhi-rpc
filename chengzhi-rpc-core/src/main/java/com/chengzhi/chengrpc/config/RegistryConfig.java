package com.chengzhi.chengrpc.config;

import com.chengzhi.chengrpc.registry.RegistryKeys;
import lombok.Data;

/**
 * @author 徐晟智
 * @version 1.0
 */

/**
 * rpc 框架注册中心配置
 */
@Data
public class RegistryConfig {
    /**
     * 注册中心类别
     */
    private String registry = RegistryKeys.ETCD;

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2380";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间（单位毫秒）
     */
    private Long timeout = 10000L;
}
