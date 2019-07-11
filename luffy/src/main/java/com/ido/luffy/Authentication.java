package com.ido.luffy;

/**
 * this interface represent authentication for login user to access protected resource
 *
 * @param <ID> the user id
 */
public interface Authentication<ID> {

    /**
     * the login user id
     *
     * @return
     */
    ID getUserId();

    /**
     * get the login user role
     *
     * @return
     */
    String getRole();

    /**
     * the user name
     *
     * @return
     */
    String getUserName();

    /**
     * the payload
     *
     * @return
     */
    Object getPayload();


}
