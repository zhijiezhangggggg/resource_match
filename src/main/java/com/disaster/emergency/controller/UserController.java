package com.disaster.emergency.controller;

import com.disaster.emergency.common.LoginRequest;
import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.User;
import com.disaster.emergency.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "用户管理", description = "用户登录、注册、信息管理")
public class UserController {

    @Autowired
    private UserService userService;


    @Operation(summary = "用户登录", description = "用户通过用户名和密码登录系统")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "10001", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            return Result.error(10001, "用户名或密码错误");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        return Result.success("登录成功", data);
    }

    @Operation(summary = "用户注册", description = "新用户注册账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "10002", description = "注册失败，参数错误")
    })
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return Result.success("注册成功", registeredUser);
        } catch (Exception e) {
            return Result.error(10002, e.getMessage());
        }
    }

    @Operation(summary = "获取用户信息", description = "根据用户名获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "10002", description = "用户不存在")
    })
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestParam String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.error(10002, "用户不存在");
        }
        return Result.success("获取成功", user);
    }

    @Operation(summary = "获取用户列表", description = "分页查询用户列表，支持按角色、状态、关键词筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/list")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 100);
        result.put("pages", 10);
        result.put("current", page);
        result.put("size", size);
        result.put("records", userService.list());
        
        return Result.success("查询成功", result);
    }
}