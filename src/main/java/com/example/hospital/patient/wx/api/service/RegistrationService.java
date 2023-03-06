package com.example.hospital.patient.wx.api.service;

import java.util.List;
import java.util.Map;

public interface RegistrationService {
     List<Map<String, Object>> searchCanRegisterInDateRange(Map<String,Object> param);
    List<Map<String,Object>> searchDeptSubDoctorPlanInDay(Map<String,Object> param);
}
