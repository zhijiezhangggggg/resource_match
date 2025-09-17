package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Organization;
import com.disaster.emergency.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/organization")
@CrossOrigin
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @PostMapping("/save")
    public Result<Organization> saveOrganization(@RequestBody Organization organization) {
        try {
            // 参数验证
            if (organization.getOrgName() == null || organization.getOrgName().trim().isEmpty()) {
                return Result.error(40001, "机构名称不能为空");
            }
            if (organization.getOrgType() == null || organization.getOrgType().trim().isEmpty()) {
                return Result.error(40001, "机构类型不能为空");
            }
            if (organization.getProvince() == null || organization.getProvince().trim().isEmpty()) {
                return Result.error(40001, "省份不能为空");
            }
            if (organization.getCity() == null || organization.getCity().trim().isEmpty()) {
                return Result.error(40001, "城市不能为空");
            }
            if (organization.getDistrict() == null || organization.getDistrict().trim().isEmpty()) {
                return Result.error(40001, "区县不能为空");
            }
            
            // 验证联系电话格式
            if (organization.getContactPhone() != null && !organization.getContactPhone().matches("^1[3-9]\\d{9}$")) {
                return Result.error(40001, "联系电话格式不正确");
            }
            
            // 验证邮箱格式
            if (organization.getEmail() != null && !organization.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return Result.error(40001, "邮箱格式不正确");
            }
            
            Organization savedOrganization = organizationService.saveOrganization(organization);
            return Result.success("机构保存成功", savedOrganization);
        } catch (Exception e) {
            return Result.error(40001, "机构保存失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getOrganizationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orgType,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {
        
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 10);
        result.put("pages", 1);
        result.put("current", page);
        result.put("size", size);
        result.put("records", organizationService.list());
        
        return Result.success("查询成功", result);
    }
}