package com.travel.travelweb.service;

import com.travel.travelweb.entity.SysUser;
import java.util.Optional;

public class LoginResult {
    public enum LoginError {
        NONE,
        ACCOUNT_NOT_FOUND,
        WRONG_PASSWORD
    }

    private final Optional<SysUser> user;
    private final LoginError error;
    private final String errorMessage;

    private LoginResult(Optional<SysUser> user, LoginError error, String errorMessage) {
        this.user = user;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public static LoginResult success(SysUser user) {
        return new LoginResult(Optional.of(user), LoginError.NONE, "");
    }

    public static LoginResult accountNotFound(String message) {
        return new LoginResult(Optional.empty(), LoginError.ACCOUNT_NOT_FOUND, message);
    }

    public static LoginResult wrongPassword(String message) {
        return new LoginResult(Optional.empty(), LoginError.WRONG_PASSWORD, message);
    }

    public boolean isSuccess() {
        return user.isPresent();
    }

    public Optional<SysUser> getUser() {
        return user;
    }

    public LoginError getError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
