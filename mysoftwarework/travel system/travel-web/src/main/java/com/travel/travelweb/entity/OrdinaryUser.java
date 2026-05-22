package com.travel.travelweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordinaryuser")
public class OrdinaryUser {

    @Id
    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "PhoneNumber", length = 20)
    private String phoneNumber;

    @Column(name = "RealName", length = 20)
    private String realName;

    @Column(name = "IDNumber", length = 20)
    private String idNumber;

    @Column(name = "Gender", length = 10)
    private String gender;

    @Column(name = "Birthday", length = 20)
    private String birthday;

    @Column(name = "RegisterTime")
    private LocalDateTime registerTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }
}
