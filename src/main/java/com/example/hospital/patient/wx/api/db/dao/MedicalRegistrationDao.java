package com.example.hospital.patient.wx.api.db.dao;


import com.example.hospital.patient.wx.api.db.pojo.MedicalRegistrationEntity;

import java.util.Map;

public interface MedicalRegistrationDao {

    long searchRegistrationCountInToday(Map<String,Object> param);

     Integer hasRegisterRecordInDay(Map<String,Object> param);

     int insert(MedicalRegistrationEntity entity);

    int discardPayment(String outTradeNo);

    Map<String,Object> searchWorkPlanIdAndScheduleId(String outTradeNo);
}




