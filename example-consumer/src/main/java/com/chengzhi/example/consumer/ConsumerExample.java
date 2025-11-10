package com.chengzhi.example.consumer;

import com.chengzhi.chengrpc.bootstrap.ConsumerBootstrap;
import com.chengzhi.chengrpc.config.RpcConfig;
import com.chengzhi.chengrpc.proxy.ServiceProxyFactory;
import com.chengzhi.chengrpc.utils.ConfigUtils;
import com.chengzhi.example.common.model.User;
import com.chengzhi.example.common.service.UserService;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class ConsumerExample {
    public static void main(String[] args) {
        //服务提供者初始化
        ConsumerBootstrap.init();

        //获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("chengzhi");
        User newUser = userService.getUser(user);
        if(newUser != null) {
            System.out.println(newUser.getName());
        }else{
            System.out.println("user == null");
        }


    }
}
