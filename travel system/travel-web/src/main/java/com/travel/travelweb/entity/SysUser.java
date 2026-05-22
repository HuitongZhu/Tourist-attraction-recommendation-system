package com.travel.travelweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "`user`")
public class SysUser {

    @Id
    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "userName", length = 30)
    private String userName;

    @Column(name = "userPassword", length = 100)
    private String userPassword;

    /** 1 管理员 2 普通用户 */
    @Column(name = "userType", length = 1)
    private String userType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
