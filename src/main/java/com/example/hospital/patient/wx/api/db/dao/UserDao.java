package com.example.hospital.patient.wx.api.db.dao;

import com.example.hospital.patient.wx.api.db.pojo.UserEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserDao {
    void insert(UserEntity param);

    Integer searchAlreadyRegistered(String openId);

    Map<String ,Object> searchUserInfo(Integer userId);

    Map<String,Object> searchOpenId(int userId);

}
