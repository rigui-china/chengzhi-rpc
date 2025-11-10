package com.chengzhi.example.common.service;

/**
 * @author 徐晟智
 * @version 1.0
 */

import com.chengzhi.example.common.model.User;

/**
 * 用户服务
 */
public interface UserService {
    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);

    default short getNumber(){
        return 1;
    }
}
