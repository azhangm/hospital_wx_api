package com.example.hospital.patient.wx.api.service;

import java.util.Map;

public interface FaceAuthService {
    Boolean hasFaceAuthInDay(Map<String, Object> param);
}
