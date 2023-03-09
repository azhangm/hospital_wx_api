package com.example.hospital.patient.wx.api.db.dao;

import com.example.hospital.patient.wx.api.db.pojo.UserInfoCardEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserInfoCardDao {
    String searchUserTel(Integer userId);

    void insert(UserInfoCardEntity param);

    Map<String,Object> searchUserInfoCard(Integer userId);

    void update(UserInfoCardEntity entity);

    Integer hasUserInfoCard(int userId);

    Boolean searchExistFaceModel(Integer userId);

     void updateExistFaceModel(Map<String,Object> param);
}
