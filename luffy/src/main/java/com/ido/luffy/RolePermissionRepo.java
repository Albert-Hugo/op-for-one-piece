package com.ido.luffy;

import java.util.Map;
import java.util.Set;

/**
 * get role permission mapping
 *
 * @author Carl
 * @date 2019/6/11
 */
public interface RolePermissionRepo {


    /**
     * e.g.
     * admin : [/admin/detail , /admin/create]
     *
     * @return role => permission table
     */
    Map<String, Set<String>> allRolesMapping();

    /**
     * get permission base on role
     *
     * @param role the role
     * @return the permission collection
     */
    Set<String> rolePermission(String role);
}
