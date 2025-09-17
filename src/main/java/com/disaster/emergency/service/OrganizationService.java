package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.Organization;

public interface OrganizationService extends IService<Organization> {
    Organization saveOrganization(Organization organization);
}
