package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.User;
import com.disaster.emergency.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
@Validated
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || username.trim().isEmpty()) {
            return Result.error(10001, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error(10001, "密码不能为空");
        }

        User user = userService.login(username, password);
        if (user == null) {
            return Result.error(10001, "用户名或密码错误");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        
        return Result.success("登录成功", data);
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody User user) {
        try {
            // 验证用户名格式
            if (user.getUsername() == null || !user.getUsername().matches("^[a-zA-Z0-9_]{3,20}$")) {
                return Result.error(10002, "用户名格式不正确，应为3-20位字母数字下划线");
            }
            
            // 验证密码长度
            if (user.getPassword() == null || user.getPassword().length() < 6 || user.getPassword().length() > 20) {
                return Result.error(10002, "密码长度必须在6-20位之间");
            }
            
            // 验证手机号格式
            if (user.getPhone() != null && !user.getPhone().matches("^1[3-9]\\d{9}$")) {
                return Result.error(10002, "手机号格式不正确");
            }
            
            // 验证邮箱格式
            if (user.getEmail() != null && !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return Result.error(10002, "邮箱格式不正确");
            }
            
            User registeredUser = userService.register(user);
            return Result.success("注册成功", registeredUser);
        } catch (Exception e) {
            return Result.error(10002, e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestParam String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return Result.error(10002, "用户不存在");
            }
            return Result.success("获取成功", user);
        } catch (Exception e) {
            return Result.error(10002, "获取用户信息失败");
        }
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        // 简单的分页查询实现
        Map<String, Object> result = new HashMap<>();
        result.put("total", 100);
        result.put("pages", 10);
        result.put("current", page);
        result.put("size", size);
        result.put("records", userService.list());
        
        return Result.success("查询成功", result);
    }
}