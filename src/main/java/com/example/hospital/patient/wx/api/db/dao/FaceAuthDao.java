package com.example.hospital.patient.wx.api.db.dao;

import com.example.hospital.patient.wx.api.db.pojo.FaceAuthEntity;

import java.util.Map;

public interface FaceAuthDao {
    Integer hasFaceAuthInDay(Map<String,Object> param);

    Integer insert(FaceAuthEntity entity);
}




