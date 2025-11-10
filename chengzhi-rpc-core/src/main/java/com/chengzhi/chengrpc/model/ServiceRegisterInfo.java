package com.chengzhi.chengrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 徐晟智
 * @version 1.0
 */

/**
 * 服务注册消息类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegisterInfo<T> {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 实现类
     */
    private Class<? extends T> implClass;
}
