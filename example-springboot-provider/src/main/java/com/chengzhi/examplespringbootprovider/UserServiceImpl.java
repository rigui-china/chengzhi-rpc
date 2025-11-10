package com.chengzhi.examplespringbootprovider;

import com.chengzhi.chengzhirpcspringbootstarter.annotation.RpcService;
import com.chengzhi.example.common.model.User;
import com.chengzhi.example.common.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author 徐晟智
 * @version 1.0
 */
@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名：" + user.getName());
        return user;
    }
}
