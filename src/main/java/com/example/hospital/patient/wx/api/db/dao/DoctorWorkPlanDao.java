package com.example.hospital.patient.wx.api.db.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DoctorWorkPlanDao {

    List<String> searchCanRegisterInDateRange(Map<String,Object> param);


    List<Map<String,Object>> searchDeptSubDoctorPlanInDay(Map<String,Object> param);

    int updateNumById(Map<String,Object> param);
}




