package com.disaster.emergency.controller;

import com.disaster.emergency.common.LoginRequest;
import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.User;
import com.disaster.emergency.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
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

        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 10;

        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (StringUtils.hasText(role)) {
            wrapper.eq("role", role);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("username", keyword)
                    .or()
                    .like("real_name", keyword)
                    .or()
                    .like("phone", keyword));
        }

        wrapper.orderByDesc("update_time");

        Page<User> userPage = userService.page(pageParam, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("total", userPage.getTotal());
        result.put("pages", userPage.getPages());
        result.put("current", userPage.getCurrent());
        result.put("size", userPage.getSize());
        result.put("records", userPage.getRecords());

        return Result.success("查询成功", result);
    }

    @Operation(summary = "新增用户（指挥中心）", description = "指挥中心管理员创建新用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "10002", description = "创建失败，参数错误")
    })
    @PostMapping
    public Result<User> createUser(@Valid @RequestBody User user) {
        try {
            user.setId(null);
            if (!StringUtils.hasText(user.getStatus())) {
                user.setStatus("active");
            }
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            boolean saved = userService.save(user);
            if (!saved) {
                return Result.error(10002, "用户创建失败");
            }
            return Result.success("创建成功", user);
        } catch (Exception e) {
            return Result.error(10002, e.getMessage());
        }
    }

    @Operation(summary = "修改用户信息（指挥中心）", description = "根据ID修改用户基本信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "10003", description = "更新失败，用户不存在或参数错误")
    })
    @PutMapping("/{id}")
    public Result<User> updateUser(
            @PathVariable @Min(1) Long id,
            @RequestBody User user) {
        User existing = userService.getById(id);
        if (existing == null) {
            return Result.error(10003, "用户不存在");
        }

        // 仅更新非空字段
        if (StringUtils.hasText(user.getUsername())) {
            existing.setUsername(user.getUsername());
        }
        if (StringUtils.hasText(user.getPassword())) {
            existing.setPassword(user.getPassword());
        }
        if (StringUtils.hasText(user.getRealName())) {
            existing.setRealName(user.getRealName());
        }
        if (StringUtils.hasText(user.getPhone())) {
            existing.setPhone(user.getPhone());
        }
        if (StringUtils.hasText(user.getEmail())) {
            existing.setEmail(user.getEmail());
        }
        if (StringUtils.hasText(user.getRole())) {
            existing.setRole(user.getRole());
        }
        if (StringUtils.hasText(user.getStatus())) {
            existing.setStatus(user.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());

        boolean updated = userService.updateById(existing);
        if (!updated) {
            return Result.error(10003, "用户更新失败");
        }
        return Result.success("更新成功", existing);
    }

    @Operation(summary = "删除用户（指挥中心）", description = "根据ID删除用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "10004", description = "删除失败，用户不存在")
    })
    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteUser(@PathVariable @Min(1) Long id) {
        boolean removed = userService.removeById(id);
        if (!removed) {
            return Result.error(10004, "用户不存在或删除失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("success", true);
        return Result.success("删除成功", data);
    }
}