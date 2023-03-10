package com.example.hospital.patient.wx.api.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public interface FaceAuthService {
    Boolean hasFaceAuthInDay(Map<String, Object> param);

    void createFaceMode(Map<String, Object> map);

    @Transactional
    boolean verifyFaceModel(int userId, String photo);
}
