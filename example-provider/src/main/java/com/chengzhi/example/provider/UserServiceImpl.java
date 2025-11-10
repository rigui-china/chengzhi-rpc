package com.chengzhi.example.provider;

import com.chengzhi.example.common.model.User;
import com.chengzhi.example.common.service.UserService;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class UserServiceImpl implements UserService {
    public User getUser(User user) {
        System.out.println("用户名" + user.getName());
        return user;
    }
}
