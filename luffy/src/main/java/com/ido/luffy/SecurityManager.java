package com.ido.luffy;


import java.util.Set;

public class SecurityManager<ID> {
    public static final String ADMIN_ROLE = RoleConstant.ADMIN;
    private ThreadLocal<Authentication<ID>> authenticationThreadLocal = new ThreadLocal<>();

    /**
     * init the security manager
     *
     * @param rolePermissionRepo the repository to get the role permission
     */
    public SecurityManager(RolePermissionRepo rolePermissionRepo) {
        this.rolePermissionRepo = rolePermissionRepo;
    }

    private RolePermissionRepo rolePermissionRepo;


    /**
     * need to set authentication after user pass jtw token verification
     *
     * @param a
     */
    public void setUserAuthorization(Authentication<ID> a) {
        authenticationThreadLocal.set(a);


    }

    public Authentication<ID> getUserAuthorization() {
        return authenticationThreadLocal.get();

    }

    /**
     * get the user id
     *
     * @return
     */
    public ID getUserId() {
        Authentication<ID> a = getUserAuthorization();
        if (a == null) {
            return null;
        }

        return a.getUserId();


    }


    /**
     * get thr permission by role
     *
     * @param role the role
     * @return the permission set
     */
    public Set<String> getRolePermission(String role) {

        return rolePermissionRepo.rolePermission(role);
    }


}
