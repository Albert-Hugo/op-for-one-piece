package com.ido.luffy;

import java.util.Map;
import java.util.Set;

/**
 * @author Carl
 * @date 2019/6/14
 */
class MemoryPermissionRepo implements RolePermissionRepo {
    private Map<String, Set<String>> table;

    public MemoryPermissionRepo(Map<String, Set<String>> rolesUrlTable) {
        this.table = rolesUrlTable;
    }

    @Override
    public Map<String, Set<String>> allRolesMapping() {
        return table;
    }

    @Override
    public Set<String> rolePermission(String role) {
        return table.get(role);
    }
}
