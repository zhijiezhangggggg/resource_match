package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
    User register(User user);
    User getUserByUsername(String username);
}
