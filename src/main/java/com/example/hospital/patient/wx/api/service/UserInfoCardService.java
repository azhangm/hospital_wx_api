package com.example.hospital.patient.wx.api.service;

import com.example.hospital.patient.wx.api.db.pojo.UserInfoCardEntity;

import java.util.Map;

public interface UserInfoCardService {

    Map<String,Object> searchUserInfoCard(Integer userId);

    void update(UserInfoCardEntity entity);
}
