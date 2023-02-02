package com.example.hospital.patient.wx.api.db.dao;

import com.example.hospital.patient.wx.api.db.pojo.UserInfoCardEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoCardDao {
    String searchUserTel(Integer userId);

    void insert(UserInfoCardEntity param);
}
