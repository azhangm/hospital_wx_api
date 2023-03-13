package com.example.hospital.patient.wx.api.service;

import java.util.Map;

public interface DoctorService {
    Map<String,Object> searchDoctorInfoById(Integer id);
}
