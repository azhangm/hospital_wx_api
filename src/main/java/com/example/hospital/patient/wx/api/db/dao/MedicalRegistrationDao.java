package com.example.hospital.patient.wx.api.db.dao;


import java.util.Map;

public interface MedicalRegistrationDao {

    long searchRegistrationCountInToday(Map<String,Object> param);

     Integer hasRegisterRecordInDay(Map<String,Object> param);
}




