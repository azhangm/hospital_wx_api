package com.example.hospital.patient.wx.api.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface RegistrationService {
     List<Map<String, Object>> searchCanRegisterInDateRange(Map<String,Object> param);

    List<Map<String,Object>> searchDeptSubDoctorPlanInDay(Map<String,Object> param);

    String checkRegisterCondition(Map<String,Object> param);

    List<Map<String,Object>> searchDoctorWorkPlanSchedule(Map<String,Object> param);

    Map<String,Object> registerMedicalAppointment(Map<String,Object> param);

    @Transactional
    void updatePayment(Map<String, Object> param);
}
