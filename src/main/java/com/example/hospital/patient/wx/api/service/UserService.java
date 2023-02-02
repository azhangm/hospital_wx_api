package com.example.hospital.patient.wx.api.service;

import com.example.hospital.patient.wx.api.db.pojo.UserInfoCardEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public interface UserService {
    Map<String,Object> loginOrRegistered(Map<String,Object> param);

    void insertUserInfoCard(UserInfoCardEntity param);

    Map<String, Object> searchUserInfo(Integer userId);
}
