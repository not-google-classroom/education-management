package com.org.education_management.service;

import com.org.education_management.util.RolesUtil;

import java.util.Map;
import java.util.logging.Logger;

public class RolesService {

    private static final Logger logger = Logger.getLogger(RolesService.class.getName());
    private static RolesService rolesService = null;

    public static RolesService getInstance() {
        if(rolesService == null) {
            rolesService = new RolesService();
        }
        return rolesService;
    }

    public Map<String, Object> getAllRoles(Long roleID) {
        return RolesUtil.getInstance().getRolesList(roleID);
    }
}
