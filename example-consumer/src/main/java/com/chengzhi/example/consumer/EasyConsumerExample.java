package com.chengzhi.example.consumer;

import com.chengzhi.chengrpc.proxy.ServiceProxyFactory;
import com.chengzhi.example.common.model.User;
import com.chengzhi.example.common.service.UserService;

/**
 * @author 徐晟智
 * @version 1.0
 */

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("chengzhi");
        //调用
        User newUser = userService.getUser(user);
        if(newUser != null) {
            System.out.println(newUser.getName());
        }else{
            System.out.println("user == null");
        }
    }
}
