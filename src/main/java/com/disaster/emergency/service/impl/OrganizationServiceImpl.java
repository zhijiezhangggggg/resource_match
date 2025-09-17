package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Organization;
import com.disaster.emergency.mapper.OrganizationMapper;
import com.disaster.emergency.service.OrganizationService;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    @Override
    public Organization saveOrganization(Organization organization) {
        organization.setStatus("active");
        organization.setCreateTime(java.time.LocalDateTime.now());
        organization.setUpdateTime(java.time.LocalDateTime.now());
        save(organization);
        return organization;
    }
}
