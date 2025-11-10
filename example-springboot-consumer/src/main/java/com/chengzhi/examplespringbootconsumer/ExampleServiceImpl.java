package com.chengzhi.examplespringbootconsumer;

import com.chengzhi.chengzhirpcspringbootstarter.annotation.RpcReference;
import com.chengzhi.example.common.model.User;
import com.chengzhi.example.common.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author 徐晟智
 * @version 1.0
 */
@Service
public class ExampleServiceImpl {
    @RpcReference
    private UserService userService;

    public void test(){
        User user = new User();
        user.setName("chengzhi");
        User serviceUser = userService.getUser(user);
        System.out.println(serviceUser.getName());
    }

}
