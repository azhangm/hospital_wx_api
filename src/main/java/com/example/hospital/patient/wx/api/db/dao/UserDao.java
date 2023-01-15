package com.example.hospital.patient.wx.api.db.dao;

import com.example.hospital.patient.wx.api.db.pojo.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
@Mapper
public interface UserDao {

    void insert(UserEntity param);

    Integer searchAlreadyRegistered(String openId);
}
